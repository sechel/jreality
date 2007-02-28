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

import static de.jreality.ui.viewerapp.SelectionEvent.DEFAULT_SELECTION;
import static de.jreality.ui.viewerapp.SelectionEvent.ENTITY_SELECTION;
import static de.jreality.ui.viewerapp.SelectionEvent.NO_SELECTION;
import static de.jreality.ui.viewerapp.SelectionEvent.TOOL_SELECTION;

import java.util.Vector;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.AttributeEntity;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;


/**
 * Manages selections of scene graph nodes within a scene graph displayed by a viewer.
 * 
 * @author msommer
 */
public class SelectionManager implements SelectionManagerInterface {
  
  private SceneGraphPath defaultSelection;
  private SceneGraphPath selection;

  private Vector<SelectionListener> listeners;
  private int type;  //current selection type (static field of SelectionEvent)
  private Tool tool = null;               //currently selected tool
  private AttributeEntity entity = null;  //currently selected attribute entity
  private boolean nothingSelected = true;  //true if default selection is selected by manager, e.g. setSelection(null)
  
  private boolean renderSelection = false;  //default
  private SceneGraphComponent selectionKit;
  private SceneGraphComponent selectionKitOwner;
  
  public SelectionManager(SceneGraphPath defaultSelection) {
    if (defaultSelection == null)
      throw new IllegalArgumentException("Default selection is null!");
    
    listeners = new Vector<SelectionListener>();
    
    //set default selection
    setDefaultSelection(defaultSelection);
    type = Integer.MIN_VALUE;  //initial value
    setSelection(null);
  }
  
  
  public SceneGraphPath getDefaultSelection() {
    return defaultSelection;
  }
  
  
  public void setDefaultSelection(SceneGraphPath defaultSelection) {
    this.defaultSelection = defaultSelection;
  }
  
  
  public SceneGraphPath getSelection() {
    return selection;
  }
  

  /**
   * Set the current selection. <br> 
   * (use this method if a {@link SceneGraphNode} is selected)
   * @param selection scene graph path to the current selection, 
   * <code>null</code> if nothing is selected (then the current selection is set to the default selection, 
   * but the selection's type is set to {@link SelectionEvent#NO_SELECTION})
   * @see SelectionManager#setSelection(SceneGraphPath, Tool, AttributeEntity)
   */
  public void setSelection(SceneGraphPath selection) {
	  setSelection(selection, null, null);
  }


  /**
   * Set the current selection. <br>
   * (use this method if a tool or an attribute entity is selected, 
   * i.e. the selection's path does not consist of {@link SceneGraphNode}s only)
   * @param selection scene graph path to the current selection 
   * (subpath of the selection's path if a tool or an attribute entity is selected), 
   * <code>null</code> if nothing is selected (then the current selection is set to the default selection, 
   * but the selection's type is set to {@link SelectionEvent#NO_SELECTION})
   * @param tool a selected tool (<code>selection.getLastElement()</code> is an instance of {@link SceneGraphComponent}), 
   * <code>null</code> if no tool is selected
   * @param entity a selected attribute entity (<code>selection.getLastElement()</code> is an instance of {@link Appearance})
   * <code>null</code> if no attribute entity is selected
   * @see SelectionManager#setSelection(SceneGraphPath)
   */
  public void setSelection(SceneGraphPath selection, Tool tool, AttributeEntity entity) {
	  if (isSelected(selection, tool, entity)) return;  //already selected
	  
	  if (selection == null)  //nothing selected
		  this.selection = defaultSelection;
	  else this.selection = selection;

	  nothingSelected = (selection == null);

	  this.tool = tool;
	  this.entity = entity;
	  //determine current selection type
	  if (tool == null && entity == null) 
		  type = (nothingSelected) ? NO_SELECTION : DEFAULT_SELECTION; 
	  else type = (tool != null) ? TOOL_SELECTION : ENTITY_SELECTION;

	  selectionChanged();
  }

  
  public boolean isSelected(SceneGraphPath selection, Tool tool, AttributeEntity entity) {

	  switch (type) {
	  case NO_SELECTION: return (selection == null);
	  case TOOL_SELECTION: return (this.tool == tool);
	  case ENTITY_SELECTION: return (this.entity == entity);
	  case DEFAULT_SELECTION: return (tool==null && entity==null && this.selection.equals(selection));
	  }

	  return false;
  }


  public void addSelectionListener(SelectionListener listener)  {
    if (listeners.contains(listener)) return;
    listeners.add(listener);
  }

  
  public void removeSelectionListener(SelectionListener listener) {
    listeners.remove(listener);
  }
  
  
  public void selectionChanged() {
    
    if (!listeners.isEmpty()) {
      for (int i = 0; i<listeners.size(); i++)  {
        SelectionListener l = listeners.get(i);
        l.selectionChanged(new SelectionEvent(this, selection, tool, entity, type));
      }
    }
    
    if (renderSelection) {
      if (nothingSelected) { 
        if (selectionKit != null) selectionKit.setVisible(false);
      }
      else {  //something selected
        updateBoundingBox();
        selectionKit.setVisible(true);
      }
    }
  }

  
  /**
   * Returns the current selection type (static field of {@link SelectionEvent})
   */
  public int getType() {
    return type;
  }
  

  private void updateBoundingBox() {
    
    if (selectionKit == null) {
      //set up representation of selection in scene graph
      selectionKit = new SceneGraphComponent("boundingBox");
      selectionKit.setOwner(this);
      Appearance app = new Appearance("app");
      app.setAttribute(CommonAttributes.EDGE_DRAW,true);
      app.setAttribute(CommonAttributes.FACE_DRAW,false);
      app.setAttribute(CommonAttributes.VERTEX_DRAW,false);
      app.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE,true);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_FACTOR, 1.0);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE_PATTERN, 0x6666);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 2.0);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DEPTH_FUDGE_FACTOR, 1.0);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
      app.setAttribute(CommonAttributes.LEVEL_OF_DETAIL,0.0);
      app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
      selectionKit.setAppearance(app);
    }
    
    if (selection.getLastComponent() == selectionKit) 
    	return;  //bounding box selected
    
    Rectangle3D bbox = GeometryUtility.calculateChildrenBoundingBox( selection.getLastComponent() ); 
    
    IndexedFaceSet box = null;
    box = IndexedFaceSetUtility.representAsSceneGraph(box, bbox);
    box.setGeometryAttributes(CommonAttributes.PICKABLE, false);
    
    selectionKit.setGeometry(box);
    if (selectionKitOwner!=null) selectionKitOwner.removeChild(selectionKit);
    selectionKitOwner = selection.getLastComponent();
    selectionKitOwner.addChild(selectionKit);
  }
  
  
  public boolean isRenderSelection() {
    return renderSelection;
  }

  
  public void setRenderSelection(boolean renderSelection) {
    this.renderSelection = renderSelection;
    if (renderSelection) updateBoundingBox();
    if (selectionKit != null) selectionKit.setVisible(renderSelection);
  }

  
  public AttributeEntity getEntity() {
    return entity;
  }

  
  public Tool getTool() {
    return tool;
  }

  
  public boolean isNothingSelected() {
    return nothingSelected;
  }

}