package de.jreality.audio;

import java.util.LinkedList;
import java.util.Queue;

import de.jreality.math.Matrix;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 * Rough first draft of sound paths with delay (for Doppler shifts and such).
 * 
 * TODO: improve interpolation, low-pass filter source and mic positions, deal with supersonic phenomena
 * 
 * @author brinkman
 *
 */
public class DelayPath implements SoundPath {

	private int sampleRate;
	private boolean attenuate = false;
	private float gain = 1f;
	private float speedOfSound = 300f;
	private float gamma;
	private Queue<float[]> frames = new LinkedList<float[]>();
	private Queue<Float> xSrc = new LinkedList<Float>();
	private Queue<Float> ySrc = new LinkedList<Float>();
	private Queue<Float> zSrc = new LinkedList<Float>();
	private float x0Src, y0Src, z0Src;
	private float x1Src, y1Src, z1Src;
	private float dxSrc, dySrc, dzSrc;
	private float x0Mic, y0Mic, z0Mic;
	private int relativeTime = 0;
	private float[] currentFrame = null;


	public DelayPath(int sampleRate) {
		this.sampleRate = sampleRate;
		gamma = sampleRate/speedOfSound;
	}
	
	public int processFrame(SampleReader reader, SoundEncoder enc, int frameSize, Matrix sourcePos, Matrix invMicPos) {
		xSrc.add((float) sourcePos.getEntry(0, 3));
		ySrc.add((float) sourcePos.getEntry(1, 3));
		zSrc.add((float) sourcePos.getEntry(2, 3));

		if (currentFrame==null) {
			advanceFrame();
		}
		
		float[] newFrame = new float[frameSize];
		int nRead = reader.read(newFrame, 0, frameSize);
		frames.add(newFrame);

		float x1Mic = (float) invMicPos.getEntry(0, 3);
		float y1Mic = (float) invMicPos.getEntry(1, 3);
		float z1Mic = (float) invMicPos.getEntry(2, 3);

		if (currentFrame==null) {
			x0Mic = x1Mic;
			y0Mic = y1Mic;
			z0Mic = z1Mic;
			relativeTime += frameSize;
		} else {
			float dxMic = (x1Mic-x0Mic)/frameSize;
			float dyMic = (y1Mic-y0Mic)/frameSize;
			float dzMic = (z1Mic-z0Mic)/frameSize;

			for(int j = 0; j<frameSize; j++) {
				encodeSample(enc, j);
				x0Mic += dxMic;
				y0Mic += dyMic;
				z0Mic += dzMic;
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
		
		float x = x0Mic+x0Src+dxSrc*st;
		float y = y0Mic+y0Src+dySrc*st;
		float z = z0Mic+z0Src+dzSrc*st;
		float r = (float) (dist(x, y, z)+1e-5);
		
		if (attenuate) {
			v /= r;
		}
		
		enc.encodeSample(v*gain, j, x/r, y/r, z/r, r);
	}

	private float sourceTime() {
		float d0 = dist(x0Mic+x0Src, y0Mic+y0Src, z0Mic+z0Src);
		float d1 = dist(x0Mic+x1Src, y0Mic+y1Src, z0Mic+z1Src);
		return (relativeTime-gamma*d0)/(1+gamma/currentFrame.length*(d1-d0));
	}

	private float dist(float x, float y, float z) {
		return (float) Math.sqrt(x*x+y*y+z*z);
	}
	
	public void setFromEffectiveAppearance(EffectiveAppearance eapp) {
		attenuate = eapp.getAttribute("volumeAttenuation", true);
		gain = eapp.getAttribute("volumeCoefficient", 1f);
		speedOfSound = eapp.getAttribute("speedOfSound", 300f);
		gamma = sampleRate/speedOfSound;
	}
}
