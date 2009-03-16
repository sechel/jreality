package de.jreality.audio.jack;

import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
	
	private static List<WeakReference<JackSource>> sources = new CopyOnWriteArrayList<WeakReference<JackSource>>();
	private static JackSink sink = null;
	private static int sampleRate = 0;

	private static int highestInPort;
	private static int highestOutPort;
	
	
	private JackHub() {
		// do nothing
	}

	protected void finalize() throws Throwable {
		removeClient();
	}
	
	public void process(JJackAudioEvent ev) {;
		FloatBuffer inputs[] = ev.getInputs();
		for(WeakReference<JackSource> ref : sources) {
			try {
				JackSource source = ref.get();
				if (source != null) {
					source.process(inputs);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (sink!=null) {
			try {
				sink.process(ev);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static int getSampleRate() {
		return sampleRate;   // return value of 0 indicates that jack client hasn't been initialized yet
	}

	public static boolean addSource(JackSource source) {
		if (sampleRate!=0) {
			if (source.highestPort()<=highestInPort) {
				source.init(sampleRate);
			} else {
				throw new IllegalStateException("highest port out of range (can't add input ports after initialization)");
			}
		}
		return sources.add(new WeakReference<JackSource>(source));
	}
	
	public static boolean removeSource(JackSource source) {
		for(WeakReference<JackSource> ref : sources) {
			if (ref.get()==source) {
				return sources.remove(ref); // okay because we're using CopyOnWriteArrayList
			}
		}
		return false;
	}
	
	public static void setSink(JackSink sink) {
		if (sampleRate!=0 && sink!=null) {
			if (sink.highestPort()<=highestOutPort) {
				sink.init(sampleRate);
			} else {
				throw new IllegalStateException("highest port out of range (can't add output ports after initialization)");
			}
		}
		JackHub.sink = sink;
	}
	
	public static void initializeClient(String name) throws JJackException {
		initializeClient(name, "", "");
	}
	
	public static void initializeClient(String name, String sinkClient) throws JJackException {
		initializeClient(name, sinkClient, "");
	}
	
	public static synchronized void initializeClient(String name, String sinkClient, String sourceClient) throws JJackException {
		if (sampleRate!=0) {
			throw new IllegalStateException("jack client is already initialized");
		}
		
		highestInPort = -1;
		for(WeakReference<JackSource> ref : sources) {
			JackSource source = ref.get();
			if (source!=null) {
				int n = source.highestPort();
				if (n>highestInPort) {
					highestInPort = n;
				}
			}
		}
		highestOutPort = (sink!=null) ? sink.highestPort() : -1;
		
		// first we need to set system properties
		System.setProperty("jjack.ports.in", Integer.toString(highestInPort+1));
		System.setProperty("jjack.ports.out", Integer.toString(highestOutPort+1));
		System.setProperty("jjack.ports.in.target", sourceClient);
		System.setProperty("jjack.ports.out.target", sinkClient);
		System.setProperty("jjack.client.name", name);
		
		// then we can initialize JJackSystem
		sampleRate = JJackSystem.getSampleRate();
		if (sampleRate==0) {
			throw new JJackException("Jack unavailable");
		}
		
		for(WeakReference<JackSource> ref : sources) {
			JackSource source = ref.get();
			if (source!=null) {
				source.init(sampleRate);
			}
		}
		if (sink!=null) {
			sink.init(sampleRate);
		}
		
		JJackSystem.setProcessor(hub);
	}
	
	public static void removeClient() throws JJackException {
		JJackSystem.shutdown();
	}
}
