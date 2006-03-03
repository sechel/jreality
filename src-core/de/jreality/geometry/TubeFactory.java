/*
 * Created on Jul 2, 2004
 *
 */
package de.jreality.geometry;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.geometry.TubeUtility.FrameInfo;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.util.LoggingSystem;

/**
  * TODO adjust the parameter which determines how the profile at the vertices of the curve
 * are "pulled back" towards the mid-segment profiles.
 * @author gunn
 *
 */
public  class TubeFactory {

		static int debug = 0;
		static Logger theLogger = LoggingSystem.getLogger(TubeFactory.class);
		
		public double[][] theCurve, vertexColors, edgeColors, crossSection = TubeUtility.octagonalCrossSection;
		public double radius = .05;
		public int frameFieldType = TubeUtility.PARALLEL;
		public int signature = Pn.EUCLIDEAN;
		public int twists = 0;
		
		public boolean closedCurve = false,
			vertexColorsEnabled = false;
		
		public TubeFactory()	{
			this(null);
		}
		
		public TubeFactory(double[][] curve)	{
			super();
			theCurve = curve;
		}
		
		public void setClosed(boolean closedCurve) {
			this.closedCurve = closedCurve;
		}

		public void setCrossSection(double[][] crossSection) {
			this.crossSection = crossSection;
		}

		public void setFrameFieldType(int frameFieldType) {
			this.frameFieldType = frameFieldType;
		}

		public void setRadius(double radius) {
			this.radius = radius;
		}

		public void setSignature(int signature) {
			this.signature = signature;
		}

		public void setTwists(int twists) {
			this.twists = twists;
		}
		
		public void setVertexColorsEnabled(boolean vertexColorsEnabled) {
			this.vertexColorsEnabled = vertexColorsEnabled;
		}

		public void setEdgeColors(double[][] edgeColors) {
			this.edgeColors = edgeColors;
		}

		public void setVertexColors(double[][] vertexColors) {
			this.vertexColors = vertexColors;
		}

			
		public void update()		{
			
		}
		

