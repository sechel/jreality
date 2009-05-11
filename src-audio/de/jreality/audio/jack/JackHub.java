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
 * be one Jack hub per JVM, so that JackHub is implemented as a singleton.
 * 
 * @author brinkman
 *
 */
public final class JackHub implements JJackAudioProcessor {


	private static final JackHub hub = new JackHub();

	private static List<WeakReference<JackSource>> sources = new CopyOnWriteArrayList<WeakReference<JackSource>>();
	private static JackSink sink = null;
	private static int sampleRate = 0;

	private static int highestInPort = -1;
	private static int highestOutPort = -1;
	private static String clientName, targetName, sourceName;

	private static boolean initialized = false;


	private JackHub() {
		JJackSystem.setPortsIn(0);
		JJackSystem.setPortsOut(0);
	}

	protected void finalize() throws Throwable {
		JJackSystem.shutdown();
	}

	public static int getSampleRate() throws JJackException {
		if (sampleRate==0) {
			sampleRate = JJackSystem.getSampleRate();
			if (sampleRate<=0) {
				throw new JJackException("jack not available");
			}
		}
		return sampleRate;
	}

	public static String getClientName() {
		return clientName;
	}

	public static void setClientName(String name) {
		if (initialized && !name.equals(clientName)) {
			throw new IllegalStateException("can't change client name after initialization");
		}
		clientName = name;
		JJackSystem.setClientName(name);
	}

	public static String getTargetName() {
		return targetName;
	}

	public static void setTargetName(String name) {
		if (initialized && !name.equals(targetName)) {
			throw new IllegalStateException("can't change target name after initialization");
		}
		targetName = name;
		JJackSystem.setPortsOutTarget(name);
	}

	public static String getSourceName() {
		return sourceName;
	}

	public static void setSourceName(String name) {
		if (initialized && !name.equals(sourceName)) {
			throw new IllegalStateException("can't change source name after initialization");
		}
		sourceName = name;
		JJackSystem.setPortsInTarget(name);
	}

	public static boolean addSource(JackSource source) {
		if (source.highestPort()>highestInPort) {
			if (initialized) {
				throw new IllegalStateException("can't register new input ports after initialization");
			} else {
				highestInPort = source.highestPort();
				JJackSystem.setPortsIn(highestInPort+1);
			}
		}
		return sources.add(new WeakReference<JackSource>(source));
	}

	public static boolean removeSource(JackSource source) {
		for(WeakReference<JackSource> ref: sources) {
			if (ref.get()==source) {
				return sources.remove(ref); // okay because we're using CopyOnWriteArrayList
			}
		}
		return false;
	}

	public static void setSink(JackSink sink) {
		if (sink!=null && sink.highestPort()>highestOutPort) {
			if (initialized) {
				throw new IllegalStateException("can't register new output ports after initialization");
			} else {
				highestOutPort = sink.highestPort();
				JJackSystem.setPortsOut(highestOutPort+1);
			}
		}
		JackHub.sink = sink;
	}
	
	public static synchronized void initializeClient() throws JJackException {
		JJackSystem.setProcessor(hub);
		initialized = true;
	}

	public static void closeClient() throws JJackException {
		JJackSystem.setProcessor(null);
	}
	
	public void process(JJackAudioEvent ev) {
		FloatBuffer inputs[] = ev.getInputs();
		for(WeakReference<JackSource> ref: sources) {
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
}
