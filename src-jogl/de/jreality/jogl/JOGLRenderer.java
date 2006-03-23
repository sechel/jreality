/*
 * Created on Nov 25, 2004
 *
  */
package de.jreality.jogl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
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
import net.java.games.jogl.GLPbuffer;
import net.java.games.jogl.GLU;
import net.java.games.jogl.util.BufferUtils;
import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.pick.Graphics3D;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.jogl.pick.PickPoint;
import de.jreality.jogl.shader.DefaultGeometryShader;
import de.jreality.jogl.shader.DefaultVertexShader;
import de.jreality.jogl.shader.RenderingHintsShader;
import de.jreality.jogl.shader.Texture2DLoaderJOGL;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Lock;
import de.jreality.scene.PointSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.scene.event.SceneGraphComponentEvent;
import de.jreality.scene.event.SceneGraphComponentListener;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.CameraUtility;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphUtility;
/**
 * @author gunn
 *
 */
public class JOGLRenderer  implements AppearanceListener {

	final static Logger theLog = JOGLConfiguration.theLog;
	final public boolean debugGL = JOGLConfiguration.debugGL;
	
	static boolean collectFrameRate = true;
	public final static int MAX_STACK_DEPTH = 28;
	protected int stackDepth;

	JOGLRenderer globalHandle = null;
	GLDrawable theCanvas;
	SceneGraphPath currentPath = new SceneGraphPath();
	
	public Viewer theViewer;
	SceneGraphComponent theRoot, auxiliaryRoot;
	JOGLPeerComponent thePeerRoot = null;
	JOGLPeerComponent thePeerAuxilliaryRoot = null;
	public JOGLRendererHelper helper;
	public JOGLRenderingState openGLState;

	int width, height;		// GLDrawable.getSize() isnt' implemented for GLPBuffer!
	int whichEye = CameraUtility.MIDDLE_EYE;
	int[] currentViewport = new int[4];
	public Graphics3D context;
	public GL globalGL;
	GLU globalGLU;
	public  boolean texResident = true;
	int numberTries = 0;		// how many times we have tried to make textures resident
	boolean  //useDisplayLists = true, 
		manyDisplayLists = false, forceResidentTextures = true;
	private boolean globalIsReflection = false;
	public int currentSignature = Pn.EUCLIDEAN;

	// pick-related stuff
	public boolean pickMode = false;
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

	public JOGLRenderer(Viewer viewer) {
		this(viewer, viewer.canvas);
		javax.swing.Timer followTimer = new javax.swing.Timer(1000, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {updateGeometryHashtable(); } } );
		followTimer.start();
	}

	public JOGLRenderer(Viewer viewer, GLDrawable d) {
		super();
		theViewer = viewer;
		theCanvas = d;

		globalHandle = this;
		helper = new JOGLRendererHelper(this);
		setAuxiliaryRoot(viewer.getAuxiliaryRoot());		

	}

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
//		if (obj instanceof Boolean) thePeerRoot.useDisplayLists = ((Boolean)obj).booleanValue();
		obj = ap.getAttribute(CommonAttributes.CLEAR_COLOR_BUFFER, Boolean.class);		// assume the best ...
		if (obj instanceof Boolean) openGLState.clearColorBuffer = ((Boolean)obj).booleanValue();
		theLog.fine("forceResTex = "+forceResidentTextures);
		theLog.fine("many display lists = "+manyDisplayLists);
