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
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.Beans;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import de.jreality.scene.Geometry;
import de.jreality.shader.CommonAttributes;
import de.jreality.toolsystem.ToolSystemViewer;
import de.jreality.ui.viewerapp.actions.AbstractSelectionListenerAction;
import de.jreality.ui.viewerapp.actions.camera.ShiftEyeSeparation;
import de.jreality.ui.viewerapp.actions.camera.ShiftFieldOfView;
import de.jreality.ui.viewerapp.actions.camera.ShiftFocus;
import de.jreality.ui.viewerapp.actions.camera.ToggleStereo;
import de.jreality.ui.viewerapp.actions.edit.AddTool;
import de.jreality.ui.viewerapp.actions.edit.AssignFaceAABBTree;
import de.jreality.ui.viewerapp.actions.edit.CreateAppearance;
import de.jreality.ui.viewerapp.actions.edit.ExportOBJ;
import de.jreality.ui.viewerapp.actions.edit.LoadFileToNode;
import de.jreality.ui.viewerapp.actions.edit.LoadReflectionMap;
import de.jreality.ui.viewerapp.actions.edit.LoadTexture;
import de.jreality.ui.viewerapp.actions.edit.Remove;
import de.jreality.ui.viewerapp.actions.edit.Rename;
import de.jreality.ui.viewerapp.actions.edit.RotateReflectionMapSides;
import de.jreality.ui.viewerapp.actions.edit.SaveSelected;
import de.jreality.ui.viewerapp.actions.edit.ToggleAppearance;
import de.jreality.ui.viewerapp.actions.edit.TogglePickable;
import de.jreality.ui.viewerapp.actions.edit.ToggleVisibility;
import de.jreality.ui.viewerapp.actions.file.ExportImage;
import de.jreality.ui.viewerapp.actions.file.ExportPS;
import de.jreality.ui.viewerapp.actions.file.ExportRIB;
import de.jreality.ui.viewerapp.actions.file.ExportSVG;
import de.jreality.ui.viewerapp.actions.file.LoadFile;
import de.jreality.ui.viewerapp.actions.file.LoadScene;
import de.jreality.ui.viewerapp.actions.file.Quit;
import de.jreality.ui.viewerapp.actions.file.SaveScene;
import de.jreality.ui.viewerapp.actions.view.LoadSkyBox;
import de.jreality.ui.viewerapp.actions.view.Maximize;
import de.jreality.ui.viewerapp.actions.view.RotateSkyboxSides;
import de.jreality.ui.viewerapp.actions.view.SetViewerSize;
import de.jreality.ui.viewerapp.actions.view.SwitchBackgroundColor;
import de.jreality.ui.viewerapp.actions.view.ToggleBeanShell;
import de.jreality.ui.viewerapp.actions.view.ToggleExternalBeanShell;
import de.jreality.ui.viewerapp.actions.view.ToggleExternalNavigator;
import de.jreality.ui.viewerapp.actions.view.ToggleMenu;
import de.jreality.ui.viewerapp.actions.view.ToggleNavigator;
import de.jreality.ui.viewerapp.actions.view.ToggleRenderSelection;
import de.jreality.ui.viewerapp.actions.view.ToggleViewerFullScreen;
import de.jreality.util.LoggingSystem;


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
  public static String CAMERA_MENU = "Camera";
  public static String VIEW_MENU = "View";
  
  //FILE MENU
  public static String LOAD_FILE = "Load files";
  public static String LOAD_FILE_MERGED = "Load merged files";
  public static String LOAD_SCENE = "Load scene";
  public static String SAVE_SCENE = "Save scene";
  public static String EXPORT = "Export";
  public static String QUIT = "Quit";
  
  //EDIT MENU
  public static String SAVE_SELECTED = "Save selected";
  public static String LOAD_FILE_TO_NODE = "Load files into node";
  public static String REMOVE = "Remove";
  public static String RENAME = "Rename";
  public static String TOGGLE_VISIBILITY = "Toggle visibility";
  public static String ASSIGN_FACE_AABBTREE = "Assign AABBTree";
  public static String APPEARANCE = "Appearance";
  public static String CREATE_APPEARANCE = "Create new Appearance";
  public static String TOGGLE_VERTEX_DRAWING = "Toggle vertex drawing";
  public static String TOGGLE_EDGE_DRAWING = "Toggle egde drawing";
  public static String TOGGLE_FACE_DRAWING = "Toggle face drawing";
  public static String LOAD_TEXTURE = "Load texture";
  public static String REFLECTIONMAP = "Reflection map";
  public static String LOAD_REFLECTIONMAP = "Load reflection map";
  public static String ROTATE_REFLECTIONMAP_SIDES = "Rotate reflection map sides";
  public static String GEOMETRY = "Geometry";
  public static String EXPORT_OBJ = "Write OBJ";
  public static String TOGGLE_PICKABLE = "Toggle pickable";
  public static String ADD_TOOL = "Add Tools";
  
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
  public static String TOGGLE_EXTERNAL_NAVIGATOR = "Open navigator in separate frame";
  public static String TOGGLE_BEANSHELL = "Show bean shell"; 
  public static String TOGGLE_EXTERNAL_BEANSHELL = "Open bean shell in separate frame";
  public static String TOGGLE_RENDER_SELECTION = "Show selection";
  public static String TOGGLE_MENU = "Hide menu bar";
  public static String SET_BACKGROUND_COLOR = "Set background color";
  public static String SKYBOX ="Skybox";
  public static String LOAD_SKYBOX ="Load skybox";
  public static String ROTATE_SKYBOX_SIDES ="Rotate skybox sides";
  public static String TOGGLE_VIEWER_FULL_SCREEN = "Toggle full screen";
  public static String MAXIMIZE = "Maximize frame size";
  public static String RESTORE = "Restore frame size";
  public static String SET_VIEWER_SIZE ="Set viewer size";
  
  private Component parentComp = null;
  private ViewerApp viewerApp = null;
  private SelectionManagerInterface sm = null;
  private ToolSystemViewer viewer = null;
  private JMenuBar menuBar;
  
  private JCheckBoxMenuItem navigatorCheckBox;
  private JCheckBoxMenuItem externalNavigatorCheckBox;
  private JCheckBoxMenuItem beanShellCheckBox;
  private JCheckBoxMenuItem externalBeanShellCheckBox;
  private JCheckBoxMenuItem renderSelectionCheckbox;
  private ExportImage exportImageAction;
	private boolean showMenuBar = true;
	private HashMap<String, Boolean> showMenu = new HashMap<String, Boolean>();


  protected ViewerAppMenu(ViewerApp v) {
    viewerApp = v;
    parentComp = v.getFrame();
    sm = v.getSelectionManager();
    viewer = v.getViewer();
    
    menuBar = new JMenuBar();
    
    addMenu(createFileMenu());
    addMenu(createEditMenu(parentComp, sm));
    addMenu(createCameraMenu());
    addMenu(createViewMenu());
  }


  private JMenu createFileMenu() {
    JMenu fileMenu = new JMenu(FILE_MENU);
    fileMenu.setMnemonic(KeyEvent.VK_F);
    
    fileMenu.add(new JMenuItem(new LoadFile(LOAD_FILE, 
    		sm.getDefaultSelection().getLastComponent(), 
    		viewerApp.getViewer(), parentComp)));
    fileMenu.add(new JMenuItem(new LoadScene(LOAD_SCENE, viewerApp)));
    fileMenu.addSeparator();
    fileMenu.add(new JMenuItem(new SaveScene(SAVE_SCENE, viewerApp.getViewer(), parentComp)));
    fileMenu.addSeparator();
    
    JMenu export = new JMenu(EXPORT);
    fileMenu.add(export);
    try {
    	export.add(new SunflowMenu(viewerApp));
    } catch (Exception e) {
    	e.printStackTrace();
    	LoggingSystem.getLogger(this).log(Level.CONFIG, "no sunflow", e);
    }
    export.add(new JMenuItem(new ExportRIB("RIB", viewer, parentComp)));
    export.add(new JMenuItem(new ExportSVG("SVG", viewer, parentComp)));
    export.add(new JMenuItem(new ExportPS("PS", viewer, parentComp)));
    if (viewer.getDelegatedViewer() instanceof ViewerSwitch) {
    	exportImageAction = new ExportImage("Image", (ViewerSwitch) viewer.getDelegatedViewer(), parentComp);
    	export.add(new JMenuItem(exportImageAction));
    }
    
    if (!Beans.isDesignTime()) {
    	fileMenu.addSeparator();
    	fileMenu.add(new JMenuItem(new Quit(QUIT)));    
    }
    
    return fileMenu;
  }
  
  
  /**
   * Creates an edit menu containing all appropriate actions.<br>
   * Also used for creating the navigator's context menu. 
   * @param parentComp use as parent component for dialogs
   */
  protected static JMenu createEditMenu(Component parentComp, SelectionManagerInterface sm) {
    JMenu editMenu = new JMenu(EDIT_MENU);
    editMenu.setMnemonic(KeyEvent.VK_E);
    
    editMenu.add(new JMenuItem(new LoadFileToNode(LOAD_FILE_TO_NODE, sm, parentComp)));
    editMenu.add(new JMenuItem(new SaveSelected(SAVE_SELECTED, sm, parentComp)));
    editMenu.addSeparator();
    editMenu.add(new JMenuItem(new Remove(REMOVE, sm)));
    editMenu.add(new JMenuItem(new Rename(RENAME, sm, parentComp)));
    editMenu.addSeparator();
    editMenu.add(new JMenuItem(new ToggleVisibility(TOGGLE_VISIBILITY, sm)));
    editMenu.add(new JMenuItem(new AssignFaceAABBTree(ASSIGN_FACE_AABBTREE, sm)));
    editMenu.addSeparator();
    
    //appearance actions
    JMenu appearance = new JMenu(new AbstractSelectionListenerAction(APPEARANCE, sm){
    	{this.setShortDescription(APPEARANCE+" options");}
    	@Override
			public void actionPerformed(ActionEvent e) {}
			@Override
			public boolean isEnabled(SelectionEvent e) {
				return (e.componentSelected() || e.appearanceSelected());
			}
    });
    editMenu.add(appearance);
    appearance.add(new JMenuItem(new CreateAppearance(CREATE_APPEARANCE, sm)));
    appearance.addSeparator();
    appearance.add(new JMenuItem(new ToggleAppearance(TOGGLE_VERTEX_DRAWING, CommonAttributes.VERTEX_DRAW, sm)));
    appearance.add(new JMenuItem(new ToggleAppearance(TOGGLE_EDGE_DRAWING, CommonAttributes.EDGE_DRAW, sm)));
    appearance.add(new JMenuItem(new ToggleAppearance(TOGGLE_FACE_DRAWING, CommonAttributes.FACE_DRAW, sm)));
    appearance.addSeparator();
    appearance.add(new JMenuItem(new LoadTexture(LOAD_TEXTURE, sm, parentComp)));
    JMenu reflectionmap = new JMenu(REFLECTIONMAP);
    appearance.add(reflectionmap);
    reflectionmap.add(new JMenuItem(new LoadReflectionMap(LOAD_REFLECTIONMAP, sm, parentComp)));
    reflectionmap.add(new JMenuItem(new RotateReflectionMapSides(ROTATE_REFLECTIONMAP_SIDES, sm, parentComp)));

    //geometry actions
    JMenu geometry = new JMenu(new AbstractSelectionListenerAction(GEOMETRY, sm){
    	{this.setShortDescription(GEOMETRY+" options");}
    	@Override
			public void actionPerformed(ActionEvent e) {}
			@Override
			public boolean isEnabled(SelectionEvent e) {
				if (e.geometrySelected()) return true;
				Geometry g = null;
				if (e.componentSelected())
					g = e.getSelection().getLastComponent().getGeometry();
				return (g instanceof Geometry);
			}
    });
    editMenu.add(geometry);
    geometry.add(new JMenuItem(new ExportOBJ(EXPORT_OBJ, sm, parentComp)));
    geometry.add(new JMenuItem(new TogglePickable(TOGGLE_PICKABLE, sm)));
    editMenu.addSeparator();
    
    editMenu.add(new JMenuItem(new AddTool(ADD_TOOL, sm, parentComp)));
    
    return editMenu;
  }

  
  private JMenu createCameraMenu() {
    JMenu cameraMenu = new JMenu(CAMERA_MENU);
    cameraMenu.setMnemonic(KeyEvent.VK_C);

    cameraMenu.add(new JMenuItem(new ShiftFieldOfView(DECREASE_FIELD_OF_VIEW, viewer, true)));
    cameraMenu.add(new JMenuItem(new ShiftFieldOfView(INCREASE_FIELD_OF_VIEW, viewer, false)));
    cameraMenu.addSeparator();
    cameraMenu.add(new JMenuItem(new ShiftFocus(DECREASE_FOCUS, viewer, true)));
    cameraMenu.add(new JMenuItem(new ShiftFocus(INCREASE_FOCUS, viewer, false)));
    cameraMenu.addSeparator();
    cameraMenu.add(new JMenuItem(new ShiftEyeSeparation(DECREASE_EYE_SEPARATION, viewer, true)));
    cameraMenu.add(new JMenuItem(new ShiftEyeSeparation(INCREASE_EYE_SEPARATION, viewer, false)));
    cameraMenu.addSeparator();
//    cameraMenu.add(new JMenuItem(new TogglePerspective(TOGGLE_PERSPECTIVE, viewerSwitch)));
    cameraMenu.add(new JMenuItem(new ToggleStereo(TOGGLE_STEREO, viewer)));
    
    return cameraMenu;
  }
  
  
  private JMenu createViewMenu() {
    JMenu viewMenu = new JMenu(VIEW_MENU);
    viewMenu.setMnemonic(KeyEvent.VK_V);
    
    navigatorCheckBox = new JCheckBoxMenuItem(new ToggleNavigator(TOGGLE_NAVIGATOR, viewerApp));
    externalNavigatorCheckBox = new JCheckBoxMenuItem(new ToggleExternalNavigator(TOGGLE_EXTERNAL_NAVIGATOR, viewerApp));
    beanShellCheckBox = new JCheckBoxMenuItem(new ToggleBeanShell(TOGGLE_BEANSHELL, viewerApp));
    externalBeanShellCheckBox = new JCheckBoxMenuItem(new ToggleExternalBeanShell(TOGGLE_EXTERNAL_BEANSHELL, viewerApp));
    viewMenu.add(navigatorCheckBox);
    viewMenu.add(externalNavigatorCheckBox);
    viewMenu.add(beanShellCheckBox);
    viewMenu.add(externalBeanShellCheckBox);
    viewMenu.addSeparator();
    
    renderSelectionCheckbox = new JCheckBoxMenuItem(new ToggleRenderSelection(TOGGLE_RENDER_SELECTION, sm));
    viewMenu.add(renderSelectionCheckbox);
    viewMenu.add(new JMenuItem(new ToggleMenu(TOGGLE_MENU, this)));
    viewMenu.addSeparator();

    //create background color list
    JMenu bgColors = new JMenu(SET_BACKGROUND_COLOR);  //background color of viewerApp
    ButtonGroup bg = new ButtonGroup();
    List<JRadioButtonMenuItem> items = new LinkedList<JRadioButtonMenuItem>();
    items.add( new JRadioButtonMenuItem(new SwitchBackgroundColor("default", viewerApp, SwitchBackgroundColor.defaultColor)) );
    items.add( new JRadioButtonMenuItem(new SwitchBackgroundColor("white", viewerApp, Color.WHITE)) );
    items.add( new JRadioButtonMenuItem(new SwitchBackgroundColor("gray", viewerApp, new Color(225, 225, 225))) );
    items.add( new JRadioButtonMenuItem(new SwitchBackgroundColor("black", viewerApp, Color.BLACK)) );
    for (JRadioButtonMenuItem item : items) {
    	bg.add(item);
    	bgColors.add(item);
    }
    viewMenu.add(bgColors);
    JMenu skybox = new JMenu(SKYBOX);
    viewMenu.add(skybox);
    skybox.add(new JMenuItem(new LoadSkyBox(LOAD_SKYBOX, viewerApp.getViewer().getSceneRoot(), parentComp)));
    skybox.add(new JMenuItem(new RotateSkyboxSides(ROTATE_SKYBOX_SIDES, viewerApp.getViewer().getSceneRoot(), parentComp)));
    viewMenu.addSeparator();
    
    viewMenu.add(new JMenuItem(ToggleViewerFullScreen.sharedInstance(TOGGLE_VIEWER_FULL_SCREEN, viewerApp)));
    viewMenu.add(new JMenuItem(Maximize.sharedInstance(MAXIMIZE, (Frame)parentComp)));
    viewMenu.add(new JMenuItem(new SetViewerSize(SET_VIEWER_SIZE, viewerApp.getViewingComponent(), (Frame)parentComp)));
    viewMenu.addSeparator();
    
    if (viewer.getDelegatedViewer() instanceof ViewerSwitch) {
        final ViewerSwitch viewerSwitch = (ViewerSwitch) viewer.getDelegatedViewer();
        String[] viewerNames = viewerSwitch.getViewerNames();
        ButtonGroup bgr = new ButtonGroup();
        for (int i=0; i<viewerSwitch.getNumViewers(); i++) {
          final int index = i;
          final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
              new javax.swing.AbstractAction(viewerNames[index]){
                private static final long serialVersionUID = 1L;
                
                public void actionPerformed(ActionEvent e) {
                  viewerSwitch.selectViewer(index);
                  viewerSwitch.getCurrentViewer().renderAsync();
                  if (exportImageAction!=null) exportImageAction.setEnabled(exportImageAction.isEnabled());
                }
              });
          item.setSelected(index==0);
          item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + index, 0));
          bgr.add(item);
          viewMenu.add(item);
        }
           	
    }
    
