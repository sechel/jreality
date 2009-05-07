package de.jreality.plugin.audio;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.jreality.audio.AudioAttributes;
import de.jreality.audio.DistanceCue;
import de.jreality.audio.DistanceCueChain;
import de.jreality.audio.DistanceCueFactory;
import de.jreality.audio.EarlyReflections;
import de.jreality.audio.FDNReverb;
import de.jreality.audio.SampleProcessor;
import de.jreality.audio.SampleProcessorChain;
import de.jreality.audio.SampleProcessorFactory;
import de.jreality.audio.SchroederReverb;
import de.jreality.audio.ShiftProcessor;
import de.jreality.plugin.audio.image.ImageHook;
import de.jreality.plugin.view.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
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
 * TODO: Implement AppearanceListener to update widgets in line with appearance changes.
 * 
 * @author brinkman
 *
 */
public class AudioOptions extends ShrinkPanelPlugin {

	private float speedOfSound = AudioAttributes.DEFAULT_SPEED_OF_SOUND;
	private float gain = AudioAttributes.DEFAULT_GAIN;
	private float reverbGain = AudioAttributes.DEFAULT_DIRECTIONLESS_GAIN;
	private float reverbTime = AudioAttributes.DEFAULT_REVERB_TIME;
	private float pitchShift = 1;

	private int[] selectedProcs = new int[0];
	private final String[] procLabels = {"none", "reflections", "pitch shift"};
	private class PreProcessorFactory implements SampleProcessorFactory {
		public SampleProcessor getInstance() {
			List<SampleProcessor> list = new ArrayList<SampleProcessor>(2);
			for(int i: selectedProcs) {
				int cnt = 1;
				if      (i==cnt++) list.add(new EarlyReflections());
				else if (i==cnt++) list.add(new ShiftProcessor());
			}
			return SampleProcessorChain.create(list);
		}
	};

	private int[] selectedCues = new int[0];
	private final String[] cueLabels = {"none", "conical", "cardioid", "low pass", "linear", "exponential"};
	private class DirectedCueFactory implements DistanceCueFactory {
		public DistanceCue getInstance() {
			List<DistanceCue> list = new ArrayList<DistanceCue>(4);
			for(int i: selectedCues) {
				int cnt = 1;
				if      (i==cnt++) list.add(new DistanceCue.CONICAL());
				else if (i==cnt++) list.add(new DistanceCue.CARDIOID());
				else if (i==cnt++) list.add(new DistanceCue.LOWPASS());
				else if (i==cnt++) list.add(new DistanceCue.LINEAR());
				else if (i==cnt++) list.add(new DistanceCue.EXPONENTIAL());
			}
			return DistanceCueChain.create(list);
		}
	};

	private int reverbType = 0;
	private final String[] reverbLabels = {"none", "Schroeder", "FDN"};
	private class ReverbFactory implements SampleProcessorFactory {
		public SampleProcessor getInstance() {
			int cnt = 1;
			if      (reverbType==cnt++) return new SchroederReverb();
			else if (reverbType==cnt++) return new FDNReverb();
			else                    return new SampleProcessor.NullProcessor();
		}
	};

	private Appearance rootAppearance;

	private JSliderVR gainWidget, speedWidget, reverbGainWidget, reverbTimeWidget, pitchShiftWidget;
	private JList procWidget, distanceCueWidget, reverbWidget;

