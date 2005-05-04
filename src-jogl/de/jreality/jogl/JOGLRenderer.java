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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.java.games.jogl.DebugGL;
import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLU;
import net.java.games.jogl.util.BufferUtils;
import de.jreality.geometry.LabelSet;
import de.jreality.geometry.SphereHelper;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.jogl.shader.DefaultGeometryShader;
import de.jreality.jogl.shader.RenderingHintsShader;
import de.jreality.jogl.shader.TextShader;
import de.jreality.scene.Camera;
import de.jreality.scene.Drawable;
import de.jreality.scene.Geometry;
import de.jreality.scene.Graphics3D;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.scene.event.SceneAncestorListener;
import de.jreality.scene.event.SceneContainerEvent;
import de.jreality.scene.event.SceneContainerListener;
import de.jreality.scene.event.SceneHierarchyEvent;
import de.jreality.scene.event.SceneTreeListener;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.scene.pick.PickPoint;
import de.jreality.util.CameraUtility;
import de.jreality.util.ClippingPlaneCollector;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.LightCollector;
import de.jreality.util.P3;
import de.jreality.util.Pn;
import de.jreality.util.Rectangle3D;
import de.jreality.util.Rn;
/**
 * TODO implement  isVisible   bit in SceneGraphNode
 * TODO implement collectAncestorVisitor (see method geometryChanged() at end of file )
 * @author gunn
 *
 */
public class JOGLRenderer extends SceneGraphVisitor implements Drawable {

	final static Logger theLog = JOGLConfiguration.theLog;
	final static boolean debugGL = JOGLConfiguration.debugGL;
	
	static boolean collectFrameRate = true;
	public final static int MAX_STACK_DEPTH = 30;
	protected int stackDepth;
	JOGLRenderer globalHandle = null;
	SceneGraphPath currentPath = new SceneGraphPath();
	
	de.jreality.jogl.Viewer theViewer;
	SceneGraphComponent theRoot, auxiliaryRoot;
	JOGLPeerComponent thePeerRoot = null;
	JOGLPeerComponent thePeerAuxilliaryRoot = null;
	JOGLRendererHelper helper;

