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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
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
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.Border;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.treeview.JTreeRenderer;
import de.jreality.ui.treeview.SceneTreeModel;

/**
 * TODO: comment UIFactory
 */
public class UIFactory
{
  private SceneGraphComponent root;
  private Component viewer;
  private Component console;
  private Component inspector;
  final Border emptyBorder=BorderFactory.createEmptyBorder();
  JTree sceneTree;

  JSplitPane content;
  
  Container createViewerContent()
  {
    JSplitPane main = createLHS();
    content=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
      main, createViewerPanel());
    content.setContinuousLayout(true);
    content.setDividerLocation(260);
    content.setOneTouchExpandable(true);
    return content;
  }

  private JSplitPane createLHS() {
    JSplitPane main=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        createNavigation(), createInspectorPanel());
      main.setContinuousLayout(true);
      main.setResizeWeight(.1);
      return main;
  }

  Component createNavigation()
  {
    sceneTree=new JTree();
    SceneTreeModel model = new SceneTreeModel(root);
    sceneTree.setModel(model);
    sceneTree.setCellRenderer(new JTreeRenderer());
    return scroll(sceneTree);
  }

  Component createViewerPanel()
  {
    JSplitPane main=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        viewer, scroll(console));

    main.setContinuousLayout(true);
    main.setOneTouchExpandable(true);
    
    main.setResizeWeight(.01);
    
    main.setDividerLocation(420);
    main.setDividerLocation(Integer.MAX_VALUE);
    return main;
  }

  Component createInspectorPanel()
  {
    JScrollPane scroll = scroll(inspector);
    return scroll;
  }

  JScrollPane scroll(Component tree)
  {
    JScrollPane scroll=new JScrollPane(tree);
    scroll.setBorder(emptyBorder);
    return scroll;
  }

  public void setViewer(Component component)
  {
    viewer=component;
  }

  public void setConsole(Component component)
  {
    console=component;
  }

  public void setRoot(SceneGraphComponent component)
  {
    root= component;
  }

  public void setInspector(JComponent component)
  {
    component.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
    inspector= component;
  }

}
