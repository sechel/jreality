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


package de.jreality.ui.viewerapp;

import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.AttributeEntity;
import de.jreality.scene.tool.Tool;


/**
 * Event for selections in the scene graph.
 * 
 * @author msommer
 */
public class SelectionEvent extends java.util.EventObject {
  
  public static final int NO_SELECTION = -1;  //nothing selected (e.g. no navigator)
  public static final int DEFAULT_SELECTION = 0;  //scene graph node selected
  public static final int TOOL_SELECTION = 1;  //tool selected
  public static final int ENTITY_SELECTION = 2;  //attribute entity selected (shader)
  
  final SceneGraphPath selection;
  private final int type;  //one of the static fields above
  
  private final Tool tool;
  private final AttributeEntity entity;
  
  
  /**
   * @param source the event's source
   * @param selection the path of the current selection
   * @param tool the currently selected tool (or <code>null</code> if no tool was selected)
   * @param entity the currently selected attribute entity (or <code>null</code> if no attribute entity was selected)
   * @param type the type of the current selection (use static fields of {@link SelectionEvent})
   */
  public SelectionEvent(Object source, SceneGraphPath selection, Tool tool, AttributeEntity entity, int type) {
    super(source);
    this.selection = selection;
    if (tool!=null && entity != null) throw new IllegalStateException("illegal selection!");
    this.tool = tool;
    this.entity = entity;
    this.type = type;
  }
  
  
  /** Get the current selection */
  public SceneGraphPath getSelection() {
    return selection;
  }

  /** Get the currently selected attribute entity (or <code>null</code> if no attribute entity was selected) */
  public AttributeEntity getEntity() {
    return entity;
  }

  /** Get the currently selected tool (or <code>null</code> if no tool was selected) */
  public Tool getTool() {
    return tool;
  }

  /** Get the type of the current selection */
  public int getType() {
    return type;
  }
  
  /** Returns true iff a {@link Tool} was selected */
  public boolean toolSelected() {
    return type == TOOL_SELECTION;
  }
  
  /** Returns true iff an {@link AttributeEntity} was selected */
  public boolean entitySelected() {
    return type == ENTITY_SELECTION;
  }
  
  /** Returns true iff nothing is selected */
  public boolean nothingSelected() {
    return type == NO_SELECTION;
  }
  
  /** Returns true iff the scene graph's root was selected */
  public boolean rootSelected() {
    return (selection.getLength()==1 && type==DEFAULT_SELECTION);
  }
  
  /** Returns true iff a {@link de.jreality.scene.SceneGraphNode} was selected */
  public boolean nodeSelected() {
    return type == DEFAULT_SELECTION;
  }
  
  /** Returns true iff a {@link SceneGraphComponent} was selected */
  public boolean componentSelected() {
    return (nodeSelected() && 
        selection.getLastElement() instanceof SceneGraphComponent);
  }
  
  /** Returns true iff a {@link Geometry} was selected */
  public boolean geometrySelected() {
    return (nodeSelected() && 
        selection.getLastElement() instanceof Geometry);
  }
  
  /** Returns true iff an {@link Appearance} was selected */
  public boolean appearanceSelected() {
    return (nodeSelected() && 
        selection.getLastElement() instanceof Appearance);
  }
  
  /** Returns true iff a {@link Transformation} was selected */
  public boolean transformationSelected() {
    return (nodeSelected() && 
        selection.getLastElement() instanceof Transformation);
  }
  
}