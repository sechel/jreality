/*
 * Created on Mar 13, 2005
 *
 */
package de.jreality.jogl;


/**
 * @author gunn
 *
 */
public class LevelOfDetailComponent extends SelectionComponent {
	double screenExtent = 0.0;
	double[] lodLevels;
	/**
	 * @param numberOfLevels
	 */
	public LevelOfDetailComponent(double[] levels) {
		super();
		lodLevels = levels;
	}
	/**
	 * @return Returns the screenExtent.
	 */
	public double getScreenExtent() {
		return screenExtent;
	}
	/**
	 * @param screenExtent The screenExtent to set.
	 */
	public void setScreenExtent(double se) {
		if (screenExtent == se) return;
		screenExtent =  se;
		int i = 0;
		for ( i = 0; i<lodLevels.length-1; ++i)	{
			if (screenExtent < lodLevels[i]) break;
		}
		setSelectedChild(i);
	}
	
}
