/*
 * Created on Jan 8, 2004
 *
 * TODO figure out stencil drawing 
 * */
package de.jreality.jogl;

import java.awt.Component;
import java.awt.EventQueue;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.java.games.jogl.DebugGL;
import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLU;
import net.java.games.jogl.util.BufferUtils;
import de.jreality.geometry.SphereHelper;
import de.jreality.jogl.pick.JOGLPickRequestor;
import de.jreality.jogl.pick.PickAction;
import de.jreality.jogl.pick.PickPoint;
import de.jreality.jogl.shader.AbstractJOGLShader;
import de.jreality.jogl.shader.DefaultGeometryShader;
import de.jreality.jogl.shader.DefaultPolygonShader;
import de.jreality.jogl.shader.DefaultVertexShader;
import de.jreality.jogl.shader.RenderingHintsShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.Graphics3D;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.util.CameraUtility;
import de.jreality.util.ClippingPlaneCollector;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.LightCollector;
import de.jreality.util.Pn;
import de.jreality.util.Rn;
/**
 * TODO implement  isVisible   bit in SceneGraphNode
 * TODO implement collectAncestorVisitor (see method geometryChanged() at end of file )
 * @author gunn
 *
 */
public class JOGLRenderer  extends SceneGraphVisitor implements JOGLRendererInterface, GeometryListener, AppearanceListener  {
	final static Logger theLog;
	static boolean debugGL = false;
	static {
		theLog	= Logger.getLogger("de.jreality.jogl");
		theLog.setLevel(Level.FINEST);
		String foo = System.getProperty("jreality.jogl.debugGL");
		if (foo != null) { if (foo.equals("false")) debugGL = false; else debugGL =true;}
		theLog.setLevel(Level.FINEST);
	}
	public final static int MAX_STACK_DEPTH = 30;
	protected int stackDepth;
	/*
	 * 	 These fields are used at each "level" of the traversal.
	 * They are in essence stacked by each visit to a Component node 
	 * (see pushContext() method)
	 */
	protected Appearance currentAppearance;
	protected RenderingHintsShader renderingHints = new RenderingHintsShader();
	protected DefaultGeometryShader currentGeometryShader = new DefaultGeometryShader();
	protected EffectiveAppearance eAp;
	protected double[] currentMatrix = new double[16];
	SceneGraphPath currentPath = new SceneGraphPath();
	protected boolean isReflection = false;
	protected boolean shaderUptodate = false;
	protected boolean insidePointSet = false;
	protected boolean insideLineSet = false;
	public GlobalRenderInfo gri;
	protected int whichEye;
	protected int childIndex;
	
	private final double pickScale = 10000.0;
	
	public class GlobalRenderInfo	{
		de.jreality.jogl.Viewer theViewer;
		SceneGraphComponent theRoot, auxiliaryRoot;
		GLCanvas theCanvas;
		Camera theCamera;
		Graphics3D gc;
		GL gl;
		GLU glu;
		public  boolean texResident;
		int numberTries = 0;		// how many times we have tried to make textures resident
		boolean  useDisplayLists, forceNewDisplayLists, insideDisplayList;
		boolean apDirty = false;
		boolean pickMode = false;
		Transformation pickT = new Transformation();
		PickPoint[] hits;
		HashSet registeredAlready = new HashSet();
		Hashtable apTable = new Hashtable();
		Hashtable rhTable = new Hashtable();
		Hashtable dlTable;
		double framerate;
		int lightCount = 0;
		int nodeCount = 0;
		public GlobalRenderInfo()	{
			super();
		}
	}

	
	// register for geometry change events
	private class RegisterForChange extends SceneGraphVisitor	{
		public GeometryListener l;
		public AppearanceListener al;
		public RegisterForChange(GeometryListener who, AppearanceListener awho)	{
			super();
			l = who;
			al = awho;
		}
		public void visit(SceneGraphComponent c) {
			//theLog.log(Level.FINER, "Register change:  Component");
		  	c.childrenAccept(this);
		}

		public void visit(Appearance a) {
			if (gri.registeredAlready.add(a)) 	{
				a.addAppearanceListener(al);
				//theLog.log(Level.FINER, "Adding appearance listener");
			} 
		}

		public void visit(Geometry sg)	{
			if (gri.registeredAlready.add(sg)) 	sg.addGeometryListener(l);
		}
	}
	RegisterForChange rc;
	
