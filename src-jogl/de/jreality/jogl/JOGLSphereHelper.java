/*
 * Created on Jan 18, 2005
 *
 */
package de.jreality.jogl;

import java.util.WeakHashMap;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.util.CameraUtility;

/**
 * @author gunn
 *
 */
public class JOGLSphereHelper extends SphereUtility {

	static boolean sharedDisplayLists = JOGLConfiguration.sharedContexts;
	static WeakHashMap sphereDListsTable = new WeakHashMap();
	static int[] globalSharedSphereDisplayLists = null;
	//TODO This can't be static; the display lists so created are invalid if the renderer parameter
	// no longer exists.  So ... these display lists have to be tied to a specific context.
	public static void setupSphereDLists(JOGLRenderer jr)	{
		int[] dlists = null; //getSphereDLists(jr);
//		if (dlists != null)	{
//			JOGLConfiguration.theLog.log(Level.WARNING,"Already have sphere display lists for this renderer "+jr);
//		}
		GL gl = jr.getCanvas().getGL();
		int n = SphereUtility.tessellatedIcosahedra.length;
		dlists = null;
		//if (!sharedDisplayLists)	dlists = (int[] ) sphereDListsTable.get(gl);
		//else 
				dlists = new int[n];
				JOGLConfiguration.theLog.log(Level.INFO,"Setting up sphere display lists for context "+gl);
		for (int i = 0; i<n; ++i)	{
			tessellatedCubeSphere(i);
			dlists[i] = gl.glGenLists(1);
			gl.glNewList(dlists[i], GL.GL_COMPILE);
			//gl.glDisable(GL.GL_SMOOTH);
			QuadMeshShape qms = SphereUtility.cubePanels[i];
			for (int j = 0; j<SphereUtility.cubeSyms.length; ++j)	{
				gl.glPushMatrix();
				gl.glMultTransposeMatrixd(SphereUtility.cubeSyms[j].getMatrix());
				JOGLRendererHelper.drawFaces(qms,jr, true, 1.0);
				gl.glPopMatrix();
			}				
			gl.glEndList();
		}
		if (!sharedDisplayLists) sphereDListsTable.put(jr, dlists);
		else globalSharedSphereDisplayLists = dlists;
	}
	
	/**
	 * @param i
	 * @return
	 */
	public static int getSphereDLists(int i,JOGLRenderer jr) {
		int[] dlists = getSphereDLists(jr);
		if (dlists == null) 	{
			JOGLConfiguration.getLogger().log(Level.WARNING,"Invalid sphere display lists");
			return 0;
		}
		return dlists[i];
	}

	/**
	 * @param i
	 * @return
	 */
	public static int[] getSphereDLists( JOGLRenderer jr) {
		int dlists[];
		//if (!sharedDisplayLists)	
			dlists =  (int[] ) sphereDListsTable.get(jr);
		//else dlists = globalSharedSphereDisplayLists;
		if (dlists == null) 	{
			setupSphereDLists(jr);
			//if (!sharedDisplayLists)	
				dlists = (int[] ) sphereDListsTable.get(jr);
			//else dlists = globalSharedSphereDisplayLists;
		}
		if (dlists == null)	{
			throw new IllegalStateException("Can't make sphere display lists successfully");
		}
		return dlists;
	}

	/**
	 * @param globalGL
	 */
	public static void disposeSphereDLists(JOGLRenderer jr) {
		int[] dlists = getSphereDLists(jr);
		if (dlists == null)	{
			throw new IllegalStateException("No such gl context");
		}
		// probably don't need to actually delete them since the context 
	}


	static double[] lodLevels = {.02,.08,.16,.32,.64};
	/**
	 * @return
	 */
	public static int getResolutionLevel(double[] o2ndc, double lod) {
		double d = lod * getNDCExtent(o2ndc);
		//JOGLConfiguration.theLog.log(Level.FINE,"Distance is "+d);
		int i = 0;
		for ( i = 0; i<5; ++i)	{
			if (d < lodLevels[i]) break;
		}
		return i;
	}

	static double[] m4 = {1,0,0,1,0,1,0,1,0,0,1,1,0,0,0,1};
	/**
	 * @param o2ndc
	 * @return
	 */
	public static double getNDCExtent(double[] o2ndc) {
		double[][] images = new double[4][4];
		Rn.transpose(o2ndc, o2ndc);
		Rn.times(o2ndc, m4, o2ndc);
		for (int i = 0; i<4; ++i)	System.arraycopy(o2ndc, 4*i, images[i], 0, 4);
		Pn.dehomogenize(images, images);
		double d = 0.0;
		for (int i = 0; i<3; ++i)	 {
			double[] tmp = Rn.subtract(null, images[3], images[i]);
			double t = Math.sqrt(Rn.innerProduct(tmp,tmp,2));
			if (t > d) d = t;
		}
		return d;
	}

}
