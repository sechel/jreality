package de.jreality.plugin.audio;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.jreality.audio.AudioAttributes;
import de.jreality.audio.Interpolation;
import de.jreality.audio.SoundPath;
import de.jreality.audio.javasound.JavaAmbisonicsStereoDecoder;
import de.jreality.audio.javasound.VbapSurroundRenderer;
import de.jreality.plugin.audio.image.ImageHook;
import de.jreality.plugin.view.View;
import de.jreality.scene.Viewer;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

/**
 * @deprecated use AudioPreferences instead
 */
public class AudioLauncher extends ShrinkPanelPlugin {

	private JComboBox renderWidget;
	private JComboBox interpolationWidget;
	private JButton launchButton;
	private JTextField targetField;
	
	private Viewer viewer;
	
	private static final String JACK1 = "Jack 1st order Ambisonics";
	private static final String JACK2 = "Jack 2nd order planar Ambisonics";
	private static final String STEREO = "Java Stereo";
	private static final String VBAP = "Java VBAP";
	
	private static final String SAMPLEHOLD = "No interpolation";
	private static final String LINEAR = "Linear interpolation";
	private static final String COSINE = "Cosine interpolation";
	private static final String CUBIC = "Cubic interpolation";
	
	private static final String RENDERKEY = "audioRenderer";
	private static final String JACKTARGETKEY = "jackTarget";
	private static final String INTERPOLATIONKEY = "interpolation";
	
	private Map<String, Interpolation.Factory> interpolations = new HashMap<String, Interpolation.Factory>();
	
	
	public AudioLauncher() {
		shrinkPanel.setLayout(new GridLayout(4, 1));
		
		renderWidget = new JComboBox();
		renderWidget.addItem(JACK1);
		renderWidget.addItem(JACK2);
		renderWidget.addItem(STEREO);
		renderWidget.addItem(VBAP);
		shrinkPanel.add(renderWidget);
		
		interpolationWidget = new JComboBox();
		interpolationWidget.addItem(SAMPLEHOLD);
		interpolationWidget.addItem(LINEAR);
		interpolationWidget.addItem(COSINE);
		interpolationWidget.addItem(CUBIC);
		shrinkPanel.add(interpolationWidget);
		
		interpolations.put(SAMPLEHOLD, Interpolation.SampleHold.FACTORY);
		interpolations.put(LINEAR, Interpolation.Linear.FACTORY);
		interpolations.put(COSINE, Interpolation.Cosine.FACTORY);
		interpolations.put(CUBIC, Interpolation.Cubic.FACTORY);
		
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
		SoundPath.Factory soundPathFactory = AudioAttributes.DEFAULT_SOUNDPATH_FACTORY;
		String inter = (String) interpolationWidget.getSelectedItem();
		Interpolation.Factory interpolationFactory = interpolations.get(inter);
		
		String type = (String) renderWidget.getSelectedItem();
		try {
			if (type.equals(JACK1)) {
				new Statement(Class.forName("de.jreality.audio.jack.JackAmbisonicsRenderer"),
								"launch", new Object[]{viewer, "jR Ambisonics", targetField.getText(), interpolationFactory, soundPathFactory}).execute();
			} else if (type.equals(JACK2)) {
				new Statement(Class.forName("de.jreality.audio.jack.JackAmbisonicsPlanar2ndOrderRenderer"),
						"launch", new Object[]{viewer, "jR Planar Ambisonics", targetField.getText(), interpolationFactory, soundPathFactory}).execute();
			} else if (type.equals(STEREO)) {
				JavaAmbisonicsStereoDecoder.launch(viewer, "jR Stereo", interpolationFactory, soundPathFactory);
			} else if (type.equals(VBAP)) {
				VbapSurroundRenderer.launch(viewer, "jR VBAP", interpolationFactory, soundPathFactory);
			}
			launchButton.setEnabled(false);
			renderWidget.setEnabled(false);
			interpolationWidget.setEnabled(false);
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
		
		renderWidget.setSelectedItem(c.getProperty(getClass(), RENDERKEY, JACK1));
		interpolationWidget.setSelectedItem(c.getProperty(getClass(), INTERPOLATIONKEY, CUBIC));
		targetField.setText(c.getProperty(getClass(), JACKTARGETKEY, ""));
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		
		c.storeProperty(getClass(), RENDERKEY, renderWidget.getSelectedItem());
		c.storeProperty(getClass(), INTERPOLATIONKEY, interpolationWidget.getSelectedItem());
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
		info.icon = ImageHook.getIcon("sound.png");
		return info;
	}

}
