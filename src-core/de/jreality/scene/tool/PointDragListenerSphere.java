package de.jreality.scene.tool;


import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

/** The listener interface for receiving drag events (dragStart, drag, dragEnd),
 * signalling that a Point of a pointset is being dragged (with some unspecified
 * input device). */

public class PointDragListenerSphere implements PointDragListener {
	
	private PointSet pointSet;
	
	public PointDragListenerSphere(){
		
	}
	
	/** A drag action with some input device has begun. */
	
	public void pointDragStart(PointDragEvent e){
		 
	}
	
	/** A drag action with some input device has been continued. */
	
	public void pointDragged(PointDragEvent e){
		pointSet = e.getPointSet();
		double[][] points=new double[pointSet.getNumPoints()][];
        pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(points);
        points[e.getIndex()]=e.getPosition();  
        pointSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(points));
	}
	
	/** A drag action with some input device has finished. */
	
	public void pointDragEnd(PointDragEvent e){
		pointSet = e.getPointSet();
		double[][] points=new double[pointSet.getNumPoints()][];
        pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(points);
        points[e.getIndex()]=e.getPosition();  
        pointSet.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(points));
	
	}
}

