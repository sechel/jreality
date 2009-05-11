package de.jreality.plugin.view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.swing.jrwindows.JRWindow;
import de.jreality.tools.ActionTool;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkSlotVertical;

public abstract class ContentAccessory extends ShrinkPanelPlugin {
	
	private SceneWindowManager
		sceneWindowManager = null;
	private ActionTool 
		actionTool=new ActionTool("PanelActivation");
	private JRWindow
		jrWindow = null;
	private boolean
		inscene = true;
	private GridBagConstraints
		panelConstraints = new GridBagConstraints();
	private View view;
	
	private boolean windowInScene=false;
	
	public abstract SceneGraphComponent getTriggerComponent();

	private class ListenerSupport extends ComponentAdapter implements ActionListener {

		@Override
		public void componentHidden(ComponentEvent e) {
			if (windowInScene) {
				actionPerformed(new ActionEvent(ContentAccessory.this, 0, "hidden"));
			}
		}

		public void actionPerformed(ActionEvent e) {
			shrinkPanel.setShrinked(false);
			if (inscene) {
				windowInScene = !windowInScene;
				if (!windowInScene) moveOutOfScene();
				else moveInScene();
			} else {
				if (shrinkPanel.isFloating()) {
					shrinkPanel.setFloating(false);
					shrinkPanel.getParentSlot().addShrinkPanel(shrinkPanel);
				} else {
					shrinkPanel.setFloating(true);
					shrinkPanel.getParentSlot().removeShrinkPanel(shrinkPanel);
				}
			}
		}

	};
	
	private ListenerSupport listeners = new ListenerSupport();
	
	public ContentAccessory() {
		panelConstraints.fill = GridBagConstraints.BOTH;
		panelConstraints.insets = new Insets(0,5,5,5);
		panelConstraints.weighty = 1.0;
		panelConstraints.weightx = 1.0;
		panelConstraints.gridwidth = GridBagConstraints.REMAINDER;
	}
	
	void moveInScene() {
		JPanel content = shrinkPanel.getContentPanel();
		JFrame frame = jrWindow.getFrame();
		Container frameContent = frame.getContentPane();
		setShowPanel(false);
		content.doLayout();
		frameContent.remove(content);
		frameContent.setLayout(new GridLayout());
		frameContent.add(content);
		
		Dimension size = content.getSize();
		if (size.height == 0 || size.width == 0) {
			LayoutManager layout = content.getLayout();
			size = layout.minimumLayoutSize(content);
			int width = ((ShrinkSlotVertical)view.getLeftSlot()).getPreferredWidth();
			Dimension pref = layout.preferredLayoutSize(content);
			if (pref.height > size.height) {
				size.height = pref.height;
			}
			size.width = width;
		}
		frame.setSize(size);
		frame.setVisible(true);
	}
	
	void moveOutOfScene() {
		JPanel content = shrinkPanel.getContentPanel();
		JFrame frame = jrWindow.getFrame();
		Container frameContent = frame.getContentPane();
		frameContent.remove(content);
		frame.setVisible(false);
		setShowPanel(true);
		shrinkPanel.setContentPanel(content);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		sceneWindowManager = c.getPlugin(SceneWindowManager.class);
		view = c.getPlugin(View.class);
		sceneWindowManager.getWindowManager().setWindowsInScene(true);
		jrWindow = sceneWindowManager.getWindowManager().createFrame();
		JFrame frame = jrWindow.getFrame();
		frame.addComponentListener(listeners);
		frame.setLayout(new GridLayout());
		actionTool.addActionListener(listeners);
		if (getTriggerComponent() != null) getTriggerComponent().addTool(actionTool);
	}
	
	
	@Override
	public void uninstall(Controller c) throws Exception {
		if (getTriggerComponent() != null) getTriggerComponent().removeTool(actionTool);
		jrWindow.getFrame().removeComponentListener(listeners);
		actionTool.removeActionListener(listeners);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	public void setInscene(boolean inscene) {
		this.inscene = inscene;
	}

}
