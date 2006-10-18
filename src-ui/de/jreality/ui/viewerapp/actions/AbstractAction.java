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

import javax.swing.KeyStroke;

import de.jreality.scene.SceneGraphPath;
import de.jreality.ui.viewerapp.SelectionEvent;
import de.jreality.ui.viewerapp.SelectionListener;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.ViewerApp;


public abstract class AbstractAction extends javax.swing.AbstractAction {

  protected Component frame;
  protected SceneGraphPath selection;
  
  
  protected AbstractAction(String name, final SelectionManager sm, Component frame) {
    super(name);
    
    if (sm == null) 
      throw new IllegalArgumentException("SelectionManager is null!");
    
    this.frame = frame;
    selection = sm.getSelection();
    
    sm.addSelectionListener(
        new SelectionListener(){
          public void selectionChanged(SelectionEvent e) {
            AbstractAction.this.selectionChanged(e);
          }
        });
  }
  
  
  protected AbstractAction(String name, SelectionManager sm) {
    this(name, sm, null);
  }
  
  
  protected AbstractAction(String name, ViewerApp viewerApp) {
    this(name, viewerApp.getSelectionManager(), viewerApp.getFrame());
  }
  
  
  protected AbstractAction(String name) {
    super(name);
  }
  
  
  protected void selectionChanged(SelectionEvent e) {
    selection = e.getSelection();
    
    if (isEnabled(e)) setEnabled(true);
    else setEnabled(false);
  }
  
  
  protected boolean isEnabled(SelectionEvent e) {
    return true;
  }
  
  
  public abstract void actionPerformed(ActionEvent e);
  
  
  protected void setAcceleratorKey(KeyStroke key) {
    putValue(ACCELERATOR_KEY, key);
  }
  
  
  protected void setName(String name) {
    putValue(NAME, name);
  }
  
}