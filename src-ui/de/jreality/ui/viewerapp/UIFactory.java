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

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.Border;




/**
 * TODO: comment UIFactory
 */
class UIFactory {
  
  
  private Component viewer;
  private Component beanShell;
  private Component inspector;
  private JTree sceneTree;
  private final Border emptyBorder=BorderFactory.createEmptyBorder();
  
  private boolean attachNavigator = false;  //default
  private boolean attachBeanShell = false;  //default
  

  protected Component getViewer() {
    if (!attachNavigator && !attachBeanShell)
      return viewer;
    
    Component right = viewer;
    if (attachBeanShell) {
      JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
          viewer, scroll(beanShell));
      jsp.setContinuousLayout(true);
      jsp.setOneTouchExpandable(true);
      jsp.setResizeWeight(.01);
      jsp.setDividerLocation(420);
      jsp.setDividerLocation(Integer.MAX_VALUE);
      right = jsp;
    }
    
    if (attachNavigator) {
      JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
          scroll(sceneTree), scroll(inspector));
      left.setContinuousLayout(true);
      left.setResizeWeight(.1);
      
      JSplitPane content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
          left, right);
      content.setContinuousLayout(true);
      content.setOneTouchExpandable(true);
      content.setDividerLocation(260);
      
      return content;
    }

    return right;
  }
  
  
  //used in ViewerAppOld
  protected JScrollPane scroll(Component comp)
  {
    JScrollPane scroll = new JScrollPane(comp);
    scroll.setBorder(emptyBorder);
    return scroll;
  }

  
  protected void setViewer(Component component) {
    viewer = component;
  }

  
  protected void setBeanShell(Component component) {
    beanShell =component;
  }


  protected void setInspector(JComponent component) {
    component.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
    inspector = component;
  }

  protected void setSceneTree(JTree sceneTree) {
    this.sceneTree = sceneTree;
  }
  
  
  //for ViewerAppOld
  protected JTree getSceneTree() {
    return sceneTree;
  }
  
  
  protected void setAttachNavigator(boolean b) {
    attachNavigator = b;
  }

  protected void setAttachBeanShell(boolean b) {
    attachBeanShell = b;
  }

}
