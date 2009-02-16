package de.jreality.ui.viewerapp;

import de.jreality.scene.SceneGraphPath;

public interface SelectionManagerInterface  {

	public abstract Selection getDefaultSelection();

	public abstract void setDefaultSelection(Selection defaultSelection);

	public abstract Selection getSelection();

	public abstract void setSelection(Selection selection);

	
	/**
	 * @deprecated use {@link SelectionManagerInterface#getDefaultSelection()}
	 */
	public abstract SceneGraphPath getDefaultSelectionPath();

	/**
	 * @deprecated use {@link SelectionManagerInterface#setDefaultSelection(Selection)}
	 */
	public abstract void setDefaultSelectionPath(SceneGraphPath defaultSelection);

	/**
	 * @deprecated use {@link SelectionManagerInterface#getSelection()}
	 */
	public abstract SceneGraphPath getSelectionPath();

	/**
	 * @deprecated use {@link SelectionManagerInterface#setSelection(Selection)}
	 */
	public abstract void setSelectionPath(SceneGraphPath selection);

	
	public abstract void addSelectionListener(SelectionListener listener);

	public abstract void removeSelectionListener(SelectionListener listener);

	public abstract boolean isRenderSelection();

	public abstract void setRenderSelection(boolean renderSelection);

	// cycling functionality
	public abstract void addSelection(SceneGraphPath p);

	public abstract void removeSelection(SceneGraphPath p);

	public abstract void clearSelections();

	public abstract void cycleSelectionPaths();

}