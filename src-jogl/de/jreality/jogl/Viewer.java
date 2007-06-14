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


package de.jreality.jogl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;
import java.util.Vector;
import java.util.logging.Level;

import javax.media.opengl.DefaultGLCapabilitiesChooser;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.swing.JPanel;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.SceneGraphUtility;
/**
 * @author Charles Gunn
 *
 */
public class Viewer implements de.jreality.scene.Viewer, GLEventListener, Runnable {
	protected SceneGraphComponent sceneRoot;
	SceneGraphComponent auxiliaryRoot;
	SceneGraphPath cameraPath;
	SceneGraphComponent cameraNode;
	public GLCanvas canvas;
	protected JOGLRenderer renderer;
	int signature;
	boolean isFlipped = false;
	static GLContext firstOne = null;		// for now, all display lists shared with this one
	public static final int 	CROSS_EYED_STEREO = 0;
	public static final int 	RED_BLUE_STEREO = 1;
	public static final int 	RED_GREEN_STEREO = 2;
	public static final int 	RED_CYAN_STEREO =  3;
	public static final int 	HARDWARE_BUFFER_STEREO = 4;
	public static final int 	STEREO_TYPES = 5;
	int stereoType = 		CROSS_EYED_STEREO;	
	private boolean debug = false;

	public Viewer() {
		this(null, null);
	}

	public Viewer(SceneGraphPath camPath, SceneGraphComponent root) {
		renderer = new JOGLRenderer(this); 
		setAuxiliaryRoot(SceneGraphUtility.createFullSceneGraphComponent("AuxiliaryRoot"));
		initializeFrom(root, camPath);	
	}

	public SceneGraphComponent getSceneRoot() {
		return sceneRoot;
	}

	public void setSceneRoot(SceneGraphComponent r) {
		if (r == null)	{
			JOGLConfiguration.getLogger().log(Level.WARNING,"Null scene root, not setting.");
			return;
		}
		sceneRoot = r;
	}

	public SceneGraphComponent getAuxiliaryRoot() {
		return auxiliaryRoot;
	}
	public void setAuxiliaryRoot(SceneGraphComponent auxiliaryRoot) {
		this.auxiliaryRoot = auxiliaryRoot;
		if (renderer != null) renderer.setAuxiliaryRoot(auxiliaryRoot);
	}

	public SceneGraphPath getCameraPath() {
		return cameraPath;
	}

	public void setCameraPath(SceneGraphPath p) {
		cameraPath = p;
	}

	public void renderAsync() {
//		if (isLinux)	{
//		    canvas.setIgnoreRepaint(false);
//			canvas.setNoAutoRedrawMode(false);			
//		}
	    synchronized (renderLock) {
				if (!pendingUpdate) {
					if (debug)
						JOGLConfiguration.theLog.log(Level.INFO,
								"Render: invoke later");
					EventQueue.invokeLater(this);
					pendingUpdate = true;
				}
			}
	}

	public boolean hasViewingComponent() {
		return true;
	}

	private JPanel component;
  
  public Object getViewingComponent() {
    // this is to avoid layout problems when returning the plain glcanvas
    if (component == null) {
      component=new javax.swing.JPanel();
      component.setLayout(new java.awt.BorderLayout());
      component.setMaximumSize(new java.awt.Dimension(32768,32768));
      component.setMinimumSize(new java.awt.Dimension(10,10));
      component.add("Center", canvas);
      canvas.addKeyListener(new KeyListener() {
        public void keyPressed(KeyEvent e) {
          component.dispatchEvent(e);
        }
        public void keyReleased(KeyEvent e) {
          component.dispatchEvent(e);
        }
        public void keyTyped(KeyEvent e) {
          component.dispatchEvent(e);
        }
      });
      canvas.addMouseListener(new MouseListener() {
        public void mouseClicked(MouseEvent e) {
          component.dispatchEvent(e);
        }
        public void mouseEntered(MouseEvent e) {
          component.dispatchEvent(e);
        }
        public void mouseExited(MouseEvent e) {
          component.dispatchEvent(e);
        }
        public void mousePressed(MouseEvent e) {
          component.dispatchEvent(e);
        }
        public void mouseReleased(MouseEvent e) {
          component.dispatchEvent(e);
        }
      });
      canvas.addMouseMotionListener(new MouseMotionListener() {
        public void mouseDragged(MouseEvent e) {
          component.dispatchEvent(e);
        }
        public void mouseMoved(MouseEvent e) {
          component.dispatchEvent(e);
        }
      });
      canvas.addMouseWheelListener(new MouseWheelListener() {
        public void mouseWheelMoved(MouseWheelEvent e) {
          component.dispatchEvent(e);
        }
      });
    }
    return component;
  }
  
