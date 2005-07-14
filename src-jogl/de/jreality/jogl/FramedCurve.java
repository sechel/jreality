/*
 * Created on Aug 19, 2004
 *
 */
package de.jreality.jogl;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.jogl.anim.AnimationUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.CubicBSpline;
import de.jreality.util.P3;
import de.jreality.util.Pn;
import de.jreality.util.Quaternion;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;

/**
 * 
 *  @author gunn
 */
public class FramedCurve extends SceneGraphComponent {
	Vector controlPoints;
	double tmin, tmax;
	IndexedLineSet curveRepresentation;
	private boolean outOfDate;
	private boolean showLastSample = true;
	private SceneGraphComponent camIcon;
	private SceneGraphComponent camIconItself;
	private SceneGraphComponent theCurveItself;
	private SceneGraphComponent currentPoint;
	private double iconScale = .02;
    private boolean useBSpline = true;
    int signature;
    double diameter;
    File sourceFile= null;
    int pointsForCurveRepn = 100;
    boolean useNatural = false;
    
	CubicBSpline cbx;
	CubicBSpline cby;
	CubicBSpline cbz;
	CubicBSpline cbr;
	CubicBSpline cbi;
	CubicBSpline cbj;
	CubicBSpline cbk;
	
	
	public FramedCurve()		{
		this(Pn.EUCLIDEAN);
	}
	
	public FramedCurve(int sig)		{
		super();
		signature = sig;
		controlPoints = new Vector();
	}
		
	public static class ControlPoint	implements Comparable {
		Transformation tt;
		double t;
			
		public int compareTo(Object o) {
			if ( t > ((ControlPoint) o).t) return 1;
			else if (t < ((ControlPoint) o).t) return -1;
			else return 0;
		}
		/**
		 * @param tt
		 * @param t
		 */
		public ControlPoint(Transformation tt, double t) {
			super();
			this.tt = tt;
			this.t = t;
		}

		/**
		 * @param value
		 */
		public void setTime(double value) {
			t = value;
		}

		/**
		 * @return
		 */
		public double getTime() {
			return t;
		}
		
		public Transformation getTransformation() {
			return tt;
		}
	}
	
