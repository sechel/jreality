/*
 * Created on Jun 16, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code G       System.out.println("made "+current.getChildComponentCount()+" components");
        System.out.println("done.");
 eneration&gt;Code and Comments
 */
package de.jreality.jogl;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

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
import de.jreality.util.SceneGraphUtilities;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Viewer implements de.jreality.scene.Viewer, GLEventListener, Runnable {
	SceneGraphComponent sceneRoot;
	SceneGraphComponent auxiliaryRoot;
	SceneGraphPath cameraPath;
	SceneGraphComponent cameraNode;
	Camera camera;
	GLCanvas canvas;
	JOGLRenderer renderer;
	int signature;
	static String OSName = null;
	static boolean multiSample = true;
	boolean isFlipped = false;			// LH Coordinate system?
	static GLCanvas firstOne = null;		// for now, all display lists shared with this one
	
	static {
		String foo = System.getProperty("jreality.jogl.multisample");
		if (foo != null) 
			if (foo.indexOf("false") != -1) multiSample = false;
			//else multisample = false;
	}
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
		initializeFrom(r, p);		
//	    canvas.setIgnoreRepaint(true);
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
			System.err.println("Invalid scene root.");
			return;
		}
		sceneRoot = r;
	}

	public void addAuxiliaryComponent(SceneGraphComponent aux)	{
		if (auxiliaryRoot == null)	{
			auxiliaryRoot = SceneGraphUtilities.createFullSceneGraphComponent("AuxiliaryRoot");
			renderer.setAuxiliaryRoot(auxiliaryRoot);
		}
		auxiliaryRoot.addChild(aux);
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
		if (p == null || p.getElementAt(0) != sceneRoot || p.getLastComponent().getCamera() == null || !(p.getLastElement() instanceof Camera))	{
			System.err.println("Invalid camera path, not setting");
			return;
		} 
		cameraPath = p;
	}
	
	private boolean debug = false;

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#render()
	 */
	public void render() {
    synchronized(renderLock) {
  		if(!pendingUpdate) {
  			if (debug) System.out.println("Render: invoke later");
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
				return canvas.getWidth();
			}
			public int getHeight()	{
				return canvas.getHeight();
			}
		};
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.Viewer#initializeFrom(de.jreality.scene.Viewer)
	 */
	public void initializeFrom(de.jreality.scene.Viewer v) {
		initializeFrom(v.getSceneRoot(), v.getCameraPath());
		// TODO handle the drawable
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
	        System.err.println("WARNING: antialiasing will be disabled because none of the available pixel formats had it to offer");
	      } else {
	        if (!available[selection].getSampleBuffers()) {
	          System.err.println("WARNING: antialiasing will be disabled because the DefaultGLCapabilitiesChooser didn't supply it");
	        }
	      }
	      return selection;
	    }
	  }
	private void initializeFrom(SceneGraphComponent r, SceneGraphPath p)	{
		setSceneRoot(r);
		setCameraPath(p);
		GLCapabilities caps = new GLCapabilities();
		if (multiSample)	{
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
		renderer =  new JOGLRenderer(this); 
		firstOne = canvas;
	}

	/**
	 * @return
	 */
	public JOGLRenderer getRenderer() {
		return renderer;
	}
	
	boolean doSpeedTest = false;
	double frameRate = 0.0;
	private boolean pendingUpdate;
	public void speedTest()		{
		doSpeedTest = true;
		render();
	}
	/* (non-Javadoc)
	 * @see net.java.games.jogl.GLEventListener#display(net.java.games.jogl.GLDrawable)
	 */
	public void display(GLDrawable arg0) {
		if (doSpeedTest)	{
			boolean isnard = canvas.getNoAutoRedrawMode();
			boolean iir = canvas.getIgnoreRepaint();
			canvas.setNoAutoRedrawMode(true);
			canvas.setIgnoreRepaint(true);
			long begin = System.currentTimeMillis();
			for (int i = 0; i<50; ++i)	{
				renderer.display(arg0);
			}
			long end = System.currentTimeMillis();
			frameRate = 50000.0/(end-begin);
			System.err.println("Timed frame rate: "+frameRate);
			doSpeedTest = false;
			canvas.setIgnoreRepaint(iir);
			canvas.setNoAutoRedrawMode(isnard);
		}
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
			renderLock.notifyAll();
		}
		if (debug) System.out.println("Render: calling display");
		canvas.display();
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
		else	try {
				EventQueue.invokeAndWait(bufferSwapper);
			}
		  catch (InterruptedException e) {}
			catch (InvocationTargetException e) {}
	}
	public boolean isFlipped() {
		return isFlipped;
	}
	public void setFlipped(boolean isFlipped) {
		this.isFlipped = isFlipped;
	}
}
