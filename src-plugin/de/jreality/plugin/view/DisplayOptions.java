package de.jreality.plugin.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToggleButton;

import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.PickShowTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.GuiUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.varylab.jrworkspace.plugin.flavor.PerspectiveFlavor;

/**
 * 
 * Plugin for quickly setting and saving camera settings and cursor/picking
 * 
 * @author brinkman
 *
 */
public class DisplayOptions extends ToolBarAggregator {

	private JToggleButton 
		pickBox = new JToggleButton(ImageHook.getIcon("mouse.png"));
	private JButton 
		loadButton = new JButton(ImageHook.getIcon("film_go.png")),
		saveButton = new JButton(ImageHook.getIcon("film_save.png"));
	
	private View view;
	private PickShowTool pickShowTool = new PickShowTool();
	
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
		setPick(pickBox.isSelected());
		loadPreferences();
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
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
	
	@Override
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}
}
