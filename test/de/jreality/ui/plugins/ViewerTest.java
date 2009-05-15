package de.jreality.ui.plugins;

import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.view.Export;

public class ViewerTest {

	public static void main(String[] args) {
		JRViewer v = JRViewer.createViewerApp();
		v.setContent(Primitives.coloredCube());
		v.registerPlugin(new Export());
		v.startup();
	}

}
