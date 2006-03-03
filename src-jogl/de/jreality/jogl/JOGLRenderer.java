/*
 * Created on Nov 25, 2004
 *
  */
package de.jreality.jogl;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.nio.IntBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.java.games.jogl.*;
import net.java.games.jogl.util.BufferUtils;
import de.jreality.jogl.pick.Graphics3D;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.jogl.shader.*;
import de.jreality.math.*;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.*;
import de.jreality.scene.pick.PickPoint;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.CameraUtility;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
/**
 * @author gunn
 *
 */
public class JOGLRenderer extends SceneGraphVisitor implements AppearanceListener {

	final static Logger theLog = JOGLConfiguration.theLog;
	final static boolean debugGL = JOGLConfiguration.debugGL;
	
	static boolean collectFrameRate = true;
	public final static int MAX_STACK_DEPTH = 28;
	protected int stackDepth;
	JOGLRenderer globalHandle = null;
	SceneGraphPath currentPath = new SceneGraphPath();
	
	public Viewer theViewer;
	SceneGraphComponent theRoot, auxiliaryRoot;
	JOGLPeerComponent thePeerRoot = null;
	JOGLPeerComponent thePeerAuxilliaryRoot = null;
	JOGLRendererHelper helper;

	GLDrawable theCanvas;
	int width, height;		// GLDrawable.getSize() isnt' implemented for GLPBuffer!
	int whichEye = CameraUtility.MIDDLE_EYE;
	int[] currentViewport = new int[4];
	Graphics3D context;
	public GL globalGL;
	GLU globalGLU;
	int[] sphereDisplayLists = null, cylinderDisplayLists = null;
	public OpenGLState openGLState = new OpenGLState();
	public  boolean texResident = true;
	int numberTries = 0;		// how many times we have tried to make textures resident
	boolean  //useDisplayLists = true, 
		manyDisplayLists = false, forceResidentTextures = true;
	private boolean globalIsReflection = false;
	int currentSignature = Pn.EUCLIDEAN;

