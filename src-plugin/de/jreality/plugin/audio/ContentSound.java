package de.jreality.plugin.audio;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.plugin.view.AlignedContent;
import de.jreality.plugin.view.View;
import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class ContentSound extends ShrinkPanelPlugin {
	
	private SceneGraphComponent hum;
	private ButtonGroup buttonGroup;
	private JButton loadButton;
	private JFileChooser fileChooser;
	private HashMap<String, ButtonModel> loopToButton =
		new HashMap<String, ButtonModel>();
	private HashMap<ButtonModel, String> buttonToLoop =
		new HashMap<ButtonModel, String>();
	private AudioSource defaultAudioSource;
	private AudioSource customAudioSource;
	private SceneGraphComponent parentForSoundLoop;
	
	
	public ContentSound() {
		buttonGroup = new ButtonGroup();
		shrinkPanel.setLayout(new GridLayout(2, 2));
		buttonGroup = new ButtonGroup();
		for (String material : new String[] {"default", "none", "custom"}) {
			JRadioButton button = new JRadioButton(material);
			loopToButton.put(material, button.getModel());
			buttonToLoop.put(button.getModel(), material);
			button.getModel().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateLoop();
				}
			});
			buttonGroup.add(button);
			shrinkPanel.add(button);
		}
		
		loadButton = new JButton("load");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadSoundFile();
			}
		});
		shrinkPanel.add(loadButton);
		
		makeFileChooser();
		
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
		setCurrentDirectory(c.getProperty(getClass(), "currentDirectory", getCurrentDirectory()));
		super.restoreStates(c);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "loop", getLoop());
		c.storeProperty(getClass(), "currentDirectory", getCurrentDirectory());
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
			loadButton.setEnabled(false);
		}
		if (loop == "custom") {
			hum.setAudioSource(customAudioSource);
			loadButton.setEnabled(true);
		}
		if (loop == "none") {
			hum.setAudioSource(null);
			loadButton.setEnabled(false);
		}
	}
	
	protected AudioSource createDefaultAudioSource() {
		CachedAudioInputStreamSource h = null;
		String humName = "churchbell_loop";
		try {
			h = new CachedAudioInputStreamSource(
					"hum",
					Input.getInput("sound/"+humName+".wav"),
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
	
	private void loadSoundFile() {
		File file = null;
		if (fileChooser.showOpenDialog(shrinkPanel) == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		}
		if (file != null) {
			try {
				customAudioSource = new CachedAudioInputStreamSource("custom hum", Input.getInput(file), true);
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			customAudioSource.start();
			hum.setAudioSource(customAudioSource);
		}
	}
	
	private void makeFileChooser() {
		FileSystemView view = FileSystemView.getFileSystemView();
		FileFilter ff = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".wav");
			}
			@Override
			public String getDescription() {
				return "wav files";
			}
		};
		String texDir = ".";
		String dataDir = Secure.getProperty(SystemProperties.JREALITY_DATA);
		if (dataDir!= null) texDir = dataDir;
		File defaultDir = new File(texDir);
		fileChooser = new JFileChooser(!defaultDir.exists() ? view.getHomeDirectory() : defaultDir, view);
		fileChooser.setFileFilter(ff);
	}
	
	public String getCurrentDirectory() {
		return fileChooser.getCurrentDirectory().getAbsolutePath();
	}

	public void setCurrentDirectory(String directory) {
		File dir = new File(directory);
		if (dir.exists() && dir.isDirectory()) {
			fileChooser.setCurrentDirectory(dir);
		} else {
			System.out.println(
					"failed to restore sound directory "+directory
			);
		}
	}
}
