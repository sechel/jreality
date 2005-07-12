/*
 * Author	gunn
 * Created on Mar 23, 2005
 *
 */
package de.jreality.jogl;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;

/**
 * @author gunn
 *
 */
public class OpenGLState {

	public boolean smoothShading;
	public boolean lighting;
	public boolean backFaceCullingEnabled;
	public boolean flipped;
	public boolean transparencyEnabled;
	public float[] diffuseColor = new float[4];
	public double lineWidth;
	public int activeTexture;
	public int frontBack = GL.GL_FRONT_AND_BACK;
	
	private void render(GL gl)	{
		if (backFaceCullingEnabled)  {
			gl.glEnable(GL.GL_CULL_FACE);
			gl.glCullFace(GL.GL_BACK);
		} else
			gl.glDisable(GL.GL_CULL_FACE);
		if (transparencyEnabled)	{
			  gl.glEnable (GL.GL_BLEND);
			  gl.glDepthMask(false);
			  gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			} else	{
			  gl.glDepthMask(true);
			  gl.glDisable(GL.GL_BLEND);
			}
		if (lighting)			gl.glEnable(GL.GL_LIGHTING);
		else						gl.glDisable(GL.GL_LIGHTING);
		if (smoothShading) gl.glShadeModel(GL.GL_SMOOTH);
		else		gl.glShadeModel(GL.GL_FLAT);
		
		if (flipped) gl.glFrontFace( GL.GL_CCW);
		else 		gl.glFrontFace( GL.GL_CCW);
		gl.glEnable(GL.GL_COLOR_MATERIAL);
		gl.glColorMaterial(frontBack, GL.GL_DIFFUSE);
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
		public  static void initializeGLState(JOGLRenderer jr)	{
			GLCanvas theCanvas = jr.getCanvas();
			GL gl = theCanvas.getGL();
			OpenGLState openGLState = jr.openGLState;
			// set drawing color and point size
			gl.glColor3f( 0.3f, 0.0f, 0.6f ); 
			gl.glEnable(GL.GL_DEPTH_TEST);							// Enables Depth Testing
			gl.glDepthFunc(GL.GL_LEQUAL);								// The Type Of Depth Testing To Do
			gl.glEnable(GL.GL_ALPHA_TEST);
			gl.glAlphaFunc(GL.GL_GREATER, 0f);
			gl.glClearDepth(1.0f);  
			gl.glEnable(GL.GL_NORMALIZE);
			gl.glEnable(GL.GL_MULTISAMPLE_ARB);	
			gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
			float[] white = {1f, 1f, 1f, 1f};
			gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, white );
			openGLState.render(gl);
		}
		// for converting double arrays to native buffers:
	//	static ByteBuffer bb = ByteBuffer.allocateDirect(444/* array.length*8 */).order(ByteOrder.nativeOrder());
	//	bb.asDoubleBuffer().put(array);
	//	bb.flip();
	//	
	//	gl.glVertexPointer(3, GL.GL_DOUBLE, 8*3, bb);
		// can re-use after checking that it's long enough, use some method to reallocate
}
