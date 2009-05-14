package de.jreality.audio.jack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackException;
import de.gulden.framework.jjack.JJackNativeClient;
import de.gulden.framework.jjack.JJackNativeClientEvent;
import de.gulden.framework.jjack.JJackNativeClientListener;


/**
 * A wrapper for JJackNativeClient that manages clients by name, so that different Jack nodes may
 * share a single client.  Any jack client other than the output client of the renderer should be
 * managed by this class because the abstract jack renderer class assumes that it only needs to
 * register as a listener with JackClient to be notified of any zombified input client.
 * 
 * @author brinkman
 *
 */
public class JackClient implements JJackAudioProcessor, JJackNativeClientListener {

	private static Map<String, JackClient> clients = new HashMap<String, JackClient>();
	private static Set<JJackNativeClientListener> listeners = new CopyOnWriteArraySet<JJackNativeClientListener>();
	
	private JJackNativeClient nativeClient = null;
	private Set<JJackAudioProcessor> processors = new CopyOnWriteArraySet<JJackAudioProcessor>();
	private String clientName = null, sourceTarget = null, sinkTarget = null;
	private int portsIn = 0, portsOut = 0;
	

	public static synchronized void registerClient(String clientName, int portsIn, int portsOut, String sourceTarget, String sinkTarget) {
		if (clients.containsKey(clientName)) {
			throw new RuntimeException("client name "+clientName+" already registered");
		}
		JackClient client = new JackClient();
		client.clientName = clientName;
		client.portsIn = portsIn;
		client.portsOut = portsOut;
		client.sourceTarget = sourceTarget;
		client.sinkTarget = sinkTarget;

		clients.put(clientName, client);
	}

	public static void addProcessor(String name, JJackAudioProcessor processor) {
		JackClient client = clients.get(name);
		if (client==null) {
			throw new RuntimeException("no client for name "+name);
		}
		client.processors.add(processor);
	}

	public static void removeProcessor(String name, JJackAudioProcessor processor) {
		JackClient client = clients.get(name);
		if (client==null) {
			throw new RuntimeException("no client for name "+name);
		}
		client.processors.remove(processor);
	}

	public static void removeProcessor(JJackAudioProcessor processor) {
		for(String clientName: clients.keySet()) {
			removeProcessor(clientName, processor);
		}
	}

	public static void addListener(JJackNativeClientListener listener) {
		listeners.add(listener);
	}
	
	public static void removeListener(JJackNativeClientListener listener) {
		listeners.remove(listener);
	}
	
	public static synchronized void launch() throws JJackException {
		JJackException jje = null;
		for(JackClient client: clients.values()) {
			try {
				client.launchClient();
			} catch (JJackException e) {
				jje = e;
			}
		}
		if (jje!=null) {
			throw jje;
		}
	}

	public static synchronized void shutdown() {
		for(JackClient client: clients.values()) {
			client.shutdownClient();
		}
	}

	private JackClient() {
		// merely hide constructor from outside world
	}

	private void launchClient() throws JJackException {
		shutdown();
		nativeClient = new JJackNativeClient(clientName, portsIn, portsOut, this);
		nativeClient.addListener(this);
		nativeClient.start(sourceTarget, sinkTarget);
	}

	private void shutdownClient() {
		if (nativeClient!=null) {
			nativeClient.close();
			nativeClient = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			shutdown();
		} finally {
			super.finalize();
		}
	}

	public void process(JJackAudioEvent e) {
		for(JJackAudioProcessor p: processors) {
			try {
				p.process(e);
			} catch (Throwable t) {
				t.printStackTrace();
				System.err.println("removing processor "+p+" from list");
				processors.remove(p);
			}
		}
	}

	public void handleShutdown(JJackNativeClientEvent e) {
		for(JJackNativeClientListener listener: listeners) {
			listener.handleShutdown(e);
		}
	}
}
