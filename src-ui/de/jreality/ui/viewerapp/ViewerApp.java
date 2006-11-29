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
import java.awt.Dimension;
import java.beans.Beans;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import jterm.BshEvaluator;
import bsh.EvalError;
import de.jreality.io.JrScene;
import de.jreality.io.JrSceneFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.shader.CommonAttributes;
import de.jreality.toolsystem.ToolSystemViewer;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.ui.viewerapp.actions.edit.SwitchBackgroundColor;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;
import de.jreality.util.RenderTrigger;
import de.jreality.util.Secure;


/**
 * Factory for the jReality Viewer application, which displays a 
 * {@link de.jreality.io.JrScene} in a frame.<br>
 * Use the factory as following:<br>
 * <code><b><pre>
 * ViewerApp viewer = new ViewerApp(...);
 * viewer.setAttachNavigator(true);
 * viewer.setAttachBeanShell(false);
 * [setting more properties]<br>
 * viewer.update();
 * viewer.display();
 * </pre></b></code>
 * Editing the viewerApp's menu:<br>
 * <code><b><pre>
 * ViewerAppMenu menu = viewerApp.getMenu();
 * menu.removeMenu(ViewerAppMenu.APP_MENU);
 * menu.addAction(ViewerAppMenu.FILE_MENU, action);
 * [etc.]
 * </pre></b></code>
 * 
 * @author weissman, msommer
 */
public class ViewerApp {
  
  private SceneGraphNode displayedNode;  //the node which is displayed in viewer
  
  private UIFactory uiFactory;  //frame layout factory depending on viewer
  
  private RenderTrigger renderTrigger;
  
  private Viewer[] viewers;  //containing possible viewers (jogl, soft, portal)
  private ViewerSwitch viewerSwitch;
  private ToolSystemViewer currViewer;  //the current viewer
  private JFrame frame;
  
  private SceneGraphComponent sceneRoot;
  private SceneGraphComponent scene;
  
  private boolean attachNavigator = false;  //default
  private boolean attachBeanShell = false;  //default
  private BeanShell beanShell;
  private Navigator navigator;
  private SelectionManager selectionManager;
  
  private boolean showMenu = true;  //default
  private ViewerAppMenu menu;
  
  private JrScene jrScene;
  
  private boolean autoRender = true;
  private boolean synchRender = false;
  
