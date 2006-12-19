package de.jreality.ui.viewerapp;

import de.jreality.scene.SceneGraphPath;

public interface SelectionManagerInterface {

	public abstract SceneGraphPath getDefaultSelection();

	public abstract void setDefaultSelection(SceneGraphPath defaultSelection);

	public abstract SceneGraphPath getSelection();

	public abstract void setSelection(SceneGraphPath selection);

	public abstract void addSelectionListener(SelectionListener listener);

	public abstract void removeSelectionListener(SelectionListener listener);

	public abstract boolean isRenderSelection();

	public abstract void setRenderSelection(boolean renderSelection);


}