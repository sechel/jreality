/*
 * Created on Jun 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl;

import junit.framework.TestCase;
import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.SphereHelper;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.util.Rn;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BezierCurveTest extends TestCase {

	public void testRefine()	{
		double[][] pts = {{1,1,0},{1,0,0},{0,0,0},{0,1,0}};
	
		BezierCurve bc = new BezierCurve(pts);
		double[][] split = bc.refine();
		System.out.println("Split = \n"+Rn.toString(split));
	}
	
	public void testRefineU()	{
		double[][][] pts = 
		{{{1,1,0},{1,0,0},{0,0,0},{0,1,0}},
		{{1,1,1},{1,0,1},{0,0,1},{0,1,1}},
		{{1,1,2},{1,0,2},{0,0,2},{0,1,2}},
		{{1,1,3},{1,0,3},{0,0,3},{0,1,3}}};
	
		double[][][] split = BezierCurve.refineU(pts);
		for (int i = 0; i<split.length; ++i)
			System.out.println("Split = \n"+Rn.toString(split[i]));
		split = BezierCurve.refineV(pts);
		for (int i = 0; i<split.length; ++i)
			System.out.println("Split = \n"+Rn.toString(split[i]));
	}
	
	public void testPatchMesh()	{
		BezierPatchMesh pbm = new BezierPatchMesh(2,2,new double[3][3][3]);
	}
	
	public void  testSphereHelper()	{
		IndexedFaceSet ifs = SphereHelper.sphereAsTriangStrip;
	}
}
