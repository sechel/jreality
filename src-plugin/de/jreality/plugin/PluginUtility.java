package de.jreality.plugin;

import java.util.List;

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
	@SuppressWarnings("unchecked")
	public static <T> T getPlugin(Controller c, Class<T> clazz) {
		List<T> candidates = c.getPlugins(clazz);
		for (T p : candidates) {
			c.getPlugin((Class<? extends Plugin>) p.getClass());
			return p;
		}
		return null;
	}
	
}
