package de.jreality.audio;

/**
 * A simple sample rate converter, to be used as a transparent plugin between AudioSource and audio renderer.
 * 
 * TODO: Implement something better than linear interpolation; the current version is only a proof of concept
 * and introduces too much noise.
 * 
 * @author brinkman
 *
 */

public class ConvertingReader implements SampleReader {

	private SampleReader reader;
	private float inBuf[];
	
	private final int targetRate, sourceRate, sampleRate;
	private final float ratio;
	private int targetIndex = 0;
	private int samplesRead = 0;
	private boolean firstSample = false;
	
	public static SampleReader createReader(SampleReader reader, int targetRate) {
		return (reader.getSampleRate()==targetRate) ? reader : new ConvertingReader(reader, targetRate);
	}
	
	private ConvertingReader(SampleReader reader, int targetRate) {
		int sourceRate = reader.getSampleRate();
		int q = gcd(targetRate, sourceRate);
		this.reader = reader;
		this.sampleRate = targetRate;
		this.sourceRate = sourceRate/q;
		this.targetRate = targetRate/q;
		ratio = ((float) sourceRate)/((float) targetRate);
		inBuf = new float[this.sourceRate+1];
	}
	
	public int getSampleRate() {
		return sampleRate;
	}
	
	public void clear() {
		reader.clear();
		targetIndex = 0;
		samplesRead = 0;
		firstSample = false;
	}

	public int read(float[] buffer, int initialIndex, int nSamples) {
		if (!firstSample) {
			if (reader.read(inBuf, 0, 1)==0) {
				return 0;
			}
			firstSample = true;
		}
		if (samplesRead<sourceRate) {
			samplesRead += reader.read(inBuf, 1+samplesRead, sourceRate-samplesRead);
		}
		
		int i;
		for(i = 0; i<nSamples; i++) {
			if (targetIndex == targetRate) {
				inBuf[0] = inBuf[sourceRate];
				samplesRead = reader.read(inBuf, 1, sourceRate);
				targetIndex = 0;
			}

			int j = (targetIndex*sourceRate)/targetRate;
			if (j>=samplesRead) {
				break;
			}
			float t = targetIndex*ratio-j;
			float xj = inBuf[j];
			buffer[initialIndex+i] = xj+t*(inBuf[j+1]-xj);
			targetIndex++;
		}
		return i;
	}
	
	private int gcd(int m, int n) {
		while (n!=0) {
			int mm = m;
			m = n;
			n = mm % n;
		}
		return m;
	}
}
