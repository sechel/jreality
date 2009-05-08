package de.jreality.audio.javasound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import de.jreality.audio.SoundEncoder;
import de.jreality.audio.VbapSoundEncoder;
import de.jreality.audio.util.Limiter;

public class VbapRenderer extends AbstractJavaSoundRenderer {

	byte[] buffer;
	
	float[] fbuffer;
	float[] fbuffer_lookAhead;

	double[][] speakers = new double[][]{
			{0.5, 0},
			{0.5, 0.5},
			{-1.5,1},
			{-1.5,-1},
			{0.5,-0.5},
	};
	int[] channelIDs = new int[]{4,0,2,3,1};

	@Override
	protected SourceDataLine createSourceDataLine() throws LineUnavailableException {		
		AudioFormat audioFormat = JavaSoundUtility.outputFormat(speakers.length);
		SourceDataLine stereoOut = JavaSoundUtility.createSourceDataLine(audioFormat);
		System.out.println("vbap out buffer size = "+stereoOut.getBufferSize());
		stereoOut.open(audioFormat, bufferLength);
		System.out.println("vbap bufferSize="+stereoOut.getBufferSize());
		stereoOut.start();
		
		return stereoOut;
	}
	
	@Override
	protected SoundEncoder createSoundEncoder() {
		return new VbapSoundEncoder(speakers.length, speakers, channelIDs) {
			public void finishFrame() {
				render(buf);
			}
		};
	}
	
	@Override
	public void launch() throws LineUnavailableException {
		setSourceDataLine(createSourceDataLine());

		System.out.println("framesize="+frameSize);
		
		buffer = new byte[bufferLength];
		fbuffer = new float[channels*frameSize];
		fbuffer_lookAhead = new float[channels*frameSize];

		super.launch();
	}

	Limiter limiter = new Limiter();
	
	public void render(float[] surroundSamples) {
		System.arraycopy(surroundSamples, 0, fbuffer_lookAhead, 0, surroundSamples.length);
		limiter.limit(fbuffer, fbuffer_lookAhead);
		JavaSoundUtility.floatToByte(buffer, fbuffer);
		outputLine.write(buffer, 0, bufferLength);
		// swap buffers
		float[] tmpF = fbuffer;
		fbuffer = fbuffer_lookAhead;
		fbuffer_lookAhead = tmpF;
	}

}
