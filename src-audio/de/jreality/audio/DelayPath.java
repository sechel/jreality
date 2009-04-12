package de.jreality.audio;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import de.jreality.math.Matrix;
import de.jreality.scene.data.SampleReader;
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

	private SampleReader reader;
	private int sampleRate;
	private float gamma;

	private Queue<float[]> sourceFrames = new LinkedList<float[]>();
	private Queue<Integer> frameLengths = new LinkedList<Integer>();
	private Queue<Matrix> sourcePositions = new LinkedList<Matrix>();
	private Matrix currentMicPosition;

	private LowPassFilter xFilter, yFilter, zFilter; // TODO: consider better interpolation
	private float xTarget, yTarget, zTarget;
	private float xCurrent, yCurrent, zCurrent;
	private float xMic, yMic, zMic;

	private float[] currentFrame = null;
	private int currentLength = 0;
	private int currentIndex = 0;
	private int relativeTime = 0;
	private int frameCount = 0;
	
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

	private float[] newFrame = null;

	public boolean processFrame(SoundEncoder enc, int frameSize, Matrix sourcePos, Matrix inverseMicMatrix, float[] directionlessBuffer) {
		if (newFrame==null || newFrame.length<frameSize) {
			newFrame = new float[frameSize];
		}
		int nRead = preProcessor.read(newFrame, 0, frameSize);
		if (nRead>0) {
			if (nRead<frameSize) {
				Arrays.fill(newFrame, nRead, frameSize, 0);
			}
			sourceFrames.add(newFrame);
			newFrame = null;
			frameCount++;
		} else {
			sourceFrames.add(null);
		}
		frameLengths.add(frameSize);

		sourcePositions.add(new Matrix(sourcePos));
		currentMicPosition = inverseMicMatrix;

		updateTarget();

		if (currentLength>0) {
			encodeFrame(enc, frameSize, directionlessBuffer);
		} else {
			initFields();
		}

		if (frameCount<=0 && currentFrame==null && !distanceCue.hasMore() && !directionlessCue.hasMore()) {
			reset();
			return false;  // nothing left to render
		} else {
			return true;   // still rendering...
		}
	}

	private void initFields() {
		advanceFrame();

		xCurrent = xFilter.initialize(xTarget);
		yCurrent = yFilter.initialize(yTarget);
		zCurrent = zFilter.initialize(zTarget);
	}

	private void encodeFrame(SoundEncoder enc, int frameSize, float[] directionlessBuffer) {
		for(int j=0; j<frameSize; j++) {
			float distance = (float) Math.sqrt(xCurrent*xCurrent+yCurrent*yCurrent+zCurrent*zCurrent);
			float time = (relativeTime++)-gamma*distance;
			int targetIndex = (int) time;
			float fractionalTime = time-targetIndex;

			for(; targetIndex>=currentIndex; currentIndex++) {
				if (currentIndex>=currentLength) {
					relativeTime -= currentLength;
					currentIndex -= currentLength;
					targetIndex -= currentLength;
					advanceFrame();
					updateTarget();
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

	private void advanceFrame() {
		currentFrame = sourceFrames.remove();
		currentLength = frameLengths.remove();
		sourcePositions.remove();

		if (currentFrame!=null) {
			frameCount--;
		}
	}

	private Matrix auxiliaryMatrix = new Matrix();

	private void updateTarget() {
		auxiliaryMatrix.assignFrom(sourcePositions.element());
		auxiliaryMatrix.multiplyOnLeft(currentMicPosition);

		// TODO: Adjust the rest of this method to generalize to curved geometries
		xTarget = (float) auxiliaryMatrix.getEntry(0, 3);
		yTarget = (float) auxiliaryMatrix.getEntry(1, 3);
		zTarget = (float) auxiliaryMatrix.getEntry(2, 3);
		
		auxiliaryMatrix.invert();
		xMic = (float) auxiliaryMatrix.getEntry(0, 3);
		yMic = (float) auxiliaryMatrix.getEntry(1, 3);
		zMic = (float) auxiliaryMatrix.getEntry(2, 3);
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
		interpolation.reset();
		preProcessor.clear();
		distanceCue.reset();
		directionlessCue.reset();
	}
}
