/*
 * Created on Jul 5, 2004
 *
 */
package de.jreality.geometry;

import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rectangle3D;

/**
 * @author gunn
 *
 */
public class SphereUtility {

	/**
	 * 
	 */
	protected SphereUtility() {
		super();
	}
	protected static int numberOfTessellatedCubes = 16;
	protected static int numberOfTessellatedIcosahedra = 6;
	protected static IndexedFaceSet[] tessellatedIcosahedra = new IndexedFaceSet[numberOfTessellatedIcosahedra];
	protected static SceneGraphComponent[] tessellatedCubes = new SceneGraphComponent[numberOfTessellatedCubes];
	public static int SPHERE_COARSE=0, SPHERE_FINE=1, SPHERE_FINER=2, SPHERE_FINEST=3, SPHERE_SUPERFINE=4, SPHERE_WAYFINE=5;
	protected static IndexedFaceSet SPHERE_BOUND;
	protected static Rectangle3D sphereBB = null;
	protected static Transformation[] cubeSyms = null;
	protected static IndexedFaceSet[] cubePanels = new IndexedFaceSet[numberOfTessellatedCubes];
	/**
	 * TODO add a flag to allow a non-shared copy of the geometry
	 */
	
	public static IndexedFaceSet tessellatedIcosahedronSphere(int i)	{
		return tessellatedIcosahedronSphere(i, false);
	}
	
