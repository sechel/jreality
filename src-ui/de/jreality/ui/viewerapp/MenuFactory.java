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
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.Navigator.SelectionEvent;
import de.jreality.ui.viewerapp.actions.AddTool;
import de.jreality.ui.viewerapp.actions.LoadFile;
import de.jreality.ui.viewerapp.actions.LoadFileMerged;
import de.jreality.ui.viewerapp.actions.Quit;
import de.jreality.ui.viewerapp.actions.Remove;
import de.jreality.ui.viewerapp.actions.Render;
import de.jreality.ui.viewerapp.actions.ToggleAppearance;
import de.jreality.ui.viewerapp.actions.ToggleFullScreen;
import de.jreality.ui.viewerapp.actions.ToggleViewerFullScreen;


/**
 * at present to use the following way:<br>
 * <code><b><pre>
 * ViewerApp viewerApp = new ViewerApp(SceneGraphNode);
 * viewerApp.display();
 * <p>
 * MenuFactory.addMenuBar(viewerApp);
 * </pre></b></code>
 */
public class MenuFactory {

  public static String LOAD_FILE = "Load files";
  public static String LOAD_FILE_MERGED = "Load merged files";
  public static String QUIT = "Quit";
  public static String REMOVE = "Remove";
  public static String ADD_TOOL = "Add Tool";
  public static String TOGGLE_VERTEX_DRAWING = "Toggle vertex drawing";
  public static String TOGGLE_EDGE_DRAWING = "Toggle egde drawing";
  public static String TOGGLE_FACE_DRAWING = "Toggle face drawing";
  public static String TOGGLE_FULL_SCREEN = "Toggle full screen";
  public static String TOGGLE_FULL_VIEWER = "Toggle viewer full screen";
  public static String RENDER = "Force Rendering";
  
  private JFrame frame = null;
  private ViewerApp viewerApp = null;
  private SelectionManager sm = null;
  private ViewerSwitch viewerSwitch = null;
  

  public MenuFactory() {
    super();
  }
  
  
  public MenuFactory(ViewerApp v) {
    setFrame(v.getFrame());
    setViewerApp(v);
    setSelectionManager(v.getSelectionManager());
    setViewerSwitch(v.getViewerSwitch());
  }
  
  
  public void setFrame(JFrame frame) {
    this.frame = frame;
  }
  
  public void setViewerApp(ViewerApp viewerApp) {
    this.viewerApp = viewerApp;
  }

  public void setSelectionManager(SelectionManager sm) {
    this.sm = sm;
  }

  public void setViewerSwitch(ViewerSwitch viewerSwitch) {
    this.viewerSwitch = viewerSwitch;
  }

    

