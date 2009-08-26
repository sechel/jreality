package de.jreality.audio.jack;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackException;
import de.gulden.framework.jjack.JJackNativeClient;
import de.gulden.framework.jjack.JJackNativeClientEvent;
import de.gulden.framework.jjack.JJackNativeClientListener;

/**
 * Manages a single JACK client across all JACK sources and backends.  Using a single client is the only way to
 * guarantee that inputs will be processed before outputs (because the outputs will depend on input from the inputs).
 * Both inputs and outputs are implementations of {@link JJackAudioProcessor}.
 * 
 * Inputs and outputs request a range of ports from JackManager and receive a key with which to access the ports.
 * After the native client has been launched, sinks and sources can obtain port numbers of the native client
 * corresponding to their keys.
 * 
 * Note that port assignments may change when the native client is restarted, so that port numbers should not be
 * cached.
 * 
 * @author brinkman
 *
 */
public class JackManager implements JJackAudioProcessor, JJackNativeClientListener {

	private static String label = "jReality";
	private static int portsIn = 0, portsOut = 0;
	private static JackManager manager = new JackManager(); // singleton
	private static JJackNativeClient nativeClient = null;
	
	private static long currentKey = 1L;
	private static HashMap<Long, Boolean> isInput = new LinkedHashMap<Long, Boolean>();
	private static HashMap<Long, Integer> ranges = new LinkedHashMap<Long, Integer>();
	private static HashMap<Long, String> targets = new LinkedHashMap<Long, String>();
	private static HashMap<Long, Integer> ports = new LinkedHashMap<Long, Integer>();
	
	private static boolean ready = false;
	private static int retries = 0;
	
	private static List<WeakReference<JJackAudioProcessor>>
		inputs = new CopyOnWriteArrayList<WeakReference<JJackAudioProcessor>>(),
		outputs = new CopyOnWriteArrayList<WeakReference<JJackAudioProcessor>>();

	
	/**
	 * Launches a native JACK client
	 * 
	 * @throws JJackException
	 */
	public static synchronized void launch() throws JJackException {
		shutdown();
		nativeClient = new JJackNativeClient(label, portsIn, portsOut, manager);
		nativeClient.addListener(manager);

		int in = 0, out = 0;
		for(long key: isInput.keySet()) {
			int r = ranges.get(key);
			String t = targets.get(key);
			if (isInput.get(key)) {
				nativeClient.connectInputPorts(in, r, t);
				ports.put(key, in);
				in += r;
			} else {
				nativeClient.connectOutputPorts(out, r, t);
				ports.put(key, out);
				out += r;
			}
		}
		ready = true;
	}
	
	/**
	 * Shuts down the native JACK client, if any
	 */
	public static synchronized void shutdown() {
		if (nativeClient!=null) {
			nativeClient.close();
			nativeClient = null;
			ready = false;
		}
	}
	
	/**
	 * Sets the name of the native JACK client.
	 * 
	 * @param label
	 */
	public static void setLabel(String label) {
		JackManager.label = label;
	}

	/**
	 * Sets the number of attempts to relaunch the native client after zombification.
	 * 
	 * @param retries
	 */
	public static void setRetries(int retries) {
		JackManager.retries = retries;
	}
	
	public static synchronized void addInput(JJackAudioProcessor proc) {
		addProcessor(proc, inputs);
	}
	
	public static synchronized void removeInput(JJackAudioProcessor proc) {
		inputs.remove(findReference(proc, inputs));
	}
	
	public static synchronized void addOutput(JJackAudioProcessor proc) {
		addProcessor(proc, outputs);
	}
	
	public static synchronized void removeOutput(JJackAudioProcessor proc) {
		outputs.remove(findReference(proc, outputs));
	}
	
	/**
	 * Reserves a range of input ports
	 * 
	 * @param range: number of ports requested
	 * @param target: regular expression defining output ports to autoconnect to
	 * @return a key (of type long) representing this range of ports
	 */
	public static synchronized long requestInputPorts(int range, String target) {
		checkRange(range, portsIn);
		portsIn += range;
		return requestPorts(range, target, true);
	}
	
	/**
	 * Reserves a range of output ports
	 * 
	 * @param range: number of ports requested
	 * @param target: regular expression defining input ports to autoconnect to
	 * @return a key (of type long) representing this range of ports
	 */
	public static synchronized long requestOutputPorts(int range, String target) {
		checkRange(range, portsOut);
		portsOut += range;
		return requestPorts(range, target, false);
	}
	
	/**
	 * Releases a range of ports corresponding to a key
	 * 
	 * @param key
	 */
	public static synchronized void releasePorts(long key) {
		if (!ranges.containsKey(key)) return;
		
		int r = ranges.get(key);
		if (isInput.get(key)) {
			portsIn -= r;
		} else {
			portsOut -= r;
		}
		
		isInput.remove(key);
		ranges.remove(key);
		targets.remove(key);
		ports.remove(key);
	}

	/**
	 * Returns the number of the first port in the range corresponding to the given key
	 * 
	 * @param key
	 * @return port number
	 */
	public static int getPort(long key) {
		return ports.get(key);
	}
	
	
	// the rest is for internal use only
	
	private static void checkRange(int range, int p0) {
		if (range<=0) {
			throw new IllegalArgumentException("range must be positive");
		}
		if (p0+range>JJackNativeClient.getMaxPorts()) {
			throw new IllegalStateException("total number of ports too large");
		}
	}
	
	private static long requestPorts(int range, String target, boolean inp) {
		isInput.put(currentKey, inp);
		ranges.put(currentKey, range);
		targets.put(currentKey, target);
		return currentKey++;
	}
	
	private static void addProcessor(JJackAudioProcessor proc, List<WeakReference<JJackAudioProcessor>> list) {
		if (findReference(proc, list)!=null) return;
		list.add(new WeakReference<JJackAudioProcessor>(proc));
	}

	private static WeakReference<JJackAudioProcessor> findReference(JJackAudioProcessor proc, List<WeakReference<JJackAudioProcessor>> list) {
		for(WeakReference<JJackAudioProcessor> ref: list) {
			if (ref.get()==proc) return ref;
		}
		return null;
	}

	private JackManager() {}

	public void process(JJackAudioEvent e) {
		if (!ready) return;
		processList(e, inputs);
		processList(e, outputs);
	}

	private void processList(JJackAudioEvent e, List<WeakReference<JJackAudioProcessor>> list) {
		for(WeakReference<JJackAudioProcessor> ref: list) {
			JJackAudioProcessor proc = ref.get();
			if (proc!=null) {
				proc.process(e);
			} else {
				list.remove(ref);
			}
		}
	}
	
	public void handleShutdown(JJackNativeClientEvent e) {
		shutdown();
		if (retries>0) {
			retries--;
			try {
				Thread.sleep(250); // long enough not to drive the CPU crazy, short enough not to be disconcerting
			} catch (InterruptedException ex) {
				// do nothing
			}
			System.err.println("relaunching jack client, "+retries+" attempts left");
			try {
				launch();
			} catch (JJackException ex) {
				ex.printStackTrace();
			}
		} else {
			System.err.println("jack client zombified; not trying to relaunch");
		}
	}
}
