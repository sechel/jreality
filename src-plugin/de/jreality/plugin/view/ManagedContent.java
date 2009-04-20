package de.jreality.plugin.view;

import java.util.HashMap;
import java.util.Map;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.Tool;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class ManagedContent extends Plugin {

	private AlignedContent
		alignedContent = null;
	private SceneGraphComponent
		contentRoot = new SceneGraphComponent("Managed Content Root");
	private Map<Class<?>, SceneGraphComponent>
		contextMap = new HashMap<Class<?>, SceneGraphComponent>();
	
	
	private SceneGraphComponent createContextRoot(Class<?> context) {
		if (contextMap.get(context) == null) {
			SceneGraphComponent contextRoot = new SceneGraphComponent();
			contextRoot.setName(context.getSimpleName());
			contentRoot.addChild(contextRoot);
			contextMap.put(context, contextRoot);
		}
		SceneGraphComponent contextRoot = contextMap.get(context);
		return contextRoot;
	}
	
	
	
	public void addContent(Class<?> context, SceneGraphComponent c) {
		SceneGraphComponent contextRoot = createContextRoot(context);
		contextRoot.addChild(c);
		updateContent();
	}
	
	public void removeContent(Class<?> context, SceneGraphComponent c) {
		SceneGraphComponent contextRoot = contextMap.get(context);
		if (contextRoot == null) {
			return;
		}
		if (contextRoot.getChildComponents().contains(c)) {
			contextRoot.removeChild(c);
			updateContent();
		}
	}
	
	public void removeAll(Class<?> context) {
		SceneGraphComponent contextRoot = contextMap.get(context);
		if (contextRoot == null) {
			return;
		}
		contextMap.remove(context);
		if (contentRoot.getChildComponents().contains(contextRoot)) {
			contentRoot.removeChild(contextRoot);
			updateContent();
		}
	}
	
	
	public void addTool(Class<?> context, Tool tool) {
		SceneGraphComponent contextRoot = createContextRoot(context);
		if (!contextRoot.getTools().contains(tool)) {
			contextRoot.addTool(tool);
		}
	}
	
	public void removeTool(Class<?> context, Tool tool) {
		SceneGraphComponent contextRoot = createContextRoot(context);
		if (contextRoot.getTools().contains(tool)) {
			contextRoot.removeTool(tool);
		}
	}
	
	
	private void updateContent() {
		if (alignedContent.getContent() != contentRoot) {
			alignedContent.setContent(contentRoot);
		}
		alignedContent.contentChanged();
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Managed Content", "Stefan Sechelmann");
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		alignedContent = c.getPlugin(AlignedContent.class);
	}
	
}
