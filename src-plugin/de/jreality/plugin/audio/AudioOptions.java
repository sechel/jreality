package de.jreality.plugin.audio;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.jreality.audio.DistanceCue;
import de.jreality.audio.LowPassFilter;
import de.jreality.audio.SoundPath;
import de.jreality.plugin.audio.image.ImageHook;
import de.jreality.plugin.view.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.ui.JSliderVR;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

/**
 * 
 * Plugin for setting basic audio parameters such as speed of sound and such
 * 
 * @author brinkman
 *
 */
public class AudioOptions extends ShrinkPanelPlugin implements AppearanceListener {

	private int[] selection = new int[0];
	private float speedOfSound = SoundPath.DEFAULT_SPEED_OF_SOUND;
	private float gain = SoundPath.DEFAULT_GAIN;
	
	private String[] cueLabels = {"constant", "low pass", "linear", "exponential"};
	private Class[] cueTypes = {DistanceCue.CONSTANT.class, LowPassFilter.class, DistanceCue.LINEAR.class, DistanceCue.EXPONENTIAL.class};
	
	private Appearance rootAppearance;
	
	private JSliderVR gainWidget, speedWidget;
	private JList distanceCueWidget;
	
	public AudioOptions() {
		shrinkPanel.setLayout(new ShrinkPanel.MinSizeGridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		shrinkPanel.add(new JLabel("Distance cue"), gbc);
		gbc.gridx = 1;
		shrinkPanel.add(distanceCueWidget = new JList(cueLabels), gbc);
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
		shrinkPanel.add(gainWidget = new JSliderVR(-60, 30, (int) toDecibels(gain)), gbc);
		gainWidget.setPreferredSize(new Dimension(20, 50));
		gainWidget.setMajorTickSpacing(30);
		gainWidget.setPaintTicks(true);
		gainWidget.setPaintLabels(true);
		gainWidget.setPaintTrack(true);
		
		distanceCueWidget.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				selection = distanceCueWidget.getSelectedIndices();
				List<Class<? extends DistanceCue>> list = new ArrayList<Class<? extends DistanceCue>>();
				for(int i: selection) {
					list.add((Class<? extends DistanceCue>) cueTypes[i]);
				}
				rootAppearance.setAttribute(SoundPath.DISTANCE_CUE_KEY, list);
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

	private void updateDistanceCue() {
		distanceCueWidget.setSelectedIndices(selection);
	}
	
	private void updateSpeed() {
		speedWidget.setValue((int) speedOfSound);
	}
	
	private void updateGain() {
		gainWidget.setValue((int) toDecibels(gain));
	}
	
	public void appearanceChanged(AppearanceEvent ev) {
		Appearance appearance = (Appearance) ev.getSourceNode();
		
//		List<Class<? extends DistanceCue>> list = (List<Class<? extends DistanceCue>>) appearance.getAttribute(SoundPath.DISTANCE_CUE_KEY);
//		Class<? extends DistanceCue> newDistanceCue = (list!=null && !list.isEmpty()) ? list.get(0) : DistanceCue.DEFAULT_CUE.getClass();
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
//		if (distanceCue!=newDistanceCue) {
//			distanceCue = newDistanceCue;
//			updateDistanceCue();
//		}
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
		info.icon = ImageHook.getIcon("Volume-Normal-Red-48x48.png");
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
		
//		rootAppearance.setAttribute(SoundPath.DISTANCE_CUE_KEY, selection);
		rootAppearance.setAttribute(SoundPath.SPEED_OF_SOUND_KEY, speedOfSound);
		rootAppearance.setAttribute(SoundPath.VOLUME_GAIN_KEY, gain);
		
		updateDistanceCue();
		updateSpeed();
		updateGain();
		
		rootAppearance.addAppearanceListener(this);
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		
//		selection = c.getProperty(getClass(), SoundPath.DISTANCE_CUE_KEY, new int[0]);
		speedOfSound = c.getProperty(getClass(), SoundPath.SPEED_OF_SOUND_KEY, SoundPath.DEFAULT_SPEED_OF_SOUND);
		gain = c.getProperty(getClass(), SoundPath.VOLUME_GAIN_KEY, SoundPath.DEFAULT_GAIN);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		
//		c.storeProperty(getClass(), SoundPath.DISTANCE_CUE_KEY, selection);
		c.storeProperty(getClass(), SoundPath.SPEED_OF_SOUND_KEY, speedOfSound);
		c.storeProperty(getClass(), SoundPath.VOLUME_GAIN_KEY, gain);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}
	
	private final double dbq = 20/Math.log(10);
	private float toDecibels(float q) {
		return (float) (Math.log(q)*dbq);
	}
	private float fromDecibels(float db) {
		return (float) Math.exp(db/dbq);
	}
}
