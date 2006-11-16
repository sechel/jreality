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


package de.jreality.ui.viewerapp.actions.edit;

import java.awt.Color;
import java.awt.event.ActionEvent;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;


/**
 * Switches the background color of a scene graph's root, 
 * which is used as the background color of the displaying viewer.  
 * 
 * @author msommer
 */
public class SwitchBackgroundColor extends AbstractJrAction {

  public static Color[] defaultColor = new Color[]{
    new Color(225, 225, 225), new Color(225, 225, 225),
    new Color(255, 225, 180), new Color(255, 225, 180), };
  
  private Color[] colors;
  private SceneGraphComponent sceneRoot;
  
  
  /**
   * Sets the scene root's background color.
   * @param name name of the action
   * @param colors array of colors with length = 1|4
   * @param sceneRoot the root of the scene graph
   */
  public SwitchBackgroundColor(String name, Color[] colors, SceneGraphComponent sceneRoot) {
    super(name);
    this.colors = colors;
    this.sceneRoot = sceneRoot;
    setShortDescription("Set the viewer's background color");
  }
  
  public SwitchBackgroundColor(String name, Color[] colors, ViewerApp viewerApp) {
    this(name, colors, viewerApp.getViewer().getSceneRoot());
  }
  
  public SwitchBackgroundColor(String name, Color[] colors, Viewer viewer) {
    this(name, colors, viewer.getSceneRoot());
  }
  
  
  @Override
  public void actionPerformed(ActionEvent e) {
    if (sceneRoot.getAppearance() != null) {
      if (colors.length == 1)
        colors = new Color[]{colors[0], colors[0], colors[0], colors[0]};
      sceneRoot.getAppearance().setAttribute("backgroundColors", colors);
    }
  }
  
}