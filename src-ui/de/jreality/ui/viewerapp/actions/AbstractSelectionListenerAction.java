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

import de.jreality.scene.SceneGraphPath;
import de.jreality.ui.viewerapp.SelectionEvent;
import de.jreality.ui.viewerapp.SelectionListener;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.ViewerApp;


/**
 * Abstract class for actions used in jReality applications 
 * (usually used in the {@link de.jreality.ui.viewerapp.ViewerAppMenu}) 
 * which do something if the selection given by the underlying selection manager changes 
 * (e.g. which only act on specific scene tree or scene graph nodes
 * and have to disable or enable themselves based on the current selection).
 * 
 * @author msommer
 */
public abstract class AbstractSelectionListenerAction extends AbstractJrAction implements SelectionListener {

  protected SceneGraphPath selection;
  
  
  /**
   * Constructor for actions which act on a selection,
   * i.e. which have an underlying selection manager, 
   * and may need a parent component e.g. for displaying dialogs.
   * @param name the name of the action
   * @param sm the underlying selection manager
   * @param frame the parent component (e.g. for dialogs)
   * @throws IllegalArgumentException if sm is <code>null</code>
   */
  public AbstractSelectionListenerAction(String name, final SelectionManager sm, Component frame) {
    super(name, sm, frame);
    
    //set initial selection
    selectionChanged(new SelectionEvent(this, 
        sm.getSelection(), 
        sm.getTool(), 
        sm.getEntity(), 
        sm.getType()));
    
    selectionManager.addSelectionListener(this);
  }
  
  
  /**
   * Constructor for actions which act on a selection,
   * i.e. which have an underlying selection manager.
   * @param name the name of the action
   * @param sm the underlying selection manager
   * @throws IllegalArgumentException if sm is <code>null</code>
   */
  public AbstractSelectionListenerAction(String name, SelectionManager sm) {
    this(name, sm, null);
  }
  
  
  /**
   * Uses the ViewerApp's selection manager and frame.
   * @see AbstractSelectionListenerAction#AbstractSelectionListenerAction(String, SelectionManager, Component)
   */
  public AbstractSelectionListenerAction(String name, ViewerApp viewerApp) {
    this(name, viewerApp.getSelectionManager(), viewerApp.getFrame());
  }
  
  
  /**
   * Override this method to specify what to do when the selection changes.
   * @param e the selection event
   * @see AbstractSelectionListenerAction#isEnabled(SelectionEvent)
   */
  public void selectionChanged(SelectionEvent e) {
    selection = e.getSelection();
    
    setEnabled( isEnabled(e) );
  }
  
  
  /**
   * Override this method to specify when to disable or enable the action 
   * based on the current selection. <br>
   * This method is called in {@link AbstractSelectionListenerAction#selectionChanged(SelectionEvent)}.
   * @param e the selection event
   * @return true iff the action is enabled based on the current selection
   * @see AbstractSelectionListenerAction#selectionChanged(SelectionEvent)
   */
  public boolean isEnabled(SelectionEvent e) {
    return true;
  }
  
}