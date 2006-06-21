/*
 * Created on 11.02.2006
 *
 * This file is part of the de.jreality.swing package.
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
package de.jreality.swing;

import java.awt.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.RepaintManager;

import sun.awt.NullComponentPeer;

import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;

public class JRJComponent extends Box {
Frame f;
MouseEventTool tool;
BufferedImage bufferedImage;
    public JRJComponent() {
        super(BoxLayout.Y_AXIS);
        setDoubleBuffered(false);
        f = new FakeFrame();
        tool = new MouseEventTool(f);
        f.setUndecorated(true);
        f.setFocusableWindowState(false);
        
        setPreferredSize(new Dimension(256,256) );
              oldRM = RepaintManager.currentManager(this);
              RepaintManager rm = new ProxyRepaintManager(
                RepaintManager.currentManager(this)
              ) {
                public void paintDirtyRegions() {
                  boolean dirty = false;
                  for (Iterator i = comps.iterator(); i.hasNext(); ) {
                    Object n = i.next();
                    if (n instanceof JComponent) {
                      if (!getDirtyRegion((JComponent)n).isEmpty()) {
                        dirty = true;
                        break;
                      }
                    }
                  }
                  if (dirty) {
                   upImpl();
                   fire();
                  } 
                  super.paintDirtyRegions();
                }
              };
              RepaintManager.setCurrentManager(rm);
              addContainerListener(cl);
              
             // tool.addMouseListener(this);
              //tool.addMouseMotionListener(this);
              
              appearance = new Appearance();
              appearance.setAttribute(CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
              appearance.setAttribute(CommonAttributes.EDGE_DRAW, false);
              appearance.setAttribute(CommonAttributes.TUBES_DRAW, false);
              
             f.add(this);        
              f.pack();
              f.validate();
              f.show();
              //f.getPeer().hide();
//              f.setEnabled(false);
//              f.remove(this);
              addNotify();
    }

    public Toolkit getToolkit(){
        return Toolkit.getDefaultToolkit();
    }

HashSet comps = new HashSet();

Component current = null;

RepaintManager oldRM;
ContainerListener cl = new ContainerListener() {
  public void componentAdded(ContainerEvent e) {
    addMe(e.getChild());
    upImpl();
    fire();
  }


public void componentRemoved(ContainerEvent e) {
    removeMe(e.getChild());
    upImpl();
    fire();
  }
  void addMe(Component c) {
    if (c instanceof Container) {
      Container ca = (Container) c;
      ca.addContainerListener(this);
      Component cs[] = ca.getComponents();
      for (int i = 0; i < cs.length; i++) {
        addMe(cs[i]);
      }
    }
    comps.add(c);
  }
  void removeMe(Component c) {
    if (c instanceof Container) {
      Container ca = (Container) c;
      ca.removeContainerListener(this);
      Component cs[] = ca.getComponents();
      for (int i = 0; i < cs.length; i++) {
        addMe(cs[i]);
      }
    }
    comps.remove(c);
  }
};
private Graphics2D graphics;
private Appearance appearance;
private Texture2D tex;
private String praefix = "polygonShader";

void upImpl() {
  this.revalidate();
  f.validate();
  //f.pack();
}

 Point origin(Component root, Component target) {
  Component comp = target;
  Point ret = new Point();
  for (; comp != null && comp != root; comp = comp.getParent()) {
    Point p = new Point(comp.getLocation());
    ret.x += p.x;
    ret.y += p.y;
  }
  if (comp != root) throw new Error();
  return ret;
}

static class ProxyRepaintManager extends RepaintManager {

  private RepaintManager delegated;
  
  public ProxyRepaintManager(RepaintManager delegated) {

    this.delegated = delegated;
  }
  public static RepaintManager currentManager(Component c) {
    return RepaintManager.currentManager(c);
  }
  public static RepaintManager currentManager(JComponent c) {
    return RepaintManager.currentManager(c);
  }
  public static void setCurrentManager(RepaintManager aRepaintManager) {
    RepaintManager.setCurrentManager(aRepaintManager);
  }
  public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
    delegated.addDirtyRegion(c, x, y, w, h);
  }
  public void addInvalidComponent(JComponent invalidComponent) {
    delegated.addInvalidComponent(invalidComponent);
  }
  public boolean equals(Object obj) {
    return delegated.equals(obj);
  }
  public Rectangle getDirtyRegion(JComponent aComponent) {
    return delegated.getDirtyRegion(aComponent);
  }
  public Dimension getDoubleBufferMaximumSize() {
    return delegated.getDoubleBufferMaximumSize();
  }
  public Image getOffscreenBuffer(Component c, int proposedWidth,
      int proposedHeight) {
    return delegated.getOffscreenBuffer(c, proposedWidth, proposedHeight);
  }
  public Image getVolatileOffscreenBuffer(Component c, int proposedWidth,
      int proposedHeight) {
    return delegated.getVolatileOffscreenBuffer(c, proposedWidth,
        proposedHeight);
  }
  public int hashCode() {
    return delegated.hashCode();
  }
  public boolean isCompletelyDirty(JComponent aComponent) {
    return delegated.isCompletelyDirty(aComponent);
  }
  public boolean isDoubleBufferingEnabled() {
    return delegated.isDoubleBufferingEnabled();
  }
  public void markCompletelyClean(JComponent aComponent) {
    delegated.markCompletelyClean(aComponent);
  }
  public void markCompletelyDirty(JComponent aComponent) {
    delegated.markCompletelyDirty(aComponent);
  }
  public void paintDirtyRegions() {
    delegated.paintDirtyRegions();
  }
  public void removeInvalidComponent(JComponent component) {
    delegated.removeInvalidComponent(component);
  }
  public void setDoubleBufferingEnabled(boolean aFlag) {
    delegated.setDoubleBufferingEnabled(aFlag);
  }
  public void setDoubleBufferMaximumSize(Dimension d) {
    delegated.setDoubleBufferMaximumSize(d);
  }
  public String toString() {
    return delegated.toString();
  }
  public void validateInvalidComponents() {
    delegated.validateInvalidComponents();
  }
}



public void setSize(Dimension d) {
    setSize(d.width,d.height);
    //tool.setSize(d.width,d.height);
}

public void setSize(int width, int height) {
        super.setSize(width, height); 
        tool.setSize(width,height);
        if(bufferedImage == null || width !=bufferedImage.getWidth() || height != bufferedImage.getHeight()) {
            bufferedImage = null;
            ensureImage();
        }
}

private void ensureImage() {
    if(bufferedImage == null) {
        bufferedImage = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
        graphics = bufferedImage.createGraphics();
        tool.setSize(getWidth(),getHeight());
    }
}
private void fire() {
    
    if(graphics == null) 
        ensureImage();
    
    graphics.setColor(getBackground());
    graphics.fillRect(0, 0, getWidth(),getHeight());
//    printAll(graphics);
    paint(graphics);
    ImageData img = new de.jreality.shader.ImageData(bufferedImage);
    if(appearance != null) {
        //System.err.print("set...");
        if(tex == null)
            tex = TextureUtility.createTexture(appearance, praefix ,img);
        else tex.setImage(img);
        //System.err.println(". texture");
    }
}

public MouseEventTool getTool() {
    return tool;
}

public Appearance getAppearance() {
    return appearance;
}

public void setAppearance(Appearance appearance) {
    this.appearance = appearance;
    tex = null;
}

public void setAppearance(Appearance appearance,String praefix) {
    setAppearance(appearance);
    this.praefix = praefix;
}
}

