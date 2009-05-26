package de.jreality.ui.viewerapp.actions;

import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;

public abstract class AbstractJrToggleAction extends AbstractJrAction {

	public AbstractJrToggleAction(String name, Component parentComp) {
		super(name, parentComp);
		setSelected(false);
	}

	public AbstractJrToggleAction(String name) {
		this(name, null);
	}

	public void setSelected(boolean value) {
	  putValue(Action.SELECTED_KEY, value);
	}
	  
	public boolean isSelected() {
	  return Boolean.TRUE.equals(getValue(Action.SELECTED_KEY));
	}

	@Override
	public JMenuItem createMenuItem() {
		return new JCheckBoxMenuItem(this);
	}
	
	@Override
	public AbstractButton createToolboxItem() {
		JToggleButton ret = new JToggleButton(this);
		if (ret.getIcon() != null) {
			String text = ret.getText();
			ret.setToolTipText(text);
			ret.setText(null);
		}
		return ret;
	}
}
