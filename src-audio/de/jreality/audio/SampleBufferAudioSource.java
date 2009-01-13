package de.jreality.audio;

import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;

/**
 * 
 * An AudioSource getting data from a precomputed sample buffer.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 *
 */
public class SampleBufferAudioSource extends AudioSource {

	protected float[] samples;
	protected int nSamples;
	protected int index;
	protected boolean loop;

	public SampleBufferAudioSource(String name, float[] sampleBuffer, int sampleRate, boolean loop) {
		super(name);
		this.loop = loop;
		this.sampleRate=sampleRate;
		this.samples=sampleBuffer;
		this.nSamples=samples.length;
		
		ringBuffer = new RingBuffer(sampleRate);
		reset();
	}

	protected void reset() {
		index = 0;
	}

	protected void writeSamples(int nRequested) {
		if (index+nRequested<samples.length) {
			ringBuffer.write(samples, index, nRequested);
			index += nRequested;
		}
		else {
			int n1 = nSamples-index;
			ringBuffer.write(samples, index, n1);
			if (loop) {
				index = nRequested-n1;
				ringBuffer.write(samples, 0, index);
			}
			else {
				state = State.STOPPED;  // to let listeners know that we're done
				reset();
				hasChanged = true;
			}
		}
	}

}