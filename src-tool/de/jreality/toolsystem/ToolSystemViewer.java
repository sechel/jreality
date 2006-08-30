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


package de.jreality.toolsystem;

import java.awt.Dimension;
import java.beans.Statement;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.pick.PickSystem;
import de.jreality.tools.AnimatorTask;
import de.jreality.tools.AnimatorTool;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;
import de.jreality.util.RenderTrigger;


/**
 * <p>
 * This class serves as the delegate for a given viewer, to manage
 * the creation and initialization of a {@link de.jreality.toolsystem.ToolSystem}.
 * Without an instance of this class or a similar class, the tools present in the
 * scene graph will not be properly initialized and activated.
 * </p>
 * 
 * <b>Usage:</b><br />
 * <ul>
 * <li>create a viewer instance, i. e. a {@link de.jreality.soft.DefaultViewer}</li>
 * <li>create a ToolSystemViewer and give it your viewer instance</li>
 * <li>set scene root and camera path on the viewer (either on the
 *     ToolSystemViewer or on the given instance)</li>
 * <li><em>Optional:</em> set the avatar path - default is the camera path</li>
 * <li><em>Optional:</em> set an empty pick path (pick path if nothing was picked) - default is the scene root</li>
 * <li>call <code>initializeTools()</code></li>
 * </ul>
 *
 * 
 * @author Steffen Weissmann
 *
 */
public class ToolSystemViewer implements Viewer {

  private Viewer viewer;
  private ToolSystem toolSystem;
  private SceneGraphPath emptyPickPath;
  private SceneGraphPath avatarPath;

  private static ToolSystemConfiguration loadConfiguration() {
    ToolSystemConfiguration config;
    try {
      String toolFile=System.getProperty("jreality.toolconfig");
      config = ToolSystemConfiguration.loadConfiguration(
          Input.getInput(toolFile)
      );
      LoggingSystem.getLogger(ToolSystemViewer.class).config("Using toolconfig="+toolFile);
    } catch (Exception e1) {
      config = ToolSystemConfiguration.loadDefaultConfiguration();
    }
    return config;
  }

  public ToolSystemViewer(Viewer viewer) {
    this(viewer, loadConfiguration(), null);
  }
  
  public ToolSystemViewer(Viewer viewer, RenderTrigger rt) {
    this(viewer, loadConfiguration(), rt);
  }
  
  public ToolSystemViewer(Viewer viewer, ToolSystemConfiguration config, RenderTrigger trigger) {
    this.viewer = viewer;
    toolSystem = new ToolSystem(viewer, config, trigger);
    setPickSystem(new AABBPickSystem());
  }
  
  public SceneGraphPath getCameraPath() {
    return viewer.getCameraPath();
  }
  
  public void setEmptyPickPath(SceneGraphPath emptyPickPath) {
    this.emptyPickPath = emptyPickPath;
    toolSystem.setEmptyPickPath(emptyPickPath);
  }

  public SceneGraphComponent getSceneRoot() {
    return viewer.getSceneRoot();
  }
  
  public int getSignature() {
    return viewer.getSignature();
  }
  
  public Object getViewingComponent() {
    return viewer.getViewingComponent();
  }
  
  public boolean hasViewingComponent() {
    return viewer.hasViewingComponent();
  }
  
  public void render() {
    viewer.render();
  }
  
  public void setCameraPath(SceneGraphPath p) {
    viewer.setCameraPath(p);
  }
  
  public void setSceneRoot(SceneGraphComponent r) {
    viewer.setSceneRoot(r);
  }
  
  public void setSignature(int sig) {
    viewer.setSignature(sig);
  }

  public void setPickSystem(PickSystem ps) {
    toolSystem.setPickSystem(ps);
  }

  public Viewer getDelegatedViewer() {
    return viewer;
  }
  
  public void schedule(Object key, AnimatorTask task) {
	  AnimatorTool.getInstance().schedule(key, task);
  }
  
  public void deschedule(Object key) {
	  AnimatorTool.getInstance().deschedule(key);
  }

  public PickSystem getPickSystem() {
    return toolSystem.getPickSystem();
  }

  public void setAvatarPath(SceneGraphPath p) {
    avatarPath = p;
    toolSystem.setAvatarPath(p);
  }
  
  public SceneGraphComponent getAuxiliaryRoot() {
    return viewer.getAuxiliaryRoot();
  }
  
  public void setAuxiliaryRoot(SceneGraphComponent ar) {
  	viewer.setAuxiliaryRoot(ar);
  }
  
  public void dispose() {
    toolSystem.dispose();
    Statement stm = new Statement(viewer, "dispose", null);
    try {
      stm.execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public SceneGraphPath getAvatarPath() {
    return avatarPath;
  }

  public SceneGraphPath getEmptyPickPath() {
    return emptyPickPath;
  }
  
  public void initializeTools() {
	toolSystem.initializeSceneTools();
  }

  public boolean canRenderAsync() {
    return viewer.canRenderAsync();
  }

  public Dimension getViewingComponentSize() {
    return viewer.getViewingComponentSize();
  }

  public void renderAsync() {
    viewer.renderAsync();
  }
}
