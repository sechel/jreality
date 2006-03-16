/*
 * Author	gunn
 * Created on Mar 23, 2005
 *
 */
package de.jreality.jogl;

import java.util.WeakHashMap;

import de.jreality.jogl.JOGLRenderer.JOGLPeerComponent;
import de.jreality.math.Pn;
import de.jreality.scene.Geometry;
import net.java.games.jogl.GL;

/**
 * @author gunn
 *
 */
public class JOGLRenderingState {

	public boolean smoothShading = true;
	public boolean lighting = true;
	public boolean backFaceCullingEnabled = false;
	public boolean flipped = false;
	public boolean transparencyEnabled = false;
	public float[] diffuseColor = new float[4];
	public double lineWidth;
	public int activeTexture;
	public int frontBack = GL.GL_FRONT_AND_BACK;
	public double levelOfDetail;
	public int numLights = 0;
	protected int[] sphereDisplayLists = null;
	protected int[] cylinderDisplayLists = null;
	GL gl = null;
	public JOGLRenderer renderer;
	public int currentSignature = Pn.EUCLIDEAN;
	public boolean currentPickMode = false;
	public Geometry currentGeometry = null;
	public double currentAlpha = 1.0;
	public boolean useDisplayLists=true;
	public boolean clearColorBuffer=true;
	
	
	public JOGLRenderingState(JOGLRenderer jr) {
		super();
		this.renderer = jr;
		gl=jr.globalGL;
	}
	public static boolean equals(float[] a, float[] b, float tol)	{
		int n = a.length;
		for (int i = 0; i<n ; ++i)	if (Math.abs(a[i]-b[i]) > tol) return false;
		return true;
	}

	/**
		 * @param sg
		 */
		// ultimately all this should happen in various visit() methods
		public  void initializeGLState()	{
			// TODO clean this up, provide an interface to set "OpenGL Preferences ..."
			// and make sure everything is here.
			// set drawing color and point size
			gl = renderer.globalGL;
			gl.glColor3f( 0.3f, 0.0f, 0.6f ); 
			gl.glEnable(GL.GL_DEPTH_TEST);							// Enables Depth Testing
			gl.glDepthFunc(GL.GL_LEQUAL);								// The Type Of Depth Testing To Do
			gl.glEnable(GL.GL_ALPHA_TEST);
			gl.glAlphaFunc(GL.GL_GREATER, 0f);				// alpha = 0 gets ignored in fragment shader: cheap transparency
			gl.glClearDepth(1.0f);  
			gl.glEnable(GL.GL_NORMALIZE);
			gl.glEnable(GL.GL_MULTISAMPLE_ARB);	
			gl.glEnable(GL.GL_VERTEX_PROGRAM_TWO_SIDE_ARB);
			gl.glLightModeli(GL.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_FALSE);
			gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
			float[] white = {1f, 1f, 1f, 1f};
			gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, white );
			float[] amb = {0f, 0f, 0f};
			float[] spec = {.5f, .5f, .5f};
			gl.glMaterialfv(frontBack, GL.GL_AMBIENT, amb);
			gl.glMaterialfv(frontBack, GL.GL_DIFFUSE, new float[]{1,0,0});
			gl.glMaterialfv(frontBack, GL.GL_SPECULAR, spec);
			gl.glMaterialf(frontBack, GL.GL_SHININESS, 60f);
			gl.glEnable(GL.GL_COLOR_MATERIAL);
			gl.glColorMaterial(frontBack, GL.GL_DIFFUSE);

			if (smoothShading) gl.glShadeModel(GL.GL_SMOOTH);
			else		gl.glShadeModel(GL.GL_FLAT);
					
			if (flipped) gl.glFrontFace( GL.GL_CW);
			else 		gl.glFrontFace( GL.GL_CCW);
		}
	public int getCylinderDisplayLists(int i) {
		if (cylinderDisplayLists == null) cylinderDisplayLists = JOGLCylinderUtility.getCylinderDLists(renderer);
		return cylinderDisplayLists[i];
	}
	public int getSphereDisplayLists(int i) {
		if (sphereDisplayLists == null) sphereDisplayLists = JOGLSphereHelper.getSphereDLists(renderer);
		return 0;
	}
	public double getCurrentAlpha() {
		return currentAlpha;
	}
	public void setCurrentAlpha(double currentAlpha) {
		this.currentAlpha = currentAlpha;
	}
	public Geometry getCurrentGeometry() {
		return currentGeometry;
	}
	public void setCurrentGeometry(Geometry currentGeometry) {
		this.currentGeometry = currentGeometry;
	}
	public boolean isCurrentPickMode() {
		return currentPickMode;
	}
	public void setCurrentPickMode(boolean currentPickMode) {
		this.currentPickMode = currentPickMode;
	}
	public int getCurrentSignature() {
		return currentSignature;
	}
	public void setCurrentSignature(int currentSignature) {
		this.currentSignature = currentSignature;
	}
	public float[] getDiffuseColor() {
		return diffuseColor;
	}
	public void setDiffuseColor(float[] diffuseColor) {
		this.diffuseColor = diffuseColor;
	}
	public JOGLRenderer getRenderer() {
		return renderer;
	}
	public void setRenderer(JOGLRenderer renderer) {
		this.renderer = renderer;
	}
	public boolean isSmoothShading() {
		return smoothShading;
	}
	public void setSmoothShading(boolean smoothShading) {
		this.smoothShading = smoothShading;
	}
	public boolean isUseDisplayLists() {
		return useDisplayLists;
	}
	public void setUseDisplayLists(boolean useDisplayLists) {
		this.useDisplayLists = useDisplayLists;
	}
	public boolean isClearColorBuffer() {
		return clearColorBuffer;
	}
	public void setClearColorBuffer(boolean clearColorBuffer) {
		this.clearColorBuffer = clearColorBuffer;
	}
}
