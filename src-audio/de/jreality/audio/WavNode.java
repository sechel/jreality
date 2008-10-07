package de.jreality.audio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;
import de.jreality.util.Input;

public class WavNode extends AudioSource {

	private boolean loop;
	
	AudioInputStream ain;

	private int bytesPerSample;

	private int channels;
	
	public WavNode(String name, Input input, boolean loop) throws UnsupportedAudioFileException, IOException {
		this(name, AudioSystem.getAudioInputStream(input.getInputStream()), loop);
	}
	
	public WavNode(String name, AudioInputStream ain, boolean loop) {
		super(name);
		
		this.ain = ain;
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
		bytesPerSample = af.getSampleSizeInBits()/8;
		channels = ain.getFormat().getChannels();
		
		if (loop) {
			//if (!ain.markSupported()) throw new IllegalArgumentException("cannot loop stream");
			//else 
				ain.mark(Integer.MAX_VALUE);
		}
		
		ringBuffer = new RingBuffer(sampleRate);
		
	}
	
	@Override
	protected void reset() {
		
	}

	@Override
	protected void setParameterImpl(String name, Object value) {
		// do nothing
	}

	@Override
	protected void writeSamples(int nRequested) {
		byte[] buf = new byte[channels*bytesPerSample*nRequested];
		try {
			int read = ain.read(buf)/(bytesPerSample*channels);
			if (read < nRequested && loop) {
				System.out.println("LOOP!");
				ain.reset();
				read += ain.read(buf, read*bytesPerSample*channels, (nRequested-read-1)*bytesPerSample*channels)/(bytesPerSample*channels);
			}
			float[] fbuf = new float[nRequested];
			for (int i=0; i<read; i++) {
				fbuf[i] = getFloat(buf, channels*bytesPerSample*i, bytesPerSample, false);
			}
			ringBuffer.write(fbuf, 0, read);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static float getFloat(byte[] b, int offset, int bytes /*per sample*/, boolean bigEndian) {
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
