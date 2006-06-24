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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.jreality.examples.CatenoidHelicoid;
import de.jreality.examples.PaintComponent;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.tool.DraggingTool;
import de.jreality.scene.tool.RotateTool;
import de.jreality.scene.tool.ToolSystem;
import de.jreality.scene.tool.config.ToolSystemConfiguration;
import de.jreality.swing.JRJComponent;
import de.jreality.util.RenderTrigger;
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
	

  Shell shell;
  GLCanvas canvas;
  private int signature;

	public SwtViewer(Shell myShell) {
		this(null, null, myShell);
	}
  
	public SwtViewer(SceneGraphPath camPath, SceneGraphComponent root, Shell myShell) {
		super();
    shell=myShell;
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

	public void render() {
//		if (isLinux)	{
//		    canvas.setIgnoreRepaint(false);
//			canvas.setNoAutoRedrawMode(false);			
//		}
	    synchronized (renderLock) {
				if (!pendingUpdate) {
          if (Thread.currentThread() == shell.getDisplay().getThread())
            run();
          else {
            shell.getDisplay().asyncExec(this);
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
    shell.setLayout(new FillLayout());
    Composite comp = new Composite(shell, SWT.NONE);
    comp.setLayout(new FillLayout());
    GLData data = new GLData ();
    data.doubleBuffer = true;
    System.out.println("data.depthSize="+data.depthSize);
    data.depthSize = 8;
    canvas = new GLCanvas(comp, SWT.NONE, data);
    canvas.setCurrent();
    shell.setText("jReality SWT/JOGL Test");
    shell.setSize(640, 480);
    shell.open();
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
    if (shell.isDisposed()) return;
		if (Thread.currentThread() != shell.getDisplay().getThread())
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

  public Shell getShell() {
    return shell;
  }
  
  public GLCanvas getGLCanvas() {
    return canvas;
  }
  
  public static void main(String[] args) throws Exception {
    
    SceneGraphComponent rootNode=new SceneGraphComponent();
    SceneGraphComponent geometryNode=new SceneGraphComponent();
    SceneGraphComponent cameraNode=new SceneGraphComponent();
    SceneGraphComponent lightNode=new SceneGraphComponent();
    
    rootNode.addChild(geometryNode);
    rootNode.addChild(cameraNode);
    cameraNode.addChild(lightNode);
    
    final CatenoidHelicoid geom=new CatenoidHelicoid(50);
    geom.setAlpha(Math.PI/2.-0.3);
    
    Camera camera=new Camera();
    Light light=new DirectionalLight();
    
    geometryNode.setGeometry(geom);
    cameraNode.setCamera(camera);
    lightNode.setLight(light);

    Appearance app=new Appearance();
    //app.setAttribute(CommonAttributes.FACE_DRAW, false);
    //app.setAttribute("diffuseColor", Color.red);
    //app.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
    //app.setAttribute(CommonAttributes.TRANSPARENCY, 0.4);
    //app.setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.blue);
    rootNode.setAppearance(app);
    
    MatrixBuilder.euclidean().rotateY(Math.PI/6).assignTo(geometryNode);
    MatrixBuilder.euclidean().translate(0, 0, 12).assignTo(cameraNode);
    MatrixBuilder.euclidean().rotate(-Math.PI/4, 1, 1, 0).assignTo(lightNode);

    //DefaultViewer viewer=new DefaultViewer();
    SwtFrame f = SwtFrame.getInstance();
    final SwtViewer viewer=new SwtViewer(f.createShell());
    viewer.setSceneRoot(rootNode);
    
    SceneGraphPath cameraPath=new SceneGraphPath();
    cameraPath.push(rootNode);
    cameraPath.push(cameraNode);
    cameraPath.push(camera);
    viewer.setCameraPath(cameraPath);

    geometryNode.addTool(new RotateTool());
    geometryNode.addTool(new DraggingTool());
    
    SwtFrame.getInstance().waitFor(new Runnable() {
      public void run() {viewer.init();};
    });
    
    ToolSystem ts = new ToolSystem(viewer, ToolSystemConfiguration.loadDefaultConfiguration());
    ts.setPickSystem(new AABBPickSystem());
    
    PaintComponent pc = new PaintComponent();
    JRJComponent jrj = new JRJComponent();
    jrj.add(pc);
    
    geometryNode.setAppearance(jrj.getAppearance());
    geometryNode.addTool(jrj.getTool());
    
    RenderTrigger rt = new RenderTrigger();
    rt.addViewer(viewer);
    rt.addSceneGraphComponent(rootNode);
    
    ts.initializeSceneTools();
    
//    while (!viewer.getShell().isDisposed()) {
//      geom.setAlpha(geom.getAlpha()+0.005);
////      viewer.render();
//      try {
//        Thread.sleep(20);
//      } catch (InterruptedException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//      }
//    }
//    
//    System.exit(0);
  }

  public double getAspectRatio() {
    return renderer.getAspectRatio(); 
  }

}
