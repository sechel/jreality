package de.jreality.audio.jack;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackException;
import de.gulden.framework.jjack.JJackNativeClient;
import de.gulden.framework.jjack.JJackNativeClientEvent;
import de.gulden.framework.jjack.JJackNativeClientListener;

/**
 * 
 * @author brinkman
 *
 */
public class JackManager implements JJackAudioProcessor, JJackNativeClientListener {

	private static String label = "jReality";
	private static int portsIn = 0, portsOut = 0;
	private static JackManager manager = new JackManager();
	private static JJackNativeClient nativeClient = null;
	
	private static long currentKey = 0L;
	private static HashMap<Long, Boolean> isInput = new HashMap<Long, Boolean>();
	private static HashMap<Long, Integer> ranges = new HashMap<Long, Integer>();
	private static HashMap<Long, String> targets = new HashMap<Long, String>();
	private static HashMap<Long, Integer> ports = new HashMap<Long, Integer>();
	
	private static boolean ready = false;
	private static int retries = 0;
	
	private static List<WeakReference<JJackAudioProcessor>>
		inputs = new CopyOnWriteArrayList<WeakReference<JJackAudioProcessor>>(),
		outputs = new CopyOnWriteArrayList<WeakReference<JJackAudioProcessor>>();

	public static int getPort(long key) {
		return ports.get(key);
	}
	
	public static synchronized long requestInputPorts(int range, String target) {
		checkRange(range, portsIn);
		portsIn += range;
		return requestPorts(range, target, true);
	}
	
	public static synchronized long requestOutputPorts(int range, String target) {
		checkRange(range, portsOut);
		portsOut += range;
		return requestPorts(range, target, false);
	}
	
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
	
	public static void setLabel(String label) {
		JackManager.label = label;
	}
	
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
	
	public static synchronized void shutdown() {
		if (nativeClient!=null) {
			nativeClient.close();
			nativeClient = null;
			ready = false;
		}
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

	public static void setRetries(int retries) {
		JackManager.retries = retries;
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
