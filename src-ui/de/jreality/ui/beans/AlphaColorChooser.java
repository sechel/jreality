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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AlphaColorChooser extends DefaultDialog implements ChangeListener
{
    private JSlider slider_;
    private JColorChooser color_chooser_;
    private ChangeListener change_listener_;
    public static AlphaColorChooser instance_ = null;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of AlphaColorChooser */
    private AlphaColorChooser(String title, Color color)
    {
        super();
        setTitle(title);
        JPanel content = new JPanel(new BorderLayout());
        change_listener_ = null;
        color_chooser_ = color != null ? new JColorChooser(color) : new JColorChooser();
        AlphaPreviewPanel previewPanel = new AlphaPreviewPanel(color);
        setChangeListener(previewPanel);
        color_chooser_.getSelectionModel().addChangeListener(this);
        JPanel frame = new JPanel();
        frame.add(previewPanel);
        frame.setBorder(new EmptyBorder(0,0,5,0));
        color_chooser_.setPreviewPanel(frame);
        content.add(color_chooser_, BorderLayout.CENTER);
        slider_ = new JSlider(JSlider.HORIZONTAL, 0, 255, color != null ? color.getAlpha() : 255);
        slider_.setMajorTickSpacing(85);
        slider_.setMinorTickSpacing(17);
        slider_.setPaintTicks(true);
        slider_.setPaintLabels(true);
        slider_.setBorder(BorderFactory.createTitledBorder("Alpha"));
        slider_.addChangeListener(this);
        content.add(slider_, BorderLayout.SOUTH);
        setMainComponent(content);
        pack();
    }
    
    static class AlphaPreviewPanel extends JButton implements ChangeListener {

        private static final String text="Black";
        private static final Font font=new Font("Sans Serif", Font.PLAIN, 30);
        
        private Color color;
        
        /**
         * @param color
         */
        AlphaPreviewPanel(Color color) {
            //setBorder(new EmptyBorder(5,0,5,5));
            setEnabled(false);
            //setMinimumSize(new Dimension(150,80));
            setPreferredSize(new Dimension(350,50));
            //setMaximumSize(new Dimension(100,50));
            setIcon(colorIcon);
            this.color=color;
        }

        public void stateChanged(ChangeEvent e) {
            AlphaColorChooser csm = (AlphaColorChooser) e.getSource();
            this.color=csm.getColor();
            repaint();
        }
        private Icon colorIcon = new Icon() {

            public void paintIcon(Component cmp, Graphics g, int x, int y) {
              Graphics2D g2d = (Graphics2D) g;
              g.setColor(Color.white);
              g.fillRect(x, y, AlphaPreviewPanel.this.getWidth(), AlphaPreviewPanel.this.getHeight());

              
        	  if (color != null) {
        	      g.setColor(Color.black);
	              
	          	  TextLayout tl = new TextLayout(text,font,g2d.getFontRenderContext());
	        	  Rectangle r = tl.getBounds().getBounds();
	        	  
	        	  // HACK: the previous implementation failed for strings without descent...
	        	  // I got cut-off in the vertical dir, so i added a border of width 2
	        	  int height = r.height; //(int) font.getLineMetrics(text,g2d.getFontRenderContext()).getHeight();//new TextLayout("fg", f, frc).getBounds().getBounds().height;
	              int width = r.width;
	
	        	  final float border = height - tl.getDescent();
	        	  g2d.setFont(font);
	        	  
	        	  int w=(getIconWidth()-width)/2;
	        	  int h=(getIconHeight()-height)/2;
	        	  g2d.drawString(text,x+w,y+height+h);
	
                g.setColor(color);
                g.fillRect(x, y, getIconWidth(), getIconHeight());
              }
              g.setColor(Color.black);
              g.drawRect(x, y, getIconWidth(), getIconHeight());
            }

            public int getIconWidth() {
              return AlphaPreviewPanel.this.getWidth();
            }

            public int getIconHeight() {
              return AlphaPreviewPanel.this.getHeight();
            }
            
          };

    }
    
    public static AlphaColorChooser getInstance()
    {
        if( instance_ == null )
        {
            instance_ = new AlphaColorChooser("Choose Color",Color.RED);
        }
        
        return instance_; 
    }
    
    //----------------------------------------------------------------------------
    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param e  a ChangeEvent object
     */
    public void stateChanged(ChangeEvent e)
    {
        if (change_listener_ != null)
        {
                        color_chooser_.setColor( getColor() );
            change_listener_.stateChanged( new ChangeEvent(this) );
        }
    }
    
    private void setChangeListener(ChangeListener change_listener)
    {
        change_listener_ = change_listener;
    }
    
    //----------------------------------------------------------------------------
    public void setColor(Color new_color)
    {
        color_chooser_.setColor(new_color);
        slider_.setValue( new_color.getAlpha() );
    }
    
    //----------------------------------------------------------------------------
    public Color getColor()
    {
        Color chooser_color = color_chooser_.getColor();
        return new Color(chooser_color.getRed(), chooser_color.getGreen(), chooser_color.getBlue(), slider_.getValue());
    }
    
    public static Color showDialog(Component c, String title, Color f)
    {
    AlphaColorChooser d = new AlphaColorChooser(title,f);
    d.setModal(true);
    d.setVisible(true);
    return d.getColor();
    }
}

