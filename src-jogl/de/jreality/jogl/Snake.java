/*
 * Created on Mar 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl;


import java.util.Set;

import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Snake extends IndexedLineSet {

	double[][] points;		// storage for points
	int[] info;		// #beginning point and # of points
	public static Attribute SNAKE_POINTS = Attribute.attributeForName("snakePoints");
	public static Attribute SNAKE_INFO = Attribute.attributeForName("snakeInfo");
	
	public Snake(double[][] p, int[] i)	{
		super(p.length);
		points = p;
		info = i;
		setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(points[0].length).createWritableDataList(points));
		setGeometryAttributes(SNAKE_POINTS, points);
		setGeometryAttributes(SNAKE_INFO, info);
	}
	
	
	protected void fireGeometryChanged(Set vertexAttributeKeys,
			Set edgeAttributeKeys, Set faceAttributeKeys, Set geomAttributeKeys) {
		// TODO Auto-generated method stub
		super.fireGeometryChanged(vertexAttributeKeys, edgeAttributeKeys,
				faceAttributeKeys, geomAttributeKeys);
	}
	
	public void fireChange()	{
		fireGeometryChanged(null, null, null, null);
	}
}
