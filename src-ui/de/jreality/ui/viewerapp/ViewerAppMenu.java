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
import java.beans.Beans;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import de.jreality.renderman.RIBViewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.actions.app.SwitchBackgroundColor;
import de.jreality.ui.viewerapp.actions.app.ToggleAppearance;
import de.jreality.ui.viewerapp.actions.camera.ShiftEyeSeparation;
import de.jreality.ui.viewerapp.actions.camera.ShiftFieldOfView;
import de.jreality.ui.viewerapp.actions.camera.ShiftFocus;
import de.jreality.ui.viewerapp.actions.camera.TogglePerspective;
import de.jreality.ui.viewerapp.actions.camera.ToggleStereo;
import de.jreality.ui.viewerapp.actions.edit.AddTool;
import de.jreality.ui.viewerapp.actions.edit.AssignFaceAABBTree;
import de.jreality.ui.viewerapp.actions.edit.Remove;
import de.jreality.ui.viewerapp.actions.edit.TogglePickable;
import de.jreality.ui.viewerapp.actions.file.ExportImage;
import de.jreality.ui.viewerapp.actions.file.ExportPS;
import de.jreality.ui.viewerapp.actions.file.ExportRIB;
import de.jreality.ui.viewerapp.actions.file.ExportSVG;
import de.jreality.ui.viewerapp.actions.file.LoadFile;
import de.jreality.ui.viewerapp.actions.file.LoadFileMerged;
import de.jreality.ui.viewerapp.actions.file.LoadScene;
import de.jreality.ui.viewerapp.actions.file.Quit;
import de.jreality.ui.viewerapp.actions.file.SaveScene;
import de.jreality.ui.viewerapp.actions.file.SaveSelected;
import de.jreality.ui.viewerapp.actions.view.Render;
import de.jreality.ui.viewerapp.actions.view.ToggleBeanShell;
import de.jreality.ui.viewerapp.actions.view.ToggleFullScreen;
import de.jreality.ui.viewerapp.actions.view.ToggleMenu;
import de.jreality.ui.viewerapp.actions.view.ToggleNavigator;
import de.jreality.ui.viewerapp.actions.view.ToggleRenderSelection;
import de.jreality.ui.viewerapp.actions.view.ToggleViewerFullScreen;


/**
 * Creates the viewerApp's menu bar and contains static fields
 * for names of menus and actions.
 * 
 * @author msommer
 */
public class ViewerAppMenu {

  //menu names
  public static String FILE_MENU = "File";
  public static String EDIT_MENU = "Edit";
  public static String APP_MENU = "Appearance";
  public static String CAMERA_MENU = "Camera";
  public static String VIEW_MENU = "View";
  
  //FILE MENU
  public static String LOAD_FILE = "Load files";
  public static String LOAD_FILE_MERGED = "Load merged files";
  public static String LOAD_SCENE = "Load scene";
  public static String SAVE_SCENE = "Save scene";
  public static String SAVE_SELECTED = "Save selected";
  public static String EXPORT = "Export";
  public static String QUIT = "Quit";
  //EDIT MENU
  public static String REMOVE = "Remove";
  public static String ADD_TOOL = "Add Tools";
  public static String TOGGLE_PICKABLE = "Toggle pickable";
  public static String ASSIGN_FACE_AABBTREE = "Assign AABBTree";
  //APPEARANCE MENU
  public static String TOGGLE_VERTEX_DRAWING = "Toggle vertex drawing";
  public static String TOGGLE_EDGE_DRAWING = "Toggle egde drawing";
  public static String TOGGLE_FACE_DRAWING = "Toggle face drawing";
  public static String BACKGROUND_COLOR = "Set background color";
  //CAMERA MENU
  public static String DECREASE_FIELD_OF_VIEW = "Decrease fieldOfView";
  public static String INCREASE_FIELD_OF_VIEW = "Increase fieldOfView";
  public static String DECREASE_FOCUS = "Decrease focus";
  public static String INCREASE_FOCUS = "Increase focus";
  public static String DECREASE_EYE_SEPARATION = "Decrease eyeSeparation";
  public static String INCREASE_EYE_SEPARATION = "Increase eyeSeparation";
  public static String TOGGLE_PERSPECTIVE = "Toggle perspective";
  public static String TOGGLE_STEREO = "Toggle stereo";
  //VIEW MENU
  public static String TOGGLE_NAVIGATOR = "Show navigator";
  public static String TOGGLE_BEANSHELL = "Show bean shell"; 
  public static String TOGGLE_MENU = "Hide menu bar";
  public static String TOGGLE_RENDER_SELECTION = "Show selection";
  public static String TOGGLE_FULL_VIEWER = "Toggle viewer full screen";
  public static String TOGGLE_FULL_SCREEN = "Toggle full screen";
  public static String RENDER = "Force Rendering";
  
