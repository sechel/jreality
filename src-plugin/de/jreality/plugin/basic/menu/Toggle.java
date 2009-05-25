package de.jreality.plugin.basic.menu;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.event.ChangeListener;

public class Toggle {

	private Icon icon;
	private String text;

	private ToggleButtonModel model=new ToggleButtonModel();
	
	public Toggle(String text, Icon icon) {
		this(text, icon, false);
	}
	
	public Toggle(String text, Icon icon, boolean selected) {
		this.text=text;
		this.icon=icon;
		model.setSelected(selected);
	}
	
	public JCheckBox createChecker() {
		JCheckBox box = new JCheckBox(text, icon);
		box.setModel(model);
		return box;
	}
	
	public JToggleButton createToggleButton() {
		JToggleButton button = new JToggleButton(icon);
		button.setToolTipText(text);
		button.setModel(model);
		return button;
	}
	
	public JCheckBoxMenuItem createMenuItem() {
		JCheckBoxMenuItem button = new JCheckBoxMenuItem(text, icon);
		button.setModel(model);
		return button;
	}

	public void setSelected(boolean b) {
		model.setSelected(b);
	}

	public boolean isSelected() {
		return model.isSelected();
	}

	public void addActionListener(ActionListener l) {
		model.addActionListener(l);
	}

	public void addChangeListener(ChangeListener l) {
		model.addChangeListener(l);
	}

	public boolean isEnabled() {
		return model.isEnabled();
	}

	public void removeActionListener(ActionListener l) {
		model.removeActionListener(l);
	}

	public void removeChangeListener(ChangeListener l) {
		model.removeChangeListener(l);
	}

	public void setEnabled(boolean b) {
		model.setEnabled(b);
	}

	
	
}