//    viewMenu.addSeparator();
//    viewMenu.add(new JMenuItem(new Render(RENDER, viewerSwitch)));
    
    return viewMenu;
  }
  
  
  //update menu items which depend on viewerApp properties
  //setupMenuBar() has to be called before
  public void update() {
    if (viewerApp == null) return;
    
    navigatorCheckBox.setSelected(viewerApp.isAttachNavigator());
    externalNavigatorCheckBox.setSelected(viewerApp.isExternalNavigator());
    beanShellCheckBox.setSelected(viewerApp.isAttachBeanShell());
    externalBeanShellCheckBox.setSelected(viewerApp.isExternalBeanShell());
    renderSelectionCheckbox.setSelected(sm.isRenderSelection());  //sm!=null if viewerApp!=null
    showMenu(EDIT_MENU, viewerApp.isAttachNavigator());
    showMenuBar(viewerApp.isShowMenu());
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
    showMenu.put(menu.getText(), menu.isVisible());
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

  
  /**
   * Show or hide the menu bar.<br>
   * When hiding the menu bar, the visibility of all contained menus is set to false 
   * (defined keystrokes for actions are still working then).
   */
  public void showMenuBar(boolean show) {

	if (showMenuBar==show) return;
	  
  	for (int i = 0; i < menuBar.getComponentCount(); i++)
      menuBar.getMenu(i).setVisible(
      		show ? this.showMenu.get(menuBar.getMenu(i).getText()) : false 
      );

  	showMenuBar  = show;
  }
  
  
  public boolean isShowMenuBar() {
  	return showMenuBar;
  }
  
  
  /**
   * Show or hide the menu with the specified name.
   * @param menuName the menu's name (use static fields of {@link de.jreality.ui.viewerapp.ViewerAppMenu})
   * @param show true iff specified menu should be visible
   */
  public void showMenu(String menuName, boolean show) {
	if (getMenu(menuName) == null) return;
	
	getMenu(menuName).setVisible(show);
  	showMenu.put(menuName, show);
  }
  
}