  private LinkedList<Component> accessory = new LinkedList<Component>();
  private HashMap<Component, String> accessoryTitles = new HashMap<Component, String>();
  
  
  /**
   * @param node the SceneGraphNode (SceneGraphComponent or Geometry) to be displayed with the viewer
   */
  public ViewerApp(SceneGraphNode node) {
    this(node, null, null, null, null);
  }
  
  
  /**
   * @param root the scene's root
   * @param cameraPath the scene's camera path
   * @param emptyPick the scene's empty pick path
   * @param avatar the scene's avatar path
   */
  public ViewerApp(SceneGraphComponent root, SceneGraphPath cameraPath, SceneGraphPath emptyPick, SceneGraphPath avatar) {
    this(null, root, cameraPath, emptyPick, avatar);
  }
  
  
  /**
   * @param scene the scene to be displayed with the viewer
   */
  public ViewerApp(JrScene scene) {
    this(scene.getSceneRoot(), scene.getPath("cameraPath"), scene.getPath("emptyPickPath"), scene.getPath("avatarPath"));
  }
  
  
  private ViewerApp(SceneGraphNode contentNode, SceneGraphComponent root, SceneGraphPath cameraPath, SceneGraphPath emptyPick, SceneGraphPath avatar) {
    
    if (contentNode != null)  //create default scene if null
      if (!(contentNode instanceof Geometry) && !(contentNode instanceof SceneGraphComponent))
        throw new IllegalArgumentException("Only Geometry or SceneGraphComponent allowed!");
    
    if (root == null) this.jrScene = getDefaultScene();
    else {
      JrScene s = new JrScene();
      s.setSceneRoot(root);
      if (cameraPath!= null) s.addPath("cameraPath", cameraPath);
      if (avatar != null) s.addPath("avatarPath", avatar);
      if (emptyPick != null) s.addPath("emptyPickPath", emptyPick);
      this.jrScene = s;
    }
    
    displayedNode = contentNode;
    
    //update autoRender & synchRender
    String autoRenderProp = Secure.getProperty( "de.jreality.ui.viewerapp.autoRender", "true" );
    if (autoRenderProp.equalsIgnoreCase("false")) {
      autoRender = false;
    }
    String synchRenderProp = Secure.getProperty( "de.jreality.ui.viewerapp.synchRender", "true" );
    if (synchRenderProp.equalsIgnoreCase("true")) {
      synchRender = true;
    }
    if (autoRender) renderTrigger = new RenderTrigger();
    if (synchRender) {
      if (autoRender) renderTrigger.setAsync(false);
      else LoggingSystem.getLogger(this).config("Inconsistant settings: no autoRender but synchRender!!");
    }
    
    //load the scene depending on environment (desktop | portal)
    //instantiates currViewer, viewers, viewerSwitch, sceneRoot, scene, uiFactory
    setupViewer(jrScene);
    
    frame = new JFrame();

    selectionManager = new SelectionManager(jrScene.getPath("emptyPickPath")); //default selection = scene node
    selectionManager.setViewer(viewerSwitch);  //used to force rendering
    try {	selectionManager.setAuxiliaryRoot(viewerSwitch.getAuxiliaryRoot()); } 
    catch (Exception e) { selectionManager.setAuxiliaryRoot(null); }  //e.g. software viewer
    
    menu = new ViewerAppMenu(this);  //uses frame, viewerSwitch, selectionManager and viewerApp itself
  }
  
  
  /**
   * Display the scene in a JFrame.
   * @return the frame
   */
  public JFrame display() {
    
    Component content = getComponent();
    
    //set general properties of UI
    try {
      //use CrossPlatformLookAndFeel (SystemLookAndFeel looks ugly on windows & linux)
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) {}
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    frame.setLocationByPlatform(true);
    
    //set viewer background color if not specified already
    if (sceneRoot.getAppearance() != null && (sceneRoot.getAppearance().getAttribute(CommonAttributes.BACKGROUND_COLORS) == Appearance.INHERITED && sceneRoot.getAppearance().getAttribute(CommonAttributes.BACKGROUND_COLOR) == Appearance.INHERITED))
      setBackgroundColor(SwitchBackgroundColor.defaultColor);
    
    //frame properties
    frame.setTitle("jReality Viewer");
    if (!Beans.isDesignTime()) 
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    Dimension size = frame.getToolkit().getScreenSize();
    size.width*=.7;
    size.height*=.7;
    frame.setSize(size);
    
    //set content of frame
    frame.getContentPane().add(content);
    
    //add menu bar
    JMenuBar menuBar = menu.getMenuBar();
    frame.setJMenuBar(menuBar);
    menuBar.setBorder(BorderFactory.createEmptyBorder());
    if (!showMenu) {
      //hide all menus, then keystrokes for actions are still working,
      //which is not the case when hiding menuBar
      for (int i = 0; i < menuBar.getComponentCount(); i++)
        menuBar.getMenu(i).setVisible(false);
    }
    
    frame.validate();
    
    //encompass scene before displaying
//  CameraUtility.encompass(currViewer.getAvatarPath(),
//  currViewer.getEmptyPickPath(),
//  currViewer.getCameraPath(),
//  1.75, currViewer.getSignature());
    
    frame.setVisible(true);
    
    return frame;
  }
  
  
  /**
   * Displays a specified SceneGraphComponent or Geometry using the jReality viewer.
   * @param node the SceneGraphNode (SceneGraphComponent or Geometry) to be displayed in the viewer
   * @return the ViewerApp factory instantiated to display the node
   */
  public static ViewerApp display(SceneGraphNode node) {
    
    ViewerApp app = new ViewerApp(node);
    app.setAttachNavigator(false);
    app.setAttachBeanShell(false);
    app.update();
    app.display();
    
    return app;
  }
  
