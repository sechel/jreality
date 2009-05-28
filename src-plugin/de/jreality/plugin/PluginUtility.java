package de.jreality.plugin;

import java.util.List;

import de.jreality.plugin.basic.Content;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;

public class PluginUtility {

	/**
	 * Returns a previously registered plug-in instance
	 * @param <T>
	 * @param clazz the class of the plug-in
	 * @return a plug-in instance or null if no such plug-in
	 * was registered
	 */
	public static Content getContentPlugin(Controller c) {
		List<Content> candidates = c.getPlugins(Content.class);
		for (Content p : candidates) {
			c.getPlugin((Class<? extends Plugin>) p.getClass());
			return p;
		}
		return null;
	}
	
}
