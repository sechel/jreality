package de.jreality.plugin.basic.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import de.jreality.plugin.PluginUtility;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.content.ContentTools;
import de.jreality.plugin.view.image.ImageHook;
import de.jreality.tools.EncompassTool;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.varylab.jrworkspace.plugin.flavor.PerspectiveFlavor;

public class ToolMenu extends ToolBarAggregator {

	EncompassTool encompassTool = new EncompassTool();
	private JCheckBoxMenuItem
		encompassItem = new JCheckBoxMenuItem("Encompass", ImageHook.getIcon("arrow_out.png"));
	
	private Content content;
	private ViewMenuBar viewMenuBar;
	
	@Override
	public void install(Controller c) throws Exception {
		encompassItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEncompassEnabled(isEncompassEnabled());
			}
		});
		super.install(c);
		viewMenuBar = c.getPlugin(ViewMenuBar.class);
		content = PluginUtility.getPlugin(c, Content.class);
		viewMenuBar.addMenuItem(getClass(), 1.0, encompassItem, "Tools");
		
		ContentTools ct = c.getPlugin(ContentTools.class);
		viewMenuBar.addMenuItem(getClass(), 1.0, ct.getDragToggle().createMenuItem(), "Tools", "Content");
		viewMenuBar.addMenuItem(getClass(), 1.0, ct.getRotateToggle().createMenuItem(), "Tools", "Content");
		viewMenuBar.addMenuItem(getClass(), 1.0, ct.getSnapToGridToggle().createMenuItem(), "Tools", "Content");
		viewMenuBar.addMenuSeparator(getClass(), 1.0, "Tools", "Content");
		viewMenuBar.addMenuItem(getClass(), 1.0, ct.getPickFacesToggle().createMenuItem(), "Tools", "Content");
		viewMenuBar.addMenuItem(getClass(), 1.0, ct.getPickEdgesToggle().createMenuItem(), "Tools", "Content");
		viewMenuBar.addMenuItem(getClass(), 1.0, ct.getPickVerticesToggle().createMenuItem(), "Tools", "Content");
	}

	public boolean isEncompassEnabled() {
		return encompassItem.isSelected();
	}

	public void setEncompassEnabled(boolean selected) {
		encompassItem.setSelected(selected);
		if (selected) content.addContentTool(encompassTool);
		else content.removeContentTool(encompassTool);
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("tool menu");
	}

	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

	
}
