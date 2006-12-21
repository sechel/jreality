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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.FileUtil;
import com.sun.opengl.util.ImageUtil;

import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.pick.Graphics3D;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.jogl.pick.PickPoint;
import de.jreality.jogl.shader.DefaultGeometryShader;
import de.jreality.jogl.shader.DefaultPolygonShader;
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
import de.jreality.scene.Viewer;
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

	private final  Logger theLog = LoggingSystem.getLogger(this);
	private static boolean collectFrameRate = true;
	private final static int MAX_STACK_DEPTH = 28;
	private int stackDepth;

	private SceneGraphPath currentPath = new SceneGraphPath();

	private SceneGraphComponent theRoot, auxiliaryRoot;
	private JOGLPeerComponent thePeerRoot = null;
	private JOGLPeerComponent thePeerAuxilliaryRoot = null;
	private JOGLRenderingState openGLState;

	private int width, height;		// GLDrawable.getSize() isnt' implemented for GLPBuffer!
	private int whichEye = CameraUtility.MIDDLE_EYE;
	private int[] currentViewport = new int[4];
	private Graphics3D context;

	private GL globalGL;
	private boolean  manyDisplayLists = false;

	private boolean texResident = true;
	private int numberTries = 0;		// how many times we have tried to make textures resident
	private boolean forceResidentTextures = true;

	private boolean globalIsReflection = false;
	private int currentSignature = Pn.EUCLIDEAN;

	// pick-related stuff
	private boolean pickMode = false, offscreenMode = false;
	private final double pickScale = 10000.0;
	private Transformation pickT = new Transformation();
	private PickPoint[] hits;
	// an exotic mode: render the back hemisphere of the 3-sphere (currently disabled)
	private boolean backSphere = false;
	private double framerate;
	private int nodeCount = 0;

	WeakHashMap<Geometry, JOGLPeerGeometry> geometries = new WeakHashMap<Geometry, JOGLPeerGeometry>();
	boolean geometryRemoved = false, lightListDirty = true;
	private Viewer theViewer;

	private int stereoType;
	private boolean flipped;

	public JOGLRenderer(Viewer viewer) {
		theViewer=viewer;
		javax.swing.Timer followTimer = new javax.swing.Timer(1000, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {updateGeometryHashtable(); } } );
		followTimer.start();
		setAuxiliaryRoot(viewer.getAuxiliaryRoot());		
	}


	public GL getGL() {
		return globalGL;
	}

	public JOGLRenderingState getRenderingState() {
		return openGLState;
	}

	public void setTextureResident(boolean b) {
		texResident=b;
	}

	public int getStereoType() {
		return stereoType;
	}

	public void setStereoType(int stereoType) {
		this.stereoType = stereoType;
	}

	public boolean isFlipped() {
		return flipped;
	}

	public void setFlipped(boolean flipped) {
		this.flipped = flipped;
	}

	public Viewer getViewer() {
		return theViewer;
	}

	private void setSceneRoot(SceneGraphComponent sgc) {
		if (theRoot != null) {
			Appearance ap = theRoot.getAppearance();
			if (ap != null) ap.removeAppearanceListener(this);
		}
		theRoot = sgc;
		if (theRoot.getAppearance() != null) theRoot.getAppearance().addAppearanceListener(this);
		thePeerRoot = constructPeerForSceneGraphComponent(theRoot, null);   
		// some top-level appearance attributes determine how we render; 
		// TODO set up a separate mechanism for controlling these top-level attributes

		theLog.info("setSceneRoot");
		extractGlobalParameters();
	}

	public void appearanceChanged(AppearanceEvent ev) {
		theLog.info("top appearance changed");
		extractGlobalParameters();
	}

	public void extractGlobalParameters()	{
		Appearance ap = theRoot.getAppearance();
		if (ap == null) return;
		theLog.finer("In extractGlobalParameters");
		Object obj = ap.getAttribute(CommonAttributes.FORCE_RESIDENT_TEXTURES, Boolean.class);		// assume the best ...
		if (obj instanceof Boolean) forceResidentTextures = ((Boolean)obj).booleanValue();
		obj = ap.getAttribute(CommonAttributes.MANY_DISPLAY_LISTS, Boolean.class);		// assume the best ...
		if (obj instanceof Boolean) manyDisplayLists = ((Boolean)obj).booleanValue();
		obj = ap.getAttribute(CommonAttributes.ANY_DISPLAY_LISTS, Boolean.class);		// assume the best ...
		obj = ap.getAttribute(CommonAttributes.CLEAR_COLOR_BUFFER, Boolean.class);		// assume the best ...
		if (obj instanceof Boolean) {
			openGLState.clearColorBuffer = ((Boolean)obj).booleanValue();
			theLog.fine("Setting clear color buffer to "+openGLState.clearColorBuffer);
		}
		theLog.fine("forceResTex = "+forceResidentTextures);
		theLog.fine("many display lists = "+manyDisplayLists);
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

		globalGL.glMatrixMode(GL.GL_PROJECTION);
		globalGL.glLoadIdentity();

		if (pickMode)
			globalGL.glMultTransposeMatrixd(pickT.getMatrix(), 0);
		else
			JOGLRendererHelper.handleBackground(this, width, height, theRoot.getAppearance());

		double aspectRatio = getAspectRatio();
		// for pick mode the aspect ratio has to be set to that of the viewer component
		if (pickMode) aspectRatio = CameraUtility.getAspectRatio(theViewer);
		double[] c2ndc = CameraUtility.getCameraToNDC(CameraUtility.getCamera(theViewer), 
				aspectRatio,
				whichEye);
		globalGL.glMultTransposeMatrixd(c2ndc, 0);

		// prepare for rendering the geometry
		globalGL.glMatrixMode(GL.GL_MODELVIEW);
		globalGL.glLoadIdentity();

		if (backSphere) {  globalGL.glLoadTransposeMatrixd(P3.p3involution, 0);	globalGL.glPushMatrix(); }
		double[] w2c = context.getWorldToCamera();
		globalGL.glLoadTransposeMatrixd(w2c, 0);
		globalIsReflection = ( isFlipped() != (Rn.determinant(w2c) < 0.0));

		JOGLRendererHelper.handleSkyBox(this, theRoot.getAppearance(),CameraUtility.getCamera(theViewer) );

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

	private void processClippingPlanes() {
		List clipPlanes = null;
		if (clipPlanes == null)	{
			clipPlanes = SceneGraphUtility.collectClippingPlanes(theRoot);
		}
		JOGLRendererHelper.processClippingPlanes(globalGL, clipPlanes);
	}

	List lights = null;
	private void processLights( ) {
		if (lights == null || lights.size() == 0 || lightListDirty) {
			lights = SceneGraphUtility.collectLights(theRoot);
			JOGLRendererHelper.resetLights(globalGL, lights);
			lightListDirty = false;
			openGLState.numLights = lights.size();
		}
		JOGLRendererHelper.processLights(globalGL, lights);
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
		// TODO!!!
		//theCanvas.display();	// this calls our display() method  directly
		return hits;
	}

	private void myglViewport(int lx, int ly, int rx, int ry)	{
		globalGL.glViewport(lx, ly, rx, ry);
		currentViewport[0] = lx;
		currentViewport[1] = ly;
		currentViewport[2] = rx;
		currentViewport[3] = ry;
	}

	public void init(GLAutoDrawable drawable) {
		if (JOGLConfiguration.debugGL) {
			drawable.setGL(new DebugGL(drawable.getGL()));
		}
		GLAutoDrawable theCanvas = drawable;
		if (!(theCanvas instanceof GLPbuffer))  {  // workaround in bug in implementation of GLPbuffer
			width = theCanvas.getWidth();
			height = theCanvas.getHeight();
		}
		init(drawable.getGL());
	}

	public void init(GL gl) {
		globalGL = gl;
		
		openGLState = new JOGLRenderingState(this);

		String vv = globalGL.glGetString(GL.GL_VERSION);
		theLog.log(Level.FINE,"version: "+vv);			
//		int[] tu = new int[1];
//		globalGL.glGetIntegerv(GL.GL_MAX_TEXTURE_UNITS, tu,0);
//		theLog.info("# of texture units: "+tu[0]);	
		// force new display lists
		Texture2DLoaderJOGL.deleteAllTextures(globalGL);
		JOGLCylinderUtility.setupCylinderDLists(this);
		JOGLSphereHelper.setupSphereDLists(this);
		// traverse tree and delete all display lists
		if (thePeerRoot != null) thePeerRoot.propagateGeometryChanged(ALL_GEOMETRY_CHANGED, true);
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.propagateGeometryChanged(ALL_GEOMETRY_CHANGED, true);
	}
	
	public void display(GLAutoDrawable drawable) {
		if (theViewer.getSceneRoot() == null || theViewer.getCameraPath() == null) {
			theLog.info("display called w/o scene root or camera path");
		}
		display(drawable.getGL());
	}

	public void display(GL gl) {
		globalGL=gl;
		openGLState.initializeGLState();
		long beginTime = 0;
		if (collectFrameRate) beginTime = System.currentTimeMillis();
		Camera theCamera = CameraUtility.getCamera(theViewer);
		clearColorBits = (openGLState.clearColorBuffer ? GL.GL_COLOR_BUFFER_BIT : 0);
//		theLog.fine("JOGLRR ccb = "+clearColorBits);
		if (offscreenMode) {
			if (theCamera.isStereo() && getStereoType() != de.jreality.jogl.Viewer.CROSS_EYED_STEREO) {
				theLog.warning("Can only save cross-eyed stereo offscreen");
				offscreenMode = false;
				return;
			}
			GLContext context = offscreenPBuffer.getContext();
			if (context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT) {
				JOGLConfiguration.getLogger().log(Level.WARNING,"Error making pbuffer's context current");
				offscreenMode = false;
				return;
			}
			globalGL = offscreenPBuffer.getGL();
			forceNewDisplayLists();
			openGLState.initializeGLState();

			Color[] bg=null;
			float[][] bgColors=null;
			if (numTiles > 1) {
				if (theRoot.getAppearance() != null && theRoot.getAppearance().getAttribute(CommonAttributes.BACKGROUND_COLORS, Color[].class) != Appearance.INHERITED) {
					bg = (Color[]) theRoot.getAppearance().getAttribute(CommonAttributes.BACKGROUND_COLORS, Color[].class);
					bgColors=new float[4][];
					bgColors[0]=bg[0].getColorComponents(null);
					bgColors[1]=bg[1].getColorComponents(null);
					bgColors[2]=bg[2].getColorComponents(null);
					bgColors[3]=bg[3].getColorComponents(null);
				}
			}
			int numImages = theCamera.isStereo() ? 2 : 1;
			tileSizeX = tileSizeX / numImages;
			myglViewport(0,0,tileSizeX, tileSizeY);
			Rectangle2D vp = CameraUtility.getViewport(theCamera, getAspectRatio()); //CameraUtility.getAspectRatio(theViewer));
			double dx = vp.getWidth()/numTiles;
			double dy = vp.getHeight()/numTiles;
			boolean isOnAxis = theCamera.isOnAxis();
			theCamera.setOnAxis(false);
			for (int st = 0; st < numImages; ++st)	{
				if (theCamera.isStereo()) {
					if (st == 0) whichEye = CameraUtility.RIGHT_EYE;
					else whichEye =  CameraUtility.LEFT_EYE;
				}
				else whichEye = CameraUtility.MIDDLE_EYE;
				for (int i = 0; i<numTiles; ++i)	{
					for (int j = 0; j<numTiles; ++j)	{
						openGLState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
						Rectangle2D lr = new Rectangle2D.Double(vp.getX()+j*dx, vp.getY()+i*dy, dx, dy);
						System.err.println("Setting vp to "+lr.toString());
						theCamera.setViewPort(lr);
						
						if (bgColors != null) {
							Color[] currentBg = new Color[4];
							currentBg[1]=interpolateBG(bgColors, i+1, j);
							currentBg[2]=interpolateBG(bgColors, i, j);
							currentBg[3]=interpolateBG(bgColors, i, j+1);
							currentBg[0]=interpolateBG(bgColors, i+1, j+1);
							theRoot.getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, currentBg);
						}
						
						render();
						globalGL.glPixelStorei(GL.GL_PACK_ROW_LENGTH,numImages*numTiles*tileSizeX);
						globalGL.glPixelStorei(GL.GL_PACK_SKIP_ROWS, i*tileSizeY);
						globalGL.glPixelStorei(GL.GL_PACK_SKIP_PIXELS, (st*numTiles+j)*tileSizeX);
						globalGL.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);

						globalGL.glReadPixels(0, 0, tileSizeX, tileSizeY,
								GL.GL_BGR, GL.GL_UNSIGNED_BYTE, offscreenBuffer);
					}
				}
				
			}

			if (bgColors != null) theRoot.getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLORS, bg);
			
			context.release();
			theCamera.setOnAxis(isOnAxis);
			Dimension d = theViewer.getViewingComponentSize();
			myglViewport(0, 0, (int) d.getWidth(), (int) d.getHeight());
			offscreenMode = false;
		} else 		if (theCamera.isStereo())		{
			setupRightEye(width, height);
			render();
			setupLeftEye(width, height);
			render();
			openGLState.colorMask =15; //globalGL.glColorMask(true, true, true, true);
		} 
		else {
			openGLState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
			myglViewport(0,0,width, height);
			whichEye=CameraUtility.MIDDLE_EYE;
			if (pickMode)	{
				myglViewport(0,0, 2,2);
				IntBuffer selectBuffer = BufferUtil.newIntBuffer(bufsize);
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
				//int numberHits = globalGL.glRenderMode(GL.GL_RENDER);
				// HACK
				//hits = JOGLPickAction.processOpenGLSelectionBuffer(numberHits, selectBuffer, pickPoint,theViewer);
				display(globalGL);
			}			
			else	 
				render();			
		}

		if (collectFrameRate)	{
			++frameCount;
			int j = (frameCount % 20);
			clockTime[j] = beginTime;
			history[j]  =  System.currentTimeMillis() - beginTime;
		}
	}

	private Color interpolateBG(float[][] bgColors, int i, int j) {
		float[] col = new float[bgColors[0].length];
		float alpha = ((float)j)/numTiles;
		float beta = 1-((float)i)/numTiles;
		//col = alpha*(1-beta)*bgColors[0]+(1-alpha)*(1-beta)*bgColors[1]+beta*(1-alpha)*bgColors[2]+alpha*beta*bgColors[3]
		for (int k = 0; k < col.length; k++) {
			col[k] = alpha*(1-beta)*bgColors[0][k]+(1-alpha)*(1-beta)*bgColors[1][k]+beta*(1-alpha)*bgColors[2][k]+alpha*beta*bgColors[3][k];
		}
		if (col.length == 3) return new Color(col[0], col[1], col[2]);
		else return new Color(col[0], col[1], col[2], col[3]);
	}

	private void setupRightEye(int width, int height) {
		int which = getStereoType();
		switch(which)	{
		case de.jreality.jogl.Viewer.CROSS_EYED_STEREO:
			openGLState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
			//globalGL.glClear (clearColorBits | GL.GL_DEPTH_BUFFER_BIT);
			int w = width/2;
			int h = height;
			myglViewport(0,0, w,h);
			break;

		case de.jreality.jogl.Viewer.RED_BLUE_STEREO:
		case de.jreality.jogl.Viewer.RED_CYAN_STEREO:
		case de.jreality.jogl.Viewer.RED_GREEN_STEREO:
			myglViewport(0,0, width, height);
			openGLState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
			if (which == de.jreality.jogl.Viewer.RED_GREEN_STEREO) openGLState.colorMask = 10; //globalGL.glColorMask(false, true, false, true);
			else if (which == de.jreality.jogl.Viewer.RED_BLUE_STEREO) openGLState.colorMask = 12; //globalGL.glColorMask(false, false, true, true);
			else if (which == de.jreality.jogl.Viewer.RED_CYAN_STEREO) openGLState.colorMask = 14; //globalGL.glColorMask(false, true, true, true);
			break;

		case de.jreality.jogl.Viewer.HARDWARE_BUFFER_STEREO:
			myglViewport(0,0, width, height);
			openGLState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
			globalGL.glDrawBuffer(GL.GL_BACK_RIGHT);
			break;			
		}
		whichEye=CameraUtility.RIGHT_EYE;
	}

	private void setupLeftEye(int width, int height) {
		int which = getStereoType();
		switch(which)	{
		case de.jreality.jogl.Viewer.CROSS_EYED_STEREO:
			int w = width/2;
			int h = height;
			openGLState.clearBufferBits = 0;
			myglViewport(w, 0, w,h);
			break;

		case de.jreality.jogl.Viewer.RED_BLUE_STEREO:
		case de.jreality.jogl.Viewer.RED_CYAN_STEREO:
		case de.jreality.jogl.Viewer.RED_GREEN_STEREO:
			openGLState.colorMask = 9; //globalGL.glColorMask(true, false, false, true);
			openGLState.clearBufferBits = GL.GL_DEPTH_BUFFER_BIT;
			break;

		case de.jreality.jogl.Viewer.HARDWARE_BUFFER_STEREO:
			globalGL.glDrawBuffer(GL.GL_BACK_LEFT);
			openGLState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
//			globalGL.glClear (clearColorBits | GL.GL_DEPTH_BUFFER_BIT);
			break;
		}
		whichEye=CameraUtility.LEFT_EYE;
	}

	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
	}

	public void reshape(GLAutoDrawable arg0,int arg1,int arg2,int arg3,int arg4) {
		width = arg3-arg1;
		height = arg4-arg2;
		myglViewport(0,0, width, height);
	}

	private final static int POINTS_CHANGED = 1;
	private final static int LINES_CHANGED = 2;
	private final static int FACES_CHANGED = 4;
	private final static int ALL_GEOMETRY_CHANGED = 7;
	private final static int POINT_SHADER_CHANGED = 8;
	private final static int LINE_SHADER_CHANGED = 16;
	private final static int POLYGON_SHADER_CHANGED = 32;
	private final static int ALL_SHADERS_CHANGED = POINT_SHADER_CHANGED | LINE_SHADER_CHANGED | POLYGON_SHADER_CHANGED;
	private final static int ALL_CHANGED = ALL_GEOMETRY_CHANGED | ALL_SHADERS_CHANGED;

	int geomDiff = 0;
	protected void updateGeometryHashtable() {
		if (!geometryRemoved) return;
		final WeakHashMap<Geometry, JOGLPeerGeometry> newG = new WeakHashMap<Geometry, JOGLPeerGeometry>();
		SceneGraphVisitor cleanup = new SceneGraphVisitor()	{
			public void visit(SceneGraphComponent c) {
				if (c.getGeometry() != null) {
					Geometry wawa = c.getGeometry();
					JOGLPeerGeometry peer = geometries.get(wawa);
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

	protected class JOGLPeerNode	{
		String name;

		public String getName()	{
			return name;
		}

		public void setName(String n)	{
			name = n;
		}
	}

	protected class JOGLPeerGeometry extends JOGLPeerNode	implements GeometryListener{
		public Geometry originalGeometry;
		Geometry[] tubeGeometry, proxyPolygonGeometry;
		IndexedFaceSet ifs;
		IndexedLineSet ils;
		PointSet ps;
		int refCount = 0;
		int signature = Pn.EUCLIDEAN;
		boolean isSurface = false;
		boolean preRender = false;

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
			Object foo = originalGeometry.getGeometryAttributes(JOGLConfiguration.PRE_RENDER);
			if (foo != null) preRender = true;
		}

		public void dispose()		{
			refCount--;
			if (refCount < 0)	{
				theLog.log(Level.WARNING,"Negative reference count!");
			}
			if (refCount == 0)	{
				theLog.log(Level.FINER,"Geometry is no longer referenced");
				originalGeometry.removeGeometryListener(this);
				geometries.remove(originalGeometry);
			}
		}

		public void render(JOGLPeerComponent jpc) {
			RenderingHintsShader renderingHints = jpc.renderingHints;
			DefaultGeometryShader geometryShader = jpc.geometryShader;
			openGLState.setUseDisplayLists(renderingHints.isUseDisplayLists()); //(); //useDisplayLists(activeDL, jpc);
			openGLState.setCurrentGeometry(originalGeometry);
//			openGLState.setCurrentSignature(signature);
			if (preRender && geometryShader.polygonShader instanceof DefaultPolygonShader)	{
				((DefaultPolygonShader) geometryShader.polygonShader).preRender(openGLState);		
				return;
			}
			renderingHints.render(openGLState);
			//theLog.fine("Rendering sgc "+jpc.getOriginalComponent().getName());
			//theLog.fine("vertex:edge:face:"+geometryShader.isVertexDraw()+geometryShader.isEdgeDraw()+geometryShader.isFaceDraw());
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
				JOGLRendererHelper.drawPointLabels(JOGLRenderer.this, ps,  jpc.geometryShader.pointShader.getTextShader());
			}
			if (geometryShader.isEdgeDraw() &&ils != null && ils.getEdgeAttributes(Attribute.LABELS) != null) {
				JOGLRendererHelper.drawEdgeLabels(JOGLRenderer.this, ils, jpc.geometryShader.lineShader.getTextShader());
			}
			if (geometryShader.isFaceDraw() &&ifs != null && ifs.getFaceAttributes(Attribute.LABELS) != null) {
				JOGLRendererHelper.drawFaceLabels(JOGLRenderer.this, ifs,  jpc.geometryShader.polygonShader.getTextShader());
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
	WeakHashMap<SceneGraphComponent, GoBetween> goBetweenTable = new WeakHashMap<SceneGraphComponent, GoBetween>();
	public  GoBetween goBetweenFor(SceneGraphComponent sgc)	{
		if (sgc == null) return null;
		GoBetween gb = null;
		Object foo = goBetweenTable.get(sgc);
		if (foo == null)	{
			gb = JOGLRenderer.this.new GoBetween(sgc);
			goBetweenTable.put(sgc, gb);
			return gb;
		}
		return ((GoBetween) foo);
	}

	protected class GoBetween implements GeometryListener, TransformationListener, AppearanceListener,SceneGraphComponentListener	{
		SceneGraphComponent originalComponent;
		ArrayList<JOGLPeerComponent> peers = new ArrayList<JOGLPeerComponent>();
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
			for ( JOGLPeerComponent peer: peers)	{
				peer.setDisplayListDirty();
			}
			peersLock.readUnlock();
		}

		public void transformationMatrixChanged(TransformationEvent ev) {
			peersLock.readLock();
			for (JOGLPeerComponent peer : peers)	{
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
			else if (key.indexOf("transparency") != -1) changed |= (POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
			else if (key.indexOf(CommonAttributes.SMOOTH_SHADING) != -1) changed |= (POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
			else if (key.indexOf("tubeRadius") != -1) changed |= (LINES_CHANGED);
			else if (key.indexOf("pointRadius") != -1) changed |= (POINTS_CHANGED);
			else if (key.indexOf("anyDisplayLists") != -1) changed |= (POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED);
			else if (key.endsWith("Shader")) changed |= LINE_SHADER_CHANGED | POINT_SHADER_CHANGED | POLYGON_SHADER_CHANGED;
			// there are some appearances which we know aren't inherited, so don't propagate change event.
			if (key.indexOf(CommonAttributes.BACKGROUND_COLOR) != -1	||
					key.indexOf("fog") != -1) propagates = false;

			peersLock.readLock();
			for ( JOGLPeerComponent peer: peers)	{
				if (propagates) peer.appearanceChanged(ev);
				if (changed != 0) peer.propagateGeometryChanged(changed);
			}
			peersLock.readUnlock();
			//theLog.log(Level.FINER,"setting display list dirty flag: "+changed);
		}
		public void childAdded(SceneGraphComponentEvent ev) {
			theLog.log(Level.FINE,"GoBetween: Container Child added to: "+originalComponent.getName());
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
			for ( JOGLPeerComponent peer: peers)	{
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
			peersLock.readLock();
			for ( JOGLPeerComponent peer: peers)	{
				peer.childRemoved(ev);
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
			}
			peersLock.readLock();
			for ( JOGLPeerComponent peer: peers)	{
				peer.childReplaced(ev);
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
			if (topLevel) thePeerRoot = peer;
			else if (myParent != null) {
				int n = myParent.children.size();
				//String space = (new char[2*sgp.getLength()]).toString();
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

	protected JOGLPeerComponent constructPeerForSceneGraphComponent(final SceneGraphComponent sgc, final JOGLPeerComponent p) {
		if (sgc == null) return null;
		final JOGLPeerComponent[] peer = new JOGLPeerComponent[1];
		ConstructPeerGraphVisitor constructPeer = new ConstructPeerGraphVisitor( sgc, p);
		peer[0] = (JOGLPeerComponent) constructPeer.visit();
		return peer[0];
	}

	public class JOGLPeerComponent extends JOGLPeerNode implements TransformationListener, AppearanceListener,SceneGraphComponentListener {

		public int[] bindings = new int[2];
		protected EffectiveAppearance eAp;
		protected Vector<JOGLPeerComponent> children;
		protected JOGLPeerComponent parent;
		protected int childIndex;
		protected GoBetween goBetween;

		protected boolean isReflection = false;
		boolean isCopyCat = false;
		protected boolean cumulativeIsReflection = false;
		double determinant = 0.0;


		RenderingHintsShader renderingHints;
		DefaultGeometryShader geometryShader;

		//Object childLock = new Object();
		Lock childlock = new Lock();
		protected Runnable renderGeometry = null;
		final JOGLPeerComponent self = this;
		double[][] matrices = null;
		double minDistance = -1, maxDistance = -1;
		protected boolean appearanceChanged = true;
		boolean effectiveAppearanceDirty = true,
		geometryIsDirty = true,
		boundIsDirty = true,
		clipToCamera = true;
		int geometryDirtyBits  = 0;
		protected boolean renderRunnableDirty = true;
		boolean[] matrixIsReflection = null;

		double[] tform = new double[16];		// for optimized access to matrix
		public JOGLPeerComponent(SceneGraphPath sgp, JOGLPeerComponent p)		{
			super();
			if (sgp == null || !(sgp.getLastElement() instanceof SceneGraphComponent))  {
				throw new IllegalArgumentException("Not a valid SceneGraphComponenet");
			}
			goBetween = goBetweenFor(sgp.getLastComponent());
			goBetween.addJOGLPeer(this);
			name = "JOGLPeer:"+goBetween.getOriginalComponent().getName();
//			isCopyCat = goBetween.getOriginalComponent() instanceof JOGLMultipleComponent;
			Geometry foo = goBetween.getOriginalComponent().getGeometry();
			isCopyCat = (foo != null &&  foo instanceof PointSet && foo.getGeometryAttributes(JOGLConfiguration.COPY_CAT) != null);
			if (isCopyCat)	{
				matrices = ((PointSet) foo).getVertexAttributes(Attribute.COLORS).toDoubleArrayArray(null);
				matrixIsReflection = new boolean[matrices.length];
				for (int i = 0; i<matrices.length; ++i)	matrixIsReflection[i] = Rn.determinant(matrices[i]) < 0.0;
			}
			children = new Vector<JOGLPeerComponent>();		// always have a child list, even if it's empty
			parent = p;
			updateTransformationInfo();
		}

		protected void updateRenderRunnable() {
			setDisplayListDirty();
			updateShaders();
			if (goBetween.peerGeometry == null) renderGeometry = null;
			else	 renderGeometry = new Runnable() {
				public void run() {
					goBetween.peerGeometry.render(self);
				}
			};
			renderRunnableDirty = false;
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
			Transformation thisT = preRender();
			//System.err.println(goBetween.getOriginalComponent().getName()+"Signature is "+currentSignature);
			renderChildren();
			postRender(thisT);
		}


		private Transformation preRender() {
			Transformation thisT = null;						
			nodeCount++;
			if (renderRunnableDirty) updateRenderRunnable();
			currentPath.push(goBetween.getOriginalComponent());
			context.setCurrentPath(currentPath);
			thisT = goBetween.getOriginalComponent().getTransformation();

			if (thisT != null) {
				pushTransformation(thisT.getMatrix());
			}

			if (eAp != null) {
				currentSignature = eAp.getAttribute(CommonAttributes.SIGNATURE, Pn.EUCLIDEAN);
				openGLState.setCurrentSignature(currentSignature);
				//System.err.println(goBetween.getOriginalComponent().getName()+" Setting sig to "+currentSignature);
			}
			if (parent != null) cumulativeIsReflection = (isReflection != parent.cumulativeIsReflection);
			else cumulativeIsReflection = (isReflection != globalIsReflection);
			if (cumulativeIsReflection != openGLState.flipped)	{
				globalGL.glFrontFace(cumulativeIsReflection ? GL.GL_CW : GL.GL_CCW);
				openGLState.flipped  = cumulativeIsReflection;
			}
			if (geometryDirtyBits  != 0)	handleChangedGeometry();
			if (appearanceChanged)  	propagateAppearanceChanged();
			if (goBetween != null && goBetween.peerGeometry != null && goBetween.peerGeometry.originalGeometry != null )	{
				Scene.executeReader(goBetween.peerGeometry.originalGeometry, renderGeometry );
			}
			return thisT;
		}
		protected void renderChildren() {
			int n = children.size();

			if (isCopyCat)		{
				boolean isReflectionBefore = cumulativeIsReflection;
				minDistance = eAp.getAttribute("discreteGroup.minDistance", minDistance);
				maxDistance = eAp.getAttribute("discreteGroup.maxDistance", maxDistance);
				clipToCamera = eAp.getAttribute("discreteGroup.clipToCamera", clipToCamera);

				int nn = matrices.length;
				double[] o2ndc = context.getObjectToNDC();
				double[] o2c = context.getObjectToCamera();
				int count = 0;
				for (int j = 0; j<nn; ++j)	{
					if (JOGLConfiguration.testMatrices)
						if (clipToCamera && !JOGLRendererHelper.accept(o2ndc, o2c, minDistance, maxDistance, matrices[j], currentSignature)) continue;
					count++;
					cumulativeIsReflection = (isReflectionBefore != matrixIsReflection[j]);
					if (cumulativeIsReflection != openGLState.flipped)	{
						globalGL.glFrontFace(cumulativeIsReflection ? GL.GL_CW : GL.GL_CCW);
						openGLState.flipped  = cumulativeIsReflection;
					}
					pushTransformation(matrices[j]);

					for (int i = 0; i<n; ++i)	{		
						JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);					
						child.render();
					}				
					popTransformation();
				}
				//System.err.println("Matrix count: "+count);
			} else {
				for (int i = 0; i<n; ++i)	{		
					JOGLPeerComponent child = (JOGLPeerComponent) children.get(i);					
					if (pickMode)	globalGL.glPushName(JOGLPickAction.SGCOMP_BASE+child.childIndex);
					child.render();
					if (pickMode)	globalGL.glPopName();
				}								
			}
		}

		private void postRender(Transformation thisT) {
			if (thisT != null) popTransformation();			
			currentPath.pop();
		}


		protected void popTransformation() {
			if (stackDepth <= MAX_STACK_DEPTH) {
				globalGL.glPopMatrix();			
				stackDepth--;
			}
		}

		protected void pushTransformation(double[] m) {
			if ( stackDepth <= MAX_STACK_DEPTH) {
				globalGL.glPushMatrix();
				globalGL.glMultTransposeMatrixd(m,0);
				stackDepth++;
			}
			else {
				globalGL.glLoadTransposeMatrixd(context.getObjectToCamera(),0);	
			}
		}

		protected void setIndexOfChildren()	{
			childlock.readLock();
			int n = goBetween.getOriginalComponent().getChildComponentCount();
			for (int i = 0; i<n; ++i)	{
				SceneGraphComponent sgc = goBetween.getOriginalComponent().getChildComponent(i);
				JOGLPeerComponent jpc = getPeerForChildComponent(sgc);
				if (jpc == null)	{
					theLog.log(Level.WARNING,"No peer for sgc "+sgc.getName());
					jpc.childIndex = -1;
				} else jpc.childIndex = i;
			}									
			childlock.readUnlock();

		}

		private JOGLPeerComponent getPeerForChildComponent(SceneGraphComponent sgc) {
			childlock.readLock();
			for (JOGLPeerComponent jpc : children)	{
				if ( jpc.goBetween.getOriginalComponent() == sgc) { // found!
					return jpc;
				}
			}
			childlock.readUnlock();
			return null;
		}

		public void appearanceChanged(AppearanceEvent ev) {
			appearanceChanged = true;
		}

		protected void propagateAppearanceChanged()	{
			Appearance thisAp = goBetween.getOriginalComponent().getAppearance(); 
			if (parent == null)	{
				if (eAp == null || eAp.getAppearanceHierarchy().indexOf(thisAp) == -1) {
					eAp = EffectiveAppearance.create();
					if (goBetween.getOriginalComponent().getAppearance() != null )	
						eAp = eAp.create(goBetween.getOriginalComponent().getAppearance());
				} 
			} else {
				if ( parent.eAp == null)	{
					throw new IllegalStateException("Parent must have effective appearance");
					//return;
				}
				// TODO when Appearance's are added or removed, have to set eAp to null
				if (effectiveAppearanceDirty || eAp == null)	{
					if (thisAp != null )	{
						eAp = parent.eAp.create(thisAp);
					} else {
						eAp = parent.eAp;	
					}
					effectiveAppearanceDirty = false;
				}
			}
			updateShaders();

			for (JOGLPeerComponent child : children) {
				child.propagateAppearanceChanged();
			}	
			//childlock.readUnlock();
			appearanceChanged=false;
		}

		/**
		 * @param thisAp
		 */
		private void updateShaders() {
//			can happen that the effective appearance isn't initialized yet; skip
			if (eAp == null) return;
			Appearance thisAp = goBetween.getOriginalComponent().getAppearance(); 
			if (thisAp == null && goBetween.getOriginalComponent().getGeometry() == null && parent != null)	{
				geometryShader = parent.geometryShader;
				renderingHints = parent.renderingHints;

			} else  {		
				//theLog.log(Level.FINER,"Updating shaders for "+goBetween.originalComponent.getName());
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
			theLog.log(Level.FINE,"JOGLPeerComponent: Container Child added to: "+goBetween.getOriginalComponent().getName());
			//theLog.log(Level.FINE,"Event is: "+ev.toString());
			switch (ev.getChildType() )	{
			case SceneGraphComponentEvent.CHILD_TYPE_GEOMETRY:
				updateRenderRunnable();
				break;

			case SceneGraphComponentEvent.CHILD_TYPE_COMPONENT:
				SceneGraphComponent sgc = (SceneGraphComponent) ev.getNewChildElement();
				JOGLPeerComponent pc = JOGLRenderer.this.constructPeerForSceneGraphComponent(sgc, this);
				//childlock.writeLock();
				//theLog.log(Level.FINE,"Before adding child count is "+children.size());
				children.add(pc);						
				//childlock.writeUnlock();
				//theLog.log(Level.FINE,"After adding child count is "+children.size());
				setIndexOfChildren();
				lightListDirty = true;
				break;
			case SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE:
				handleNewAppearance();
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

		private void handleNewAppearance() {
			int changed = ALL_CHANGED;
			propagateGeometryChanged(changed);	
			appearanceChanged = true;
			effectiveAppearanceDirty=true;
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
				//childlock.writeLock();
				children.remove(jpc);						
				//childlock.writeUnlock();
				//theLog.log(Level.FINE,"After removal child count is "+children.size());
				jpc.dispose();		// there are no other references to this child
				setIndexOfChildren();
				lightListDirty = true;
				break;
			case SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE:
				handleNewAppearance();
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
				renderRunnableDirty = true; 
				break;

			case SceneGraphComponentEvent.CHILD_TYPE_APPEARANCE:
				handleNewAppearance();
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

		public void propagateGeometryChanged(int changed)	{
			propagateGeometryChanged(changed, false);
		}
		
		public void propagateGeometryChanged(int changed, boolean doitnow) {
//			theLog.finer("set bits to "+changed);
			geometryDirtyBits  = changed;
			childlock.readLock();
			for (JOGLPeerComponent child: children){		
				child.propagateGeometryChanged(changed, doitnow);
			}	
			if (doitnow) handleChangedGeometry();
			childlock.readUnlock();

		}

		private void handleChangedGeometry() {
			if (geometryShader != null)	{
//				theLog.fine("Handling bits: "+geometryDirtyBits+" for "+goBetween.originalComponent.getName());
				if (geometryShader.pointShader != null && (geometryDirtyBits  & POINTS_CHANGED) != 0) geometryShader.pointShader.flushCachedState(JOGLRenderer.this);
				if (geometryShader.lineShader != null && (geometryDirtyBits  & LINES_CHANGED) != 0) geometryShader.lineShader.flushCachedState(JOGLRenderer.this);
				if (geometryShader.polygonShader != null && (geometryDirtyBits  & FACES_CHANGED) != 0) geometryShader.polygonShader.flushCachedState(JOGLRenderer.this);				
				if ((geometryDirtyBits  & POINT_SHADER_CHANGED) != 0) geometryShader.pointShader = null;
				if ((geometryDirtyBits  & LINE_SHADER_CHANGED) != 0) geometryShader.lineShader = null;
				if ((geometryDirtyBits  & POLYGON_SHADER_CHANGED) != 0) geometryShader.polygonShader = null;
                // set the dirty flag to clean again
                geometryDirtyBits  = 0;
			}
		}

		private void setDisplayListDirty()	{
            geometryDirtyBits = POINTS_CHANGED | LINES_CHANGED | FACES_CHANGED;
		}

		public SceneGraphComponent getOriginalComponent() {
			return goBetween.getOriginalComponent();
		}

		public void visibilityChanged(SceneGraphComponentEvent ev) {
		}
	}

	private int clearColorBits;
	private GLPbuffer offscreenPBuffer;
	private Buffer offscreenBuffer;

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

	private int tileSizeX=1024, tileSizeY=768,numTiles=4;
	
	public void renderOffscreen(int imageWidth, int imageHeight, File file, GLCanvas canvas) {
		BufferedImage img = renderOffscreen(imageWidth, imageHeight, canvas);
		writeBufferedImage(file, img);
	}

	public static void writeBufferedImage(File file, BufferedImage img) {
		//boolean worked=true;
		System.err.println("Writing to file "+file.getPath());
		if (file.getName().endsWith(".tiff") || file.getName().endsWith(".tif")) {
			try {
				// TODO: !!!
				//worked = ImageIO.write(img, "TIFF", new File(noSuffix+".tiff"));
				Method cm = Class.forName("javax.media.jai.JAI").getMethod("create", new Class[]{String.class, RenderedImage.class, Object.class, Object.class});
				cm.invoke(null, new Object[]{"filestore", img, file.getPath(), "tiff"});
			} catch(Throwable e) {
//				//worked=false;
//				LoggingSystem.getLogger(this).log(Level.CONFIG, "could not write TIFF: "+file.getPath(), e);
				e.printStackTrace();
			}
		} else {
			//if (!worked)
			try {
				String suffix = getFileSuffix(file);
				System.err.println("suffix is "+suffix);
				if (suffix != "")
				    if (!ImageIO.write(img, getFileSuffix(file), file)) {
					    JOGLConfiguration.getLogger().log(Level.WARNING,"Error writing file using ImageIO (unsupported file format?)");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String getFileSuffix(File file) {
		int lastDot = file.getName().lastIndexOf('.');
		if (lastDot == -1) return "png";
		return file.getName().substring(lastDot+1);
	}


	public BufferedImage renderOffscreen(int imageWidth, int imageHeight, GLCanvas canvas) {
		if (!GLDrawableFactory.getFactory().canCreateGLPbuffer()) {
			JOGLConfiguration.getLogger().log(Level.WARNING,"PBuffers not supported");
			return null;
		}

		numTiles = Math.max(imageWidth/1024, imageHeight/1024);
		if (imageWidth % 1024 != 0 ||  imageHeight % 1024 != 0) numTiles ++;
		tileSizeX = imageWidth/numTiles;
		tileSizeY = imageHeight/numTiles;
		imageWidth = (tileSizeX) * numTiles;
		imageHeight = (tileSizeY) * numTiles;
		GLCapabilities caps = new GLCapabilities();
		caps.setDoubleBuffered(false);
		offscreenPBuffer = GLDrawableFactory.getFactory().createGLPbuffer(
				caps, null,
				tileSizeX, tileSizeY,
				canvas.getContext());
		BufferedImage img = null;
		offscreenBuffer = null;
//		imageWidth = numTiles*tileSizeX;
//		imageHeight = numTiles*tileSizeY;
		img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
		offscreenBuffer = ByteBuffer.wrap(((DataBufferByte) img.getRaster().getDataBuffer()).getData());

		offscreenMode = true;
		canvas.display();
		//display(offscreenPBuffer);

		ImageUtil.flipImageVertically(img);

		// force alpha channel to be "pre-multiplied"
		img.coerceData(true);
		return img;
	}

}