	public static IndexedFaceSet tessellatedIcosahedronSphere(int i, boolean sharedInstance)	{
		if (i<0 || i >= numberOfTessellatedIcosahedra) {
			LoggingSystem.getLogger(SphereUtility.class).warning("Invalid index");
			if (i<0) i = 0; 
			else i = numberOfTessellatedIcosahedra-1;
		}
		if (tessellatedIcosahedra[i] == null)	{
			if (i == 0)	{
				tessellatedIcosahedra[i] = Primitives.icosahedron();
			} else {
				tessellatedIcosahedra[i] = IndexedFaceSetUtility.binaryRefine(tessellatedIcosahedronSphere(i-1, true));
				double[][] verts = tessellatedIcosahedra[i].getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
				int vlength = GeometryUtility.getVectorLength(tessellatedIcosahedra[i]);
				Rn.normalize(verts, verts);
				tessellatedIcosahedra[i].setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
			}
			tessellatedIcosahedra[i].setVertexAttributes(Attribute.NORMALS, tessellatedIcosahedra[i].getVertexAttributes(Attribute.COORDINATES)); 
			GeometryUtility.calculateAndSetFaceNormals(tessellatedIcosahedra[i]);
			IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(tessellatedIcosahedra[i]);			
		}
		if (sharedInstance) return tessellatedIcosahedra[i];
		// TODO need a method to copy IndexedFaceSets
		IndexedFaceSet ifs = tessellatedIcosahedra[i];
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setFaceCount(ifs.getNumFaces());
		ifsf.setFaceIndices(ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null));
		ifsf.setVertexCount(ifs.getNumPoints());
		ifsf.setVertexCoordinates(ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null));
		ifsf.setVertexNormals(ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(null));
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}
	
	/**
	 * TODO add a flag to allow a non-shared copy of the geometry
	 */
	public static SceneGraphComponent tessellatedCubeSphere(int i)	{
		if (i<0 || i >= numberOfTessellatedCubes) {
			LoggingSystem.getLogger(SphereUtility.class).warning("Invalid index");
			if (i<0) i = 0; 
			else i = numberOfTessellatedCubes-1;
		}
		if (cubeSyms == null)	{
			cubeSyms = new Transformation[2];
			cubeSyms[0] = new Transformation();
			cubeSyms[1] = new Transformation( new double[] {-1,0,0,0,  0,0,1,0,  0,1,0,0, 0,0,0,1});
		}
		if (tessellatedCubes[i] == null)	{
			cubePanels[i] = oneHalfSphere(2*i+2);//uvPanel(0.0, 0.0,270.0, 90.0, 6*i+4, 2*i+2, 1.0);
			tessellatedCubes[i] = new SceneGraphComponent();
			for (int j = 0; j<2; ++j)	{
				SceneGraphComponent sgc = new SceneGraphComponent();
				sgc.setTransformation(cubeSyms[j]);
				sgc.setGeometry(cubePanels[i]);
				tessellatedCubes[i].addChild(sgc);
			}
		}
		return tessellatedCubes[i];
	}
	
	public static Rectangle3D getSphereBoundingBox()	{
		if (sphereBB == null)	{
			double[][] bnds = {{-1d,-1d,-1d},{1d,1d,1d}};
			sphereBB = new Rectangle3D();
			sphereBB.setBounds(bnds);	
		}
		return sphereBB;
	}
	

	/**
	 * 
	 * @param cU
	 * @param cV
	 * @param uSize
	 * @param vSize
	 * @param n
	 * @param m
	 * @param r
	 * @return
	 */
	public static IndexedFaceSet sphericalPatch(double cU, double cV, double uSize, double vSize, int xDetail, int yDetail, double radius)	{
		double factor = Math.PI/180.0;
		double uH = uSize/2.0; double vH = vSize/2.0;
		//Globe qms = new Globe(n, m, false, false, factor*(cU-uH), factor*(cU+uH), factor*(cV-vH), factor*(cV+vH), r);
		//Globe qms = new Globe(n, m, false, false, 
		double umin = factor*(cU-uH), umax = factor*(cU+uH), vmin = factor*(cV-vH), vmax= factor*(cV+vH);
		AbstractQuadMeshFactory qmf = new AbstractQuadMeshFactory(xDetail, yDetail, false, false);
		double du = umax - umin;
		double dv = vmax - vmin;
		du = du/(xDetail-1.0);
		dv = dv/(yDetail-1.0);
		double[] points = new double[xDetail*yDetail*3];
		double x,y, cu, cv, su, sv;
		int index;
		for (int i = 0; i< yDetail; ++i)	{
			y = vmin + i*dv;
			for (int j = 0; j<xDetail; ++j)	{
				index  = 3*(i*xDetail + j);
				x = umin+j*du;
				cu = Math.cos(x);
				su = Math.sin(x);
				cv = Math.cos(-y);
				sv = Math.sin(-y);
				points[index] = radius * cu * cv;
				points[index+1] = radius * su*cv;
				points[index+2] = radius * sv;
			}
		}
		qmf.setVertexCoordinates(points);
		qmf.setVertexNormals(points);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateTextureCoordinates(true);
		qmf.update();
		return qmf.getIndexedFaceSet();
//		qms.setVetexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE3_INLINED.createReadOnly(points));
//		qms.buildEdgesFromFaces();
//		qms.setVertexAttributes(Attribute.NORMALS, qms.getVertexAttributes(Attribute.COORDINATES));
//		GeometryUtility.calculateAndSetFaceNormals(qms);
//		GeometryUtility.calculateAndSetTextureCoordinates(qms);
//		return  qms;
	}
	
	public static IndexedFaceSet oneHalfSphere( int n)	{
		AbstractQuadMeshFactory qmf = new AbstractQuadMeshFactory(3*n-2,n,false, false);
		double[][] verts = new double[n*(3*n-2)][3];
		for (int i = 0; i<n; ++i)	{
			double y = 1.0 - 2 * (i/(n-1.0));
			for (int j = 0 ; j<n ; ++j)	{
				double x = -1.0 + 2 * (j/(n-1.0));
				double[] v = {x,y,1.0};
				Rn.normalize(v,v);
				System.arraycopy(v,0,verts[i*(3*n-2)+j], 0, 3);
				double tmp = v[2];v[2] = -v[0];v[0] = tmp;
				System.arraycopy(v,0,verts[i*(3*n-2)+j + (n-1)], 0, 3);
				tmp = v[2];v[2] = -v[0];v[0] = tmp;
				System.arraycopy(v,0,verts[i*(3*n-2)+j + (2*n-2)], 0, 3);
			}
		}
		qmf.setVertexCoordinates(verts);
		qmf.setVertexNormals(verts);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateTextureCoordinates(true);
		qmf.update();
		return qmf.getIndexedFaceSet();
//		qms.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(verts));
//		qms.setVertexAttributes(Attribute.NORMALS, qms.getVertexAttributes(Attribute.COORDINATES));
//		GeometryUtility.calculateAndSetFaceNormals(qms);
//		GeometryUtility.calculateAndSetTextureCoordinates(qms);
//		return  qms;
	}
	
	public static IndexedFaceSet sphereAsTriangStrip = null;
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
