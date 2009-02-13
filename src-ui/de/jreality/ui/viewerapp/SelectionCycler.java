package de.jreality.ui.viewerapp;

import java.util.Iterator;
import java.util.Vector;

import de.jreality.scene.SceneGraphPath;
import de.jreality.util.LoggingSystem;

public class SelectionCycler {

	private SceneGraphPath currentCycleSelection;
	private Vector<SceneGraphPath> selectionList;
	private SelectionManagerInterface selectionManager;
	
	public SelectionCycler(SelectionManagerInterface sm)	{
		selectionManager = sm;
		selectionList = new Vector<SceneGraphPath>();
	}
	
	public void addSelection(SceneGraphPath p)	{
		Iterator iter = selectionList.iterator();
		while (iter.hasNext())	{
			SceneGraphPath sgp = (SceneGraphPath) iter.next();
			if (sgp.isEqual(p)) return;
		}
		selectionList.add(p);
		LoggingSystem.getLogger(this).fine("Adding path "+p.toString());
	}
		
	public void removeSelection(SceneGraphPath p)	{
		Iterator iter = selectionList.iterator();
		while (iter.hasNext())	{
			SceneGraphPath sgp = (SceneGraphPath) iter.next();
			if (sgp.isEqual(p)) {
				if (currentCycleSelection != null && 
						currentCycleSelection.equals(sgp)) cycleSelectionPaths();
				selectionList.remove(sgp);
				LoggingSystem.getLogger(this).info("Removing path "+p.toString());
				return;
			}
		}
	}
	
	public void clearSelections()	{
		selectionList.clear();
	}
		
	public void cycleSelectionPaths()	{
		int target = 0;
		if (selectionList == null || selectionList.size() == 0)		return;
		if (currentCycleSelection != null) {
			int which = selectionList.indexOf(currentCycleSelection);
			if (which != -1)  {
				target = (which + 1) % selectionList.size();
			}
		}
		currentCycleSelection = (SceneGraphPath) selectionList.get(target);
		LoggingSystem.getLogger(this).info("Cycling selection to "+currentCycleSelection.toString());
		selectionManager.setSelectionPath(currentCycleSelection);
	}
	

}
