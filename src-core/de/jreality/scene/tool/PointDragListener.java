package de.jreality.scene.tool;

/** The listener interface for receiving drag events (dragStart, drag, dragEnd),
 * signalling that a Point of a pointset is being dragged (with some unspecified
 * input device). */

public interface PointDragListener extends java.util.EventListener {
	
	/** A drag action with some input device has begun. */
	
	public void pointDragStart(PointDragEvent e);
	
	/** A drag action with some input device has been continued. */
	
	public void pointDragged(PointDragEvent e);
	
	/** A drag action with some input device has finished. */
	
	public void pointDragEnd(PointDragEvent e);
}

