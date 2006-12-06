package de.jreality.plugin;

import java.util.prefs.Preferences;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public interface Plugin {
	
	String getName();
	
	JMenu getMainMenu();
	
	JMenuItem getExportMenu();
	
	void savePreferences(Preferences prefs, String prefix);
	
	void restorePreferences(Preferences prefs, String prefix);
	
	void restoreDefaults();
}