	GLCanvas theCanvas;
	int[] currentViewport = new int[4];
	Graphics3D context;
	public GL globalGL;
	GLU globalGLU;
	int[] sphereDisplayLists;
	public OpenGLState openGLState = new OpenGLState();
	public  boolean texResident;
	int numberTries = 0;		// how many times we have tried to make textures resident
	boolean  useDisplayLists;
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
	/**
	 * @param viewer
	 */
	public JOGLRenderer(de.jreality.jogl.Viewer viewer) {
		super();
		theViewer = viewer;
		theCanvas = ((GLCanvas) viewer.getViewingComponent());
		theRoot = viewer.getSceneRoot();
		auxiliaryRoot = viewer.getAuxiliaryRoot();
		useDisplayLists = true;
		theLog.log(Level.FINER, "Looked up logger successfully");
		
		globalHandle = this;
		helper = new JOGLRendererHelper();
		
		javax.swing.Timer followTimer = new javax.swing.Timer(1000, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {updateGeometryHashtable(); } } );
		followTimer.start();
		
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
			theRoot = theViewer.getSceneRoot();
			thePeerRoot = constructPeerForSceneGraphComponent(theRoot, null);
		}
		
		context  = new Graphics3D(theViewer);
		
		globalGL.glMatrixMode(GL.GL_PROJECTION);
		globalGL.glLoadIdentity();

		if (!pickMode)
			JOGLRendererHelper.handleBackground(theCanvas, theRoot.getAppearance());
		if (pickMode)
			globalGL.glMultTransposeMatrixd(pickT.getMatrix());

		theCanvas.setAutoSwapBufferMode(!pickMode);
		
		// We "inline" the visit to the camera since it cannot be visited in the traversal order
		// load the projection transformation
		globalGL.glMultTransposeMatrixd(CameraUtility.getCamera(theViewer).getCameraToNDC());

		// prepare for rendering the geometry
		globalGL.glMatrixMode(GL.GL_MODELVIEW);
		if (backSphere) {  globalGL.glLoadTransposeMatrixd(p3involution);	globalGL.glPushMatrix(); }
		double[] w2c = context.getWorldToCamera();
		globalGL.glLoadTransposeMatrixd(w2c);
		globalIsReflection = (theViewer.isFlipped != (Rn.determinant(w2c) < 0.0));
		if (!pickMode) processLights();
		
		processClippingPlanes();
		
		nodeCount = 0;			// for profiling info
		texResident = true;		// assume the best ...
		OpenGLState.initializeGLState(this);
		thePeerRoot.render();		
		if (!pickMode && thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.render();
		if (backSphere) globalGL.glPopMatrix();
		globalGL.glLoadIdentity();
		forceResidentTextures();
		
		lightListDirty = false;
		return null;
	}
	
	/**
	 * @param theRoot2
	 * @return
	 */
	protected JOGLPeerComponent constructPeerForSceneGraphComponent(SceneGraphComponent sgc, JOGLPeerComponent p) {
		if (sgc == null) return null;
		JOGLPeerComponent peer = null;
		synchronized(sgc.getChildLock())	{
			ConstructPeerGraphVisitor constructPeer = new ConstructPeerGraphVisitor( sgc, p);
			peer = (JOGLPeerComponent) constructPeer.visit();				
		}
		return peer;
	}



	/**
	 * 
	 */
	private void processClippingPlanes() {
		List clipPlanes = null;
		if (clipPlanes == null)	{
			ClippingPlaneCollector lc = new ClippingPlaneCollector(theRoot);
			clipPlanes = (List) lc.visit();			
		}
		helper.processClippingPlanes(globalGL, clipPlanes);
	}

	// TODO convert this to peer structure
	static List lights = null;
	/**
	 * @param theRoot2
	 * @param globalGL2
	 * @param lightListDirty2
	 */
	private void processLights( ) {
		if (lights == null || lights.size() == 0 || lightListDirty) {
			LightCollector lc = new LightCollector(theRoot);
			lights = (List) lc.visit();
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
			doIt.schedule(rerenderTask, 10);
			forceNewDisplayLists();
			numberTries++;		// don't keep trying indefinitely
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
		theCanvas = (GLCanvas) drawable;
		// it should be possible to set these once and for all, since each context (a GL instance)
		// gets its own init() call.
		globalGL = theCanvas.getGL();
		globalGLU = theCanvas.getGLU();

		if (debugGL)	{
			String vv = globalGL.glGetString(GL.GL_VERSION);
			theLog.log(Level.INFO,"version: "+vv);			
			int[] tu = new int[1];
			globalGL.glGetIntegerv(GL.GL_MAX_TEXTURE_UNITS, tu);
			theLog.log(Level.INFO,"# of texture units: "+tu[0]);			
		}
		
//		otime = System.currentTimeMillis();
		// all display lists need to be set to dirty
		if (thePeerRoot != null) thePeerRoot.propagateGeometryChanged(ALL_CHANGED);
		sphereDisplayLists = JOGLSphereHelper.getSphereDLists(this);
		if (debugGL)	theLog.log(Level.INFO,"Got new sphere display lists for context "+globalGL);
		
		if (CameraUtility.getCamera(theViewer) == null || theCanvas == null) return;
		CameraUtility.getCamera(theViewer).setAspectRatio(((double) theCanvas.getWidth())/theCanvas.getHeight());
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
				int w = theCanvas.getWidth()/2;
				int h = theCanvas.getHeight();
				theCamera.setAspectRatio(((double) w)/h);
				theCamera.setEye(Camera.RIGHT_EYE);
				theCamera.update();
				globalGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				myglViewport(0,0, w,h);
				visit();
				theCamera.setEye(Camera.LEFT_EYE);
				myglViewport(w, 0, w,h);
				visit();
			} 
			else if (which >= Viewer.RED_BLUE_STEREO &&  which <= Viewer.RED_CYAN_STEREO) {
				theCamera.setAspectRatio(((double) theCanvas.getWidth())/theCanvas.getHeight());
				myglViewport(0,0, theCanvas.getWidth(), theCanvas.getHeight());
				globalGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				theCamera.setEye(Camera.RIGHT_EYE);
		        if (which == Viewer.RED_GREEN_STEREO) globalGL.glColorMask(false, true, false, true);
		        else if (which == Viewer.RED_BLUE_STEREO) globalGL.glColorMask(false, false, true, true);
		        else if (which == Viewer.RED_CYAN_STEREO) globalGL.glColorMask(false, true, true, true);
				visit();
				theCamera.setEye(Camera.LEFT_EYE);
		        globalGL.glColorMask(true, false, false, true);
				globalGL.glClear (GL.GL_DEPTH_BUFFER_BIT);
				visit();
		        globalGL.glColorMask(true, true, true, true);
			} 
			else	{
				theCamera.setAspectRatio(((double) theCanvas.getWidth())/theCanvas.getHeight());
				myglViewport(0,0, theCanvas.getWidth(), theCanvas.getHeight());
				theCamera.setEye(Camera.RIGHT_EYE);
				globalGL.glDrawBuffer(GL.GL_BACK_RIGHT);
				globalGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				visit();
				theCamera.setEye(Camera.LEFT_EYE);
				globalGL.glDrawBuffer(GL.GL_BACK_LEFT);
				globalGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				visit();
			}
		} 
		else {
			globalGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			theCamera.setAspectRatio(((double) theCanvas.getWidth())/theCanvas.getHeight());
			theCamera.setEye(Camera.MIDDLE_EYE);
			myglViewport(0,0, theCanvas.getWidth(), theCanvas.getHeight());
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
				
				pickT.setTranslation(pp3);
				double[] stretch = {pickScale, pickScale, 1.0};
				pickT.setStretch(stretch);
				boolean store = isUseDisplayLists();
				useDisplayLists = false;
				thePeerRoot.propagateGeometryChanged(POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
				globalGL.glSelectBuffer(bufsize, selectBuffer);		
				globalGL.glRenderMode(GL.GL_SELECT);
				globalGL.glInitNames();
				globalGL.glPushName(0);
				visit();
				pickMode = false;
				thePeerRoot.propagateGeometryChanged(POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
				int numberHits = globalGL.glRenderMode(GL.GL_RENDER);
				//JOGLConfiguration.theLog.log(Level.INFO,numberHits+" hits");
				hits = JOGLPickAction.processOpenGLSelectionBuffer(numberHits, selectBuffer, pickPoint,theViewer);
				useDisplayLists = store;
				display(drawable);
			}
		}
		if (screenShot)   JOGLRendererHelper.saveScreenShot(theCanvas, screenShotFile);
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
		CameraUtility.getCamera(theViewer).setAspectRatio(((double) theCanvas.getWidth())/theCanvas.getHeight());
		myglViewport(0,0, theCanvas.getWidth(), theCanvas.getHeight());
	}

	/**
	 * @return
	 */
	public boolean isUseDisplayLists() {
		return useDisplayLists;
	}

	/**
	 * @param b
	 */
	public void setUseDisplayLists(boolean b) {
		useDisplayLists = b;
		if (useDisplayLists)	forceNewDisplayLists();
	}

