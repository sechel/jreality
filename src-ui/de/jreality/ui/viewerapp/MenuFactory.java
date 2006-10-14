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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

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
import de.jreality.ui.viewerapp.actions.app.SwitchBackgroundColor;
import de.jreality.ui.viewerapp.actions.app.ToggleAppearance;
import de.jreality.ui.viewerapp.actions.camera.ShiftEyeSeparation;
import de.jreality.ui.viewerapp.actions.camera.ShiftFieldOfView;
import de.jreality.ui.viewerapp.actions.camera.ShiftFocus;
import de.jreality.ui.viewerapp.actions.camera.TogglePerspective;
import de.jreality.ui.viewerapp.actions.camera.ToggleStereo;
import de.jreality.ui.viewerapp.actions.comp.AddTool;
import de.jreality.ui.viewerapp.actions.comp.AssignFaceAABBTree;
import de.jreality.ui.viewerapp.actions.comp.Remove;
import de.jreality.ui.viewerapp.actions.comp.TogglePickable;
import de.jreality.ui.viewerapp.actions.file.LoadFile;
import de.jreality.ui.viewerapp.actions.file.LoadFileMerged;
import de.jreality.ui.viewerapp.actions.file.LoadScene;
import de.jreality.ui.viewerapp.actions.file.Quit;
import de.jreality.ui.viewerapp.actions.file.SaveScene;
import de.jreality.ui.viewerapp.actions.file.SaveSelected;
import de.jreality.ui.viewerapp.actions.viewer.Render;
import de.jreality.ui.viewerapp.actions.viewer.ToggleFullScreen;
import de.jreality.ui.viewerapp.actions.viewer.ToggleViewerFullScreen;


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
  public static String LOAD_SCENE = "Load scene";
  public static String SAVE_SCENE = "Save scene";
  public static String SAVE_SELECTED = "Save selected";
  public static String QUIT = "Quit";
  public static String REMOVE = "Remove";
  public static String ADD_TOOL = "Add Tools";
  public static String TOGGLE_PICKABLE = "Toggle pickable";
  public static String ASSIGN_FACE_AABBTREE = "Assign AABBTree";
  public static String TOGGLE_VERTEX_DRAWING = "Toggle vertex drawing";
  public static String TOGGLE_EDGE_DRAWING = "Toggle egde drawing";
  public static String TOGGLE_FACE_DRAWING = "Toggle face drawing";
  public static String BACKGROUND_COLOR = "Set background color";
  public static String TOGGLE_FULL_SCREEN = "Toggle full screen";
  public static String TOGGLE_FULL_VIEWER = "Toggle viewer full screen";
  public static String RENDER = "Force Rendering";
  public static String DECREASE_FIELD_OF_VIEW = "Decrease fieldOfView";
  public static String INCREASE_FIELD_OF_VIEW = "Increase fieldOfView";
  public static String DECREASE_FOCUS = "Decrease focus";
  public static String INCREASE_FOCUS = "Increase focus";
  public static String DECREASE_EYE_SEPARATION = "Decrease eyeSeparation";
  public static String INCREASE_EYE_SEPARATION = "Increase eyeSeparation";
  public static String TOGGLE_PERSPECTIVE = "Toggle perspective";
  public static String TOGGLE_STEREO = "Toggle stereo";
  
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
    
    menuBar.setBorder(null);
    
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
    
    //-- FILE MENU ---------------------------------
    fileMenu.insert(new JMenuItem(new LoadFile(LOAD_FILE, sm, viewerApp, frame)), 0);
    fileMenu.insert(new JMenuItem(new LoadFileMerged(LOAD_FILE_MERGED, sm, viewerApp, frame)), 1);
    fileMenu.insertSeparator(2);
    fileMenu.insert(new JMenuItem(new SaveSelected(SAVE_SELECTED, sm, frame)), 3);
    fileMenu.insertSeparator(4);
    if (viewerApp != null) {
      fileMenu.insert(new JMenuItem(new LoadScene(LOAD_SCENE, viewerApp, frame)), 2);
      fileMenu.insert(new JMenuItem(new SaveScene(SAVE_SCENE, viewerApp.getViewer(), frame)), 4);
    }
    
