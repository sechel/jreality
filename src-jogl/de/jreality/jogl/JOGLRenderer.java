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

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLORS;

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
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;

import com.sun.opengl.util.ImageUtil;

import de.jreality.jogl.pick.PickPoint;
import de.jreality.jogl.shader.RenderingHintsInfo;
import de.jreality.jogl.shader.Texture2DLoaderJOGL;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.util.CameraUtility;
import de.jreality.util.CopyVisitor;
import de.jreality.util.ImageUtility;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphUtility;

public class JOGLRenderer   {

	public GL globalGL;
	protected SceneGraphComponent theRoot, auxiliaryRoot;

	// peer objects 
	transient protected JOGLPeerComponent thePeerRoot = null,
		thePeerAuxilliaryRoot = null;
	// helper objects
	transient public JOGLRenderingState renderingState;
	transient protected JOGLLightHelper lightHelper;
	transient protected JOGLTopLevelAppearance topAp;
	transient protected JOGLOffscreenRenderer offscreenRenderer;
	transient protected JOGLPerformanceMeter perfMeter;

	transient protected int width, height;		// GLDrawable.getSize() isnt' implemented for GLPBuffer!
	transient protected int whichEye = CameraUtility.MIDDLE_EYE;
	transient protected int[] currentViewport = new int[4];

	transient private final  Logger theLog = LoggingSystem.getLogger(this);
	// software matrix stack
	transient protected final static int MAX_STACK_DEPTH = 28;	// hardware supported
	transient protected Matrix[] matrixStack = new Matrix[128];
	transient protected int stackCounter,
		stackDepth;
	transient protected Stack<RenderingHintsInfo> rhStack = new Stack<RenderingHintsInfo>();

	transient protected boolean texResident = true;
	transient protected int numberTries = 0;		// how many times we have tried to make textures resident
	// miscellaneous fields and methods
	transient protected int clearColorBits;
	// pick-related stuff
	transient protected boolean offscreenMode = false;
	// an exotic mode: render the back hemisphere of the 3-sphere (currently disabled)
	transient protected static double[] frontZBuffer = new double[16], backZBuffer = new double[16];
	private javax.swing.Timer followTimer;

	transient boolean geometryRemoved = false, 
		lightListDirty = true, 
		lightsChanged = true, 
		clippingPlanesDirty = true,
		disposed = false,
		frontBanana = false;
	protected Viewer theViewer;
	Camera theCamera;