	// pick-related stuff
	boolean pickMode = false;
	private final double pickScale = 10000.0;
	Transformation pickT = new Transformation();
	PickPoint[] hits;
	// another eccentric mode: render in order to capture a screenshot
	boolean screenShot = false;
	boolean backSphere = false;
	double framerate;
	int lightCount = 0;
	int nodeCount = 0;
	WeakHashMap geometries = new WeakHashMap();
	boolean geometryRemoved = false, lightListDirty = true;
	static double[] p3involution = P3.makeStretchMatrix(null, new double[]{-1d,-1d,-1d,1d});
	DefaultVertexShader dvs = new DefaultVertexShader();
	/**
	 * @param viewer
	 */
	public JOGLRenderer(Viewer viewer) {
		this(viewer, ((GLDrawable) viewer.canvas));
		javax.swing.Timer followTimer = new javax.swing.Timer(1000, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {updateGeometryHashtable(); } } );
		followTimer.start();
	}
	public JOGLRenderer(Viewer viewer, GLDrawable d) {
		super();
		theViewer = viewer;
		theCanvas = d;
		//setSceneRoot(viewer.getSceneRoot());
		globalHandle = this;
		helper = new JOGLRendererHelper();
		//useDisplayLists = true;

		setAuxiliaryRoot(viewer.getAuxiliaryRoot());		
		
	}


	
	/**
	 * @param viewer
	 */
	private void setSceneRoot(SceneGraphComponent sgc) {
		if (theRoot != null) {
			Appearance ap = theRoot.getAppearance();
			if (ap != null) ap.removeAppearanceListener(this);
		}
		theRoot = sgc;
		thePeerRoot = constructPeerForSceneGraphComponent(theRoot, null);		
		// some top-level appearance attributes determine how we render; 
		// TODO set up a separate mechanism for controlling these top-level attributes
		theLog.info("setSceneRoot");
		Appearance ap = theRoot.getAppearance();
		if (ap == null) return;
		ap.addAppearanceListener(this);
		extractGlobalParameters();
	}
	
	public void appearanceChanged(AppearanceEvent ev) {
		theLog.fine("top appearance changed");
		extractGlobalParameters();
	}
	
	public void extractGlobalParameters()	{
		Appearance ap = theRoot.getAppearance();
		Object obj = ap.getAttribute(CommonAttributes.FORCE_RESIDENT_TEXTURES, Boolean.class);		// assume the best ...
		if (obj instanceof Boolean) forceResidentTextures = ((Boolean)obj).booleanValue();
		obj = ap.getAttribute(CommonAttributes.MANY_DISPLAY_LISTS, Boolean.class);		// assume the best ...
		if (obj instanceof Boolean) manyDisplayLists = ((Boolean)obj).booleanValue();
		obj = ap.getAttribute(CommonAttributes.ANY_DISPLAY_LISTS, Boolean.class);		// assume the best ...
		if (obj instanceof Boolean) thePeerRoot.useDisplayLists = ((Boolean)obj).booleanValue();
		theLog.fine("forceResTex = "+forceResidentTextures);
		theLog.fine("many display lists = "+manyDisplayLists);
		theLog.fine(" any display lists = "+thePeerRoot.useDisplayLists);
	}
	
	public SceneGraphComponent getAuxiliaryRoot() {
		return auxiliaryRoot;
	}
	public void setAuxiliaryRoot(SceneGraphComponent auxiliaryRoot) {
		this.auxiliaryRoot = auxiliaryRoot;
		if (auxiliaryRoot != null) {
			if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.dispose();
			thePeerAuxilliaryRoot = constructPeerForSceneGraphComponent(auxiliaryRoot, null);
		}

	}

	/* (non-Javadoc)
	 * @see de.jreality..SceneGraphVisitor#init()
	 */
	public Object visit() {
		//System.err.println("Initializing Visiting");
		// check to see that the root hasn't changed; all other changes handled by hierarchy events
		if (thePeerRoot == null || theViewer.getSceneRoot() != thePeerRoot.getOriginalComponent())	{
			if (thePeerRoot != null) thePeerRoot.dispose();
			setSceneRoot(theViewer.getSceneRoot());
		}
		context  = new Graphics3D(theViewer.getCameraPath(), null, CameraUtility.getAspectRatio(theViewer));
		//theLog.finer(" top level display lists = "+thePeerRoot.useDisplayLists);
		
		globalGL.glMatrixMode(GL.GL_PROJECTION);
		globalGL.glLoadIdentity();

		if (!pickMode)
			JOGLRendererHelper.handleBackground(theCanvas, width, height, theRoot.getAppearance());
		if (pickMode)
			globalGL.glMultTransposeMatrixd(pickT.getMatrix());

		theCanvas.setAutoSwapBufferMode(!pickMode);
		double aspectRatio = getAspectRatio();
		// for pick mode the aspect ratio has to be set to that of the viewer component
		if (pickMode) aspectRatio = CameraUtility.getAspectRatio(theViewer);
		// load the camera transformation
		globalGL.glMultTransposeMatrixd(CameraUtility.getCameraToNDC(CameraUtility.getCamera(theViewer), 
				aspectRatio,
				whichEye));

		
		// prepare for rendering the geometry
		globalGL.glMatrixMode(GL.GL_MODELVIEW);
		globalGL.glLoadIdentity();

		if (backSphere) {  globalGL.glLoadTransposeMatrixd(p3involution);	globalGL.glPushMatrix(); }
		double[] w2c = context.getWorldToCamera();
		globalGL.glLoadTransposeMatrixd(w2c);
		globalIsReflection = (theViewer.isFlipped() != (Rn.determinant(w2c) < 0.0));

		if (theRoot.getAppearance() != null) JOGLRendererHelper.handleSkyBox(theCanvas, theRoot.getAppearance(), globalHandle);
		
		if (!pickMode) processLights();
		
		processClippingPlanes();
		
		nodeCount = 0;			// for profiling info
		texResident=true;
		thePeerRoot.render();		
		if (!pickMode && thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.render();
		if (backSphere) globalGL.glPopMatrix();
		globalGL.glLoadIdentity();
		if (forceResidentTextures) forceResidentTextures();
		
		lightListDirty = false;
		return null;
	}
	
	/**
	 * @param theRoot2
	 * @return
	 */
	protected JOGLPeerComponent constructPeerForSceneGraphComponent(final SceneGraphComponent sgc, final JOGLPeerComponent p) {
		if (sgc == null) return null;
		final JOGLPeerComponent[] peer = new JOGLPeerComponent[1];
    ConstructPeerGraphVisitor constructPeer = new ConstructPeerGraphVisitor( sgc, p);
	  peer[0] = (JOGLPeerComponent) constructPeer.visit();
		return peer[0];
	}



	/**
	 * 
	 */
	private void processClippingPlanes() {
		List clipPlanes = null;
		if (clipPlanes == null)	{
			clipPlanes = SceneGraphUtility.collectClippingPlanes(theRoot);
		}
		helper.processClippingPlanes(globalGL, clipPlanes);
	}

	// TODO convert this to peer structure
	List lights = null;
	/**
	 * @param theRoot2
	 * @param globalGL2
	 * @param lightListDirty2
	 */
	private void processLights( ) {
		if (lights == null || lights.size() == 0 || lightListDirty) {
			lights = SceneGraphUtility.collectLights(theRoot);
			helper.resetLights(globalGL, lights);
			lightListDirty = false;
			openGLState.numLights = lights.size();
		}
		helper.processLights(globalGL, lights);
	}



	/**
	 * 
	 */
	private void forceResidentTextures() {
		// Try to force textures to be resident if they're not already
		if (!texResident && numberTries < 3)	{
			final Viewer theV = theViewer;
			TimerTask rerenderTask = new TimerTask()	{
				public void run()	{
					theV.render();
				}
			};
			Timer doIt = new Timer();
			forceNewDisplayLists();
			doIt.schedule(rerenderTask, 10);
			numberTries++;		// don't keep trying indefinitely
			JOGLConfiguration.theLog.log(Level.WARNING,"Textures not resident");
		} else numberTries = 0;
	}


	/**
	 * 
	 */
	private void forceNewDisplayLists() {
		if (thePeerRoot != null) thePeerRoot.setDisplayListDirty(true);
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.setDisplayListDirty(true);
	}


	int frameCount = 0;
	long[] history = new long[20];
	long[] clockTime = new long[20];
	
	public void setSize(int w, int h)	{
		width = w; height = h;
	}
	/* (non-Javadoc)
	 * @see net.java.games.jogl.GLEventListener#init(net.java.games.jogl.GLDrawable)
	 */
	public void init(GLDrawable drawable) {
		if (debugGL) {
			drawable.setGL(new DebugGL(drawable.getGL()));
		}
		if (sphereDisplayLists != null)	{
			theLog.log(Level.WARNING,"New context, same JOGLRenderer. ");
			//JOGLSphereHelper.disposeSphereDLists(globalGL);
		}
		theCanvas = drawable;
		if (!(theCanvas instanceof GLPbuffer))	{  // workaround in bug in implementation of GLPbuffer
			width = theCanvas.getSize().width;
			height = theCanvas.getSize().height;
		}
		// it should be possible to set these once and for all, since each context (a GL instance)
		// gets its own init() call.
		globalGL = theCanvas.getGL();
		globalGLU = theCanvas.getGLU();

		if (debugGL)	{
			String vv = globalGL.glGetString(GL.GL_VERSION);
			theLog.log(Level.INFO,"version: "+vv);			
			int[] tu = new int[1];
			globalGL.glGetIntegerv(GL.GL_MAX_TEXTURE_UNITS, tu);
			theLog.info("# of texture units: "+tu[0]);			
		}
		
//		otime = System.currentTimeMillis();
		// all display lists need to be set to dirty
		if (thePeerRoot != null) thePeerRoot.propagateGeometryChanged(ALL_CHANGED);
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.propagateGeometryChanged(ALL_CHANGED);
		Texture2DLoaderJOGL.deleteAllTextures(globalGL);
//		sphereDisplayLists = JOGLSphereHelper.getSphereDLists(this);
//		cylinderDisplayLists = JOGLCylinderUtility.getCylinderDLists(this);
		if (debugGL)	theLog.log(Level.INFO,"Got new sphere display lists for context "+globalGL);
//		OpenGLState.initializeGLState(this);
		
		//if (CameraUtility.getCamera(theViewer) == null || theCanvas == null) return;
		//CameraUtility.getCamera(theViewer).setAspectRatio(((double) theCanvas.getWidth())/theCanvas.getHeight());
		//globalGL.glViewport(0,0, theCanvas.getWidth(), theCanvas.getHeight());

}

	private void myglViewport(int lx, int ly, int rx, int ry)	{
		globalGL.glViewport(lx, ly, rx, ry);
		currentViewport[0] = lx;
		currentViewport[1] = ly;
		currentViewport[2] = rx;
		currentViewport[3] = ry;
	}

	/* (non-Javadoc)
	 * @see net.java.games.jogl.GLEventListener#display(net.java.games.jogl.GLDrawable)
	 */
	public void display(GLDrawable drawable) {
  if (theViewer.getSceneRoot() == null || theViewer.getCameraPath() == null) {
     LoggingSystem.getLogger(this).info("display called w/o scene root or camera path");
     // TODO: clear background
     return;
    }
	  
    OpenGLState.initializeGLState(this);
      
    //if (pickMode) return;
		long beginTime = 0;
		if (collectFrameRate) beginTime = System.currentTimeMillis();
		// following two statements should be redundant: they occur in init() which
		// gets called exactly once during the life of a context
		//theCanvas = (GLCanvas) drawable;
		//globalGL = theCanvas.getGL();
		Camera theCamera = CameraUtility.getCamera(theViewer);

		//theCamera.update();
		// TODO for split screen stereo, may want to have a real rectangle here, not always at (0,0)
		// for now just do cross-eyed stereo
		if (theCamera.isStereo())		{
			int which = theViewer.getStereoType();
			if (which == Viewer.CROSS_EYED_STEREO)		{
				int w = width/2;
				int h = height;
				whichEye=CameraUtility.RIGHT_EYE;
				//theCamera.update();
				globalGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				myglViewport(0,0, w,h);
				visit();
				whichEye=CameraUtility.LEFT_EYE;
				myglViewport(w, 0, w,h);
				visit();
			} 
			else if (which >= Viewer.RED_BLUE_STEREO &&  which <= Viewer.RED_CYAN_STEREO) {
				myglViewport(0,0, width, height);
				globalGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				whichEye=CameraUtility.RIGHT_EYE;
		        if (which == Viewer.RED_GREEN_STEREO) globalGL.glColorMask(false, true, false, true);
		        else if (which == Viewer.RED_BLUE_STEREO) globalGL.glColorMask(false, false, true, true);
		        else if (which == Viewer.RED_CYAN_STEREO) globalGL.glColorMask(false, true, true, true);
				visit();
				whichEye=CameraUtility.LEFT_EYE;
		        globalGL.glColorMask(true, false, false, true);
				globalGL.glClear (GL.GL_DEPTH_BUFFER_BIT);
				visit();
		        globalGL.glColorMask(true, true, true, true);
			} 
			else	{
				myglViewport(0,0, width, height);
				whichEye=CameraUtility.RIGHT_EYE;
				globalGL.glDrawBuffer(GL.GL_BACK_RIGHT);
				globalGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				visit();
				whichEye=CameraUtility.LEFT_EYE;
				globalGL.glDrawBuffer(GL.GL_BACK_LEFT);
				globalGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				visit();
			}
		} 
		else {
			globalGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			myglViewport(0,0,width, height);
			whichEye=CameraUtility.MIDDLE_EYE;
			if (!pickMode)	{
				// Following code seems to have NO effect: An attempt to render the "back banana"
//				if (theViewer.getSignature() == Pn.ELLIPTIC )	{
//					if (useDisplayLists)	{		// debug purposes
//						backSphere = true;
//						visit();						
//					}
//					backSphere = false;
//					visit();
//				}
//				 else 
				 	visit();
			}
			else		{
				// set up the "pick transformation"
				myglViewport(0,0, 2,2);
				IntBuffer selectBuffer = BufferUtils.newIntBuffer(bufsize);
				//JOGLConfiguration.theLog.log(Level.INFO,"Picking "+frameCount);
				double[] pp3 = new double[3];
				pp3[0] = -pickScale * pickPoint[0]; pp3[1] = -pickScale * pickPoint[1]; pp3[2] = 0.0;
				MatrixBuilder.euclidian().translate(pp3).scale(pickScale, pickScale, 1.0).assignTo(pickT);
				thePeerRoot.propagateGeometryChanged(POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
				globalGL.glSelectBuffer(bufsize, selectBuffer);		
				globalGL.glRenderMode(GL.GL_SELECT);
				globalGL.glInitNames();
				globalGL.glPushName(0);
				visit();
				pickMode = false;
				thePeerRoot.propagateGeometryChanged(POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
				int numberHits = globalGL.glRenderMode(GL.GL_RENDER);
				hits = JOGLPickAction.processOpenGLSelectionBuffer(numberHits, selectBuffer, pickPoint,theViewer);
//				useDisplayLists = store;
				display(drawable);
			}
		}
		if (screenShot)	{
			if (theCanvas instanceof GLCanvas) JOGLRendererHelper.saveScreenShot((GLCanvas) theCanvas, screenShotFile);
			else JOGLConfiguration.theLog.log(Level.WARNING, "Can't find the size of class "+theCanvas.getClass());
		}
		
//		if (++frameCount % 100 == 0) {
//			long time = System.currentTimeMillis();
//			framerate = (100000.0/(time-otime));
//			otime = time;
//		} 
		if (collectFrameRate)	{
			++frameCount;
			int j = (frameCount % 20);
			clockTime[j] = beginTime;
			history[j]  =  System.currentTimeMillis() - beginTime;
		}
	}

	/* (non-Javadoc)
	 * @see net.java.games.jogl.GLEventListener#displayChanged(net.java.games.jogl.GLDrawable, boolean, boolean)
	 */
	public void displayChanged(GLDrawable arg0, boolean arg1, boolean arg2) {
	}

	/* (non-Javadoc)
	 * @see net.java.games.jogl.GLEventListener#reshape(net.java.games.jogl.GLDrawable, int, int, int, int)
	 */
	public void reshape(GLDrawable arg0,int arg1,int arg2,int arg3,int arg4) {
//		CameraUtility.getCamera(theViewer).setAspectRatio(((double) theCanvas.getWidth())/theCanvas.getHeight());
//		myglViewport(0,0, theCanvas.getWidth(), theCanvas.getHeight());
		//System.out.println("Reshape args are "+arg1+" "+arg2+" "+arg3+" "+arg4);
		width = arg3-arg1;
		height = arg4-arg2;
//		CameraUtility.getCamera(theViewer).setAspectRatio(((double) width)/height);
		myglViewport(0,0, width, height);
	}

//	/**
//	 * @return
//	 */
//	public boolean isUseDisplayLists() {
//		return useDisplayLists;
//	}
//
//	/**
//	 * @param b
//	 */
//	public void setUseDisplayLists(boolean b) {
//		useDisplayLists = b;
//		if (useDisplayLists)	forceNewDisplayLists();
//	}

	private final static int POINTDL = 0;
	private final static int PROXY_POINTDL = 1;
	private final static int LINEDL = 2;
	private final static int PROXY_LINEDL = 3;
	private final static int POLYGONDL = 4;
    private final static int NUM_DLISTS = 5;
    private final static int POINTS_CHANGED = 1;
    private final static int LINES_CHANGED = 2;
    private final static int FACES_CHANGED = 4;
    private final static int ALL_CHANGED = 7;
	static Color[] cdbg = {Color.BLUE, Color.GREEN, Color.YELLOW,  Color.RED,Color.GRAY, Color.WHITE};
   
	private class DisplayListInfo	{
		private boolean useDisplayList, 	// can decide based on dynamic evaluation whether it makes sense 
					insideDisplayList,
					realDLDirty[] = new boolean[NUM_DLISTS];
		private int dl[];
		private int changeCount;
		private long frameCountAtLastChange;
		DisplayListInfo()	{
			super();
			dl = new int[NUM_DLISTS];
			for (int i = 0; i<NUM_DLISTS; ++i) { realDLDirty[i] = true; dl[i] = -1;}
			insideDisplayList = false;
			frameCountAtLastChange = frameCount;
			changeCount = 0;
		}
		boolean useDisplayList()	{
			if (changeCount <= 3) return true;
			long df = frameCount - frameCountAtLastChange;
			if (useDisplayList && df <= 5) useDisplayList = false;
			else if (!useDisplayList && df >= 5) useDisplayList = true;
			return useDisplayList;
		}

		public void setDisplayListID(int type, int id)	{
			dl[type] = id;
		}
		
		public int getDisplayListID(int type) {
			return dl[type];
		}

		public boolean isDisplayListDirty(int type) {
			return realDLDirty[type];
		}
		
		public void setDisplayListDirty(int type, boolean b) {
			realDLDirty[type] = b;
		}
		
		// mark ALL display lists as dirty.
		public void setDisplayListsDirty() {
			for (int i = 0; i<NUM_DLISTS; ++i) realDLDirty[i] = true;
		}
		
		public boolean isInsideDisplayList() {
			return insideDisplayList;
		}
		
		public void setInsideDisplayList(boolean b) {
			insideDisplayList = b;
		}
		
		public boolean isValidDisplayList(int type) {
			return false;
		}
		/**
		 * 
		 */
		public void setChange() {
			frameCountAtLastChange = frameCount;
			changeCount++;
			setDisplayListsDirty();
		}
		/**
		 * 
		 */
		public void dispose() {
			for (int i = 0; i<NUM_DLISTS; ++i) {
				if (dl[i] != -1) {
					globalGL.glDeleteLists(dl[i], 1);
//					JOGLConfiguration.theLog.log(Level.FINER, "Deleting display list "+dl[i]);
					dl[i] = -1;
				}
			}	
		}
		public void geometryChanged(int type) {
			
			if ((type & POINTS_CHANGED) != 0)	{
				setDisplayListDirty(POINTDL, true);
				setDisplayListDirty(PROXY_POINTDL, true);
			}
			if ((type & LINES_CHANGED) != 0)	{
				setDisplayListDirty(LINEDL, true);
				setDisplayListDirty(PROXY_LINEDL, true);
			}
			if ((type & FACES_CHANGED) != 0)	{
				setDisplayListDirty(POLYGONDL, true);
			}
			theLog.log(Level.FINER,"Setting display lists dirty with flag: "+type);
		}
	}
	
	
	public double getFramerate()	{
		long totalTime = 0;
		for (int i = 0; i<20; ++i)	totalTime += history[i];
		framerate = 20*1000.0 / totalTime;
		return framerate;
	}
	
	public double getClockrate()	{
		int j = frameCount % 20;
		int k = (frameCount +1) % 20;
		long totalTime = clockTime[j] - clockTime[k];
		double clockrate = 20*1000.0 / totalTime;
		return clockrate;
		
	}

	private static  int bufsize =16384;
	double[] pickPoint = new double[2];
	public PickPoint[] performPick(double[] p)	{
		if (CameraUtility.getCamera(theViewer).isStereo())		{
			theLog.log(Level.WARNING,"Can't pick in stereo mode");
			return null;
		}
		pickPoint[0] = p[0];  pickPoint[1] = p[1];
		pickMode = true;
		theCanvas.display();	// this calls out display() method  directly
		return hits;
	}
	
	protected class JOGLPeerNode	{
		String name;
		
		public String getName()	{
			return name;
		}
		
		public void setName(String n)	{
			name = n;
		}
	}

	/**
	 * 
	 */
	int geomDiff = 0;
	protected void updateGeometryHashtable() {
		JOGLConfiguration.theLog.log(Level.FINEST, "Memory usage: "+getMemoryUsage());
		if (geometries == null) return;
		if (!geometryRemoved) return;
		final WeakHashMap newG = new WeakHashMap();
		SceneGraphVisitor cleanup = new SceneGraphVisitor()	{
			public void visit(SceneGraphComponent c) {
				if (c.getGeometry() != null) {
					Object wawa = c.getGeometry();
					Object peer = geometries.get(wawa);
					newG.put(wawa, peer);
				}
				c.childrenAccept(this);
			}
		};
		cleanup.visit(theRoot);
		geometryRemoved = false;
		//TODO dispose of the peer geomtry nodes which are no longer in the graph
		if (geometries.size() - newG.size() != geomDiff)	{
			JOGLConfiguration.theLog.log(Level.WARNING,"Old, new hash size: "+geometries.size()+" "+newG.size());
			geomDiff = geometries.size() - newG.size() ;
		}
		return;
	}

	public String getMemoryUsage() {
        Runtime r = Runtime.getRuntime();
        int block = 1024;
        return "Memory usage: " + ((r.totalMemory() / block) - (r.freeMemory() / block)) + " kB";
    }
	
	
	protected class JOGLPeerGeometry extends JOGLPeerNode implements GeometryListener	{
		Geometry originalGeometry;
		Geometry[] tubeGeometry, proxyPolygonGeometry;
		Vector proxyGeometry;
		DisplayListInfo dlInfo;
		IndexedFaceSet ifs;
		IndexedLineSet ils;
		PointSet ps;
		int refCount = 0;
		
		protected JOGLPeerGeometry(Geometry g)	{
			super();
			originalGeometry = g;
			name = "JOGLPeer:"+g.getName();
			dlInfo = new DisplayListInfo();
			ifs = null; ils = null; ps = null;
			if (g instanceof IndexedFaceSet) ifs = (IndexedFaceSet) g;
			if (g instanceof IndexedLineSet) ils = (IndexedLineSet) g;
			if (g instanceof PointSet) ps = (PointSet) g;
			originalGeometry.addGeometryListener(this);
		}
		
		public void dispose()		{
			refCount--;
			if (refCount < 0)	{
				JOGLConfiguration.theLog.log(Level.WARNING,"Negative reference count!");
			}
			if (refCount == 0)	{
				JOGLConfiguration.theLog.log(Level.FINER,"Geometry is no longer referenced");
				dlInfo.dispose();
				originalGeometry.removeGeometryListener(this);	
				geometries.remove(originalGeometry);
			}
		}
		
		public void geometryChanged(GeometryEvent ev) {
			//TODO make more differentiated response based on event ev
//			final SceneGraphNode sg = (SceneGraphNode) ev.getSource();
			dlInfo.setChange();
		}
		
		public void geometryChanged(int type)	{
			dlInfo.geometryChanged(type);
		}
		/**
		 * 
		 */
		public void render(JOGLPeerComponent jpc) {
			//theLog.log(Level.FINER,"In JOGLPeerGeometry render() for "+originalGeometry.getName());
			//originalGeometry.startReader();
			// test billboarding
			if (originalGeometry instanceof Billboard)	{
				Billboard bb = (Billboard) originalGeometry;
				double[] mat = P3.calculateBillboardMatrix(null,bb.getXscale(), bb.getYscale(), bb.getOffset(), context.getCameraToObject(), bb.getPosition(), Pn.EUCLIDEAN);
				//globalGL.glPushMatrix();
				globalGL.glMultTransposeMatrixd(mat);
				//globalGL.glPopMatrix();
			}
			RenderingHintsShader renderingHints = jpc.renderingHints;
			DefaultGeometryShader geometryShader = jpc.geometryShader;
			renderingHints.render(globalHandle);
			DisplayListInfo activeDL = manyDisplayLists ? jpc.dlInfo : dlInfo;
			if (geometryShader.isFaceDraw() && originalGeometry instanceof Sphere)	{
//				IndexedFaceSet foo = getProxyFor((Sphere) originalGeometry);
//				ifs = foo;
//				ils = foo;
//				ps = foo;
				
				if (sphereDisplayLists == null) sphereDisplayLists = JOGLSphereHelper.getSphereDLists(globalHandle);
				geometryShader.polygonShader.render(globalHandle);	
				int i = 3;
				if (debugGL)	{
					double lod = renderingHints.getLevelOfDetail();
					// TODO do this in a timer
					i = JOGLSphereHelper.getResolutionLevel(context.getObjectToNDC(), lod);
					//int i = 3;
				}
				int dlist = sphereDisplayLists[i];
				
				//globalGL.glDisable(GL.GL_SMOOTH);
				if (pickMode) globalGL.glPushName(JOGLPickAction.GEOMETRY_BASE);
				if (debugGL) 
					globalGL.glColor4fv(cdbg[i].getRGBComponents(null));
				globalGL.glCallList(dlist);
				if (pickMode) globalGL.glPopName();
				geometryShader.polygonShader.postRender(globalHandle);
				return;
			} else if ((geometryShader.isFaceDraw() && originalGeometry instanceof Cylinder))	{
				if (cylinderDisplayLists == null) cylinderDisplayLists = JOGLCylinderUtility.getCylinderDLists(globalHandle);
				geometryShader.polygonShader.render(globalHandle);	
				int i = 3;
				if (debugGL)	{
					double lod = renderingHints.getLevelOfDetail();
					// TODO do this in a timer
					i = JOGLCylinderUtility.getResolutionLevel(context.getObjectToNDC(), lod);
					//int i = 3;
				}
	
				int dlist = cylinderDisplayLists[i];
				
				//globalGL.glDisable(GL.GL_SMOOTH);
				if (pickMode) globalGL.glPushName(JOGLPickAction.GEOMETRY_BASE);
				if (debugGL) 
					globalGL.glColor4fv(cdbg[i].getRGBComponents(null));
				globalGL.glCallList(dlist);
				if (pickMode) globalGL.glPopName();
				geometryShader.polygonShader.postRender(globalHandle);
				return;
				
			}
//			else 	if (originalGeometry instanceof LabelSet)	{
//				// NOTE we don't use display lists here because the text rendering is done in screen
//				// coordinates and must be recalculated for each frame
//				TextShader textShader = new TextShader();
//				textShader.setFromEffectiveAppearance(jpc.eAp, "textShader");
//				textShader.render(globalHandle);
//				JOGLRendererHelper.drawLabels(((LabelSet) originalGeometry), globalHandle);
//			}

			boolean useDisplayLists = useDisplayLists(activeDL, jpc);
			if (geometryShader.isEdgeDraw() && ils != null)	{
				geometryShader.lineShader.render(globalHandle);
				boolean proxy = geometryShader.lineShader.providesProxyGeometry();
				double alpha = openGLState.diffuseColor[3];
				boolean smooth = false; 
				int type = proxy ? PROXY_LINEDL : LINEDL;
				if (proxy)	{
					if ((!useDisplayLists || activeDL.isDisplayListDirty(type))) {
						theLog.log(Level.FINER,"Recalculating tubes");
						int dl  = geometryShader.lineShader.proxyGeometryFor(ils, globalHandle, currentSignature, useDisplayLists);
						if (dl != -1) {
							activeDL.setDisplayListID(type, dl);
							activeDL.setDisplayListDirty(type, false);
						}
					}
					if (useDisplayLists) globalGL.glCallList(activeDL.getDisplayListID(type));
				}
				else 	{
					if (useDisplayLists)		setupDisplayLists(activeDL, type);
					if (!useDisplayLists || activeDL.isInsideDisplayList()) 
						JOGLRendererHelper.drawLines(ils, globalHandle, pickMode, smooth, alpha);
					if (useDisplayLists)		cleanupDisplayLists(activeDL, type);
				}
				geometryShader.lineShader.postRender(globalHandle);
			}
			if (geometryShader.isVertexDraw() && ps != null)	{
				geometryShader.pointShader.render(globalHandle);
				double alpha = geometryShader.pointShader.getDiffuseColor().getAlpha()/255.0;
				boolean proxy = geometryShader.pointShader.providesProxyGeometry();
				int type = proxy ? PROXY_POINTDL : POINTDL;
				if (proxy)	{
					if ((!useDisplayLists || activeDL.isDisplayListDirty(type))) {
						theLog.log(Level.FINER,"Recalculating spheres");
						int dl = geometryShader.pointShader.proxyGeometryFor(ps, globalHandle, currentSignature,useDisplayLists);
						if (dl != -1) {
							activeDL.setDisplayListID(type, dl);
							activeDL.setDisplayListDirty(type, false);
						}
					}
					if (useDisplayLists) globalGL.glCallList(activeDL.getDisplayListID(type));
				}
				else 	{
					if (useDisplayLists)		setupDisplayLists(activeDL, type);
					if (!useDisplayLists || activeDL.isInsideDisplayList()) 
						JOGLRendererHelper.drawVertices(ps, globalHandle, pickMode, alpha);
					if (useDisplayLists)		cleanupDisplayLists(activeDL, type);
				}
				if (ps.getVertexAttributes(Attribute.LABELS) != null)	{
					JOGLRendererHelper.drawLabels(ps, globalHandle);
				}
				geometryShader.pointShader.postRender(globalHandle);
			}
			// do I need this?Yes, the point and line shader can turn off lighting
			renderingHints.render(globalHandle);
			if (geometryShader.isFaceDraw() && ifs != null)	{
//				if (geometryShader.polygonShaderNew != null) DefaultPolygonShader.renderNew(geometryShader.polygonShaderNew, globalHandle);
//				else 
				geometryShader.polygonShader.render(globalHandle);
				double alpha = openGLState.diffuseColor[3];
				boolean ss = openGLState.smoothShading;
				int type = POLYGONDL;
				boolean proxy = geometryShader.polygonShader.providesProxyGeometry();
				theLog.log(Level.FINE,"Using display lists: "+useDisplayLists);
				if (proxy)	{
					if ((!useDisplayLists || activeDL.isDisplayListDirty(type))) {
						theLog.log(Level.FINER, "Asking "+ geometryShader.polygonShader+ " for proxy geometry ");
						int dl = geometryShader.polygonShader.proxyGeometryFor(ifs, globalHandle, currentSignature,useDisplayLists);
						if (dl != -1) {
							activeDL.setDisplayListID(type, dl);
							activeDL.setDisplayListDirty(type, false);
						}
					}
					if (useDisplayLists) globalGL.glCallList(activeDL.getDisplayListID(type));
				}
				else 	{
					if (useDisplayLists)		setupDisplayLists(activeDL, type);
					if (!useDisplayLists || activeDL.isInsideDisplayList()) 
						JOGLRendererHelper.drawFaces(ifs, globalHandle,ss, alpha, pickMode);
					if (useDisplayLists)		cleanupDisplayLists(activeDL, type);
				}
				geometryShader.polygonShader.postRender(globalHandle);
			}
			renderingHints.postRender(globalHandle);
			//originalGeometry.finishReader();
		}

		private boolean useDisplayLists(DisplayListInfo dl, JOGLPeerComponent jpc)	{
			return jpc.useDisplayLists && dl.useDisplayList();
		}
		
		private void setupDisplayLists(DisplayListInfo dl, int type)	{
			if (!dl.isDisplayListDirty(type))	{
				//JOGLConfiguration.theLog.log(Level.INFO,"Using display list");
				globalGL.glCallList(dl.getDisplayListID(type));
				dl.setInsideDisplayList(false);
				return;
			}
			
			if (dl.getDisplayListID(type) != -1) {
				globalGL.glDeleteLists(dl.getDisplayListID(type), 1);
				JOGLConfiguration.theLog.log(Level.FINE, "Deleting display list "+dl.getDisplayListID(type));
				dl.setDisplayListID(type, -1);
			}
			int nextDL = globalGL.glGenLists(1);
			JOGLConfiguration.theLog.log(Level.FINE, "Allocating new display list "+nextDL);
			dl.setDisplayListID(type, nextDL);
			globalGL.glNewList(dl.getDisplayListID(type), GL.GL_COMPILE); //_AND_EXECUTE);
			//JOGLConfiguration.theLog.log(Level.INFO,"Beginning display list for "+originalGeometry.getName());
			dl.setInsideDisplayList(true);
		}
		/**
		 * @param activeDL
		 * @param type
		 */
		private void cleanupDisplayLists(DisplayListInfo activeDL, int type) {
			if (!activeDL.isInsideDisplayList()) return;
			globalGL.glEndList();	
			globalGL.glCallList(activeDL.getDisplayListID(type));
			activeDL.setDisplayListDirty(type, false);
			activeDL.setInsideDisplayList(false);
		}

	}
	
	
	public  JOGLPeerGeometry getJOGLPeerGeometryFor(Geometry g)	{
		JOGLPeerGeometry pg;
		synchronized(geometries)	{
			pg = (JOGLPeerGeometry) geometries.get(g);
			if (pg != null) return pg;
			pg = new JOGLPeerGeometry(g);
			geometries.put(g, pg);			
		}
		return pg;
	}
	
	// register for geometry change events
	//static Hashtable goBetweenTable = new Hashtable();
	WeakHashMap goBetweenTable = new WeakHashMap();
	public  GoBetween goBetweenFor(SceneGraphComponent sgc)	{
		if (sgc == null) return null;
		GoBetween gb = null;
		Object foo = goBetweenTable.get(sgc);
		if (foo == null)	{
			gb = globalHandle.new GoBetween(sgc);
			goBetweenTable.put(sgc, gb);
			return gb;
		}
		return ((GoBetween) foo);
	}
	
	protected class GoBetween implements TransformationListener, AppearanceListener,SceneGraphComponentListener	{
		SceneGraphComponent originalComponent;
		ArrayList peers = new ArrayList();
		JOGLPeerGeometry peerGeometry;

		protected GoBetween(SceneGraphComponent sgc)	{
			super();
			originalComponent = sgc;
			if (originalComponent.getGeometry() != null)  {
				peerGeometry = getJOGLPeerGeometryFor(originalComponent.getGeometry());
				peerGeometry.refCount++;
			} else peerGeometry = null;
			originalComponent.addSceneGraphComponentListener(this);
			if (originalComponent.getAppearance() != null) 
				originalComponent.getAppearance().addAppearanceListener(this);				
		}
		
		public void dispose()	{
			originalComponent.removeSceneGraphComponentListener(this);
			if (originalComponent.getAppearance() != null) originalComponent.getAppearance().removeAppearanceListener(this);
			if (peerGeometry != null)		peerGeometry.dispose();
		}
		
		
		public void addJOGLPeer(JOGLPeerComponent jpc)	{
			if (peers.contains(jpc)) return;
			peers.add(jpc);
		}
		
		public void removeJOGLPeer(JOGLPeerComponent jpc)	{
			if (!peers.contains(jpc)) return;
			peers.remove(jpc);
			if (peers.size() == 0)	{
				theLog.log(Level.FINE,"GoBetween for "+originalComponent.getName()+" has no peers left");
				goBetweenTable.remove(originalComponent);
				dispose();
			}
		}
		
		public JOGLPeerGeometry getPeerGeometry() {
			return peerGeometry;
		}
		
		public void transformationMatrixChanged(TransformationEvent ev) {
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.transformationMatrixChanged(ev);
			}
		}
		
		public void appearanceChanged(AppearanceEvent ev) {
			String key = ev.getKey();
			int changed = 0;
			boolean propagates = true;
			// TODO shaders should register keywords somehow and which geometries might be changed
			if (key.indexOf("implodeFactor") != -1 ) changed |= (FACES_CHANGED);
			else if (key.indexOf("transparency") != -1) changed |= (FACES_CHANGED);
			else if (key.indexOf("tubeRadius") != -1) changed |= (
					LINES_CHANGED);
			else if (key.indexOf("pointRadius") != -1) changed |= (POINTS_CHANGED);
			else if (key.indexOf("anyDisplayLists") != -1) changed |= (POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
			// there are some appearances which we know aren't inherited, so don't propagate change event.
			if (key.indexOf(CommonAttributes.BACKGROUND_COLOR) != -1	||
				key.indexOf("fog") != -1) propagates = false;
				
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				if (propagates) peer.appearanceChanged(ev);
				if (changed != 0) peer.propagateGeometryChanged(changed);
			}
			//theLog.log(Level.FINER,"setting display list dirty flag: "+changed);
		}
		public void childAdded(SceneGraphComponentEvent ev) {
			if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY) {
					if (peerGeometry != null)	{
						peerGeometry.dispose();
						geometryRemoved = true;
						theLog.log(Level.WARNING, "Adding geometry while old one still valid");
						peerGeometry=null;
					}
					if (originalComponent.getGeometry() != null)  {
						peerGeometry = getJOGLPeerGeometryFor(originalComponent.getGeometry());
						peerGeometry.refCount++;
					} 
//					return;
			}
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childAdded(ev);
			}
		}
		public void childRemoved(SceneGraphComponentEvent ev) {
			if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY) {
				if (peerGeometry != null) {
					peerGeometry.dispose();		// really decreases reference count
					peerGeometry = null;
					geometryRemoved = true;
				}
//				return;
			}
			boolean apAdded = (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE);
			int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childRemoved(ev);
				// why isn't the following done in peer.childRemoved?
				if (apAdded) peer.propagateGeometryChanged(changed);
			}
		}
		
		public void childReplaced(SceneGraphComponentEvent ev) {
			if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY) {
					if (peerGeometry != null && peerGeometry.originalGeometry == originalComponent.getGeometry()) return;		// no change, really
					if (peerGeometry != null) {
						peerGeometry.dispose();
						geometryRemoved=true;
						peerGeometry = null;
					}
					if (originalComponent.getGeometry() != null)  {
						peerGeometry = getJOGLPeerGeometryFor(originalComponent.getGeometry());
						peerGeometry.refCount++;
					} 
//					return;
			}
			boolean apAdded = (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE);
			int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childReplaced(ev);
				if (apAdded) peer.propagateGeometryChanged(changed);
				}				
		}
		
		public SceneGraphComponent getOriginalComponent() {
			return originalComponent;
		}

    public void visibilityChanged(SceneGraphComponentEvent ev) {
      // TODO Auto-generated method stub
      
    }
	}
	private class ConstructPeerGraphVisitor extends SceneGraphVisitor	{
		SceneGraphComponent myRoot;
		JOGLPeerComponent thePeerRoot, myParent;
		SceneGraphPath sgp;
		boolean topLevel = true;
		public ConstructPeerGraphVisitor(SceneGraphComponent r, JOGLPeerComponent p)	{
			super();
			myRoot = r;
			sgp = new SceneGraphPath();
			myParent = p;
		}
		
		private ConstructPeerGraphVisitor(ConstructPeerGraphVisitor pv, JOGLPeerComponent p)	{
			super();
			sgp = (SceneGraphPath) pv.sgp.clone();
			myParent = p;
			topLevel = false;
		}
		
		public void visit(SceneGraphComponent c) {
			sgp.push(c);
			JOGLPeerComponent peer = new JOGLPeerComponent(sgp, myParent);
			// we don't add the top-level node here to its parent; 
			// that has to be done carefully using a childLock by the caller
			if (topLevel) thePeerRoot = peer;
			else if (myParent != null) {
				int n = myParent.children.size();
				myParent.children.add(peer);
				peer.childIndex = n;
			}
		  	c.childrenAccept(new ConstructPeerGraphVisitor(this, peer));
		  	sgp.pop();
		}

		public Object visit()	{
			visit(myRoot);
			return thePeerRoot;
		}
		
	}
	protected class JOGLPeerComponent extends JOGLPeerNode implements TransformationListener, AppearanceListener,SceneGraphComponentListener {
		
		public int[] bindings = new int[2];
		EffectiveAppearance eAp;
		Vector children;
		JOGLPeerComponent parent;
		int childIndex;
		GoBetween goBetween;
		DisplayListInfo dlInfo = new DisplayListInfo();
		
		Rectangle3D childrenBound;
		Rectangle2D ndcExtent;
		SceneGraphPath	pathToHere;
		boolean isReflection = false, cumulativeIsReflection = false;
		double determinant = 0.0;
		
		RenderingHintsShader renderingHints;
		DefaultGeometryShader geometryShader;
		
		Object childLock = new Object();		
		Runnable renderGeometry = null;
		final JOGLPeerComponent self = this;
		
		boolean appearanceChanged = false,
			appearanceIsDirty = true,
			geometryIsDirty = true,
			boundIsDirty = true,
			object2WorldDirty = true,
			useDisplayLists = true;

		double[] tform = new double[16];		// for optimized access to matrix
		public JOGLPeerComponent(SceneGraphPath sgp, JOGLPeerComponent p)		{
			super();
			if (sgp == null || !(sgp.getLastElement() instanceof SceneGraphComponent))  {
				throw new IllegalArgumentException("Not a valid SceneGraphComponenet");
			}
			pathToHere = sgp;
			goBetween = goBetweenFor(sgp.getLastComponent());
			goBetween.addJOGLPeer(this);
			name = "JOGLPeer:"+goBetween.getOriginalComponent().getName();
			children = new Vector();		// always have a child list, even if it's empty
			parent = p;
			updateTransformationInfo();
			updateRenderRunnable();
		}
		
		private void updateRenderRunnable() {
			if (goBetween.peerGeometry == null) renderGeometry = null;
			else	renderGeometry = new Runnable() {
				public void run() {
					goBetween.peerGeometry.render(self);
				}
			};
		}

		public void dispose()	{
			//synchronized(childLock)	{
				int n = children.size();
				for (int i = n-1; i>=0; --i)	{
					JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);
					child.dispose();
				}	
				goBetween.removeJOGLPeer(this);
				dlInfo.dispose();
			//}

		}
		public void render()		{
			if (!goBetween.getOriginalComponent().isVisible()) return;
						
			nodeCount++;
			currentPath.push(goBetween.getOriginalComponent());
			context.setCurrentPath(currentPath);
			Transformation thisT = goBetween.getOriginalComponent().getTransformation();
			
			theLog.log(Level.FINE,"In JOGLPeerComponent render() for "+goBetween.getOriginalComponent().getName());
			if (thisT != null)	{
				if (stackDepth <= MAX_STACK_DEPTH) {
					globalGL.glPushMatrix();
					globalGL.glMultTransposeMatrixd(thisT.getMatrix(tform));
					stackDepth++;
				}
				else {
					globalGL.glLoadTransposeMatrixd(context.getObjectToCamera());	
				}
				currentSignature = thisT.getSignature();
			}  
			
			if (parent != null) cumulativeIsReflection = (isReflection != parent.cumulativeIsReflection);
			else cumulativeIsReflection = (isReflection != globalIsReflection);
			if (cumulativeIsReflection != openGLState.flipped)	{
				globalGL.glFrontFace(cumulativeIsReflection ? GL.GL_CW : GL.GL_CCW);
				openGLState.flipped  = cumulativeIsReflection;
			}

			if (appearanceChanged)  	propagateAppearanceChanged();
			if (appearanceIsDirty)		updateAppearance();
			JOGLConfiguration.theLog.log(Level.FINEST, goBetween.getOriginalComponent().getName()+"Using display list: "+useDisplayLists);
			if (goBetween.getPeerGeometry() != null)	{
				Scene.executeReader(goBetween.peerGeometry.originalGeometry, renderGeometry );
				//goBetween.peerGeometry.render(this);
			}
			
			synchronized(childLock)	{
				int n = children.size();
				for (int i = 0; i<n; ++i)	{		
					JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);					
					if (pickMode)	globalGL.glPushName(JOGLPickAction.SGCOMP_BASE+child.childIndex);
					child.render();
					if (pickMode)	globalGL.glPopName();
				}				
			}
			
			if (thisT != null)	{
				if (stackDepth <= MAX_STACK_DEPTH) {
					globalGL.glPopMatrix();			
					stackDepth--;
				}
			}			
			currentPath.pop();
		}
		
		public void setIndexOfChildren()	{
			synchronized(childLock){
				int n = goBetween.getOriginalComponent().getChildComponentCount();
				//synchronized(goBetween.getOriginalComponent().getChildLock()) {
					for (int i = 0; i<n; ++i)	{
						SceneGraphComponent sgc = goBetween.getOriginalComponent().getChildComponent(i);
						JOGLPeerComponent jpc = getPeerForChildComponent(sgc);
						if (jpc == null)	{
							theLog.log(Level.WARNING,"No peer for sgc "+sgc.getName());
							jpc.childIndex = -1;
						} else jpc.childIndex = i;
					}									
				//}
			}
		}

		
		private void updateAppearance()	{
			Appearance thisAp = goBetween.getOriginalComponent().getAppearance(); 
			if (parent == null)	{
				if (eAp == null) {
					eAp = EffectiveAppearance.create();
					if (goBetween.getOriginalComponent().getAppearance() != null )	
						eAp = eAp.create(goBetween.getOriginalComponent().getAppearance());
				}
			} else {
				if ( parent.eAp == null)	{
					theLog.log(Level.WARNING,"No effective appearance in parent "+parent.getName());
					return;
				}
				// TODO when Appearance's are added or removed, have to set eAp to null
				if (eAp == null)	{
					if (thisAp != null )	
						eAp = parent.eAp.create(thisAp);
					else {
						eAp = parent.eAp;	
					}
				}
			}
			if (thisAp == null && parent != null)	{
				geometryShader = parent.geometryShader;
				renderingHints = parent.renderingHints;
			} else {
				if (geometryShader == null)
					geometryShader = DefaultGeometryShader.createFromEffectiveAppearance(eAp, "");
				else 
					geometryShader.setFromEffectiveAppearance(eAp, "");
				
				
				//theLog.log(Level.FINE,"component "+goBetween.getOriginalComponent().getName()+" vertex draw is "+geometryShader.isVertexDraw());
				if (renderingHints == null)
					renderingHints = RenderingHintsShader.createFromEffectiveAppearance(eAp, "");
				else
					renderingHints.setFromEffectiveAppearance(eAp, "");				
			}
			useDisplayLists = renderingHints.isUseDisplayLists();
			appearanceIsDirty = false;
		}
		
		/**
		 * @param sgc
		 * @return
		 */
		private JOGLPeerComponent getPeerForChildComponent(SceneGraphComponent sgc) {
			
			int n = children.size();
			for (int i = 0; i<n; ++i)	{
				JOGLPeerComponent jpc = (JOGLPeerComponent) children.get(i);
				if ( jpc.goBetween.getOriginalComponent() == sgc) { // found!
					return jpc;
				}
			}
			return null;
		}

		public void childAdded(SceneGraphComponentEvent ev) {
			theLog.log(Level.FINE,"Container Child added to: "+goBetween.getOriginalComponent().getName());
			//theLog.log(Level.FINE,"Event is: "+ev.toString());
			switch (ev.getChildType() )	{
				case SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY:
					updateRenderRunnable();
					break;

				case SceneGraphComponentEvent.CHILD_TYPE_COMPONENT:
					SceneGraphComponent sgc = (SceneGraphComponent) ev.getNewChildElement();
					JOGLPeerComponent pc = globalHandle.constructPeerForSceneGraphComponent(sgc, this);
					synchronized(childLock)	{
				    //theLog.log(Level.FINE,"Before adding child count is "+children.size());
						children.add(pc);						
					  
					//theLog.log(Level.FINE,"After adding child count is "+children.size());
					}
					setIndexOfChildren();
					lightListDirty = true;
					break;
				case SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE:
					int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
					propagateGeometryChanged(changed);	
					appearanceChanged = true;
					theLog.log(Level.FINE,"Propagating geometry change due to added appearance");
					if (this == thePeerRoot) {
						theRoot.getAppearance().addAppearanceListener(globalHandle);
						extractGlobalParameters();
					}
					break;				
				case SceneGraphComponentEvent.CHILD_TYPE_LIGHT:
					lightListDirty = true;
					break;
				case SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION:
					updateTransformationInfo();
					break;
				default:
					theLog.log(Level.INFO,"Taking no action for addition of child type "+ev.getChildType());
					break;
			}
		}
		
		public void childRemoved(SceneGraphComponentEvent ev) {
			//theLog.log(Level.FINE,"Container Child removed from: "+goBetween.getOriginalComponent().getName());
			switch (ev.getChildType() )	{
				case SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY:
					updateRenderRunnable();
					break;
	
				case SceneGraphComponentEvent.CHILD_TYPE_COMPONENT:
					SceneGraphComponent sgc = (SceneGraphComponent) ev.getOldChildElement();
				    JOGLPeerComponent jpc = getPeerForChildComponent(sgc);
				    if (jpc == null) return;
				    //theLog.log(Level.FINE,"removing peer "+jpc.getName());
				    //SystheLog.log(Level.FINE,("Before removal child count is "+children.size());
					synchronized(childLock)	{
					    children.remove(jpc);						
					}
					//theLog.log(Level.FINE,"After removal child count is "+children.size());
				    jpc.dispose();		// there are no other references to this child
				    setIndexOfChildren();
				    lightListDirty = true;
					break;
				case SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE:
					int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
					propagateGeometryChanged(changed);	
					appearanceChanged = true;
					if (this == thePeerRoot) {
						((Appearance) ev.getOldChildElement()).removeAppearanceListener(globalHandle);
					}
					theLog.log(Level.INFO,"Propagating geometry change due to removed appearance");
					break;				
				case SceneGraphComponentEvent.CHILD_TYPE_LIGHT:
					lightListDirty = true;
					break;
				case SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION:
					updateTransformationInfo();
					break;			
				default:
					theLog.log(Level.INFO,"Taking no action for removal of child type "+ev.getChildType());
					break;
		}
		}
		
		public void childReplaced(SceneGraphComponentEvent ev) {
			//theLog.log(Level.FINE,"Container Child replaced at: "+goBetween.getOriginalComponent().getName());
			switch(ev.getChildType())	{
				case SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY:
					updateRenderRunnable();
					break;

				case SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE:
					int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
					propagateGeometryChanged(changed);	
					appearanceChanged = true;
					if (this == thePeerRoot) {
						theRoot.getAppearance().addAppearanceListener(globalHandle);
						extractGlobalParameters();
					}
					theLog.log(Level.INFO,"Propagating geometry change due to replaced appearance");
					break;
				case SceneGraphComponentEvent.CHILD_TYPE_LIGHT:
					lightListDirty = true;
					break;
				case SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION:
					updateTransformationInfo();
					break;
				default:
					theLog.log(Level.INFO,"Taking no action for replacement of child type "+ev.getChildType());
					break;
			}
		}
		
		public void transformationMatrixChanged(TransformationEvent ev) {
			// TODO notify ancestors that their bounds are no longer valid
			updateTransformationInfo();
		}
		
		/**
		 * 
		 */
		private void updateTransformationInfo() {
			if (goBetween.getOriginalComponent().getTransformation() != null) {
//				isReflection = goBetween.getOriginalComponent().getTransformation().getIsReflection();
				isReflection = Rn.determinant(goBetween.getOriginalComponent().getTransformation().getMatrix()) < 0;
			} else {
				determinant  = 0.0;
				isReflection = false;
			}
		}

		public void appearanceChanged(AppearanceEvent ev) {
			appearanceChanged = true;
		}
		
		private void propagateAppearanceChanged()	{
			appearanceIsDirty = true;	
			int n = children.size();
			for (int i = 0; i<n; ++i)	{		
				JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);
				child.propagateAppearanceChanged();
			}
			appearanceChanged = false;
		}
		
		/**
		 * @param changed
		 */
		public void propagateGeometryChanged(int changed) {
			if (goBetween != null && goBetween.getPeerGeometry() != null) 
				goBetween.getPeerGeometry().geometryChanged(changed);
			dlInfo.geometryChanged(changed);
			synchronized(childLock)	{
				int n = children.size();
				for (int i = 0; i<n; ++i)	{		
					JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);
					child.propagateGeometryChanged(changed);
				}				
			}
		}

		private void setDisplayListDirty(boolean b)	{
			if (b) propagateGeometryChanged(POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
		}
		
		public SceneGraphComponent getOriginalComponent() {
			return goBetween.getOriginalComponent();
		}

		public void visibilityChanged(SceneGraphComponentEvent ev) {
		}
	}

	
	public GLDrawable getCanvas()	{
		return theCanvas;
	}

	public void setCanvas(GLDrawable d)	{
		theCanvas = d;
	}
	/**
	 * @param file
	 */
	File screenShotFile = null;
	public void saveScreenShot(File file)	{
		screenShot = true;
		screenShotFile = file;
		theCanvas.display();
		screenShot = false;
	}
	public Graphics3D getContext() {
		return context;
	}
	public boolean isPickMode() {
		return pickMode;
	}
	public void setPickMode(boolean pickMode) {
		this.pickMode = pickMode;
	}


	public int[] getCurrentViewport()	{
		return currentViewport;
	}



	/* (non-Javadoc)
	 * @see de.jreality.scene.Drawable#getAspectRatio()
	 */
	public double getAspectRatio() {
		return ((double) getWidth())/getHeight();
	}



	/* (non-Javadoc)
	 * @see de.jreality.scene.Drawable#getWidth()
	 */
	public int getWidth() {
		return currentViewport[2];
	}



	/* (non-Javadoc)
	 * @see de.jreality.scene.Drawable#getHeight()
	 */
	public int getHeight() {
		return currentViewport[3];
	}



	/* (non-Javadoc)
	 * @see de.jreality.scene.Drawable#getXMin()
	 */
	public int getXMin() {
		return currentViewport[0];
	}



	/* (non-Javadoc)
	 * @see de.jreality.scene.Drawable#getYMin()
	 */
	public int getYMin() {
		return currentViewport[1];
	}


}
