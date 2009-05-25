package de.jreality.plugin.basic.scene;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import de.jreality.plugin.basic.View;
import de.jreality.plugin.vr.image.ImageHook;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.PluginNameComparator;
import de.varylab.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.varylab.jrworkspace.plugin.flavor.PerspectiveFlavor;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public class HeadUpDisplay extends ToolBarAggregator implements ActionListener {

	private Controller
		c = null;
	private WindowManager
		windowManager = null;
	private UpdateTimer
		timer = null;
	private Set<ShrinkPanelPlugin>
		shrinkers = new HashSet<ShrinkPanelPlugin>();
	private Map<ShrinkPanelPlugin, JFrame>
		pluginWindowMap = new HashMap<ShrinkPanelPlugin, JFrame>();
	private Map<String, ShrinkPanelPlugin>
		commandMap = new HashMap<String, ShrinkPanelPlugin>();
	private JFrame
		mainPanel = null;
	private GridBagConstraints
		panelConstraints = new GridBagConstraints(),
		masterConstraints = new GridBagConstraints();
	
	private JToggleButton
		showMasterPanelToggle = new JToggleButton(ImageHook.getIcon("layout_add.png"), false);
	private Map<ShrinkPanelPlugin, JCheckBoxMenuItem> checkBoxes = new HashMap<ShrinkPanelPlugin, JCheckBoxMenuItem>();
	
	public HeadUpDisplay() {
		panelConstraints.fill = GridBagConstraints.BOTH;
		panelConstraints.insets = new Insets(0,5,5,5);
		panelConstraints.weighty = 1.0;
		panelConstraints.weightx = 1.0;
		panelConstraints.gridwidth = GridBagConstraints.REMAINDER;
		
		masterConstraints.fill = GridBagConstraints.BOTH;
		masterConstraints.insets = new Insets(2,5,2,5);
		masterConstraints.weighty = 1.0;
		masterConstraints.weightx = 1.0;
		masterConstraints.gridwidth = GridBagConstraints.REMAINDER;
		
		showMasterPanelToggle.setToolTipText("In-Scene Master Panel");
		showMasterPanelToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mainPanel != null) {
					SwingUtilities.updateComponentTreeUI(getMainFrame());
					getMainFrame().setVisible(showMasterPanelToggle.isSelected());
				}
			}
		});
		addTool(getClass(), 0.0, showMasterPanelToggle);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		final ShrinkPanelPlugin plugin = commandMap.get(command);
		final ShrinkPanel sp = plugin.getShrinkPanel();
		final JPanel content = sp.getContentPanel();
		
		JFrame frame = pluginWindowMap.get(plugin);
		if (frame == null) {
			final JFrame fframe = windowManager.createFrame(plugin.getClass().getSimpleName());
			pluginWindowMap.put(plugin, fframe);
			fframe.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					checkBoxes.get(plugin).setSelected(false);
				}
			});
			frame = fframe;
		}
		Container frameContent = frame.getContentPane();
		if (frame.isShowing()) {
			frameContent.remove(content);
			frame.setVisible(false);
			plugin.setShowPanel(true);
			sp.setContentPanel(content);
		} else {
			plugin.setShowPanel(false);
			content.doLayout();
			frameContent.remove(content);
			frameContent.setLayout(new GridBagLayout());
			frameContent.add(content, panelConstraints);
			
			Dimension size = content.getSize();
			if (size.height == 0 || size.width == 0) {
				LayoutManager layout = content.getLayout();
				size = layout.minimumLayoutSize(content);
				int width = 250;//((ShrinkSlotVertical)view.getLeftSlot()).getPreferredWidth();
				Dimension pref = layout.preferredLayoutSize(content);
				if (pref.height > size.height) {
					size.height = pref.height;
				}
				size.width = width;
			}
			frame.setSize(size);
			frame.setVisible(true);
		}
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "showMasterPanel", showMasterPanelToggle.isSelected());
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		showMasterPanelToggle.setSelected(c.getProperty(getClass(), "showMasterPanel", showMasterPanelToggle.isSelected()));
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		this.c = c;
		windowManager = c.getPlugin(WindowManager.class);
		timer = new UpdateTimer();
		windowManager.windowRoot.addTool(timer);
	}
	
	
	@Override
	public void uninstall(Controller c) throws Exception {
		windowManager.windowRoot.removeTool(timer);
		super.uninstall(c);
		List<ShrinkPanelPlugin> sList = new LinkedList<ShrinkPanelPlugin>(shrinkers);
		for (ShrinkPanelPlugin p : sList) {
			removeShrinkerButton(p);
		}
		getMainFrame().setVisible(false);
	}
	
	
	public JFrame getMainFrame() {
		if (mainPanel == null) {
			mainPanel = windowManager.createFrame("HeadUp Display");
		}
		return mainPanel;
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Head-Up Display";
		info.icon = ImageHook.getIcon("layout_add.png");
		info.vendorName = "Stefan Sechelmann";
		return info;
	}

	
	public void addShrinkerButton(ShrinkPanelPlugin p) {
		shrinkers.add(p);
		List<ShrinkPanelPlugin> sList = new LinkedList<ShrinkPanelPlugin>(shrinkers);
		Collections.sort(sList, new PluginNameComparator());
		
		JFrame frame = getMainFrame();
		Container content = frame.getContentPane();
		content.setLayout(new GridBagLayout());
		content.removeAll();
		commandMap.clear();
		checkBoxes.clear();
		for (final ShrinkPanelPlugin sp : sList) {
			JCheckBoxMenuItem b = new JCheckBoxMenuItem(sp.toString());
			b.setBackground(new JPanel().getBackground());
			if (sp.getPluginInfo().icon != null) {
				Icon icon = ImageHook.scaleIcon(sp.getPluginInfo().icon, 16, 16);
				b.setIcon(icon);
			} else {
				b.setIcon(ShrinkPanelPlugin.getDefaultIcon());
			}
			String command = sp.getClass().getName();
			b.setActionCommand(command);
			commandMap.put(command, sp);
			checkBoxes.put(sp, b);
			b.addActionListener(this);
			content.add(b, masterConstraints);
		}
		frame.setSize(180, 300);
		if (showMasterPanelToggle.isSelected()) {
			frame.setVisible(true);
		}
	}
	
	
	public void removeShrinkerButton(ShrinkPanelPlugin p) {
		shrinkers.remove(p);
		JFrame pw = pluginWindowMap.get(p);
		if (pw != null && pw.isShowing()) {
			pw.setVisible(false);
			JPanel content = p.getShrinkPanel().getContentPanel();
			p.getShrinkPanel().setContentPanel(content);
			p.setShowPanel(true);
		}
	}
	
	
	private void updatePanels() {
		List<ShrinkPanelPlugin> plugins =  c.getPlugins(ShrinkPanelPlugin.class);
		Set<ShrinkPanelPlugin> viewShrinkers = new HashSet<ShrinkPanelPlugin>();
		for (ShrinkPanelPlugin spp : plugins) {
			if (spp.getPerspectivePluginClass().equals(View.class)) {
				viewShrinkers.add(spp);
			}
		}
		if (!shrinkers.equals(viewShrinkers)) {
			// do something
			Set<ShrinkPanelPlugin> addedShrinkers = new HashSet<ShrinkPanelPlugin>(viewShrinkers);
			addedShrinkers.removeAll(shrinkers);
			Set<ShrinkPanelPlugin> removedShrinkers = new HashSet<ShrinkPanelPlugin>(shrinkers);
			removedShrinkers.removeAll(viewShrinkers);
			
			if (addedShrinkers.size() != 0) {
				for (ShrinkPanelPlugin p : addedShrinkers) {
					addShrinkerButton(p);
				}
			}
			if (removedShrinkers.size() != 0) {
				for (ShrinkPanelPlugin p : removedShrinkers) {
					removeShrinkerButton(p);
				}
			}
		}
	}
	
	
	private class UpdateTimer extends AbstractTool {
		
		public UpdateTimer() {
			super();
			addCurrentSlot(InputSlot.SYSTEM_TIME);
		}
		
		long lastAction=0;
		
		@Override
		public void perform(ToolContext tc) {
			long ct = tc.getTime();
			if ((ct-lastAction) < 1000) return;
			lastAction = ct;
			updatePanels();
		}
		
	}
	
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}
	
	@Override
	public double getToolBarPriority() {
		return 3.0;
	}
	
}
