package de.jreality.audio.plugin;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.jreality.audio.Attenuation;
import de.jreality.audio.SoundPath;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.ui.plugin.View;
import de.jreality.ui.plugin.image.ImageHook;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

/**
 * 
 * Plugin for setting basic audio parameters such as speed of sound and such
 * 
 * @author brinkman
 *
 */
public class AudioOptions extends ShrinkPanelPlugin implements AppearanceListener {

	private Attenuation attenuation = SoundPath.DEFAULT_ATTENUATION;
	private float speedOfSound = SoundPath.DEFAULT_SPEED_OF_SOUND;
	private float gain = SoundPath.DEFAULT_GAIN;
	
	private Map<String, Attenuation> attenuations = new HashMap<String, Attenuation>();
	private Map<Attenuation, String> attenuationLabels = new HashMap<Attenuation, String>();
	
	private Appearance rootAppearance;
	
	private JTextField speedWidget, gainWidget;
	private JComboBox attenuationWidget;
	
	public AudioOptions() {
		attenuations.put("constant", Attenuation.CONSTANT);
		attenuations.put("linear", Attenuation.LINEAR);
		attenuations.put("exponential", Attenuation.EXPONENTIAL);
		
		shrinkPanel.setLayout(new GridLayout(3, 2));
		
		attenuationWidget = new JComboBox();
		for(String s: attenuations.keySet()) {
			attenuationWidget.addItem(s);
			attenuationLabels.put(attenuations.get(s), s);
		}
		
		attenuationWidget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				attenuation = attenuations.get(attenuationWidget.getSelectedItem());
				rootAppearance.setAttribute(SoundPath.VOLUME_ATTENUATION_KEY, attenuation);
			}
		});
		
		shrinkPanel.add(new JLabel("Attenuation"));
		shrinkPanel.add(attenuationWidget);
		shrinkPanel.add(new JLabel("Speed of sound"));
		shrinkPanel.add(speedWidget = new JTextField(Float.toString(speedOfSound), 7));
		shrinkPanel.add(new JLabel("Gain"));
		shrinkPanel.add(gainWidget = new JTextField(Float.toString(gain), 7));
		
		speedWidget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float newSpeed = Float.valueOf(speedWidget.getText()).floatValue();
				if (newSpeed<0) {
					updateSpeed();
				} else {
					speedOfSound = newSpeed;
					rootAppearance.setAttribute(SoundPath.SPEED_OF_SOUND_KEY, speedOfSound);
				}
			}
		});
		gainWidget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float newGain = Float.valueOf(gainWidget.getText()).floatValue();
				if (newGain<0 || newGain>4) {
					updateGain();
				} else {
					gain = newGain;
					rootAppearance.setAttribute(SoundPath.VOLUME_GAIN_KEY, gain);
				}
			}
		});
	}

	private void updateAttenuation() {
		attenuationWidget.setSelectedItem(attenuationLabels.get(attenuation));
	}
	
	private void updateSpeed() {
		speedWidget.setText(Float.toString(speedOfSound));
	}
	
	private void updateGain() {
		gainWidget.setText(Float.toString(gain));
	}
	
	public void appearanceChanged(AppearanceEvent ev) {
		Appearance appearance = (Appearance) ev.getSourceNode();
		
		Attenuation newAttenuation = (Attenuation) appearance.getAttribute(SoundPath.VOLUME_ATTENUATION_KEY);
		float newSpeedOfSound = ((Float) appearance.getAttribute(SoundPath.SPEED_OF_SOUND_KEY)).floatValue();
		float newGain = ((Float) appearance.getAttribute(SoundPath.VOLUME_GAIN_KEY)).floatValue();
		
		if (gain!=newGain) {
			gain = newGain;
			updateGain();
		}
		if (speedOfSound!=newSpeedOfSound) {
			speedOfSound = newSpeedOfSound;
			updateSpeed();
		}
		if (attenuation!=newAttenuation) {
			attenuation = newAttenuation;
			updateAttenuation();
		}
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Audio Options";
		info.vendorName = "Peter Brinkmann"; 
		info.icon = ImageHook.getIcon("radioactive.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		
		SceneGraphComponent root = c.getPlugin(View.class).getSceneRoot();
		rootAppearance = root.getAppearance();
		if (rootAppearance==null) {
			root.setAppearance(rootAppearance = new Appearance());
		}
		
		rootAppearance.setAttribute(SoundPath.VOLUME_ATTENUATION_KEY, attenuation);
		rootAppearance.setAttribute(SoundPath.SPEED_OF_SOUND_KEY, speedOfSound);
		rootAppearance.setAttribute(SoundPath.VOLUME_GAIN_KEY, gain);
		
		updateAttenuation();
		updateSpeed();
		updateGain();
		
		rootAppearance.addAppearanceListener(this);
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		
		String label = c.getProperty(getClass(), SoundPath.VOLUME_ATTENUATION_KEY, "linear");
		attenuation = attenuations.get(label);
		speedOfSound = c.getProperty(getClass(), SoundPath.SPEED_OF_SOUND_KEY, SoundPath.DEFAULT_SPEED_OF_SOUND);
		gain = c.getProperty(getClass(), SoundPath.VOLUME_GAIN_KEY, SoundPath.DEFAULT_GAIN);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		
		c.storeProperty(getClass(), SoundPath.VOLUME_ATTENUATION_KEY, attenuationLabels.get(attenuation));
		c.storeProperty(getClass(), SoundPath.SPEED_OF_SOUND_KEY, speedOfSound);
		c.storeProperty(getClass(), SoundPath.VOLUME_GAIN_KEY, gain);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}
}
