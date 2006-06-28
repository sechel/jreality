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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
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

package de.jreality.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.RepaintManager;

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

