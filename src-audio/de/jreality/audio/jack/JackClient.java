package de.jreality.audio.jack;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackException;
import de.gulden.framework.jjack.JJackNativeClient;


public class JackClient {

	private static Map<String, JackClient> clients = new HashMap<String, JackClient>();

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

	public static synchronized void addProcessor(String name, JJackAudioProcessor processor) {
		JackClient client = clients.get(name);
		if (client==null) {
			throw new RuntimeException("no client for name "+name);
		}
		for(WeakReference<JJackAudioProcessor> ref: client.processors) {
			JJackAudioProcessor p = ref.get();
			if (p==null) {
				client.processors.remove(ref);
			}
			if (p==processor) {
				return; // processor already registered; nothing to do here
			}
		}
		client.processors.add(new WeakReference<JJackAudioProcessor>(processor));
	}

	public static synchronized void removeProcessor(String name, JJackAudioProcessor processor) {
		JackClient client = clients.get(name);
		if (client==null) {
			throw new RuntimeException("no client for name "+name);
		}
		for(WeakReference<JJackAudioProcessor> ref: client.processors) {
			JJackAudioProcessor p = ref.get();
			if (p==null || p==processor) {
				client.processors.remove(ref);
			}
		}
	}

	public static synchronized void removeProcessor(JJackAudioProcessor processor) {
		for(String clientName: clients.keySet()) {
			removeProcessor(clientName, processor);
		}
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


	private JJackNativeClient nativeClient = null;
	private List<WeakReference<JJackAudioProcessor>> processors = new CopyOnWriteArrayList<WeakReference<JJackAudioProcessor>>();
	private String clientName = null, sourceTarget = null, sinkTarget = null;
	private int portsIn = 0, portsOut = 0;

	JJackAudioProcessor proc = new JJackAudioProcessor() {
		public void process(JJackAudioEvent e) {
			for(WeakReference<JJackAudioProcessor> ref: processors) {
				JJackAudioProcessor p = ref.get();
				if (p!=null) {
					try {
						p.process(e);
					} catch (Throwable t) {
						t.printStackTrace();
						System.err.println("removing processor "+p+" from list");
						processors.remove(ref);
					}
				} else {
					processors.remove(ref); // okay because we're using CopyOnWriteArrayList
				}
			}
		}
	};

	private JackClient() {
		// merely hide constructor from outside world
	}

	private void launchClient() throws JJackException {
		shutdown();
		nativeClient = new JJackNativeClient(clientName, portsIn, portsOut, proc);
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

}
