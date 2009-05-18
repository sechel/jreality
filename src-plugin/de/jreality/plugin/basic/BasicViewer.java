package de.jreality.plugin.basic;

import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class BasicViewer {

	public static void main(String[] args) {
		SimpleController c = new DebugController();
		c.registerPlugin(new View(true));
		//c.registerPlugin(new EmptyPickContent());
		c.registerPlugin(new BoxAlignedContent());
		c.registerPlugin(new ViewMenuBar());
		c.registerPlugin(new Inspector());
		c.registerPlugin(new ContentLoader());
		c.registerPlugin(new ContentTools());
		c.startup();
	}
	
	
	
}
