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

	private float gain = 1f;
	private float speedOfSound = 300f;
	private float updateCutOff = 10f;
	
	private SampleReader reader;
	private int sampleRate;
	private float gamma;
	
	private Queue<float[]> frames = new LinkedList<float[]>();
	private Queue<Float> xSrc = new LinkedList<Float>();
	private Queue<Float> ySrc = new LinkedList<Float>();
	private Queue<Float> zSrc = new LinkedList<Float>();
	
	private LowPassFilter xLpfSrc = new LowPassFilter(), yLpfSrc = new LowPassFilter(), zLpfSrc = new LowPassFilter();
	private LowPassFilter xLpfMic = new LowPassFilter(), yLpfMic = new LowPassFilter(), zLpfMic = new LowPassFilter();
	
	private float x0Src, y0Src, z0Src;
	private float x1Src, y1Src, z1Src;
	private float dxSrc, dySrc, dzSrc;
	private float xMic, yMic, zMic;
	
	private int relativeTime = 0;
	private float[] currentFrame = null;

	
	public DelayPath(SampleReader reader, int sampleRate) {
		this.reader = reader;
		this.sampleRate = sampleRate;
		update();
	}
	
	public void setFromEffectiveAppearance(EffectiveAppearance eapp) {
		gain = eapp.getAttribute("volumeCoefficient", 1f);
		speedOfSound = eapp.getAttribute("speedOfSound", 300f);
		updateCutOff = eapp.getAttribute("updateCutOff", 10f);
		update();
	}

	private void update() {
		gamma = speedOfSound/sampleRate;
	}
	
	public int processFrame(SoundEncoder enc, int frameSize, Matrix sourcePos, Matrix invMicPos) {
		float alpha = LowPassFilter.filterCoefficient(sampleRate, frameSize, updateCutOff);
		
		float x1Mic = xLpfMic.nextValue((float) invMicPos.getEntry(0, 3), alpha);
		float y1Mic = yLpfMic.nextValue((float) invMicPos.getEntry(1, 3), alpha);
		float z1Mic = zLpfMic.nextValue((float) invMicPos.getEntry(2, 3), alpha);
	
		xSrc.add(xLpfSrc.nextValue((float) sourcePos.getEntry(0, 3), alpha));
		ySrc.add(yLpfSrc.nextValue((float) sourcePos.getEntry(1, 3), alpha));
		zSrc.add(zLpfSrc.nextValue((float) sourcePos.getEntry(2, 3), alpha));

		if (currentFrame==null) {
			advanceFrame();
			xMic = x1Mic;
			yMic = y1Mic;
			zMic = z1Mic;
		}
		
		float[] newFrame = new float[frameSize];
		int nRead = reader.read(newFrame, 0, frameSize);
		frames.add(newFrame);

		if (currentFrame!=null) {
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
		}

		return nRead;
	}

	private void advanceFrame() {
		if (currentFrame!=null) {
			relativeTime -= currentFrame.length;
		}
		
		currentFrame = frames.poll(); // frames is empty when this method is called for the first time
		
		x0Src = x1Src;
		y0Src = y1Src;
		z0Src = z1Src;
		
		x1Src = xSrc.remove();
		y1Src = ySrc.remove();
		z1Src = zSrc.remove();

		if (currentFrame!=null) {
			dxSrc = (x1Src-x0Src)/currentFrame.length;
			dySrc = (y1Src-y0Src)/currentFrame.length;
			dzSrc = (z1Src-z0Src)/currentFrame.length;
		}
	}
	
	private float[] nextFrame() {
		return frames.element();
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
		
		float x = xMic+x0Src+dxSrc*time;
		float y = yMic+y0Src+dySrc*time;
		float z = zMic+z0Src+dzSrc*time;

		enc.encodeSample(v*gain, j, x, y, z);
	}

	private float sourceTime() {
		while (true) {
			float d0 = distFromMic(x0Src, y0Src, z0Src);
			float d1 = distFromMic(x1Src, y1Src, z1Src);
			
			float time = (relativeTime*gamma-d0)/(gamma+(d1-d0)/currentFrame.length)+0.5f; // fudge factor to deal with roundoff errors

			if (time<currentFrame.length) {
				return time;
			}
			
			advanceFrame();
		}
	}

	private float distFromMic(float xs, float ys, float zs) {
		float x = xMic+xs;
		float y = yMic+ys;
		float z = zMic+zs;
		
		return (float) Math.sqrt(x*x+y*y+z*z);
	}
}
