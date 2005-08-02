/*
 * Created on Mar 8, 2005
 *
 */
package de.jreality.jogl;


import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

/**
 * @author gunn
 *
 */
public class Snake extends IndexedLineSet {

	double[][] points;		// storage for points
	int[][] indices;
	int[] info;		// #beginning point and # of points
	public static Attribute SNAKE_INFO = Attribute.attributeForName("snakeInfo");
	boolean active = false;
	
	public Snake(double[][] p)	{
		super(p.length, 1);
		points = p;
		info = new int[3];
		info[0] = 0; info[1] = p.length; info[2] = -1;
		vertexAttributes.addWritable(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(points[0].length),points);
		update();
	}
	
	int[][] nullindices = {{0}};
	public void update()	{
			int begin = info[0];
			int length = info[1];
			int oldlength = info[2];
			if (length != oldlength) indices = new int[1][length];

			for (int i = 0; i<length; ++i)	{
				indices[0][i] = (i+begin)%points.length;
			}
			//setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(indices));
			if (length != oldlength)	{
				  nodeLock.writeLock();
				  try 
				  {
						edgeAttributes.remove(Attribute.INDICES);
						edgeAttributes.addWritable(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY, indices);
				  } finally {
				    nodeLock.writeUnlock();
				  }
			}
			info[2] = length;
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
