package de.jreality.audio.javasound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import de.jreality.audio.AbstractAudioRenderer;
import de.jreality.audio.AudioBackend;
import de.jreality.audio.SoundEncoder;

public abstract class AbstractJavaSoundRenderer extends AbstractAudioRenderer implements Runnable {

	protected SourceDataLine outputLine;

	private SoundEncoder encoder;

	protected int frameSize=512;
	
	protected int sampleRate;
	protected int channels;
	
	private boolean running;
	
	Thread soundThread=null;

	protected int bufferLength;
	
	/**
	 * computes buffer length
	 * 
	 * @param sdl
	 */
	protected void setSourceDataLine(SourceDataLine sdl) {
		outputLine=sdl;
		AudioFormat af = outputLine.getFormat();
		sampleRate = (int) af.getSampleRate();
		channels=af.getChannels();
		int bytesPerSample = (af.getSampleSizeInBits()+7)/8;
		bufferLength = frameSize * bytesPerSample * channels; // 2 channels, 2 bytes per sample
	}
	
	public void launch() throws LineUnavailableException {
		if (root == null || microphonePath == null) throw new IllegalStateException("need root and microphone path to launch");
		if (isRunning()) throw new IllegalStateException("JavaSound renderer already started...");
		encoder=createSoundEncoder();
		backend=new AudioBackend(root, microphonePath, sampleRate, interpolationFactory, soundPathFactory);
		startRenderThread();
	}

	protected abstract SourceDataLine createSourceDataLine() throws LineUnavailableException;

	protected abstract SoundEncoder createSoundEncoder();

	private void startRenderThread() {
		setRunning(true);
		soundThread=new Thread(this, "JavaSound render thread");
		soundThread.setPriority(Thread.MAX_PRIORITY);
		soundThread.setDaemon(true);
		soundThread.start();
	}

	public synchronized void unlaunch() {
		setRunning(false);
		while (isRunning()) {
			try {
				wait(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		soundThread=null;
		super.unlaunch();
		outputLine.close();
	}

	public void run() {
		while (isRunning()) {
			backend.processFrame(encoder, frameSize);
		}
		System.out.println("JavaSound stopped");
	}

	public synchronized boolean isRunning() {
		return running;
	}

	private synchronized void setRunning(boolean b) {
		running=b;
	}
	
	public void setFrameSize(int fs) {
		frameSize=fs;
	}
	
	public int getFrameSize() {
		return frameSize;
	}

}
