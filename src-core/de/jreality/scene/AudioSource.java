package de.jreality.scene;


import de.jreality.scene.data.RingBuffer;
import de.jreality.scene.event.AudioEvent;
import de.jreality.scene.event.AudioEventMulticaster;
import de.jreality.scene.event.AudioListener;

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
	
	public void setParameter(String name, Object value) {
		startWriter();
		try {
			setParameterImpl(name, value);
		} finally {
			finishWriter();
		}
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
    
	public int readSamples(RingBuffer.Reader reader, float buffer[], int initialIndex, int nSamples) {
		if (!reader.checkBuffer(ringBuffer)) throw new IllegalArgumentException("reader does not match ringbuffer!");
		startReader();
		try {
			synchronized (this) {
				int needed = nSamples-reader.valuesLeft();
				if (nSamples>reader.valuesLeft()) {
					if (needed>0 && state == State.RUNNING) {
						writeSamples(needed);
					}
				}
			}
			return reader.read(buffer, initialIndex, nSamples);
		} finally {
			finishReader();
		}
	}

	// actual handling of parameter changes, sync and other administrative stuff taken care of in setParameter
	protected abstract void setParameterImpl(String name, Object value);
	
	// reset audio engine; no sync necessary, only to be called from stop method
	protected abstract void reset();

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
