package de.jreality.audio;

import java.util.LinkedList;
import java.util.Queue;

import de.jreality.math.Matrix;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 * Sound paths with delay (for Doppler shifts and such).
 * 
 * TODO: possibly improve interpolation, handle curved geometries, optimize?
 * 
 * @author brinkman
 *
 */
public class DelayPath implements SoundPath {
	
	private float gain = DEFAULT_GAIN;
	private float speedOfSound = DEFAULT_SPEED_OF_SOUND;
	private float updateCutOff = DEFAULT_UPDATE_CUTOFF;
	private Attenuation attenuation = DEFAULT_ATTENUATION;
	
	private SampleReader reader;
	private int sampleRate;
	private float gamma;
	private Queue<float[]> sourceFrames = new LinkedList<float[]>();
	private Queue<Float> xSrcQueue = new LinkedList<Float>(), ySrcQueue = new LinkedList<Float>(), zSrcQueue = new LinkedList<Float>();
	
	private LowPassFilter xLpfSrc = new LowPassFilter(), yLpfSrc = new LowPassFilter(), zLpfSrc = new LowPassFilter();
	private LowPassFilter xLpfMic = new LowPassFilter(), yLpfMic = new LowPassFilter(), zLpfMic = new LowPassFilter();
	
	private float xSrcPrev, ySrcPrev, zSrcPrev;
	private float x0Src, y0Src, z0Src;
	private float x1Src, y1Src, z1Src;
	private float dxSrc, dySrc, dzSrc;
	
	private float xMic, yMic, zMic;
	
	private int relativeTime = 0;
	private float[] currentFrame = null;
	private boolean firstFrame = true;
	

	public DelayPath(SampleReader reader, int sampleRate) {
		this.reader = reader;
		this.sampleRate = sampleRate;
		updateParameters();
	}
	
	public int processFrame(SoundEncoder enc, int frameSize, Matrix sourcePos, Matrix invMicPos) {
		float[] newFrame = new float[frameSize];
		int nRead = reader.read(newFrame, 0, frameSize);
		sourceFrames.add(newFrame);
	
		float alpha = LowPassFilter.filterCoefficient(sampleRate, frameSize, updateCutOff);
		
		xSrcQueue.add(xLpfSrc.nextValue((float) sourcePos.getEntry(0, 3), alpha));
		ySrcQueue.add(yLpfSrc.nextValue((float) sourcePos.getEntry(1, 3), alpha));
		zSrcQueue.add(zLpfSrc.nextValue((float) sourcePos.getEntry(2, 3), alpha));
		
		float x1Mic = xLpfMic.nextValue((float) invMicPos.getEntry(0, 3), alpha);
		float y1Mic = yLpfMic.nextValue((float) invMicPos.getEntry(1, 3), alpha);
		float z1Mic = zLpfMic.nextValue((float) invMicPos.getEntry(2, 3), alpha);
	
		if (currentFrame==null) { // first or second call?
			if (firstFrame) { // first call?
				firstFrame = false;
				
				advanceSourcePosition();

				xMic = x1Mic;
				yMic = y1Mic;
				zMic = z1Mic;
				
				return nRead;
			}

			advanceSourceFrame();
			
			xSrcPrev = x0Src;
			ySrcPrev = y0Src;
			zSrcPrev = z0Src;
		}
	
		float dxMic = (x1Mic-xMic)/frameSize;
		float dyMic = (y1Mic-yMic)/frameSize;
		float dzMic = (z1Mic-zMic)/frameSize;

		for(int j = 0; j<frameSize; j++) {
			encodeSample(enc, j);
			
			xMic += dxMic;
			yMic += dyMic;
			zMic += dzMic;
			
			relativeTime++;
		}

		return nRead;
	}

	public void setFromEffectiveAppearance(EffectiveAppearance eapp) {
		gain = eapp.getAttribute(VOLUME_GAIN_KEY, DEFAULT_GAIN);
		speedOfSound = eapp.getAttribute(SPEED_OF_SOUND_KEY, DEFAULT_SPEED_OF_SOUND);
		updateCutOff = eapp.getAttribute(UPDATE_CUT_OFF_KEY, DEFAULT_UPDATE_CUTOFF);
		attenuation = (Attenuation) eapp.getAttribute(VOLUME_ATTENUATION_KEY, DEFAULT_ATTENUATION);
		updateParameters();
	}

	private void updateParameters() {
		gamma = sampleRate/speedOfSound; // samples per distance
	}
	
	private void advanceSourceFrame() {
		currentFrame = sourceFrames.remove();
		
		advanceSourcePosition();
		
		dxSrc = (x1Src-x0Src)/currentFrame.length;
		dySrc = (y1Src-y0Src)/currentFrame.length;
		dzSrc = (z1Src-z0Src)/currentFrame.length;
	}

	private void advanceSourcePosition() {
		x0Src = x1Src;
		y0Src = y1Src;
		z0Src = z1Src;
		
		x1Src = xSrcQueue.remove();
		y1Src = ySrcQueue.remove();
		z1Src = zSrcQueue.remove();
	}
	
	private float[] nextFrame() {
		return sourceFrames.element();
	}
	
	private void encodeSample(SoundEncoder enc, int j) {
		float time = sourceTime();
		if (time<0f) {
			return; // too early to start rendering
		}
		
		int index = (int) time;
		float fractionalTime = time-index;
		
		float v0 = currentFrame[index++];
		float v1 = (index<currentFrame.length) ? currentFrame[index] : nextFrame()[0];
		float v = v0+fractionalTime*(v1-v0);
		
		xSrcPrev = x0Src+dxSrc*time;
		ySrcPrev = y0Src+dySrc*time;
		zSrcPrev = z0Src+dzSrc*time;

		enc.encodeSample(v*gain, j, xSrcPrev+xMic, ySrcPrev+yMic, zSrcPrev+zMic, attenuation);
	}

	private float sourceTime() {
		while (true) {
			float time;
			if (gamma>1e-6f) {  // positive speed of sound?
				float x = xSrcPrev+xMic;
				float y = ySrcPrev+yMic;
				float z = zSrcPrev+zMic;
				
				time = (float) (relativeTime-Math.sqrt(x*x+y*y+z*z)*gamma+0.5);
			} else {  // nonpositive speed of sound means instantaneous propagation
				time = relativeTime;
			}
			
			if (time<currentFrame.length) {
				return time;
			}
			
			relativeTime -= currentFrame.length;
			advanceSourceFrame();
		}
	}
}
