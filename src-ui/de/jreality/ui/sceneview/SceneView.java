package de.jreality.ui.sceneview;


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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import de.jreality.io.JrScene;
import de.jreality.io.JrSceneFactory;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.ui.viewerapp.Selection;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerInterface;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.ui.viewerapp.actions.file.ExportImage;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;
import de.jreality.util.RenderTrigger;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;


/**
 * @author pinkall
 */
public class SceneView extends ChangeEventSource {

	private ViewerSwitch viewerSwitch;
	private ToolSystem toolSystem;
	private RenderTrigger renderTrigger;
	private boolean autoRender = true;
	private boolean synchRender = false;

	private SelectionManagerInterface selectionManager;
	private JMenu menu;
	private ExportImage exportImageAction;
	private ToolSystemConfiguration toolSystemConfiguration;
	private String toolConfig;
	private SceneGraphComponent contentParent;

	/*
	 * Returns the the <code>SceneGraphComponent</code> that is meant to hold the principal
	 * content of the scene. Guaranteed to be non-null.
	 * @return the content parent
	 */
	public SceneGraphComponent getContentParent() {
		return contentParent != null ? contentParent : viewerSwitch.getSceneRoot();
	}

	/*
	 * Sets the <code>SceneGraphComponent</code> that is meant to hold the principal
	 * content of the scene. If <code>contentParent</code> is <code>null</code> then
	 * <code>getContentParent()</code> will henceforth return the scene root.
	 * 
	 * @argument the new <code>contentParent</code>
	 */
	public void setContentParent(SceneGraphComponent contentParent) {
		this.contentParent = contentParent;
	}

	public SceneView() { 
		
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
		
		// load tool system configuration
		toolConfig = Secure.getProperty(SystemProperties.TOOL_CONFIG, SystemProperties.TOOL_CONFIG_DEFAULT);
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
		
		// create selection manager
		selectionManager = SelectionManager.selectionManagerForViewer(viewerSwitch);
		
		// create default scene
		String environment = Secure.getProperty(SystemProperties.ENVIRONMENT, SystemProperties.ENVIRONMENT_DEFAULT);
		JrScene jrScene;
		if (environment.equals("desktop")) {
			jrScene = JrSceneFactory.getDefaultDesktopScene();
		} else if (environment.equals("portal")) {
			jrScene = JrSceneFactory.getDefaultPortalScene();
		} else if (environment.equals("portal-remote")) {
			jrScene = JrSceneFactory.getDefaultPortalRemoteScene();
		} else {
			throw new IllegalStateException("unknown environment: "+environment);
		}
		SceneGraphPath path = jrScene.getPath("emptyPickPath");
		SceneGraphComponent parent = null;
		if (path != null) {
			parent = path.getLastComponent();
		}
		setScene(
				jrScene.getSceneRoot(),
				jrScene.getPath("cameraPath"),
				jrScene.getPath("emptyPickPath"),
				jrScene.getPath("avatarPath"),
				parent
		);
		
		// set preferred size
		viewerSwitch.getViewingComponent().setPreferredSize(new Dimension(600,600));
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
	
	public JMenu getMenu() {
		if (menu == null) {
			menu = new JMenu("Viewer");
			String[] viewerNames = viewerSwitch.getViewerNames();
			ButtonGroup bgr = new ButtonGroup();
			for (int i=0; i<viewerSwitch.getNumViewers(); i++) {
				final int index = i;
				final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
						new javax.swing.AbstractAction(viewerNames[index]) {
							private static final long serialVersionUID = 1L;

							public void actionPerformed(ActionEvent e) {
								viewerSwitch.selectViewer(index);
								viewerSwitch.getCurrentViewer().renderAsync();
								if (exportImageAction!=null) exportImageAction.setEnabled(exportImageAction.isEnabled());
							}
						});
				item.setSelected(index==0);
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + index, 0));
				bgr.add(item);
				menu.add(item);
			}
			exportImageAction = new ExportImage(
					"Export Image",
					viewerSwitch,
					viewerSwitch.getViewingComponent()
			);
			menu.add(exportImageAction);
		}
		return menu;
	}
	
	public void setScene(SceneGraphComponent root, SceneGraphPath cameraPath) {
		setScene(root, cameraPath, null, null, null);
	}
	
	public void setScene(
			SceneGraphComponent root,
			SceneGraphPath cameraPath,
			SceneGraphPath emptyPickPath,
			SceneGraphPath avatarPath,
			SceneGraphComponent contentParent
	) {
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
					if (!(toolConfig == "portal-remote")) {
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

		setCameraPath(cameraPath);
		setAvatarPath(avatarPath);
		setEmptyPickPath(emptyPickPath);
		setContentParent(contentParent);

		toolSystem.initializeSceneTools();
		
		// set the default selection of the selection manager
		emptyPickPath = toolSystem.getEmptyPickPath();
		Selection s = new Selection(emptyPickPath);
		selectionManager.setDefaultSelection(new Selection(s));
		selectionManager.setSelection(s);
	}
	
	public SceneGraphPath getCameraPath() {
		return viewerSwitch.getCameraPath();
	}
	
	public void setCameraPath(SceneGraphPath path) {
		if (path != null) viewerSwitch.setCameraPath(path);
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
		if (path != null) {
			if (path.getFirstElement() != viewerSwitch.getSceneRoot()) {
				throw new IllegalArgumentException("emptyPickPath does not start with the current scene root");
			}
		} else {
			path = new SceneGraphPath();
			path.push(viewerSwitch.getSceneRoot());
		}
		toolSystem.setEmptyPickPath(path);
	}
	
	
	public void dispose() {
		if (autoRender) {
			renderTrigger.removeSceneGraphComponent(viewerSwitch.getSceneRoot());
			renderTrigger.removeViewer(viewerSwitch);
		}
		if (toolSystem != null) toolSystem.dispose();
	}
}