//		theLog.fine(" any display lists = "+thePeerRoot.useDisplayLists);
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

	public Object render() {
		openGLState.setCurrentPickMode(pickMode);

		if (thePeerRoot == null || theViewer.getSceneRoot() != thePeerRoot.getOriginalComponent())	{
			if (thePeerRoot != null) thePeerRoot.dispose();
			setSceneRoot(theViewer.getSceneRoot());
		}
		context  = new Graphics3D(theViewer.getCameraPath(), null, CameraUtility.getAspectRatio(theViewer));
		//theLog.finer(" top level display lists = "+thePeerRoot.useDisplayLists);
		
		globalGL.glMatrixMode(GL.GL_PROJECTION);
		globalGL.glLoadIdentity();

		if (!pickMode)
			helper.handleBackground( width, height, theRoot.getAppearance());
		if (pickMode)
			globalGL.glMultTransposeMatrixd(pickT.getMatrix());

		theCanvas.setAutoSwapBufferMode(!pickMode);
		double aspectRatio = getAspectRatio();
		// for pick mode the aspect ratio has to be set to that of the viewer component
		if (pickMode) aspectRatio = CameraUtility.getAspectRatio(theViewer);
		// load the camera transformation
		double[] c2ndc = CameraUtility.getCameraToNDC(CameraUtility.getCamera(theViewer), 
				aspectRatio,
				whichEye);
		globalGL.glMultTransposeMatrixd(c2ndc);

		
		// prepare for rendering the geometry
		globalGL.glMatrixMode(GL.GL_MODELVIEW);
		globalGL.glLoadIdentity();

		if (backSphere) {  globalGL.glLoadTransposeMatrixd(p3involution);	globalGL.glPushMatrix(); }
		double[] w2c = context.getWorldToCamera();
		globalGL.glLoadTransposeMatrixd(w2c);
		globalIsReflection = (theViewer.isFlipped() != (Rn.determinant(w2c) < 0.0));

		if (theRoot.getAppearance() != null) 
			helper.handleSkyBox(theCanvas, theRoot.getAppearance(), globalHandle);
		
		if (!pickMode) processLights();
		
		processClippingPlanes();
		
		nodeCount = 0;			// for profiling info
		texResident=true;
		currentPath.clear();
		thePeerRoot.render();		
		if (!pickMode && thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.render();
		if (backSphere) globalGL.glPopMatrix();
		globalGL.glLoadIdentity();
		if (forceResidentTextures) forceResidentTextures();
		
		lightListDirty = false;
		return null;
	}
	
	protected JOGLPeerComponent constructPeerForSceneGraphComponent(final SceneGraphComponent sgc, final JOGLPeerComponent p) {
		if (sgc == null) return null;
		final JOGLPeerComponent[] peer = new JOGLPeerComponent[1];
		ConstructPeerGraphVisitor constructPeer = new ConstructPeerGraphVisitor( sgc, p);
		peer[0] = (JOGLPeerComponent) constructPeer.visit();
		return peer[0];
	}

	private void processClippingPlanes() {
		List clipPlanes = null;
		if (clipPlanes == null)	{
			clipPlanes = SceneGraphUtility.collectClippingPlanes(theRoot);
		}
		helper.processClippingPlanes(globalGL, clipPlanes);
	}

	List lights = null;
	private void processLights( ) {
		if (lights == null || lights.size() == 0 || lightListDirty) {
			lights = SceneGraphUtility.collectLights(theRoot);
			helper.resetLights(globalGL, lights);
			lightListDirty = false;
			openGLState.numLights = lights.size();
		}
		helper.processLights(globalGL, lights);
	}

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


	private void forceNewDisplayLists() {
		if (thePeerRoot != null) thePeerRoot.setDisplayListDirty();
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.setDisplayListDirty();
	}


	int frameCount = 0;
	long[] history = new long[20];
	long[] clockTime = new long[20];
	
	public void setSize(int w, int h)	{
		width = w; height = h;
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
	
	private void myglViewport(int lx, int ly, int rx, int ry)	{
		globalGL.glViewport(lx, ly, rx, ry);
		currentViewport[0] = lx;
		currentViewport[1] = ly;
		currentViewport[2] = rx;
		currentViewport[3] = ry;
	}

	/* (non-Javadoc)
	 * @see net.java.games.jogl.GLEventListener#init(net.java.games.jogl.GLDrawable)
	 */
	public void init(GLDrawable drawable) {
		if (debugGL) {
			drawable.setGL(new DebugGL(drawable.getGL()));
		}

		openGLState = new JOGLRenderingState(this);
		theCanvas = drawable;
		if (!(theCanvas instanceof GLPbuffer))	{  // workaround in bug in implementation of GLPbuffer
			width = theCanvas.getSize().width;
			height = theCanvas.getSize().height;
		}

		globalGL = theCanvas.getGL();
		globalGLU = theCanvas.getGLU();

		if (debugGL)	{
			String vv = globalGL.glGetString(GL.GL_VERSION);
			theLog.log(Level.INFO,"version: "+vv);			
			int[] tu = new int[1];
			globalGL.glGetIntegerv(GL.GL_MAX_TEXTURE_UNITS, tu);
			theLog.info("# of texture units: "+tu[0]);			
		}
		

		if (thePeerRoot != null) thePeerRoot.propagateGeometryChanged(ALL_CHANGED);
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.propagateGeometryChanged(ALL_CHANGED);
		Texture2DLoaderJOGL.deleteAllTextures(globalGL);
		if (debugGL)	theLog.log(Level.INFO,"Got new sphere display lists for context "+globalGL);
}
	public void display(GLDrawable drawable) {
		if (theViewer.getSceneRoot() == null || theViewer.getCameraPath() == null) {
			LoggingSystem.getLogger(this).info("display called w/o scene root or camera path");
		}
	  
		openGLState.initializeGLState();
      
		long beginTime = 0;
		if (collectFrameRate) beginTime = System.currentTimeMillis();
		Camera theCamera = CameraUtility.getCamera(theViewer);
		clearColorBits = (openGLState.clearColorBuffer ? GL.GL_COLOR_BUFFER_BIT : 0);
		if (theCamera.isStereo())		{
			setupRightEye();
			render();
			setupLeftEye();
			render();
	        globalGL.glColorMask(true, true, true, true);
		} 
		else {
			globalGL.glClear (clearColorBits | GL.GL_DEPTH_BUFFER_BIT);
			myglViewport(0,0,width, height);
			whichEye=CameraUtility.MIDDLE_EYE;
			if (!pickMode)	{
				 	render();
			}
			else		{
				myglViewport(0,0, 2,2);
				IntBuffer selectBuffer = BufferUtils.newIntBuffer(bufsize);
				//JOGLConfiguration.theLog.log(Level.INFO,"Picking "+frameCount);
				double[] pp3 = new double[3];
				pp3[0] = -pickScale * pickPoint[0]; pp3[1] = -pickScale * pickPoint[1]; pp3[2] = 0.0;
				MatrixBuilder.euclidean().translate(pp3).scale(pickScale, pickScale, 1.0).assignTo(pickT);
				thePeerRoot.propagateGeometryChanged(POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
				globalGL.glSelectBuffer(bufsize, selectBuffer);		
				globalGL.glRenderMode(GL.GL_SELECT);
				globalGL.glInitNames();
				globalGL.glPushName(0);
				render();
				pickMode = false;
				thePeerRoot.propagateGeometryChanged(POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
				int numberHits = globalGL.glRenderMode(GL.GL_RENDER);
				hits = JOGLPickAction.processOpenGLSelectionBuffer(numberHits, selectBuffer, pickPoint,theViewer);
				display(drawable);
			}
		}
		if (screenShot)	{
			if (theCanvas instanceof GLCanvas) helper.saveScreenShot((GLCanvas) theCanvas, screenShotFile);
			else JOGLConfiguration.theLog.log(Level.WARNING, "Can't find the size of class "+theCanvas.getClass());
		}
		
		if (collectFrameRate)	{
			++frameCount;
			int j = (frameCount % 20);
			clockTime[j] = beginTime;
			history[j]  =  System.currentTimeMillis() - beginTime;
		}
	}

// Following code seems to have NO effect: An attempt to render the "back banana"
//	if (theViewer.getSignature() == Pn.ELLIPTIC )	{
//		if (useDisplayLists)	{		// debug purposes
//			backSphere = true;
//			visit();						
//		}
//		backSphere = false;
//		visit();
//	}
//	 else 

	private void setupRightEye() {
		int which = theViewer.getStereoType();
		switch(which)	{
		case Viewer.CROSS_EYED_STEREO:
			globalGL.glClear (clearColorBits | GL.GL_DEPTH_BUFFER_BIT);
			int w = width/2;
			int h = height;
			myglViewport(0,0, w,h);
			break;
		
		case Viewer.RED_BLUE_STEREO:
		case Viewer.RED_CYAN_STEREO:
		case Viewer.RED_GREEN_STEREO:
			myglViewport(0,0, width, height);
			globalGL.glClear (clearColorBits | GL.GL_DEPTH_BUFFER_BIT);
	        if (which == Viewer.RED_GREEN_STEREO) globalGL.glColorMask(false, true, false, true);
	        else if (which == Viewer.RED_BLUE_STEREO) globalGL.glColorMask(false, false, true, true);
	        else if (which == Viewer.RED_CYAN_STEREO) globalGL.glColorMask(false, true, true, true);
			break;
			
		case Viewer.HARDWARE_BUFFER_STEREO:
			myglViewport(0,0, width, height);
			globalGL.glClear (clearColorBits | GL.GL_DEPTH_BUFFER_BIT);
			globalGL.glDrawBuffer(GL.GL_BACK_RIGHT);
			break;			
		}
		whichEye=CameraUtility.RIGHT_EYE;
	}

	private void setupLeftEye() {
		int which = theViewer.getStereoType();
		switch(which)	{
		case Viewer.CROSS_EYED_STEREO:
			int w = width/2;
			int h = height;
			myglViewport(w, 0, w,h);
			break;
			
		case Viewer.RED_BLUE_STEREO:
		case Viewer.RED_CYAN_STEREO:
		case Viewer.RED_GREEN_STEREO:
	       globalGL.glColorMask(true, false, false, true);
			globalGL.glClear (GL.GL_DEPTH_BUFFER_BIT);
			break;
			
		case Viewer.HARDWARE_BUFFER_STEREO:
			globalGL.glDrawBuffer(GL.GL_BACK_LEFT);
			globalGL.glClear (clearColorBits | GL.GL_DEPTH_BUFFER_BIT);
			break;
		}
		whichEye=CameraUtility.LEFT_EYE;
	}

	public void displayChanged(GLDrawable arg0, boolean arg1, boolean arg2) {
	}

	public void reshape(GLDrawable arg0,int arg1,int arg2,int arg3,int arg4) {
		width = arg3-arg1;
		height = arg4-arg2;
		myglViewport(0,0, width, height);
	}

    private final static int POINTS_CHANGED = 1;
    private final static int LINES_CHANGED = 2;
    private final static int FACES_CHANGED = 4;
    private final static int ALL_CHANGED = 7;
	
	protected class JOGLPeerNode	{
		String name;
		
		public String getName()	{
			return name;
		}
		
		public void setName(String n)	{
			name = n;
		}
	}

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
	
	
	protected class JOGLPeerGeometry extends JOGLPeerNode	implements GeometryListener{
		Geometry originalGeometry;
		Geometry[] tubeGeometry, proxyPolygonGeometry;
		Vector proxyGeometry;
		IndexedFaceSet ifs;
		IndexedLineSet ils;
		PointSet ps;
		int refCount = 0;
		int signature = Pn.EUCLIDEAN;
		boolean isSurface = false;
		
		protected JOGLPeerGeometry(Geometry g)	{
			super();
			originalGeometry = g;
			name = "JOGLPeer:"+g.getName();
			ifs = null; ils = null; ps = null;
			if (g instanceof IndexedFaceSet) ifs = (IndexedFaceSet) g;
			if (g instanceof IndexedLineSet) ils = (IndexedLineSet) g;
			if (g instanceof PointSet) ps = (PointSet) g;
			originalGeometry.addGeometryListener(this);
			if (ifs != null || g instanceof Sphere || g instanceof Cylinder) isSurface = true;
		}
		
		public void dispose()		{
			refCount--;
			if (refCount < 0)	{
				JOGLConfiguration.theLog.log(Level.WARNING,"Negative reference count!");
			}
			if (refCount == 0)	{
				JOGLConfiguration.theLog.log(Level.FINER,"Geometry is no longer referenced");
				originalGeometry.removeGeometryListener(this);
				geometries.remove(originalGeometry);
			}
		}
		
		public void render(JOGLPeerComponent jpc) {
			RenderingHintsShader renderingHints = jpc.renderingHints;
			DefaultGeometryShader geometryShader = jpc.geometryShader;
			openGLState.setUseDisplayLists(renderingHints.isUseDisplayLists()); //(); //useDisplayLists(activeDL, jpc);
			openGLState.setCurrentGeometry(originalGeometry);
			openGLState.setCurrentSignature(signature);
			renderingHints.render(openGLState);
			theLog.fine("Rendering sgc "+jpc.getOriginalComponent().getName());
			theLog.fine("vertex:edge:face:"+geometryShader.isVertexDraw()+geometryShader.isEdgeDraw()+geometryShader.isFaceDraw());
			if (geometryShader.isEdgeDraw() && ils != null)	{
				geometryShader.lineShader.render(openGLState);
				geometryShader.lineShader.postRender(openGLState);
			}
			if (geometryShader.isVertexDraw() && ps != null)	{
				geometryShader.pointShader.render(openGLState);
				geometryShader.pointShader.postRender(openGLState);
			}
			renderingHints.render(openGLState);
			if (geometryShader.isFaceDraw() && isSurface) {
				geometryShader.polygonShader.render(openGLState);
				geometryShader.polygonShader.postRender(openGLState);
			}	
			if (geometryShader.isVertexDraw() && ps!=null && ps.getVertexAttributes(Attribute.LABELS) != null) {
				helper.drawPointLabels(ps,  jpc.geometryShader.pointShader.getTextShader());
			}
		    if (geometryShader.isEdgeDraw() &&ils != null && ils.getEdgeAttributes(Attribute.LABELS) != null) {
		        helper.drawEdgeLabels(ils, jpc.geometryShader.lineShader.getTextShader());
		       }
		    if (geometryShader.isFaceDraw() &&ifs != null && ifs.getFaceAttributes(Attribute.LABELS) != null) {
		    	helper.drawFaceLabels(ifs,  jpc.geometryShader.polygonShader.getTextShader());
		    }
			renderingHints.postRender(openGLState);
		}

		public void geometryChanged(GeometryEvent ev) {
			if (ev.getChangedGeometryAttributes().size() > 0)	{
				Object foo = originalGeometry.getGeometryAttributes(GeometryUtility.SIGNATURE);
				if (foo != null) {
					Integer foo2 = (Integer) foo;
					signature = foo2.intValue();
				}				
			}
			
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
	
	protected class GoBetween implements GeometryListener, TransformationListener, AppearanceListener,SceneGraphComponentListener	{
		SceneGraphComponent originalComponent;
		ArrayList peers = new ArrayList();
		JOGLPeerGeometry peerGeometry;
		Lock peersLock = new Lock();

		protected GoBetween(SceneGraphComponent sgc)	{
			super();
			originalComponent = sgc;
			if (originalComponent.getGeometry() != null)  {
				peerGeometry = getJOGLPeerGeometryFor(originalComponent.getGeometry());
				peerGeometry.refCount++;
				originalComponent.getGeometry().addGeometryListener(this);
			} else peerGeometry = null;
			originalComponent.addSceneGraphComponentListener(this);
			if (originalComponent.getAppearance() != null) 
				//originalComponent.getAppearance().removeAppearanceListener(this);				
				originalComponent.getAppearance().addAppearanceListener(this);				
		}
		
		public void dispose()	{
			originalComponent.removeSceneGraphComponentListener(this);
			if (originalComponent.getAppearance() != null) originalComponent.getAppearance().removeAppearanceListener(this);
			if (peerGeometry != null)		{
				originalComponent.getGeometry().removeGeometryListener(this);
				peerGeometry.dispose();
			}
		}
		
		
		public void addJOGLPeer(JOGLPeerComponent jpc)	{
			if (peers.contains(jpc)) return;
			peersLock.writeLock();
			peers.add(jpc);
			peersLock.writeUnlock();
		}
		
		public void removeJOGLPeer(JOGLPeerComponent jpc)	{
			if (!peers.contains(jpc)) return;
			peersLock.writeLock();
			peers.remove(jpc);
			peersLock.writeUnlock();
	
			if (peers.size() == 0)	{
				theLog.log(Level.FINE,"GoBetween for "+originalComponent.getName()+" has no peers left");
				goBetweenTable.remove(originalComponent);
				dispose();
			}
		}
		
		public JOGLPeerGeometry getPeerGeometry() {
			return peerGeometry;
		}
		
		public void geometryChanged(GeometryEvent ev) {
			peersLock.readLock();
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.setDisplayListDirty();
			}
			peersLock.readUnlock();
		}

		public void transformationMatrixChanged(TransformationEvent ev) {
			peersLock.readLock();
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.transformationMatrixChanged(ev);
			}
			peersLock.readUnlock();
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
				
			peersLock.readLock();
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				if (propagates) peer.appearanceChanged(ev);
				if (changed != 0) peer.propagateGeometryChanged(changed);
			}
			peersLock.readUnlock();
			//theLog.log(Level.FINER,"setting display list dirty flag: "+changed);
		}
		public void childAdded(SceneGraphComponentEvent ev) {
			if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY) {
				if (peerGeometry != null)	{
					((Geometry) ev.getOldChildElement()).removeGeometryListener(this);						
					peerGeometry.dispose();
					geometryRemoved = true;
					theLog.log(Level.WARNING, "Adding geometry while old one still valid");
					peerGeometry=null;
				}
				if (originalComponent.getGeometry() != null)  {
					peerGeometry = getJOGLPeerGeometryFor(originalComponent.getGeometry());
					originalComponent.getGeometry().addGeometryListener(this);
					peerGeometry.refCount++;
				} 
			}
			peersLock.readLock();
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childAdded(ev);
			}
			peersLock.readUnlock();
		}
		public void childRemoved(SceneGraphComponentEvent ev) {
			if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY) {
				if (peerGeometry != null) {
					((Geometry) ev.getOldChildElement()).removeGeometryListener(this);						
					peerGeometry.dispose();		// really decreases reference count
					peerGeometry = null;
					geometryRemoved = true;
				}
//				return;
			}
//			boolean apAdded = (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE);
//			int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
			peersLock.readLock();
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childRemoved(ev);
				// why isn't the following done in peer.childRemoved?
//				if (apAdded) peer.propagateGeometryChanged(changed);
			}
			peersLock.readUnlock();
		}
		
		public void childReplaced(SceneGraphComponentEvent ev) {
			if  (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY) {
				if (peerGeometry != null && peerGeometry.originalGeometry == originalComponent.getGeometry()) return;		// no change, really
				if (peerGeometry != null) {
					((Geometry) ev.getOldChildElement()).removeGeometryListener(this);						
					peerGeometry.dispose();
					geometryRemoved=true;
					peerGeometry = null;
				}
				if (originalComponent.getGeometry() != null)  {
					originalComponent.getGeometry().addGeometryListener(this);
					peerGeometry = getJOGLPeerGeometryFor(originalComponent.getGeometry());
					peerGeometry.refCount++;
				} 
//					return;
			}
//			boolean apAdded = (ev.getChildType() ==  SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE);
//			int changed = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
			peersLock.readLock();
			Iterator iter = peers.iterator();
			while (iter.hasNext())	{
				JOGLPeerComponent peer = (JOGLPeerComponent) iter.next();
				peer.childReplaced(ev);
//				if (apAdded) peer.propagateGeometryChanged(changed);
				}				
			peersLock.readUnlock();
		}
		
		public SceneGraphComponent getOriginalComponent() {
			return originalComponent;
		}

		public void visibilityChanged(SceneGraphComponentEvent ev) {
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
		
		boolean isReflection = false, cumulativeIsReflection = false;
		double determinant = 0.0;
		
		RenderingHintsShader renderingHints;
		DefaultGeometryShader geometryShader;

		Object childLock = new Object();		
		Runnable renderGeometry = null;
		final JOGLPeerComponent self = this;
		
		boolean appearanceChanged = true,
			geometryIsDirty = true,
			boundIsDirty = true,
			flushCachedInfo  = false;

		double[] tform = new double[16];		// for optimized access to matrix
		public JOGLPeerComponent(SceneGraphPath sgp, JOGLPeerComponent p)		{
			super();
			if (sgp == null || !(sgp.getLastElement() instanceof SceneGraphComponent))  {
				throw new IllegalArgumentException("Not a valid SceneGraphComponenet");
			}
			goBetween = goBetweenFor(sgp.getLastComponent());
			goBetween.addJOGLPeer(this);
			name = "JOGLPeer:"+goBetween.getOriginalComponent().getName();
			children = new Vector();		// always have a child list, even if it's empty
			parent = p;
			updateTransformationInfo();
			updateRenderRunnable();
		}
		
		private void updateRenderRunnable() {
			flushCachedInfo();
			updateShaders();
			if (goBetween.peerGeometry == null) renderGeometry = null;
			else	renderGeometry = new Runnable() {
				public void run() {
					goBetween.peerGeometry.render(self);
				}
			};
		}

		public void dispose()	{
				int n = children.size();
				for (int i = n-1; i>=0; --i)	{
					JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);
					child.dispose();
				}	
				goBetween.removeJOGLPeer(this);
		}

		public void render()		{
			if (!goBetween.getOriginalComponent().isVisible()) return;
						
			nodeCount++;
			currentPath.push(goBetween.getOriginalComponent());
			context.setCurrentPath(currentPath);
			Transformation thisT = goBetween.getOriginalComponent().getTransformation();
			
			if (thisT != null)	{
				if ( stackDepth <= MAX_STACK_DEPTH) {
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
			if (flushCachedInfo)		flushCachedInfo();
			if (goBetween.getPeerGeometry() != null)	{
				Scene.executeReader(goBetween.peerGeometry.originalGeometry, renderGeometry );
			}
			
			int n = children.size();
			for (int i = 0; i<n; ++i)	{		
				JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);					
				if (pickMode)	globalGL.glPushName(JOGLPickAction.SGCOMP_BASE+child.childIndex);
				child.render();
				if (pickMode)	globalGL.glPopName();
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
					for (int i = 0; i<n; ++i)	{
						SceneGraphComponent sgc = goBetween.getOriginalComponent().getChildComponent(i);
						JOGLPeerComponent jpc = getPeerForChildComponent(sgc);
						if (jpc == null)	{
							theLog.log(Level.WARNING,"No peer for sgc "+sgc.getName());
							jpc.childIndex = -1;
						} else jpc.childIndex = i;
					}									
			}
		}

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

		public void appearanceChanged(AppearanceEvent ev) {
			appearanceChanged = true;
		}
		
		private void propagateAppearanceChanged()	{
			Appearance thisAp = goBetween.getOriginalComponent().getAppearance(); 
			if (parent == null)	{
				if (eAp == null) {
					eAp = EffectiveAppearance.create();
					if (goBetween.getOriginalComponent().getAppearance() != null )	
						eAp = eAp.create(goBetween.getOriginalComponent().getAppearance());
				}
			} else {
				if ( parent.eAp == null)	{
					//throw new IllegalStateException("Parent must have effective appearance");
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
			updateShaders();
			int n = children.size();
			for (int i = 0; i<n; ++i)	{		
				JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);
				child.propagateAppearanceChanged();
			}	
			appearanceChanged=false;
		}

		/**
		 * @param thisAp
		 */
		private void updateShaders() {
//			 can happen that the effective appearance isn't initialized yet; skip
			if (eAp == null) return;
			Appearance thisAp = goBetween.getOriginalComponent().getAppearance(); 
			if (thisAp == null && goBetween.getOriginalComponent().getGeometry() == null && parent != null)	{
				geometryShader = parent.geometryShader;
				renderingHints = parent.renderingHints;
			
			} else  {		
				if (geometryShader == null)
					geometryShader = DefaultGeometryShader.createFromEffectiveAppearance(eAp, "");
				else 
					geometryShader.setFromEffectiveAppearance(eAp, "");
				
				if (renderingHints == null)
					renderingHints = RenderingHintsShader.createFromEffectiveAppearance(eAp, "");
				else
					renderingHints.setFromEffectiveAppearance(eAp, "");								
			}
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
					theLog.log(Level.INFO,"Propagating geometry change due to replaced appearance");
					appearanceChanged = true;
					if (this == thePeerRoot) extractGlobalParameters();
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

		public void propagateGeometryChanged(int changed) {
//			if (goBetween != null && goBetween.getPeerGeometry() != null) 
//				goBetween.getPeerGeometry().geometryChanged(changed);
//			dlInfo.geometryChanged(changed);
			geometryChanged(changed);
			synchronized(childLock)	{
				int n = children.size();
				for (int i = 0; i<n; ++i)	{		
					JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);
					child.propagateGeometryChanged(changed);
				}				
			}
		}

		private void geometryChanged(int changed) {
			if (geometryShader != null)	{
				if (geometryShader.pointShader != null && (changed & POINTS_CHANGED) != 0) geometryShader.pointShader.flushCachedState(globalHandle);
				if (geometryShader.lineShader != null && (changed & LINES_CHANGED) != 0) geometryShader.lineShader.flushCachedState(globalHandle);
				if (geometryShader.polygonShader != null && (changed & FACES_CHANGED) != 0) geometryShader.polygonShader.flushCachedState(globalHandle);				
			}
		}

		private void setDisplayListDirty()	{
			flushCachedInfo = true;
		}

		private void flushCachedInfo() {
			geometryChanged(POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
			flushCachedInfo = false;
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

	File screenShotFile = null;
	private int clearColorBits;
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

	public double getAspectRatio() {
		return ((double) currentViewport[2])/currentViewport[3];
	}
}
