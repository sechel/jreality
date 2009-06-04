package de.jreality.plugin.basic;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.jreality.plugin.icon.ImageHook;
import de.jreality.scene.SceneGraphNode;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.flavor.PreferencesFlavor;

public class ViewPreferences extends Plugin implements PreferencesFlavor, ActionListener, ComponentListener {

	private JPanel 
		mainPage = new JPanel();
	private JCheckBox
		threadSafeChecker = new JCheckBox("Thread Safe Scene Graph", SceneGraphNode.getThreadSafe());
	private JCheckBox
		toolBarChecker = new JCheckBox("Show Tool bar");
	private JComboBox
		colorChooserModeCombo = new JComboBox(new String[] {"HUE", "SAT", "BRI", "RED", "GREEN", "BLUE"});
	private List<ColorPickerModeChangedListener>
		colorModeListeners = new LinkedList<ColorPickerModeChangedListener>();
	private ViewToolBar toolBar;
	
	public static interface ColorPickerModeChangedListener {
		
		public void colorPickerModeChanged(int mode);
		
	}
	
	public ViewPreferences() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2,2,2,2);
		c.anchor = GridBagConstraints.CENTER;
		mainPage.setLayout(new GridBagLayout());
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		mainPage.add(threadSafeChecker, c);
		mainPage.add(toolBarChecker, c);
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		mainPage.add(new JLabel("Color Chooser Mode"), c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		mainPage.add(colorChooserModeCombo, c);
		colorChooserModeCombo.setSelectedIndex(1);
		
		threadSafeChecker.addActionListener(this);
		toolBarChecker.addActionListener(this);
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
		if (threadSafeChecker == s) {
			System.out.println("ThreadSafe is " + threadSafeChecker.isSelected());
			SceneGraphNode.setThreadSafe(threadSafeChecker.isSelected());
		}
		if (colorChooserModeCombo == s) {
			fireColorModeChanged(colorChooserModeCombo.getSelectedIndex());
		}
		if (toolBarChecker == s) {
			setToolBarVisible(toolBarChecker.isSelected());
		}
	}

	public void setToolBarVisible(boolean selected) {
		toolBarChecker.setSelected(selected);
		if (toolBar != null) {
			toolBar.getToolBarComponent().setVisible(selected);
		}
	}

	public boolean isToolBarVisible() {
		return toolBarChecker.isSelected();
	}
	
	protected void fireColorModeChanged(int mode) {
		for (ColorPickerModeChangedListener l : colorModeListeners) {
			l.colorPickerModeChanged(mode);
		}
	}
	
	
	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "threadSafeSceneGraph", SceneGraphNode.getThreadSafe());
		c.storeProperty(getClass(), "colorChooserMode", colorChooserModeCombo.getSelectedIndex());
		c.storeProperty(getClass(), "toolBarVisible", isToolBarVisible());
		super.storeStates(c);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		threadSafeChecker.setSelected(c.getProperty(getClass(), "threadSafeSceneGraph", SceneGraphNode.getThreadSafe()));
		SceneGraphNode.setThreadSafe(threadSafeChecker.isSelected());
		colorChooserModeCombo.setSelectedIndex(c.getProperty(getClass(), "colorChooserMode", colorChooserModeCombo.getSelectedIndex()));
		setToolBarVisible(c.getProperty(getClass(), "toolBarVisible", isToolBarVisible()));
		super.restoreStates(c);
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
	
	public int getColorPickerMode() {
		return colorChooserModeCombo.getSelectedIndex();
	}
	
	public boolean addColorPickerChangedListener(ColorPickerModeChangedListener l) {
		return colorModeListeners.add(l);
	}
	
	public boolean removeColorPickerChangedListener(ColorPickerModeChangedListener l) {
		return colorModeListeners.remove(l);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		toolBar = c.getPlugin(ViewToolBar.class);
		setToolBarVisible(isToolBarVisible());
		toolBar.getToolBarComponent().addComponentListener(this);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		toolBar.getToolBarComponent().removeComponentListener(this);
	}
	public void componentHidden(ComponentEvent e) {
		setToolBarVisible(false);
	}
	public void componentMoved(ComponentEvent e) {
	}
	public void componentResized(ComponentEvent e) {
	}
	public void componentShown(ComponentEvent e) {
		setToolBarVisible(true);
	}
}