	public AudioOptions() {
		speedWidget = new JSliderVR(0, 1000);
		gainWidget = new JSliderVR(-60, 30, (int) toDecibels(gain));
		pitchShiftWidget = new JSliderVR(-120, 120, (int) (toCents(pitchShift)/10));
		procWidget = new JList(procLabels);
		distanceCueWidget = new JList(cueLabels);
		reverbWidget = new JList(reverbLabels);
		reverbWidget.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reverbTimeWidget = new JSliderVR(0, 50, (int) reverbTime*10);
		reverbGainWidget = new JSliderVR(-60, 30, (int) toDecibels(reverbGain));
		
		procWidget.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				selectedProcs = procWidget.getSelectedIndices();
				setProcessorAttribute();
			}
		});
		distanceCueWidget.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				selectedCues = distanceCueWidget.getSelectedIndices();
				setDistanceCueAttribute();
			}
		});
		pitchShiftWidget.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				pitchShift = fromCents(pitchShiftWidget.getValue()*10);
				setPitchAttribute();
			}
		});
		speedWidget.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				speedOfSound = speedWidget.getValue();
				setSpeedAttribute();
			}
		});
		gainWidget.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				gain = fromDecibels(gainWidget.getValue());
				setGainAttribute();
			}
		});
		reverbWidget.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				reverbType = reverbWidget.getSelectedIndex();
				if (reverbType<0) {
					reverbType = 0;
				}
				setReverbAttribute();
			}
		});
		reverbGainWidget.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				reverbGain = fromDecibels(reverbGainWidget.getValue());
				setReverbGainAttribute();
			}
		});
		reverbTimeWidget.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				reverbTime = (float) reverbTimeWidget.getValue()/10;
				setReverbTimeAttribute();
			}
		});
		
		JPanel generalPanel = new JPanel();
		JPanel preprocPanel = new JPanel();
		JPanel distCuePanel = new JPanel();
		JPanel reverbPanel = new JPanel();
		
		generalPanel.setLayout(new GridBagLayout());
		generalPanel.setBorder(BorderFactory.createTitledBorder("General"));
		preprocPanel.setLayout(new GridBagLayout());
		preprocPanel.setBorder(BorderFactory.createTitledBorder("Preprocessor"));
		distCuePanel.setLayout(new GridBagLayout());
		distCuePanel.setBorder(BorderFactory.createTitledBorder("Distance cues"));
		reverbPanel.setLayout(new GridBagLayout());
		reverbPanel.setBorder(BorderFactory.createTitledBorder("Reverb"));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.weighty = 0;

		gbc.weightx = 0;
		gbc.gridwidth = 1;
		generalPanel.add(new JLabel("Speed of sound (m/s)"), gbc);
		gbc.weightx = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		generalPanel.add(speedWidget, gbc);

		gbc.weightx = 0;
		gbc.gridwidth = 1;
		generalPanel.add(new JLabel("Gain (dB)"), gbc);
		gbc.weightx = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		generalPanel.add(gainWidget, gbc);

		preprocPanel.add(procWidget, gbc);

		gbc.weightx = 0;
		gbc.gridwidth = 1;
		preprocPanel.add(new JLabel("Pitch shift (10 cents)"), gbc);
		gbc.weightx = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		preprocPanel.add(pitchShiftWidget, gbc);

		distCuePanel.add(distanceCueWidget, gbc);

		reverbPanel.add(reverbWidget, gbc);

		gbc.weightx = 0;
		gbc.gridwidth = 1;
		reverbPanel.add(new JLabel("Time (.1 sec)"), gbc);
		gbc.weightx = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		reverbPanel.add(reverbTimeWidget, gbc);

		gbc.weightx = 0;
		gbc.gridwidth = 1;
		reverbPanel.add(new JLabel("Gain (dB)"), gbc);
		gbc.weightx = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		reverbPanel.add(reverbGainWidget, gbc);
		
		shrinkPanel.setLayout(new ShrinkPanel.MinSizeGridBagLayout());
		gbc.insets = new Insets(0, 0, 0, 0);
		shrinkPanel.add(generalPanel, gbc);
		shrinkPanel.add(preprocPanel, gbc);
		shrinkPanel.add(distCuePanel, gbc);
		shrinkPanel.add(reverbPanel, gbc);
	}

	private void setProcessorAttribute() {
		rootAppearance.setAttribute(AudioAttributes.PREPROCESSOR_KEY, new PreProcessorFactory());
	}

	private void setDistanceCueAttribute() {
		rootAppearance.setAttribute(AudioAttributes.DISTANCE_CUE_KEY, new DirectedCueFactory());
	}

	private void setReverbAttribute() {
		rootAppearance.setAttribute(AudioAttributes.DIRECTIONLESS_PROCESSOR_KEY, (reverbType==0) ? Appearance.DEFAULT : new ReverbFactory());
	}

	private void setSpeedAttribute() {
		rootAppearance.setAttribute(AudioAttributes.SPEED_OF_SOUND_KEY, speedOfSound);
	}

	private void setPitchAttribute() {
		rootAppearance.setAttribute(AudioAttributes.PITCH_SHIFT_KEY, pitchShift);
	}

	private void setGainAttribute() {
		rootAppearance.setAttribute(AudioAttributes.VOLUME_GAIN_KEY, gain);
	}

	private void setReverbGainAttribute() {
		rootAppearance.setAttribute(AudioAttributes.DIRECTIONLESS_GAIN_KEY, reverbGain);
	}

	private void setReverbTimeAttribute() {
		rootAppearance.setAttribute(AudioAttributes.REVERB_TIME_KEY, reverbTime);
	}

	private void updateProcWidget() {
		procWidget.setSelectedIndices(selectedProcs);
	}

	private void updateCueWidget() {
		distanceCueWidget.setSelectedIndices(selectedCues);
	}

	private void updatePitchWidget() {
		pitchShiftWidget.setValue((int) (toCents(pitchShift)/10));
	}
	private void updateSpeedWidget() {
		speedWidget.setValue((int) speedOfSound);
	}

	private void updateGainWidget() {
		gainWidget.setValue((int) toDecibels(gain));
	}

	private void updateReverbWidget() {
		reverbWidget.setSelectedIndex(reverbType);
	}

	private void updateReverbGainWidget() {
		reverbGainWidget.setValue((int) toDecibels(reverbGain));
	}

	private void updateReverbTimeWidget() {
		reverbTimeWidget.setValue((int) reverbTime*10);
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
		info.icon = ImageHook.getIcon("sound_add.png");
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

		updateProcWidget();
		updateCueWidget();
		updatePitchWidget();
		updateSpeedWidget();
		updateGainWidget();
		updateReverbWidget();
		updateReverbGainWidget();
		updateReverbTimeWidget();
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);

		try {
			selectedProcs = c.getProperty(getClass(), AudioAttributes.PREPROCESSOR_KEY, new int[0]);
			selectedCues = c.getProperty(getClass(), AudioAttributes.DISTANCE_CUE_KEY, new int[0]);
			speedOfSound = c.getProperty(getClass(), AudioAttributes.SPEED_OF_SOUND_KEY, AudioAttributes.DEFAULT_SPEED_OF_SOUND);
			pitchShift = c.getProperty(getClass(), AudioAttributes.PITCH_SHIFT_KEY, AudioAttributes.DEFAULT_PITCH_SHIFT);
			gain = c.getProperty(getClass(), AudioAttributes.VOLUME_GAIN_KEY, AudioAttributes.DEFAULT_GAIN);
			reverbType = c.getProperty(getClass(), AudioAttributes.DIRECTIONLESS_PROCESSOR_KEY, 0);
			reverbGain = c.getProperty(getClass(), AudioAttributes.DIRECTIONLESS_GAIN_KEY, AudioAttributes.DEFAULT_DIRECTIONLESS_GAIN);
			reverbTime = c.getProperty(getClass(), AudioAttributes.REVERB_TIME_KEY, AudioAttributes.DEFAULT_REVERB_TIME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);

		c.storeProperty(getClass(), AudioAttributes.PREPROCESSOR_KEY, selectedProcs);
		c.storeProperty(getClass(), AudioAttributes.DISTANCE_CUE_KEY, selectedCues);
		c.storeProperty(getClass(), AudioAttributes.SPEED_OF_SOUND_KEY, speedOfSound);
		c.storeProperty(getClass(), AudioAttributes.PITCH_SHIFT_KEY, pitchShift);
		c.storeProperty(getClass(), AudioAttributes.VOLUME_GAIN_KEY, gain);
		c.storeProperty(getClass(), AudioAttributes.DIRECTIONLESS_PROCESSOR_KEY, reverbType);
		c.storeProperty(getClass(), AudioAttributes.DIRECTIONLESS_GAIN_KEY, reverbGain);
		c.storeProperty(getClass(), AudioAttributes.REVERB_TIME_KEY, reverbTime);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}

	private static final double dbq = 20/Math.log(10);
	private float toDecibels(float q) {
		return (float) (Math.log(q)*dbq);
	}
	private float fromDecibels(float db) {
		return (float) Math.exp(db/dbq);
	}

	private static final double cpq = 1200/Math.log(2);
	private float toCents(float q) {
		return (float) (Math.log(q)*cpq);
	}
	private float fromCents(float c) {
		return (float) Math.exp(c/cpq);
	}
}
