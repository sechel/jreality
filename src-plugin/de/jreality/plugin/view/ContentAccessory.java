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
				toggle();
			}
		});
	}
	
	@Override
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
		if (windowInScene) moveOutOfScene();
		else moveIntoScene();
	}
	
	void moveIntoScene() {
		if (windowInScene) return;
		windowInScene = true;
		// move content to scene slot
		JPanel content = shrinkPanel.getContentPanel();
		internalShrinkPanel.setContentPanel(content);
		// hide outer panel
		shrinkPanel.setVisible(false);
		// show internal panel
		internalShrinkPanel.setVisible(true);
		// force display of scene slot
		sceneSlot.setVisible(true);
	}
	
	void moveOutOfScene() {
		if (!windowInScene) return;
		windowInScene = false;
		// move content to non-scene slot	
		JPanel content = internalShrinkPanel.getContentPanel();
		shrinkPanel.setContentPanel(content);
		// hide inner panel
		internalShrinkPanel.setVisible(false);
		// show external panel
		shrinkPanel.setVisible(true);
		// close internal frame if empty
		sceneSlot.closeFrameIfEmpty();
		
	}
	
	void sceneFrameClosed() {
		moveOutOfScene();
	}
	
	
}
