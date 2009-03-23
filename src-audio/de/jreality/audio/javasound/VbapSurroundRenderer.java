package de.jreality.audio.javasound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import de.jreality.audio.AudioBackend;
import de.jreality.audio.Interpolation;
import de.jreality.audio.SoundPath;
import de.jreality.audio.VbapSoundEncoder;
import de.jreality.audio.util.Limiter;
import de.jreality.scene.Viewer;

public class VbapSurroundRenderer {

	SourceDataLine surroundOut;
	byte[] buffer;
	private int byteLen;
	private int channels=5;
	
	float[] fbuffer;
	float[] fbuffer_lookAhead;
	
	public VbapSurroundRenderer(int framesize) throws LineUnavailableException {
		fbuffer = new float[channels*framesize];
		fbuffer_lookAhead = new float[channels*framesize];
		
		byteLen = framesize * channels * 2; // channels * 2 bytes per sample
		buffer = new byte[byteLen];

		AudioFormat outFormat = JavaSoundUtility.outputFormat(channels);
		
		surroundOut = JavaSoundUtility.createSourceDataLine(outFormat);
		surroundOut.open(outFormat, channels*byteLen);
		System.out.println("surroundOut bufferSize="+surroundOut.getBufferSize());
		surroundOut.start();
	}
	
	Limiter limiter = new Limiter();
	
	public void render(float[] surroundSamples) {
		System.arraycopy(surroundSamples, 0, fbuffer_lookAhead, 0, surroundSamples.length);
		limiter.limit(fbuffer, fbuffer_lookAhead);
		JavaSoundUtility.floatToByte(buffer, fbuffer);
		surroundOut.write(buffer, 0, byteLen);
		// swap buffers
		float[] tmpF = fbuffer;
		fbuffer = fbuffer_lookAhead;
		fbuffer_lookAhead = tmpF;
	}
	
	public static void launch(Viewer viewer, String label, Interpolation.Factory iFactory, SoundPath.Factory spFactory) throws LineUnavailableException {
		double[][] speakers = new double[][]{
				{0.5, 0},
				{0.5, 0.5},
				{-1.5,1},
				{-1.5,-1},
				{0.5,-0.5},
		};
		int[] channelIDs = new int[]{4,0,2,3,1};
		final int frameSize = 512;
		
		final VbapSurroundRenderer dec = new VbapSurroundRenderer(frameSize);
		final AudioBackend backend = new AudioBackend(viewer.getSceneRoot(), viewer.getCameraPath(), JavaSoundUtility.getSampleRate(), iFactory, spFactory);
		final VbapSoundEncoder enc = new VbapSoundEncoder(speakers.length, speakers, channelIDs) {
			public void finishFrame() {
				dec.render(buf);
			}
		};
		
		JavaSoundUtility.launchAudioThread(backend, enc, frameSize, label);
	}
}
