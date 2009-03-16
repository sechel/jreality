package de.jreality.plugin.audio;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Statement;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.jreality.audio.javasound.JavaAmbisonicsStereoDecoder;
import de.jreality.audio.javasound.VbapSurroundRenderer;
import de.jreality.plugin.audio.image.ImageHook;
import de.jreality.plugin.view.View;
import de.jreality.scene.Viewer;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class AudioLauncher extends ShrinkPanelPlugin {

	private JComboBox renderers;
	private JButton launchButton;
	private JTextField targetField;
	
	private Viewer viewer;
	
	private static final String JACK1 = "Jack 1st order Ambisonics";
	private static final String JACK2 = "Jack 2nd order planar Ambisonics";
	private static final String STEREO = "Java Stereo";
	private static final String VBAP = "Java VBAP";
	
	private static final String RENDERKEY = "audioRenderer";
	private static final String JACKTARGETKEY = "jackTarget";
	
	
	public AudioLauncher() {
		shrinkPanel.setLayout(new GridLayout(3, 1));
		
		renderers = new JComboBox();
		renderers.addItem(JACK1);
		renderers.addItem(JACK2);
		renderers.addItem(STEREO);
		renderers.addItem(VBAP);
		shrinkPanel.add(renderers);
		
		JPanel panel = new JPanel();
		panel.add(new JLabel("Jack target port"));
		targetField = new JTextField(8);
		panel.add(targetField);
		shrinkPanel.add(panel);
		
		launchButton = new JButton("Launch");
		launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchAudio();
			}
		});
		shrinkPanel.add(launchButton);
	}
	
	private void launchAudio() {
		String type = (String) renderers.getSelectedItem();
		try {
			if (type.equals(JACK1)) {
				new Statement(Class.forName("de.jreality.audio.jack.JackAmbisonicsRenderer"),
								"launch", new Object[]{viewer, "jR Ambisonics", targetField.getText()}).execute();
			} else if (type.equals(JACK2)) {
				new Statement(Class.forName("de.jreality.audio.jack.JackAmbisonicsPlanar2ndOrderRenderer"),
						"launch", new Object[]{viewer, "jR Planar Ambisonics", targetField.getText()}).execute();
			} else if (type.equals(STEREO)) {
				JavaAmbisonicsStereoDecoder.launch(viewer, "jR Stereo");
			} else if (type.equals(VBAP)) {
				VbapSurroundRenderer.launch(viewer, "jR VBAP");
			}
			launchButton.setEnabled(false);
			renderers.setEnabled(false);
			targetField.setEnabled(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		viewer = c.getPlugin(View.class).getViewer();
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		
		renderers.setSelectedItem(c.getProperty(getClass(), RENDERKEY, JACK1));
		targetField.setText(c.getProperty(getClass(), JACKTARGETKEY, ""));
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		
		c.storeProperty(getClass(), RENDERKEY, renderers.getSelectedItem());
		c.storeProperty(getClass(), JACKTARGETKEY, targetField.getText());
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Audio Launcher";
		info.vendorName = "Peter Brinkmann"; 
		info.icon = ImageHook.getIcon("Volume-Normal-48x48.png");
		return info;
	}

}
