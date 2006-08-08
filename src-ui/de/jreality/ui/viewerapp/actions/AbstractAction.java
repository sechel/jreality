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


package de.jreality.ui.viewerapp.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.treeview.SelectionEvent;
import de.jreality.ui.treeview.SelectionListener;
import de.jreality.ui.viewerapp.Navigator;


public abstract class AbstractAction extends javax.swing.AbstractAction {

  Component frame;
  Navigator navigator;
  SceneGraphComponent node;
  Object actee;
  
  
  public AbstractAction(String name, SceneGraphComponent node, Component frame) {
    super(name);
    
    if (node == null) 
      throw new IllegalArgumentException("Node is null!");
    
    this.frame = frame;
    this.node = node;
    actee = getDefaultActee();
  }
  
  
  public AbstractAction(String name, Navigator n, Component frame) {
    super(name);
    
    if (n == null) 
      throw new IllegalArgumentException("Navigator is null!");
    
    this.frame = frame;
    navigator = n;
    node = navigator.getRoot();
    actee = getDefaultActee();
    
    navigator.getTreeSelectionModel().addTreeSelectionListener(
        new SelectionListener(){
          public void selectionChanged(SelectionEvent e) {
            AbstractAction.this.selectionChanged(e);
          }
        });
  }
  
  
  Object getDefaultActee() {
    if (navigator != null)
      return navigator.getRoot();
    return node;
  }
  
  
  abstract void selectionChanged(SelectionEvent e);
  
  public abstract void actionPerformed(ActionEvent e);
  
}