/*
 * Created on Jun 30, 2004
 *
 */
package de.jreality.geometry;

import java.awt.Color;

import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.pick.bounding.AABBTree;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

/**
 * @author gunn
 *
 */
public class Primitives {

		private Primitives()	{
			super();
		}
		static private double[][] cubeVerts3 =  
		{{1,1,1},{1,1,-1},{1,-1,1},{1,-1,-1},{-1,1,1},{-1,1,-1},{-1,-1,1},{-1,-1,-1}};

		static private int[][] cubeIndices = {
				{0,2,3,1},
				{1,5,4,0},
				{3,7,5,1},
				{5,7,6,4},
				{2,6,7,3},
				{0,4,6,2}};
		
		static private double[][] cubeColors = {
			{0d, 1d, 0d},
			{0d, 0d, 1d},
			{1d, 0d, 0d},
			{1d, 0d, 1d},
			{1d, 1d, 0d},
			{0d, 1d, 1d}};
			
		public static IndexedFaceSet cube()	{return cube(false);}
		public static IndexedFaceSet coloredCube()	{return cube(true);}
		
		public static IndexedFaceSet cube(boolean colored)	{
			
			IndexedFaceSet cube = new IndexedFaceSet(8, 6);

			cube.setFaceAttributes(Attribute.INDICES, new IntArrayArray.Array(cubeIndices));
			cube.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(cubeVerts3));
			if (colored)	{
				cube.setFaceAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(cubeColors));
			}
			IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(cube);
			GeometryUtility.calculateAndSetFaceNormals(cube);		
			return cube;
	}
		
		static private double[][] tetrahedronVerts3 =  
		{{1,1,1},{1,-1,-1},{-1,1,-1},{-1,-1,1}};

		static private int[][] tetrahedronIndices = {
				{0,1,2},
				{2,1,3},
				{1,0,3},
				{0,2,3}};
		
		static private double[][] tetrahedronColors = {
			{0d, 1d, 0d},
			{0d, 0d, 1d},
			{1d, 0d, 0d},
			{1d, 0d, 1d}
			};
			
		public static IndexedFaceSet tetrahedron()	{return tetrahedron(false);}
		public static IndexedFaceSet coloredTetrahedron()	{return tetrahedron(true);}
		
		public static IndexedFaceSet tetrahedron(boolean colored)	{
			
			IndexedFaceSet tetrahedron = new IndexedFaceSet(4, 4);

			tetrahedron.setFaceAttributes(Attribute.INDICES, new IntArrayArray.Array(tetrahedronIndices));
			tetrahedron.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(tetrahedronVerts3));
			if (colored)	{
				tetrahedron.setFaceAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(tetrahedronColors));
			}
			IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(tetrahedron);
			GeometryUtility.calculateAndSetFaceNormals(tetrahedron);		
			return tetrahedron;
	}

		static public double[][] icoVerts3 =  {
				{0.850651026, 0, 0.525731027}, 
				{0.850651026, 0, -0.525731027}, 
				{0.525731027, 0.850651026, 0}, 
				{0.525731027, -0.850651026, 0.0}, 
				{0.0, -0.525731027, 0.850651026}, 
				{0.0, 0.525731027, 0.850651026}, 
				{-0.850651026, 0, -0.525731027}, 
				{ -0.850651026, 0, 0.525731027}, 
				{-0.525731027, 0.850651026, 0}, 
				{ 0.0, 0.525731027, -0.850651026}, 
				{0.0, -0.525731027, -0.850651026}, 
				{-0.525731027, -0.850651026, 0.0}};

		// Don't remove: good for testing backend capability to deal with 4D vertices
		static private double[][] icoVerts4 =  {
				{0.850651026, 0, 0.525731027, 1.0}, 
				{0.850651026, 0, -0.525731027, 1.0}, 
				{0.525731027, 0.850651026, 0, 1.0}, 
				{0.525731027, -0.850651026, 0.0, 1.0}, 
				{0.0, -0.525731027, 0.850651026, 1.0}, 
				{0.0, 0.525731027, 0.850651026, 1.0}, 
				{-0.850651026, 0, -0.525731027, 1.0}, 
				{ -0.850651026, 0, 0.525731027, 1.0}, 
				{-0.525731027, 0.850651026, 0, 1.0}, 
				{ 0.0, 0.525731027, -0.850651026, 1.0}, 
				{0.0, -0.525731027, -0.850651026, 1.0}, 
				{-0.525731027, -0.850651026, 0.0, 1.0}};

		static private int[][] icoIndices = {
						{0, 1, 2},
						{0, 3, 1},
						{0, 4, 3},
						{0, 5, 4},
						{0, 2, 5},
						{6, 7, 8},
						{6, 8, 9},
						{6, 9, 10},
						{6, 10, 11},
						{6, 11, 7},
						{1, 3, 10},
						{3, 4, 11},
						{4, 5, 7},
						{5, 2, 8},
						{2, 1, 9},
						{7, 11, 4},
						{8, 7, 5},
						{9, 8, 2},
						{10, 9, 1},
						{11, 10, 3}};

		public static IndexedFaceSet sharedIcosahedron = null; 
		static  {
			sharedIcosahedron = icosahedron();
		}
				
		public static IndexedFaceSet icosahedron() {
					
			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(12);
			ifsf.setFaceCount(20);
			ifsf.setVertexCoordinates(icoVerts3);
			ifsf.setVertexNormals(icoVerts3);
			ifsf.setFaceIndices(icoIndices);
			ifsf.setGenerateEdgesFromFaces(true);
			ifsf.setGenerateFaceNormals(true);
			ifsf.update();
			return ifsf.getIndexedFaceSet();
			}
		
		public static PointSet point( double[] center)	{
			PointSet ps = new PointSet(1);
			int n = center.length;
			double[][] pts = new double[1][n];
			System.arraycopy(center,0,pts[0],0,n);
			ps.setVertexCountAndAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(n).createReadOnly(pts));
			return ps;
		}
		
		public static SceneGraphComponent sphere(double radius, double x, double y, double z)	{
			return sphere(radius, new double[] {x,y,z}, Pn.EUCLIDEAN);
		}
		
		public static SceneGraphComponent sphere(double radius, double[] center) {
			return sphere(radius, center, Pn.EUCLIDEAN);
		}
		
		public static SceneGraphComponent sphere(double radius, double[] center, int signature) {
			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("sphere");
			if (center == null)  center = Pn.originP3;
			MatrixBuilder.init(null,signature).translate(center).scale(radius).assignTo(sgc.getTransformation());
			sgc.setGeometry(new Sphere());
			return sgc;
		}
		
		/**
		 * @return SceneGraphComponent with wire-frame sphere (azimuth/elevation coordinate mesh)
		 */
		public static SceneGraphComponent wireframeSphere() {
			SceneGraphComponent hypersphere = SceneGraphUtility.createFullSceneGraphComponent("hyperbolic sphere");
			hypersphere.setGeometry(SphereUtility.sphericalPatch(0.0, 0.0, 360.0, 180.0, 40, 20, 1.0));
			Appearance ap = hypersphere.getAppearance();
			ap.setAttribute(CommonAttributes.FACE_DRAW, false);
			ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
			ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
			ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(200, 200, 200));
			ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 0.5);
			return hypersphere;
		}
		
		public static IndexedFaceSet cylinder(int n) {
			int rn = n+1;
			double[] verts = new double[2*3*rn];
			double angle = 0, delta = Math.PI*2/(n);
			for (int i = 0 ;i<rn; ++i)	{
				angle = i*delta;
				verts[3*(i+rn)] = verts[3*i] = Math.cos(angle);
				verts[3*(i+rn)+1] = verts[3*i+1] = Math.sin(angle);
				verts[3*i+2] = 1;
				verts[3*(i+rn)+2] = -1;
			}
			QuadMeshFactory qmf = new QuadMeshFactory();//Pn.EUCLIDEAN, n+1, 2, true, false);
			qmf.setULineCount(n+1);
			qmf.setVLineCount(2);
			qmf.setClosedInUDirection(true);
			qmf.setVertexCoordinates(verts);
			qmf.setGenerateEdgesFromFaces(true);
			qmf.setGenerateFaceNormals(true);
			qmf.setGenerateVertexNormals(true);
			qmf.update();
			return qmf.getIndexedFaceSet();
		}
		
		public static IndexedFaceSet pyramid(double[][] base, double[] tip)	{
			int n = base.length;
			int l = base[0].length;
			if (l != tip.length)	{
				throw new IllegalArgumentException("Points must have same dimension");
			}
			double[][] newVerts = new double[n+1][l];
			for (int i = 0; i<n; ++i)		System.arraycopy(base[i], 0, newVerts[i], 0, l);
			System.arraycopy(tip, 0, newVerts[n],0,l);
			int[][] indices = new int[n+1][];
			for (int i = 0; i<n; ++i)	{
				indices[i] = new int[3];
				indices[i][0] = i;
				indices[i][1] = (i+1)%n;
				indices[i][2] = n;
			}
			indices[n] = new int[n];
			for (int i = 0; i<n; ++i)	indices[n][i] = i;
//			IndexedFaceSet ifs = IndexedFaceSetUtility.createIndexedFaceSetFrom(indices, newVerts, null, null,null,null);
			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(n+1);
			ifsf.setFaceCount(n+1);
			ifsf.setVertexCoordinates(newVerts);
			ifsf.setFaceIndices(indices);
			ifsf.setGenerateEdgesFromFaces(true);
			ifsf.setGenerateFaceNormals(true);
			ifsf.update();
			return ifsf.getIndexedFaceSet();
		}
		
		static double[][] viewerVerts = {{1d,0d,-1.5d},{1d,1d,-1.5d},{-1d,1d,-1.5d},{-1d, 0d, -1.5d}};
		static double[] tip = {0,.5d,-1d};
		private static SceneGraphComponent _camIcon = null;
		static {
			_camIcon = SceneGraphUtility.createFullSceneGraphComponent("cameraIcon");
			_camIcon.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
			_camIcon.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
			_camIcon.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
			_camIcon.setGeometry(cube());
			MatrixBuilder.euclidean().scale(.3,1.0,1.0).assignTo(_camIcon.getTransformation());

			SceneGraphComponent viewer = new SceneGraphComponent();
			viewer.setGeometry(pyramid(viewerVerts, tip));
			_camIcon.addChild(viewer);
		}
		
		public static SceneGraphComponent cameraIcon(double scale)		{
			SceneGraphComponent ci = SceneGraphUtility.createFullSceneGraphComponent("cameraIcon");
			MatrixBuilder.euclidean().scale(scale).assignTo(ci.getTransformation());
			ci.addChild(_camIcon);
			return ci;
		}
		
		public static IndexedLineSet discreteTorusKnot(double R, double r, int n, int m, int nPts)	{
			double[][] vertices = new double[nPts][3];
			for (int i = 0; i<nPts; ++i)	{
				double angle = ( i * 2.0 * Math.PI)/ nPts;
				double a = m * angle, A = n * angle;
				double C = Math.cos(A),			S = Math.sin(A);
				double c = r*Math.cos(a), 			s = r*Math.sin(a);
				
				vertices[i][0] = C * (R + c);
				vertices[i][1] = s;
				vertices[i][2] = S * (R+c);
			}
			return IndexedLineSetUtility.createCurveFromPoints(vertices, true);
		}
		/**
		 * @param order
		 * @return
		 */
		public static IndexedFaceSet regularPolygon(int order) {
			double[][] verts = new double[order][3];
			for (int  i =0; i<order; ++i)	{
				double angle = 2 * (i+.5) * Math.PI/order;
				verts[i][0] = Math.cos(angle);
				verts[i][1] = Math.sin(angle);
				verts[i][2] = 0.0;
			}
			return IndexedFaceSetUtility.constructPolygon(verts);
		}
		public static IndexedLineSet arrow(double x0, double y0, double x1, double y1, double tipSize)	{
			return arrow(x0, y0, x1, y1, tipSize, false);
		}
		public static IndexedLineSet arrow(double x0, double y0, double x1, double y1, double tipSize, boolean halfArrow)	{
			IndexedLineSet ifs = new IndexedLineSet(4, 3);
			double[][] verts = new double[4][3];
			verts[0][0] = x0;
			verts[0][1] = y0;
			verts[0][2] = 0.0;
			verts[1][0] = x1; 
			verts[1][1] = y1;
			verts[1][2] = 0.0;
			double dx = (x1 - x0)*tipSize;
			double dy = (y1 - y0)*tipSize;
			verts[2][0] = x1 - dx + dy;
			verts[2][1] = y1 - dy - dx;
			verts[2][2] = 0.0;
			verts[3][0] = x1 - dx - dy;
			verts[3][1] = y1 - dy + dx;
			verts[3][2] = 0.0;
			int[][] indices;
			if (halfArrow) indices = new int[2][2];
			else indices = new int[3][2];
			indices[0][0] = 0;
			indices[0][1] = 1;
			indices[1][0] = 1;
			indices[1][1] = 2;
			if (!halfArrow)	{
				indices[2][0] = 1;
				indices[2][1] = 3;			
			}
			ifs.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(verts));
			ifs.setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.array(2).createReadOnly(indices));
			return ifs;
		}
		/**
		 * Create a surface of revolution surface by rotating the profile curve around the X-axis.
		 * The resulting array with have the original curve twicee, once at the beginning and also
		 * at the end.  This is necessary to be able to texture the surface under current conditions.
		 * @param profile	a 3- or 4-d array of points (generally of form (x,y,0) or (x,y,0,1))
		 * @param num		number of copies of the curve to make
		 * @return
		 */
		public static double[][] surfaceOfRevolution(double[][] profile, int num, double angle) {
			if (num <= 1 || profile[0].length < 3) {
				throw new IllegalArgumentException("Bad parameters");
			}
			double[][] vals = new double[num * profile.length][profile[0].length];
			for (int i = 0 ; i < num; ++i)	{
				double a = i * angle/(num-1);
				double[] rot = P3.makeRotationMatrixX(null, a);
				for (int j = 0; j<profile.length; ++j)
					Rn.matrixTimesVector(vals[i*profile.length+j], rot, profile[j]);
			}
			return vals;
		}
		
		/*
		 * @deprecated
		 */
		public static IndexedFaceSet surfaceOfRevolutionAsIFS(double[][] profile, int num, double angle)	{
			QuadMeshFactory qmf = new QuadMeshFactory();//Pn.EUCLIDEAN, profile.length, num, false, false);
//			QuadMeshShape qm = new QuadMeshShape( profile.length, num, false, false);
			qmf.setULineCount(profile.length);
			qmf.setVLineCount(num);
			double[][] vals = surfaceOfRevolution(profile, num, angle);
			qmf.setVertexCoordinates(vals);
			qmf.setGenerateFaceNormals(true);
			qmf.setGenerateVertexNormals(true);
			qmf.update();
//			qm.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vals[0].length).createReadOnly(vals));
//			GeometryUtility.calculateAndSetNormals(qm);
			return qmf.getIndexedFaceSet();
		}
		
		public static SceneGraphComponent clippingPlane(double[] planeEquation)	{
			return clippingPlane(planeEquation, Pn.EUCLIDEAN);
		}

		public static SceneGraphComponent clippingPlane(double[] planeEquation, int sig)	{
			double[] normal = new double[4];
			System.arraycopy(planeEquation, 0, normal, 0, 3);
			double[] rotation = P3.makeRotationMatrix(null, new double[]{0,0,1}, normal);
			double l = Rn.euclideanNormSquared(normal);
			double[] tform;
			if (l != 0)	{
				double f = -planeEquation[3]/l;
				double[] tlate = new double[4];
				Rn.times(tlate, f, normal);
				tlate[3] = 1.0;
				double[] translation = P3.makeTranslationMatrix(null, 	tlate, sig);
				tform =  Rn.times(null, translation, rotation);
			} else tform = rotation;
			SceneGraphComponent cp = SceneGraphUtility.createFullSceneGraphComponent("clippingPlane");
			cp.getTransformation().setMatrix(tform);
			cp.setGeometry(new ClippingPlane());
			return cp;
		}
   
		public static IndexedFaceSet torus(final double bR, final double sR, int bDetail, int sDetail) {
		    
		    ParametricSurfaceFactory.Immersion immersion =
		        new ParametricSurfaceFactory.Immersion() {

                    public int getDimensionOfAmbientSpace() {
                        return 3;
                    }

                    public void evaluate(double x, double y, double[] targetArray, int arrayLocation) {
                        // TODO Auto-generated method stub
                        double sRMulSinY=sR*Math.sin(y);
                        targetArray[arrayLocation  ] = Math.cos(-x)*(bR+sRMulSinY);
                        targetArray[arrayLocation+1] = sR*Math.cos(y);
                        targetArray[arrayLocation+2] = Math.sin(-x)*(bR+sRMulSinY);   
                    }

					public boolean isImmutable() {
						return true;
					}
		        
		    };
		    
		    ParametricSurfaceFactory factory = new ParametricSurfaceFactory( immersion);
		    
		    factory.setULineCount(bDetail+1);
		    factory.setVLineCount(sDetail+1);
		    
		    factory.setClosedInUDirection(true);
		    factory.setClosedInVDirection(true);
		    
		    factory.setUMax(2*Math.PI);
		    factory.setVMax(2*Math.PI);
		    
        factory.setGenerateFaceNormals(true);
        factory.setGenerateVertexNormals(true);
        
		    factory.update();
		    
		    return factory.getIndexedFaceSet();
		}
		
        public static IndexedFaceSet sphere(final int detail ) {
		    
		    ParametricSurfaceFactory.Immersion immersion =
		        new ParametricSurfaceFactory.Immersion() {

                    public int getDimensionOfAmbientSpace() {
                        return 3;
                    }

                    public void evaluate(double x, double y, double[] targetArray, int arrayLocation) {
                        
                        targetArray[arrayLocation  ] = Math.cos(x)*Math.sin(y);
                        targetArray[arrayLocation+1] = Math.sin(x)*Math.sin(y);
                        targetArray[arrayLocation+2] = Math.cos(y);
                    }

					public boolean isImmutable() {
						return true;
					}
		        
		    };
		    
		    ParametricSurfaceFactory factory = new ParametricSurfaceFactory( immersion);
		    
		    factory.setULineCount(detail+1);
		    factory.setVLineCount(detail+1);
		    
		    factory.setClosedInUDirection(true);
		    factory.setClosedInVDirection(false);
		    
		    factory.setUMax(2*Math.PI);
		    factory.setVMin(1e-5);
		    factory.setVMax(Math.PI-1e-5);
		    
            factory.setGenerateFaceNormals(true);
            factory.setGenerateVertexNormals(true);
        
		    factory.update();
		    
		    return factory.getIndexedFaceSet();
		}
		public static IndexedFaceSet texturedSquare(double[] points) {
		    
		    IndexedFaceSetFactory factory = new IndexedFaceSetFactory();
		    
		    factory.setVertexCount( 4 );
		    factory.setFaceCount(1);
		    factory.setVertexCoordinates(points);
		    factory.setFaceIndices(new int[][] {{ 0,1,2,3}});
		    factory.setVertexTextureCoordinates(new double[] { 0,0,1,0,1,1,0,1});
		    factory.setGenerateVertexNormals(true);
		    factory.setGenerateFaceNormals(true);
		    factory.setGenerateEdgesFromFaces(true);
		    
		    factory.update();
		    
		    return factory.getIndexedFaceSet();
		}
    
    public static IndexedFaceSet plainQuadMesh(double xStep, double yStep, int xDetail, int yDetail) {
      ParametricSurfaceFactory factory = new ParametricSurfaceFactory(new ParametricSurfaceFactory.DefaultImmersion() {
        public double evaluate(double u, double v) {
          return 0;
        }
      });
      
      factory.setULineCount(xDetail+1);
      factory.setVLineCount(yDetail+1);
      
      factory.setClosedInUDirection(false);
      factory.setClosedInVDirection(false);
      
      factory.setUMin(-xStep*xDetail/2);
      factory.setUMax(xStep*xDetail/2);
      factory.setVMin(-yStep*yDetail/2);
      factory.setVMax(yStep*yDetail/2);
      
      factory.setGenerateFaceNormals(true);
      factory.setGenerateVertexNormals(false); // ??
      
      factory.update();
      
      return factory.getIndexedFaceSet();      
    }
}
