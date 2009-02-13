package de.jreality.plugin.audio;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.audio.Attenuation;
import de.jreality.audio.SoundPath;
import de.jreality.plugin.view.View;
import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.ui.JSliderVR;
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
	
	private JSliderVR gainWidget, speedWidget;
	private JComboBox attenuationWidget;
	
	public AudioOptions() {
		attenuations.put("constant", Attenuation.CONSTANT);
		attenuations.put("linear", Attenuation.LINEAR);
		attenuations.put("exponential", Attenuation.EXPONENTIAL);
		
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		shrinkPanel.add(new JLabel("Attenuation"), gbc);
		gbc.gridx = 1;
		shrinkPanel.add(attenuationWidget = new JComboBox(), gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		shrinkPanel.add(new JLabel("Speed of sound (m/s)"), gbc);
		gbc.gridx = 1;
		shrinkPanel.add(speedWidget = new JSliderVR(0, 1000), gbc);
		speedWidget.setPreferredSize(new Dimension(20, 50));
		speedWidget.setMajorTickSpacing(500);
		speedWidget.setPaintTicks(true);
		speedWidget.setPaintLabels(true);
		speedWidget.setPaintTrack(true);
		gbc.gridx = 0;
		gbc.gridy = 2;
		shrinkPanel.add(new JLabel("Gain (dB)"), gbc);
		gbc.gridx = 1;
		shrinkPanel.add(gainWidget = new JSliderVR(-80, 40, (int) toDecibels(gain)), gbc);
		gainWidget.setPreferredSize(new Dimension(20, 50));
		gainWidget.setMajorTickSpacing(40);
		gainWidget.setPaintTicks(true);
		gainWidget.setPaintLabels(true);
		gainWidget.setPaintTrack(true);
		
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
		
		speedWidget.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				speedOfSound = speedWidget.getValue();
				rootAppearance.setAttribute(SoundPath.SPEED_OF_SOUND_KEY, speedOfSound);
			}
		});
		gainWidget.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				gain = fromDecibels(gainWidget.getValue());
				rootAppearance.setAttribute(SoundPath.VOLUME_GAIN_KEY, gain);
			}
		});
	}

	private void updateAttenuation() {
		attenuationWidget.setSelectedItem(attenuationLabels.get(attenuation));
	}
	
	private void updateSpeed() {
		speedWidget.setValue((int) speedOfSound);
	}
	
	private void updateGain() {
		gainWidget.setValue((int) toDecibels(gain));
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
	
	private final double dbq = 10/Math.log(2);
	private float toDecibels(float q) {
		return (float) (Math.log(q)*dbq);
	}
	private float fromDecibels(float db) {
		return (float) Math.exp(db/dbq);
	}
}
