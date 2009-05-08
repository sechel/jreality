package de.jreality.ui.plugins;

import java.awt.GridLayout;

import javax.swing.JFrame;

import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.view.CameraStand;
import de.jreality.plugin.view.ContentTools;
import de.jreality.plugin.view.Lights;

public class StartupLocalTest {

	public static void main(String[] args) {
		JRViewer v = JRViewer.createEmptyViewer();
		v.registerPlugin(new CameraStand());
		v.registerPlugin(new Lights());
		v.registerPlugin(new ContentTools());
		v.setContent(Primitives.sharedIcosahedron);
		v.startupLocal();
		
		JFrame f = new JFrame();
		f.setSize(800, 600);
		f.setLayout(new GridLayout());
		f.add(v.getView().getContentPanel());
		f.setVisible(true);
	}

}