//    JMenu export = new JMenu("Export");
//    fileMenu.insertSeparator(7);
//    fileMenu.insert(export, 7);
//    export.add(new JMenuItem("RIB"));
////    mi.addActionListener(new ActionListener(){
////      public void actionPerformed(ActionEvent arg0) {
////        File file = FileLoaderDialog.selectTargetFile(frame,"rib", " renderman RIB");
////        if (file == null) return;
//////      try {
////        String fileName = file.getPath();
////        RIBViewer rv = new RIBViewer();
////        rv.initializeFrom(viewerSwitch);
////        rv.setFileName(fileName);
////        rv.render();
//////      System.out.println("file name is "+fileName);
////      }
////    });
//    
//    export.add(new JMenuItem("SVG"));
////    mi.addActionListener(new ActionListener(){
////      public void actionPerformed(ActionEvent arg0) {
////        File file = FileLoaderDialog.selectTargetFile(frame,"svg", " svg export");
////        if (file == null) return;
//////      try {
////        String fileName = file.getPath();
////        de.jreality.soft.SVGViewer rv = new de.jreality.soft.SVGViewer(fileName);
////        rv.initializeFrom(viewerSwitch);
////        rv.setWidth(viewerSwitch.getViewingComponent().getWidth());
////        rv.setHeight(viewerSwitch.getViewingComponent().getHeight());
////        rv.render();
//////      System.out.println("file name is "+fileName);
////      }
////    });

    
    //-- COMPONENT MENU ---------------------------
    compMenu.add(new JMenuItem(new Remove(REMOVE, sm)));
    compMenu.addSeparator();
    compMenu.add(new JMenuItem(new AddTool(ADD_TOOL, sm, frame)));
    compMenu.addSeparator();
    compMenu.add(new JMenuItem(new TogglePickable(TOGGLE_PICKABLE, sm)));
    compMenu.add(new JMenuItem(new AssignFaceAABBTree(ASSIGN_FACE_AABBTREE, sm)));
    
    //-- APPEARANCE MENU ---------------------------
    final JMenu appMenu = new JMenu("Appearance");
    appMenu.setMnemonic(KeyEvent.VK_A);
    menuBar.add(appMenu);
    appMenu.add(new JMenuItem(new ToggleAppearance(TOGGLE_VERTEX_DRAWING, CommonAttributes.VERTEX_DRAW, sm)));
    appMenu.add(new JMenuItem(new ToggleAppearance(TOGGLE_EDGE_DRAWING, CommonAttributes.EDGE_DRAW, sm)));
    appMenu.add(new JMenuItem(new ToggleAppearance(TOGGLE_FACE_DRAWING, CommonAttributes.FACE_DRAW, sm)));
    if (viewerApp != null) {  //background color of viewerApp
      JMenu bgColors = new JMenu(BACKGROUND_COLOR);
      ButtonGroup bg = new ButtonGroup();
      List<JRadioButtonMenuItem> items = new LinkedList<JRadioButtonMenuItem>();
      JRadioButtonMenuItem def = new JRadioButtonMenuItem(new SwitchBackgroundColor("default", SwitchBackgroundColor.defaultColor, viewerApp)); 
      def.setSelected(true);
      items.add( def );
      items.add( new JRadioButtonMenuItem(new SwitchBackgroundColor("white", new Color[]{Color.WHITE}, viewerApp)) );
      items.add( new JRadioButtonMenuItem(new SwitchBackgroundColor("gray", new Color[]{new Color(225, 225, 225)}, viewerApp)) );
      for (JRadioButtonMenuItem item : items) {
        bg.add(item);
        bgColors.add(item);
      }
      appMenu.addSeparator();
      appMenu.add(bgColors);
    }
    
    //-- VIEWER MENU -------------------------------
    final JMenu viewerMenu = new JMenu("Viewer");
    viewerMenu.setMnemonic(KeyEvent.VK_V);
    menuBar.add(viewerMenu);
    if (viewerApp != null)
      viewerMenu.add(new JMenuItem(ToggleViewerFullScreen.sharedInstance(TOGGLE_FULL_VIEWER, viewerApp)));
    viewerMenu.add(new JMenuItem(ToggleFullScreen.sharedInstance(TOGGLE_FULL_SCREEN, frame)));
    
    //create actions which require a Viewer
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
      viewerMenu.add(new JMenuItem(new Render(RENDER, viewerSwitch)));
      
      //-- CAMERA MENU -------------------------------
      final JMenu cameraMenu = new JMenu("Camera");
      cameraMenu.setMnemonic(KeyEvent.VK_M);
      menuBar.add(cameraMenu, 3);
      cameraMenu.add(new JMenuItem(new ShiftFieldOfView(DECREASE_FIELD_OF_VIEW, viewerSwitch, true)));
      cameraMenu.add(new JMenuItem(new ShiftFieldOfView(INCREASE_FIELD_OF_VIEW, viewerSwitch, false)));
      cameraMenu.addSeparator();
      cameraMenu.add(new JMenuItem(new ShiftFocus(DECREASE_FOCUS, viewerSwitch, true)));
      cameraMenu.add(new JMenuItem(new ShiftFocus(INCREASE_FOCUS, viewerSwitch, false)));
      cameraMenu.addSeparator();
      cameraMenu.add(new JMenuItem(new ShiftEyeSeparation(DECREASE_EYE_SEPARATION, viewerSwitch, true)));
      cameraMenu.add(new JMenuItem(new ShiftEyeSeparation(INCREASE_EYE_SEPARATION, viewerSwitch, false)));
      cameraMenu.addSeparator();
      cameraMenu.add(new JMenuItem(new TogglePerspective(TOGGLE_PERSPECTIVE, viewerSwitch)));
      cameraMenu.add(new JMenuItem(new ToggleStereo(TOGGLE_STEREO, viewerSwitch)));
    }    
    
    
    //enable or disable menus depending on navigator selection
    if (viewerApp != null && viewerApp.isAttachNavigator()) {
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