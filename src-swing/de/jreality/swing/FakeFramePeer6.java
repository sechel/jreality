package de.jreality.swing;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Rectangle;
import java.awt.peer.FramePeer;

import sun.awt.CausedFocusEvent.Cause;

class FakeFramePeer6 extends FakeFramePeer implements FramePeer {

	FakeFramePeer6(JFakeFrame f) {
		super(f);
	}

	public Rectangle getBoundsPrivate() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAlwaysOnTop(boolean alwaysOnTop) {
		// TODO Auto-generated method stub
		
	}

	public void setModalBlocked(Dialog blocker, boolean blocked) {
		// TODO Auto-generated method stub
		
	}

	public void updateIconImages() {
		// TODO Auto-generated method stub
		
	}

	public void updateMinimumSize() {
		// TODO Auto-generated method stub
		
	}

	public boolean requestFocus(Component lightweightChild, boolean temporary, boolean focusedWindowChangeAllowed, long time, Cause cause) {
		// TODO Auto-generated method stub
		return false;
	}

}