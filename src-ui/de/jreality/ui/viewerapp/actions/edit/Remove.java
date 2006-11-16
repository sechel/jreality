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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.tool.Tool;
import de.jreality.ui.viewerapp.SelectionEvent;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.actions.AbstractSelectionListenerAction;
import de.jreality.util.SceneGraphUtility;


/**
 * Removes selected scene tree or scene graph nodes if they are not attribute entities or the root 
 * (otherwise this action is disabled).
 * 
 * @author msommer
 */
public class Remove extends AbstractSelectionListenerAction {

  private Tool tool = null;
  
  
  public Remove(String name, SelectionManager sm) {
    super(name, sm);

    setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    setShortDescription("Delete");
  }
  
  public Remove(String name, ViewerApp v) {
    this(name, v.getSelectionManager());
  }
  
  
  public void actionPerformed(ActionEvent e) {
    
    SceneGraphNode node = selection.getLastElement();  //the node to be removed
    SceneGraphPath parentPath = selection.popNew();  //selection.getLength() > 1
    final SceneGraphComponent parent = parentPath.getLastComponent();
    
    if (tool == null) {  //DEFAULT_SELECTION
      SceneGraphUtility.removeChildNode(parent, node);
      selectionManager.setSelection(parentPath);
    }
    else {  //TOOL_SELECTION
      selection.getLastComponent().removeTool(tool);
      selectionManager.setSelection(selection);
    }
  }

  
  @Override
  public void selectionChanged(SelectionEvent e) {
    super.selectionChanged(e);
    
    tool = (e.getType()==SelectionEvent.TOOL_SELECTION) ? e.getTool() : null;
  }
  
  
  @Override
  public boolean isEnabled(SelectionEvent e) {
    return (!e.entitySelected() &&
        !e.rootSelected() &&  //don't allow to remove the sceneRoot
        !e.nothingSelected());
  }
  
}