package de.jreality.plugin;

import static java.util.Collections.sort;

import java.util.Comparator;
import java.util.List;

import de.jreality.plugin.basic.Content;
import de.jreality.plugin.content.DirectContent;
import de.varylab.jrworkspace.plugin.Controller;

public class PluginUtility {

	public static Class<? extends Content>
		defaultContentClass = DirectContent.class;
	
	/**
	 * Returns a Content instance if there is one registered
	 * @param <T>
	 * @param clazz the class of the plug-in
	 * @return a plug-in instance or null if no such plug-in
	 * was registered
	 */
	public static Content getContentPlugin(Controller c) {
		List<Content> candidates = c.getPlugins(Content.class);
		if (candidates.size() != 0) {
			sort(candidates, new ContentPriorityComparator());
			return c.getPlugin(candidates.get(0).getClass());
		} else {
			return c.getPlugin(defaultContentClass);
		}
	}
	

	/**
	 * A descending priority comparator
	 * @author sechel
	 *
	 */
	protected static class ContentPriorityComparator implements Comparator<Content> {

		public int compare(Content o1, Content o2) {
			return o1.getContentPriority() < o2.getContentPriority() ? 1 : -1;
		}
	
	}		


}
