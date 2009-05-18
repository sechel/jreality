package de.jreality.plugin.basic;

import javax.swing.JPopupMenu;

import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class BasicViewer {

	public static void main(String[] args) {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		SimpleController c = new DebugController();
		c.registerPlugin(new View(true));
		c.registerPlugin(new DisplayOptions());
		c.registerPlugin(new BasicContent());
		c.registerPlugin(new ViewMenuBar());
		c.registerPlugin(new Inspector());
		c.registerPlugin(new ContentLoader());
		c.registerPlugin(new ContentTools());
		c.registerPlugin(new Shell());
		c.registerPlugin(new ContentAppearance());
		c.startup();
	}
	
}
