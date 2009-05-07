package de.jreality.ui.plugins;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;

public class ViewerTest {

	public static void main(String[] args) {
		JRViewer v = JRViewer.createViewer();
		v.setContent(Primitives.sharedIcosahedron);
		v.addAccessory(new JButton("Hello!)"));
		v.startup();
	}

}
