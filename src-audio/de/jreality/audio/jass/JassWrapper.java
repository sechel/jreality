package de.jreality.audio.jass;

import java.util.Arrays;

import jass.engine.BufferNotAvailableException;
import jass.engine.InOut;
import jass.engine.SinkIsFullException;
import jass.engine.Source;
import jass.patches.CombReverb;
import de.jreality.audio.SampleProcessor;
import de.jreality.scene.data.RingBuffer;


public class JassWrapper implements SampleProcessor {

	private InOut processor;
	private RingBuffer inBuf, outBuf;
	private RingBuffer.Reader inReader, outReader;
	private Source source;
	private long time = 0;
	private int sampleRate;

	private class JassSource implements Source {

		private long sourceTime = 0;
		private float[] buffer;
		private int bufferSize;

		private JassSource(int bufferSize) {
			setBufferSize(bufferSize);
		}

		public void clearBuffer() {
			Arrays.fill(buffer, 0);
		}

		public float[] getBuffer(long t) throws BufferNotAvailableException {
			while (t>sourceTime && inReader.valuesLeft()>=bufferSize) {
				inReader.read(buffer, 0, bufferSize);
				sourceTime++;
			}
			if (t>sourceTime) {
				throw new BufferNotAvailableException();
			}
			return buffer;
		}

		public int getBufferSize() {
			return bufferSize;
		}

		public long getTime() {
			return sourceTime;
		}

		public void setBufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
			buffer = new float[bufferSize];
		}

		public void setTime(long t) {
			sourceTime = t;
			clearBuffer();
		}
	}

	public JassWrapper(InOut processor, int sampleRate) throws SinkIsFullException {
		this.processor = processor;
		this.sampleRate = sampleRate;

		inBuf = new RingBuffer(sampleRate);
		outBuf = new RingBuffer(sampleRate);

		inReader = inBuf.createReader();
		outReader = outBuf.createReader();

		source = new JassSource(processor.getBufferSize());
		processor.addSource(source);
	}

	public void processInput(float[] buf, int frameSize) {
		inBuf.write(buf, 0, frameSize);
	}

	public void clear() {
		inReader.clear();
		outReader.clear();
		time = 0;
		if (processor.getTime()!=0) {
			processor.resetTime(0);
		}
		source.clearBuffer();
		processor.clearBuffer();
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public int read(float[] buffer, int initialIndex, int samples) {
		try {
			while (inReader.valuesLeft()>=processor.getBufferSize() && outReader.valuesLeft()<samples) {
				float[] buf = processor.getBuffer(time++);
				outBuf.write(buf, 0, processor.getBufferSize());
			}
		} catch (BufferNotAvailableException e) {
			e.printStackTrace();
		}

		if (outReader.valuesLeft()<samples) {  // only render full frames
			return 0;
		}

		return outReader.read(buffer, 0, samples);
	}
	
	public static void main(String[] args) throws SinkIsFullException {
		JassWrapper jw = new JassWrapper(new CombReverb(48, 44100, 3), 44100);
		float[] inBuf = new float[] {0, 0, 0, 0, 1, 1, 1, 1};
		float[] outBuf = new float[8];
		while (true) {
			jw.processInput(inBuf, 8);
			jw.read(outBuf, 0, 8);
			System.out.println(Arrays.toString(outBuf));
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
