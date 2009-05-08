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
	 * @throws LineUnavailableException 
	 */
	protected void openSourceDataLine() throws LineUnavailableException {
		AudioFormat af = JavaSoundUtility.outputFormat(channels);
		outputLine = JavaSoundUtility.createSourceDataLine(af);
		sampleRate = (int) af.getSampleRate();
		int bytesPerSample = (af.getSampleSizeInBits()+7)/8;
		bufferLength = frameSize * bytesPerSample * channels;
		System.out.println("stereo out buffer size = "+outputLine.getBufferSize());
		outputLine.open(af, bufferLength);
		System.out.println("stereoOut bufferSize="+outputLine.getBufferSize());
		outputLine.start();
	}
	
	public void launch() throws LineUnavailableException {
		if (root == null || microphonePath == null) throw new IllegalStateException("need root and microphone path to launch");
		if (isRunning()) throw new IllegalStateException("JavaSound renderer already started...");
		encoder=createSoundEncoder();
		backend=new AudioBackend(root, microphonePath, sampleRate, interpolationFactory, soundPathFactory);
		startRenderThread();
	}

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
