/*
 * Created on Jan 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl;

import java.util.Hashtable;

import net.java.games.jogl.GL;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.SphereHelper;
import de.jreality.util.CameraUtility;
import de.jreality.util.Pn;
import de.jreality.util.Rn;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JOGLSphereHelper extends SphereHelper {

	static boolean sharedDisplayLists = Viewer.sharedContexts;
	static Hashtable sphereDListsTable = new Hashtable();
	static int[] globalSharedSphereDisplayLists = null;
	//TODO This can't be static; the display lists so created are invalid if the renderer parameter
	// no longer exists.  So ... these display lists have to be tied to a specific context.
	public static void setupSphereDLists(GL gl)	{
		int n = SphereHelper.spheres.length;
		int[] dlists = null;
		//if (!sharedDisplayLists)	dlists = (int[] ) sphereDListsTable.get(gl);
		//else 
				dlists = new int[n];
		System.out.println("Setting up sphere display lists for context "+gl);
		for (int i = 0; i<n; ++i)	{
			dlists[i] = gl.glGenLists(1);
			gl.glNewList(dlists[i], GL.GL_COMPILE);
			//gl.glDisable(GL.GL_SMOOTH);
			QuadMeshShape qms = SphereHelper.cubePanels[i];
			for (int j = 0; j<SphereHelper.cubeSyms.length; ++j)	{
				gl.glPushMatrix();
				gl.glMultTransposeMatrixd(SphereHelper.cubeSyms[j].getMatrix());
				JOGLRendererHelper.drawFaces(qms, gl, true, 1.0);
				gl.glPopMatrix();
			}				
			gl.glEndList();
		}
		if (!sharedDisplayLists) sphereDListsTable.put(gl, dlists);
		else globalSharedSphereDisplayLists = dlists;
	}
	
	/**
	 * @param i
	 * @return
	 */
	public static int getSphereDLists(int i, GL gl) {
		int[] dlists = getSphereDLists(gl);
		if (dlists == null) 	{
			System.err.println("Invalid sphere display lists");
			return 0;
		}
		return dlists[i];
	}

	/**
	 * @param i
	 * @return
	 */
	public static int[] getSphereDLists( GL gl) {
		int dlists[];
		if (!sharedDisplayLists)	dlists =  (int[] ) sphereDListsTable.get(gl);
		else dlists = globalSharedSphereDisplayLists;
		if (dlists == null) 	{
			setupSphereDLists(gl);
			if (!sharedDisplayLists)	dlists = (int[] ) sphereDListsTable.get(gl);
			else dlists = globalSharedSphereDisplayLists;
		}
		return dlists;
	}

	/**
	 * @param globalGL
	 */
	public static void disposeSphereDLists(GL gl) {
		int[] dlists = getSphereDLists(gl);
		if (dlists == null)	{
			System.out.println("disposeSphereDLists: No such context "+gl);
			return;
		}
		// probably don't need to actually delete them since the context 
	}


	static double[] lodLevels = {.02,.08,.16,.32,.64};
	/**
	 * @return
	 */
	public static int getResolutionLevel(double[] o2ndc, double lod) {
		double d = lod * CameraUtility.getNDCExtent(o2ndc);
		//System.out.println("Distance is "+d);
		int i = 0;
		for ( i = 0; i<5; ++i)	{
			if (d < lodLevels[i]) break;
		}
		return i;
	}

}
