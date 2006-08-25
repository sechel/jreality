package de.jreality.toolsystem.raw;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Poller {

	private final Timer timer;
	private final TimerTask task;
	
	private final LinkedList<PollingDevice> pollingDevices=new LinkedList<PollingDevice>();
	
	private static final long period=5;
	
	private static Poller pollerInstance=new Poller();
	
	public static Poller getSharedInstance() {
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

	protected void pollDevices() {
		synchronized (pollingDevices) {
			for (PollingDevice pd : pollingDevices) pd.poll();
		}
	}
	
	public void addPollingDevice(final PollingDevice pd) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				pollingDevices.add(pd);
			}
		}, 0);
	}
	public void removePollingDevice(final PollingDevice pd) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				pollingDevices.remove(pd);
			}
		}, 0);
	}
	
}
