package de.jreality.audio.plugin;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JRadioButton;

import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.plugin.AlignedContent;
import de.jreality.ui.plugin.View;
import de.jreality.ui.plugin.image.ImageHook;
import de.jreality.util.Input;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class ContentSound extends ShrinkPanelPlugin {
	
	private SceneGraphComponent hum;
	private ButtonGroup buttonGroup;
	private HashMap<String, ButtonModel> loopToButton =
		new HashMap<String, ButtonModel>();
	private HashMap<ButtonModel, String> buttonToLoop =
		new HashMap<ButtonModel, String>();
	private AudioSource defaultAudioSource;
	private AudioSource customAudioSource;
	private SceneGraphComponent parentForSoundLoop;
	
	
	public ContentSound() {
		buttonGroup = new ButtonGroup();
		shrinkPanel.setLayout(new GridLayout());
		buttonGroup = new ButtonGroup();
		for (String material : new String[] {"default", "costum", "none"}) {
			JRadioButton button = new JRadioButton(material);
			loopToButton.put(material, button.getModel());
			buttonToLoop.put(button.getModel(), material);
			button.getModel().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					updateLoop();
				}
			});
			buttonGroup.add(button);
			shrinkPanel.add(button);
		}
		defaultAudioSource = createDefaultAudioSource();
		setLoop("none");
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Content Sound";
		info.vendorName = "Ulrich Pinkall"; 
		info.icon = ImageHook.getIcon("radioactive1.png");
		return info;
	}
	
	public void install(SceneGraphComponent parentForSoundLoop) {
		this.parentForSoundLoop = parentForSoundLoop;
		hum = new SceneGraphComponent("hum");
		hum.setAudioSource(createDefaultAudioSource());
		parentForSoundLoop.addChild(hum);
		updateLoop();
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		setLoop(c.getProperty(getClass(), "loop", getLoop()));
		super.restoreStates(c);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "loop", getLoop());
		super.storeStates(c);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		c.getPlugin(Audio.class);
		SceneGraphComponent scalingComponent =
			c.getPlugin(AlignedContent.class).getScalingComponent();
		install(scalingComponent);
		updateLoop();
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		parentForSoundLoop.removeChild(hum);
	}

	
	public String getLoop() {
		return buttonToLoop.get(buttonGroup.getSelection());
	}
	
	public void setLoop(String loop) {
		loopToButton.get(loop).setSelected(true);
	}
	
	private void updateLoop() {
		String loop = getLoop();
		if (loop == "default") {
			hum.setAudioSource(defaultAudioSource);
		}
		if (loop == "custom") {
			hum.setAudioSource(customAudioSource);
		}
		if (loop == "none") {
			hum.setAudioSource(null);
		}
	}
	
	protected AudioSource createDefaultAudioSource() {
		CachedAudioInputStreamSource h = null;
		String humName = "churchbell_loop";
		try {
			h = new CachedAudioInputStreamSource(
					"hum",
					Input.getInput("data/"+humName+".wav"),
					true
			);
			h.start();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return h;
	}
}
