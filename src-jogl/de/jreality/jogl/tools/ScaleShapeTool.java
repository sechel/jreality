/*
 * Created on Mar 23, 2004
 *
 */
package de.jreality.jogl.tools;

import java.awt.event.MouseEvent;

import de.jreality.math.P3;
import de.jreality.math.Rn;

/**
 * @author Charles Gunn
 *
 */
public class ScaleShapeTool extends AbstractShapeTool {
	double	theScale;

	/**
	 * 
	 */
	public ScaleShapeTool() {
		super();
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#track(java.awt.event.MouseEvent)
	 */
	public boolean track(MouseEvent e) {
		if (!isTracking || !super.track(e)) return false;
		
		theScale = 1.0 + diff[1];
		myTransform.setStretch(theScale);
		double[] composite;
		// TODO figure out how to do scales in non-euc space, which aren't around the origin
		if (false)	{	//(signature != Pn.EUCLIDEAN)	
			double[] tlate = P3.makeTranslationMatrix(null, anchorV, signature);
			composite = Rn.conjugateByMatrix(null, myTransform.getArray(), tlate);
			Rn.times(composite, origM, composite);
		} else {
			composite = Rn.times(null, origM, myTransform.getArray());
		}
		theEditedTransform.setMatrix(composite);
		return true;
	}

	
}
