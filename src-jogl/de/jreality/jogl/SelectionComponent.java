/*
 * Created on Mar 13, 2005
 *
 */
package de.jreality.jogl;

import de.jreality.scene.SceneGraphComponent;

/**
 * @author gunn
 *
 */
public class SelectionComponent extends SceneGraphComponent {
	int selectedChild = -1;

	/**
	 * 
	 */
	public SelectionComponent() {
		super();
	}
	
	
	/**
	 * @return Returns the selectedChild.
	 */
	public int getSelectedChild() {
		return selectedChild;
	}
	/**
	 * @param selectedChild The selectedChild to set.
	 */
	public void setSelectedChild(int sc) {
		if (sc == selectedChild) return;
		selectedChild = sc;
		int n = getChildComponentCount();
		for (int i = 0; i<n; ++i)	{
			if (i == selectedChild) getChildComponent(i).setVisible(true);
			else  getChildComponent(i).setVisible(false);
		}
	}
}
