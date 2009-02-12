package de.jreality.ui.plugin.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import de.jreality.scene.SceneGraphNode;
import de.jreality.ui.plugin.view.image.ImageHook;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.flavor.FrontendFlavor;
import de.varylab.jrworkspace.plugin.flavor.PreferencesFlavor;

public class ViewPreferences extends Plugin implements
		PreferencesFlavor, FrontendFlavor, ActionListener {

	private View 
		sceneViewPlugin = null;
	private ViewMenuBar 
		sceneViewMenu = null;
	private ContentAppearance 
		contentAppearance = null;
	private JPanel 
		mainPage = new JPanel();
	private FrontendListener 
		frontendListener = null;
	private JCheckBoxMenuItem  
		fullscreenItem = new JCheckBoxMenuItem("Fullscreen");
	private JCheckBox
		threadSafeChecker = new JCheckBox("Thread Safe Scene Graph", SceneGraphNode.getThreadSafe());
	private JComboBox
		colorChooserModeCombo = new JComboBox(new String[] {"HUE", "SAT", "BRI", "RED", "GREEN", "BLUE"});
	
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
		if (fullscreenItem == s) {
			sceneViewPlugin.setHidePanels(fullscreenItem.isSelected());
			frontendListener.setShowMenuBar(!fullscreenItem.isSelected());
			frontendListener.setShowStatusBar(!fullscreenItem.isSelected());
			frontendListener.setFullscreen(fullscreenItem.isSelected());
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
		super.storeStates(c);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		threadSafeChecker.setSelected(c.getProperty(getClass(), "threadSafeSceneGraph", SceneGraphNode.getThreadSafe()));
		SceneGraphNode.setThreadSafe(threadSafeChecker.isSelected());
		colorChooserModeCombo.setSelectedIndex(c.getProperty(getClass(), "colorChooserMode", colorChooserModeCombo.getSelectedIndex()));
		super.restoreStates(c);
	}
	
	public void setFrontendListener(FrontendListener l) {
		this.frontendListener = l;
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		sceneViewPlugin = c.getPlugin(View.class);
		contentAppearance = c.getPlugin(ContentAppearance.class);
		int activeMode = colorChooserModeCombo.getSelectedIndex();
		contentAppearance.getPanel().setColorPickerMode(activeMode);
		sceneViewMenu = c.getPlugin(ViewMenuBar.class);
		sceneViewMenu.addMenuItem(getClass(), 1.0, fullscreenItem, "Viewer");
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		sceneViewMenu.removeMenuAll(getClass());
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