  public JMenuBar getMenuBar() {
    
    final JMenuBar menuBar = new JMenuBar();
    //create general actions
    final JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    fileMenu.add(new JMenuItem(new Quit(QUIT)));
    menuBar.add(fileMenu);

    if (sm == null) return menuBar;
    
    //create actions which require a SelectionManager
    final JMenu compMenu = new JMenu("Component");
    compMenu.setMnemonic(KeyEvent.VK_C);
    menuBar.add(compMenu);
    
    fileMenu.insert(new JMenuItem(new LoadFile(LOAD_FILE, sm, frame)), 0);
    fileMenu.insert(new JMenuItem(new LoadFileMerged(LOAD_FILE_MERGED, sm, frame)), 1);
    compMenu.add(new JMenuItem(new Remove(REMOVE, sm)));
    compMenu.add(new JMenuItem(new AddTool(ADD_TOOL, sm, frame)));
    
    final JMenu appMenu = new JMenu("Appearance");
    appMenu.setMnemonic(KeyEvent.VK_A);
    menuBar.add(appMenu);
    appMenu.add(new JMenuItem(new ToggleAppearance(TOGGLE_VERTEX_DRAWING, CommonAttributes.VERTEX_DRAW, sm)));
    appMenu.add(new JMenuItem(new ToggleAppearance(TOGGLE_EDGE_DRAWING, CommonAttributes.EDGE_DRAW, sm)));
    appMenu.add(new JMenuItem(new ToggleAppearance(TOGGLE_FACE_DRAWING, CommonAttributes.FACE_DRAW, sm)));
    
    final JMenu viewerMenu = new JMenu("Viewer");
    viewerMenu.setMnemonic(KeyEvent.VK_V);
    menuBar.add(viewerMenu);
    if (viewerApp != null)
      viewerMenu.add(new JMenuItem(ToggleViewerFullScreen.sharedInstance(TOGGLE_FULL_VIEWER, viewerApp)));
    viewerMenu.add(new JMenuItem(ToggleFullScreen.sharedInstance(TOGGLE_FULL_SCREEN, frame)));
    
    //create actions which require a ViewerSwitch
    if (viewerSwitch != null) {
      viewerMenu.addSeparator();
      
      String[] viewerNames = viewerSwitch.getViewerNames();
      ButtonGroup bg = new ButtonGroup();
      for (int i=0; i<viewerSwitch.getNumViewers(); i++) {
        final int index = i;
        final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
            new javax.swing.AbstractAction(viewerNames[index]){
              private static final long serialVersionUID = 1L;
              
              public void actionPerformed(ActionEvent e) {
                viewerSwitch.selectViewer(index);
                viewerSwitch.getCurrentViewer().renderAsync();
              }
        });
        item.setSelected(index==0);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + index, 0));
        bg.add(item);
        viewerMenu.add(item);
      }

      viewerMenu.addSeparator();
      JMenuItem mi = new JMenuItem(new Render(RENDER, viewerSwitch));
      mi.setAccelerator(KeyStroke.getKeyStroke("R"));
      viewerMenu.add(mi);
    }    
    
    //enable or disable menus depending on navigator selection
    if (viewerApp != null) {
      Navigator navigator = (Navigator) viewerApp.getNavigator();
      if (navigator != null) {
        navigator.getTreeSelectionModel().addTreeSelectionListener(
            new Navigator.SelectionListener() {
              @Override
              public void selectionChanged(SelectionEvent e) {
                compMenu.setEnabled(false);
                appMenu.setEnabled(false);
                if (e.selectionIsSGNode()) {
                  e.selectionAsSGNode().accept(new SceneGraphVisitor(){
                    @Override
                    public void visit(SceneGraphComponent c) {
                      compMenu.setEnabled(true);
                      appMenu.setEnabled(true);
                    }
                    @Override
                    public void visit(Appearance a) {
                      appMenu.setEnabled(true);
                    }
                  });
                }
              }
            });
      }
    }
    
    return menuBar;
  }
  
  
  /**
   * Adds a JMenuBar to a specified JFrame containing actions 
   * which can be performed on a SceneGraph.
   * @param frame the JFrame to which the MenuBar is added 
   * @param sm the SelectionManager required for most of the actions in the menu 
   * (if sm equals null the menu bar does only contain a few general actions)
   * @return the menu factory used to create the menu bar
   */
  public static MenuFactory addMenuBar(JFrame frame, SelectionManager sm) {
    MenuFactory menuFac = new MenuFactory();
    menuFac.setFrame(frame);
    menuFac.setSelectionManager(sm);
    frame.setJMenuBar( menuFac.getMenuBar() );
    frame.validate();
    
    return menuFac;
  }

  
  /**
   * Adds a JMenuBar to the specified ViewerApp containing actions 
   * which can be performed on a SceneGraph.
   * @param viewerApp the viewer application 
   * @return the menu factory used to create the menu bar
   */
  public static MenuFactory addMenuBar(ViewerApp viewerApp) {
    MenuFactory menuFac = new MenuFactory(viewerApp);
    menuFac.frame.setJMenuBar( menuFac.getMenuBar() );
    menuFac.frame.validate();
    
    return menuFac;
  }
  
   
//  public void setupNavigatorContextMenu() {
//    
//    if (navigator == null)
//      throw new UnsupportedOperationException("No navigator instantiated, call setNavigator(navigator)!");
//    
//    final JPopupMenu cm = createContextMenu();  //creates TreeSelectionListener
//    final JTree sceneTree = navigator.getSceneTree();
//    
//    sceneTree.addMouseListener(new MouseAdapter() {
//      
//      public void mousePressed( MouseEvent e ) {
//        handlePopup( e );
//      }
//      
//      public void mouseReleased( MouseEvent e ) {
//        handlePopup( e );
//      }
//      
//      private void handlePopup( MouseEvent e ) {
//        if ( e.isPopupTrigger() ) {
//          TreePath path = sceneTree.getPathForLocation( e.getX(), e.getY() );
//          if ( path != null ) {
//            TreeSelectionModel selectionModel = sceneTree.getSelectionModel();
//            selectionModel.clearSelection();  //ensures that SelectionListeners are notified even if path did not change
//            selectionModel.setSelectionPath( path );
//            cm.show( e.getComponent(), e.getX(), e.getY()+10 );
//          }
//        }
//      }
//    });//end mouse listener
//    
//  }
//  
//  
//  private JPopupMenu createContextMenu() {
//    JPopupMenu cm = new JPopupMenu();
//    cm.add(new JMenuItem(new LoadFile(LOAD_FILE, navigator, frame)));  //frame is allowed to be null
//    cm.add(new JMenuItem(new Remove(REMOVE, navigator)));
//    cm.add(new JMenuItem(new AddTool(ADD_TOOL, navigator, frame)));
//    return cm;
//  }

}