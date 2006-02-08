/*
 * Created on Jul 2, 2004
 *
 */
package de.jreality.geometry;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;

/**
  * TODO adjust the parameter which determines how the profile at the vertices of the curve
 * are "pulled back" towards the mid-segment profiles.
 * @author gunn
 * 
 */
public class TubeUtility {

		static int debug = 0;
		static Logger theLogger = null;
		static TubeUtility tubeUtilityInstance = null;  // needed to create instances of the contained class FrameInfo
		static {
			tubeUtilityInstance = new TubeUtility();
		}
		
		private TubeUtility()	{
			super();
			theLogger = LoggingSystem.getLogger(TubeUtility.class);
		}
		
		public static class FrameInfo		{
			double[] frame;				// the frame itself
			double length;				// curve length (as a fraction of 1) to this point
			double theta,				// angle between adjacent segments
					phi;					// angle required to rotate the frenet normal vector (intrinsic)
										// to the parallel one
			public FrameInfo(double[] f, double l, double t, double p)	{
				super();
				frame = f;
				length = l;
				theta = t;
				phi = p;
			}
			
			public String toString()	{
				StringBuffer sb = new StringBuffer();
				sb.append("Frame is\n"+Rn.matrixToString(frame));
				sb.append("Length is: "+length+"\n");
				sb.append("Theta is: "+theta+"\n");
				sb.append("Phi is: "+phi+"\n");
				return new String(sb);
			}
		}

		public static double[][] diamondCrossSection = {{1,0,0},{0,1,0},{-1,0,0},{0,-1,0},{1,0,0}};
		public static double[][] octagonalCrossSection = {{1,0,0}, 
			{.707, .707, 0}, 
			{0,1,0},
			{-.707, .707, 0},
			{-1,0,0},
			{-.707, -.707, 0},
			{0,-1,0},
			{.707, -.707, 0},
			{1,0,0}};
		public static final int PARALLEL = 1;
		public static final int FRENET = 2;
		 
		static double[] px1 = {0,0,-.5,1};
		static double[] px2 = {0,0,.5,1};

		protected static double[] getInitialBinormal(double[][] polygon, int signature)	{
			int n = polygon.length;
			double[] B = new double[4];
			for (int i = 1; i<n-1; ++i)	{
				Pn.polarize(B, P3.planeFromPoints(null, polygon[i-1], polygon[i], polygon[i+1]),signature);	
				if (Rn.euclideanNormSquared(B) > 10E-16) return B;
			}
			B = new double[] {Math.random(), Math.random(), Math.random(), 1.0};
			return Pn.polarizePlane(null, P3.planeFromPoints(null, B, polygon[1], polygon[2]),signature);
		}

		protected static double[] e1 = 	{Math.random(), Math.random(), Math.random(), 1.0};
		public static IndexedFaceSet[] urTube = new IndexedFaceSet[3];
		private static double[][] urTubeVerts;
		public static double[][] canonicalTranslation=new double[3][];
		private static double[] translation = {0,0,.5,1};
		private static int[] signatures = {Pn.HYPERBOLIC, Pn.EUCLIDEAN, Pn.ELLIPTIC};
		static int urTubeLength;
		static {
		    int n = octagonalCrossSection.length;
			urTubeLength = n;
			urTubeVerts = new  double[2*n][3];
			for (int i = 0; i<2; ++i){
				for (int j = 0; j<n; ++j)	{
				    int q = n - j - 1;
				    System.arraycopy(octagonalCrossSection[j], 0, urTubeVerts[i*n+q],0,3);
				    if (i==0) urTubeVerts[i*n+q][2] = -0.5;
				    else urTubeVerts[i*n+q][2] = 0.5;
				}
			}
			DataList verts = StorageModel.DOUBLE_ARRAY.array(urTubeVerts[0].length).createReadOnly(urTubeVerts);
			for (int k = 0; k<3; ++k)	{
				canonicalTranslation[k] = P3.makeTranslationMatrix(null, translation, signatures[k]);
				QuadMeshFactory qmf = new QuadMeshFactory();//signatures[k], n, 2, true, false);
				qmf.setSignature(signatures[k]);
				qmf.setULineCount(n);
				qmf.setVLineCount(2);
				qmf.setClosedInUDirection(true);
				qmf.setVertexCoordinates(verts);
				qmf.setGenerateEdgesFromFaces(true);
				qmf.setGenerateFaceNormals(true);
				qmf.setGenerateVertexNormals(true);
				qmf.setGenerateTextureCoordinates(false);
				qmf.update();
				urTube[k] = qmf.getIndexedFaceSet();
			}
		}
		
