package de.jreality.ui.beans;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;

import javax.swing.JComboBox;
import javax.swing.JPanel;

public class BooleanEditor extends PropertyEditorSupport {

  public boolean supportsCustomEditor() {
    return true;
  }
  
  public Component getCustomEditor() {
    JPanel comp = new JPanel(new BorderLayout());
    final JComboBox box = new JComboBox(new Object[]{Boolean.TRUE, Boolean.FALSE, "Inherit"});
    box.setSelectedItem(selected());
    box.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setValue(box.getSelectedItem());
      }
    });
    comp.add("Center", box);
    return comp;
  }
   
  public void setValue(Object arg0) {
    Object value = arg0 == "Inherit" ? (Boolean) null : arg0;
    super.setValue(value);
  }
  
  private Object selected() {
    final Object val = getValue();
    if (val == null) return "Inherit";
    return val;
  }
}
