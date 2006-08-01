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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.jreality.geometry.Primitives;
import de.jreality.ui.viewerapp.actions.AddTool;
import de.jreality.ui.viewerapp.actions.LoadFile;
import de.jreality.ui.viewerapp.actions.Quit;
import de.jreality.ui.viewerapp.actions.Remove;


/**
 * at present to use the following way:<br>
 * 
 * ViewerApp viewerApp = new ViewerApp(SceneGraphNode);
 * JFrame frame = viewerApp.display();
 *
 * MenuFactory menu = new MenuFactory(frame, viewerApp.getNavigator());
 * menu.addMenuToFrame();
 * menu.addContextMenuToNavigator(); 
 */
public class MenuFactory {

  public static String LOAD_FILE = "Load File";
  public static String REMOVE = "Remove";
  public static String ADD_TOOL = "Add Tool";
  public static String QUIT = "Quit";
  
  private JFrame frame = null;
  private Navigator navigator = null;
//  private ViewerSwitch viewerSwitch = null;
  
  
  public MenuFactory(JFrame frame, Navigator navigator) {
    this(frame, navigator, null);
  }
  
  
  public MenuFactory(JFrame frame, Navigator navigator, ViewerSwitch viewerSwitch) {
    this.frame = frame;
    this.navigator = navigator;
//    this.viewerSwitch = viewerSwitch;
  }
  
  
  public static void main(String[] args) {
    ViewerApp viewerApp = new ViewerApp(Primitives.icosahedron());
    viewerApp.setAttachNavigator(true);
    viewerApp.setAttachBeanShell(true);
    viewerApp.update();
    JFrame frame = viewerApp.display();
   
    MenuFactory menu = new MenuFactory(frame, viewerApp.getNavigator(), viewerApp.getViewerSwitch());
    menu.addMenuToFrame();
    menu.addContextMenuToNavigator(); 
  }
  
  
  public void addMenuToFrame() {
    frame.setJMenuBar(getMenu());
    frame.validate();
  }
  
  
  public JMenuBar getMenu() {
    
    JMenuBar menu = new JMenuBar();
    JMenu fileMenu = new JMenu("Menu");
    
    fileMenu.add(new JMenuItem(new LoadFile(LOAD_FILE, navigator, frame)));
    fileMenu.add(new JMenuItem(new Remove(REMOVE, navigator, frame)));
    fileMenu.add(new JMenuItem(new AddTool(ADD_TOOL, navigator, frame)));
    fileMenu.add(new JMenuItem(new Quit(QUIT, navigator, frame)));
    menu.add(fileMenu);
    
    return menu;
  }
  
  
  public static void addMenu(JFrame frame, Navigator navigator) {
    MenuFactory menu = new MenuFactory(frame, navigator);
    menu.addMenuToFrame();
  }

  
  public void addContextMenuToNavigator() {
    
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
    cm.add(new JMenuItem(new LoadFile(LOAD_FILE, navigator, frame)));
    cm.add(new JMenuItem(new Remove(REMOVE, navigator, frame)));
    cm.add(new JMenuItem(new AddTool(ADD_TOOL, navigator, frame)));
    return cm;
  }
  
  
  public static void addContextMenu(JFrame frame, Navigator navigator) {
    MenuFactory menu = new MenuFactory(frame, navigator);
    menu.addContextMenuToNavigator();
  }
}
