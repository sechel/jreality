package de.jreality.audio.javasound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import de.jreality.audio.AmbisonicsSoundEncoder;
import de.jreality.audio.AudioBackend;
import de.jreality.audio.AudioLauncher;
import de.jreality.audio.util.Limiter;
import de.jreality.scene.Viewer;

/**
 * 
 * An Ambisonics Decoder that renders into the default JavaSound stereo output, 44.1 kHz, 16 bit signed PCM.
 * Use the {@code launch}-method to activate this renderer for a given {@link Viewer}.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 *
 */
public class JavaAmbisonicsStereoDecoder {

	private static final boolean LIMIT = true;
	
	private static final float W_SCALE = (float) Math.sqrt(0.5);
	private static final float Y_SCALE = 0.5f;

	SourceDataLine stereoOut;
	byte[] buffer;
	float[] fbuffer;
	float[] fbuffer_lookAhead;
	
	protected int byteLen;
	protected int framesize;

	public JavaAmbisonicsStereoDecoder(int framesize) throws LineUnavailableException {

		this.framesize = framesize;
		byteLen = framesize * 2 * 2; // 2 channels, 2 bytes per sample
		buffer = new byte[byteLen];
		fbuffer = new float[2*framesize]; // 2 channels
		fbuffer_lookAhead = new float[2*framesize];
		
		AudioFormat audioFormat = JavaSoundUtility.outputFormat(2);
		
		stereoOut = JavaSoundUtility.createSourceDataLine(audioFormat);

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
		
		JavaSoundUtility.floatToByte(buffer, fbuffer);
		stereoOut.write(buffer, 0, byteLen);
		
		swapBuffers();
	}

	protected void swapBuffers() {
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
		JavaSoundUtility.floatToByte(buffer, fbuffer);
		stereoOut.write(buffer, 0, byteLen);
	}
		
	public int getSamplerate() {
		return AudioLauncher.getSampleRate();
	}
	
	public static void launch(Viewer viewer) throws LineUnavailableException {
		final int frameSize = 1024;
		final JavaAmbisonicsStereoDecoder dec = new JavaAmbisonicsStereoDecoder(frameSize);
		final AudioBackend backend = new AudioBackend(viewer.getSceneRoot(), viewer.getCameraPath(), AudioLauncher.getSampleRate());
		final AmbisonicsSoundEncoder enc = new AmbisonicsSoundEncoder() {
			public void finishFrame() {
				dec.renderAmbisonics(bw, bx, by, bz);
			}
		};
		
		JavaSoundUtility.launchAudioBackend(backend, enc, frameSize);
	}

}
