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
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.ImageUtil;

import de.jreality.jogl.pick.Graphics3D;
import de.jreality.jogl.pick.PickPoint;
import de.jreality.jogl.shader.Texture2DLoaderJOGL;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.ImageUtility;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphUtility;
/**
 * @author gunn
 *
 */
public class JOGLRenderer  implements AppearanceListener {

	private final  Logger theLog = LoggingSystem.getLogger(this);
	private static boolean collectFrameRate = true;
	protected final static int MAX_STACK_DEPTH = 28;
	protected int stackDepth;
    protected Matrix[] matrixStack = new Matrix[128];
    protected int stackCounter = 0;

	SceneGraphPath currentPath = new SceneGraphPath();

	protected SceneGraphComponent theRoot, auxiliaryRoot;
	protected JOGLPeerComponent thePeerRoot = null;
	protected JOGLPeerComponent thePeerAuxilliaryRoot = null;
	protected JOGLRenderingState renderingState;
	protected JOGLLightHelper lightHelper;

	protected int width, height;		// GLDrawable.getSize() isnt' implemented for GLPBuffer!
	protected int whichEye = CameraUtility.MIDDLE_EYE;
	protected int[] currentViewport = new int[4];
	protected Graphics3D context;

	protected GL globalGL;

	protected boolean texResident = true;
	protected int numberTries = 0;		// how many times we have tried to make textures resident
	protected boolean forceResidentTextures = true;
	protected boolean oneTexture2DPerImage = false;
	protected boolean globalIsReflection = false;

	// pick-related stuff
	public boolean pickMode = false, offscreenMode = false;
	protected final double pickScale = 10000.0;
	protected Transformation pickT = new Transformation();
	protected PickPoint[] hits;
	// an exotic mode: render the back hemisphere of the 3-sphere (currently disabled)
	protected boolean backSphere = false;
	protected double framerate;
	protected int nodeCount = 0;

	boolean geometryRemoved = false, lightListDirty = true, lightsChanged = true, clippingPlanesDirty = true;
	protected Viewer theViewer;

	protected int stereoType;
	protected boolean flipped;

