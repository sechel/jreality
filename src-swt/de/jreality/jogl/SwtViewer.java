/*
 * Created on Jun 16, 2004
 *
 */
package de.jreality.jogl;

import java.awt.Component;
import java.util.logging.Level;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.SceneGraphUtility;
/**
 * @author Charles Gunn
 *
 */
public class SwtViewer implements de.jreality.scene.Viewer, Runnable {
	
  protected SceneGraphComponent sceneRoot;
	SceneGraphComponent auxiliaryRoot;
	SceneGraphPath cameraPath;

	JOGLRenderer renderer;
	
  public static final int 	CROSS_EYED_STEREO = 0;
	public static final int 	RED_BLUE_STEREO = 1;
	public static final int 	RED_GREEN_STEREO = 2;
	public static final int 	RED_CYAN_STEREO =  3;
	public static final int 	HARDWARE_BUFFER_STEREO = 4;
	public static final int 	STEREO_TYPES = 5;
	int stereoType = 		CROSS_EYED_STEREO;	
	

  GLCanvas canvas;
  private int signature;

	public SwtViewer(GLCanvas canvas) {
		this(null, null, canvas);
	}
  
	public SwtViewer(SceneGraphPath camPath, SceneGraphComponent root, GLCanvas canvas) {
		super();
    this.canvas=canvas;
    setAuxiliaryRoot(SceneGraphUtility.createFullSceneGraphComponent("AuxiliaryRoot"));
		initializeFrom(root, camPath);
    SwtQueue.getInstance().waitFor(new Runnable() {
      public void run() {init();};
    });
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

	public void render() {
//		if (isLinux)	{
//		    canvas.setIgnoreRepaint(false);
//			canvas.setNoAutoRedrawMode(false);			
//		}
	    synchronized (renderLock) {
				if (!pendingUpdate) {
          if (canvas.isDisposed()) return;
          if (Thread.currentThread() == canvas.getDisplay().getThread())
            run();
          else {
            canvas.getDisplay().asyncExec(this);
	  				pendingUpdate = true;
          }
				}
			}
	}

	public boolean hasViewingComponent() {
		return false;
	}


	public Component getViewingComponent() {
		return null;
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
	
  public void setStereoType(int type) {
    renderer.setStereoType(type);
  }
  
  // used in JOGLRenderer
  public int getStereoType()  {
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
		
	  private void initializeFrom(SceneGraphComponent r, SceneGraphPath p)	{
		  setSceneRoot(r);
		  setCameraPath(p);
      renderer=new JOGLRenderer(this);
    }
    
	private boolean pendingUpdate;
	
	private final Object renderLock=new Object();
	boolean autoSwapBuffers=true;
	
	public boolean isRendering() {
		synchronized(renderLock) {
			return pendingUpdate;
		}
	}
	
  private GLContext context;
  
  boolean init = true;
  
  int rot = 0;

  public void init() {
    canvas.addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event event) {
        Rectangle bounds = canvas.getBounds();
        renderer.setSize(bounds.width, bounds.height);
      }
    });
    context = GLDrawableFactory.getFactory().createExternalGLContext();
    Rectangle bounds = canvas.getBounds();
    renderer.setSize(bounds.width, bounds.height);
    context.makeCurrent();
    GL gl = context.getGL();
    renderer.init(gl);
    context.release();
  }
  
	public void run() {
    if (canvas.isDisposed()) return;
		if (Thread.currentThread() != canvas.getDisplay().getThread())
			throw new IllegalStateException();
		synchronized (renderLock) {
			pendingUpdate = false;
      canvas.setCurrent();
      context.makeCurrent();
      GL gl = context.getGL();
      renderer.display(gl);
      canvas.swapBuffers();
      context.release();
			renderLock.notifyAll();
		}
	}

  public GLCanvas getGLCanvas() {
    return canvas;
  }
  
  public double getAspectRatio() {
    return renderer.getAspectRatio(); 
  }

}
