package de.jreality.plugin.basic;


/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.io.JrScene;
import de.jreality.io.JrSceneFactory;
import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.shader.ShaderUtility;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.ui.viewerapp.Selection;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerInterface;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;
import de.jreality.util.RenderTrigger;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;
import de.jtem.beans.ChangeEventMulticaster;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;


/**
 * @author pinkall
 */
public class View extends SideContainerPerspective {

	private transient ChangeListener changeListener;
	private ViewerSwitch viewerSwitch;
	private ToolSystem toolSystem;
	private RenderTrigger renderTrigger;
	private boolean autoRender = true;
	private boolean synchRender = false;

	private ToolSystemConfiguration toolSystemConfiguration;
	private RunningEnvironment runningEnvironment;
	private String toolConfig;
	private SceneGraphPath contentPath;

	public enum RunningEnvironment {
		PORTAL,
		PORTAL_REMOTE,
		DESKTOP
	};

	public View() {
		this(false);
	}
	
	public View(boolean loadDefaultScene) {

		// determine running environment
		String environment = Secure.getProperty(SystemProperties.ENVIRONMENT, SystemProperties.ENVIRONMENT_DEFAULT);
		if ("portal".equals(environment)) {
			runningEnvironment = RunningEnvironment.PORTAL; 
		} else if ("portal-remote".equals(environment)) {
			runningEnvironment = RunningEnvironment.PORTAL_REMOTE;
		} else {
			runningEnvironment = RunningEnvironment.DESKTOP;
		}

		// retrieve autoRender & synchRender system properties
		String autoRenderProp = Secure.getProperty(SystemProperties.AUTO_RENDER, SystemProperties.AUTO_RENDER_DEFAULT);
		if (autoRenderProp.equalsIgnoreCase("false")) {
			autoRender = false;
		}
		String synchRenderProp = Secure.getProperty(SystemProperties.SYNCH_RENDER, SystemProperties.SYNCH_RENDER_DEFAULT);
		if (synchRenderProp.equalsIgnoreCase("true")) {
			synchRender = true;
		}
		if (autoRender) {
			renderTrigger = new RenderTrigger();
		}
		if (synchRender) {
			if (autoRender) renderTrigger.setAsync(false);
			else LoggingSystem.getLogger(this).config("Inconsistant settings: no autoRender but synchRender!!");
		}

		// determine running environment
		toolConfig = Secure.getProperty(SystemProperties.TOOL_CONFIG, SystemProperties.TOOL_CONFIG_DEFAULT);

		// load tool system configuration
		toolSystemConfiguration = Secure.doPrivileged(new PrivilegedAction<ToolSystemConfiguration>() {
			public ToolSystemConfiguration run() {
				ToolSystemConfiguration cfg=null;
				// HACK: only works for "regular" URLs
				try {
					if (toolConfig.contains("://")) {
						cfg = ToolSystemConfiguration.loadConfiguration(new Input(new URL(toolConfig)));
					} else {
						if (toolConfig.equals("default")) cfg = ToolSystemConfiguration.loadDefaultDesktopConfiguration();
						if (toolConfig.equals("portal")) cfg = ToolSystemConfiguration.loadDefaultPortalConfiguration();
						if (toolConfig.equals("portal-remote")) cfg = ToolSystemConfiguration.loadRemotePortalConfiguration();
						if (toolConfig.equals("default+portal")) cfg = ToolSystemConfiguration.loadDefaultDesktopAndPortalConfiguration();
					}
				} catch (IOException e) {
					// should not happen
					e.printStackTrace();
				}
				if (cfg == null) throw new IllegalStateException("couldn't load config ["+toolConfig+"]");
				return cfg;
			}
		});

		// make viewerSwitch
		Viewer[] viewers = null;
		if (toolConfig.equals("portal-remote")) {
			String viewer = Secure.getProperty(SystemProperties.VIEWER, SystemProperties.VIEWER_DEFAULT_JOGL);
			try {
				viewers = new Viewer[]{(Viewer) Class.forName(viewer).newInstance()};
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			String viewer = Secure.getProperty(SystemProperties.VIEWER, SystemProperties.VIEWER_DEFAULT_JOGL+" "+SystemProperties.VIEWER_DEFAULT_SOFT); // de.jreality.portal.DesktopPortalViewer");
			String[] vrs = viewer.split(" ");
			List<Viewer> viewerList = new LinkedList<Viewer>();
			String viewerClassName;
			for (int i = 0; i < vrs.length; i++) {
				viewerClassName = vrs[i];
				try {
					Viewer v = (Viewer) Class.forName(viewerClassName).newInstance();
					viewerList.add(v);
				} catch (Exception e) { // catches creation problems - i. e. no jogl in classpath
					LoggingSystem.getLogger(this).info("could not create viewer instance of ["+viewerClassName+"]");
				} catch (NoClassDefFoundError ndfe) {
					System.out.println("Possibly no jogl in classpath!");
				} catch (UnsatisfiedLinkError le) {
					System.out.println("Possibly no jogl libraries in java.library.path!");
				}
			}
			viewers = viewerList.toArray(new Viewer[viewerList.size()]);
		}
		viewerSwitch = new ViewerSwitch(viewers);
		if (autoRender) {
			renderTrigger.addViewer(viewerSwitch);
		}

		if (loadDefaultScene) {
			JrScene scene = null;
			RunningEnvironment env = getRunningEnvironment();
			switch (env) {
			case DESKTOP:
				scene = JrSceneFactory.getDefaultDesktopSceneWithoutTools();
				break;
			case PORTAL:
				scene = JrSceneFactory.getDefaultPortalScene();
				break;
			case PORTAL_REMOTE:
				scene = JrSceneFactory.getDefaultPortalRemoteScene();
				break;
			}
			setScene(scene.getSceneRoot(), scene.getPath("cameraPath"), scene.getPath("emptyPickPath"), scene.getPath("avatarPath"));
		} else {
			SceneGraphComponent root = new SceneGraphComponent("root");
			Appearance rootAppearance = new Appearance("root appearance");
			//rootAppearance.setAttribute(CommonAttributes.ANY_DISPLAY_LISTS, false);
			ShaderUtility.createRootAppearance(rootAppearance);
			root.setAppearance(rootAppearance);
	
			SceneGraphPath emptyPickPath = new SceneGraphPath();
			emptyPickPath.push(root);
	
			setScene(root, null, emptyPickPath, null);
		}
		getContentPanel().setLayout(new GridLayout());
		getContentPanel().add(viewerSwitch.getViewingComponent());
		getContentPanel().setPreferredSize(new Dimension(600,600));
		getContentPanel().setMinimumSize(new Dimension(300, 200));
		
	}
	
	public ViewerSwitch getViewer()	{
		return viewerSwitch;
	}

	/**
	 * Get the root node of the scene.
	 * @return the root
	 */
	public SceneGraphComponent getSceneRoot() {
		return viewerSwitch.getSceneRoot();
	}

	public RunningEnvironment getRunningEnvironment() {
		return runningEnvironment;
	}

	public void setScene(
			SceneGraphComponent root,
			SceneGraphPath cameraPath,
			SceneGraphPath emptyPickPath,
			SceneGraphPath avatarPath
	) {
		boolean changed = false;

		if (root != viewerSwitch.getSceneRoot()) {
			changed = true;
			// make new root known to renderTrigger
			if (autoRender) {
				if (viewerSwitch.getSceneRoot() != null) {
					renderTrigger.removeSceneGraphComponent(viewerSwitch.getSceneRoot());
				}
				renderTrigger.addSceneGraphComponent(root);
			}

			// set the root of the viewer
			viewerSwitch.setSceneRoot(root);

			// make a new toolSystem
			if (toolSystem != null)	toolSystem.dispose();
			
			Secure.doPrivileged(new PrivilegedAction<ToolSystem>() {
				public ToolSystem run() {
					try {
						if (!("portal-remote".equals(toolConfig))) {
							toolSystem = new ToolSystem(
									viewerSwitch,
									toolSystemConfiguration,
									synchRender ? renderTrigger : null
							);
						}
						else  {
							try {
								Class<?> clazz = Class.forName("de.jreality.toolsystem.PortalToolSystemImpl");
								Class<? extends ToolSystem> portalToolSystem = clazz.asSubclass(ToolSystem.class);
								Constructor<? extends ToolSystem> cc = portalToolSystem.getConstructor(new Class[]{de.jreality.jogl.Viewer.class, ToolSystemConfiguration.class});
								de.jreality.jogl.Viewer cv = (de.jreality.jogl.Viewer) viewerSwitch.getCurrentViewer();
								toolSystem = cc.newInstance(new Object[]{cv, toolSystemConfiguration});
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					viewerSwitch.setToolSystem(toolSystem);
					return null;
				}
			});
		}
		if (cameraPath  != viewerSwitch.getCameraPath()) {
			viewerSwitch.setCameraPath(cameraPath);
			changed = true;
		}
		if (avatarPath  != toolSystem.getAvatarPath()) {
			toolSystem.setAvatarPath(avatarPath);
			changed = true;
		}
		if (emptyPickPath  != toolSystem.getEmptyPickPath()) {
			toolSystem.setEmptyPickPath(emptyPickPath);
			changed = true;
		}

		// set the default selection of the selection manager
		SelectionManagerInterface selectionManager =
			SelectionManager.selectionManagerForViewer(viewerSwitch);
		emptyPickPath = toolSystem.getEmptyPickPath();
		Selection s = new Selection(emptyPickPath);
		selectionManager.setDefaultSelection(new Selection(s));
		selectionManager.setSelection(s);

		//initialize tools in the new scene
		toolSystem.initializeSceneTools();

		// notify listeners
		if (changed) fireStateChanged();
	}

	public SceneGraphPath getCameraPath() {
		return viewerSwitch.getCameraPath();
	}

	public void setCameraPath(SceneGraphPath path) {
		if (path != viewerSwitch.getCameraPath()) {
			viewerSwitch.setCameraPath(path);
			fireStateChanged();
		}
	}

	/*
	 * Returns the avatarPath of the ToolSystem. The result is guaranteed to be
	 * non-null and to have the current scene root as its first element.
	 * 
	 * @return The avatarPath of the tool system.
	 */
	public SceneGraphPath getAvatarPath() {
		return toolSystem.getAvatarPath();
	}

	/*
	 * Sets the <code>avatarPath</code> of the ToolSystem. If <code>path</code> is
	 * <code>null</code>, the <code>avatarPath</code> will be set to the  last
	 * component of the camera path. Otherwise <code>path</code> has to have the
	 * current scene root as its first element (failure will result in exception).
	 * 
	 * @argument The new <code>avatarPath</code>.
	 */
	public void setAvatarPath(SceneGraphPath path) {
		if (path != toolSystem.getAvatarPath()) {
			if (path != null) {
				if (!path.isValid()) {
					throw new IllegalArgumentException("emptyPickPath is not a valid SceneGraphPath");
				}
				if (path.getFirstElement() != viewerSwitch.getSceneRoot()) {
					throw new IllegalArgumentException("emptyPickPath does not start with the current scene root");
				}
			} else {
				List<SceneGraphNode> nodes = getCameraPath().toList();
				nodes.remove(nodes.size()-1);
				path = new SceneGraphPath();
				for (SceneGraphNode node : nodes) {
					path.push(node);
				}
			}
			toolSystem.setAvatarPath(path);
			fireStateChanged();
		}
	}

	/*
	 * Returns the <code>emptyPickPath</code> of the ToolSystem. The result is
	 * guaranteed to be non-null and to have the current scene root as its first
	 * element.
	 * 
	 * @return The <code>emptyPickPath</code> of the tool system.
	 */
	public SceneGraphPath getEmptyPickPath() {
		return toolSystem.getEmptyPickPath();
	}

	/*
	 * Sets the emptyPickPath of the ToolSystem. If <code>path</code> is 
	 * <code>null</code>, the <code>emptyPickPath</code> will be set to a
	 * <code>SceneGraphPath</code> starting and ending at the scene root.
	 * Otherwise <code>path</code> has to have the current scene root as its
	 * first element (failure will result in exception).
	 * 
	 * @argument The new <code>emptyPickPath</code>.
	 */
	public void setEmptyPickPath(SceneGraphPath path) {
		if (path != toolSystem.getEmptyPickPath()) {
			if (path != null) {
				if (path.getFirstElement() != viewerSwitch.getSceneRoot()) {
					throw new IllegalArgumentException("emptyPickPath does not start with the current scene root");
				}
			} else {
				path = new SceneGraphPath();
				path.push(viewerSwitch.getSceneRoot());
			}
			toolSystem.setEmptyPickPath(path);
			fireStateChanged();
		}
	}
	
	private void fireStateChanged() {
		if (changeListener != null) {
			changeListener.stateChanged(new ChangeEvent(this));
		}
	}
	
	public void addChangeListener(ChangeListener l) {
		changeListener = ChangeEventMulticaster.add(changeListener, l);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		changeListener=ChangeEventMulticaster.remove(changeListener, listener);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Viewer";
		info.vendorName = "Ulrich Pinkall"; 
		info.icon = ImageHook.getIcon("hausgruen.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		if (autoRender) {
			renderTrigger.removeSceneGraphComponent(viewerSwitch.getSceneRoot());
			renderTrigger.removeViewer(viewerSwitch);
		}
		if (toolSystem != null) {
			toolSystem.dispose();
		}
		if (viewerSwitch != null) {
			viewerSwitch.dispose();
		}
	}

	public Icon getIcon() {
		return getPluginInfo().icon;
	}

	public String getTitle() {
		return getPluginInfo().name;
	}

	public void setVisible(boolean visible) {

	}

	public ToolSystem getToolSystem() {
		return toolSystem;
	}

	public SelectionManagerInterface getSelectionManager() {
		return SelectionManager.selectionManagerForViewer(getViewer());
	}

	public SceneGraphPath getContentPath() {
		return contentPath;
	}
	
}