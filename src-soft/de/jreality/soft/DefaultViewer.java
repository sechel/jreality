/*
 * Created on Dec 6, 2003
 *
 * This file is part of the jReality package.
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
package de.jreality.soft;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;

import de.jreality.scene.*;
import de.jreality.scene.Viewer;
import de.jreality.util.LoggingSystem;

/**
 * The default software renderer component.
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 * @author Holger Pietsch
 */
public class DefaultViewer extends Component implements Runnable, Viewer {
  private static final boolean ENFORCE_PAINT_ON_MOUSEEVENTS= false;
  private final Object renderLock= new Object();
  //private Camera camera;
  private SceneGraphPath cameraPath;
  private SceneGraphComponent root;

  private transient BufferedImage offscreen;

  private Renderer renderer;

  private boolean upToDate= false;
  private boolean backgroundExplicitlySet;
  private boolean imageValid;
  private boolean useDouble = false;
  
  public DefaultViewer() {
      this(false);
  }
  public DefaultViewer(boolean useDouble) {
    super();
    this.useDouble = useDouble;
    //backgroundExplicitlySet=getBackground()!=null;
    setBackground(Color.white);
    if(ENFORCE_PAINT_ON_MOUSEEVENTS)
      enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
    new Thread(this, "jReality render thread").start();

  }

  public boolean isFocusable() {
    return true;
  }

  /* (non-Javadoc)
   * @see de.jreality.soft.Viewer#getViewingComponent()
   */
  public Component getViewingComponent() {
    return this;
  }

  /* (non-Javadoc)
   * @see de.jreality.soft.Viewer#setSceneRoot(de.jreality.scene.SceneGraphComponent)
   */
  public void setSceneRoot(SceneGraphComponent c) {
    root=c;
    if(renderer!=null) renderer.setSceneRoot(c);
  }

  /* (non-Javadoc)
   * @see de.jreality.soft.Viewer#getSceneRoot()
   */
  public SceneGraphComponent getSceneRoot() {
    return root;
  }


//  public void setCamera(Camera c) {
//    camera=c;
//    if(renderer!=null) renderer.setCamera(c);
//  }
//
//
//  public Camera getCamera() {
//    return camera;
//  }

