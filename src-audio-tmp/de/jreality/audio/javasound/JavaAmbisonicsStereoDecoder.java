package de.jreality.audio.javasound;

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.Mixer.Info;

import de.jreality.audio.AmbisonicsVisitor;
import de.jreality.scene.Viewer;

/**
 * 
 * An Ambisonics Decoder which renders into the default JavaSound stereo output.
 * Use the {@code launch}-method to activate this renderer for a given {@link Viewer}.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 *
 */
public class JavaAmbisonicsStereoDecoder {

	private static final float wScale = (float) Math.sqrt(0.5);
	private static final float yScale = 0.5f;

	Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
	int channels = 2; // stereo!
	int samplerate = 44100; // cd sampling rate
	int bytesPerSample = 4;

	SourceDataLine stereoOut;
	byte[] buffer = new byte[4096];
	private int frameSize;

	public JavaAmbisonicsStereoDecoder() throws LineUnavailableException {

		Info[] mixerInfos = AudioSystem.getMixerInfo();
		System.out.println(Arrays.toString(mixerInfos));
		Info info = mixerInfos[0];
		Mixer mixer = AudioSystem.getMixer(info);
		mixer.open();

		int sampleSizeInBits = 8 * bytesPerSample;
		frameSize = channels * bytesPerSample;

		AudioFormat audioFormat = new AudioFormat(encoding, // the audio
															// encoding
															// technique
				samplerate, // the number of samples per second
				sampleSizeInBits, // the number of bits in each sample
				channels, // the number of channels (1 for mono, 2 for stereo,
							// and so on)
				frameSize, // the number of bytes in each frame
				samplerate, // the number of frames per second
				false); // big endian ?

		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		if (!mixer.isLineSupported(dataLineInfo)) {
			throw new RuntimeException();
		}

		stereoOut = (SourceDataLine) mixer.getLine(dataLineInfo);

		stereoOut.open(audioFormat);
		stereoOut.start();

	}

	public void renderAmbisonics(float[] wBuf, float[] xBuf, float[] yBuf, float[] zBuf, int samples) {
		int byteLen = samples * bytesPerSample * channels;
		if (buffer.length < byteLen)
			buffer = new byte[byteLen];

		for (int i = 0; i < samples; i++) {
			float w = wBuf[i] * wScale;
			float y = yBuf[i] * yScale;
			fromDouble(w + y, buffer, frameSize * i, bytesPerSample, false);
			fromDouble(w - y, buffer, frameSize * i + frameSize / 2,
					bytesPerSample, false);
		}
		stereoOut.write(buffer, 0, byteLen);
	}

	public void renderMono(float[] data, int frames) {

		int byteLen = frames * bytesPerSample * channels;

		if (buffer.length < byteLen)
			buffer = new byte[byteLen];

		for (int i = 0; i < frames; i++) {
			fromDouble(data[i], buffer, frameSize * i, bytesPerSample, false);
			fromDouble(data[i], buffer, frameSize * i + frameSize / 2,
					bytesPerSample, false);
		}
		stereoOut.write(buffer, 0, byteLen);
	}

	public void renderStereo(float[] dataLeft, float[] dataRight, int frames) {

		int byteLen = frames * bytesPerSample * channels;

		if (buffer.length < byteLen)
			buffer = new byte[byteLen];

		for (int i = 0; i < frames; i++) {
			fromDouble(dataLeft[i], buffer, frameSize * i, bytesPerSample,
					false);
			fromDouble(dataRight[i], buffer, frameSize * i + frameSize / 2,
					bytesPerSample, false);
		}
		stereoOut.write(buffer, 0, byteLen);
	}

	static final void fromDouble(float value, byte[] b, int offset, int bytes, boolean bigEndian) {
		int ival = -1;
		switch (bytes) {
		case 1: // 8 bit
			b[offset + 0] = new Float(value * (double) Byte.MAX_VALUE)
					.byteValue();
			break;
		case 2: // 16 bit
			short sval = new Float(value * (double) Short.MAX_VALUE)
					.shortValue();
			if (bigEndian) {
				b[offset + 0] = (byte) ((sval & 0x0000ff00) >> 8);
				b[offset + 1] = (byte) (sval & 0x000000ff);
			} else {
				b[offset + 0] = (byte) (sval & 0x000000ff);
				b[offset + 1] = (byte) ((sval & 0x0000ff00) >> 8);
			}
			break;
		case 3: // 24 bit
			ival = new Float(value * (double) 8388608).intValue();
			if (bigEndian) {
				b[offset + 0] = (byte) ((ival & 0x00ff0000) >> (8 * 2));
				b[offset + 1] = (byte) ((ival & 0x0000ff00) >> 8);
				b[offset + 2] = (byte) (ival  & 0x000000ff);
			} else {
				b[offset + 0] = (byte) (ival  & 0x000000ff);
				b[offset + 1] = (byte) ((ival & 0x0000ff00) >> 8);
				b[offset + 2] = (byte) ((ival & 0x00ff0000) >> (8 * 2));
			}
			break;
		case 4: // 32 bit
			ival = new Float(value * (double) Integer.MAX_VALUE).intValue();
			if (bigEndian) {
				b[offset + 0] = (byte) ((ival & 0xff000000) >> (8 * 3));
				b[offset + 1] = (byte) ((ival & 0x00ff0000) >> (8 * 2));
				b[offset + 2] = (byte) ((ival & 0x0000ff00) >> 8);
				b[offset + 3] = (byte) (ival  & 0x000000ff);
			} else {
				b[offset + 0] = (byte) (ival  & 0x000000ff);
				b[offset + 1] = (byte) ((ival & 0x0000ff00) >> 8);
				b[offset + 2] = (byte) ((ival & 0x00ff0000) >> (8 * 2));
				b[offset + 3] = (byte) ((ival & 0xff000000) >> (8 * 3));
			}
			break;
		}
	}
	
	public int getSamplerate() {
		return samplerate;
	}
	
	public static void launch(Viewer viewer) throws LineUnavailableException {
		final JavaAmbisonicsStereoDecoder dec = new JavaAmbisonicsStereoDecoder();
		final AmbisonicsVisitor ambiVisitor = new AmbisonicsVisitor(dec.getSamplerate());
		ambiVisitor.setRoot(viewer.getSceneRoot());
		ambiVisitor.setMicrophonePath(viewer.getCameraPath());

		final int frameSize = 1024;

		final float bw[] = new float[frameSize];
		final float bx[] = new float[frameSize];
		final float by[] = new float[frameSize];
		final float bz[] = new float[frameSize];
		
		Runnable soundRenderer = new Runnable() {
			public void run() {
				while (true) {
					ambiVisitor.collateAmbisonics(bw, bx, by, bz, frameSize);
					dec.renderAmbisonics(bw, bx, by, bz, frameSize);
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		new Thread(soundRenderer).start();
	}

}