  /**
   * Displays a scene specified by the following parameters.
   * @param root the scene's root
   * @param cameraPath the scene's camera path
   * @param emptyPick the scene's empty pick path
   * @param avatar the scene's avatar path
   * @return the ViewerApp factory instantiated to display the scene
   */
  public static ViewerApp display(SceneGraphComponent root, SceneGraphPath cameraPath, SceneGraphPath emptyPick, SceneGraphPath avatar) {
    
    ViewerApp app = new ViewerApp(null, root, cameraPath, emptyPick, avatar);
    app.setAttachNavigator(false);
    app.setAttachBeanShell(false);
    app.update();
    app.display();
    
    return app;
    
  }
  
  
  /**
   * Update the viewer factory.<br>
   * Needs to be invoked before calling display or getter methods. 
   */
  public void update() {
    
    uiFactory = new UIFactory();
    uiFactory.setViewer((Component) currViewer.getViewingComponent());
    
    //set up beanShell and uiFactory.beanShell
    if (attachBeanShell && beanShell == null) setupBeanShell();
    
    //setup navigator, uiFactory.inspector and uiFactory.sceneTree
    if (attachNavigator && navigator == null) setupNavigator();
    
    if (attachBeanShell) {
      uiFactory.setBeanShell(beanShell.getJTerm());
      Object self = getViewer().getSceneRoot();
      if (attachNavigator) {
        navigator.setBeanShell(beanShell);
        self = navigator.getCurrentSelection();
      }
      beanShell.setSelf(self);
    }
    
    if (attachNavigator) {
      uiFactory.setInspector(navigator.getInspector());
      uiFactory.setSceneTree(navigator.getSceneTree());
      selectionManager.setNavigator(navigator);
      if (!attachBeanShell) navigator.setBeanShell(null);
    } else {
      selectionManager.setNavigator(null);
    }
    
    uiFactory.setAttachNavigator(attachNavigator);
    uiFactory.setAttachBeanShell(attachBeanShell);
    
    //add accessory
    for (Component c : accessory) 
      uiFactory.addAccessory(c, accessoryTitles.get(c));
    
    //update menu (depends on attachNavigator/BeanShell, SelectionManager)
    menu.update();
  }
  
  
  /**
   * Get the default Scene depending on the environment (desktop or portal).
   * @return the default scene
   */
  private JrScene getDefaultScene() {
    String environment = Secure.getProperty( "de.jreality.viewerapp.env", "desktop" );
    
    if (!environment.equals("desktop") && !environment.equals("portal"))
      throw new IllegalArgumentException("unknown environment!");
    
    if (environment.equals("desktop"))
      return JrSceneFactory.getDefaultDesktopScene();
    else
      return JrSceneFactory.getDefaultPortalScene();
  }
  
  
  /**
   * Set up the viewer depending on chosen properties.<br>
   * (Creates ToolSystemViewer, Navigator, BeanShell and UIFactory.)
   * @param sc the scene to load
   */  
  private void setupViewer(JrScene sc) {
    if (currViewer != null) {
      if (autoRender) {
        renderTrigger.removeViewer(currViewer);
        if (currViewer.getSceneRoot() != null)
          renderTrigger.removeSceneGraphComponent(currViewer.getSceneRoot());
      }
      currViewer.dispose();
    }
    
//  uiFactory = new UIFactory();
    
    currViewer = AccessController.doPrivileged(new PrivilegedAction<ToolSystemViewer>() {
      public ToolSystemViewer run() {
        try {
          return createViewer();
        } catch (Exception exc) { exc.printStackTrace(); }
        return null;
      }
    });
    
    //set sceneRoot and paths of viewer
    sceneRoot = sc.getSceneRoot();
    currViewer.setSceneRoot(sceneRoot);
    
    if (autoRender) {
      renderTrigger.addViewer(currViewer);
      renderTrigger.addSceneGraphComponent(sceneRoot);
    }
    
    SceneGraphPath path = sc.getPath("cameraPath");
    if (path != null) currViewer.setCameraPath(path);
    path = sc.getPath("avatarPath");
    if (path != null) currViewer.setAvatarPath(path);
    path = sc.getPath("emptyPickPath");
    if (path != null) {
      //init scene 
      scene = path.getLastComponent();
      currViewer.setEmptyPickPath(path);
    }
    currViewer.initializeTools();
    
    //set viewer and sceneRoot of uiFactory
//  uiFactory.setViewer((Component) currViewer.getViewingComponent());
    
    //add node to this scene depending on its type
    if (displayedNode != null) {  //show scene even if displayedNode=null
      final SceneGraphNode node = displayedNode;
      node.accept(new SceneGraphVisitor() {
        public void visit(SceneGraphComponent sc) {
          scene.addChild(sc);
        }
        public void visit(Geometry g) {
          scene.setGeometry(g);
        }
      });
    }
    
  }
  
  
  private ToolSystemViewer createViewer() throws IOException {
    if (viewers == null) {
      
      String viewer = Secure.getProperty( "de.jreality.scene.Viewer", "de.jreality.jogl.Viewer de.jreality.softviewer.SoftViewer" ); // de.jreality.portal.DesktopPortalViewer");
      String[] vrs = viewer.split(" ");
      List<Viewer> viewerList = new LinkedList<Viewer>();
      String viewerClassName;
      for (int i = 0; i < vrs.length; i++) {
        viewerClassName = vrs[i];
        try {
          Viewer v = createViewer(viewerClassName);
          viewerList.add(v);
        } catch (Exception e) { // catches creation problems - i. e. no jogl in classpath
          LoggingSystem.getLogger(this).info("could not create viewer instance of ["+viewerClassName+"]");
        } catch (NoClassDefFoundError ndfe) {
          System.out.println("Possibly no jogl in classpath!");
        } catch (UnsatisfiedLinkError le) {
          System.out.println("Possibly no jogl libraries in java.library.path!");
        }
      }
      viewers = viewerList.toArray(new Viewer[viewerList.size()]);
      viewerSwitch = new ViewerSwitch(viewers);
    }
    
    //create ToolSystemViewer with configuration corresp. to environment
    ToolSystemConfiguration cfg = loadToolSystemConfiguration();
    
    
    ToolSystemViewer viewer = new ToolSystemViewer(viewerSwitch, cfg, synchRender ? renderTrigger : null);
    viewer.setPickSystem(new AABBPickSystem());
    
    return viewer;
  }
  
  
  private ToolSystemConfiguration loadToolSystemConfiguration() {
    return AccessController.doPrivileged(new PrivilegedAction<ToolSystemConfiguration>() {
      public ToolSystemConfiguration run() {
        String config = Secure.getProperty( "de.jreality.scene.tool.Config", "default" );
        ToolSystemConfiguration cfg=null;
        // HACK: only works for "regular" URLs
        try {
          if (config.contains("://")) {
            cfg = ToolSystemConfiguration.loadConfiguration(new Input(new URL(config)));
          } else {
            if (config.equals("default")) cfg = ToolSystemConfiguration.loadDefaultDesktopConfiguration();
            if (config.equals("portal")) cfg = ToolSystemConfiguration.loadDefaultPortalConfiguration();
            if (config.equals("default+portal")) cfg = ToolSystemConfiguration.loadDefaultDesktopAndPortalConfiguration();
          }
        } catch (IOException e) {
          // should not happen
          e.printStackTrace();
        }
        if (cfg == null) throw new IllegalStateException("couldn't load config ["+config+"]");
        return cfg;
      }
    });
  }
  
  
  private Viewer createViewer(String viewer) 
  throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
  {
    return (Viewer)Class.forName(viewer).newInstance();
  }
  
  
  /**
   * Set up the BeanShell.
   */
  private void setupBeanShell() {
    
    beanShell = new BeanShell();
    
    beanShell.eval("import de.jreality.geometry.*;");
    beanShell.eval("import de.jreality.math.*;");    
    beanShell.eval("import de.jreality.scene.*;");
    beanShell.eval("import de.jreality.scene.data.*;");
    beanShell.eval("import de.jreality.scene.tool.*;");
    beanShell.eval("import de.jreality.shader.*;");
    beanShell.eval("import de.jreality.tools.*;");
    beanShell.eval("import de.jreality.util.*;");
    
    BshEvaluator bshEval = beanShell.getBshEval();
    try { 
      bshEval.getInterpreter().set("_viewer", viewerSwitch);
      bshEval.getInterpreter().set("_toolSystemViewer", currViewer);      
    } 
    catch (EvalError error) { error.printStackTrace(); }
    
    beanShell.setSelf(sceneRoot);
  }
  
  
  /**
   * Set up the navigator (sceneTree and inspector).
   */
  private void setupNavigator() {
    navigator = new Navigator(sceneRoot);
  }
  
  
  /**
   * Use to attach a navigator (sceneTree and inspector) to the viewer.
   * @param b true iff navigator is to be attached
   */
  public void setAttachNavigator(boolean b) {
    attachNavigator = b;
  }
  
  
  /**
   * Use to attach a bean shell to the viewer. 
   * @param b true iff bean shell is to be attached
   */
  public void setAttachBeanShell(boolean b) {
    attachBeanShell = b;
  }
  
  
  /**
   * Get current ToolSystemViewer.
   * @return the viewer
   */
  public ToolSystemViewer getViewer() {
    if (currViewer == null)
      throw new UnsupportedOperationException("No viewer instantiated, call update()!");
    
    return currViewer;
  }
  
  
  /**
   * Get current frame displaying the scene.
   * @return the frame
   */
  public JFrame getFrame() {
    return frame;
  }
  
  
  /**
   * Get the viewer as a component.
   * @return the viewer component
   */
  public Component getComponent() {
    if (uiFactory == null)
      throw new UnsupportedOperationException("No viewer instantiated, call update()!");
    
    return uiFactory.getComponent();
  }
  
  
  /**
   * Returns true iff a bean shell is attached to the viewer.
   */
  public boolean isAttachBeanShell() {
    return attachBeanShell;
  }
  
  
  /**
   * Returns true iff a navigator is attached to the viewer. 
   */
  public boolean isAttachNavigator() {
    return attachNavigator;
  }
  
  
  /**
   * @return the navigator
   */
  public Navigator getNavigator() {
    if (navigator == null)
      throw new UnsupportedOperationException("No navigator attached!");  //TODO: reasonable?
    return navigator;
  }
  
  
  /**
   * @return the viewer switch
   */
  public ViewerSwitch getViewerSwitch() {
    return viewerSwitch;
  }
  
  
  /**
   * @return the JrScene
   */
  public JrScene getJrScene() {
    return jrScene;
  }
  
  
  /**
   * Use to include a menu bar and context menus in ViewerApp.
   * @param b true iff menu is to be shown
   */
  public void setShowMenu(boolean b) {
    showMenu = b;
  }
  
  
  public boolean isShowMenu() {
    return showMenu;
  }
  
  
  /**
   * Use to edit the menu bar (add/remove menus, add/remove items or actions to special menus).
   * @return the viewerApp's menu
   */
  public ViewerAppMenu getMenu() {
    return menu;
  }
  
  
  /**
   * Get the SelectionManager managing selections in the ViewerApp
   * @return the SelectionManager
   */
  public SelectionManager getSelectionManager() {
    return selectionManager;
  }
  
  
  public void addAccessory(Component c) {
    addAccessory(c, null);
  }
  
  
  public void addAccessory(Component c, String title) {
    accessory.add(c);
    accessoryTitles.put(c, title);
  }
  
  
  /**
   * Sets the scene root's background color.
   * @param colors list of colors with length = 1 or 4
   */
  public void setBackgroundColor(Color... colors) {
    if (colors == null || (colors.length!=1 && colors.length!=4)) 
      throw new IllegalArgumentException("illegal length of colors[]");
    if (sceneRoot.getAppearance() == null) sceneRoot.setAppearance(new Appearance());
    
    //trim colors[] if it contains the same 4 colors
    if (colors.length == 4) {
      boolean equal = true;
      for (int i = 1; i < colors.length; i++)
        if (colors[i] != colors[0]) equal = false;
      if (equal) colors = new Color[]{ colors[0] };
    }
    
    sceneRoot.getAppearance().setAttribute("backgroundColor", (colors.length==1)? colors[0] : Appearance.INHERITED);
    sceneRoot.getAppearance().setAttribute("backgroundColors", (colors.length==4)? colors : Appearance.INHERITED); 
  }
  
  
  public void dispose() {
    if (autoRender) {
      renderTrigger.removeSceneGraphComponent(sceneRoot);
      renderTrigger.removeViewer(currViewer);
    }
    if (currViewer != null) currViewer.dispose();
    
    frame.dispose();
  }
  
  
  public static void main(String[] args) {
    ViewerApp va = new ViewerApp(null, null, null, null, null);
    va.setAttachNavigator(true);
    va.setAttachBeanShell(true);
    va.update();
    va.display();
  }
  
}