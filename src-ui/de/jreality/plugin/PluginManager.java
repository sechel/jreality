package de.jreality.plugin;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.prefs.Preferences;


public class PluginManager {

	private static HashSet<Plugin> plugins = new HashSet<Plugin>();
	
	public static void registerPlugin(Plugin p) {
		plugins.add(p);
	}
	
	public static void savePreferences(Class c) {
		Preferences prefs = getPreferences(c);
		for (Plugin p : plugins) {
			p.savePreferences(prefs, p.getName()+".");
		}
	}
	
	public static void restorePreferences(Class c) {
		Preferences prefs = getPreferences(c);
		for (Plugin p : plugins) {
			p.restorePreferences(prefs, p.getName()+".");
		}
	}
	
	public static void restoreDefaults() {
		for (Plugin p : plugins) {
			p.restoreDefaults();
		}
	}
	
	private static Preferences getPreferences(final Class c) {
		return AccessController.doPrivileged(new PrivilegedAction<Preferences>() {
			public Preferences run() {
				return Preferences.userNodeForPackage(c);
			}
		});
	}
}
