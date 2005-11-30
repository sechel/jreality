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

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.beans.PropertyEditorManager;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.beans.BooleanEditor;
import de.jreality.ui.beans.ColorEditor;
import de.jreality.ui.beans.DoubleEditor;
import de.jreality.ui.beans.IntegerEditor;
import de.jreality.ui.treeview.JTreeRenderer;
import de.jreality.ui.treeview.SceneTreeModel;

/**
 * TODO: comment UIFactory
 */
public class UIFactory
{
  static {
    PropertyEditorManager.registerEditor(Color.class, ColorEditor.class);
    PropertyEditorManager.registerEditor(Boolean.class, BooleanEditor.class);
    PropertyEditorManager.registerEditor(Double.class, DoubleEditor.class);
    PropertyEditorManager.registerEditor(Integer.class, IntegerEditor.class);
  }
  private SceneGraphComponent root;
  private Component viewer;
  private Component inspector;
  final Border emptyBorder=BorderFactory.createEmptyBorder(2,2,2,2);
  JTree sceneTree;

  JSplitPane content;
  JFrame frame;
  
  public JFrame createFrame()
  {
    if (frame == null) frame=new JFrame("Viewer");
    frame.setContentPane(createViewerContent());
    frame.pack();
    frame.setSize(Math.max(900, frame.getWidth()),
      Math.max(600, frame.getHeight()));
    frame.validate();
    //frame.show();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    return frame;
  }

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

  private Component createNavigation()
  {
    sceneTree=new JTree();
    sceneTree.setBorder(emptyBorder);
    SceneTreeModel model = new SceneTreeModel(root);
    sceneTree.setModel(model);
    sceneTree.setCellRenderer(new JTreeRenderer());

    return scroll(sceneTree);
  }

  private Component createViewerPanel()
  {
    return viewer;
  }

  private Component createInspectorPanel()
  {
    JScrollPane scroll = scroll(inspector);
    return scroll;
  }

  JScrollPane scroll(Component tree)
  {
    JScrollPane scroll=new JScrollPane(tree);
//    scroll.setBorder(BorderFactory.createEmptyBorder());
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

  public void setInspector(JComponent component)
  {
    inspector= component;
    inspector.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    component.setBorder(emptyBorder);
  }

  public void update() {
    createFrame();
  }

}
