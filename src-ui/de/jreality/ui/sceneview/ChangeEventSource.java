package de.jreality.ui.sceneview;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jtem.beans.ChangeEventMulticaster;

public abstract class ChangeEventSource {
	private transient ChangeListener changeListener;

	public void fireStateChanged(Object source) {
		if (changeListener != null) {
			changeListener.stateChanged(new ChangeEvent(source));
		}
	}
	
	public void fireStateChanged() {
		fireStateChanged(this);
	}
	
	public void addChangeListener(ChangeListener l) {
		changeListener = ChangeEventMulticaster.add(changeListener, l);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		changeListener=ChangeEventMulticaster.remove(changeListener, listener);
	}
}
