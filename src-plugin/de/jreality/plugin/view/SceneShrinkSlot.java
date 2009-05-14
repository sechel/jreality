package de.jreality.plugin.view;

import java.awt.Container;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jreality.swing.JFakeFrameWithGeometry;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkSlotVertical;

public class SceneShrinkSlot extends Plugin {

	Set<ShrinkPanel> panels = new HashSet<ShrinkPanel>();
	
	ShrinkSlotVertical slot = new ShrinkSlotVertical(250) {
		@Override
		protected void addShrinkPanelAt(ShrinkPanel p, int pos) {
			super.addShrinkPanelAt(p, pos);
			panels.add(p);
		}
		@Override
		public void removeShrinkPanel(ShrinkPanel panel) {
			// TODO Auto-generated method stub
			super.removeShrinkPanel(panel);
			panels.remove(panel);
		}
	};
	
	private JFakeFrameWithGeometry slotFrame;

	private Set<ContentAccessory> accessories = new HashSet<ContentAccessory>();
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("embedded ShrinkSlot");
	}
	
	@Override
	public void install(Controller c) throws Exception {
		WindowManager wm = c.getPlugin(WindowManager.class);
		slotFrame = wm.createFrame("Controls");
		slotFrame.setLayout(new GridLayout());
		slotFrame.add(slot);
		slotFrame.setBounds(slotFrame.getDesktopWidth()/2, slotFrame.getDesktopHeight()/2, 250, 350);
	}
	
	ShrinkSlotVertical getShrinkSlot() {
		return slot;
	}
	
	public void setVisible(boolean v) {
		slotFrame.setVisible(v);
	}

	public boolean isVisible() {
		return slotFrame.isVisible();
	}

	public void closeFrameIfEmpty() {
		if (!isVisible()) return;
		boolean vis = false;
		for (ShrinkPanel p : panels) {
			if (p.isVisible()) vis = true;
		}
		if (!vis) {
			setVisible(false);
			//for (ContentAccessory a : accessories) a.sceneFrameClosed();
		}
	}

	void registerAccessory(ContentAccessory contentAccessory) {
		accessories.add(contentAccessory);
	}

	void unregisterAccessory(ContentAccessory contentAccessory) {
		accessories.remove(contentAccessory);
	}

}
