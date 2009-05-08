package de.jreality.plugin.view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.swing.jrwindows.JRWindow;
import de.jreality.tools.ActionTool;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkSlotVertical;

public abstract class ContentAccessory extends ShrinkPanelPlugin implements ActionListener {

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
	
	public abstract SceneGraphComponent getTriggerComponent();

	public ContentAccessory() {
		panelConstraints.fill = GridBagConstraints.BOTH;
		panelConstraints.insets = new Insets(0,5,5,5);
		panelConstraints.weighty = 1.0;
		panelConstraints.weightx = 1.0;
		panelConstraints.gridwidth = GridBagConstraints.REMAINDER;
	}
	
	public void actionPerformed(ActionEvent e) {
		JPanel content = shrinkPanel.getContentPanel();
		JFrame frame = jrWindow.getFrame();
		Container frameContent = frame.getContentPane();
		shrinkPanel.setShrinked(false);
		if (inscene) {
			if (frame.isShowing()) {
				frameContent.remove(content);
				frame.setVisible(false);
				setShowPanel(true);
				shrinkPanel.setContentPanel(content);
			} else {
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
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		sceneWindowManager = c.getPlugin(SceneWindowManager.class);
		view = c.getPlugin(View.class);
		jrWindow = sceneWindowManager.getWindowManager().createFrame();
		JFrame frame = jrWindow.getFrame();
		frame.setLayout(new GridLayout());
		actionTool.addActionListener(this);
		getTriggerComponent().addTool(actionTool);
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		getTriggerComponent().removeTool(actionTool);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	public void setInscene(boolean inscene) {
		this.inscene = inscene;
	}

}
