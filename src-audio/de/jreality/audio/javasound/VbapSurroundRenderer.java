package de.jreality.audio.javasound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import de.jreality.audio.AudioBackend;
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
	
	public static void launch(Viewer viewer) throws LineUnavailableException {
		
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
		
		final AudioBackend backend = new AudioBackend(viewer.getSceneRoot(), viewer.getCameraPath(), JavaSoundUtility.SAMPLE_RATE);

		final VbapSoundEncoder enc = new VbapSoundEncoder(speakers.length, speakers, channelIDs) {

			@Override
			public void finishFrame() {
				dec.render(buf);
			}
			
		};
		
		
		
		/*

		// Visual Editor for speaker positions, uses java2d, java2dx, modelling.
		
		de.jreality.audio.VbapSpeakerEditor editor = new de.jreality.audio.VbapSpeakerEditor(enc);
		javax.swing.JFrame f = new javax.swing.JFrame("VBAP Speakers");
		f.setSize(800, 600);
		f.getContentPane().add(editor);
		f.setVisible(true);

		*/
		
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

	
}
