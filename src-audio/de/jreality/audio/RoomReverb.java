package de.jreality.audio;

import java.util.Arrays;

import de.jreality.shader.EffectiveAppearance;


/**
 * 
 * Simple room reverb, based on the reverb opcode of Csound.
 * 
 * @author brinkman
 *
 */
public class RoomReverb implements SampleProcessor {

	private static final float[] combDelays = {0.0297f, 0.0371f, 0.0411f, 0.0437f, 0.0050f, 0.0017f};
	private float[] coeffs = new float[6];
	private float[][] comb = new float[6][];
	private int[] combIndex = new int[6];
	private float reverbTime = AudioAttributes.DEFAULT_REVERB_TIME;
	private static final float Q0 = (float) Math.log(0.001);
	private int sampleRate;
	private RingBuffer buffer;
	private RingBuffer.Reader reader;
	
	
	public RoomReverb() {
		computeCoefficients();
	}
	
	public void setProperties(EffectiveAppearance app) {
		float reverbTime = app.getAttribute(AudioAttributes.REVERB_TIME, AudioAttributes.DEFAULT_REVERB_TIME);
		if (reverbTime!=this.reverbTime) {
			this.reverbTime = reverbTime;
			computeCoefficients();
		}
	}

	private void computeCoefficients() {
		float q = Q0/reverbTime;
		for(int i = 0; i < 6; i++) {
			coeffs[i] = (float) Math.exp(q*combDelays[i]);
		}
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
		for(int i = 0; i<6; i++) {
			comb[i] = new float[(int) (sampleRate*combDelays[i]+0.5)];
		}
		buffer = new RingBuffer(sampleRate);
		reader = buffer.createReader();
	}

	public void write(float[] buf, int initialIndex, int samples) {
		buffer.write(buf, initialIndex, samples);
	}

	public void clear() {
		for(int i = 0; i < 6; i++) {
			Arrays.fill(comb[i], 0);
		}
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public int read(float[] buf, int initialIndex, int samples) {
		final int nRead = reader.read(buf, initialIndex, samples);
		final int terminalIndex = initialIndex+nRead;
		
		for(int i = initialIndex; i<terminalIndex; i++) {
			float acc = 0;
			float v = buf[i];
			
			for(int j = 0; j < 4; j++) {
				float w = currentCombValue(j);
				acc += w;
				setCombValue(j, coeffs[j]*w + v);
				advanceCombIndex(j);
			}
			
			float y1 = currentCombValue(4);
			float z;
			setCombValue(4, z = coeffs[4]*y1 + acc);
			advanceCombIndex(4);
			
			y1 -= coeffs[4] * z;
			float y2 = currentCombValue(5);
			setCombValue(5, z = coeffs[5]*y2 + y1);
			advanceCombIndex(5);
			
			buf[i] = y2 - coeffs[5]*z;
		}
		return nRead;
	}

	private void advanceCombIndex(int j) {
		combIndex[j] = (combIndex[j]+1) % comb[j].length;
	}

	private float currentCombValue(int j) {
		return comb[j][combIndex[j]];
	}
	
	private float setCombValue(int j, float v) {
		return comb[j][combIndex[j]] = v;
	}
}
