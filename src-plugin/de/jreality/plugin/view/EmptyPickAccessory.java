package de.jreality.plugin.view;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.SceneGraphPath;
import de.varylab.jrworkspace.plugin.Controller;

public abstract class EmptyPickAccessory extends ContentAccessory {

	private class EmptyPickPathListener implements ChangeListener {
		EmptyPickAccessory accessory;
		EmptyPickPathListener(EmptyPickAccessory acc) {
			accessory=acc;
		}
		public void stateChanged(ChangeEvent e) {
			accessory.updateTrigger();
		}
	}

	SceneGraphPath currentEmptyPick;
	private View view;
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		view.addChangeListener(new EmptyPickPathListener(this));
		updateTrigger();
	}

	private void updateTrigger() {
		setTriggerComponent(view.getEmptyPickPath().getLastComponent());
	}

}