//	public void geometryChanged(GeometryEvent e) {
//		}
		/*
		CollectAncestorsVisitor cav = new CollectAncestorsVisitor(theRoot, sg);
		List anc = (List) cav.visit();
		for (int i = 0; i<anc.size(); ++i)		{
			Object ancx = anc.get(i);
			//System.err.println("ancestor: "+ancx);
			foo = dlTable.get(ancx);
			if (foo!= null) dlTable.remove(ancx);
		}
		*/
//	}
	private final static int POINTDL = 0;
	private final static int PROXY_POINTDL = 1;
	private final static int LINEDL = 2;
	private final static int PROXY_LINEDL = 3;
	private final static int FLAT_POLYGONDL = 4;
	private final static int SMOOTH_POLYGONDL = 5;
    private final static int NUM_DLISTS = 6;
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

		public void setFlatPolygonDisplayListID(int id)	{
			dl[FLAT_POLYGONDL] = id;
		}
		
		public int getFlatPolygonDisplayListID() {
			return dl[FLAT_POLYGONDL];
		}
		
		public void setSmoothPolygonDisplayListID(int id)	{
			dl[SMOOTH_POLYGONDL] = id;
		}
		
		public int getSmoothPolygonDisplayListID() {
			return dl[SMOOTH_POLYGONDL];
		}
		
		public void setLineDisplayListID(int id)	{
			dl[LINEDL] = id;
		}
		
		public int getLineDisplayListID() {
			return dl[LINEDL];
		}
		
		public int getTubeDisplayListID() {
			return dl[PROXY_LINEDL];
		}
		
		public void setTubeDisplayListID(int id)	{
			dl[PROXY_LINEDL] = id;
		}
		
		public void setPointDisplayListID(int id)	{
			dl[POINTDL] = id;
		}
		
		public int getPointDisplayListID() {
			return dl[POINTDL];
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
	
	public class JOGLPeerNode	{
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
		Logger.getLogger("de.jreality.jogl").log(Level.FINEST, "Memory usage: "+getMemoryUsage());
		if (geometries == null) return;
		if (!geometryRemoved) return;
		final Hashtable newG = new Hashtable();
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
			theLog.log(Level.INFO,"Old, new hash size: "+geometries.size()+" "+newG.size());
			geomDiff = geometries.size() - newG.size() ;
		}
		return;
	}

	public String getMemoryUsage() {
        Runtime r = Runtime.getRuntime();
        int block = 1024;
        return
                "Memory usage: " + ((r.totalMemory() / block) - (r.freeMemory() / block)) + " kB";
    }
	
	
	public class JOGLPeerGeometry extends JOGLPeerNode implements GeometryListener	{
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
				//JOGLConfiguration.theLog.log(Level.INFO,"Geometry is no longer referenced");
				originalGeometry.removeGeometryListener(this);	
				geometries.remove(originalGeometry);
			}
		}
		
		public void geometryChanged(GeometryEvent ev) {
			//TODO make more differentiated response based on event ev
			final SceneGraphNode sg = (SceneGraphNode) ev.getSource();
			dlInfo.setChange();
		}
		
		public void geometryChanged(int type)	{
			if ((type & POINTS_CHANGED) != 0)	{
				dlInfo.setDisplayListDirty(POINTDL, true);
				dlInfo.setDisplayListDirty(PROXY_POINTDL, true);
			}
			if ((type & LINES_CHANGED) != 0)	{
				dlInfo.setDisplayListDirty(LINEDL, true);
				dlInfo.setDisplayListDirty(PROXY_LINEDL, true);
			}
			if ((type & FACES_CHANGED) != 0)	{
				dlInfo.setDisplayListDirty(FLAT_POLYGONDL, true);
				dlInfo.setDisplayListDirty(SMOOTH_POLYGONDL, true);
			}
			theLog.log(Level.FINER,"Setting display lists dirty with flag: "+type);
		}
		/**
		 * 
		 */
		public void render(JOGLPeerComponent jpc) {
			//theLog.log(Level.FINER,"In JOGLPeerGeometry render() for "+originalGeometry.getName());
			RenderingHintsShader renderingHints = jpc.renderingHints;
			DefaultGeometryShader geometryShader = jpc.geometryShader;
			renderingHints.render(globalHandle);
			if (geometryShader.isFaceDraw() && originalGeometry instanceof Sphere)	{
//				IndexedFaceSet foo = getProxyFor((Sphere) originalGeometry);
//				ifs = foo;
//				ils = foo;
//				ps = foo;
				
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
			}		
			else 	if (originalGeometry instanceof LabelSet)	{
				// NOTE we don't use display lists here because the text rendering is done in screen
				// coordinates and must be recalculated for each frame
				TextShader textShader = new TextShader();
				textShader.setFromEffectiveAppearance(jpc.eAp, "textShader");
				textShader.render(globalHandle);
				JOGLRendererHelper.drawLabels(((LabelSet) originalGeometry), globalHandle);
			}

			if (geometryShader.isFaceDraw() && ifs != null)	{
				geometryShader.polygonShader.render(globalHandle);
				double alpha = openGLState.diffuseColor[3];
				boolean ss = openGLState.smoothShading;
				int type = ss ? SMOOTH_POLYGONDL : FLAT_POLYGONDL;
				boolean proxy = geometryShader.polygonShader.providesProxyGeometry();
				if (proxy && dlInfo.isDisplayListDirty(type))	{
					//theLog.log(Level.FINER,"Asking "+geometryShader.polygonShader+" for proxy geometry ");
					int dl  = geometryShader.polygonShader.proxyGeometryFor(ils, globalHandle, currentSignature);
					if (dl != -1) {
						dlInfo.setDisplayListID(type, dl);
						dlInfo.setDisplayListDirty(type, false);
					}
				}
				if (proxy)	globalGL.glCallList(dlInfo.getDisplayListID(type));
				else 	{
					if (!processDisplayListState(type))		 // false return implies no display lists used
						JOGLRendererHelper.drawFaces(ifs, globalHandle,ss, alpha, pickMode, JOGLPickAction.GEOMETRY_FACE);
					else // we are using display lists
						if (dlInfo.isInsideDisplayList())	{		// display list wasn't clean, so we have to regenerate it
							JOGLRendererHelper.drawFaces(ifs, globalHandle, ss, alpha, pickMode, JOGLPickAction.GEOMETRY_FACE);
							globalGL.glEndList();	
							globalGL.glCallList(dlInfo.getDisplayListID(type));
							dlInfo.setDisplayListDirty(type, false);
							dlInfo.setInsideDisplayList(false);							
						}					
				}
				geometryShader.polygonShader.postRender(globalHandle);
			}
			if (geometryShader.isEdgeDraw() && ils != null)	{
				geometryShader.lineShader.render(globalHandle);
				boolean proxy = geometryShader.lineShader.providesProxyGeometry();
				double alpha = openGLState.diffuseColor[3];
				boolean smooth = false; //openGLState.smoothShading;;
				int type = proxy ? PROXY_LINEDL : LINEDL;
				if (proxy && dlInfo.isDisplayListDirty(PROXY_LINEDL))	{
					theLog.log(Level.FINER,"Recalculating tubes");
					int dl  = geometryShader.lineShader.proxyGeometryFor(ils, globalHandle, currentSignature);
					if (dl != -1) {
						dlInfo.setDisplayListID(type, dl);
						dlInfo.setDisplayListDirty(type, false);
					}
				}
				if (proxy)	globalGL.glCallList(dlInfo.getDisplayListID(type));
				else {
					if (!processDisplayListState(type))		 // false return implies no display lists used
						JOGLRendererHelper.drawLines(ils, globalHandle, pickMode, smooth, alpha);			
					else // we are using display lists
						if (dlInfo.isInsideDisplayList())	{		// display list wasn't clean, so we have to regenerate it
							JOGLRendererHelper.drawLines(ils, globalHandle, pickMode, smooth, alpha);			
							globalGL.glEndList();	
							globalGL.glCallList(dlInfo.getDisplayListID(type));
							dlInfo.setDisplayListDirty(type, false);
							dlInfo.setInsideDisplayList(false);							
						}
				}
			}
			if (geometryShader.isVertexDraw() && ps != null)	{
				geometryShader.pointShader.render(globalHandle);
				double alpha = geometryShader.pointShader.getDiffuseColor().getAlpha()/255.0;
				boolean proxy = geometryShader.pointShader.providesProxyGeometry();
				int type = proxy ? PROXY_POINTDL : POINTDL;
				if (proxy && dlInfo.isDisplayListDirty(type))	{
					theLog.log(Level.FINER,"Recalculating spheres");
					int dl  = geometryShader.pointShader.proxyGeometryFor(ps, globalHandle, currentSignature);
					if (dl != -1) {
						theLog.log(Level.FINER,"spheres created");
						dlInfo.setDisplayListID(type, dl);
						dlInfo.setDisplayListDirty(type, false);
					}
				}
				if (proxy) globalGL.glCallList(dlInfo.getDisplayListID(type));
				else {
					if (!processDisplayListState(type))		{// false return implies no display lists used
						JOGLRendererHelper.drawVertices(ps, globalHandle, pickMode, alpha);			
					}  else  {		// we are using display lists 
						if (dlInfo.isInsideDisplayList())	{		// display list wasn't clean, so we have to regenerate it
							JOGLRendererHelper.drawVertices(ps, globalHandle,  pickMode, alpha);			
							globalGL.glEndList();	
							globalGL.glCallList(dlInfo.getDisplayListID(type));
							dlInfo.setDisplayListDirty(type, false);
							dlInfo.setInsideDisplayList(false);
						}
					}			
				}
			}
		}

		private boolean processDisplayListState(int type)	{
			DisplayListInfo dl = dlInfo;
			
			//theLog.log(Level.FINER,"Valid display list for "+pc.getOriginalComponent().getName()+"is "+dl.isValidDisplayList());
			dl.setInsideDisplayList(false);
			if (!useDisplayLists || !dl.useDisplayList()) {
				return false;
			}
			if (!dl.isDisplayListDirty(type))	{
				//JOGLConfiguration.theLog.log(Level.INFO,"Using display list");
				globalGL.glCallList(dlInfo.getDisplayListID(type));
				return true;
			}
			
			if (dl.getDisplayListID(type) != -1) {
				globalGL.glDeleteLists(dl.getDisplayListID(type), 1);
				dl.setDisplayListID(type, -1);
			}
			int nextDL = globalGL.glGenLists(1);
			JOGLConfiguration.theLog.log(Level.FINE, "Allocating new display list "+nextDL);
			dl.setDisplayListID(type, nextDL);
			globalGL.glNewList(dl.getDisplayListID(type), GL.GL_COMPILE); //_AND_EXECUTE);
			//JOGLConfiguration.theLog.log(Level.INFO,"Beginning display list for "+originalGeometry.getName());
			dl.setInsideDisplayList(true);
			return true;
		}

		/**
		 * @param sphere
		 * @return
		 */
		private IndexedFaceSet getProxyFor(Sphere sphere) {
			return SphereHelper.tessellatedIcosahedra[2];
		}
	}
	
