package de.jreality.plugin.view;

import javax.swing.Icon;

import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.flavor.HelpFlavor;

public class JRealityPagePlugin extends Plugin implements HelpFlavor {

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("JReality Home Page");
	}

	public String getHelpDocument() {
		return "";
	}

	public Class<?> getHelpHandle() {
		return getClass();
	}

	public Icon getHelpIcon() {
		return null;
	}

	public String getHelpPath() {
		return "http://www3.math.tu-berlin.de/jreality/";
	}

	public String getHelpStyleSheet() {
		return "files/main.css";
	}

	public String getHelpTitle() {
		return "jReality Home Page";
	}

	public void setHelpListener(HelpListener l) {
		
	}

}
