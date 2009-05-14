package de.jreality.plugin.view;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.SceneGraphPath;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;

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
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		// TODO Auto-generated method stub
		return null;
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
	
	@Override
	public void uninstall(Controller c) throws Exception {
		// TODO Auto-generated method stub
		super.uninstall(c);
	}

}
