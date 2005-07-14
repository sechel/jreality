/*
 * Created on Jun 16, 2004
 *
 */
package de.jreality.jogl;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import net.java.games.jogl.DefaultGLCapabilitiesChooser;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLCapabilities;
import net.java.games.jogl.GLCapabilitiesChooser;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLDrawableFactory;
import net.java.games.jogl.GLEventListener;
import de.jreality.scene.Camera;
import de.jreality.scene.Drawable;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.util.CameraUtility;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author Charles Gunn
 *
 */
public class Viewer implements de.jreality.scene.Viewer, GLEventListener, Runnable {
	SceneGraphComponent sceneRoot;
	SceneGraphComponent auxiliaryRoot;
	SceneGraphPath cameraPath;
	SceneGraphComponent cameraNode;
	Camera camera;
	public GLCanvas canvas;
	JOGLRenderer renderer;
	int signature;
	boolean isFlipped = false;			// LH Coordinate system?
	static GLCanvas firstOne = null;		// for now, all display lists shared with this one
	public int getSignature() {
		return signature;
	}
	public void setSignature(int signature) {
		this.signature = signature;
		SceneGraphUtilities.setSignature(sceneRoot, signature);
		
	}
	public static final int 	CROSS_EYED_STEREO = 1;
	public static final int 	RED_BLUE_STEREO = 2;
	public static final int 	RED_GREEN_STEREO = 3;
	public static final int 	RED_CYAN_STEREO =  4;
	// this is not used: it's "quad buffer"
	public static final int 	HARDWARE_BUFFER_STEREO = 23;
	int stereoType = 		CROSS_EYED_STEREO;

	/**
	 * 
	 */
	public Viewer() {
		this(null, null);
	}