	// I'd use clone() but it's not supported by super [SceneGraphVisitor]
	public JOGLRenderer (JOGLRenderer p) {
		super();
		gri = p.gri;
		renderingHints = p.renderingHints;
		currentGeometryShader = p.currentGeometryShader;
		renderingHints = p.renderingHints;
		currentAppearance = p.currentAppearance;
		eAp = p.eAp;
		shaderUptodate = p.shaderUptodate;
		isReflection = p.isReflection;
		//currentMatrix = ((double[]) p.currentMatrix.clone());
		currentPath = p.currentPath;
		shaderUptodate = p.shaderUptodate;
		childIndex = 0;
	}
	
	/**
	 * @param canvas
	 * @param viewer
	 */
	public JOGLRenderer(de.jreality.jogl.Viewer viewer) {
		super();
		gri = new GlobalRenderInfo();
		gri.theViewer = viewer;
		gri.gc  = new Graphics3D(viewer);
		//gri.gc.setCamera(cam);
		gri.theCanvas = (GLCanvas) viewer.getViewingComponent();
		gri.theRoot = viewer.getSceneRoot();
		gri.theCamera = CameraUtility.getCamera(gri.theViewer);
		gri.dlTable = new Hashtable();
		Appearance defaultAp = new Appearance();
		currentGeometryShader.setDefaultValues(defaultAp);
		CommonAttributes.setDefaultValues(defaultAp);
		renderingHints.setDefaultValues(defaultAp);
		//currentMaterialShader.setDefaultValues(defaultAp);
		eAp =  EffectiveAppearance.create();
		eAp =  eAp.create(defaultAp);
		gri.gc.setEffectiveAppearance(eAp);
		gri.useDisplayLists = true;
		Rn.setIdentityMatrix(currentMatrix);
		theLog.log(Level.FINER, "Looked up logger successfully");
		rc = new RegisterForChange( this, this);
		childIndex = 0;
	}


	public  JOGLRenderer pushContext()	{
		return new JOGLRenderer(this);
	}
	
	
	public SceneGraphComponent getAuxiliaryRoot() {
		return gri.auxiliaryRoot;
	}
	public void setAuxiliaryRoot(SceneGraphComponent auxiliaryRoot) {
		gri.auxiliaryRoot = auxiliaryRoot;
	}

	private static final int MAX_DLIST_SIZE = 10000;
	/* (non-Javadoc)
	 * @see de.jreality..SceneGraphVisitor#init()
	 */
	public Object visit() {
		//System.err.println("Initializing Visiting");
		// register for changes
		//TODO this is wasteful: use hierarchy events to keep uptodate with scene graph.
		rc.visit(gri.theRoot);
		if (gri.auxiliaryRoot != null) rc.visit(gri.auxiliaryRoot);
		if (gri.apDirty || !gri.texResident )	{
			gri.apTable.clear();
			gri.rhTable.clear();
			gri.forceNewDisplayLists = true;
			gri.apDirty = false;
		}
		
		gri.gl.glMatrixMode(GL.GL_PROJECTION);
		gri.gl.glLoadIdentity();

		if (!gri.pickMode)
			JOGLRendererHelperNew.handleBackground(gri.theCanvas, gri.theRoot.getAppearance());
		if (gri.pickMode)	{
			gri.gl.glMultTransposeMatrixd(gri.pickT.getMatrix());
		}
		gri.theCanvas.setAutoSwapBufferMode(
				gri.theViewer.autoSwapBuffers&&!gri.pickMode);
		
		// We "inline" the visit to the camera since it cannot be visited in the traversal order
		// load the projection transformation
		gri.gl.glMultTransposeMatrixd(gri.theCamera.getCameraToNDC(whichEye));

		// prepare for rendering the geometry
		gri.gl.glMatrixMode(GL.GL_MODELVIEW);
		gri.gl.glLoadIdentity();
		double[] w2c = gri.gc.getWorldToCamera();
		gri.gl.glLoadTransposeMatrixd(w2c);
		isReflection = (Rn.determinant(w2c) > 0) ? false : true;

		if (!gri.pickMode) processLights();
		processClippingPlanes();
		
		gri.nodeCount = 0;
		gri.texResident = true;		// assume the best ...
		gri.theRoot.accept(this);			
		//System.out.println("Nodes visited in render traversal: "+gri.nodeCount);
		//if (!gri.pickMode)	System.out.println("Rendering");
		//else 	System.out.println("Picking");
		if (!gri.pickMode && gri.auxiliaryRoot != null) gri.auxiliaryRoot.accept(this);
		//if (gri.auxiliaryRoot != null) gri.auxiliaryRoot.accept(this);
		gri.gl.glLoadIdentity();
		gri.forceNewDisplayLists = false;
		
		//if (!gri.pickMode) gri.theCanvas.swapBuffers();
		
		//System.err.println("Display list population: "+gri.dlTable.size());
		if (gri.dlTable.size() > MAX_DLIST_SIZE)	{
			gri.dlTable.clear();
			gri.forceNewDisplayLists = true;
		} 
		forceResidentTextures();
		return null;
	}
	
