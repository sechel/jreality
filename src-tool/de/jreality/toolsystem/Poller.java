package de.jreality.toolsystem;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.Timer;

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
class Poller implements ActionListener {

	private final Timer timer;
	
	private final LinkedList<PollingDevice> pollingDevices=new LinkedList<PollingDevice>();
	
	private static final long period=5;
	
	private static Poller pollerInstance=new Poller();
	
	static Poller getSharedInstance() {
		return pollerInstance;
	}
	
	private Poller() {
		timer=new Timer((int) period, this);
		timer.setCoalesce(true);
		timer.start();
	}

	private void pollDevices() {
		synchronized (pollingDevices) {
			for (PollingDevice pd : pollingDevices) pd.poll();
		}
	}
	
	void addPollingDevice(final PollingDevice pd) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				pollingDevices.add(pd);
			}
		});
	}
	void removePollingDevice(final PollingDevice pd) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				pollingDevices.remove(pd);
			}
		});
	}
	
	public void actionPerformed(ActionEvent e) {
		pollDevices();
	}
	
}
