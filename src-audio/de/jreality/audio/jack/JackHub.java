package de.jreality.audio.jack;

import java.nio.FloatBuffer;
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
	
	private List<JackSource> sources = new LinkedList<JackSource>();
	private JackSink sink = null;
	private int sampleRate = 0;
	
	private JackHub() {
		// do nothing
	}
	
	protected void finalize() throws JJackException {
		JJackSystem.shutdown();  // necessary?
	}
	
	public void process(JJackAudioEvent ev) {;
		FloatBuffer inputs[] = ev.getInputs();
		for(JackSource source: sources) {
			try {
				source.process(inputs);
			} catch (Exception e) {
				// silent failures aren't nice, but we can't risk zombifying the jack client
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
			throw new IllegalStateException("can't add sources after initialization");
		}
		return hub.sources.add(source);
	}
	
	public static synchronized boolean removeSource(JackSource source) {
		if (hub.sampleRate!=0) {
			throw new IllegalStateException("can't remove sources after initialization");
		}
		return hub.sources.remove(source);
	}
	
	public static synchronized void setSink(JackSink sink) {
		if (hub.sampleRate!=0) {
			throw new IllegalStateException("can't set sink after initialization");
		}
		hub.sink = sink;
	}
	
	public static synchronized void initializeClient(String name) {
		if (hub.sampleRate!=0) {
			throw new IllegalStateException("jack client is already initialized");
		}
		
		int nSources = -1;
		for(JackSource source: hub.sources) {
			int n = source.highestPort();
			if (n>nSources) {
				nSources = n;
			}
		}
		
		// first we need to set system properties
		System.setProperty("jjack.ports.in", Integer.toString(nSources+1));
		System.setProperty("jjack.ports.out", Integer.toString((hub.sink!=null) ? hub.sink.highestPort()+1 : 0));
		System.setProperty("jjack.client.name", name);
		
		// then we can initialize JJackSystem
		hub.sampleRate = JJackSystem.getSampleRate();
		for(JackSource source: hub.sources) {
			source.init(hub.sampleRate);
		}
		if (hub.sink!=null) {
			hub.sink.init(hub.sampleRate);
		}
		
		JJackSystem.setProcessor(hub);
	}
	
}
