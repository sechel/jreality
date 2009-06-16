package de.jreality.audio;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.smartcardio.CommandAPDU;

import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.scene.data.SampleReader;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 * Sound path with delay (for Doppler shifts and such)
 * 
 * Compensates for discrepancies between video and audio frame rate
 * by low-pass filtering position information
 * 
 * @author brinkman
 *
 */
public class DelayPath implements SoundPath {

	public static final Factory FACTORY = new Factory() {
		public SoundPath newSoundPath() {
			return new DelayPath();
		}
	};

	private DistanceCueFactory directedFactory = AudioAttributes.DEFAULT_DISTANCE_CUE_FACTORY;
	private DistanceCueFactory directionlessFactory = AudioAttributes.DEFAULT_DISTANCE_CUE_FACTORY;
	private SampleProcessorFactory preProcFactory = AudioAttributes.DEFAULT_PROCESSOR_FACTORY;

	private DistanceCue distanceCue = directedFactory.getInstance();
	private DistanceCue directionlessCue = directionlessFactory.getInstance();
	private SampleProcessor preProcessor = preProcFactory.getInstance();

	private float gain = AudioAttributes.DEFAULT_GAIN;
	private float directionlessGain = AudioAttributes.DEFAULT_DIRECTIONLESS_GAIN;
	private float speedOfSound = AudioAttributes.DEFAULT_SPEED_OF_SOUND;
	private float updateCutoff = AudioAttributes.DEFAULT_UPDATE_CUTOFF;
	private int earshot = AudioAttributes.DEFAULT_EARSHOT;
	private boolean withinEarshot = true;
	private int samplesOutOfEarshot = 0;

	private SampleReader reader;
	private int sampleRate;
	private float gamma;

	private Queue<float[]> sourceFrames = new LinkedList<float[]>();
	private Queue<Integer> frameLengths = new LinkedList<Integer>();
	private Queue<Matrix> sourcePositions = new LinkedList<Matrix>();
	private Matrix currentMicPosition, currentSourcePosition;

	private LowPassFilter xFilter, yFilter, zFilter, wFilter;
	private float xTarget, yTarget, zTarget, wTarget;
	private float xCurrent, yCurrent, zCurrent, wCurrent;
	private float xMic, yMic, zMic, wMic;

	private float[] currentFrame = null;
	private int currentLength = 0;
	private int currentIndex = 0;
	private int relativeTime = 0;
	private int frameCount = 0;
	private int metric = Pn.EUCLIDEAN;

	private Interpolation interpolation;


	public void initialize(SampleReader reader, Interpolation.Factory factory) {
		this.reader = reader;
		sampleRate = reader.getSampleRate();
		distanceCue.setSampleRate(sampleRate);
		directionlessCue.setSampleRate(sampleRate);
		preProcessor.initialize(reader);
		interpolation = factory.newInterpolation();

		xFilter = new LowPassFilter(sampleRate);
		yFilter = new LowPassFilter(sampleRate);
		zFilter = new LowPassFilter(sampleRate);

		updateParameters();
	}

