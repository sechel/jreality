package de.jreality.audio;

import java.util.LinkedList;
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
	private Attenuation attenuation = DEFAULT_ATTENUATION;
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

	public void setProperties(EffectiveAppearance eapp) {
		gain = eapp.getAttribute(VOLUME_GAIN_KEY, DEFAULT_GAIN);
		speedOfSound = eapp.getAttribute(SPEED_OF_SOUND_KEY, DEFAULT_SPEED_OF_SOUND);
		attenuation = (Attenuation) eapp.getAttribute(VOLUME_ATTENUATION_KEY, DEFAULT_ATTENUATION, Attenuation.class);
		
		updateParameters();
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
	
		if (frameCount==0 && currentFrame==null) {
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
			float dist = (float) Math.sqrt(xCurrent*xCurrent+yCurrent*yCurrent+zCurrent*zCurrent);
			float time;
			while ((time = relativeTime+j-gamma*dist+0.5f)>=currentLength) {
				advanceFrame();
				updateTarget();
			}

			if (currentFrame!=null && time>=0f) {
				int index = (int) time;
				float fractionalTime = time-index;

				float v0 = currentFrame[index++];
				float v1;
				if (index<currentLength) {
					v1 = currentFrame[index];
				} else {
					float[] nextFrame = sourceFrames.peek();
					v1 = (nextFrame!=null) ? nextFrame[0] : 0;
				}
				float v = v0+fractionalTime*(v1-v0);

				enc.encodeSample(attenuation.attenuate(v*gain, dist), j, xCurrent, yCurrent, zCurrent);
			}
			
			xCurrent = xFilter.nextValue(xTarget);
			yCurrent = yFilter.nextValue(yTarget);
			zCurrent = zFilter.nextValue(zTarget);
		}
		relativeTime += frameSize;
	}

	private void advanceFrame() {
		relativeTime -= currentLength;
		
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
		relativeTime = 0;
		frameCount = 0;
	}
}