//	public void visit(Sphere sg) {
//		//Primitives.sharedIcosahedron.accept(this);
//		//double lod = renderingHints.getLevelOfDetail();
//		SceneGraphComponent helper = null;
//		JOGLConfiguration.theLog.log(Level.INFO,"Rendering sphere");
//		//if (lod == 0.0) 
//			
//		else	{
//			double area = GeometryUtility.getNDCArea(sg, gc.getObjectToNDC());
//			if (area < .01)	helper = SphereHelper.SPHERE_COARSE;
//			else if (area < .1 ) helper = SphereHelper.SPHERE_FINE;
//			else if (area < .5) helper = SphereHelper.SPHERE_FINER;
//			else helper = SphereHelper.SPHERE_FINEST;
//		}
//		SphereHelper.SPHERE_FINE.accept(this);
//		//Rectangle3D uc = Rectangle3D.unitCube;
//		//SphereHelper.SPHERE_BOUND.accept(this);
//	}
	
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
	static WeakHashMap goBetweenTable = new WeakHashMap();
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
	
	protected class GoBetween implements TransformationListener, AppearanceListener,
	SceneAncestorListener, SceneContainerListener, SceneTreeListener	{
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
			originalComponent.addSceneAncestorListener(this);
			originalComponent.addSceneContainerListener(this);
			originalComponent.addSceneTreeListener(this);
			if (originalComponent.getAppearance() != null) originalComponent.getAppearance().addAppearanceListener(this);				
		}
		
		public void dispose()	{
			originalComponent.removeSceneAncestorListener(this);
			originalComponent.removeSceneContainerListener(this);
			originalComponent.removeSceneTreeListener(this);
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
			Iterator iter = peers.iterator();
			String key = ev.getKey();
			int changed = 0;
			// TODO shaders should register keywords somehow and which geometries might be changed
			if (key.indexOf("implodeFactor") != -1 ) changed |= (FACES_CHANGED);
			else if (key.indexOf("transparency") != -1) changed |= (FACES_CHANGED);
			else if (key.indexOf("tubeRadius") != -1) changed |= (LINES_CHANGED);
			else if (key.indexOf("pointRadius") != -1) changed |= (POINTS_CHANGED);
//			peerGeometry.geometryChanged(changed);
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.appearanceChanged(ev);
				if (changed != 0) peer.propagateGeometryChanged(changed);
			}
			//theLog.log(Level.FINER,"setting display list dirty flag: "+changed);
		}
		
		public void ancestorAttached(SceneHierarchyEvent ev) {
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.ancestorAttached(ev);
			}
		}
		public void ancestorDetached(SceneHierarchyEvent ev) {
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.ancestorDetached(ev);
			}
		}
		public void childAdded(SceneContainerEvent ev) {
			if  (ev.getChildType() ==  SceneContainerEvent.CHILD_TYPE_GEOMETRY) {
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
					return;
			}
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childAdded(ev);
			}
		}
		public void childRemoved(SceneContainerEvent ev) {
			if  (ev.getChildType() ==  SceneContainerEvent.CHILD_TYPE_GEOMETRY) {
				if (peerGeometry != null) {
					peerGeometry.dispose();		// really decreases reference count
					peerGeometry = null;
					geometryRemoved = true;
				}
				return;
			}
			boolean apAdded = (ev.getChildType() ==  SceneContainerEvent.CHILD_TYPE_APPEARANCE);
			int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childRemoved(ev);
				if (apAdded) peer.propagateGeometryChanged(changed);
			}
		}
		
		public void childReplaced(SceneContainerEvent ev) {
			if  (ev.getChildType() ==  SceneContainerEvent.CHILD_TYPE_GEOMETRY) {
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
					return;
			}
			boolean apAdded = (ev.getChildType() ==  SceneContainerEvent.CHILD_TYPE_APPEARANCE);
			int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childReplaced(ev);
				if (apAdded) peer.propagateGeometryChanged(changed);
				}				
		}
		public void childAdded(SceneHierarchyEvent ev) {
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childAdded(ev);
			}
		}
		public void childRemoved(SceneHierarchyEvent ev) {
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childRemoved(ev);
			}
		}
		public void childReplaced(SceneHierarchyEvent ev) {
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childReplaced(ev);
			}
		}
		
		public SceneGraphComponent getOriginalComponent() {
			return originalComponent;
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
		  	//peer.setIndexOfChildren();
		  	sgp.pop();
		}

		public Object visit()	{
			visit(myRoot);
			return thePeerRoot;
		}
		
	}
	public class JOGLPeerComponent extends JOGLPeerNode implements TransformationListener, AppearanceListener,
		SceneAncestorListener, SceneContainerListener, SceneTreeListener {
		
		public int[] bindings = new int[2];
		//SceneGraphComponent originalComponent;
		EffectiveAppearance eAp;
		Vector children;
		JOGLPeerComponent parent;
		int childIndex;
		GoBetween goBetween;
		//JOGLPeerGeometry peerGeometry;
		
		Rectangle3D childrenBound;
		Rectangle2D ndcExtent;
		SceneGraphPath	pathToHere;
		boolean isReflection = false, cumulativeIsReflection = false;
		double determinant = 0.0;
		
		// for now, keep the primitive shading support
		RenderingHintsShader renderingHints;
		DefaultGeometryShader geometryShader;
		
		Object childLock = new Object();		
		
		boolean appearanceChanged,
			appearanceIsDirty,
			geometryIsDirty,
			boundIsDirty,
			object2WorldDirty;

		public JOGLPeerComponent(SceneGraphPath sgp, JOGLPeerComponent p)		{
			super();
			if (sgp == null || !(sgp.getLastElement() instanceof SceneGraphComponent))  {
				throw new IllegalArgumentException("Not a valid SceneGraphComponenet");
			}
			pathToHere = sgp;
			goBetween = goBetweenFor(sgp.getLastComponent());
			goBetween.addJOGLPeer(this);
			name = "JOGLPeer:"+goBetween.getOriginalComponent().getName();
			appearanceChanged = false;
			appearanceIsDirty = true;
			geometryIsDirty = true;
			boundIsDirty = true;
			object2WorldDirty = true;
			children = new Vector();		// always have a child list, even if it's empty
			parent = p;
			updateTransformationInfo();
		}
		
		public void dispose()	{
			//synchronized(childLock)	{
				int n = children.size();
				for (int i = n-1; i>=0; --i)	{
					JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);
					child.dispose();
				}	
				goBetween.removeJOGLPeer(this);
			//}

		}
		public void render()		{
			theLog.log(Level.FINE,"Rendering "+goBetween.getOriginalComponent().getName());
			if (!goBetween.getOriginalComponent().isVisible()) return;
						
			// the following looks very dangerous
			//theLog.log(Level.FINE,"Rendering node "+nodeCount);
			nodeCount++;
			currentPath.push(goBetween.getOriginalComponent());
			context.setCurrentPath(currentPath);
			Transformation thisT = goBetween.getOriginalComponent().getTransformation();
			
			//theLog.log(Level.FINE,"In JOGLPeerComponent render() for "+goBetween.getOriginalComponent().getName());
			if (thisT != null)	{
				if (stackDepth <= MAX_STACK_DEPTH) {
					globalGL.glPushMatrix();
					globalGL.glMultTransposeMatrixd(thisT.getMatrix());
					stackDepth++;
				}
				else 
					globalGL.glLoadTransposeMatrixd(context.getObjectToCamera());	
				currentSignature = thisT.getSignature();
			}  
			// should depend on camera transformation ...
			if (parent != null) cumulativeIsReflection = (isReflection != parent.cumulativeIsReflection);
			else cumulativeIsReflection = (isReflection != globalIsReflection);
			if (cumulativeIsReflection != openGLState.flipped)	{
				globalGL.glFrontFace(cumulativeIsReflection ? GL.GL_CW : GL.GL_CCW);
				openGLState.flipped  = cumulativeIsReflection;
			}

			if (appearanceChanged)  	propagateAppearanceChanged();
			if (appearanceIsDirty)	updateAppearance();
			context.setEffectiveAppearance(eAp);
			
			// render the geometry
			if (goBetween.getPeerGeometry() != null)	goBetween.getPeerGeometry().render(this);
			
			if (goBetween.getOriginalComponent() instanceof LevelOfDetailComponent)	{
				double d = CameraUtility.getNDCExtent(context.getObjectToNDC());
				double lod = renderingHints.getLevelOfDetail();
				((LevelOfDetailComponent)goBetween.getOriginalComponent()).setScreenExtent(d * lod);
			}
			synchronized(childLock)	{
				// render the children
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
			if (parent == null)	{
				if (eAp == null) eAp = EffectiveAppearance.create();
				if (goBetween.getOriginalComponent().getAppearance() != null )	
					eAp = eAp.create(goBetween.getOriginalComponent().getAppearance());
				// TODO figure out why I put this here in the first place
				//else eAp = eAp.create(new Appearance());				
			} else {
				if ( parent.eAp == null)	{
					theLog.log(Level.WARNING,"No effective appearance in parent "+parent.getName());
					return;
				}
				if (goBetween.getOriginalComponent().getAppearance() != null )	
					eAp = parent.eAp.create(goBetween.getOriginalComponent().getAppearance());
				else eAp = parent.eAp;				
			}
			geometryShader = DefaultGeometryShader.createFromEffectiveAppearance(eAp, "");
			//theLog.log(Level.FINE,"component "+goBetween.getOriginalComponent().getName()+" vertex draw is "+geometryShader.isVertexDraw());
			renderingHints = RenderingHintsShader.createFromEffectiveAppearance(eAp, "");
			appearanceIsDirty = false;
		}
		
		public void ancestorAttached(SceneHierarchyEvent ev) {
		}
		
		public void ancestorDetached(SceneHierarchyEvent ev) {
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

		public void childAdded(SceneContainerEvent ev) {
			//theLog.log(Level.FINE,"Container Child added to: "+goBetween.getOriginalComponent().getName());
			//theLog.log(Level.FINE,"Event is: "+ev.toString());
			switch (ev.getChildType() )	{
				case SceneContainerEvent.CHILD_TYPE_COMPONENT:
					SceneGraphComponent sgc = (SceneGraphComponent) ev.getNewChildElement();
					JOGLPeerComponent pc = globalHandle.constructPeerForSceneGraphComponent(sgc, this);
					synchronized(childLock)	{
				    //theLog.log(Level.FINE,"Before adding child count is "+children.size());
						children.add(pc);						
					  
					//theLog.log(Level.FINE,"After adding child count is "+children.size());
					}
					setIndexOfChildren();
					break;
				case SceneContainerEvent.CHILD_TYPE_APPEARANCE:
					int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
					propagateGeometryChanged(changed);	
					appearanceChanged = true;
					theLog.log(Level.FINE,"Propagating geometry change due to added appearance");
					break;				
				case SceneContainerEvent.CHILD_TYPE_LIGHT:
					lightListDirty = true;
					break;
				case SceneContainerEvent.CHILD_TYPE_TRANSFORMATION:
					updateTransformationInfo();
					break;
				default:
					theLog.log(Level.INFO,"Taking no action for addition of child type "+ev.getChildType());
					break;
			}
		}
		
		public void childRemoved(SceneContainerEvent ev) {
			//theLog.log(Level.FINE,"Container Child removed from: "+goBetween.getOriginalComponent().getName());
			switch (ev.getChildType() )	{
				case SceneContainerEvent.CHILD_TYPE_COMPONENT:
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
					break;
				case SceneContainerEvent.CHILD_TYPE_APPEARANCE:
					int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
					propagateGeometryChanged(changed);	
					appearanceChanged = true;
					theLog.log(Level.INFO,"Propagating geometry change due to removed appearance");
					break;				
				case SceneContainerEvent.CHILD_TYPE_LIGHT:
					lightListDirty = true;
					break;
				case SceneContainerEvent.CHILD_TYPE_TRANSFORMATION:
					updateTransformationInfo();
					break;			
				default:
					theLog.log(Level.INFO,"Taking no action for removal of child type "+ev.getChildType());
					break;
		}
		}
		
		public void childReplaced(SceneContainerEvent ev) {
			//theLog.log(Level.FINE,"Container Child replaced at: "+goBetween.getOriginalComponent().getName());
			switch(ev.getChildType())	{
				case SceneContainerEvent.CHILD_TYPE_APPEARANCE:
					int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
					propagateGeometryChanged(changed);	
					appearanceChanged = true;
					theLog.log(Level.INFO,"Propagating geometry change due to replaced appearance");
					break;
				case SceneContainerEvent.CHILD_TYPE_LIGHT:
					lightListDirty = true;
					break;
				case SceneContainerEvent.CHILD_TYPE_TRANSFORMATION:
					updateTransformationInfo();
					break;
				default:
					theLog.log(Level.INFO,"Taking no action for replacement of child type "+ev.getChildType());
					break;
			}
		}
		
		public void childAdded(SceneHierarchyEvent ev) {
			theLog.log(Level.INFO,"Hierarchy Child added");
		}
		
		public void childRemoved(SceneHierarchyEvent ev) {
			theLog.log(Level.INFO,"Hierarchy Child removed");
		}
		
		public void childReplaced(SceneHierarchyEvent ev) {
			theLog.log(Level.INFO,"Hierarchy Child replaced");
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
				isReflection = goBetween.getOriginalComponent().getTransformation().getIsReflection();
			} else {
				determinant  = 0.0;
				isReflection = false;
			}
		}

		public void appearanceChanged(AppearanceEvent ev) {
			//theLog.log(Level.FINE,"Appearance change "+goBetween.getOriginalComponent().getName());
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
			if (goBetween != null && goBetween.getPeerGeometry() != null) goBetween.getPeerGeometry().geometryChanged(changed);
			synchronized(childLock)	{
				int n = children.size();
				for (int i = 0; i<n; ++i)	{		
					JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);
					child.propagateGeometryChanged(changed);
				}				
			}
		}

		private void setDisplayListDirty(boolean b)	{
			propagateGeometryChanged(POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
		}
		
		public SceneGraphComponent getOriginalComponent() {
			return goBetween.getOriginalComponent();
		}
	}
	/**
	 * @return
	 */
	public GLCanvas getCanvas()	{
		return theCanvas;
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
		return ((double) getWidth()/getHeight());
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
