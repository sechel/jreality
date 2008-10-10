package de.jreality.audio.javasound;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;
import de.jreality.scene.event.AudioEvent;
import de.jreality.util.Input;

/**
 * 
 * An AudioSource getting data from a JavaSound AudioInputStream. The whole
 * Stream is read into a buffer which makes it easy to loop the sample - but
 * this requires much memory for longer samples. 
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 *
 */
public class CachedAudioInputStreamSource extends AudioSource {

	private float[] samples;
	private int nSamples;
	private int index;
	private boolean loop;

	public CachedAudioInputStreamSource(String name, AudioInputStream ain, boolean loop) {
		super(name);
		this.loop = loop;

		AudioFormat af = ain.getFormat();
		if (af.getEncoding() != Encoding.PCM_SIGNED) {
			System.out.println("converting from format "+af);
			AudioFormat baseFormat = af;
			float inSampleRate = baseFormat.getSampleRate();
			if (inSampleRate == AudioSystem.NOT_SPECIFIED) {
				inSampleRate = 44100;
			}
			af =
			    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
			                    inSampleRate,
			                    16,
			                    baseFormat.getChannels(),
			                    baseFormat.getChannels() * 2,
			                    inSampleRate,
			                    false);
			System.out.println("to format "+af);
			ain = AudioSystem.getAudioInputStream(af, ain);
		}
		sampleRate = (int) af.getSampleRate();
		int bytesPerSample = af.getSampleSizeInBits()/8;

		nSamples = (int) ain.getFrameLength();

		ArrayList<Float> samplesL = new ArrayList<Float>(nSamples > 0 ? nSamples : 1024);

		byte[] buf = new byte[1024*af.getFrameSize()];

		int read = -1;
		try {
			while ((read=ain.read(buf))!= -1) {
				int readFrames = read/af.getFrameSize();
				for (int i=0; i<readFrames; i++) {
					float v = getFloat(buf, i*af.getFrameSize(), bytesPerSample, false);
					samplesL.add(v);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nSamples = samplesL.size();
		samples = new float[nSamples];
		for (int i = 0; i<nSamples; i++) {
			samples[i]=samplesL.get(i);
		}
		ringBuffer = new RingBuffer(sampleRate);
		reset();
	}

	public CachedAudioInputStreamSource(String name, Input input, boolean loop) throws UnsupportedAudioFileException, IOException {
		this(name, AudioSystem.getAudioInputStream(input.getInputStream()), loop);
	}

	@Override
	protected void reset() {
		index = 0;
	}

	@Override
	protected void setParameterImpl(String name, Object value) {
		// do nothing
	}

	@Override
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

	static float getFloat(byte[] b, int offset, int bytes /*per sample*/, boolean bigEndian) {
		float sample = 0;
		int ret = 0;
		int length = bytes;
		for (int i = 0; i < bytes; i++, length--)
		{
			ret |= ((int) (b[offset + i] & 0xFF) << ((((bigEndian) ? length : (i + 1)) * 8) - 8));
		}
		switch (bytes)
		{
		case 1 :
			if (ret > 0x7F)
			{
				ret = ~ret + 1;
				ret &= 0x7F;
				ret = ~ret + 1;
			}
			sample = (float) ((double) ret / (double) Byte.MAX_VALUE);
			break;
		case 2 :
			if (ret > 0x7FFF)
			{
				ret = ~ret + 1;
				ret &= 0x7FFF;
				ret = ~ret + 1;
			}
			sample = (float) ((double) ret / (double) Short.MAX_VALUE);
			break;
		case 3 :
			if (ret > 0x7FFFFF)
			{
				ret = ~ret + 1;
				ret &= 0x7FFFFF;
				ret = ~ret + 1;
			}
			sample = (float) ((double) ret / 8388608f);
			break;
		case 4 :
			sample = (float) ((double) ret / (double) Integer.MAX_VALUE);
			break;
		}
		return sample;
	}
}