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

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JOGLSphereHelper extends SphereHelper {

	static boolean useQuadMesh = true;
	static boolean sharedDisplayLists = true;
	static Hashtable sphereDListsTable = new Hashtable();
	static int[] sphereDLists = null;
	//TODO This can't be static; the display lists so created are invalid if the renderer parameter
	// no longer exists.  So ... these display lists have to be tied to a specific context.
	public static void setupSphereDLists(GL gl)	{
		int n = SphereHelper.spheres.length;
		int[] dlists = null;
		if (!sharedDisplayLists)	dlists = (int[] ) sphereDListsTable.get(gl);
		else dlists = new int[n];
		//dps = new DefaultPolygonShader();
//		EffectiveAppearance eap = EffectiveAppearance.create();
//		eap = eap.create(SphereHelper.pointAsSphereAp);
//		dps =ShaderLookup.getPolygonShaderAttr(eap, "", CommonAttributes.POLYGON_SHADER);		
		System.out.println("Setting up sphere display lists");
		for (int i = 0; i<n; ++i)	{
			dlists[i] = gl.glGenLists(1);
			gl.glNewList(dlists[i], GL.GL_COMPILE);
			if (useQuadMesh) {
				QuadMeshShape qms = SphereHelper.cubePanels[i];
				for (int j = 0; j<SphereHelper.cubeSyms.length; ++j)	{
					gl.glPushMatrix();
					gl.glMultTransposeMatrixd(SphereHelper.cubeSyms[j].getMatrix());
					JOGLRendererHelper.drawFaces(qms, gl, false, true, 1.0);
					gl.glPopMatrix();
				}				
			} else {
				JOGLRendererHelper.drawFaces(SphereHelper.spheres[i], gl, false, true, 1.0);
			}
			gl.glEndList();
		}
		if (!sharedDisplayLists) sphereDListsTable.put(gl, dlists);
		else sphereDLists = dlists;
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
		if (!sharedDisplayLists)	dlists =  getSphereDLists(gl);
		else dlists = sphereDLists;
		if (dlists == null) 	{
			setupSphereDLists(gl);
			if (!sharedDisplayLists)	dlists = (int[] ) sphereDListsTable.get(gl);
			else dlists = sphereDLists;
		}
		return dlists;
	}

}
