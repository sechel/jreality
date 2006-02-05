/*
 * Created on Jun 21, 2005
 *
 * This file is part of the de.jreality.scene.tool package.
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
