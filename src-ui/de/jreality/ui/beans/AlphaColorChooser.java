//JTEM - Java Tools for Experimental Mathematics
//Copyright (C) 2002 JEM-Group
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package de.jreality.ui.beans;

import java.awt.*;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.font.TextLayout;

import javax.swing.*;
import javax.swing.JDialog;
import javax.swing.JSlider;
import javax.swing.JColorChooser;
import javax.swing.border.EmptyBorder;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.BorderFactory;

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

