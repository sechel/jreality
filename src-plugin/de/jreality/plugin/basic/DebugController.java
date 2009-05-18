package de.jreality.plugin.basic;

import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class DebugController extends SimpleController {

	@Override
	public void registerPlugin(Plugin p) {
		if (p.getClass().getName().equals("de.jreality.plugin.view.View")) {
			new Exception().printStackTrace();
		}
		super.registerPlugin(p);
	}
	@Override
	public <T extends Plugin> T getPlugin(Class<T> clazz) {
		if (clazz.getName().equals("de.jreality.plugin.view.View")) {
			new Exception().printStackTrace();
		}
		return super.getPlugin(clazz);
	}
	
	@Override
	protected void activatePlugin(Plugin p) {
		if (p.getClass().getName().equals("de.jreality.plugin.view.View")) {
			new Exception().printStackTrace();
		}
		super.activatePlugin(p);
		System.out.println("DebugController.activatePlugin(): "+p.getClass().getName());
	}
	
}
