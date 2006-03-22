/*
 * Created on May 13, 2005
 *
 * This file is part of the jReality package.
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
package de.jreality.ui.viewerapp;

import java.awt.Component;
import java.awt.Container;

import javax.swing.*;
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
