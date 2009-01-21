package de.jreality.audio;

import java.util.LinkedList;
import java.util.Queue;

import de.jreality.math.Matrix;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 * Rough first draft of sound paths with delay (for Doppler shifts and such).
 * 
 * TODO: possibly improve interpolation, optimize?
 * 
 * @author brinkman
 *
 */
public class DelayPath implements SoundPath {

	private boolean attenuate = false;
	private float gain = 1f;
	private float speedOfSound = 300f;
	private float updateCutOff = 10f;
	
	private int sampleRate;
	private float gamma, gamma2;
	
	private Queue<float[]> frames = new LinkedList<float[]>();
	private Queue<Float> xSrc = new LinkedList<Float>();
	private Queue<Float> ySrc = new LinkedList<Float>();
	private Queue<Float> zSrc = new LinkedList<Float>();
	
	private LPF xLpfSrc = new LPF(), yLpfSrc = new LPF(), zLpfSrc = new LPF();
	private LPF xLpfMic = new LPF(), yLpfMic = new LPF(), zLpfMic = new LPF();
	
	private float x0Src, y0Src, z0Src;
	private float x1Src, y1Src, z1Src;
	private float dxSrc, dySrc, dzSrc;
	private float xMic, yMic, zMic;
	
	private int relativeTime = 0;
	private float[] currentFrame = null;

	private class LPF { // crude low-pass filter to smooth out difference between frame rate and video update rate
		float value;
		boolean firstValue = true;
		
		float nextValue(float v, float alpha) { // coefficient alpha cannot be static because it may differ across backends
			if (firstValue) {
				firstValue = false;
				value = v;
			} else {
				value += alpha*(v-value);
			}
			return value;
		}
	}
	
	public DelayPath(int sampleRate) {
		this.sampleRate = sampleRate;
		gamma = sampleRate/speedOfSound;
	}
	
	public int processFrame(SampleReader reader, SoundEncoder enc, int frameSize, Matrix sourcePos, Matrix invMicPos) {
		float frameRate = ((float) sampleRate)/frameSize;
		float tau = (float) (1/(2*Math.PI*updateCutOff));
		float alpha = 1/(1+tau*frameRate); // see LowPassFilter.java for details
		
		float x1Mic = xLpfMic.nextValue((float) invMicPos.getEntry(0, 3), alpha);
		float y1Mic = yLpfMic.nextValue((float) invMicPos.getEntry(1, 3), alpha);
		float z1Mic = zLpfMic.nextValue((float) invMicPos.getEntry(2, 3), alpha);
	
		xSrc.add(xLpfSrc.nextValue((float) sourcePos.getEntry(0, 3), alpha));
		ySrc.add(yLpfSrc.nextValue((float) sourcePos.getEntry(1, 3), alpha));
		zSrc.add(zLpfSrc.nextValue((float) sourcePos.getEntry(2, 3), alpha));

		if (currentFrame==null) {
			advanceFrame();
		}
		
		float[] newFrame = new float[frameSize];
		int nRead = reader.read(newFrame, 0, frameSize);
		frames.add(newFrame);

		if (currentFrame==null) {
			xMic = x1Mic;
			yMic = y1Mic;
			zMic = z1Mic;
		} else {
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
			gamma2 = gamma/currentFrame.length;
		}
	}
	
	private void encodeSample(SoundEncoder enc, int j) {
		float st;
		while (true) {
			st = sourceTime()+0.5f; // fudge factor to deal with roundoff errors
			if (st<0) {
				return;  // too early to start rendering
			}
			if (st<currentFrame.length) {
				break;
			}
			advanceFrame();
		}
		
		int idx = (int) st;
		float localTime = st-idx;

		float v0 = currentFrame[idx++];
		float v1;
		
		if (idx<currentFrame.length) {
			v1 = currentFrame[idx];
		} else {
			advanceFrame();
			v1 = currentFrame[0];
		}
		
		float v = v0+localTime*(v1-v0);
		
		float x = xMic+x0Src+dxSrc*st;
		float y = yMic+y0Src+dySrc*st;
		float z = zMic+z0Src+dzSrc*st;
		float r = (float) (norm(x, y, z)+1e-5);
		
		if (attenuate) {
			v /= r;
		}
		
		enc.encodeSample(v*gain, j, x/r, y/r, z/r, r);
	}

	private float sourceTime() {
		float d0 = norm(xMic+x0Src, yMic+y0Src, zMic+z0Src);
		float d1 = norm(xMic+x1Src, yMic+y1Src, zMic+z1Src);
		return (relativeTime-gamma*d0)/(1+gamma2*(d1-d0));
	}

	private float norm(float x, float y, float z) {
		return (float) Math.sqrt(x*x+y*y+z*z);
	}
	
	public void setFromEffectiveAppearance(EffectiveAppearance eapp) {
		attenuate = eapp.getAttribute("volumeAttenuation", true);
		gain = eapp.getAttribute("volumeCoefficient", 1f);
		speedOfSound = eapp.getAttribute("speedOfSound", 300f);
		updateCutOff = eapp.getAttribute("updateCutOff", 10f);
		gamma = sampleRate/speedOfSound;
	}
}
