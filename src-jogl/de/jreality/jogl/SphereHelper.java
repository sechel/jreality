/*
 * Created on Jul 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.Rn;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SphereHelper {

	/**
	 * 
	 */
	private SphereHelper() {
		super();
	}
	
	public static IndexedFaceSet[] spheres = new IndexedFaceSet[5];
	public static IndexedFaceSet SPHERE_COARSE, SPHERE_FINE, SPHERE_FINER, SPHERE_FINEST;
	
	static {
		for (int i = 0; i<4; ++i)	{
			if (i == 0)		spheres[0] = Primitives.icosahedron();
			else {
				spheres[i] = GeometryUtility.binaryRefine(spheres[i-1]);
				double[][] verts = spheres[i].getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
				int vlength = GeometryUtility.getVectorLength(spheres[i]);
				Rn.normalize(verts, verts);
				spheres[i].setVertexAttributes(Attribute.COORDINATES, new DataList(StorageModel.DOUBLE_ARRAY.array(vlength), verts));
			}
			spheres[i].setVertexAttributes(Attribute.NORMALS, spheres[i].getVertexAttributes(Attribute.COORDINATES)); 
			GeometryUtility.calculateAndSetFaceNormals(spheres[i]);
			spheres[i].buildEdgesFromFaces();			
		}
		SPHERE_COARSE = spheres[0];
		SPHERE_FINE = spheres[1];
		SPHERE_FINER = spheres[2];
		SPHERE_FINEST = spheres[3];
	}

	static Appearance pointAsSphereAp = null;
	static	{
		pointAsSphereAp = new Appearance();
		pointAsSphereAp.setAttribute(CommonAttributes.FACE_DRAW, true);
		pointAsSphereAp.setAttribute(CommonAttributes.EDGE_DRAW, false);
		pointAsSphereAp.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		pointAsSphereAp.setAttribute(CommonAttributes.SMOOTH_SHADING, true);
		pointAsSphereAp.setAttribute(CommonAttributes.FACE_NORMALS, false);
		//pointAsSphereAp.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
	}
	
	static IndexedFaceSet sphereAsTriangStrip = null;
	/*
	static	{
		sphereAsTriangStrip = new IndexedFaceSet(102, 10);
		double[][] verts = new double[102][3];
		int[][] indices = new int[10][];
		
		int count = 0, count2;
		double theta = 0, phi = 0;
		for (int i = 0; i<5; ++i)		{
			phi = (Math.PI/2.0 ) * (i/5.0);
			double cp = Math.cos(phi);
			double sp = Math.sin(phi);
			int lim = 4 * (5-i);
			indices[i] = new int[lim];
			indices[9-i] = new int[lim];
			for (int j = 0; j < lim; ++j)	{
				theta = (Math.PI * 2.0  * j)/lim;
				double ct = Math.cos(theta);
				double st = Math.sin(theta);
				verts[count] = new double[] {cp * ct, cp * st, sp};
				
				count++;
			}
		}
		verts[count++] = new double[] {0,0,1};
		System.err.println("vertex in n hemisphere: "+count);
		for (int i = 20; i<count; ++i)		{
			Rn.copy(verts[i+count-20], verts[i]);
			verts[i+count][2] *= -1.0;
		}
		for (int i = 0; i<5; ++i)	{
		}
	}
	*/
}
