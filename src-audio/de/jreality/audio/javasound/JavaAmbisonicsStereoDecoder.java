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
import de.jreality.scene.data.RingBuffer;

/**
 * 
 * An Ambisonics Decoder which renders into the default JavaSound stereo output, 44.1 kHz, 16 bit signed PCM.
 * Use the {@code launch}-method to activate this renderer for a given {@link Viewer}.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 *
 */
public class JavaAmbisonicsStereoDecoder {

	private static final boolean SLEEP=true;
	
	private static final float wScale = (float) Math.sqrt(0.5);
	private static final float yScale = 0.5f;

	private static final boolean BIG_ENDIAN = false;

	SourceDataLine stereoOut;
	byte[] buffer;
	float[] fbuffer;
	RingBuffer lookAheadBuffer;
	RingBuffer.Reader lastFrameReader;
	
	private int byteLen;
	private int framesize;

	public JavaAmbisonicsStereoDecoder(int framesize) throws LineUnavailableException {

		this.framesize = framesize;
		byteLen = framesize * 2 * 2;
		buffer = new byte[byteLen];
		fbuffer = new float[2*framesize];
		lookAheadBuffer = new RingBuffer(4*framesize); // 1 frame look ahead
		lastFrameReader = lookAheadBuffer.createReader();
		
		Info[] mixerInfos = AudioSystem.getMixerInfo();
		System.out.println(Arrays.toString(mixerInfos));
		Info info = mixerInfos[0];
		Mixer mixer = AudioSystem.getMixer(info);
		mixer.open();

		AudioFormat audioFormat = new AudioFormat(
					44100, // the number of samples per second
					16, // the number of bits in each sample
					2, // the number of channels
					true, // signed/unsigned PCM
					BIG_ENDIAN); // big endian ?
		
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat, framesize);
		if (!mixer.isLineSupported(dataLineInfo)) {
			throw new RuntimeException("no source data line found.");
		}
	
		stereoOut = (SourceDataLine) mixer.getLine(dataLineInfo);

		stereoOut.open(audioFormat);
		stereoOut.start();
	}

//	private static final double MAX_SIGNAL_INCREASE_FACTOR = 0.995;
//	boolean limit=true;
//	float maxSignal=1f;
//	int holdcnt;
//	
//	public void renderAmbisonics(float[] wBuf, float[] xBuf, float[] yBuf, float[] zBuf) {
//		
//		float frameMaxSignal = maxSignal;
//		
//		for (int i = 0; i < framesize; i++) {
//			float w = wBuf[i] * wScale;
//			float y = yBuf[i] * yScale;
//			fbuffer[2*i]=w+y;
//			fbuffer[2*i+1]=w-y;
//			float abs = Math.abs(w)+Math.abs(y);
//			if (abs > frameMaxSignal) {
//				frameMaxSignal=abs;
//				holdcnt=16;
//			}
//		}
//		
//		lookAheadBuffer.write(fbuffer, 0, framesize);
//		lastFrameReader.read(fbuffer, 0, framesize);
//		float delta = Math.abs(frameMaxSignal-maxSignal);
//		//System.out.println("delta="+delta);
//		if (delta == 0) {
//			//if (holdcnt > 0) System.out.println("decreasing hold cnt "+holdcnt);
//			if (holdcnt == 0) {
//				//System.out.println("ramp down...");
//				//start ramp down...
//				delta = - (float) (maxSignal*(1-MAX_SIGNAL_INCREASE_FACTOR));
//			} else {
//				holdcnt--;
//			}
//		}
//		
//		float dd = delta/framesize;
//		for (int i=0; i<framesize; i++) {
//			maxSignal+=dd;
//			if (maxSignal < 1) maxSignal = 1;
//			fromDouble(fbuffer[2*i]/maxSignal, buffer, javaOutFormatFrameSize * i, bytesPerSample, false);
//			fromDouble(fbuffer[2*i+1]/maxSignal, buffer, javaOutFormatFrameSize * i + javaOutFormatFrameSize / 2, bytesPerSample, false);
//		}
//		stereoOut.write(buffer, 0, byteLen);
//		//System.out.println("maxSignal="+maxSignal);
//	}
	
	public void renderAmbisonics(float[] wBuf, float[] xBuf, float[] yBuf, float[] zBuf) {
		for (int i = 0; i < framesize; i++) {
			float w = wBuf[i] * wScale;
			float y = yBuf[i] * yScale;
			fbuffer[2*i]=w+y;
			fbuffer[2*i+1]=w-y;
		}
		floatToByte(buffer, fbuffer);
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
	static final public void floatToByte(byte[] byteSound, float[] dbuf) {
		int bufsz = dbuf.length;
		int ib = 0;
		if (BIG_ENDIAN) {
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
		return 44100;
	}
	
	public static void launch(Viewer viewer) throws LineUnavailableException {
		
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
				while (true) {
					ambiVisitor.collateAmbisonics(bw, bx, by, bz, frameSize);
					dec.renderAmbisonics(bw, bx, by, bz);
					if (SLEEP) try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		Thread soundThread = new Thread(soundRenderer);
		soundThread.setName("jReality audio renderer");
		soundThread.setPriority(Thread.MAX_PRIORITY);
		soundThread.start();
	}

}
