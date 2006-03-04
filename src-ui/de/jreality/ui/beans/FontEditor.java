package de.jreality.ui.beans;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;

/**
 * A PropertyEditor that brings up a JFileChooser panel to select a
 * file.
 */
public class FontEditor extends PropertyEditorSupport {

    /** The Component returned by getCustomEditor(). */
    JButton button;
    Font defaultFont;
    
    public final static String title = "Select...";

    /** Create FilePropertyEditor. */
    public FontEditor() {
        button = new JButton("Select...");
        defaultFont = button.getFont();
        button.setMargin(new Insets(2,2,2,2));
    }

    /**
     * PropertyEditor interface.
     * 
     * @return true
     */
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public void setValue(Object value) {
      if (value == null) {
        button.setFont(defaultFont);
        button.setText("Select...");
      } else {
        button.setFont(((Font) value).deriveFont(defaultFont.getSize2D()));
        button.setText("["+((Font)value).getSize()+"] Change...");
      }
      super.setValue(value);
    }

    /**
     * Returns a JButton that will bring up a JFileChooser dialog.
     * 
     * @return JButton button
     */
    public Component getCustomEditor() {
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Font font = FontChooserDialog.showDialog(button, (Font) getValue());
                FontEditor.this.setValue(font);
            }
        });
        button.setMinimumSize(new Dimension(50, 12));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add("Center", button);
        
        return panel;
    }

}