package de.jreality.ui.plugins;

import javax.swing.JButton;

import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public class ViewerTest {

	public static void main(String[] args) {
		JRViewer v = JRViewer.createViewer();
		v.setContent(Primitives.sharedIcosahedron);
		v.startup();
		
		ShrinkPanel sp = new ShrinkPanel("Test Shrinker");
		sp.add(new JButton("Hello!"));
		v.getView().getLeftSlot().addShrinkPanel(sp);
	}

}