	// consider synchronization when changing this method...
	public void setProperties(EffectiveAppearance app) {
		gain = app.getAttribute(AudioAttributes.VOLUME_GAIN_KEY, AudioAttributes.DEFAULT_GAIN);
		directionlessGain = app.getAttribute(AudioAttributes.DIRECTIONLESS_GAIN_KEY, AudioAttributes.DEFAULT_DIRECTIONLESS_GAIN);
		speedOfSound = app.getAttribute(AudioAttributes.SPEED_OF_SOUND_KEY, AudioAttributes.DEFAULT_SPEED_OF_SOUND);
		updateCutoff = app.getAttribute(AudioAttributes.UPDATE_CUTOFF_KEY, AudioAttributes.DEFAULT_UPDATE_CUTOFF);
		earshot = app.getAttribute(AudioAttributes.EARSHOT_KEY, AudioAttributes.DEFAULT_EARSHOT);
		metric = app.getAttribute(CommonAttributes.METRIC, Pn.EUCLIDEAN);
		updateParameters();

		SampleProcessorFactory spf = (SampleProcessorFactory) app.getAttribute(AudioAttributes.PREPROCESSOR_KEY,
				AudioAttributes.DEFAULT_PROCESSOR_FACTORY, SampleProcessorFactory.class);
		if (spf!=preProcFactory) {
			preProcFactory = spf;
			SampleProcessor proc = spf.getInstance();
			proc.initialize(reader);
			preProcessor = proc;
		}

		DistanceCueFactory dcf = (DistanceCueFactory) app.getAttribute(AudioAttributes.DISTANCE_CUE_KEY,
				AudioAttributes.DEFAULT_DISTANCE_CUE_FACTORY, DistanceCueFactory.class);
		if (dcf!=directedFactory) {
			directedFactory = dcf;
			DistanceCue dc = dcf.getInstance();
			dc.setSampleRate(sampleRate);
			distanceCue = dc;
		}

		dcf = (DistanceCueFactory) app.getAttribute(AudioAttributes.DIRECTIONLESS_CUE_KEY,
				AudioAttributes.DEFAULT_DISTANCE_CUE_FACTORY, DistanceCueFactory.class);
		if (dcf!=directionlessFactory) {
			directionlessFactory = dcf;
			DistanceCue dc = dcf.getInstance();
			dc.setSampleRate(sampleRate);
			directionlessCue = dc;
		}

		preProcessor.setProperties(app);
		distanceCue.setProperties(app);
		directionlessCue.setProperties(app);
	}

	private void updateParameters() {
		gamma = (speedOfSound>0f) ? sampleRate/speedOfSound : 0f; // samples per distance
		if (updateCutoff!=xFilter.getCutOff()) {
			xFilter.setCutOff(updateCutoff);
			yFilter.setCutOff(updateCutoff);
			zFilter.setCutOff(updateCutoff);
		}
	}
	
	public boolean processFrame(SoundEncoder enc, int frameSize, Matrix sourcePosition, Matrix inverseMicMatrix, float[] directionlessBuffer) {
		currentMicPosition = inverseMicMatrix;
		currentSourcePosition = sourcePosition;
		updateTarget();
		
		boolean sourceActive = evaluateSourceFrame(frameSize);
		encodeFrame(enc, frameSize, directionlessBuffer);

		if (sourceActive || frameCount>0 || currentFrame!=null || 
				preProcessor.hasMore() || distanceCue.hasMore() || directionlessCue.hasMore()) {
			return true;   // still rendering...
		} else {
			reset();
			return false;  // nothing left to render
		}
	}

	private boolean evaluateSourceFrame(int frameSize) {
		float[] newFrame = getBuffer(frameSize);
		int nRead = preProcessor.read(newFrame, 0, frameSize);
		if (withinEarshot) { // within earshot: render audio normally
			if (nRead>0) {
				frameCount++;
				if (nRead<frameSize) {
					Arrays.fill(newFrame, nRead, frameSize, 0);
				}
				queueFrame(frameSize, newFrame);
			} else {
				reuseBuffer(newFrame);
				queueFrame(frameSize, null);
			}
			withinEarshot = (earshot<=0 || relativeTime<earshot+4*frameSize);
		} else { // out of earshot: render null frames
			reuseBuffer(newFrame);
			samplesOutOfEarshot += frameSize;
			withinEarshot = (relativeTime<earshot);
			if (withinEarshot || 2*samplesOutOfEarshot/frameSize>relativeTime/earshot) {
				queueNullFrame();
			}
		}
		return nRead>0;
	}

	private void queueNullFrame() {
		queueFrame(samplesOutOfEarshot, null);
		samplesOutOfEarshot = 0;
	}

	private void queueFrame(int size, float[] frame) {
		sourcePositions.add(new Matrix(currentSourcePosition));
		if (currentLength>0) {
			frameLengths.add(size);
			sourceFrames.add(frame);
		} else {
			currentLength = size;
			currentFrame = frame;

			xCurrent = xFilter.initialize(xTarget);
			yCurrent = yFilter.initialize(yTarget);
			zCurrent = zFilter.initialize(zTarget);
		}
	}

	private Matrix auxiliaryMatrix = new Matrix();

