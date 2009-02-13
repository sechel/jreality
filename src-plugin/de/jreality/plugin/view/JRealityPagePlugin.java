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

	@Override
	public String getHelpDocument() {
		return "";
	}

	@Override
	public Class<?> getHelpHandle() {
		return getClass();
	}

	@Override
	public Icon getHelpIcon() {
		return null;
	}

	@Override
	public String getHelpPath() {
		return "http://www3.math.tu-berlin.de/jreality/";
	}

	@Override
	public String getHelpStyleSheet() {
		return "files/main.css";
	}

	@Override
	public String getHelpTitle() {
		return "jReality Home Page";
	}

	@Override
	public void setHelpListener(HelpListener l) {
		
	}

}