  /* (non-Javadoc)
   * @see de.jreality.soft.Viewer#render()
   */
  public void render() {
    if(upToDate) {
    upToDate= false;
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        synchronized(renderLock) {
          renderLock.notify();
        }
      }});
    }
  }

  public void invalidate() {
    super.invalidate();
    imageValid = false;
    upToDate = false;
  }

  public boolean isDoubleBuffered() {
    return true;
  }

  public void paint(Graphics g) {
  	if(!isShowing()) return;
  	Rectangle clip = g.getClipBounds();
    if(clip!=null && clip.isEmpty()) return;
    synchronized(this) {
      if(imageValid) {
        if(offscreen != null) {
          g.drawImage(offscreen, 0, 0, null);
          return;
        } else System.err.println("paint: no offscreen in paint");
      } else if(!upToDate) synchronized(renderLock) {
        renderLock.notify();
      }
    }
  }

  public void update(Graphics g) {
    paint(g);
  }

  public void run() {
    if(EventQueue.isDispatchThread()) {
      paintImmediately();
    }
    else while (true) try {
      if(upToDate) {
        try {
          synchronized(renderLock) {
            renderLock.wait();
          }
        } catch(InterruptedException e) {
          e.printStackTrace();
        }
      }

      renderImpl();
      //repaint();
      if(imageValid) EventQueue.invokeLater(this);
      else try {
        synchronized(renderLock) {
          renderLock.wait();
        }
      } catch(InterruptedException e) {
        e.printStackTrace();
      }
    } catch(Exception ex) {
      Thread t= Thread.currentThread();
      t.getThreadGroup().uncaughtException(t, ex);
    }
  }
  // TODO: add a structure lock to the scene graph
  public final synchronized void renderSync() {
    renderImpl();
  }

  private void renderImpl() {
      synchronized(this) {
        upToDate=true;
      }
    Dimension d= getSize();
    if (d.width > 0 && d.height > 0) {
      //System.out.println("render: off="+offscreen);
      if (offscreen == null
        || offscreen.getWidth() != d.width
        || offscreen.getHeight() != d.height) {
        imageValid = false;
        if(useDouble) {
//            offscreen=
//                    new BufferedImage(d.width, d.height, BufferedImage.TYPE_3BYTE_BGR);
//        renderer=new Renderer.ByteArray(offscreen);
//            renderer = new Renderer.ByteArrayDouble(offscreen);
            offscreen=
                new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
            renderer=new Renderer.IntArrayDouble(offscreen);
        
        } else {
            offscreen=
                new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
            renderer=new Renderer.IntArray(offscreen);
        }
        Color c = getBackground();
        renderer.setBackgroundColor(c !=null? c.getRGB(): 0);
        renderer.setCameraPath(cameraPath);
        renderer.setSceneRoot(root);
      } else if(!backgroundExplicitlySet) {
        Color c = getBackground();//inherited from parent
        renderer.setBackgroundColor(c !=null? c.getRGB(): 0);
      }
//      System.out.println("start rendering "+new java.util.Date());
      try {
        renderer.render();
      } catch (Exception e) {
        LoggingSystem.getLogger(this).log(Level.SEVERE, "renderer.render() failed! ", e);
        
      }
      synchronized(this) {
        //imageValid = false;
        renderer.update();
        imageValid=true;
      }
//      System.out.println("end rendering "+new java.util.Date());
    } else {
      System.out.println("no area to paint");
    }
  }

  protected void processMouseMotionEvent(MouseEvent e) {
    super.processMouseMotionEvent(e);
    if (ENFORCE_PAINT_ON_MOUSEEVENTS)
      render();
  }

  public void addNotify() {
    super.addNotify();
    requestFocus();
  }

  protected void processMouseEvent(MouseEvent e) {
    super.processMouseEvent(e);
    switch(e.getID()) {
      case MouseEvent.MOUSE_CLICKED: case MouseEvent.MOUSE_PRESSED:
      case MouseEvent.MOUSE_RELEASED:
        requestFocus();
    }
    if (ENFORCE_PAINT_ON_MOUSEEVENTS)
      render();
  }

  public void setBackground(Color c) {
    super.setBackground(c);
    backgroundExplicitlySet=c!=null;
    if(backgroundExplicitlySet&&renderer!=null)
      renderer.setBackgroundColor(c.getRGB());
  }

  public Renderer getRenderer() {
    return renderer;
  }
  private void paintImmediately() {
    if(!isShowing()) return;

    Component c=this;
    Component parent;
    Rectangle bounds=new Rectangle(0, 0, getWidth(), getHeight());

    for(parent=c.getParent();
        parent!=null && c.isLightweight();
        parent=c.getParent()) {
      bounds.x+=c.getX();
      bounds.y+=c.getY();
      c=parent;
    }
    Graphics gfx=c.getGraphics();
    gfx.setClip(bounds);
    c.paint(gfx);
  }
/* (non-Javadoc)
 * @see de.jreality.scene.Viewer#getCameraPath()
 */
public SceneGraphPath getCameraPath() {
    return cameraPath;
}
/* (non-Javadoc)
 * @see de.jreality.scene.Viewer#setCameraPath(de.jreality.util.SceneGraphPath)
 */
public void setCameraPath(SceneGraphPath p) {
    cameraPath = p;
    //camera = (Camera) p.getLastElement();
    
}
/* (non-Javadoc)
 * @see de.jreality.scene.Viewer#hasViewingComponent()
 */
public boolean hasViewingComponent() {
    return true;
}
/* (non-Javadoc)
 * @see de.jreality.scene.Viewer#initializeFrom(de.jreality.scene.Viewer)
 */
public void initializeFrom(Viewer v) {
    setSceneRoot(v.getSceneRoot());
    setCameraPath(v.getCameraPath());
}
/* (non-Javadoc)
 * @see de.jreality.scene.Viewer#getSignature()
 */
public int getSignature() {
    // TODO Auto-generated method stub
    return 0;
}
/* (non-Javadoc)
 * @see de.jreality.scene.Viewer#setSignature(int)
 */
public void setSignature(int sig) {
    // TODO Auto-generated method stub
    
}
/* (non-Javadoc)
 * @see de.jreality.scene.Viewer#setAuxiliaryRoot(de.jreality.scene.SceneGraphComponent)
 */
public void setAuxiliaryRoot(SceneGraphComponent ar) {
    throw new UnsupportedOperationException("not implemented");
}
/* (non-Javadoc)
 * @see de.jreality.scene.Viewer#getAuxiliaryRoot()
 */
public SceneGraphComponent getAuxiliaryRoot() {
    throw new UnsupportedOperationException("not implemented");
}

}