		/**
		 * 
		 * @param ip1
		 * @param ip2
		 * @param rad
		 * @param crossSection
		 * @param signature
		 * @return
		 */
		 public static SceneGraphComponent tubeOneEdge(double[] ip1, double[] ip2, double rad, double[][] crossSection, int signature)	{
			if (ip1.length < 3 || ip1.length > 4 || ip2.length < 3 || ip2.length > 4)	{
				throw new IllegalArgumentException("Invalid dimension");
			}
			double p1[] = new double[4];
			double p2[] = new double[4];
			if (ip1.length == 3)	Pn.homogenize(p1, ip1);
			else p1 = ip1;
			if (ip2.length == 3) Pn.homogenize(p2, ip2);
			else p2 = ip2;
			Pn.normalize(p1, p1, signature);
			Pn.normalize(p2, p2, signature);
			
			if ((debug & 2) != 0) theLogger.log(Level.FINE,"p1 is "+Rn.toString(p1, 6));					
			if ((debug & 2) != 0) theLogger.log(Level.FINE,"p2 is "+Rn.toString(p2, 6));					
			double[] polarPlane = Pn.polarizePoint(null, p1, signature);
			if ((debug & 2) != 0) theLogger.log(Level.FINE,"Polar plane is "+Rn.toString(polarPlane, 6));					

			double[] tangent = P3.lineIntersectPlane(null, p1, p2, polarPlane);	

			double[] diff = Rn.subtract(null, p2, p1);
			if (Rn.innerProduct(diff, tangent) < 0.0)  Rn.times(tangent, -1.0, tangent);

			Pn.setToLength(tangent,tangent, 1.0, signature);
			
			double[] normal = Pn.polarizePlane(null, P3.planeFromPoints(null,  p1, tangent, e1),signature);		
			double[] binormal = Pn.polarizePlane(null, P3.planeFromPoints(null, p1, tangent, normal),signature);
			Pn.setToLength(normal, normal, 1.0, signature);			
			Pn.setToLength(binormal, binormal, 1.0, signature);

			double[] frame = new double[16];
			// for reasons unknown/murky, to get a RH.C.S. the vectors have to be assembled as follows in the matrix
			System.arraycopy(binormal, 0, frame, 0, 4);
			System.arraycopy(normal, 0, frame, 4, 4);
			System.arraycopy(tangent, 0, frame, 8, 4);
			System.arraycopy(p1, 0, frame, 12, 4);		
			// make sure the transformation is orientation-preserving
			// NOTE: If there appear shading problems on tubes, un-comment this code
//			if (Rn.determinant(frame) < 0)		{
//				System.arraycopy(normal, 0, frame, 0, 4);
//				System.arraycopy(binormal, 0, frame, 4, 4);
//
//			}
			if ((debug & 16) != 0)  {
				theLogger.log(Level.FINE,"Frame is "+Rn.matrixToString(frame));
				theLogger.log(Level.FINE,"Det is "+Rn.determinant(frame));
//				double[] QQ = Rn.identityMatrix(4);
//				QQ[15] = signature;
//				double[] result = Rn.times(null, Rn.transpose(null, frame), Rn.times(null, QQ, frame));
//				LoggingSystem.getLogger().log(Level.FINE,"Transformed Q is "+Rn.matrixToString(result));
			}
			Rn.transpose(frame, frame);
			
			double[] scaler = Rn.identityMatrix(4);
			double dist = Pn.distanceBetween(p1, p2, signature);
			double coord = dist/2;
			if (signature == Pn.HYPERBOLIC)	coord = Pn.tanh(dist/2.0);
			else if (signature == Pn.ELLIPTIC)	coord = Math.tan(dist/2.0);
			scaler[10] = 2*coord;
			
			double radcoord = rad;
			if (signature == Pn.HYPERBOLIC)	radcoord = Math.sqrt(1-coord*coord)*Pn.tanh(rad);
			else if (signature == Pn.ELLIPTIC)	radcoord = Math.sqrt(1+coord*coord)*Math.tan(rad);
			scaler[0] = scaler[5] = radcoord; 
			
			if ((debug & 1) != 0)	{
				theLogger.log(Level.FINE,"distance is \t"+dist+ " scaler is \t"+coord+" and radius factor is \t"+radcoord);
			}
			//LoggingSystem.getLogger().log(Level.FINE,"Frame is "+Rn.matrixToString(frames[0]));
			//LoggingSystem.getLogger().log(Level.FINE,"Scaler is "+Rn.matrixToString(scaler));
			double[] translate = {0,0,coord,1};
			double[] translateM = P3.makeTranslationMatrix(null, translate, signature);
			// the matrix net should  be a transformation that takes the two input points
			// to the (dehomogenized) points (0,0,+/-.5,1).
			double[] net = Rn.times(null, frame, Rn.times(null, translateM, scaler));
			if ((debug & 64) != 0) theLogger.log(Level.FINE,"net is \n"+Rn.matrixToString( net,6));
//			double[] inet = Rn.inverse(null, net);
//			double[] inp1 = Rn.matrixTimesVector(null, inet, p1);
//			double[] inp2 = Rn.matrixTimesVector(null, inet, p2);
//			if ((debug & 64) != 0) theLogger.log(Level.FINE,"Image of end points: "+Rn.toString(Pn.dehomogenize(null,inp1), 6)+"  "+Rn.toString(Pn.dehomogenize(null,inp2),6));
			SceneGraphComponent sgc = new SceneGraphComponent();
			sgc.setGeometry(urTube[signature+1]);
			sgc.setTransformation(new Transformation(net)); 
			//LoggingSystem.getLogger().log(Level.FINE,"Matrix is "+Rn.matrixToString(sgc.getTransformation().getMatrix()));
			return sgc;
		}
		
	public static void calculateAndSetNormalVectorsForCurve(IndexedLineSet ils)	{
		double[][] polygon = ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int n = polygon.length;
		double[][] normals = new double[n][4];

		if (n <= 1) {
			throw new IllegalArgumentException(
					"Can't tube a vertex list of length less than 2");
		}

		double[][] polygon2 = new double[n+2][];
		for (int i = 0; i < n; ++i)  {
			polygon2[i + 1] = polygon[i];
			polygon2[0] = Rn.add(null, polygon[0], Rn.subtract(null,
					polygon[0], polygon[1]));
			polygon2[n + 1] = Rn.add(null, polygon[n - 1], Rn.subtract(null,
					polygon[n - 1], polygon[n - 2]));

		}
		FrameInfo[] frames = new TubeFactory().makeFrameField(polygon2, FRENET, Pn.EUCLIDEAN);
		
		for(int i=0; i<n; ++i)	{
			for (int j=0;j<4;++j)	{
				normals[i][j] = frames[i].frame[4*j];
			}
			normals[i][3] *= -1;
			Pn.normalize(normals[i], normals[i], Pn.EUCLIDEAN);
		}
		ils.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(normals));
	}
}
