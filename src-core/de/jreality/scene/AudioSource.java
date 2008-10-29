package de.jreality.scene;


import de.jreality.scene.data.RingBuffer;
import de.jreality.scene.event.AudioEvent;
import de.jreality.scene.event.AudioEventMulticaster;
import de.jreality.scene.event.AudioListener;

/**
 * The core of audio for jReality.  The basic idea is that a scene graph component can have an audio source
 * attached to it.  An audio source writes a mono signal into a ring buffer upon request, and audio renderers
 * request readers for the ring buffer, one for each occurrence of the source in the scene graph.  An audio
 * source keeps track of time in terms of the number of samples requested so far.  Readers can read samples
 * concurrently, and sample requests are queued and managed so that an audio source only writes as many samples
 * as the fastest renderer requests.
 * 
 * Samples are floats in the range from -1 to 1.
 * 
 * @author brinkman
 *
 */
public abstract class AudioSource extends SceneGraphNode {
	
	protected transient AudioListener audioListener = null;
	protected transient Boolean hasChanged = false;

	public enum State {RUNNING, STOPPED, PAUSED}
	protected State state = State.STOPPED;
	protected RingBuffer ringBuffer = null;
	protected int sampleRate = 0;

	public AudioSource(String name) {
		super(name);
	}	
 
	public int getSampleRate() {
		return sampleRate;  // need sync?
	}
	
	public RingBuffer.Reader createReader() {
		return ringBuffer.createReader();
	}
	
	public State getState() {
		startReader();
		try {
			return state;
		} finally {
			finishReader();
		}
	}
	
	/**
	 * set the state of the node.
	 *       
	 * @param state set the state of the audio source
	 */
	public void setState(State state) {
		switch (state) {
		case RUNNING:
			start();
			break;
		case PAUSED:
			pause();
			break;
		case STOPPED:
			stop();
			break;
		default:
			break;
		}
	}
    
	public int readSamples(RingBuffer.Reader reader, float buffer[], int initialIndex, int nSamples) {
		if (!reader.checkBuffer(ringBuffer)) throw new IllegalArgumentException("reader does not match ringbuffer!");
		startReader();
		try {
			if (state != State.RUNNING) {
				return 0;
			}
			synchronized(this) {
				int needed = nSamples-reader.valuesLeft();
				if (needed>0) {
					writeSamples(needed);
				}
			}
			return reader.read(buffer, initialIndex, nSamples);
		} finally {
			writingFinished();
			finishReader();
		}
	}

	// reset audio engine; no sync necessary, only to be called from stop method
	protected void reset() {
		// default: do nothing
	}

	// write _at least_ n samples to ringBuffer if available, no sync necessary
	protected abstract void writeSamples(int n);


// *************************************** transport functions *****************************************
	
	public void start() {
		startWriter();
		try {
			if (state != State.RUNNING) {
				state = State.RUNNING;
				hasChanged = true;
			}
		} finally {
			finishWriter();
		}
	}
	public void stop() {
		startWriter();
		try {
			if (state != State.STOPPED) {
				state = State.STOPPED;
				reset();
				hasChanged = true;
			}
		} finally {
			finishWriter();
		}
	}
	public void pause() {
		startWriter();
		try {
			if (state != State.PAUSED) {
				state = State.PAUSED;
				hasChanged = true;
			}
		} finally {
			finishWriter();
		}
	}

	
// ************************************** the rest is boilerplate *************************************

	public void accept(SceneGraphVisitor v) {
		startReader();
		try {
			v.visit(this);
		} finally {
			finishReader();
		}
	}
	static void superAccept(AudioSource a, SceneGraphVisitor v) {
		a.superAccept(v);
	}
	private void superAccept(SceneGraphVisitor v) {
		super.accept(v);
	}
	public void addAudioListener(AudioListener listener) {
		startReader();
		try {
			audioListener=AudioEventMulticaster.add(audioListener, listener);
		} finally {
			finishReader();
		}
	}
	public void removeAudioListener(AudioListener listener) {
		startReader();
		try {
			audioListener=AudioEventMulticaster.remove(audioListener, listener);
		} finally {
			finishReader();
		}
	}
	protected void writingFinished() {
		if (hasChanged && audioListener != null) {
			audioListener.audioChanged(new AudioEvent(this));
		}
		hasChanged = false;
	}
}
