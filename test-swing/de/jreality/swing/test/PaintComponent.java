/*
 * Created on 11.02.2006
 *
 * This file is part of the de.jreality.swing.test package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.swing.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JPanel;

public class PaintComponent extends JPanel implements MouseListener, MouseMotionListener,ActionListener {
    BufferedImage myoff;
    int xold, yold;
    private Graphics2D graphics;
    private JButton button = new JButton("Clear");
    public PaintComponent() {
        super();
        add(button);
        button.addActionListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
       if(myoff!= null) g.drawImage(myoff,0,0,null);
    }

    public void mouseDragged(MouseEvent e) {
        //System.out.println("cp m dragged "+e);
        if(myoff == null) {
            clearMe();
        }
        int x = e.getX();
        int y = e.getY();
        graphics.drawLine(xold,yold,x,y);
        xold=x;
        yold = y;
        repaint();
        //invalidate();
    }
    
    private void clearMe() {
        if(myoff == null) {
            myoff = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
            graphics = myoff.createGraphics();
        }
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0,0,getWidth()-1,getHeight()-1);
        graphics.setColor(Color.RED);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setStroke(new BasicStroke(1.1f));
        for(int i = 0; i<225;i+=20) {
            graphics.drawLine(i,0,i,255);
            graphics.drawLine(0,i,255,i);
        }
        graphics.setColor(Color.BLACK);        
        graphics.setStroke(new BasicStroke(2.2f));
        repaint();
        //invalidate();
    }

    public void validate() {
        if(myoff == null || this.getWidth() != myoff.getWidth() || this.getHeight() != myoff.getHeight()){
            myoff = null;
            clearMe();
        }
        
        super.validate();
    }

    
    public void mouseMoved(MouseEvent e) {
        //System.out.println("cp m moved");
    }

    public void mouseClicked(MouseEvent e) {
        //System.out.println("cp m clicked");
    }

    public void mouseEntered(MouseEvent e) {
        //System.out.println("cp m entered");
    }

    public void mouseExited(MouseEvent e) {
        //System.out.println("cp m exited "+e);
        //System.out.println("  --> "+e.getSource());
    }

    public void mousePressed(MouseEvent e) {
        //System.out.println("cp m pressed");
        xold = e.getX();
        yold = e.getY();
        
    }

    public void mouseReleased(MouseEvent e) {
        //System.out.println("cp m released");
    }

    public void actionPerformed(ActionEvent e) {
        clearMe();
    }

    public Dimension getPreferredSize() {
        return new Dimension(256,256);
    }

    

}
