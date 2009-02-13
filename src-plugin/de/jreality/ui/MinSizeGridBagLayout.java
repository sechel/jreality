/**
 * 
 */
package de.jreality.ui;

import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagLayoutInfo;

public class MinSizeGridBagLayout extends GridBagLayout {
	
	private static final long 
		serialVersionUID = 1L;

	@Override
	protected GridBagLayoutInfo getLayoutInfo(Container parent, int sizeflag) {
		return super.getLayoutInfo(parent, GridBagLayout.MINSIZE);
	}
	
}