	/**
	 * 
	 */
	private void forceResidentTextures() {
		// Try to force textures to be resident if they're not already
		if (!gri.texResident && gri.numberTries < 3)	{
			TimerTask rerenderTask = new TimerTask()	{
				public void run()	{
					rerender();
				}
			};
			Timer doIt = new Timer();
			doIt.schedule(rerenderTask, 10);
			gri.forceNewDisplayLists = true;
			System.out.println("Attempting to force textures resident");
			gri.numberTries++;		// don't keep trying indefinitely
		} else gri.numberTries = 0;
	}

	/**
	 * 
	 */
	List lights = null;
	private void processLights() {
		gri.lightCount = GL.GL_LIGHT0;
		
		// collect and process the lights
		// with a peer structure we don't do this but once, and then
		// use event listening to keep our list up-to-date
		// DEBUG: see what happens if we always reuse the light list
		LightCollector lc = new LightCollector(gri.theRoot);
		lights = (List) lc.visit();
		int n = lights.size();
		double[] zDirectiond = {0d,0d,1d,0d};
		double[] origind = {0d,0d,0d,1d};
		for (int i = 0; i<n; ++i)	{
			SceneGraphPath lp = (SceneGraphPath) lights.get(i);
			//System.out.println("Light"+i+": "+lp.toString());
			double[] mat = lp.getMatrix(null);
			double[] mat2 = Rn.identityMatrix(4);
			double[] dir, trans;
			dir = Rn.matrixTimesVector(null, mat, zDirectiond);
			trans = Rn.matrixTimesVector(null, mat,origind);
			Pn.dehomogenize(dir, dir);
			Pn.dehomogenize(trans, trans);
			for (int j=0; j<3; ++j) mat2[4*j+2] = dir[j];
			for (int j=0; j<3; ++j) mat2[4*j+3] = trans[j];
			//System.out.println("Light matrix is: "+Rn.matrixToString(mat));
			gri.gl.glPushMatrix();
			gri.gl.glMultTransposeMatrixd(mat2);
			SceneGraphNode light = lp.getLastElement();
			if (light instanceof SpotLight)		wisit((SpotLight) light);
			else if (light instanceof PointLight)		wisit((PointLight) light);
			else if (light instanceof DirectionalLight)		wisit((DirectionalLight) light);
			else System.out.println("Invalid light class "+light.getClass().toString());
			gri.gl.glPopMatrix();
		}
	}
	
	private static float[] zDirection = {0,0,1,0};
	private static float[] mzDirection = {0,0,1,0};
	private static float[] origin = {0,0,0,1};
	
	public void wisit(Light dl)	{
		  //System.out.println("Visiting directional light");
		  //gri.gl.glLightfv(gri.lightCount, GL.GL_AMBIENT, lightAmbient);
		  gri.gl.glLightfv(gri.lightCount, GL.GL_DIFFUSE, dl.getScaledColorAsFloat());
		  float f = (float) dl.getIntensity();
		  float[] specC = {f,f,f};
		  gri.gl.glLightfv(gri.lightCount, GL.GL_SPECULAR, specC);
		  //gri.gl.glLightfv(gri.lightCount, GL.GL_SPECULAR, white);	
	}
	
	public void wisit(DirectionalLight dl)		{
		  if (gri.lightCount > GL.GL_LIGHT7)	{
		  	System.out.println("Max. # lights exceeded");
		  	return;
		  }
		  wisit( (Light) dl);
		  gri.gl.glLightfv(gri.lightCount, GL.GL_POSITION, zDirection);
		  gri.gl.glEnable(gri.lightCount);
		  gri.lightCount++;
	}
	
	public void wisit(PointLight dl)		{
		  if (gri.lightCount >= GL.GL_LIGHT7)	{
		  	System.out.println("Max. # lights exceeded");
		  	return;
		  }
		  //gri.gl.glLightfv(gri.lightCount, GL.GL_AMBIENT, lightAmbient);
		  wisit((Light) dl);
		  gri.gl.glLightfv(gri.lightCount, GL.GL_POSITION, origin);
		  gri.gl.glLightf(gri.lightCount, GL.GL_CONSTANT_ATTENUATION, (float) dl.getFalloffA0());
		  gri.gl.glLightf(gri.lightCount, GL.GL_LINEAR_ATTENUATION, (float) dl.getFalloffA1());
		  gri.gl.glLightf(gri.lightCount, GL.GL_QUADRATIC_ATTENUATION, (float) dl.getFalloffA2());
		  if (!(dl instanceof SpotLight)) 	{
			  	gri.gl.glEnable(gri.lightCount);
		  		gri.lightCount++;
		  }
	}
	