		static double[] px1 = {0,0,-.5,1};
		static double[] px2 = {0,0,.5,1};
		private double[][] tangentField;
		private double[][] frenetNormalField;
		private double[][] parallelNormalField;
		private double[][] binormalField;
		private FrameInfo[] frameInfo;
		/**
		 * The primary method in the tube-generating process.  The code is complicated by:
		 * 	1) dealing with euclidean, hyperbolic, and elliptic cases simultaneously and
		 *  2) loads of debugging code required to get 1) working correctly
		 * 
		 * Explanation of the algorithm:
		 *     Assume that <i> polygon </i> is an array of length <i>n</i> 
		 * 
		 * The input curve <i>polygon</i> is assumed to have an initial and terminal point with the following properties:
		 *     1) if the curve is closed, <i>polygon[0]=polygon[n-2]</i> and <i>polygon[n-1]=polygon[1]</i>
		 *     2) if the curve is not closed, then polygon[0] is the mirror of polygon[2] wrt polygon[1], similarly for polygon[n-1].
		 * 
		 * The return array is of length <i>n-2</i>.
		 * The basic idea is: first calculate the Frenet frame for the curve. That is, in the best case,
		 * a simple matter at each point <i>P[i]</i> of:
		 * 	1) Calculate the polar ("tangent") plane at P 
		 *      i)  In the euclidean case this is the plane at infinity (w=0), i.e., tangent vectors are distinguished by 
		 * having fourth coordinate 0. 
		 *      ii) In non-euclidean case, the polar plane is uniquely defined by P. 
		 *      iii) All vectors in the frame for P belong to this tangent plane. 
		 *  2) Calculate the osculating plane for the curve at P.  This is the plane spanned by P[i-1], P[i], P[i+1]. 
		 * When polarized, this gives the binormal vector
		 *      i) In the euclidean case, polarizing means "set the 4th coordinate to 0".  This yields the binormal vector
		 *      ii) In the non-euclidean case, we still get the binormal vector.
		 *  3) Calculate the tangent vector at P[i]
		 * ...to be continued ...
		 *   At points where the curve is not curved, this isn't well-defined. Then just push
		 *      the most recent data forward
		 *   If type=PARALLEL, calculate the adjustment needed to rotate the Frenet frame to the parallel one
		 * Th
		 * @param polygon
		 * @param type		PARALLEL or FRENET
		 * @param signature	Pn.EUCLIDEAN, Pn.HYPERBOLIC, or Ph.ELLIPTIC
		 * @return	an array of length (n-2) of type FrameInfo containing an orthonormal frame for each internal point in the initial polygon array.
		 */
		public FrameInfo[] makeFrameField(double[][] polygon, int type, int signature)		{
		 	int n = polygon.length;
		 	double[][] polygonh;
		 	// to simplify life, convert all points to homogeneous coordinates
		 	if (polygon[0].length == 3) {
		 		polygonh = Pn.homogenize(null, polygon);
		 		Pn.normalize(polygonh, polygonh, signature);
		 	}
			else if (polygon[0].length == 4)	
				polygonh = Pn.normalize(null, polygon, signature);
			else {
				throw new IllegalArgumentException("Points must have dimension 4");
			}
		 	if ((debug & 1) != 0)	
		 		theLogger.log(Level.FINER,"Generating frame field for signature "+signature);
		 	if (tangentField == null || tangentField.length < (n-2))	{
				tangentField = new double[n-2][4];
				frenetNormalField = new double[n-2][4];
				parallelNormalField = new double[n-2][4];
				binormalField = new double[n-2][4];
		 	}
			frameInfo = new FrameInfo[n-2];		 		
			double[] d  = new double[n-2];			// distances between adjacent points
			if ((debug & 32) != 0)	{
				for (int i = 0; i<n; ++i)	{
					theLogger.log(Level.FINER,"Vertex "+i+" : "+Rn.toString(polygonh[i]));
				}
			}
			double[] frame = new double[16];
			double totalLength = 0.0;
			for (int i = 1; i<n-1; ++i)	{
				d[i-1] = (totalLength += Pn.distanceBetween(polygonh[i-1], polygonh[i], signature));
			}
			totalLength = 1.0/totalLength;
			// Normalize the distances between points to have total sum 1.
			for (int i = 1; i<n-1; ++i)	d[i-1] *= totalLength;

			for (int i = 1; i<n-1; ++i)	{
				
				/*
				 * calculate the binormal from the osculating plane
				 */
				double theta = 0.0, phi=0.0;
				boolean collinear = false;
				double[] polarPlane = Pn.polarizePoint(null, polygonh[i], signature);
				if ((debug & 2) != 0) theLogger.log(Level.FINER,"Polar plane is: "+Rn.toString(polarPlane, 6));					
				
				double[] osculatingPlane = P3.planeFromPoints(null, polygonh[i-1], polygonh[i], polygonh[i+1]);
				double size = Rn.euclideanNormSquared(osculatingPlane);
				if (size < 10E-16)	{			// collinear points!
					collinear = true;
					if ((debug & 2) != 0) theLogger.log(Level.FINER,"degenerate binormal");
					if (i == 1)		binormalField[i-1] = getInitialBinormal(polygonh, signature);
					else Pn.projectToTangentSpace(binormalField[i-1], polygonh[i], binormalField[i-2], signature);
				} else
					Pn.polarizePlane(binormalField[i-1], osculatingPlane,signature);					
				Pn.setToLength(binormalField[i-1], binormalField[i-1], 1.0, signature);
				if ((debug & 2) != 0) theLogger.log(Level.FINER,"Binormal is "+Rn.toString(binormalField[i-1],6));

				/*
				 * Next try to calculate the tangent as a "mid-plane" if the three points are not collinear
				 */
				double[] midPlane = null, plane1 = null, plane2 = null;
				if (!collinear)	{
					plane1 = P3.planeFromPoints(null, binormalField[i-1], polygonh[i], polygonh[i-1]);
					plane2 = P3.planeFromPoints(null, binormalField[i-1], polygonh[i], polygonh[i+1]);
					midPlane = Pn.midPlane(null, plane1, plane2, signature);
					size = Rn.euclideanNormSquared(midPlane);
					if ((debug & 2) != 0) theLogger.log(Level.FINER,"tangent norm squared is "+size);					
					theta = Pn.angleBetween(plane1, plane2, signature);
				}
				/*
				 * if this is degenerate, then the curve must be collinear at this node
				 * get the tangent by projecting the line into the tangent space at this point
				 */ 
				if (collinear || size < 10E-16)	{
					// the three points must be collinear
					if ((debug & 2) != 0) theLogger.log(Level.FINER,"degenerate Tangent vector");
					// TODO figure out why much breaks 
					// if the two vertices in the following call are swapped
					double[] pseudoT = P3.lineIntersectPlane(null, polygonh[i-1], polygonh[i+1], polarPlane);	
					if ((debug & 2) != 0) theLogger.log(Level.FINE,"pseudo-Tangent vector is "+Rn.toString(pseudoT,6));
					// more euclidean/noneuclidean trouble
					// we want the plane equation of the midplane 
					if (signature != Pn.EUCLIDEAN)	{
						midPlane = Pn.polarizePoint(null, pseudoT, signature);
					} else {
						// TODO figure out why the vector (the output of lineIntersectPlane)
						// has to be flipped in this case but not in the non-euclidean case
						//midPlane = Rn.times(null, -1.0, pseudoT);
						midPlane = pseudoT;
						// the eucliean polar of a point is the plane at infinity: we want something
						// much more specific: 
						// we assume the polygonal data is dehomogenized (last coord = 1)
						midPlane[3] = -Rn.innerProduct(midPlane, polygonh[i], 3);						
					}	
					// TODO detect case where the angle is 0, also
					theta = Math.PI;
				}
				if ((debug & 2) != 0) theLogger.log(Level.FINE,"Midplane is "+Rn.toString(midPlane,6));
				Pn.polarizePlane(tangentField[i-1], midPlane, signature);	
				// This is a hack to try to choose the correct version of the tangent vector:
				// since we're in projective space, t and -t are equivalent but only one
				// "points" in the correct direction.  Deserves further study!
				double[] diff = Rn.subtract(null, polygonh[i], polygonh[i-1]);
				if (Rn.innerProduct(diff, tangentField[i-1]) < 0.0)  Rn.times(tangentField[i-1], -1.0, tangentField[i-1]);

				Pn.setToLength(tangentField[i-1], tangentField[i-1], 1.0, signature);
				// finally calculate the normal vector
				Pn.polarizePlane(frenetNormalField[i-1], P3.planeFromPoints(null,binormalField[i-1], tangentField[i-1],  polygonh[i]),signature);					
				Pn.setToLength(frenetNormalField[i-1], frenetNormalField[i-1], 1.0, signature);
				if ((debug & 2) != 0) theLogger.log(Level.FINE,"frenet normal is "+Rn.toString(frenetNormalField[i-1],6));

				if (type == TubeUtility.PARALLEL)	{
					// get started 
					if (i == 1)		System.arraycopy(frenetNormalField[0], 0, parallelNormalField[0], 0, 4);		
					else 	{
						double[] nPlane = P3.planeFromPoints(null, polygonh[i], polygonh[i-1], parallelNormalField[i-2]);
						double[] projectedN = P3.pointFromPlanes(null, nPlane, midPlane, polarPlane );
						if (Rn.euclideanNormSquared(projectedN) < 10E-16)	{
							theLogger.log(Level.FINE,"degenerate normal");
							projectedN = parallelNormalField[i-2];		// try something!
						}
						parallelNormalField[i-1] = Pn.normalizePlane(null, projectedN, signature);
						//if (Rn.innerProduct(pN[i-1], pN[i-2]) < 0) Rn.times(pN[i-1], -1.0, pN[i-1]);
						if ((debug & 128) != 0)	theLogger.log(Level.FINE,"Parallel normal is "+Rn.toString(parallelNormalField[i-1],6));
					}
				} 
//				size = Rn.euclideanNormSquared(pN[i-1]);
				if (size < 10E-16)	{
					if ((debug & 2) != 0) theLogger.log(Level.FINE,"degenerate parallel normal");
					if (i > 1) parallelNormalField[i-1] = parallelNormalField[i-2];
				}
				Pn.setToLength(parallelNormalField[i-1], parallelNormalField[i-1], 1.0, signature);
				if (type == TubeUtility.PARALLEL)		{
					phi = Pn.angleBetween(frenetNormalField[i-1],parallelNormalField[i-1],signature);
					double a = Pn.angleBetween(parallelNormalField[i-1],binormalField[i-1],signature);
					if (a > Math.PI/2) phi = -phi;
				}
				else phi = 0.0;
				
				System.arraycopy(frenetNormalField[i-1], 0, frame, 0, 4);
				System.arraycopy(binormalField[i-1], 0, frame, 4, 4);
				System.arraycopy(tangentField[i-1], 0, frame, 8, 4);
				System.arraycopy(polygonh[i], 0, frame, 12, 4);
				   	
				if ((debug & 4) != 0) theLogger.log(Level.FINE,"determinant is:\n"+Rn.determinant(frame));
				frameInfo[i-1] = new FrameInfo(Rn.transpose(null, frame),d[i-1],theta, phi);
				if ((debug & 16) != 0) theLogger.log(Level.FINE,"Frame "+(i-1)+": "+frameInfo[i-1].toString());
			}
			return frameInfo;
		 }

		/**
		 * 
		 * @param polygon
		 * @param signature
		 * @return
		 */
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

}