  private JFrame frame = null;
  private ViewerApp viewerApp = null;
  private SelectionManager sm = null;
  private ViewerSwitch viewerSwitch = null;
  private JMenuBar menuBar;
  
  private JCheckBoxMenuItem navigatorCheckBox;
  private JCheckBoxMenuItem beanShellCheckBox;
  private JCheckBoxMenuItem renderSelectionCheckbox;
  private ExportImage exportImageAction;


  protected ViewerAppMenu(ViewerApp v) {
    viewerApp = v;
    frame = v.getFrame();
    sm = v.getSelectionManager();
    viewerSwitch = v.getViewerSwitch();
    
    setupMenuBar();
  }
  

  private void setupMenuBar() {
    
    menuBar = new JMenuBar();
    
    //FILE MENU
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setMnemonic(KeyEvent.VK_F);
    menuBar.add(fileMenu);
    
    fileMenu.add(new JMenuItem(new LoadFile(LOAD_FILE, sm, viewerApp, frame)));
    fileMenu.add(new JMenuItem(new LoadFileMerged(LOAD_FILE_MERGED, sm, viewerApp, frame)));
    fileMenu.add(new JMenuItem(new LoadScene(LOAD_SCENE, viewerApp, frame)));
    fileMenu.addSeparator();
    fileMenu.add(new JMenuItem(new SaveScene(SAVE_SCENE, viewerApp.getViewer(), frame)));
    fileMenu.add(new JMenuItem(new SaveSelected(SAVE_SELECTED, sm, frame)));
    fileMenu.addSeparator();
    JMenu export = new JMenu(EXPORT);
    fileMenu.add(export);
    JMenu rib = new JMenu("RIB");
    export.add(rib);
    rib.add(new JMenuItem(new ExportRIB("Pixar", RIBViewer.TYPE_PIXAR, viewerSwitch, frame)));
    rib.add(new JMenuItem(new ExportRIB("3DLight", RIBViewer.TYPE_3DELIGHT, viewerSwitch, frame)));
    rib.add(new JMenuItem(new ExportRIB("Aqsis", RIBViewer.TYPE_AQSIS, viewerSwitch, frame)));

    export.add(new JMenuItem(new ExportSVG("SVG", viewerSwitch, frame)));
    export.add(new JMenuItem(new ExportPS("PS", viewerSwitch, frame)));
    exportImageAction = new ExportImage("Image", viewerSwitch, frame);
    export.add(new JMenuItem(exportImageAction));
    if (!Beans.isDesignTime()) {
    	fileMenu.addSeparator();
    	fileMenu.add(new JMenuItem(new Quit(QUIT)));    
    }
    
    //EDIT MENU
    JMenu editMenu = new JMenu(EDIT_MENU);
    editMenu.setMnemonic(KeyEvent.VK_E);
    menuBar.add(editMenu);
    
    editMenu.add(new JMenuItem(new Remove(REMOVE, sm)));
    editMenu.addSeparator();
    editMenu.add(new JMenuItem(new AddTool(ADD_TOOL, sm, frame)));
    editMenu.addSeparator();
    editMenu.add(new JMenuItem(new TogglePickable(TOGGLE_PICKABLE, sm)));
    editMenu.add(new JMenuItem(new AssignFaceAABBTree(ASSIGN_FACE_AABBTREE, sm)));
    
    //APPEARANCE MENU
    JMenu appMenu = new JMenu(APP_MENU);
    appMenu.setMnemonic(KeyEvent.VK_A);
    menuBar.add(appMenu);
    
    appMenu.add(new JMenuItem(new ToggleAppearance(TOGGLE_VERTEX_DRAWING, CommonAttributes.VERTEX_DRAW, sm)));
    appMenu.add(new JMenuItem(new ToggleAppearance(TOGGLE_EDGE_DRAWING, CommonAttributes.EDGE_DRAW, sm)));
    appMenu.add(new JMenuItem(new ToggleAppearance(TOGGLE_FACE_DRAWING, CommonAttributes.FACE_DRAW, sm)));
    appMenu.addSeparator();
    JMenu bgColors = new JMenu(BACKGROUND_COLOR);  //background color of viewerApp
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
    appMenu.add(bgColors);
    
    //CAMERA MENU
    JMenu cameraMenu = new JMenu(CAMERA_MENU);
    cameraMenu.setMnemonic(KeyEvent.VK_C);
    menuBar.add(cameraMenu);
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

    //VIEW MENU
    JMenu viewerMenu = new JMenu(VIEW_MENU);
    viewerMenu.setMnemonic(KeyEvent.VK_V);
    menuBar.add(viewerMenu);
    
    navigatorCheckBox = new JCheckBoxMenuItem(new ToggleNavigator(TOGGLE_NAVIGATOR, viewerApp));
    beanShellCheckBox = new JCheckBoxMenuItem(new ToggleBeanShell(TOGGLE_BEANSHELL, viewerApp));
    viewerMenu.add(navigatorCheckBox);
    viewerMenu.add(beanShellCheckBox);
    viewerMenu.addSeparator();
    renderSelectionCheckbox = new JCheckBoxMenuItem(new ToggleRenderSelection(TOGGLE_RENDER_SELECTION, sm));
    viewerMenu.add(renderSelectionCheckbox);
    viewerMenu.addSeparator();
    viewerMenu.add(new JMenuItem(new ToggleMenu(TOGGLE_MENU, menuBar)));
    viewerMenu.addSeparator();
    viewerMenu.add(new JMenuItem(ToggleViewerFullScreen.sharedInstance(TOGGLE_FULL_VIEWER, viewerApp)));
    viewerMenu.add(new JMenuItem(ToggleFullScreen.sharedInstance(TOGGLE_FULL_SCREEN, frame)));      
    viewerMenu.addSeparator();
    
    String[] viewerNames = viewerSwitch.getViewerNames();
    bg = new ButtonGroup();
    for (int i=0; i<viewerSwitch.getNumViewers(); i++) {
      final int index = i;
      final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
          new javax.swing.AbstractAction(viewerNames[index]){
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(ActionEvent e) {
              viewerSwitch.selectViewer(index);
              viewerSwitch.getCurrentViewer().renderAsync();
              exportImageAction.setEnabled(exportImageAction.isEnabled());
            }
          });
      item.setSelected(index==0);
      item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + index, 0));
      bg.add(item);
      viewerMenu.add(item);
    }
    
    viewerMenu.addSeparator();
    viewerMenu.add(new JMenuItem(new Render(RENDER, viewerSwitch)));
    
  }
  
  
  //update menu items which depend on viewerApp properties
  //(isAttachNavigator/BeanShell, isShowMenu)
  //setupMenuBar() has to be called before
  public void update() {
    if (viewerApp == null) return;
    
    navigatorCheckBox.setSelected(viewerApp.isAttachNavigator());
    beanShellCheckBox.setSelected(viewerApp.isAttachBeanShell());
    renderSelectionCheckbox.setSelected(sm.isRenderSelection());  //sm!=null if viewerApp!=null
  }
  
  
  /**
   * Get the menu bar.
   * @return the menu bar
   */
  public JMenuBar getMenuBar() {
    return menuBar;
  }
  
  
  /**
   * Add a menu to the end of the menu bar.
   * @param menu the menu to add
   * @see ViewerApp#addMenu(JMenu, int)
   */
  public void addMenu(JMenu menu) {
    addMenu(menu, menuBar.getComponentCount());  //add to end of menuBar
  }

  
  /**
   * Add a menu to the menu bar at the specified index.
   * @param menu the menu to add
   * @param index the menu's position in the menu bar
   * @throws IllegalArgumentException if an invalid index is specified
   */
  public void addMenu(JMenu menu, int index) {
    menuBar.add(menu, index);
  }
  
  
  /**
   * Remove the menu with the specified name.
   * @param menuName the menu's name (use static fields of {@link de.jreality.ui.viewerapp.ViewerAppMenu})
   * @return false iff the specified menu is not contained in the menu bar
   */
  public boolean removeMenu(String menuName) {
    JMenu menu = getMenu(menuName);
    if (menu != null) menuBar.remove(menu);
    return (menu != null);
  }
  
  
  /**
   * Get a menu specified by its name.
   * @param menuName the menu's name (use static fields of {@link de.jreality.ui.viewerapp.ViewerAppMenu})
   * @return the menu or null if the specified menu doesn't exist
   */
  public JMenu getMenu(String menuName) {
    JMenu menu = null;
    for (int i = 0; i < menuBar.getComponentCount(); i++) {
      if ( ((JMenu)menuBar.getComponent(i)).getText().equals(menuName) )
        menu = (JMenu)menuBar.getComponent(i);
    }
    return menu;
  }
  
  
  /**
   * Add a menu item to the end of the menu with the specified name.
   * @param item the menu item to add
   * @param menuName the menu's name (use static fields of {@link de.jreality.ui.viewerapp.ViewerAppMenu})
   * @return false iff the specified menu is not contained in the menu bar
   * @see ViewerApp#addMenuItem(JMenuItem, String, int)
   */
  public boolean addMenuItem(JMenuItem item, String menuName) {
    return addMenuItem(item, menuName, menuBar.getComponentCount());  //add to end of menu
  }
  
  
  /**
   * Add a menu item to the menu with the specified name at the specified index.
   * @param item the menu item to add
   * @param menuName the menu's name (use static fields of {@link de.jreality.ui.viewerapp.ViewerAppMenu})
   * @param index the menu item's position in the menu (note that separators are also components of the menu)
   * @return false iff the specified menu is not contained in the menu bar
   * @throws IllegalArgumentException if an invalid index is specified
   */
  public boolean addMenuItem(JMenuItem item, String menuName, int index) {
    JMenu menu = getMenu(menuName);
    if (menu != null) menu.insert(item, index);
    return (menu != null);
  }
  
  
  /**
   * Remove the menu item at given position of the menu with the specified name.
   * @param menuName the menu's name (use static fields of {@link de.jreality.ui.viewerapp.ViewerAppMenu})
   * @param index the menu item's position
   * @return false iff the specified menu is not contained in the menu bar
   * @throws IllegalArgumentException if an invalid index is specified
   */
  public boolean removeMenuItem(String menuName, int index) {
    JMenu menu = getMenu(menuName);
    if (menu != null) menu.remove(index);
    return (menu != null);
  }

  
  /**
   * Add an action to the end of the menu with the specified name.
   * @param a the action to add
   * @param menuName the menu's name (use static fields of {@link de.jreality.ui.viewerapp.ViewerAppMenu})
   * @return false iff the specified menu is not contained in the menu bar
   * @see ViewerApp#addAction(Action, String, int)
   */
  public boolean addAction(Action a, String menuName) {
    int index = 0;
    JMenu menu = getMenu(menuName);
    if (menu != null) index = menu.getMenuComponentCount();
    else return false;
    
    return addAction(a, menuName, index);  //add to end of menu
  }
  
  
  /**
   * Add an action to the menu with the specified name at the specified index.
   * @param a the action to add
   * @param menuName the menu's name (use static fields of {@link de.jreality.ui.viewerapp.ViewerAppMenu})
   * @param index the action's position in the menu
   * @return false iff the specified menu is not contained in the menu bar
   * @throws IllegalArgumentException if an invalid index is specified
   */
  public boolean addAction(Action a, String menuName, int index) {
    return addMenuItem(new JMenuItem(a), menuName, index);
  }
  
  
  /**
   * Add a separator to the end of the menu with the specified name.
   * @param menuName the menu's name (use static fields of {@link de.jreality.ui.viewerapp.ViewerAppMenu})
   * @return false iff the specified menu is not contained in the menu bar
   * @see ViewerApp#addSeparator(String, int)
   */
  public boolean addSeparator(String menuName) {
    int index = 0;
    JMenu menu = getMenu(menuName);
    if (menu != null) index = menu.getMenuComponentCount();
    else return false;
    
    return addSeparator(menuName, index);
  }
  
  
  /**
   * Add a separator to the menu with the specified name at the specified index.
   * @param menuName the menu's name (use static fields of {@link de.jreality.ui.viewerapp.ViewerAppMenu})
   * @param index the separators's position in the menu
   * @return false iff the specified menu is not contained in the menu bar
   * @throws IllegalArgumentException if an invalid index is specified
   */
  public boolean addSeparator(String menuName, int index) {
    JMenu menu = getMenu(menuName);
    if (menu != null) menu.insertSeparator(index);
    return (menu != null);
  }
  
}