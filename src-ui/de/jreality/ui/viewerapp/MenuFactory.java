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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.actions.AddTool;
import de.jreality.ui.viewerapp.actions.LoadFile;
import de.jreality.ui.viewerapp.actions.Quit;
import de.jreality.ui.viewerapp.actions.Remove;
import de.jreality.ui.viewerapp.actions.Render;


/**
 * at present to use the following way:<br>
 * 
 * ViewerApp viewerApp = new ViewerApp(SceneGraphNode);
 * viewerApp.display();
 *
 * MenuFactory menu = new MenuFactory(viewerApp);
 * menu.addMenuToFrame();
 * menu.addContextMenuToNavigator(); 
 */
public class MenuFactory {

  public static String LOAD_FILE = "Load File";
  public static String REMOVE = "Remove";
  public static String ADD_TOOL = "Add Tool";
  public static String QUIT = "Quit";
  public static String RENDER = "Render";
  
  private JFrame frame = null;
  private Navigator navigator = null;
  private SceneGraphComponent node = null;
  private ViewerSwitch viewerSwitch = null;
  

  public MenuFactory() {
    super();
  }
  
  
  public MenuFactory(ViewerApp v) {
    setFrame(v.getFrame());
    setNavigator(v.getNavigator());
    setNode(v.getNavigator().getRoot());
    setViewerSwitch(v.getViewerSwitch());
  }
  
  
  public void setFrame(JFrame frame) {
    this.frame = frame;
  }

  public void setNavigator(Navigator navigator) {
    this.navigator = navigator;
  }

  public void setNode(SceneGraphComponent node) {
    this.node = node;
  }

  public void setViewerSwitch(ViewerSwitch viewerSwitch) {
    this.viewerSwitch = viewerSwitch;
  }

    
  public void addMenuToFrame() {
    frame.setJMenuBar(getMenu());
    frame.validate();
  }
  
  
  public JMenuBar getMenu() {
    
    JMenuBar menu = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.add(new JMenuItem(new Quit(QUIT)));
    menu.add(fileMenu);

    if (navigator == null && node == null)
      return menu;
    
    JMenu editMenu = new JMenu("Edit");
    menu.add(editMenu);
    
    if (navigator != null) {
      fileMenu.insert(new JMenuItem(new LoadFile(LOAD_FILE, navigator, frame)), 0);
      editMenu.add(new JMenuItem(new Remove(REMOVE, navigator)));
      editMenu.add(new JMenuItem(new AddTool(ADD_TOOL, navigator, frame)));
    }
    else {
      fileMenu.insert(new JMenuItem(new LoadFile(LOAD_FILE, navigator, frame)), 0);
      //fileMenu.add(new JMenuItem(new Remove(REMOVE, navigator)));
      editMenu.add(new JMenuItem(new AddTool(ADD_TOOL, navigator, frame)));
    }
    
    if (viewerSwitch != null) {
      final JMenu viewerMenu = new JMenu("Viewer");
      menu.add(viewerMenu);
      
      String[] viewerNames = viewerSwitch.getViewerNames();
      ButtonGroup bg = new ButtonGroup();
      for (int i=0; i<viewerSwitch.getNumViewers(); i++) {
        final int ind = i;
        final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
            new javax.swing.AbstractAction(viewerNames[ind]){
              private static final long serialVersionUID = 1L;
              
              public void actionPerformed(ActionEvent e) {
                viewerSwitch.selectViewer(ind);
                viewerSwitch.getCurrentViewer().renderAsync();
              }
        });
        item.setSelected(ind==0);
        bg.add(item);
        viewerMenu.add(item);
      }

      viewerMenu.addSeparator();
      viewerMenu.add(new JMenuItem(new Render(RENDER, viewerSwitch.getCurrentViewer())));
    }    
    
    return menu;
  }
  
  
  public static void addMenu(Navigator navigator, JFrame frame) {
    MenuFactory menu = new MenuFactory();
    menu.setNavigator(navigator);
    menu.setFrame(frame);
    menu.addMenuToFrame();
  }

  
  public static void addMenu(SceneGraphComponent node, JFrame frame) {
    MenuFactory menu = new MenuFactory();
    menu.setNode(node);
    menu.setFrame(frame);
    menu.addMenuToFrame();
  }
  
  
  public void addContextMenuToNavigator() {
    
    if (navigator == null)
      throw new UnsupportedOperationException("No navigator instantiated, call setNavigator(navigator)!");
    
    final JPopupMenu cm = createContextMenu();  //creates TreeSelectionListener
    final JTree sceneTree = navigator.getSceneTree();
    
    sceneTree.addMouseListener(new MouseAdapter() {
      
      public void mousePressed( MouseEvent e ) {
        handlePopup( e );
      }
      
      public void mouseReleased( MouseEvent e ) {
        handlePopup( e );
      }
      
      private void handlePopup( MouseEvent e ) {
        if ( e.isPopupTrigger() ) {
          TreePath path = sceneTree.getPathForLocation( e.getX(), e.getY() );
          if ( path != null ) {
            TreeSelectionModel selectionModel = sceneTree.getSelectionModel();
            selectionModel.clearSelection();  //ensures that SelectionListeners are notified even if path did not change
            selectionModel.setSelectionPath( path );
            cm.show( e.getComponent(), e.getX(), e.getY()+10 );
          }
        }
      }
    });//end mouse listener
    
  }
  
  
  private JPopupMenu createContextMenu() {
    JPopupMenu cm = new JPopupMenu();
    cm.add(new JMenuItem(new LoadFile(LOAD_FILE, navigator, frame)));  //frame is allowed to be null
    cm.add(new JMenuItem(new Remove(REMOVE, navigator)));
    cm.add(new JMenuItem(new AddTool(ADD_TOOL, navigator, frame)));
    return cm;
  }
  
  
  public static void addContextMenu(Navigator navigator, JFrame frame) {
    //frame is allowed to be null
    MenuFactory menu = new MenuFactory();
    menu.setNavigator(navigator);
    menu.setFrame(frame);
    menu.addContextMenuToNavigator();
  }

}