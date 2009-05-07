package de.jreality.plugin.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.PickShowTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.GuiUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.varylab.jrworkspace.plugin.flavor.FrontendFlavor;
import de.varylab.jrworkspace.plugin.flavor.PerspectiveFlavor;

/**
 * 
 * Plugin for quickly setting and saving camera settings and cursor/picking
 * 
 * @author brinkman
 *
 */
public class DisplayOptions extends ToolBarAggregator implements ActionListener, FrontendFlavor {

	private JToggleButton 
		pickBox = new JToggleButton(ImageHook.getIcon("mouse.png"));
	private JButton 
		loadButton = new JButton(ImageHook.getIcon("film_go.png")),
		saveButton = new JButton(ImageHook.getIcon("film_save.png"));
	private JCheckBoxMenuItem  
		fullscreenItem = new JCheckBoxMenuItem("Fullscreen", ImageHook.getIcon("arrow_out.png"));
	private boolean 
		windowedHidePanelsTmp = false;
	private FrontendListener
		frontendListener = null;
	
	private View 
		view = null;
	private ViewMenuBar
		viewMenuBar = null;
	private PickShowTool 
		pickShowTool = new PickShowTool();
	
	public DisplayOptions() {
		addTool(getClass(), 1, loadButton);
		addTool(getClass(), 2, saveButton);
		addSeparator(getClass(), 3);
		addTool(getClass(), 4, pickBox);
		
		loadButton.setToolTipText("Load Camera");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadPreferences();
			}
		});
		saveButton.setToolTipText("Save Camera");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				savePreferences();
			}
		});
		pickBox.setToolTipText("Show Pick");
		pickBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPick(pickBox.isSelected());
			}
		});
		fullscreenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
		fullscreenItem.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (fullscreenItem == e.getSource()) {
			boolean fs = fullscreenItem.isSelected();
			if (fs) {
				windowedHidePanelsTmp = view.isHidePanels();
				view.setHidePanels(true);
			} else {
				view.setHidePanels(windowedHidePanelsTmp);
			}
			frontendListener.setShowMenuBar(!fs);
			frontendListener.setShowToolBar(!fs);
			frontendListener.setShowStatusBar(!fs);
			frontendListener.setFullscreen(fs);
			frontendListener.updateFrontendUI();
			view.getViewer().getViewingComponent().requestFocusInWindow();
		}
	}
	
	
	private void setPick(boolean showPick) {
		Component frame = view.getViewer().getViewingComponent();
		if (showPick) {
			GuiUtility.hideCursor(frame);
		} else {
			GuiUtility.showCursor(frame);
		}
		
		SceneGraphComponent root = view.getViewer().getSceneRoot();
		if (showPick && !root.getTools().contains(pickShowTool)) {
			root.addTool(pickShowTool);
		}
		if (!showPick && root.getTools().contains(pickShowTool)) {
			root.removeTool(pickShowTool);
		}
	}
	
	private void savePreferences() {
		CameraUtility.savePreferences((Camera) view.getCameraPath().getLastElement());
	}

	private void loadPreferences() {
		CameraUtility.loadPreferences((Camera) view.getCameraPath().getLastElement());
		view.getViewer().renderAsync();
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Display Options";
		info.vendorName = "Peter Brinkmann"; 
		info.icon = ImageHook.getIcon("camera.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		c.getPlugin(CameraStand.class);
		view = c.getPlugin(View.class);
		viewMenuBar = c.getPlugin(ViewMenuBar.class);
		viewMenuBar.addMenuItem(getClass(), 1.0, fullscreenItem, "Viewer");
		setPick(pickBox.isSelected());
		loadPreferences();
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		viewMenuBar.removeAll(getClass());
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		pickBox.setSelected(c.getProperty(getClass(), "showPick", false));
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "showPick", pickBox.isSelected());
	}
	
	@Override
	public double getToolBarPriority() {
		return 1.0;
	}
	
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}
	
	@Override
	public void setFrontendListener(FrontendListener l) {
		frontendListener = l;
	}
	
}
