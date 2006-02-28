/*
 * Created on May 31, 2005
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.scene.tool;

import java.awt.Component;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickSystem;
import de.jreality.scene.tool.config.ToolSystemConfiguration;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;


/**
 *
 * TODO: comment this
 *
 * @author weissman
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
    this(viewer, loadConfiguration());
  }
  
  public ToolSystemViewer(Viewer viewer, ToolSystemConfiguration config) {
    this.viewer = viewer;
    toolSystem = new ToolSystem(viewer, config);
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
  
  public Component getViewingComponent() {
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

  public AnimationSystem getAnimationSystem() {
    return toolSystem.getAnimationSystem();
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
}
