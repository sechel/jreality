package de.jreality.ui.beans;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class NumberEditor extends PropertyEditorSupport {

  public boolean supportsCustomEditor() {
    return true;
  }
  
  final JTextField field = new JTextField();
  
  public Component getCustomEditor() {
    JPanel comp = new JPanel(new BorderLayout());
    field.setText(getAsText());
    field.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setAsText(field.getText());
      }
    });
    comp.add("Center", field);
    return comp;
  }
  
  public abstract String getAsText();
  public abstract void setAsText(String text);
  
  public void setValue(Object value) {    
    super.setValue(value);
    field.setText(getAsText());
  }
}
