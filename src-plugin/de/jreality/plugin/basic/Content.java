package de.jreality.plugin.basic;

import static de.jreality.util.CameraUtility.encompass;

import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Pn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.tool.Tool;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;

public abstract class Content extends Plugin {

	protected SceneGraphNode
		content = null;
	
	public static enum ChangeEventType {
		ContentChanged,
		ContentReset,
		ToolAdded,
		ToolRemoved
	}
	
	public static class ContentChangedEvent {
		
		public ContentChangedEvent(ChangeEventType type) {
			this.type = type;
		}
		
		public ChangeEventType
			type = ChangeEventType.ContentChanged;
		public SceneGraphNode
			node = null;
		public Tool
			tool = null;
		
	}
	
	public static interface ContentChangedListener {
	
		public void contentChanged(ContentChangedEvent cce);
		
	}
	
	protected Scene
		scene = null;
	protected List<ContentChangedListener>
		listeners = new LinkedList<ContentChangedListener>();
	
	public abstract void setContent(SceneGraphNode content);
	
	protected void setContentNode(SceneGraphNode node) {
		content = node;
	}
	
	
	protected SceneGraphNode getContentNode() {
		return content;
	}
	
	
	public void resetContent() {
		fireContentChanged(new ContentChangedEvent(ChangeEventType.ContentReset));
	}
	
	
	public SceneGraphComponent getContentRoot() {
		return scene.getContentComponent();
	}
	
	protected boolean addContentToolImpl(Tool tool) {
		if (getToolComponent().getTools().contains(tool)) {
			return false;
		} else {
			getContentRoot().addTool(tool);
			ContentChangedEvent cce = new ContentChangedEvent(ChangeEventType.ToolAdded);
			cce.tool = tool;
			fireContentChanged(cce);
			return true;
		}
	}
	
	protected SceneGraphComponent getToolComponent() {
		return getContentRoot();
	}

	protected boolean removeContentToolImpl(Tool tool) {
		boolean removed = getToolComponent().removeTool(tool);
		if (removed) {
			ContentChangedEvent cce = new ContentChangedEvent(ChangeEventType.ToolRemoved);
			cce.tool = tool;
			fireContentChanged(cce);
		}
		return removed;
	}
	
	
	/**
	 * Add a content tool. Each Content implementation may reject adding/removing
	 * tools, which is signaled by the return value. The return value gives information
	 * if the tool is part of the Content tools after the method call (not if it was
	 * added due to this call, in contrast to the Collections API).
	 * 
	 * @param tool
	 * @return false if the Content rejects the given tool, true otherwise.
	 */
	public boolean addContentTool(Tool tool) {
		addContentToolImpl(tool);
		return true;
	}
	
	/**
	 * Remove a content tool.
	 * 
	 * @param tool
	 * @return true if the tool was removed, false if it was not set before or if removing is rejected.
	 */
	public boolean removeContentTool(Tool tool) {
		removeContentToolImpl(tool);
		return true;
	}
	
	public synchronized void fireContentChanged(ContentChangedEvent cce) {
		for (ContentChangedListener l : listeners) {
			l.contentChanged(cce);
		}
	}
	
	
	public synchronized void fireContentChanged() {
		ContentChangedEvent cce = new ContentChangedEvent(ChangeEventType.ContentChanged);
		for (ContentChangedListener l : listeners) {
			l.contentChanged(cce);
		}
	}
	
	
	public synchronized boolean addContentChangedListener(ContentChangedListener l) {
		return listeners.add(l);
	}
	
	public synchronized boolean removeContentChangedListener(ContentChangedListener l) {
		return listeners.remove(l);
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		scene = c.getPlugin(Scene.class);
	}
	
	public void encompassEuclidean() {
		SceneGraphPath avatarPath = scene.getAvatarPath();
		SceneGraphPath contentPath = scene.getContentPath();
		SceneGraphPath cameraPath = scene.getCameraPath();
		try {
			encompass(avatarPath, contentPath, cameraPath, 1.75, Pn.EUCLIDEAN);
		} catch (Exception e) {}
	}
	
}
