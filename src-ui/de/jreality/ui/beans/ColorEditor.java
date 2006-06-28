/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.ui.beans;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * A PropertyEditor that brings up a JFileChooser panel to select a
 * file.
 */
public class ColorEditor extends PropertyEditorSupport {

    /** The Component returned by getCustomEditor(). */
    JButton button;

    public final static String title = "Select...";

    /** Create FilePropertyEditor. */
    public ColorEditor() {
        button = new JButton(title);
        button.setMargin(new Insets(2,2,2,2));
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
                Color col = AlphaColorChooser.showDialog(button, title, (Color) getValue());
                ColorEditor.this.setValue(col);
            }
        });
        button.setMinimumSize(new Dimension(50, 12));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add("Center", button);
        
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