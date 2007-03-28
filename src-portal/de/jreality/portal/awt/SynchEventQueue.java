package de.jreality.portal.awt;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;

import de.jreality.vr.ViewerVR;

public class SynchEventQueue extends EventQueue {

	private final Object synchLock = new Object();
	
	@Override
	public AWTEvent getNextEvent() throws InterruptedException {
		AWTEvent e= super.getNextEvent();
		if (e instanceof InvocationEvent) {
			// wait fort synchronized execution
			synchronized (synchLock) {
				synchLock.wait();
			}
		}
		return e;
	}

	public void unlock() {
		synchronized (synchLock) {
			synchLock.notify();
		}
	}
}
