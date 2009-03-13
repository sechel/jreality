package de.jreality.audio;

import java.util.Arrays;

import de.jreality.scene.data.SampleReader;
import de.jreality.shader.EffectiveAppearance;


/**
 * 
 * Schroeder reverberator, implementation based on (but slightly different from) the reverb opcode of Csound.
 * 
 * @author brinkman
 *
 */
public class ReverbReader implements SampleProcessor {

	private static final float[] delays = {0.0297f, 0.0371f, 0.0411f, 0.0437f, 0.09683f, 0.03292f};
	private float[] coeffs = new float[6];
	private float[][] delayLines = new float[6][];
	private int[] lineIndex = new int[6];
	private float reverbTime = AudioAttributes.DEFAULT_REVERB_TIME;
	private static final float Q0 = (float) Math.log(0.001);
	private SampleReader reader;

	
	public ReverbReader(SampleReader reader) {
		initialize(reader);
	}
	
	public ReverbReader() {
		// do nothing
	}
	
	public void initialize(SampleReader reader) {
		this.reader = reader;
		int sampleRate = reader.getSampleRate();
		for(int i = 0; i<6; i++) {
			delayLines[i] = new float[(int) (sampleRate*delays[i]+0.5)];
		}
		setReverbTime(reverbTime);
		coeffs[4] = (float) Math.exp(Q0*delays[4]/0.005);
		coeffs[5] = (float) Math.exp(Q0*delays[5]/0.0017);
	}

	public void setProperties(EffectiveAppearance app) {
		float reverbTime = app.getAttribute(AudioAttributes.REVERB_TIME, AudioAttributes.DEFAULT_REVERB_TIME);
		if (reverbTime!=getReverbTime()) {
			setReverbTime(reverbTime);
		}
	}

	public void setReverbTime(float reverbTime) {
		this.reverbTime = reverbTime;
		float q = Q0/reverbTime;
		for(int i = 0; i < 4; i++) {
			coeffs[i] = (float) Math.exp(q*delays[i]);
		}
	}

	public float getReverbTime() {
		return reverbTime;
	}
	
	public void clear() {
		reader.clear();
		for(int i = 0; i < 6; i++) {
			Arrays.fill(delayLines[i], 0);
		}
	}

	public int getSampleRate() {
		return reader.getSampleRate();
	}

	public int read(float[] buf, int initialIndex, int samples) {
		final int nRead = reader.read(buf, initialIndex, samples);
		final int terminalIndex = initialIndex+nRead;

		for(int i = initialIndex; i<terminalIndex; i++) {
			float acc = 0;
			float v = buf[i];

			// four comb filters
			for(int j = 0; j < 4; j++) {
				float w = currentFilterValue(j);
				acc += w;
				setFilterValue(j, coeffs[j]*w + v);
				advanceFilterIndex(j);
			}
			        
			// all-pass filter
			float y1 = currentFilterValue(4);
			float z = coeffs[4]*y1 + acc;      // feedback
			setFilterValue(4, z);
			advanceFilterIndex(4);
			y1 -= coeffs[4] * z;               // feedforward
			
			// another all-pass filter
			float y2 = currentFilterValue(5);
			z = coeffs[5]*y2 + y1;
			setFilterValue(5, coeffs[5]*y2 + y1);
			advanceFilterIndex(5);
			buf[i] = y2 - coeffs[5]*z;
		}
		return nRead;
	}

	private void advanceFilterIndex(int j) {
		lineIndex[j] = (lineIndex[j]+1) % delayLines[j].length;
	}

	private float currentFilterValue(int j) {
		return delayLines[j][lineIndex[j]];
	}

	private float setFilterValue(int j, float v) {
		return delayLines[j][lineIndex[j]] = v;
	}
}