	public JOGLRenderer(Viewer viewer) {
		theViewer=viewer;
		javax.swing.Timer followTimer = new javax.swing.Timer(1000, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {updateGeometryHashtable(); } } );
		followTimer.start();
		setAuxiliaryRoot(viewer.getAuxiliaryRoot());	
		// have to make sure JOGLConfiguration is initialized before we do anything else
		JOGLConfiguration.getLogger(); 
	}


	public GL getGL() {
		return globalGL;
	}

	public JOGLRenderingState getRenderingState() {
		return renderingState;
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
		if (thePeerRoot != null) thePeerRoot.dispose();
		thePeerRoot = null;
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
		obj = ap.getAttribute(CommonAttributes.ONE_TEXTURE2D_PER_IMAGE, Boolean.class);		// assume the best ...
		if (obj instanceof Boolean) oneTexture2DPerImage = ((Boolean)obj).booleanValue();
		theLog.info("one texture per image: "+oneTexture2DPerImage);
		obj = ap.getAttribute(CommonAttributes.CLEAR_COLOR_BUFFER, Boolean.class);		// assume the best ...
		if (obj instanceof Boolean) {
			renderingState.clearColorBuffer = ((Boolean)obj).booleanValue();
			theLog.fine("Setting clear color buffer to "+renderingState.clearColorBuffer);
		}
		obj = ap.getAttribute(CommonAttributes.USE_OLD_TRANSPARENCY, Boolean.class);		
		// a bit ugly: we make this a static variable so shaders can access it easily
		if (obj instanceof Boolean) JOGLRenderingState.useOldTransparency = ((Boolean)obj).booleanValue();
		theLog.info("forceResTex = "+forceResidentTextures);
//		theLog.info("component display lists = "+renderingState.componentDisplayLists);
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
    protected Runnable testClean = null;
	public Object render() {
		Texture2DLoaderJOGL.setupBoundTextureTable(globalGL, oneTexture2DPerImage);
		if (thePeerRoot == null || theViewer.getSceneRoot() != thePeerRoot.getOriginalComponent())	{
			setSceneRoot(theViewer.getSceneRoot());
			thePeerRoot = constructPeerForSceneGraphComponent(theRoot, null); 
			theLog.finer("Creating peer scenegraph");
		}
		if (auxiliaryRoot != null && thePeerAuxilliaryRoot == null)
			thePeerAuxilliaryRoot = constructPeerForSceneGraphComponent(auxiliaryRoot, null);

		context  = new Graphics3D(theViewer.getCameraPath(), currentPath, CameraUtility.getAspectRatio(theViewer));
		//context.setCurrentPath(currentPath);
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
		renderingState.cameraToWorld = context.getCameraToWorld();
		double[] w2c = Rn.inverse(null, renderingState.cameraToWorld);
		globalGL.glLoadTransposeMatrixd(w2c, 0);
		globalIsReflection = ( isFlipped() != (Rn.determinant(w2c) < 0.0));

		JOGLRendererHelper.handleSkyBox(this, theRoot.getAppearance(),CameraUtility.getCamera(theViewer) );

		if (!pickMode) processLights();

		processClippingPlanes();

		nodeCount = renderingState.polygonCount = 0;			// for profiling info
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

	List clipPlanes = null;
	private void processClippingPlanes() {
		if (clipPlanes == null  || clippingPlanesDirty) {
			clipPlanes = SceneGraphUtility.collectClippingPlanes(theRoot);
		}
		JOGLRendererHelper.processClippingPlanes(globalGL, clipPlanes);
		clippingPlanesDirty = false;
	}

	List<SceneGraphPath> lights = null;
	private void processLights( ) {
		lightsChanged = true;
		if (lights == null || lights.size() == 0 || lightListDirty) {
			lightHelper.disposeLights();
			lights = SceneGraphUtility.collectLights(theRoot);
			lightHelper.resetLights(globalGL, lights);
			lightListDirty = false;
			renderingState.numLights = lights.size();
			lightsChanged = true;
		}
		lightHelper.enableLights(globalGL, lights.size());
		if (lightsChanged) {
			lightHelper.processLights(globalGL, lights);
			lightsChanged = false;
		}
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

	public int getPolygonCount()	{
		return renderingState.polygonCount;
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
		renderingState.currentPickMode = true;
		// TODO!!!
		//theCanvas.display();	// this calls our display() method  directly
		return hits;
	}

	protected void myglViewport(int lx, int ly, int rx, int ry)	{
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
	
		renderingState = new JOGLRenderingState(this);
		lightHelper = new JOGLLightHelper(this);
		String vv = globalGL.glGetString(GL.GL_VERSION);
		theLog.log(Level.FINE,"new GL: "+gl);			
		theLog.log(Level.FINE,"version: "+vv);			
		lightsChanged = true;
		Texture2DLoaderJOGL.deleteAllTextures(globalGL);
		JOGLCylinderUtility.setupCylinderDLists(this);
		JOGLSphereHelper.setupSphereDLists(this);
		if (theRoot != null) extractGlobalParameters();
		// traverse tree and delete all display lists
		if (thePeerRoot != null) thePeerRoot.propagateGeometryChanged(JOGLPeerComponent.ALL_GEOMETRY_CHANGED);
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.propagateGeometryChanged(JOGLPeerComponent.ALL_GEOMETRY_CHANGED);
	}
	
	public void display(GLAutoDrawable drawable) {
		if (theViewer.getSceneRoot() == null || theViewer.getCameraPath() == null) {
			theLog.info("display called w/o scene root or camera path");
		}
		display(drawable.getGL());
	}

	public void display(GL gl) {
		globalGL=gl;
		renderingState.initializeGLState();
		long beginTime = 0;
		if (collectFrameRate) beginTime = System.currentTimeMillis();
		Camera theCamera;
		try {
			theCamera = CameraUtility.getCamera(theViewer);
		} catch (IllegalStateException ise) {
			// no camera (yet):
			return;
		}
		clearColorBits = (renderingState.clearColorBuffer ? GL.GL_COLOR_BUFFER_BIT : 0);
//		theLog.fine("JOGLRR ccb = "+clearColorBits);
		if (offscreenMode) {
			if (theCamera.isStereo() && getStereoType() != de.jreality.jogl.Viewer.CROSS_EYED_STEREO) {
				theLog.warning("Invalid stereo mode: Can only save cross-eyed stereo offscreen");
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
			renderingState.initializeGLState();

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
			double[] c2ndc = CameraUtility.getCameraToNDC(CameraUtility.getCamera(theViewer), 
					CameraUtility.getAspectRatio(theViewer),
					CameraUtility.MIDDLE_EYE);
			System.err.println("c2ndc is "+Rn.matrixToString(c2ndc));
			int numImages = theCamera.isStereo() ? 2 : 1;
			tileSizeX = tileSizeX / numImages;
			myglViewport(0,0,tileSizeX, tileSizeY);
			Rectangle2D vp = CameraUtility.getViewport(theCamera, getAspectRatio()); //CameraUtility.getAspectRatio(theViewer));
			double dx = vp.getWidth()/numTiles;
			double dy = vp.getHeight()/numTiles;
			boolean perspective = theCamera.isPerspective();
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
						renderingState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
						Rectangle2D lr = new Rectangle2D.Double(vp.getX()+j*dx, vp.getY()+i*dy, dx, dy);
						System.err.println("Setting vp to "+lr.toString());
						theCamera.setViewPort(lr);
						c2ndc = CameraUtility.getCameraToNDC(CameraUtility.getCamera(theViewer), 
								CameraUtility.getAspectRatio(theViewer),
								CameraUtility.MIDDLE_EYE);
						System.err.println(i+j+"c2ndc is "+Rn.matrixToString(c2ndc));
						
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
			renderingState.colorMask =15; //globalGL.glColorMask(true, true, true, true);
		} 
		else {
			renderingState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
			myglViewport(0,0,width, height);
			whichEye=CameraUtility.MIDDLE_EYE;
			if (pickMode)	{
				myglViewport(0,0, 2,2);
				IntBuffer selectBuffer = BufferUtil.newIntBuffer(bufsize);
				//JOGLConfiguration.theLog.log(Level.INFO,"Picking "+frameCount);
				double[] pp3 = new double[3];
				pp3[0] = -pickScale * pickPoint[0]; pp3[1] = -pickScale * pickPoint[1]; pp3[2] = 0.0;
				MatrixBuilder.euclidean().translate(pp3).scale(pickScale, pickScale, 1.0).assignTo(pickT);
				thePeerRoot.propagateGeometryChanged(JOGLPeerComponent.ALL_GEOMETRY_CHANGED);
				globalGL.glSelectBuffer(bufsize, selectBuffer);		
				globalGL.glRenderMode(GL.GL_SELECT);
				globalGL.glInitNames();
				globalGL.glPushName(0);
				render();
				pickMode = false;
				thePeerRoot.propagateGeometryChanged(JOGLPeerComponent.ALL_GEOMETRY_CHANGED);
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

	protected void setupRightEye(int width, int height) {
		int which = getStereoType();
		switch(which)	{
		case de.jreality.jogl.Viewer.CROSS_EYED_STEREO:
			renderingState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
			//globalGL.glClear (clearColorBits | GL.GL_DEPTH_BUFFER_BIT);
			int w = width/2;
			int h = height;
			myglViewport(0,0, w,h);
			break;

		case de.jreality.jogl.Viewer.RED_BLUE_STEREO:
		case de.jreality.jogl.Viewer.RED_CYAN_STEREO:
		case de.jreality.jogl.Viewer.RED_GREEN_STEREO:
			myglViewport(0,0, width, height);
			renderingState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
			if (which == de.jreality.jogl.Viewer.RED_GREEN_STEREO) renderingState.colorMask = 10; //globalGL.glColorMask(false, true, false, true);
			else if (which == de.jreality.jogl.Viewer.RED_BLUE_STEREO) renderingState.colorMask = 12; //globalGL.glColorMask(false, false, true, true);
			else if (which == de.jreality.jogl.Viewer.RED_CYAN_STEREO) renderingState.colorMask = 14; //globalGL.glColorMask(false, true, true, true);
			break;

		case de.jreality.jogl.Viewer.HARDWARE_BUFFER_STEREO:
			myglViewport(0,0, width, height);
			renderingState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
			globalGL.glDrawBuffer(GL.GL_BACK_RIGHT);
			break;			
		}
		whichEye=CameraUtility.RIGHT_EYE;
	}

	protected void setupLeftEye(int width, int height) {
		int which = getStereoType();
		switch(which)	{
		case de.jreality.jogl.Viewer.CROSS_EYED_STEREO:
			int w = width/2;
			int h = height;
			renderingState.clearBufferBits = 0;
			myglViewport(w, 0, w,h);
			break;

		case de.jreality.jogl.Viewer.RED_BLUE_STEREO:
		case de.jreality.jogl.Viewer.RED_CYAN_STEREO:
		case de.jreality.jogl.Viewer.RED_GREEN_STEREO:
			renderingState.colorMask = 9; //globalGL.glColorMask(true, false, false, true);
			renderingState.clearBufferBits = GL.GL_DEPTH_BUFFER_BIT;
			break;

		case de.jreality.jogl.Viewer.HARDWARE_BUFFER_STEREO:
			globalGL.glDrawBuffer(GL.GL_BACK_LEFT);
			renderingState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
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


	static Class<? extends GoBetween> gbClass = GoBetween.class;
	public static void setGoBetweenClass(Class<? extends GoBetween> c)	{
		gbClass = c; 
	}

//	static {
//		try {
//			String foo = Secure.getProperty("jreality.jogl.goBetweenClass");  //TODO: move to de.jreality.util.SystemProperties
//			if (foo != null)
//				try {
//					gbClass = (Class<? extends GoBetween>) Class.forName(foo);
//				} catch (ClassNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		} catch (AccessControlException e) {
//			e.printStackTrace();
//		} catch(SecurityException se)	{
//			LoggingSystem.getLogger(JOGLRenderer.class).warning("Security exception in setting configuration options");
//		}	
//	}
	WeakHashMap<SceneGraphComponent, GoBetween> goBetweenTable = new WeakHashMap<SceneGraphComponent, GoBetween>();
	public   GoBetween goBetweenFor(SceneGraphComponent sgc)	{
		if (sgc == null) return null;
		GoBetween gb = null;
		Object foo = goBetweenTable.get(sgc);
		if (foo == null)	{
			try {
				gb = gbClass.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //new GoBetween(); //sgc, this);
			gb.init(sgc, this);
			goBetweenTable.put(sgc, gb);
			return gb;
		}
		return ((GoBetween) foo);
	}


	int geomDiff = 0;
	WeakHashMap<Geometry, JOGLPeerGeometry> geometries = new WeakHashMap<Geometry, JOGLPeerGeometry>();
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
	public  JOGLPeerGeometry getJOGLPeerGeometryFor(Geometry g)	{
		JOGLPeerGeometry pg;
		synchronized(geometries)	{
			pg = (JOGLPeerGeometry) geometries.get(g);
			if (pg != null) return pg;
			pg = new JOGLPeerGeometry(g, this);
			geometries.put(g, pg);			
		}
		return pg;
	}

	protected JOGLPeerComponent constructPeerForSceneGraphComponent(final SceneGraphComponent sgc, final JOGLPeerComponent p) {
		if (sgc == null) return null;
		final JOGLPeerComponent[] peer = new JOGLPeerComponent[1];
		ConstructPeerGraphVisitor constructPeer = new ConstructPeerGraphVisitor( sgc, p, this);
		peer[0] = (JOGLPeerComponent) constructPeer.visit();
		return peer[0];
	}

//	public WeakHashMap<Geometry, Integer> 
//		pointDisplayLists = new WeakHashMap<Geometry, Integer>(),
//		lineDisplayLists = new WeakHashMap<Geometry, Integer>(),
//		polygonDisplayLists = new WeakHashMap<Geometry, Integer>(),
//		pointProxyDisplayLists = new WeakHashMap<Geometry, Integer>(),
//		lineProxyDisplayLists = new WeakHashMap<Geometry, Integer>(),
//		polygonProxyDisplayLists = new WeakHashMap<Geometry, Integer>();
//	
//	public WeakHashMap[] maps = {pointDisplayLists,
//			lineDisplayLists,
//			polygonDisplayLists,
//			pointProxyDisplayLists,
//			lineProxyDisplayLists,
//			polygonProxyDisplayLists
//	};
//	
//	public void removeGeometryDisplayLists(Geometry g)	{
//		for (WeakHashMap<Geometry, Integer> whm : maps)	{
//			Integer dlist = whm.get(g);
//			if (dlist != null) {
//				globalGL.glDeleteLists(dlist.intValue(), 0);
//				whm.remove(g);
//			}			
//		}
//	}
//	
//	public void clearGeometryDisplayLists()	{
//		for (WeakHashMap<Geometry, Integer> whm : maps)	{
//			whm.clear();
//		}
//		
//	}
	// miscellaneous fields and methods
	protected int clearColorBits;
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
		ImageUtility.writeBufferedImage(file, img);
	}

	public BufferedImage renderOffscreen(int imageWidth, int imageHeight, GLCanvas canvas) {
		if (!GLDrawableFactory.getFactory().canCreateGLPbuffer()) {
			JOGLConfiguration.getLogger().log(Level.WARNING,"PBuffers not supported");
			return null;
		}
		lightsChanged = true;
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