	/**
	 * @param p
	 * @param r
	 * @param fullscreen2
	 */
	public Viewer(SceneGraphPath p, SceneGraphComponent r) {
		super();
		auxiliaryRoot = SceneGraphUtilities.createFullSceneGraphComponent("AuxiliaryRoot");
		initializeFrom(r, p);	
		// for reasons unknown, have to be very careful on Linux not to try to draw too early.
		// to avoid this, set the following variables 
		if (JOGLConfiguration.isLinux)	{
		    canvas.setIgnoreRepaint(true);
			canvas.setNoAutoRedrawMode(true);			
		}
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#getSceneRoot()
	 */
	public SceneGraphComponent getSceneRoot() {
		return sceneRoot;
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#setSceneRoot(de.jreality.scene.SceneGraphComponent)
	 */
	public void setSceneRoot(SceneGraphComponent r) {
		if (r == null)	{
			JOGLConfiguration.getLogger().log(Level.WARNING,"Null scene root, ignoring.");
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
	public void addAuxiliaryComponent(SceneGraphComponent aux)	{
//		if (auxiliaryRoot == null)	{
//			auxiliaryRoot = SceneGraphUtilities.createFullSceneGraphComponent("AuxiliaryRoot");
//		}
		if (!auxiliaryRoot.isDirectAncestor(aux)) auxiliaryRoot.addChild(aux);
	}
	
	public void removeAuxiliaryComponent(SceneGraphComponent aux)	{
		if (auxiliaryRoot == null)	return;
		if (!auxiliaryRoot.isDirectAncestor(aux) ) return;
		auxiliaryRoot.removeChild(aux);
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
	    if (!CameraUtility.isCameraPathValid(p) ) {
				throw new IllegalArgumentException("Invalid camera path, not setting");
		}
		cameraPath = p;
	}
	
	private boolean debug = false;

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#render()
	 */
	public void render() {
//		if (isLinux)	{
//		    canvas.setIgnoreRepaint(false);
//			canvas.setNoAutoRedrawMode(false);			
//		}
    synchronized(renderLock) {
  		if(!pendingUpdate) {
  			if (debug) JOGLConfiguration.theLog.log(Level.INFO,"Render: invoke later");
 			EventQueue.invokeLater(this);
  			pendingUpdate=true;
      }
		}
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#hasViewingComponent()
	 */
	public boolean hasViewingComponent() {
		return true;
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#getViewingComponent()
	 */
	public Component getViewingComponent() {
		return canvas;
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#hasDrawable()
	 */
	public boolean hasDrawable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#getDrawable()
	 */
	public Drawable getDrawable() {
		return new Drawable()	{
			public double getAspectRatio()	{
				return canvas.getWidth()/((double) canvas.getHeight());
			}
			public int getWidth()	{
				return renderer.getWidth();
			}
			public int getHeight()	{
				return renderer.getHeight();
			}
			public int getXMin()	{
				return renderer.getXMin();
			}
			public int getYMin()	{
				return renderer.getYMin();
			}
		};
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#initializeFrom(de.jreality.scene.Viewer)
	 */
	public void initializeFrom(de.jreality.scene.Viewer v) {
		initializeFrom(v.getSceneRoot(), v.getCameraPath());
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
		if (JOGLConfiguration.multiSample)	{
			GLCapabilitiesChooser chooser = new MultisampleChooser();
			caps.setSampleBuffers(true);
			caps.setNumSamples(4);
			canvas = GLDrawableFactory.getFactory().createGLCanvas(caps, chooser, firstOne);
		} else {
			GLCapabilities capabilities = new GLCapabilities();
			canvas = GLDrawableFactory.getFactory().createGLCanvas(capabilities, null, firstOne);			
		}
		canvas.addGLEventListener(this);
		canvas.requestFocus();
		//renderer =  new JOGLRenderer(this); 
		if (JOGLConfiguration.sharedContexts && firstOne == null) firstOne = canvas;
	}

	/**
	 * @return
	 */
	public JOGLRenderer getRenderer() {
		return renderer;
	}
	
	private boolean pendingUpdate;
	/* (non-Javadoc)
	 * @see net.java.games.jogl.GLEventListener#display(net.java.games.jogl.GLDrawable)
	 */
	public void display(GLDrawable arg0) {
		renderer.display(arg0);
	}

	/* (non-Javadoc)
	 * @see net.java.games.jogl.GLEventListener#displayChanged(net.java.games.jogl.GLDrawable, boolean, boolean)
	 */
	public void displayChanged(GLDrawable arg0, boolean arg1, boolean arg2) {
		renderer.displayChanged(arg0, arg1, arg2);
	}

	/* (non-Javadoc)
	 * @see net.java.games.jogl.GLEventListener#init(net.java.games.jogl.GLDrawable)
	 */
	public void init(GLDrawable arg0) {
		JOGLConfiguration.theLog.log(Level.INFO,"JOGL Context initialization, creating new renderer");
		renderer =  new JOGLRenderer(this); 
	   renderer.init(arg0);  
	}

	/* (non-Javadoc)
	 * @see net.java.games.jogl.GLEventListener#reshape(net.java.games.jogl.GLDrawable, int, int, int, int)
	 */
	public void reshape(
		GLDrawable arg0,
		int arg1,
		int arg2,
		int arg3,
		int arg4) {
		renderer.reshape(arg0, arg1, arg2, arg3, arg4);
	}

	public void setStereoType(int type)	{
		stereoType = type;
	}
	
	// used in JOGLRenderer
	public int getStereoType()	{
		return stereoType;
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
			if (JOGLConfiguration.portalUsage) canvas.display();
			renderLock.notifyAll();
		}
		if (debug) JOGLConfiguration.theLog.log(Level.INFO,"Render: calling display");
		if (!JOGLConfiguration.portalUsage) canvas.display();
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
	
	public boolean isFlipped() {
		return isFlipped;
	}
	public void setFlipped(boolean isFlipped) {
		this.isFlipped = isFlipped;
	}
}
