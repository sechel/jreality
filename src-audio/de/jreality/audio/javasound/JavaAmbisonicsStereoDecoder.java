package de.jreality.audio.javasound;

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Mixer.Info;

import de.jreality.audio.AmbisonicsSoundEncoder;
import de.jreality.audio.AmbisonicsVisitor;
import de.jreality.audio.AudioBackend;
import de.jreality.audio.util.Limiter;
import de.jreality.scene.Viewer;

/**
 * 
 * An Ambisonics Decoder which renders into the default JavaSound stereo output, 44.1 kHz, 16 bit signed PCM.
 * Use the {@code launch}-method to activate this renderer for a given {@link Viewer}.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 *
 */
public class JavaAmbisonicsStereoDecoder {

	public static int SAMPLE_RATE = 44100;
	private static final boolean LIMIT = true;
	
	private static final float W_SCALE = (float) Math.sqrt(0.5);
	private static final float Y_SCALE = 0.5f;

	private static final boolean BIG_ENDIAN = false;

	SourceDataLine stereoOut;
	byte[] buffer;
	float[] fbuffer;
	float[] fbuffer_lookAhead;
	
	private int byteLen;
	private int framesize;

	public JavaAmbisonicsStereoDecoder(int framesize) throws LineUnavailableException {

		this.framesize = framesize;
		byteLen = framesize * 2 * 2; // 2 channels, 2 bytes per sample
		buffer = new byte[byteLen];
		fbuffer = new float[2*framesize]; // 2 channels
		fbuffer_lookAhead = new float[2*framesize];
		
		Info[] mixerInfos = AudioSystem.getMixerInfo();
		System.out.println(Arrays.toString(mixerInfos));
		Info info = mixerInfos[0];
		Mixer mixer = AudioSystem.getMixer(info);
		mixer.open();

		AudioFormat audioFormat = new AudioFormat(
					SAMPLE_RATE, // the number of samples per second
					16, // the number of bits in each sample
					2, // the number of channels
					true, // signed/unsigned PCM
					BIG_ENDIAN); // big endian ?
		
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
		if (!mixer.isLineSupported(dataLineInfo)) {
			throw new RuntimeException("no source data line found.");
		}
	
		stereoOut = (SourceDataLine) mixer.getLine(dataLineInfo);

		//stereoOut.open(audioFormat);
		stereoOut.open(audioFormat, 2*byteLen);
		System.out.println("stereoOut bufferSize="+stereoOut.getBufferSize());
		stereoOut.start();
	}

	public void renderAmbisonics(float[] wBuf, float[] xBuf, float[] yBuf, float[] zBuf) {
		if (LIMIT) renderAmbisonicsLimited(wBuf, xBuf, yBuf, zBuf);
		else renderAmbisonicsPlain(wBuf, xBuf, yBuf, zBuf);
	}
		

	Limiter limiter = new Limiter();
	
	public void renderAmbisonicsLimited(float[] wBuf, float[] xBuf, float[] yBuf, float[] zBuf) {
		
		for (int i = 0; i < framesize; i++) {
			float w = wBuf[i] * W_SCALE;
			float y = yBuf[i] * Y_SCALE;
			fbuffer_lookAhead[2*i]=w+y;
			fbuffer_lookAhead[2*i+1]=w-y;
		}

		limiter.limit(fbuffer, fbuffer_lookAhead);
		
		floatToByte(buffer, fbuffer, BIG_ENDIAN);
		stereoOut.write(buffer, 0, byteLen);
		
		// swap buffers
		float[] tmpF = fbuffer;
		fbuffer = fbuffer_lookAhead;
		fbuffer_lookAhead = tmpF;
	}
	
	public void renderAmbisonicsPlain(float[] wBuf, float[] xBuf, float[] yBuf, float[] zBuf) {
		for (int i = 0; i < framesize; i++) {
			float w = wBuf[i] * W_SCALE;
			float y = yBuf[i] * Y_SCALE;
			fbuffer[2*i]=w+y;
			fbuffer[2*i+1]=w-y;
		}
		floatToByte(buffer, fbuffer, BIG_ENDIAN);
		stereoOut.write(buffer, 0, byteLen);
	}
	
    /**
	 * Convert float array to byte array.
	 * 
	 * @param byteSound
	 *            User provided byte array to return result in.
	 * @param dbuf
	 *            User provided float array to convert.
	 */
	static final public void floatToByte(byte[] byteSound, float[] dbuf, boolean bigEndian) {
		int bufsz = dbuf.length;
		int ib = 0;
		if (bigEndian) {
			for (int i = 0; i < bufsz; i++) {
				short y = (short) (32767. * dbuf[i]);
				byteSound[ib] = (byte) (y >> 8);
				ib++;
				byteSound[ib] = (byte) (y & 0x00ff);
				ib++;
			}
		} else {
			for (int i = 0; i < bufsz; i++) {
				short y = (short) (32767. * dbuf[i]);
				byteSound[ib] = (byte) (y & 0x00ff);
				ib++;
				byteSound[ib] = (byte) (y >> 8);
				ib++;
			}
		}
	}

	
	public int getSamplerate() {
		return SAMPLE_RATE;
	}
	
	public static void launch(Viewer viewer) throws LineUnavailableException {
		
		final int frameSize = 1024;

		final JavaAmbisonicsStereoDecoder dec = new JavaAmbisonicsStereoDecoder(frameSize);
		
		final AudioBackend backend = new AudioBackend(viewer.getSceneRoot(), viewer.getCameraPath(), SAMPLE_RATE);

		final AmbisonicsSoundEncoder enc = new AmbisonicsSoundEncoder() {

//			long st;
//			@Override
//			public void startFrame(int framesize) {
//				super.startFrame(framesize);
//				st=System.currentTimeMillis();
//			}
			@Override
			public void finishFrame() {
//				long dt = System.currentTimeMillis()-st;
//				System.out.println("dt="+dt);
				dec.renderAmbisonics(bw, bx, by, bz);
			}
			
		};
		
		Runnable soundRenderer = new Runnable() {
			public void run() {
				while (true) {
					backend.encodeSound(enc, frameSize);
					
				}
			}
		};

		Thread soundThread = new Thread(soundRenderer);
		soundThread.setName("jReality audio renderer");
		soundThread.setPriority(Thread.MAX_PRIORITY);
		soundThread.start();
	}

	public static void launchOld(Viewer viewer) throws LineUnavailableException {
		
		final int frameSize = 1024;

		final JavaAmbisonicsStereoDecoder dec = new JavaAmbisonicsStereoDecoder(frameSize);
		final AmbisonicsVisitor ambiVisitor = new AmbisonicsVisitor(dec.getSamplerate());
		ambiVisitor.setRoot(viewer.getSceneRoot());
		ambiVisitor.setMicrophonePath(viewer.getCameraPath());


		final float bw[] = new float[frameSize];
		final float bx[] = new float[frameSize];
		final float by[] = new float[frameSize];
		final float bz[] = new float[frameSize];
		
		Runnable soundRenderer = new Runnable() {
			public void run() {
				long st;
				while (true) {
					st=System.currentTimeMillis();
					ambiVisitor.collateAmbisonics(bw, bx, by, bz, frameSize);
//					System.out.println("dt="+(System.currentTimeMillis()-st));
					dec.renderAmbisonics(bw, bx, by, bz);
				}
			}
		};

		Thread soundThread = new Thread(soundRenderer);
		soundThread.setName("jReality audio renderer");
		soundThread.setPriority(Thread.MAX_PRIORITY);
		soundThread.start();
	}

}
