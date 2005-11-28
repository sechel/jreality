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

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.Border;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.treeview.SceneTreeViewer;

/**
 * TODO: comment UIFactory
 */
public class UIFactory
{
  private SceneGraphComponent root;
  private Component viewer;
  private Component inspector;
  final Border emptyBorder=BorderFactory.createEmptyBorder();
  SceneTreeViewer sceneTree;

  public JFrame createFrame()
  {
    JFrame frame=new JFrame("Viewer");
    frame.setContentPane(createViewerContent());
    frame.pack();
    frame.setSize(Math.max(800, frame.getWidth()),
      Math.max(600, frame.getHeight()));
    frame.validate();
    //frame.show();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    return frame;
  }

  Container createViewerContent()
  {
    JSplitPane main=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
      createNavigation(), createViewerPanel());
    main.setResizeWeight(.5);
    JSplitPane content=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
      main, createInspectorPanel());
    return content;
  }

  private Component createNavigation()
  {
    sceneTree=new SceneTreeViewer();
    sceneTree.setRoot(root);
    return scroll(sceneTree);
  }

  private Component createViewerPanel()
  {
    return viewer;
  }

  private Component createInspectorPanel()
  {
    return scroll(inspector);
  }

  private JScrollPane scroll(Component tree)
  {
    JScrollPane scroll=new JScrollPane(tree);
    scroll.setBorder(emptyBorder);
    return scroll;
  }

  public void setViewer(Component component)
  {
    viewer=component;
  }


  public void setRoot(SceneGraphComponent component)
  {
    root= component;
  }

  public void setInspector(Component component)
  {
    inspector= component;
  }

}
