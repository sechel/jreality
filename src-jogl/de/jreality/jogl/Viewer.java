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
	JOGLRendererInterface renderer;
	int signature;
	HelpOverlay helpOverlay;
	
	static String OSName = null;
	static boolean multiSample = true, newBackend = false;
	static {
		String foo = System.getProperty("jreality.jogl.newBackend");
		if (foo != null) 
			if (foo.indexOf("true") != -1) newBackend = true;
			else newBackend = false;
		if (newBackend) System.out.println("Using new backend");
		foo = System.getProperty("jreality.jogl.multisample");
		if (foo != null) 
			if (foo.indexOf("true") != -1) multiSample = true;
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
			System.err.println("Invalid scene root, creating new root.");
			sceneRoot = new SceneGraphComponent();
		}
		else sceneRoot = r;
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
		if (p == null || p.getElementAt(0) != sceneRoot || p.getLastComponent().getCamera() == null)	{
			System.err.println("Invalid camera path, adding new camera.");
			Camera c = new Camera();
			SceneGraphComponent sgc = new SceneGraphComponent();
			sgc.setTransformation(new Transformation());
			sgc.setCamera(c);
			sceneRoot.addChild(sgc);
			cameraPath = SceneGraphPath.getFirstPathBetween(sceneRoot, c);
		} else  cameraPath = p;
		cameraNode = cameraPath.getLastComponent();
		camera = ((Camera) cameraPath.getLastElement());
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
		//String[] = gluCheckExtension("GL_ARB_multisample"
		GLCapabilities caps = new GLCapabilities();
		if (multiSample)	{
			GLCapabilitiesChooser chooser = new MultisampleChooser();
			caps.setSampleBuffers(true);
			caps.setNumSamples(4);
			canvas = GLDrawableFactory.getFactory().createGLCanvas(caps, chooser);
		} else {
			GLCapabilities capabilities = new GLCapabilities();
			canvas = GLDrawableFactory.getFactory().createGLCanvas(capabilities);			
		}
		//canvas.setNoAutoRedrawMode(true);
		canvas.addGLEventListener(this);
		
		canvas.requestFocus();
		
		if (newBackend) renderer =  new JOGLRendererNew(this); 
		else renderer = new JOGLRenderer(this);
	}

	/**
	 * @return
	 */
	public JOGLRendererInterface getRenderer() {
		return renderer;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated  Use {@link de.jreality.util.CameraUtility.getCameraNode(Viewer v)}
	 */public SceneGraphComponent getCameraNode()	{
		return CameraUtility.getCameraNode(this);
	}

	/**
	 * 
	 * @return
	 * @deprecated  Use {@link de.jreality.util.CameraUtility.getCamera(Viewer v)}
	 */	
	 public Camera getCamera()	{
		return CameraUtility.getCamera(this);
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
			double rate;
			
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
			canvas.setIgnoreRepaint(false);
			canvas.setNoAutoRedrawMode(false);
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
		//canvas.update(canvas.getGraphics());
		if (debug) System.out.println("Render: calling display");
		canvas.display();
	}

	public void setAutoSwapMode(boolean autoSwap) {
		autoSwapBuffers=autoSwap;
		canvas.setAutoSwapBufferMode(autoSwap);
	}
	
	public void swapBuffers() {
		if(EventQueue.isDispatchThread()) canvas.swapBuffers();
		else	try {
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
						canvas.swapBuffers();
					}
				});
			}
		    catch (InterruptedException e) {}
			catch (InvocationTargetException e) {}
	}

	public HelpOverlay getHelpOverlay() {
		return helpOverlay;
	}
}
