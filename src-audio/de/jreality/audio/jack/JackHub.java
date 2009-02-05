package de.jreality.audio.jack;

import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackException;
import de.gulden.framework.jjack.JJackSystem;

/**
 * This class manages Jack sources as well as the Jack sink.  Due to the design of JJack, there can only
 * be one Jack hub per JVM, so that JackHub is implemented as a singleton.  Note that the sink and all
 * sources have to be created before the hub is initialized; once the hub is initialized, the number of
 * input channels cannot be changed.  JackHub also takes care of the correct initialization order (see
 * comment in JackSource.java for details).
 * 
 * @author brinkman
 *
 */
public final class JackHub implements JJackAudioProcessor {

	
	private static final JackHub hub = new JackHub();
	
	private List<WeakReference<JackSource>> sources = new LinkedList<WeakReference<JackSource>>();
	private JackSink sink = null;
	private int sampleRate = 0;

	private static int highestInPort = -1;
	private static int highestOutPort = -1;
	
	private JackHub() {
		// do nothing
	}
	
	protected void finalize() throws JJackException {
		JJackSystem.shutdown();  // necessary?
	}
	
	public void process(JJackAudioEvent ev) {;
		FloatBuffer inputs[] = ev.getInputs();
		boolean disposedSources=false;
		for(WeakReference<JackSource> ref: sources) {
			try {
				JackSource source = ref.get();
				if (source != null) {
					source.process(inputs);
				} else {
					disposedSources=true;
				}
			} catch (Exception e) {
				// silent failures aren't nice, but we can't risk zombifying the jack client
			}
		}
		if (disposedSources) {
			for (Iterator<WeakReference<JackSource>> iter = sources.iterator(); iter.hasNext(); ) {
				WeakReference<JackSource> ref = iter.next();
				if (ref.get() == null) iter.remove();
			}
		}
		if (sink!=null) {
			sink.process(ev);
		}
	}

	public static synchronized int getSampleRate() {
		return hub.sampleRate;   // return value of 0 indicates that jack client hasn't been initialized yet
	}

	public static synchronized boolean addSource(JackSource source) {
		if (hub.sampleRate!=0) {
			if (source.highestPort()<=highestInPort) {
				source.init(hub.sampleRate);
			} else {
				throw new IllegalStateException("highest port out of range (can't add input ports after initialization)");
			}
		}
		return hub.sources.add(new WeakReference<JackSource>(source));
	}
	
	public static synchronized boolean removeSource(JackSource source) {
		for (Iterator<WeakReference<JackSource>> iter = hub.sources.iterator(); iter.hasNext(); ) {
			WeakReference<JackSource> src = iter.next();
			if (src.get() == source) {
				iter.remove();
				return true;
			}
		}
		return false;
	}
	
	public static synchronized void setSink(JackSink sink) {
		if (hub.sampleRate!=0) {
			if (sink.highestPort()<=highestOutPort) {
				sink.init(hub.sampleRate);
			} else {
				throw new IllegalStateException("highest port out of range (can't add output ports after initialization)");
			}
		}
		hub.sink = sink;
	}
	
	public static synchronized void initializeClient(String name) throws JJackException {
		if (hub.sampleRate!=0) {
			throw new IllegalStateException("jack client is already initialized");
		}
		
		for(WeakReference<JackSource> ref : hub.sources) {
			JackSource source = ref.get();
			if (source == null) continue;
			int n = source.highestPort();
			if (n>highestInPort) {
				highestInPort = n;
			}
		}
		highestOutPort = (hub.sink!=null) ? hub.sink.highestPort() : -1;
		
		// first we need to set system properties
		System.setProperty("jjack.ports.in", Integer.toString(highestInPort+1));
		System.setProperty("jjack.ports.out", Integer.toString(highestOutPort+1));
		System.setProperty("jjack.client.name", name);
		
		// then we can initialize JJackSystem
		hub.sampleRate = JJackSystem.getSampleRate();
		if (hub.sampleRate==0) {
			throw new JJackException("Jack unavailable");
		}
		
		for(WeakReference<JackSource> ref : hub.sources) {
			JackSource source = ref.get();
			if (source == null) continue;
			source.init(hub.sampleRate);
		}
		if (hub.sink!=null) {
			hub.sink.init(hub.sampleRate);
		}
		
		JJackSystem.setProcessor(hub);
	}
	
}
