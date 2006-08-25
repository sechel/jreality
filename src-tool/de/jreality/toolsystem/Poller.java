package de.jreality.toolsystem;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import de.jreality.toolsystem.raw.PollingDevice;

/**
 * Polling devices implement PollingDevice, and do their
 * polling in the poll() method. Please write no raw
 * devices that start extra timers.
 * 
 * This class is ONLY for internal usage in the ToolSystem.
 * 
 * @author Steffen Weissmann
 *
 */
class Poller {

	private final Timer timer;
	private final TimerTask task;
	
	private final LinkedList<PollingDevice> pollingDevices=new LinkedList<PollingDevice>();
	
	private static final long period=5;
	
	private static Poller pollerInstance=new Poller();
	
	static Poller getSharedInstance() {
		return pollerInstance;
	}
	
	private Poller() {
		task = new TimerTask() {
			@Override
			public void run() {
				pollDevices();
			}
		};
		timer = new Timer("jReality device poller");
		timer.scheduleAtFixedRate(task, period, period);
	}

	private void pollDevices() {
		synchronized (pollingDevices) {
			for (PollingDevice pd : pollingDevices) pd.poll();
		}
	}
	
	void addPollingDevice(final PollingDevice pd) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				pollingDevices.add(pd);
			}
		}, 0);
	}
	void removePollingDevice(final PollingDevice pd) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				pollingDevices.remove(pd);
			}
		}, 0);
	}
	
}