	static {
		MatrixBuilder.euclidean().translate(0,0,-.5).scale(1,1,.5).assignTo(frontZBuffer);
		MatrixBuilder.euclidean().translate(0,0,.5).scale(1,1,.5).assignTo(backZBuffer);
		Rn.times(backZBuffer, -1, backZBuffer);
	}
	public JOGLRenderer(Viewer viewer) {
		theViewer=viewer;
		followTimer = new javax.swing.Timer(1000, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {updateGeometryHashtable(); } } );
		followTimer.start();
		//TODO figure out I do this here
		setAuxiliaryRoot(viewer.getAuxiliaryRoot());	
		offscreenRenderer = new JOGLOffscreenRenderer(this);
		perfMeter = new JOGLPerformanceMeter(this);
	}

	public void dispose() {
		disposed = true;
		lightHelper.disposeLights();
		followTimer.setRepeats(false);
		followTimer.stop();
		setSceneRoot(null);
		setAuxiliaryRoot(null);
		Texture2DLoaderJOGL.deleteAllTextures(globalGL);
		if (topAp != null) topAp.dispose();
		LoggingSystem.getLogger(this).info("gobetween table has "+GoBetween.rendererTable.get(this).size());
		LoggingSystem.getLogger(this).info("geom table has "+geometries.size());
	}

	public int getStereoType() {
		return renderingState.stereoType;
	}

	public void setStereoType(int stereoType) {
		renderingState.stereoType = stereoType;
	}

	public Viewer getViewer() {
		return theViewer;
	}

	private void setSceneRoot(SceneGraphComponent sgc) {
		if (topAp != null) {
			topAp.dispose();
		}
		theRoot = sgc;
		if (theRoot != null && theRoot.getAppearance() != null)  {
			topAp = new JOGLTopLevelAppearance(theRoot.getAppearance());

		}
		if (thePeerRoot != null) {
			thePeerRoot.dispose();
			thePeerRoot = null;
		}
		else return;

		theLog.fine("setSceneRoot");
	}

	public SceneGraphComponent getAuxiliaryRoot() {
		return auxiliaryRoot;
	}
	public void setAuxiliaryRoot(SceneGraphComponent auxiliaryRoot) {
		this.auxiliaryRoot = auxiliaryRoot;
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.dispose();
		if (auxiliaryRoot != null) {
			thePeerAuxilliaryRoot = constructPeerForSceneGraphComponent(auxiliaryRoot, null);
		}
	}
	public void render() {
// 		following optimization has been abandoned for now
//		System.err.println("in render "+frameCount);
		if (disposed) return;
		Texture2DLoaderJOGL.markAnimatedTexturesDirty(globalGL);
		if (thePeerRoot == null || theViewer.getSceneRoot() != thePeerRoot.getOriginalComponent())	{
			setSceneRoot(theViewer.getSceneRoot());
			thePeerRoot = constructPeerForSceneGraphComponent(theRoot, null); 
		}
		if (auxiliaryRoot != null && thePeerAuxilliaryRoot == null)
			thePeerAuxilliaryRoot = constructPeerForSceneGraphComponent(auxiliaryRoot, null);

		renderingState.context  = new Graphics3D(theViewer.getCameraPath(), renderingState.currentPath, CameraUtility.getAspectRatio(theViewer));
		globalGL.glMatrixMode(GL.GL_PROJECTION);
		globalGL.glLoadIdentity();

		JOGLRendererHelper.handleBackground(this, width, height, theRoot.getAppearance());

		frontBanana = true;
		renderOnePass();
		if (topAp.isRenderSpherical())	{
			frontBanana = false;
			renderOnePass();
		}
		if (topAp.isForceResidentTextures()) forceResidentTextures();

		lightListDirty = false;
	}


	private void renderOnePass() {
		double aspectRatio = getAspectRatio();
		// for pick mode the aspect ratio has to be set to that of the viewer component
		globalGL.glMatrixMode(GL.GL_PROJECTION);
		globalGL.glLoadIdentity();
		if (topAp.isRenderSpherical())	
		{
			globalGL.glMultTransposeMatrixd(frontBanana ? frontZBuffer : backZBuffer, 0);
//			System.err.println("c2ndc = "+Rn.matrixToString(
//					Rn.times(null, frontBanana ? frontZBuffer : backZBuffer, c2ndc)));
		}
		double[] c2ndc = CameraUtility.getCameraToNDC(theCamera, 
				aspectRatio,
				whichEye);
		globalGL.glMultTransposeMatrixd(c2ndc, 0);

		// prepare for rendering the geometry
		globalGL.glMatrixMode(GL.GL_MODELVIEW);
		globalGL.glLoadIdentity();

		renderingState.cameraToWorld = renderingState.context.getCameraToWorld();
		renderingState.worldToCamera = Rn.inverse(null, renderingState.cameraToWorld);
		globalGL.glMultTransposeMatrixd(renderingState.worldToCamera, 0);
		if (topAp.getSkyboxCubemap() != null) 
			JOGLSkyBox.render(globalGL, 
					renderingState.worldToCamera, 
					topAp.getSkyboxCubemap(), 
					CameraUtility.getCamera(theViewer));

		processLights();

		processClippingPlanes();

		rhStack.clear();
		rhStack.push(RenderingHintsInfo.defaultRHInfo);
		RenderingHintsInfo.defaultRHInfo.render(renderingState, null);
		renderingState.flipped = (Rn.determinant(renderingState.worldToCamera) < 0.0);
//		System.err.println("JOGLR: flipped = "+renderingState.flipped);
		globalGL.glFrontFace(renderingState.flipped ? GL.GL_CW : GL.GL_CCW);

		texResident=true;
		renderingState.currentPath.clear();
		thePeerRoot.render();		
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.render();
		if (topAp.isRenderSpherical() && !frontBanana) globalGL.glPopMatrix();
		globalGL.glLoadIdentity();
	}

	List clipPlanes = null;
	private void processClippingPlanes() {
		if (clipPlanes == null  || clippingPlanesDirty) {
			clipPlanes = SceneGraphUtility.collectClippingPlanes(theRoot);
		}
		JOGLRendererHelper.processClippingPlanes(this, clipPlanes);
		clippingPlanesDirty = false;
	}

	List<SceneGraphPath> lights = null;
	private void processLights( ) {
//		lightsChanged = false;
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
			lightHelper.cacheLightMatrices(lights);
			lightsChanged = false;
		}
		lightHelper.processLights(globalGL, lights);
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


	public double getFramerate()	{
		return perfMeter.getFramerate();
	}

	public double getClockrate()	{
		return perfMeter.getClockrate();
	}

	public int getPolygonCount()	{
		return renderingState.polygonCount;
	}
	
	protected void myglViewport(int lx, int ly, int rx, int ry)	{
		globalGL.glViewport(lx, ly, rx, ry);
		currentViewport[0] = lx;
		currentViewport[1] = ly;
		currentViewport[2] = rx;
		currentViewport[3] = ry;
	}

	/*
	 * Here are the methods from the GLListener interface
	 */
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
		System.err.println("initing gl");
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
//		if (theRoot != null) extractGlobalParameters();
		// traverse tree and delete all display lists
		if (thePeerRoot != null) thePeerRoot.propagateGeometryChanged(JOGLPeerComponent.ALL_GEOMETRY_CHANGED);
		if (thePeerAuxilliaryRoot != null) thePeerAuxilliaryRoot.propagateGeometryChanged(JOGLPeerComponent.ALL_GEOMETRY_CHANGED);
	}
	
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
	}

	public void reshape(GLAutoDrawable arg0,int arg1,int arg2,int arg3,int arg4) {
		globalGL = arg0.getGL();
		width = arg3-arg1;
		height = arg4-arg2;
		myglViewport(0,0, width, height);
	}

	public void display(GLAutoDrawable drawable) {
		if (theViewer.getSceneRoot() == null || theViewer.getCameraPath() == null) {
			theLog.info("display called w/o scene root or camera path");
		}
		display(drawable.getGL());
	}
	protected int[] whichTile = new int[2];
	
	public void display(GL gl) {
		globalGL=gl;
		perfMeter.beginFrame();
		renderingState.initializeGLState();
		renderingState.currentEye = CameraUtility.MIDDLE_EYE;		
		clearColorBits = (renderingState.clearColorBuffer ? GL.GL_COLOR_BUFFER_BIT : 0);
		try {
			theCamera = CameraUtility.getCamera(theViewer);
		} catch (IllegalStateException ise) {
			return;
		}
		if (offscreenMode) {
			if (theCamera.isStereo() && renderingState.stereoType != de.jreality.jogl.Viewer.CROSS_EYED_STEREO) {
				theLog.warning("Invalid stereo mode: Can only save cross-eyed stereo offscreen");
				offscreenMode = false;
				return;
			}
			GLContext context = offscreenRenderer.getOffscreenPBuffer().getContext();
			if (context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT) {
				JOGLConfiguration.getLogger().log(Level.WARNING,"Error making pbuffer's context current");
				offscreenMode = false;
				return;
			}
			globalGL = offscreenRenderer.getOffscreenPBuffer().getGL();
			if (true || !JOGLConfiguration.sharedContexts)  forceNewDisplayLists();
			renderingState.initializeGLState();
			// we need another camera to avoid alerting the listeners when
			// we change the camera while doing the offscreen render
			CopyVisitor copier = new CopyVisitor();
			copier.visit(theCamera);			
			Camera offscreenCamera = theCamera = (Camera) copier.getCopy();
			
			Color[] bg=null;
			float[][] bgColors=null;
			int tileSizeX = offscreenRenderer.getTileSizeX(),
			tileSizeY = offscreenRenderer.getTileSizeY(),
			numTiles = offscreenRenderer.getNumTiles();
			if (numTiles > 1) {
				if (theRoot.getAppearance() != null && theRoot.getAppearance().getAttribute(BACKGROUND_COLORS, Color[].class) != Appearance.INHERITED) {
					bg = (Color[]) theRoot.getAppearance().getAttribute(BACKGROUND_COLORS, Color[].class);
					bgColors=new float[4][];
					bgColors[0]=bg[0].getRGBComponents(null);
					bgColors[1]=bg[1].getRGBComponents(null);
					bgColors[2]=bg[2].getRGBComponents(null);
					bgColors[3]=bg[3].getRGBComponents(null);
				}
			}
			double[] c2ndc = CameraUtility.getCameraToNDC(offscreenCamera, 
					CameraUtility.getAspectRatio(theViewer),
					CameraUtility.MIDDLE_EYE);
//			System.err.println("c2ndc is "+Rn.matrixToString(c2ndc));
			int numImages = offscreenCamera.isStereo() ? 2 : 1;
			tileSizeX = tileSizeX / numImages;
			myglViewport(0,0,tileSizeX, tileSizeY);
			Rectangle2D vp = CameraUtility.getViewport(theCamera, getAspectRatio()); //CameraUtility.getAspectRatio(theViewer));
			double dx = vp.getWidth()/numTiles;
			double dy = vp.getHeight()/numTiles;
			boolean isOnAxis = offscreenCamera.isOnAxis();
			offscreenCamera.setOnAxis(false);
			for (int st = 0; st < numImages; ++st)	{
				if (offscreenCamera.isStereo()) {
					if (st == 0) whichEye = CameraUtility.RIGHT_EYE;
					else whichEye =  CameraUtility.LEFT_EYE;
				}
				else whichEye = CameraUtility.MIDDLE_EYE;
				for (int i = 0; i<numTiles; ++i)	{
					for (int j = 0; j<numTiles; ++j)	{
						whichTile[0] = j; whichTile[1] = i;
						renderingState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
						Rectangle2D lr = new Rectangle2D.Double(vp.getX()+j*dx, vp.getY()+i*dy, dx, dy);
//						System.err.println("Setting vp to "+lr.toString());
						offscreenCamera.setViewPort(lr);
						c2ndc = CameraUtility.getCameraToNDC(offscreenCamera, 
								CameraUtility.getAspectRatio(theViewer),
								CameraUtility.MIDDLE_EYE);
//						System.err.println(i+j+"c2ndc is "+Rn.matrixToString(c2ndc));
						
						if (bgColors != null) {
							Color[] currentBg = new Color[4];
							currentBg[1]=interpolateBG(bgColors, i+1, j, numTiles);
							currentBg[2]=interpolateBG(bgColors, i, j, numTiles);
							currentBg[3]=interpolateBG(bgColors, i, j+1, numTiles);
							currentBg[0]=interpolateBG(bgColors, i+1, j+1, numTiles);
							theRoot.getAppearance().setAttribute(BACKGROUND_COLORS, currentBg);
						}
						
						render();
						if (i == 0 && j == 0) render();
						globalGL.glPixelStorei(GL.GL_PACK_ROW_LENGTH,numImages*numTiles*tileSizeX);
						globalGL.glPixelStorei(GL.GL_PACK_SKIP_ROWS, i*tileSizeY);
						globalGL.glPixelStorei(GL.GL_PACK_SKIP_PIXELS, (st*numTiles+j)*tileSizeX);
						globalGL.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);

						globalGL.glReadPixels(0, 0, tileSizeX, tileSizeY,
								GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, offscreenRenderer.getOffscreenBuffer());
//								GL.GL_RGB, GL.GL_UNSIGNED_BYTE, offscreenBuffer);
					}
				}
				
			}

			if (bgColors != null) theRoot.getAppearance().setAttribute(BACKGROUND_COLORS, bg);
			
			context.release();
			theCamera = CameraUtility.getCamera(theViewer);
			Dimension d = theViewer.getViewingComponentSize();
			myglViewport(0, 0, (int) d.getWidth(), (int) d.getHeight());
			offscreenMode = false;
		} else if (theCamera.isStereo())		{
			setupRightEye(width, height);
			renderingState.currentEye = whichEye;
			render();
			setupLeftEye(width, height);
			renderingState.currentEye = whichEye;
			render();
			renderingState.colorMask =15; //globalGL.glColorMask(true, true, true, true);
		} 
		else {
			renderingState.clearBufferBits = clearColorBits | GL.GL_DEPTH_BUFFER_BIT;
			myglViewport(0,0,width, height);
			whichEye=CameraUtility.MIDDLE_EYE;
			render();			
		}

		perfMeter.endFrame();
	}

	private Color interpolateBG(float[][] bgColors, int i, int j, int numTiles) {
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
		int which = renderingState.stereoType;
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
		int which = renderingState.stereoType;
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
	/**
	 * @deprecated
	 */
	public   GoBetween goBetweenFor(SceneGraphComponent sgc, boolean singlePeer)	{
		return GoBetween.goBetweenFor(this, sgc, singlePeer);
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


	public Graphics3D getContext() {
		return renderingState.context;
	}

	public int[] getCurrentViewport()	{
		return currentViewport;
	}

	public double getAspectRatio() {
		return ((double) currentViewport[2])/currentViewport[3];
	}

	public void renderOffscreen(int imageWidth, int imageHeight, File file, GLAutoDrawable canvas) {
		BufferedImage img = offscreenRenderer.renderOffscreen(imageWidth, imageHeight, canvas);
		ImageUtility.writeBufferedImage(file, img);
	}

	public BufferedImage renderOffscreen(int imageWidth, int imageHeight, GLAutoDrawable canvas) {
		return offscreenRenderer.renderOffscreen(imageWidth, imageHeight, canvas);
	}

	public PickPoint[] performPick(double[] pickPointNDC) {
		throw new IllegalArgumentException("Picking has been removed from JOGL renderer");
	}




}
