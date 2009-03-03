package de.jreality.audio;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.jreality.math.Matrix;
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
	
	private float gain = DEFAULT_GAIN;
	private float speedOfSound = DEFAULT_SPEED_OF_SOUND;
	private DistanceCue distanceCue = DEFAULT_DISTANCE_CUE;
	private static final float UPDATE_CUTOFF = 6f; // play with this parameter if audio gets choppy
	
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
	
	private float[] currentFrame = null;
	private int currentLength = 0;
	private int currentIndex = 0;
	private float previousSample = 0f, currentSample = 0f;
	private int relativeTime = 0;
	private int frameCount = 0;

	
	public DelayPath(SampleReader reader, int sampleRate) {
		if (sampleRate<=0) {
			throw new IllegalArgumentException("sample rate must be positive");
		}
		if (reader==null) {
			throw new IllegalArgumentException("reader cannot be null");
		}
		this.reader = ConvertingReader.createReader(reader, sampleRate);
		this.sampleRate = sampleRate;
	
		xFilter = new LowPassFilter(sampleRate, UPDATE_CUTOFF);
		yFilter = new LowPassFilter(sampleRate, UPDATE_CUTOFF);
		zFilter = new LowPassFilter(sampleRate, UPDATE_CUTOFF);
		
		updateParameters();
	}

	private List<Class<? extends DistanceCue>> oldChain = null;
	
	public synchronized void setProperties(EffectiveAppearance eapp) {
		gain = eapp.getAttribute(VOLUME_GAIN_KEY, DEFAULT_GAIN);
		speedOfSound = eapp.getAttribute(SPEED_OF_SOUND_KEY, DEFAULT_SPEED_OF_SOUND);
		updateParameters();
		
		List<Class<? extends DistanceCue>> newChain = (List<Class<? extends DistanceCue>>) eapp.getAttribute(DISTANCE_CUE_KEY, null, List.class);
		if (!newChain.equals(oldChain)) {
			try {
				oldChain = newChain;
				DistanceCue c = DistanceCueChain.create(newChain);
				c.setSampleRate(sampleRate);
				distanceCue = c;
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void updateParameters() {
		gamma = (speedOfSound>0f) ? sampleRate/speedOfSound : 0f; // samples per distance
	}
	
	private float[] newFrame = null;
	
	public boolean processFrame(SoundEncoder enc, int frameSize, Matrix sourcePos, Matrix invMicPos) {
		if (newFrame==null || newFrame.length<frameSize) {
			newFrame = new float[frameSize];
		}
		if (reader.read(newFrame, 0, frameSize)>0) {
			sourceFrames.add(newFrame);
			newFrame = null;
			frameCount++;
		} else {
			sourceFrames.add(null);
		}
		frameLengths.add(frameSize);
		
		sourcePositions.add(new Matrix(sourcePos));
		currentMicPosition = invMicPos;
		
		updateTarget();
		
		if (currentLength>0) {
			encodeFrame(enc, frameSize);
		} else {
			initFields();
		}
	
		if (frameCount==0 && currentFrame==null && !distanceCue.hasMore()) {
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
	
	private void encodeFrame(SoundEncoder enc, int frameSize) {
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
				previousSample = currentSample;
				float newSample = (currentFrame!=null) ? currentFrame[currentIndex] : 0f;
				currentSample = distanceCue.nextValue(newSample*gain, distance);
			}

			float v = previousSample+fractionalTime*(currentSample-previousSample);
			enc.encodeSample(v, j, xCurrent, yCurrent, zCurrent);

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
		
		// TODO: Adjust the next three lines to generalize to curved geometries
		xTarget = (float) auxiliaryMatrix.getEntry(0, 3);
		yTarget = (float) auxiliaryMatrix.getEntry(1, 3);
		zTarget = (float) auxiliaryMatrix.getEntry(2, 3);
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
		previousSample = 0;
		currentSample = 0f;
		distanceCue.reset();
	}
}
