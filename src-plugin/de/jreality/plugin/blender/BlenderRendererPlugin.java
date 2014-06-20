package de.jreality.plugin.blender;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import de.jreality.plugin.basic.ViewShrinkPanelPlugin;

public class BlenderRendererPlugin extends ViewShrinkPanelPlugin implements ActionListener {

	private JButton 
		renderButton = new JButton();
	
	public BlenderRendererPlugin() {
		shrinkPanel.add(renderButton);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
	
}