  /* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#initializeFrom(de.jreality.scene.Viewer)
	 */
	public void initializeFrom(de.jreality.scene.Viewer v) {
		initializeFrom(v.getSceneRoot(), v.getCameraPath());
	}
	
	public int getSignature() {
		return signature;
	}
	public void setSignature(int signature) {
		this.signature = signature;
		SceneGraphUtility.setSignature(sceneRoot, signature);
		
	}
	
/*********** Non-standard set/get ******************/
	
		public void setStereoType(int type)	{
			renderer.setStereoType(type);
		}
		
		// used in JOGLRenderer
		public int getStereoType()	{
			return renderer.getStereoType();
		}

		public boolean isFlipped() {
			return renderer.isFlipped();
		}
		public void setFlipped(boolean isFlipped) {
			renderer.setFlipped(isFlipped);
		}

		public JOGLRenderer getRenderer() {
			return renderer;
		}
		
/****** listeners!  ************/
		Vector<RenderListener> listeners;
		
		public interface RenderListener extends java.util.EventListener	{
			public void renderPerformed(EventObject e);
		}

		public void addRenderListener(Viewer.RenderListener l)	{
			if (listeners == null)	listeners = new Vector<RenderListener>();
			if (listeners.contains(l)) return;
			listeners.add(l);
			//JOGLConfiguration.theLog.log(Level.INFO,"Viewer: Adding geometry listener"+l+"to this:"+this);
		}
		
		public void removeRenderListener(Viewer.RenderListener l)	{
			if (listeners == null)	return;
			listeners.remove(l);
		}

		public void broadcastChange()	{
			if (listeners == null) return;
			//SyJOGLConfiguration.theLog.log(Level.INFO,"Viewer: broadcasting"+listeners.size()+" listeners");
			if (!listeners.isEmpty())	{
				EventObject e = new EventObject(this);
				//JOGLConfiguration.theLog.log(Level.INFO,"Viewer: broadcasting"+listeners.size()+" listeners");
				for (int i = 0; i<listeners.size(); ++i)	{
					Viewer.RenderListener l = (Viewer.RenderListener) listeners.get(i);
					l.renderPerformed(e);
				}
			}
		}

		/****** Convenience methods ************/
		public void addAuxiliaryComponent(SceneGraphComponent aux)	{
			if (auxiliaryRoot == null)	{
				setAuxiliaryRoot(SceneGraphUtility.createFullSceneGraphComponent("AuxiliaryRoot"));
			}
			if (!auxiliaryRoot.isDirectAncestor(aux)) auxiliaryRoot.addChild(aux);
		}
		
		public void removeAuxiliaryComponent(SceneGraphComponent aux)	{
			if (auxiliaryRoot == null)	return;
			if (!auxiliaryRoot.isDirectAncestor(aux) ) return;
			auxiliaryRoot.removeChild(aux);
		}
		
		 // Simple class to warn if results are not going to be as expected
		  static class MultisampleChooser extends DefaultGLCapabilitiesChooser {
		    public int chooseCapabilities(GLCapabilities desired,
		                                  GLCapabilities[] available,
		                                  int windowSystemRecommendedChoice) {
		      boolean anyHaveSampleBuffers = false;
		      for (int i = 0; i < available.length; i++) {
		        GLCapabilities caps = available[i];
		        if (caps != null && caps.getSampleBuffers()) {
		          anyHaveSampleBuffers = true;
		          break;
		        }
		      }
		      int selection = super.chooseCapabilities(desired, available, windowSystemRecommendedChoice);
		      if (!anyHaveSampleBuffers) {
		      	JOGLConfiguration.getLogger().log(Level.WARNING,"WARNING: antialiasing will be disabled because none of the available pixel formats had it to offer");
		      } else {
		        if (!available[selection].getSampleBuffers()) {
		        	JOGLConfiguration.getLogger().log(Level.WARNING,"WARNING: antialiasing will be disabled because the DefaultGLCapabilitiesChooser didn't supply it");
		        }
		      }
		      return selection;
		    }
		  }