	public void wisit(SpotLight dl)		{
		  if (gri.lightCount >= GL.GL_LIGHT7)	{
		  	System.out.println("Max. # lights exceeded");
		  	return;
		  }
		  PointLight pl = (PointLight) dl;
		  wisit(pl);
		  gri.gl.glLightf(gri.lightCount, GL.GL_SPOT_CUTOFF, (float) ((180.0/Math.PI) * dl.getConeAngle()));
		  gri.gl.glLightfv(gri.lightCount, GL.GL_SPOT_DIRECTION, mzDirection);
		  gri.gl.glLightf(gri.lightCount, GL.GL_SPOT_EXPONENT, (float) dl.getDistribution());
		  gri.gl.glEnable(gri.lightCount);
		  gri.lightCount++;
	}
	
	static double[] clipPlane = {0d, 0d, -1d, 0d};
	
	public void wisit(ClippingPlane cp)	{
		gri.gl.glClipPlane(GL.GL_CLIP_PLANE0, clipPlane);
		gri.gl.glEnable(GL.GL_CLIP_PLANE0);
	}

	/**
	 * 
	 */
	List clipPlanes = null;
	private void processClippingPlanes() {
		gri.lightCount = GL.GL_CLIP_PLANE0;
		
		// collect and process the lights
		// with a peer structure we don't do this but once, and then
		// use event listening to keep our list up-to-date
		// DEBUG: see what happens if we always reuse the light list
		ClippingPlaneCollector lc = new ClippingPlaneCollector(gri.theRoot);
		clipPlanes = (List) lc.visit();
		int n = clipPlanes.size();
		double[] zDirectiond = {0d,0d,1d,0d};
		double[] origind = {0d,0d,0d,1d};
		for (int i = 0; i<n; ++i)	{
			SceneGraphPath lp = (SceneGraphPath) clipPlanes.get(i);
			//System.out.println("Light"+i+": "+lp.toString());
			double[] mat = lp.getMatrix(null);
			gri.gl.glPushMatrix();
			gri.gl.glMultTransposeMatrixd(mat);
			SceneGraphNode cp = lp.getLastElement();
			if (cp instanceof ClippingPlane)		wisit((ClippingPlane) cp);
			else System.out.println("Invalid clipplane class "+cp.getClass().toString());
			gri.gl.glPopMatrix();
		}
	}

	public void rerender()	{
		gri.theViewer.render();
	}
	
