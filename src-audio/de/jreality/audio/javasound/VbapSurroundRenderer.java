package de.jreality.audio.javasound;

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Mixer.Info;

import de.jreality.audio.AudioBackend;
import de.jreality.audio.VbapSoundEncoder;
import de.jreality.audio.util.Limiter;
import de.jreality.scene.Viewer;

public class VbapSurroundRenderer {

	private static final int SAMPLE_RATE = 44100;
	private static final boolean BIG_ENDIAN = false;
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
		
		Info[] mixerInfos = AudioSystem.getMixerInfo();
		System.out.println(Arrays.toString(mixerInfos));
		Info info = mixerInfos[0];
		Mixer mixer = AudioSystem.getMixer(info);
		mixer.open();

		AudioFormat audioFormat = new AudioFormat(
					SAMPLE_RATE, // the number of samples per second
					16, // the number of bits in each sample
					channels, // the number of channels
					true, // signed/unsigned PCM
					BIG_ENDIAN); // big endian ?
		
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
		if (!mixer.isLineSupported(dataLineInfo)) {
			throw new RuntimeException("no source data line found.");
		}
	
		surroundOut = (SourceDataLine) mixer.getLine(dataLineInfo);

		surroundOut.open(audioFormat, channels*byteLen);
		System.out.println("surroundOut bufferSize="+surroundOut.getBufferSize());
		surroundOut.start();
	}
	
	Limiter limiter = new Limiter();
	
	public void render(float[] surroundSamples) {
		System.arraycopy(surroundSamples, 0, fbuffer_lookAhead, 0, surroundSamples.length);
		limiter.limit(fbuffer, fbuffer_lookAhead);
		JavaAmbisonicsStereoDecoder.floatToByte(buffer, fbuffer, BIG_ENDIAN);
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
		
		final AudioBackend backend = new AudioBackend(viewer.getSceneRoot(), viewer.getCameraPath(), SAMPLE_RATE);

		final VbapSoundEncoder enc = new VbapSoundEncoder(speakers.length, speakers, channelIDs) {

			@Override
			public void finishFrame() {
				dec.render(buf);
			}
			
		};
		
		/*
		
		// Visual Editor for speaker positions, uses java2d, java2dx, modelling.
		
		VbapSpeakerEditor editor = new VbapSpeakerEditor(enc);
		JFrame f = new JFrame("VBAP Speakers");
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