	public static FramedCurve frameCurveFactory(ControlPoint[] cp)	{
		FramedCurve fc = new FramedCurve();
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.white);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE, 3.0);
		fc.setAppearance(ap);
		fc.setControlPoints(cp);
		return fc;
	}
	
     public void setControlPoints(Transformation[] cp)	{
		controlPoints.clear();
		int n = cp.length;
		for (int i  = 0; i<n; ++i)	{
			double p = (i==0) ? 0d : (i/(n-1.0));
	   		controlPoints.add( new ControlPoint(cp[i], p));	
		}
		outOfDate=true;
		update();
	}

    public void setControlPoints(double[][] matrices)	{
    		controlPoints.clear();
    		int n = matrices.length;
    		for (int i  = 0; i<n; ++i)	{
    			double p = (i==0) ? 0d : (i/(n-1.0));
    	   		controlPoints.add(new ControlPoint(new Transformation(matrices[i]), p));	
    		}
    		outOfDate=true;
    		update();
    }
    
    public void setControlPoints(ControlPoint[] cp)		{
    		controlPoints.clear();
    		int n = cp.length;
    		for (int i = 0; i<n; ++i)	controlPoints.add(cp[i]);
    		outOfDate = true;
    		update();
	}
    
    public void addControlPoint(ControlPoint cp)	{
    		controlPoints.add(cp);
    		outOfDate = true;
    		//update();
    }

	public double getTmax() {
		if (outOfDate) update();
		return tmax;
	}
	public double getTmin() {
		if (outOfDate) update();
		return tmin;
	}
	
    private void update()		{
    		if (!outOfDate) return;
    		//JOGLConfiguration.theLog.log(Level.FINE
    		Collections.sort(controlPoints);
    		tmin = ((ControlPoint) controlPoints.firstElement()).t;
    		tmax = ((ControlPoint) controlPoints.lastElement()).t;
    		// extract translational part
       	double[] p0 = Rn.matrixTimesVector( null, ((ControlPoint) controlPoints.firstElement()).tt.getMatrix(), Pn.originP3);
       	double[] p1 = Rn.matrixTimesVector( null, ((ControlPoint) controlPoints.lastElement()).tt.getMatrix(), Pn.originP3);
       	diameter = Pn.distanceBetween(p0, p1, signature);
       	iconScale = .0002 * diameter;
    		int n = controlPoints.size();
    		Iterator iter;
    		int i;
    		if (useBSpline  && controlPoints.size() > 1)	{
    			double[] t = new double[n];
    			double[] x = new double[n];
    			double[] y = new double[n];
    			double[] z = new double[n];
    			double[] r = new double[n];
    			double[] qi = new double[n];
    			double[] qj = new double[n];
    			double[] qk = new double[n];
    			Quaternion oldQ = null;
    	   		for ( i = 0, iter = controlPoints.iterator(); iter.hasNext(); ++i )	{
    	   			ControlPoint tmp = (ControlPoint) iter.next();
        			 double[] m = tmp.tt.getMatrix();
        			 Quaternion rot = tmp.tt.getRotationQuaternion();
    				double w = m[15];
    				t[i] = tmp.t;
    				x[i] = m[3]/w;
    				y[i] = m[7]/w;
    				z[i] = m[11]/w;
    				if (oldQ != null) {
    					double dot = Quaternion.innerProduct(oldQ, rot);
    					if (dot < 0) Quaternion.times(rot, -1.0, rot);
    				}
    				r[i] = rot.re;
    				qi[i] = rot.x;
    				qj[i] = rot.y;
    				qk[i] = rot.z;
    				oldQ = rot;
    				
    			}
    	   		if (useNatural)	{
        			cbx = new CubicBSpline.Natural(t, x);
        			cby = new CubicBSpline.Natural(t, y);
        			cbz = new CubicBSpline.Natural(t, z);
        			cbr = new CubicBSpline.Natural(t, r);
        			cbi = new CubicBSpline.Natural(t, qi);
        			cbj = new CubicBSpline.Natural(t, qj);
        			cbk = new CubicBSpline.Natural(t, qk);
    	   		} else {
    	   			cbx = new CubicBSpline.Default(t, x);
        			cby = new CubicBSpline.Default(t, y);
        			cbz = new CubicBSpline.Default(t, z);
        			cbr = new CubicBSpline.Default(t, r);
        			cbi = new CubicBSpline.Default(t, qi);
        			cbj = new CubicBSpline.Default(t, qj);
        			cbk = new CubicBSpline.Default(t, qk);   	   			
    	   		}

//   		for ( i = 0, iter = controlPoints.iterator(); iter.hasNext(); ++i )	{
//    			ControlPoint tmp = (ControlPoint) iter.next();
//    			Rn.matrixTimesVector(verts[i], tmp.tt.getMatrix(), P3.originP3);
//     	}
    		// construct the vertex list for the curve
    		pointsForCurveRepn = 16 * controlPoints.size();
    		double[][] verts = new double[pointsForCurveRepn][4];
    		double dt = (tmax - tmin)/pointsForCurveRepn;
    		for (i = 0; i<pointsForCurveRepn;++i)	{
    			double tt = tmin + dt * i;
    			verts[i][0] = cbx.valueAt(tt);
    			verts[i][1] = cby.valueAt(tt);
    			verts[i][2] = cbz.valueAt(tt);
    			verts[i][3] = 1.0;
    			//double w = cbw.valueAt(t);
    			
    		}
     	curveRepresentation = IndexedLineSetUtility.createCurveFromPoints(verts, false);
     	if (theCurveItself == null)
     	{
     		theCurveItself = SceneGraphUtilities.createFullSceneGraphComponent();
     		addChild(theCurveItself);
     	}
     	
		theCurveItself.setGeometry(curveRepresentation);
//		theCurveItself.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		theCurveItself.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
		
		camIcon = new SceneGraphComponent();
		addChild(camIcon);
		camIconItself = Primitives.cameraIcon(iconScale);
   		for ( i = 0, iter = controlPoints.iterator(); iter.hasNext(); ++i )	{
   			ControlPoint tmp = (ControlPoint) iter.next();
			SceneGraphComponent sgc = new SceneGraphComponent();
			sgc.setTransformation(tmp.tt);
			sgc.addChild(camIconItself);
			camIcon.addChild(sgc);
		}
   		SceneGraphUtilities.setDefaultMatrix(this);
   		outOfDate = false;
    		}
    }
        
    public void setUnitSpeed()	{
    		double length = 0.0;
         Iterator iter;
    		int i;
    		double[] pt = new double[4];
    		double[] opt = new double[4];
    		for ( i = 0, iter = controlPoints.iterator(); iter.hasNext(); ++i )	{
    			ControlPoint cp = (ControlPoint) iter.next();
   			double m[] = cp.tt.getMatrix();
			pt[0] = m[3]; pt[1] = m[7]; pt[2] = m[11];  pt[3] = m[15];
   			if (i>0)	{
     			double d =  Pn.distanceBetween(pt, opt, signature);
    				cp.t  = length = length + d;
    			} else cp.t = 0.0;
    			System.arraycopy(pt, 0, opt, 0, 4);
    		}
		tmin = 0.0;
		tmax = length;
		outOfDate=true;
    }
    
    private int previousSegment = -20;
    public int getSegmentAtTime(double t)	{
   		if (outOfDate) update();
		// check to see if its in the previous segment: usually will be
   	   	if (previousSegment >= 0 && previousSegment <= (controlPoints.size() -1))	{
   			if (t >= ((ControlPoint) controlPoints.get(previousSegment)).getTime() && 
   					t < ((ControlPoint) controlPoints.get(previousSegment+1)).getTime()) 
   				return previousSegment;
   		}
   		Iterator iter;
	    	int i;
	    	if (t < tmin) return -1;
	    	if (t > tmax) return controlPoints.size();
	    	for ( i = 0, iter = controlPoints.iterator(); iter.hasNext(); ++i )	{
	    		ControlPoint cp = (ControlPoint) iter.next();
	    		if (t <= cp.t)   break;
	    	}
	    	if (i==0) return -1;
	    previousSegment = i-1;
	    return previousSegment;
    }
    
   public Transformation getValueAtTime(double t, Transformation dst)		{
    		if (controlPoints.size() == 1) 	{
    			ControlPoint cp = ((ControlPoint) controlPoints.get(0));
    			try {
					dst = ((Transformation) cp.tt.clone());
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			return dst;
    		}
    		if (dst == null) dst = new Transformation();
    		// assume 0<=t<=1  
    		// assume each segment is same length in parameter
    		if (outOfDate) update();
    		int whichSeg = getSegmentAtTime(t);
    		if (whichSeg == -1) {whichSeg = 0; t = tmin;}
    		else if (whichSeg >= controlPoints.size()-1) {t = tmax; whichSeg = controlPoints.size()-2; }
        	ControlPoint cp1 = (ControlPoint) controlPoints.get(whichSeg);
       	ControlPoint cp2 = (ControlPoint) controlPoints.get(whichSeg+1);
       	double dt = cp2.t - cp1.t;
       	if (currentPoint == null) {
       		currentPoint = new SceneGraphComponent();
       		currentPoint.setTransformation(new Transformation());
       		currentPoint.addChild(camIconItself);
       		camIcon.addChild(currentPoint);
       	}	// else ??
		double[] trans = new double[3];
		Quaternion rot = new Quaternion();
		if (useBSpline)	{
			trans[0] = cbx.valueAt(t);
			trans[1] = cby.valueAt(t);
			trans[2] = cbz.valueAt(t);
	      	dst.setTranslation(trans);		
			rot.re = cbr.valueAt(t);
			rot.x = cbi.valueAt(t);
			rot.y = cbj.valueAt(t);
			rot.z = cbk.valueAt(t);
			Quaternion.normalize(rot, rot);
	      	dst.setRotation(rot);	
		} else {
	     	AnimationUtility.linearInterpolation(dst, cp1.tt, cp2.tt, (t - cp1.t)/dt);
		}
       	currentPoint.getTransformation().setMatrix(dst.getMatrix());	 			
     	return dst;
    }
    
   public static FramedCurve readFromFile(String filename)		{
	File file = new File(filename);
	return readFromFile(file);
}

public static FramedCurve readFromFile(File file)		{
	FramedCurve fc = null;
	try {
		//File file = new File(filename);
		BufferedReader fin = new BufferedReader(new FileReader(file));
		fc = readFromFile(fin);
		fin.close();
			fc.sourceFile = file;
		}
		catch (java.io.IOException ev)	{
			ev.printStackTrace();
		}
		return fc;
    }
    
   public static FramedCurve readFromFile(BufferedReader fin)		{
			String ss;
			double[] mat = new double[16];
			double thist = 0;
			int count = 0;
			Vector cplist = new Vector();
			int signature = Pn.EUCLIDEAN;
			boolean firsttime = true;
			try {
				while ( (ss = fin.readLine()) != null)	{
					StringTokenizer st = new java.util.StringTokenizer(ss);
					while (st.hasMoreTokens())	{
						String firstT = st.nextToken();
						if (firstT.charAt(0) == '#') continue;
						if (count == 0) thist = Double.parseDouble(firstT);
						else mat[count-1] = Double.parseDouble(firstT);
						count++;
						if ((count % 17) == 0)	{	// read one matrix
							if (firsttime && signature == Pn.PROJECTIVE)	{	// first matrix!
								if (mat[12] == 0.0 && mat[13] == 0.0 && mat[14] == 0.0 && mat[15] == 1.0) signature = Pn.EUCLIDEAN;
								else {		// try to figure out the curvature;
									double[] row1 = new double[4];
									System.arraycopy(mat, 0, row1, 0, 4);
									if (Rn.innerProduct(row1, row1) > 1.01) signature = Pn.HYPERBOLIC;
									else signature = Pn.ELLIPTIC;
								}
								firsttime = false;
							}
							count = 0;
							Transformation gen = new Transformation(signature, mat);
							ControlPoint thiscp = new ControlPoint(gen, thist);
							cplist.add(thiscp);
						}
					}
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ControlPoint[] garray = new ControlPoint[0];
			garray = (ControlPoint[]) cplist.toArray(garray);
			FramedCurve fc = frameCurveFactory(garray);
			return fc;
	
}
    public  void writeToFile(String filename)		{
		if (outOfDate) update();
		File file = new File(filename);
		writeToFile(file);
    }
    
    public  void writeToFile(File file)		{
    		if (outOfDate) update();
 		try {
	        PrintWriter pw = new PrintWriter(new FileWriter(file));
	        Iterator iter;
	   		int i;
	   		for ( i = 0, iter = controlPoints.iterator(); iter.hasNext(); ++i )	{
	   			ControlPoint tmp = (ControlPoint) iter.next();
	   			pw.println(tmp.t);
	   			pw.print(Rn.matrixToString(tmp.tt.getMatrix()));
			}
			pw.close();
 		}
		catch (java.io.IOException ev)	{
			ev.printStackTrace();
		}
    }
    
    public ControlPoint getControlPoint(int i)	{
    		ControlPoint cp = null;
    		i = i%controlPoints.size();
    		if (outOfDate) update();
    		cp = (ControlPoint) controlPoints.elementAt(i);
    		return cp;
    }

	/**
	 * @param inspectedPoint
	 */
	public void deleteControlPoint(int i) {
		if (outOfDate) update();
		if (i>=controlPoints.size()) return;
		if (i<0) return;
		controlPoints.remove(i);
		outOfDate = true;
	}
	/**
	 * @param b
	 */
	public void setOutOfDate(boolean b) {
		outOfDate = b;
		
	}

	/**
	 * @return
	 */
	public int getNumberControlPoints() {
		return controlPoints.size();
	}
	public File getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	public void multiplyOnLeft(double[] mat)	{
		Iterator iter;
	    	for (iter = controlPoints.iterator(); iter.hasNext(); )	{
	    		ControlPoint cp = (ControlPoint) iter.next();
	    		cp.tt.multiplyOnLeft(mat);
	    	}
	}

	/**
	 * @param mat
	 */
	public void repair(double[] mat1, double[] mat2) {
		Iterator iter;
	    	for (iter = controlPoints.iterator(); iter.hasNext(); )	{
	    		ControlPoint cp = (ControlPoint) iter.next();
	    		//cp.tt.setMatrix(Rn.conjugateByMatrix(null, cp.tt.getMatrix(), mat));
	    		double [] tr = cp.tt.getMatrix();
	    		boolean toOriginal = false;
	    		if (toOriginal)	{
		    		tr[3] = tr[3] + mat1[3]; // - mat2[3];
		    		tr[7] = tr[7] + mat1[7]; // - mat2[7];
		    		tr[11] = tr[11] + mat1[11]; // - mat2[11];	
	    		} else {
		    		tr[3] = tr[3] - mat2[3];
		    		tr[7] = tr[7] - mat2[7];
		    		tr[11] = tr[11]  - mat2[11];	
	    			
	    		}
	    		cp.tt.setMatrix(tr);
	    	}
	}
 }