	public void visit(Appearance ap)	{
		currentAppearance = ap;
		currentGeometryShader = (DefaultGeometryShader) gri.apTable.get(ap);
		renderingHints = (RenderingHintsShader) gri.rhTable.get(ap);
		if (currentGeometryShader == null ) {
			shaderUptodate = false;
			setupShader();
		}
		if (renderingHints.isLightingEnabled())	gri.gl.glEnable(GL.GL_LIGHTING);
		else									gri.gl.glDisable(GL.GL_LIGHTING);
		
		if (eAp.getAttribute("polygonShader.useGLShader", false) == true)	{
			Object obj = (Object) eAp.getAttribute("polygonShader.GLShader", null, AbstractJOGLShader.class);
			if (obj instanceof AbstractJOGLShader) {
				System.out.println("Attempting to activate GL shader");
				AbstractJOGLShader sh = (AbstractJOGLShader) obj;
				//sh.setupShader();
				sh.activate(gri.theCanvas);
			}
		}
		//System.out.println("Transparency "+(renderingHints.isTransparencyEnabled() ? "enabled" : "disabled"));
		//gri.gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, polygonShader.getDiffuseColorAsFloat());
		float[] testcolor = {.3f, .5f, .7f, .5f};
		DefaultPolygonShader dps =  ((DefaultPolygonShader) currentGeometryShader.getPolygonShader());
		if (dps != null) gri.gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, dps.getDiffuseColorAsFloat());
		//System.out.println("Setting diffuse color to : "+currentGeometryShader.polygonShader.getDiffuseColor().toString());
		gri.gl.glMaterialfv(GL.GL_BACK, GL.GL_DIFFUSE, testcolor);
		if (dps != null) gri.gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, ((DefaultVertexShader) ((DefaultPolygonShader) currentGeometryShader.getPolygonShader()).vertexShader).getSpecularColorAsFloat());
		if (dps != null) gri.gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, (float) ((DefaultVertexShader) ((DefaultPolygonShader) currentGeometryShader.getPolygonShader()).vertexShader).getSpecularExponent());
	}
	
	private void setupShader()	{
		if (shaderUptodate) return;
		eAp = eAp.create(currentAppearance);
		gri.gc.setEffectiveAppearance(eAp);
		if (currentGeometryShader == null) 	{
			currentGeometryShader = DefaultGeometryShader.createFromEffectiveAppearance(eAp,"");
			gri.apTable.put(currentAppearance, currentGeometryShader);
			renderingHints = RenderingHintsShader.createFromEffectiveAppearance(eAp,"");
			gri.rhTable.put(currentAppearance, renderingHints);
		} 
		shaderUptodate = true;
	}
	/* (non-Javadoc)
	 * @see de.jreality..SceneGraphVisitor#visit(de.jreality..Camera)
	 */
	public void visit(PointSet sg) {
		if (!shaderUptodate) setupShader();
		//Logger.getLoggerer.getLogger("de.jreality").log(Level.FINER, "Visiting VertexShape3D");
		//if (!sg.isVisible()) return;		
		boolean insideAtEntry = gri.insideDisplayList;
		
		if (displayListValid(sg))	return;
		// TODO figure out how to avoid recurring on "helper" geometry
		// i.e., we don't want to draw spheres around the vertices of
		// a geometry used to represent a point as a sphere.
		if (insidePointSet) return;
		
		insidePointSet = true;
		JOGLRendererHelper.drawVertices(sg, gri.theCanvas, currentGeometryShader, renderingHints, this, gri.pickMode);
		if (gri.useDisplayLists && !insideAtEntry)	{
			gri.gl.glEndList();
			gri.insideDisplayList = false;
		}
		insidePointSet = false;
	}
	

	/* (non-Javadoc)
	 * @see de.jreality..SceneGraphVisitor#visit(de.jreality..IndexedLineSet)
	 */
	public void visit(IndexedLineSet sg) {

		if (!shaderUptodate) setupShader();
		//theLog.log(Level.FINER, "Visiting IndexedLineSet");
		//if (!sg.isVisible()) return;
		int colorBind,normalBind;
		
		boolean insideAtEntry = gri.insideDisplayList;
		if (displayListValid(sg))	return;
		if (sg.getEdgeAttributes(Attribute.INDICES) == null) return;
		insideLineSet = true;
		if (currentGeometryShader.isEdgeDraw())	{
			JOGLRendererHelper.drawLines(sg, gri.theCanvas, currentGeometryShader, renderingHints, insidePointSet, gri.pickMode);
		}
		insideLineSet = false;
		visit((PointSet) sg);
		if ( gri.useDisplayLists && !insideAtEntry)	{
			gri.gl.glEndList();
			gri.insideDisplayList = false;
		}

	}
	
	/* (non-Javadoc)
	 * @see de.jreality..SceneGraphVisitor#visit(de.jreality..IndexedFaceSet)
	 */
	public void visit(IndexedFaceSet sg) {
		//if (!sg.isVisible()) return;

		if (!shaderUptodate) setupShader();
		//theLog.log(Level.FINER, "Visiting IndexedFaceSet");
		
		boolean insideAtEntry = gri.insideDisplayList;
		if (displayListValid(sg))	return;
		
		if (currentGeometryShader.isFaceDraw())	{
			JOGLRendererHelper.drawFaces(this, sg, gri.theCanvas, currentGeometryShader, renderingHints, insidePointSet, insideLineSet, gri.pickMode);
		}
		visit((IndexedLineSet) sg);
		if (!insideAtEntry && gri.useDisplayLists)	{
			gri.gl.glEndList();
			gri.insideDisplayList = false;
		}
	}
  
	public void visit(Sphere sg) {
		//Primitives.sharedIcosahedron.accept(this);
		double lod = renderingHints.getLevelOfDetail();
		Geometry helper = null;
		//if (lod == 0.0) 
			//helper = SphereHelper.SPHERE_FINE;
//		else	{
//			double area = GeometryUtility.getNDCArea(sg, gri.gc.getObjectToNDC());
//			if (area < .01)	helper = SphereHelper.SPHERE_COARSE;
//			else if (area < .1 ) helper = SphereHelper.SPHERE_FINE;
//			else if (area < .5) helper = SphereHelper.SPHERE_FINER;
//			else helper = SphereHelper.SPHERE_FINEST;
//		}
			SphereHelper.SPHERE_FINE.accept(this);
		//Rectangle3D uc = Rectangle3D.unitCube;
		//SphereHelper.SPHERE_BOUND.accept(this);
	}
	
	public void visit(SceneGraphComponent sg) {
		//Logger.getLogger("de.jreality").log(Level.FINER, "Visiting SceneGraphComponent");
		if (gri.pickMode)	{
			gri.gl.glPushName(childIndex++);
		}
		if (!sg.isVisible()) return;
		sg.preRender(gri.gc);
		gri.nodeCount++;
		// we aren't maintaining a software stack; use the path to calculate current matrix
		// if needed
		currentPath.push(sg);
		gri.gc.setCurrentPath(currentPath);
		//System.out.println("Node "+sg.getName());
		stackDepth++;
		if (stackDepth <= MAX_STACK_DEPTH) gri.gl.glPushMatrix();
		sg.childrenAccept(pushContext());			
		if (stackDepth <= MAX_STACK_DEPTH) gri.gl.glPopMatrix();
		stackDepth--;
		
		currentPath.pop();
		if (gri.pickMode)	{
			gri.gl.glPopName();
		}

	}
	
	/* (non-Javadoc)
	 * @see de.jreality..SceneGraphVisitor#visit(de.jreality..SceneGraphKit)
	 */
	public void visit(Transformation sg) {
		//theLog.log(Level.FINER, "Visiting Transformation");
		boolean isR = false;
		//Rn.times(currentMatrix, currentMatrix, sg.getMatrix());
		//gri.gc.setObjectToWorld(currentMatrix);
		isR = sg.getIsReflection();
		isReflection = !(isR == isReflection);
		gri.gl.glFrontFace( isReflection? GL.GL_CW : GL.GL_CCW);
		if (stackDepth <= MAX_STACK_DEPTH) gri.gl.glMultTransposeMatrixd(sg.getMatrix());
		else gri.gl.glLoadTransposeMatrixd(gri.gc.getObjectToCamera());

	}

	static long frameCount = 0, otime;
	
	/* (non-Javadoc)
	 * @see net.java.games.jogl.GLEventListener#init(net.java.games.jogl.GLDrawable)
	 */
	public void init(GLDrawable drawable) {
		//theLog.log(Level.FINER, "In init()");
		if (debugGL) {
			drawable.setGL(new DebugGL(drawable.getGL()));
		}
		//Thread currentThread = Thread.currentThread();
		//Thread rThread = drawable.getRenderingThread();
		//if (rThread == null || rThread != currentThread)	{
			//theLog.log(Level.INFO, "Current thread: "+currentThread.getName());
			//if (rThread == null) theLog.log(Level.INFO, "Rendering thread is null");
			//else theLog.log(Level.INFO, "Rendering thread: "+rThread.getName());
			//drawable.setRenderingThread(currentThread);
		//}
		gri.gl = gri.theCanvas.getGL();
		gri.glu = gri.theCanvas.getGLU();

		String vv = (String) gri.gl.glGetString(GL.GL_VERSION);
		theLog.log(Level.INFO,"version: "+vv);
		
		otime = System.currentTimeMillis();

		gri.theCamera.setAspectRatio(((double) gri.theCanvas.getWidth())/gri.theCanvas.getHeight());
		gri.gl.glViewport(0,0, gri.theCanvas.getWidth(), gri.theCanvas.getHeight());

		JOGLRendererHelperNew.initializeGLState(gri.theCanvas);
}

	public void display(GLDrawable drawable) {
		//if (gri.pickMode) return;
		gri.theCanvas = (GLCanvas) drawable;
		gri.gl = gri.theCanvas.getGL();
		if (gri.theCamera != CameraUtility.getCamera(gri.theViewer))	{
			gri.theCamera = CameraUtility.getCamera(gri.theViewer);
			gri.theCamera.setAspectRatio(((double) gri.theCanvas.getWidth())/gri.theCanvas.getHeight());
			gri.gl.glViewport(0,0, gri.theCanvas.getWidth(), gri.theCanvas.getHeight());
		}

		//gri.theCamera.update();
		// TODO for split screen stereo, may want to have a real rectangle here, not always at (0,0)
		// for now just do cross-eyed stereo
		if (gri.theCamera.isStereo())		{
			int which = gri.theViewer.getStereoType();
			if (which == Viewer.CROSS_EYED_STEREO)		{
				int w = gri.theCanvas.getWidth()/2;
				int h = gri.theCanvas.getHeight();
				gri.theCamera.setAspectRatio(((double) w)/h);
				gri.theCamera.update();
				whichEye = Camera.RIGHT_EYE;
				gri.gl.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				gri.gl.glViewport(0,0, w,h);
				visit();
				whichEye = Camera.LEFT_EYE;
				gri.gl.glViewport(w, 0, w,h);
				visit();
			} 
			else if (which >= Viewer.RED_BLUE_STEREO &&  which <= Viewer.RED_CYAN_STEREO) {
				gri.theCamera.setAspectRatio(((double) gri.theCanvas.getWidth())/gri.theCanvas.getHeight());
				gri.gl.glViewport(0,0, gri.theCanvas.getWidth(), gri.theCanvas.getHeight());
				gri.gl.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				whichEye = Camera.RIGHT_EYE;
		        if (which == Viewer.RED_GREEN_STEREO) gri.gl.glColorMask(false, true, false, true);
		        else if (which == Viewer.RED_BLUE_STEREO) gri.gl.glColorMask(false, false, true, true);
		        else if (which == Viewer.RED_CYAN_STEREO) gri.gl.glColorMask(false, true, true, true);
				visit();
				whichEye = Camera.LEFT_EYE;
		        gri.gl.glColorMask(true, false, false, true);
				gri.gl.glClear (GL.GL_DEPTH_BUFFER_BIT);
				visit();
		        gri.gl.glColorMask(true, true, true, true);
			} 
			else	{
				gri.theCamera.setAspectRatio(((double) gri.theCanvas.getWidth())/gri.theCanvas.getHeight());
				gri.gl.glViewport(0,0, gri.theCanvas.getWidth(), gri.theCanvas.getHeight());
				whichEye = Camera.RIGHT_EYE;
				gri.gl.glDrawBuffer(GL.GL_BACK_RIGHT);
				gri.gl.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				visit();
				whichEye = Camera.LEFT_EYE;
				gri.gl.glDrawBuffer(GL.GL_BACK_LEFT);
				gri.gl.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				visit();
			}
		} 
		else {
			gri.gl.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			gri.theCamera.setAspectRatio(((double) gri.theCanvas.getWidth())/gri.theCanvas.getHeight());
			gri.gl.glViewport(0,0, gri.theCanvas.getWidth(), gri.theCanvas.getHeight());
			if (!gri.pickMode)	visit();
			else		{
				// set up the "pick transformation"
				IntBuffer selectBuffer = BufferUtils.newIntBuffer(bufsize);
				
				double[] pp3 = new double[3];
				pp3[0] = -pickScale * pickPoint[0]; pp3[1] = -pickScale * pickPoint[1]; pp3[2] = 0.0;
				
				gri.pickT.setTranslation(pp3);
				double[] stretch = {pickScale, pickScale, 1.0};
				gri.pickT.setStretch(stretch);
				boolean store = isUseDisplayLists();
				gri.useDisplayLists = false;
				childIndex = 0;
				gri.gl.glSelectBuffer(bufsize, selectBuffer);		
				gri.gl.glRenderMode(GL.GL_SELECT);
				gri.gl.glInitNames();
				visit();
				gri.pickMode = false;
				int numberHits = gri.gl.glRenderMode(GL.GL_RENDER);
				//System.out.println(numberHits+" hits");
				gri.hits = PickAction.processOpenGLSelectionBuffer(numberHits, selectBuffer, pickPoint,gri.theViewer);
				gri.useDisplayLists = store;
				display(drawable);
				//gri.theViewer.render();
				// redraw
//				display(drawable);
//				if (pickRequestor != null)	{
//					pickRequestor.pickPerformed( hits);
//					pickRequestor = null;
//				}
			}
		}
		if (++frameCount % 100 == 0) {
			long time = System.currentTimeMillis();
			//theLog.log(Level.FINER,"Frame rate:\t"+(1000000.0/(time-otime)));
			//System.err.println("Frame rate:\t"+(1000000.0/(time-otime)));
			gri.framerate = (100000.0/(time-otime));
			otime = time;
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
		gri.theCamera.setAspectRatio(((double) gri.theCanvas.getWidth())/gri.theCanvas.getHeight());
		gri.gl.glViewport(0,0, gri.theCanvas.getWidth(), gri.theCanvas.getHeight());
	}

	/**
	 * @return
	 */
	public boolean isUseDisplayLists() {
		return gri.useDisplayLists;
	}

	/**
	 * @param b
	 */
	public void setUseDisplayLists(boolean b) {
		gri.useDisplayLists = b;
		if (gri.useDisplayLists)	gri.forceNewDisplayLists = true;
	}

	private static boolean removeOnChange = true;
	/* (non-Javadoc)
	 * @see de.jreality..VertexShape3D.Listener#geometryChanged(de.jreality..VertexShape3D.Changed)
	 */
	public void geometryChanged(GeometryEvent e) {
		//System.err.println("SGRT: geometryChanged");
		final SceneGraphNode sg = (SceneGraphNode) e.getSource();
		Object foo = gri.dlTable.get(sg);
		if (foo!= null && foo instanceof DisplayListInfo) {
			DisplayListInfo dli = (DisplayListInfo) foo;
			dli.frameCountAtLastChange = frameCount;
			final int dlId = dli.displayListID;
			dli.changeCount++;
			dli.displayListDirty = true;
			//System.err.println("Deleting display list for geometry: "+sg.getName());
			// remove the entry from the table:
			if (removeOnChange)	{
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							gri.dlTable.remove(sg);
							gri.gl.glDeleteLists(dlId, 1);				
						}
					});
			}
		}
		/*
		CollectAncestorsVisitor cav = new CollectAncestorsVisitor(gri.theRoot, sg);
		List anc = (List) cav.visit();
		for (int i = 0; i<anc.size(); ++i)		{
			Object ancx = anc.get(i);
			//System.err.println("ancestor: "+ancx);
			foo = gri.dlTable.get(ancx);
			if (foo!= null) gri.dlTable.remove(ancx);
		}
		*/
	}
	
	private class DisplayListInfo	{
		boolean useDisplayList;
		int displayListID;
		boolean displayListDirty;
		int changeCount;
		long frameCountAtLastChange;
		DisplayListInfo(int id)	{
			super();
			displayListID = id;
			useDisplayList = true;
			displayListDirty = false;
			frameCountAtLastChange = frameCount;
			changeCount = 0;
		}
		boolean useDisplayList(long frameCount)	{
			if (changeCount <= 3) return true;
			long df = frameCount - frameCountAtLastChange;
			if (useDisplayList && df <= 5) useDisplayList = false;
			else if (!useDisplayList && df >= 5) useDisplayList = true;
			return useDisplayList;
		}
	}
	
	
	private boolean displayListValid(Object sg)	{
		// There are two cases where we don't generate display lists
		// First, if we're already inside a display list 
		// or the global flag for display lists is turned off
		if (gri.insideDisplayList || !gri.useDisplayLists) return false;
		
		boolean reuse = false;
		DisplayListInfo dl = (DisplayListInfo) gri.dlTable.get(sg);
		
		// and secondly if this particular object dynamically determines that
		// display lists are a bad idea
		if (dl != null)	{
			if (!dl.useDisplayList(frameCount)) return false;
			if (!gri.forceNewDisplayLists) reuse = true;
			if (dl.displayListDirty) reuse = false;
		}
		// to be here means we will use display lists
		// Either the current one is valid ...
		if (reuse)	{
			gri.gl.glCallList(dl.displayListID);
			return true;
		} // or we need to generate a new display list
		int nextDL = gri.gl.glGenLists(1);
		if (dl == null)	{
			dl = new DisplayListInfo(nextDL);					
			gri.dlTable.put(sg,dl);
		} else {
			gri.gl.glDeleteLists(dl.displayListID, 1);
		}
		dl.displayListID = nextDL;
		gri.gl.glNewList(nextDL, GL.GL_COMPILE_AND_EXECUTE);
		gri.insideDisplayList = true;
		dl.displayListDirty = false;
		return false; 
	}

	/**
	 * @return
	 */
	public Graphics3D getGraphics3D() {
		return gri.gc;
	}

	/**
	 * @param graphics3D
	 */
	/* public void setGraphics3D(Graphics3D graphics3D) {
		gri.gc = graphics3D;
	} */

	/* (non-Javadoc)
	 * @see de.jreality..AncestorFound#ancestorFound(java.lang.Object)
	 */
	public void ancestorFound(Object ancestor) {
		Object foo = gri.dlTable.get(ancestor);
		if (foo!= null) gri.dlTable.remove(ancestor);

	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.event.AppearanceListener#appearanceChanged(de.jreality.scene.event.AppearanceEvent)
	 */
	public void appearanceChanged(AppearanceEvent ev) {
		gri.apDirty = true;
		//theLog.log(Level.FINER, "SGRT: appearanceChanged");

	}

	public double getFramerate()	{
		return gri.framerate;
	}

	private static boolean toggle = true;
	private static  int bufsize = 4096;
	double[] pickPoint = new double[2];
	JOGLPickRequestor  pickRequestor = null;
	public PickPoint[] performPick(double[] p)	{
		if (gri.theCamera.isStereo())		{
			System.out.println("Can't pick in stereo mode");
			return null;
		}
		pickPoint[0] = p[0];  pickPoint[1] = p[1];
		gri.pickMode = true;
		final Component viewingComponent = gri.theViewer.getViewingComponent();
		((GLCanvas) viewingComponent).display();
//		if(EventQueue.isDispatchThread()) {
//			viewingComponent.update(viewingComponent.getGraphics());
//		} else
//			try {
//				EventQueue.invokeAndWait(new Runnable() {
//					public void run() {
//						viewingComponent.update(viewingComponent.getGraphics());
//					}
//				});
//			}
//		    catch (InterruptedException e) {}
//			catch (InvocationTargetException e) {}
		return gri.hits;
	}

}
