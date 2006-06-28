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


package de.jreality.scene.tool;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Panel;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.List;

/**
 * empty tool class that supports 
 * dispatching various awt events via a given component
 **/
public class AWTEventTool extends Tool {

  private Component comp;
  
  public AWTEventTool(Component c) {
    this.comp = c;
  }
  
  public AWTEventTool() {
    this(new Panel());
  }
  
  /* (non-Javadoc)
   * @see de.jreality.scene.tool.Tool#getActivationSlots()
   */
  public List getActivationSlots() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see de.jreality.scene.tool.Tool#getCurrentSlots()
   */
  public List getCurrentSlots() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see de.jreality.scene.tool.Tool#getOutputSlots()
   */
  public List getOutputSlots() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see de.jreality.scene.tool.Tool#activate(de.jreality.scene.tool.ToolContext)
   */
  public void activate(ToolContext tc) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see de.jreality.scene.tool.Tool#perform(de.jreality.scene.tool.ToolContext)
   */
  public void perform(ToolContext tc) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see de.jreality.scene.tool.Tool#deactivate(de.jreality.scene.tool.ToolContext)
   */
  public void deactivate(ToolContext tc) {
    // TODO Auto-generated method stub

  }
  
  public void addKeyListener(KeyListener l) {
    comp.addKeyListener(l);
  }
  public void addMouseListener(MouseListener l) {
    comp.addMouseListener(l);
  }
  public void addMouseMotionListener(MouseMotionListener l) {
    comp.addMouseMotionListener(l);
  }
  public void addMouseWheelListener(MouseWheelListener l) {
    comp.addMouseWheelListener(l);
  }
  public void removeKeyListener(KeyListener l) {
    comp.removeKeyListener(l);
  }
  public void removeMouseListener(MouseListener l) {
    comp.removeMouseListener(l);
  }
  public void removeMouseMotionListener(MouseMotionListener l) {
    comp.removeMouseMotionListener(l);
  }
  public void removeMouseWheelListener(MouseWheelListener l) {
    comp.removeMouseWheelListener(l);
  }
  public Component getComponent() {
    return comp;
  }
  
  /**
   * this method is safe as it executes in the DispatchThread.
   * @param e
   */
  public void dispatchEvent(final AWTEvent e) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        comp.dispatchEvent(e);
      }
    });
  }
}