	  private void initializeFrom(SceneGraphComponent r, SceneGraphPath p)	{
		setSceneRoot(r);
		setCameraPath(p);
		GLCapabilities caps = new GLCapabilities();
		caps.setAlphaBits(8);
		caps.setStereo(JOGLConfiguration.quadBufferedStereo);
		caps.setDoubleBuffered(true);
		if (JOGLConfiguration.multiSample)	{
			GLCapabilitiesChooser chooser = new MultisampleChooser();
			caps.setSampleBuffers(true);
			caps.setNumSamples(4);
			caps.setStereo(JOGLConfiguration.quadBufferedStereo);
			canvas = new GLCanvas(caps, chooser, firstOne,  GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
		} else {
			canvas = new GLCanvas(caps);
		}
        JOGLConfiguration.getLogger().log(Level.INFO, "Caps is "+caps.toString());
 		canvas.addGLEventListener(this);
 		if (JOGLConfiguration.quadBufferedStereo) setStereoType(HARDWARE_BUFFER_STEREO);
//		canvas.requestFocus();
		if (JOGLConfiguration.sharedContexts && firstOne == null) firstOne = canvas.getContext();
	}

	  public BufferedImage renderOffscreen(int w, int h) {
		  if (renderer != null) {
			  return renderer.renderOffscreen(w, h, canvas);
		  } else {
			  JOGLConfiguration.getLogger().log(Level.WARNING,"Renderer not initialized");
			  return null;
		  }
	  }
	  
	  public void renderOffscreen(int w, int h, File file)	{
		  if (renderer != null) renderer.renderOffscreen(w,h,file, canvas);
		  else JOGLConfiguration.getLogger().log(Level.WARNING,"Renderer not initialized");
	  }

	private boolean pendingUpdate;
	
	public void display(GLAutoDrawable arg0) {
		renderer.display(arg0);
	}

	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
		renderer.displayChanged(arg0, arg1, arg2);
	}

	public void init(GLAutoDrawable arg0) {
		JOGLConfiguration.theLog.log(Level.INFO,"JOGL Context initialization, creating new renderer");
		
		renderer = new JOGLRenderer(this);
		renderer.init(arg0);  
	}

	public void reshape(
		GLAutoDrawable arg0,int arg1,int arg2,int arg3,int arg4) {
		renderer.reshape(arg0, arg1, arg2, arg3, arg4);
  }
	private final Object renderLock=new Object();
	boolean autoSwapBuffers=true;
	
	public boolean isRendering() {
		synchronized(renderLock) {
			return pendingUpdate;
		}
	}
	  
	public void waitForRenderFinish() {
		synchronized(renderLock) {
			while(pendingUpdate) try {
				renderLock.wait();
			} catch(InterruptedException ex) {}
		}
	}
  
	public void run() {
		if (!EventQueue.isDispatchThread())
			throw new IllegalStateException();
		synchronized (renderLock) {
			pendingUpdate = false;
//			if (JOGLConfiguration.portalUsage) 
				canvas.display();
//			JOGLConfiguration.theLog.log(Level.INFO,"rendering "+renderer.frameCount);
				if (listeners!=null) broadcastChange();
			renderLock.notifyAll();
		}
		if (debug) JOGLConfiguration.theLog.log(Level.INFO,"Render: calling display");
//		if (!JOGLConfiguration.portalUsage) {
//			canvas.display();
//			if (listeners!=null) broadcastChange();
//		}
	}

	public void setAutoSwapMode(boolean autoSwap) {
		autoSwapBuffers=autoSwap;
		canvas.setAutoSwapBufferMode(autoSwap);
	}
	
    final Runnable bufferSwapper = new Runnable() {
        public void run() {
            canvas.swapBuffers();
        }
    };
                
	public void swapBuffers() {
		if(EventQueue.isDispatchThread()) canvas.swapBuffers();
		else
			try {
				EventQueue.invokeAndWait(bufferSwapper);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
	}

  public Dimension getViewingComponentSize() {
    return ((Component) getViewingComponent()).getSize();
  }

  public boolean canRenderAsync() {
    return true;
  }

  public void render() {
    if (EventQueue.isDispatchThread()) run();
    else
      try {
        EventQueue.invokeAndWait(this);
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
  }
	
}
