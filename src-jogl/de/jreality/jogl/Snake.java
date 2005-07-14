/*
 * Created on Mar 8, 2005
 *
 */
package de.jreality.jogl;


import java.util.logging.Level;

import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

/**
 * @author gunn
 *
 */
public class Snake extends IndexedLineSet {

	double[][] points;		// storage for points
	int[] info;		// #beginning point and # of points
	public static Attribute SNAKE_POINTS = Attribute.attributeForName("snakePoints");
	public static Attribute SNAKE_INFO = Attribute.attributeForName("snakeInfo");
	boolean active = false;
	
	public Snake(double[][] p)	{
		super(p.length);
		points = p;
		info = new int[2];
		info[0] = 0;
		info[1] = p.length;
		setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(points[0].length).createWritableDataList(points));
		activate(true);
	}
	
	int[][] nullindices = {{0}};
	public void activate(boolean b)		{
		if (active == b) return;
		active = b;
		if (active) {
			setGeometryAttributes(SNAKE_POINTS, points);
			setGeometryAttributes(SNAKE_INFO, info);
			setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(nullindices));
		} else {
			setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(points[0].length).createReadOnly(points));
			int begin = info[0];
			int length = info[1];
			JOGLConfiguration.theLog.log(Level.FINE,"de-activating snake: "+begin+" "+length);
			int[][] indices = new int[1][length];
			for (int i = 0; i<length; ++i)	{
				indices[0][i] = (i+begin)%points.length;
			}
			setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(indices));
			setGeometryAttributes(SNAKE_POINTS, null);
			setGeometryAttributes(SNAKE_INFO, null);
		}
	}
	
	public void fireChange()	{
		fireGeometryChanged(null, null, null, null);
	}
	/**
	 * @return Returns the info.
	 */
	public int[] getInfo() {
		return info;
	}
}
