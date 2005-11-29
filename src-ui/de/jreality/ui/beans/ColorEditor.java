package de.jreality.ui.beans;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
public class ColorEditor extends PropertyEditorSupport {

    /** The Component returned by getCustomEditor(). */
    JButton button;

    public final static String title = "Select color...";

    /** Create FilePropertyEditor. */
    public ColorEditor() {
        button = new JButton(title);
        button.setIcon(colorIcon);
    }

    /**
     * PropertyEditor interface.
     * 
     * @return true
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Returns a JButton that will bring up a JFileChooser dialog.
     * 
     * @return JButton button
     */
    public Component getCustomEditor() {
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color col = JColorChooser.showDialog(button, title, (Color) getValue());
                ColorEditor.this.setValue(col);
            }
        });

        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        panel.setLayout(gridbag);
        gridbag.setConstraints(button, c);
        panel.add(button);
        
        return panel;
    }
    
    private Icon colorIcon = new Icon() {

      public void paintIcon(Component cmp, Graphics g, int x, int y) {
        if (getValue() != null) {
          g.setColor((Color) getValue());
          g.fillRect(x, y, getIconWidth(), getIconHeight());
          g.setColor(Color.black);
          g.drawRect(x, y, getIconWidth(), getIconHeight());
        }
      }

      public int getIconWidth() {
        return 16;
      }

      public int getIconHeight() {
        return 8;
      }
      
    };

}