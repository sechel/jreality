package de.jreality.plugin.basic;

import java.io.File;

import javax.swing.Action;

import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.event.SceneGraphComponentEvent;
import de.jreality.scene.event.SceneGraphComponentListener;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.actions.file.LoadFile;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class ContentLoader extends Plugin {

	private ViewMenuBar 
		viewerMenuAggregator = null;

	private LoadFile
		contentLoadAction;

	Content content;
	
	public Action getAction() {
		return contentLoadAction;
	}
	
	SceneGraphComponent dummy;

	private View view;
	
	private void updateAction() {
		dummy = new SceneGraphComponent();
		contentLoadAction = new LoadFile("load file", dummy, view.getViewer());
		dummy.addSceneGraphComponentListener(new SceneGraphComponentListener() {
			public void childAdded(SceneGraphComponentEvent ev) {
				content.setContent(ev.getNewChildElement());
				updateAction(); 
			}
			public void childRemoved(SceneGraphComponentEvent ev) {}
			public void childReplaced(SceneGraphComponentEvent ev) {}
			public void visibilityChanged(SceneGraphComponentEvent ev) {}
		});
	}
	
	public void install(View sceneView) {
		view = sceneView;
		updateAction();
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Content Loader";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("diskette.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		View viewPlugin = c.getPlugin(View.class);
		content = c.getPlugins(Content.class).get(0);
		install(
				viewPlugin
		);
		viewerMenuAggregator = c.getPlugin(ViewMenuBar.class);
		viewerMenuAggregator.addMenuItem(getClass(), 0.0, contentLoadAction, "File");
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		viewerMenuAggregator.removeAll(getClass()); 
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		setCurrentDirectory(c.getProperty(getClass(), "currentDirectory", getCurrentDirectory()));
		super.restoreStates(c);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "currentDirectory", getCurrentDirectory());
		super.storeStates(c);
	}

	public String getCurrentDirectory() {
		return FileLoaderDialog.getLastDir().getAbsolutePath();
	}
	
 	public void setCurrentDirectory(String directory) {
		File dir = new File(directory);
		if (dir.exists() && dir.isDirectory()) {
			FileLoaderDialog.setLastDir(dir);
		} else {
			System.out.println(
					"failed to restore ContentLoader directory "+directory
			);
		}
	}
 	
}

