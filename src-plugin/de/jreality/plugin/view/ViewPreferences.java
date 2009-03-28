package de.jreality.plugin.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import de.jreality.plugin.view.image.ImageHook;
import de.jreality.reader.ReaderJRS;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.writer.WriterJRS;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.flavor.FrontendFlavor;
import de.varylab.jrworkspace.plugin.flavor.PreferencesFlavor;

public class ViewPreferences extends Plugin implements
		PreferencesFlavor, FrontendFlavor, ActionListener {

	private View 
		view = null;
	private ViewMenuBar 
		viewMenuBar = null;
	private ContentAppearance 
		contentAppearance = null;
	private AlignedContent
		content = null;
	private JPanel 
		mainPage = new JPanel();
	private FrontendListener 
		frontendListener = null;
	private JCheckBoxMenuItem  
		fullscreenItem = new JCheckBoxMenuItem("Fullscreen");
	private JCheckBox
		threadSafeChecker = new JCheckBox("Thread Safe Scene Graph", SceneGraphNode.getThreadSafe()),
		saveSceneContent = new JCheckBox("Save Scene Content");
	private JComboBox
		colorChooserModeCombo = new JComboBox(new String[] {"HUE", "SAT", "BRI", "RED", "GREEN", "BLUE"});
	private SceneGraphComponent
		storedContent = null;
	
	public ViewPreferences() {
		fullscreenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
	
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2,2,2,2);
		c.anchor = GridBagConstraints.CENTER;
		mainPage.setLayout(new GridBagLayout());
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		mainPage.add(threadSafeChecker, c);
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		mainPage.add(new JLabel("Color Chooser Mode"), c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		mainPage.add(colorChooserModeCombo, c);
		colorChooserModeCombo.setSelectedIndex(1);
		mainPage.add(saveSceneContent, c);
		
		threadSafeChecker.addActionListener(this);
		fullscreenItem.addActionListener(this);
		colorChooserModeCombo.addActionListener(this);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Viewer Preferences";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("preferences.png");
		return info;
	}
	
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		boolean fs = fullscreenItem.isSelected();
		if (fullscreenItem == s) {
			view.setHidePanels(fs);
			frontendListener.setShowMenuBar(!fs);
			frontendListener.setShowToolBar(!fs);
			frontendListener.setShowStatusBar(!fs);
			frontendListener.setFullscreen(fs);
			frontendListener.updateFrontendUI();
		} else if (threadSafeChecker == s) {
			System.out.println("ThreadSafe is " + threadSafeChecker.isSelected());
			SceneGraphNode.setThreadSafe(threadSafeChecker.isSelected());
		} else if (colorChooserModeCombo == s) {
			if (contentAppearance != null) {
				contentAppearance.getPanel().setColorPickerMode(colorChooserModeCombo.getSelectedIndex());
			}
		}
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "threadSafeSceneGraph", SceneGraphNode.getThreadSafe());
		c.storeProperty(getClass(), "colorChooserMode", colorChooserModeCombo.getSelectedIndex());
		c.storeProperty(getClass(), "saveSceneContent", saveSceneContent.isSelected());
		try {
			if (saveSceneContent.isSelected()) {
				SceneGraphComponent cgc = content.getContent();
				if (cgc == null) {
					cgc = new SceneGraphComponent();
				}
				StringWriter sw = new StringWriter();
				WriterJRS writerJRS = new WriterJRS();
				writerJRS.write(cgc, sw);
				String sceneString = sw.getBuffer().toString();
				c.storeProperty(getClass(), "sceneContent", sceneString);
			} else {
				c.deleteProperty(getClass(), "sceneContent");
			}
		} catch (Exception e) {
			System.out.println("Cannot store scene to properties: " + e.getMessage());
		}
		super.storeStates(c);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		threadSafeChecker.setSelected(c.getProperty(getClass(), "threadSafeSceneGraph", SceneGraphNode.getThreadSafe()));
		SceneGraphNode.setThreadSafe(threadSafeChecker.isSelected());
		colorChooserModeCombo.setSelectedIndex(c.getProperty(getClass(), "colorChooserMode", colorChooserModeCombo.getSelectedIndex()));
		saveSceneContent.setSelected(c.getProperty(getClass(), "saveSceneContent", saveSceneContent.isSelected()));
		try {
			if (saveSceneContent.isSelected()) {
				String sceneString  = c.getProperty(getClass(), "sceneContent", null);
				File tmpSceneFile = File.createTempFile("storedScene", "jrs");
				FileWriter fw = new FileWriter(tmpSceneFile);
				fw.append(sceneString);
				fw.close();
				ReaderJRS readerJRS = new ReaderJRS();
				storedContent = readerJRS.read(tmpSceneFile);
				tmpSceneFile.delete();
			}
		} catch (Exception e) {
			System.out.println("Cannot restore scene from properties: " + e.getMessage());
		}
		super.restoreStates(c);
	}
	
	public void setFrontendListener(FrontendListener l) {
		this.frontendListener = l;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		view = c.getPlugin(View.class);
		contentAppearance = c.getPlugin(ContentAppearance.class);
		int activeMode = colorChooserModeCombo.getSelectedIndex();
		contentAppearance.getPanel().setColorPickerMode(activeMode);
		viewMenuBar = c.getPlugin(ViewMenuBar.class);
		viewMenuBar.addMenuItem(getClass(), 1.0, fullscreenItem, "Viewer");
		content = c.getPlugin(AlignedContent.class);
		if (storedContent != null) {
			content.setContent(storedContent);
		}
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		viewMenuBar.removeAll(getClass());
	}

	public Icon getMainIcon() {
		return null;
	}

	public String getMainName() {
		return "jReality Viewer";
	}

	public JPanel getMainPage() {
		return mainPage;
	}

	public int getNumSubPages() {
		return 0;
	}

	public JPanel getSubPage(int i) {
		return null;
	}

	public Icon getSubPageIcon(int i) {
		return null;
	}

	public String getSubPageName(int i) {
		return null;
	}

}
