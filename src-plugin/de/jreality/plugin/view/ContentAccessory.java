package de.jreality.plugin.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.ActionTool;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public abstract class ContentAccessory extends ShrinkPanelPlugin {
	
	private ActionTool 
		actionTool=new ActionTool("PanelActivation");
	
	private boolean windowInScene=false;

	private SceneShrinkSlot sceneSlot;
	
	private ShrinkPanel internalShrinkPanel;
	
	private SceneGraphComponent currentTrigger;
	
	public ContentAccessory() {
		actionTool.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(ContentAccessory.this.getClass().getName()+".actionPerformed()");
				toggle();
			}
		});
	}
	
	public void install(Controller c) throws Exception {
		super.install(c);
		sceneSlot = c.getPlugin(SceneShrinkSlot.class);
		
		sceneSlot.registerAccessory(this);
		
		internalShrinkPanel = new ShrinkPanel(shrinkPanel.getName());
		internalShrinkPanel.setFloatable(false);
		
		sceneSlot.getShrinkSlot().addShrinkPanel(internalShrinkPanel);
		internalShrinkPanel.setVisible(false);

	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		installTrigger(null);
		sceneSlot.unregisterAccessory(this);
	}

	private void installTrigger(SceneGraphComponent trigger) {
		if (trigger == currentTrigger) return;
		if (currentTrigger != null) currentTrigger.removeTool(actionTool);
		String n1 = trigger == null ? "null" : trigger.getName();
		String n2 = currentTrigger == null ? "null" : currentTrigger.getName();
		System.out.println(ContentAccessory.this.getClass().getName()+" newTrigger="+n1+" oldTrigger="+n2);
		currentTrigger = trigger;
		if (trigger != null) trigger.addTool(actionTool);
	}

	public void setTriggerComponent(SceneGraphComponent cmp) {
		installTrigger(cmp);
	}

	protected void toggle() {
		if (!windowInScene) {
			JPanel content = shrinkPanel.getContentPanel();
			shrinkPanel.setVisible(false);
			internalShrinkPanel.setContentPanel(content);
			internalShrinkPanel.setVisible(true);
			sceneSlot.setVisible(true);
		} else {
			JPanel content = internalShrinkPanel.getContentPanel();
			internalShrinkPanel.setVisible(false);
			shrinkPanel.setContentPanel(content);
			shrinkPanel.setVisible(true);
			sceneSlot.closeFrameIfEmpty();
 		}
		windowInScene = !windowInScene;
	}
	
	void sceneFrameClosed() {
		if (windowInScene) toggle();
	}
	
	
}
