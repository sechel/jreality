/*
 * Created on Mar 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.tools;

import java.awt.event.MouseEvent;

import de.jreality.util.P3;
import de.jreality.util.Rn;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
			composite = Rn.conjugateByMatrix(null, myTransform.getMatrix(), tlate);
			Rn.times(composite, origM, composite);
		} else {
			composite = Rn.times(null, origM, myTransform.getMatrix());
		}
		theEditedTransform.setMatrix(composite);
		return true;
	}

	
}
