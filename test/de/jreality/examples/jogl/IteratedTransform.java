/*
 * Created on Jan 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.examples.jogl;

import de.jreality.geometry.Primitives;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;

/**
 * @author gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class IteratedTransform extends SceneGraphComponent implements TransformationListener {
	int iterationCount = 0;
	boolean countChanged = true;
	Transformation theT;
	SceneGraphNode geometry;
	/**
	 * 
	 */
	public IteratedTransform(Transformation t, int itcount, SceneGraphNode g) {
		super();
		theT = t;
		setTransformation(theT);
		theT.addTransformationListener(this);
 		geometry = g;
		setIterationCount(itcount);
	}
	
	public IteratedTransform()	{
		this(new Transformation(), 1, Primitives.coloredCube());
	}

	/**
	 * @return
	 */
	public int getIterationCount() {
		return iterationCount;
	}

	/**
	 * @param i
	 */
	public void setIterationCount(int i) {
		if ( i == iterationCount) return;
		iterationCount = i;
		countChanged = true;
		update();
	}
	
	public void update()	{
		//Transformation theT = (Transformation) transforms.get(0);
		//if (theT == null)	theT = new Transformation();
		double[] theM = theT.getMatrix();
		if (countChanged) {
			SceneGraphComponent parent = this, child = null;
			for (int i = 0 ; i<iterationCount; ++i)	{
				child = new SceneGraphComponent();
				child.setName("itT"+i);
				child.setTransformation(theT);
				if (geometry instanceof Geometry) child.setGeometry((Geometry) geometry);
				else if (geometry instanceof SceneGraphComponent) child.addChild((SceneGraphComponent) geometry);
				//parent.removeChildren();
				parent.addChild(child);
				parent = child;
			}
			countChanged = false;
		} 
	}

		

	/* (non-Javadoc)
	 * @see de.jreality.scene.event.TransformationListener#transformationMatrixChanged(de.jreality.scene.event.TransformationEvent)
	 */
	public void transformationMatrixChanged(TransformationEvent ev) {
		update();

	}

}
