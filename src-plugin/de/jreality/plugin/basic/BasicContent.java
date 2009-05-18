package de.jreality.plugin.basic;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.SceneGraphUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class BasicContent extends AbstractContent {
		
	protected SceneGraphComponent contentCmp;
	
	protected SceneGraphNode oldContent;

	public BasicContent() {
		contentCmp=new SceneGraphComponent("content");
		contentCmp.setAppearance(new Appearance("content app"));
	}
	
	public void setContent(SceneGraphNode content) {
		if (oldContent != null) SceneGraphUtility.removeChildNode(contentCmp, oldContent);
		oldContent = content;
		SceneGraphUtility.addChildNode(contentCmp, content);
	}

	@Override
	public void install(Controller c) throws Exception {
		View view = c.getPlugin(View.class);
		contentCmp.setAppearance(new Appearance("content app"));
		view.getSceneRoot().addChild(contentCmp);
		view.setEmptyPickPath(new SceneGraphPath(view.getSceneRoot(), contentCmp));
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("aligned content");
	}

	@Override
	protected SceneGraphComponent getToolCmp() {
		return contentCmp;
	}

	public void contentChanged() {
		setContent(oldContent);
	}

	public Appearance getContentAppearance() {
		return contentCmp.getAppearance();
	}
	
}
