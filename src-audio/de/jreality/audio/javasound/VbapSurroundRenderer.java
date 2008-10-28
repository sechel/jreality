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
import de.jreality.audio.AudioBackend;
import de.jreality.audio.VbapSoundEncoder;
import de.jreality.scene.Viewer;

public class VbapSurroundRenderer {

	private static final int SAMPLE_RATE = 44100;
	private static final boolean BIG_ENDIAN = false;
	SourceDataLine surroundOut;
	byte[] buffer;
	private int byteLen;
	private int channels=5;
	
	public VbapSurroundRenderer(int framesize) throws LineUnavailableException {
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
	
	public void render(float[] surroundSamples) {
		JavaAmbisonicsStereoDecoder.floatToByte(buffer, surroundSamples, BIG_ENDIAN);
		surroundOut.write(buffer, 0, byteLen);
	}
	
	public static void launch(Viewer viewer) throws LineUnavailableException {
		
		double sqr = Math.sqrt(0.5);
		
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

		final VbapSoundEncoder enc = new VbapSoundEncoder(speakers, channelIDs) {

			long st;
			@Override
			public void startFrame(int framesize) {
				super.startFrame(framesize);
				st=System.currentTimeMillis();
			}
			@Override
			public void finishFrame() {
				long dt = System.currentTimeMillis()-st;
				//System.out.println("dt="+dt);
				//System.out.println("buf="+Arrays.toString(buf));
				dec.render(buf);
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

	
}