	private void updateTarget() {
		auxiliaryMatrix.assignFrom(sourcePositions.isEmpty() ? currentSourcePosition : sourcePositions.element());
		auxiliaryMatrix.multiplyOnLeft(currentMicPosition);

		// TODO: Adjust the rest of this method to generalize to curved geometries
		xTarget = (float) auxiliaryMatrix.getEntry(0, 3);
		yTarget = (float) auxiliaryMatrix.getEntry(1, 3);
		zTarget = (float) auxiliaryMatrix.getEntry(2, 3);
		wTarget = (float) auxiliaryMatrix.getEntry(3, 3);

		auxiliaryMatrix.invert();
		xMic = (float) auxiliaryMatrix.getEntry(0, 3);
		yMic = (float) auxiliaryMatrix.getEntry(1, 3);
		zMic = (float) auxiliaryMatrix.getEntry(2, 3);
		wMic = (float) auxiliaryMatrix.getEntry(3, 3);
	}

	private void advanceFrame() {
		if (currentFrame!=null) {
			reuseBuffer(currentFrame);
		}
		
		currentFrame = sourceFrames.remove();
		currentLength = frameLengths.remove();
		sourcePositions.remove();
		updateTarget();

		if (currentFrame!=null) {
			frameCount--;
		}
	}

	private void encodeFrame(SoundEncoder enc, int frameSize, float[] directionlessBuffer) {
		for(int j=0; j<frameSize; j++) {
			double[] currentPos = new double[]{xCurrent,yCurrent,zCurrent,wCurrent};
			double norm = Pn.distanceBetween(currentPos, Pn.originP3, metric);
			float distance = (float) norm; //(float) Math.sqrt(xCurrent*xCurrent+yCurrent*yCurrent+zCurrent*zCurrent);
			float time = (relativeTime++)-gamma*distance;
			int targetIndex = (int) time;
			float fractionalTime = time-targetIndex;

			for(; targetIndex>=currentIndex; currentIndex++) {
				if (currentIndex>=currentLength) {
					relativeTime -= currentLength;
					currentIndex -= currentLength;
					targetIndex -= currentLength;
					advanceFrame();
				}
				float newSample = (currentFrame!=null) ? currentFrame[currentIndex]*gain : 0f;
				interpolation.put(directionlessCue.nextValue(newSample, distance, xMic, yMic, zMic));
			}

			float v = interpolation.get(fractionalTime);
			enc.encodeSample(distanceCue.nextValue(v, distance, xMic, yMic, zMic), j, xCurrent, yCurrent, zCurrent);
			if (directionlessBuffer!=null) {
				directionlessBuffer[j] += v*directionlessGain;
			}

			xCurrent = xFilter.nextValue(xTarget);
			yCurrent = yFilter.nextValue(yTarget);
			zCurrent = zFilter.nextValue(zTarget);
		}
	}

	private void reset() {
		sourceFrames.clear();
		frameLengths.clear();
		sourcePositions.clear();
		currentFrame = null;
		currentLength = 0;
		currentIndex = 0;
		relativeTime = 0;
		frameCount = 0;
		withinEarshot = true;
		samplesOutOfEarshot = 0;
		interpolation.reset();
		preProcessor.clear();
		distanceCue.reset();
		directionlessCue.reset();
	}

	private static final Map<Integer, Queue<WeakReference<float[]>>> framePool = new HashMap<Integer, Queue<WeakReference<float[]>>>();

	private static void reuseBuffer(float[] frame) {
		int size = frame.length;
		synchronized(framePool) {
			Queue<WeakReference<float[]>> queue = framePool.get(size);
			if (queue==null) {
				queue = new LinkedList<WeakReference<float[]>>();
				framePool.put(size, queue);
			}
			queue.add(new WeakReference<float[]>(frame));
		}
	}

	private static float[] getBuffer(int size) {
		synchronized (framePool) {
			Queue<WeakReference<float[]>> queue = framePool.get(size);
			if (queue!=null) {
				while (!queue.isEmpty()) {
					float[] frame = queue.remove().get();
					if (frame!=null) {
						return frame;
					}
				}
			}
		}
		return new float[size];
	}
}
