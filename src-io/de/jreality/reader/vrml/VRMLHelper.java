/*
 * Author	gunn
 * Created on Nov 29, 2005
 *
 */
package de.jreality.reader.vrml;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.FactoredMatrix;

public class VRMLHelper {
	public static boolean verbose = false;
	
	public static final int DEFAULT = 1;
	public static final int OVERALL = 2;
	public static final int PER_PART = 3;
	public static final int PER_PART_INDEXED = 4;
	public static final int PER_FACE = 5;
	public static final int PER_FACE_INDEXED = 6;
	public static final int PER_VERTEX = 7;
	public static final int PER_VERTEX_INDEXED = 8;

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

	public static Color[] listToColorArray(List l)		{
		Color[] foo = new Color[l.size()];
		int count = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext()	)	{
			foo[count++] = ((Color)iter.next());
		}
		return foo;
	}
	

	public static int[][] convertIndices(int[] input)	{
		// count the number of negative entries
		int count = 0;
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
	
	public static double[] reallocate(double[] array)	{
		int n = array.length;
		double[] newarray = new double[n*2];
		System.arraycopy(array, 0, newarray, 0, n);
		return newarray;
	}

	public static int[] reallocate(int[] array)	{
		int n = array.length;
		int[] newarray = new int[n*2];
		System.arraycopy(array, 0, newarray, 0, n);
		return newarray;
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
