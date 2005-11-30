/*
 * Author	gunn
 * Created on Nov 29, 2005
 *
 */
package de.jreality.reader.vrml;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.FactoredMatrix;

public class VRMLHelper {
	public static boolean verbose = false;
	
	public VRMLHelper() {
		super();
		// TODO Auto-generated constructor stub
	}

	// convert a List of Integer objects into an int[]
	public static int[] listToIntArray(List l)		{
		int[] foo = new int[l.size()];
		int count = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext()	)	{
			foo[count++] = ((Integer)iter.next()).intValue();
		}
		return foo;
	}
	public static double[] listToDoubleArray(List l)		{
		double[] foo = new double[l.size()];
		int count = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext()	)	{
			foo[count++] = ((Double)iter.next()).doubleValue();
		}
		return foo;
	}
	
//	IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
	public static double[][] listToDoubleArrayArray(List l)		{
		double[][] foo = new double[l.size()][];
		int count = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext()	)	{
			foo[count++] = ((double[])iter.next());
		}
		return foo;
	}
	
	public static int[][] convertIndices(int[] input)	{
		// count the number of negative entries
		int count = 0, subcount = 0;
		List breakpoints = new Vector();
		for (int i=0; i<input.length; ++i)	{
			if (input[i] < 0) breakpoints.add(new Integer(i));
		}
		count = breakpoints.size();
		int[][] output = new int[count][];
		int oldIndex, newIndex;
		newIndex = -1;
		int faceCount = 0;
		Iterator iter = breakpoints.iterator();
		while (iter.hasNext())	{
			oldIndex = newIndex+1;
			newIndex = ((Integer) iter.next()).intValue();
			output[faceCount] = new int[newIndex - oldIndex];
			for (int j = oldIndex; j<newIndex; ++j)	{
				output[faceCount][j-oldIndex] = input[j];
				if (verbose) System.err.print(input[j]+" ");
			}
			faceCount++;
			if (verbose) System.err.println("");
		}
		return output;
	}
	
	public void foo()	{
		FactoredMatrix fm = new FactoredMatrix();
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
	}

//	public static void viewSceneGraph(SceneGraphComponent r) {
//		InteractiveViewerDemo ivd = new InteractiveViewerDemo();
//		if (r.getTransformation() == null)
//			r.setTransformation(new Transformation());
//		if (r.getAppearance() == null)	
//			r.setAppearance(new Appearance());
//		r.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
//		ivd.viewer.getSceneRoot().addChild(r);
//		ivd.viewer.getCameraPath().getLastComponent().addChild(ivd.makeLights());
//		SceneGraphPath toSGC = new SceneGraphPath();
//		toSGC.push(ivd.viewer.getSceneRoot());
//		toSGC.push(r);
//		ivd.viewer.getSelectionManager().setSelection(toSGC);
//		ivd.viewer.getSelectionManager().setDefaultSelection(toSGC);
//		CameraUtility.encompass(ivd.viewer);
//		ivd.viewer.render();
//		
//	}

}
