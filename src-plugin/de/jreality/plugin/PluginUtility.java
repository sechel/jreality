package de.jreality.plugin;

import java.util.List;

import de.jreality.plugin.basic.Content;
import de.jreality.plugin.content.DirectContent;
import de.varylab.jrworkspace.plugin.Controller;

public class PluginUtility {

	/**
	 * Returns a Content instance if there is one registered
	 * @param <T>
	 * @param clazz the class of the plug-in
	 * @return a plug-in instance or null if no such plug-in
	 * was registered
	 */
	public static Content getContentPlugin(Controller c) {
		List<Content> candidates = c.getPlugins(Content.class);
		for (Content p : candidates) {
			c.getPlugin(p.getClass());
			return p;
		}
		Content fallbackContent = c.getPlugin(DirectContent.class);
		return fallbackContent;
	}
	
}
