package de.jreality.ui.plugins;

import java.awt.GridLayout;

import javax.swing.JButton;

import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.view.ContentAccessory;
import de.jreality.scene.SceneGraphComponent;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class ScenePanelTest {

	public static void main(String[] args) {
		JRViewer v = JRViewer.createViewer();
		final SceneGraphComponent cmp = new SceneGraphComponent();
		cmp.setGeometry(Primitives.sharedIcosahedron);
		
		ContentAccessory sp = new ContentAccessory() {
			{
				shrinkPanel.setLayout(new GridLayout(3,2));
				shrinkPanel.add(new JButton("fooawsdasdfs"));
				shrinkPanel.add(new JButton("foosdfs"));
				shrinkPanel.add(new JButton("foo34f    dfw"));
				shrinkPanel.add(new JButton("foosdfsdf"));
				shrinkPanel.add(new JButton("foo345345"));
				shrinkPanel.add(new JButton("foosdfsdfsdfsdfsdf"));
			}
			@Override
			public PluginInfo getPluginInfo() {
				return new PluginInfo("foo", "bar");
			}
			@Override
			public SceneGraphComponent getTriggerComponent() {
				return cmp;
			}
		};
		
		v.registerPlugin(sp);

		v.setContent(cmp);
		v.startup();

	}

}
