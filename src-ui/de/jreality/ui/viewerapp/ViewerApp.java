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

import java.awt.Component;
import java.awt.Dimension;
import java.beans.Beans;
import java.io.IOException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

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
import de.jreality.ui.viewerapp.actions.app.SwitchBackgroundColor;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;
import de.jreality.util.RenderTrigger;


/**
 * Factory for the jReality Viewer application, which displays a {@link Geometry} or a {@link SceneGraphComponent}.<br>
 * Use the factory as following:<br>
 * <code><b><pre>
 * SceneGraphComponent node;
 * //or
 * Geometry node;
 * [...]<br>
 * ViewerApp viewer = new ViewerApp(node);
 * viewer.setAttachNavigator(true);
 * viewer.setAttachBeanShell(false);
 * [setting more properties]<br>
 * viewer.update();
 * viewer.display();
 * </pre></b></code>
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
  
  private boolean showMenu = false;  //default
  private JMenuBar menuBar;
  
  private JrScene jrScene;

  private boolean autoRender = true;
  private boolean synchRender = false;

  /**
   * @param node the SceneGraphNode (SceneGraphComponent or Geometry) to be displayed in the viewer
   */
  public ViewerApp(SceneGraphNode node) {
    this(node, null, null, null, null);
  }
  
  public ViewerApp(SceneGraphComponent root, SceneGraphPath cameraPath, SceneGraphPath emptyPick, SceneGraphPath avatar) {
    this(null, root, cameraPath, emptyPick, avatar);
  }
  
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
    String autoRenderProp = getProperty("de.jreality.ui.viewerapp.autoRender", "true");
    if (autoRenderProp.equalsIgnoreCase("false")) {
      autoRender = false;
    }
    String synchRenderProp = getProperty("de.jreality.ui.viewerapp.synchRender", "true");
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
    selectionManager = new SelectionManager(jrScene.getPath("emptyPickPath"));
    MenuFactory menuFactory = new MenuFactory(this);  //uses frame, viewerSwitch, selectionManager and viewerapp itself
    menuBar = menuFactory.getMenuBar();
  }
  
  
  /**
   * Displays the scene in a JFrame.
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
    
    //set viewer background colors
    if (sceneRoot.getAppearance() != null && (sceneRoot.getAppearance().getAttribute(CommonAttributes.BACKGROUND_COLORS) == Appearance.INHERITED && sceneRoot.getAppearance().getAttribute(CommonAttributes.BACKGROUND_COLOR) == Appearance.INHERITED)) {
        sceneRoot.getAppearance().setAttribute("backgroundColors", SwitchBackgroundColor.defaultColor);
      }
    
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
    frame.setJMenuBar(menuBar);
    if (!showMenu) {  
    	//hide all menus, then keystrokes for actions are still working,
    	//which is not the case when hiding menuBar
    	for (int i = 0; i < menuBar.getComponentCount(); i++)
    	  menuBar.getMenu(i).setVisible(false);
    }
    
    frame.validate();
    
    //encompass scene before displaying
//    CameraUtility.encompass(currViewer);
//    currViewer.render();
	
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
   * @param root the scene root
   * @param cameraPath the camera path
   * @param emptyPick the empty pick path
   * @param avatar the avatar path
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
   * Update the factory (needs to be invoked before calling display or getter methods). 
   */
  public void update() {
    
    //set up beanShell and uiFactory.beanShell
    if (attachBeanShell) setupBeanShell();
    else beanShell = null;

    //setup navigator, uiFactory.inspector and uiFactory.sceneTree
    if (attachNavigator) setupNavigator();
    else navigator = null;
    
    uiFactory.setAttachNavigator(attachNavigator);
    uiFactory.setAttachBeanShell(attachBeanShell);

    //update selectionManager
    if (attachNavigator) 
      selectionManager.attachNavigator(navigator);
  }
  

  private static String getProperty( String key, String def ) {
    try {
      return System.getProperty( key, def );
    } 
    catch( AccessControlException e ) {
      return def;
    }
  }
  
  
  /**
   * Get the default Scene depending on the environment (desktop or portal).
   * @return the default scene
   */
  private JrScene getDefaultScene() {
    String environment = getProperty("de.jreality.viewerapp.env", "desktop");
    
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
    
    uiFactory = new UIFactory();
    
    try { currViewer = createViewer(); } 
    catch (Exception exc) { exc.printStackTrace(); }

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
    uiFactory.setViewer((Component) currViewer.getViewingComponent());
    
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
      
      String viewer = getProperty("de.jreality.scene.Viewer", "de.jreality.jogl.Viewer de.jreality.soft.DefaultViewer"); // de.jreality.portal.DesktopPortalViewer");
      StringTokenizer st = new StringTokenizer(viewer);
      List<Viewer> viewerList = new LinkedList<Viewer>();
      String viewerClassName;
      while (st.hasMoreTokens()) {
        viewerClassName = st.nextToken();
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
    ToolSystemConfiguration cfg = null;
    
    String config = getProperty("de.jreality.scene.tool.Config", "default");
    
    // HACK: only works for "regular" URLs
    if (config.contains("://")) {
      cfg = ToolSystemConfiguration.loadConfiguration(new Input(new URL(config)));
    } else {
      if (config.equals("default")) cfg = ToolSystemConfiguration.loadDefaultDesktopConfiguration();
      if (config.equals("portal")) cfg = ToolSystemConfiguration.loadDefaultPortalConfiguration();
      if (config.equals("default+portal")) cfg = ToolSystemConfiguration.loadDefaultDesktopAndPortalConfiguration();
    }    
    if (cfg == null) throw new IllegalStateException("couldn't load config ["+config+"]");
    
    ToolSystemViewer viewer = new ToolSystemViewer(viewerSwitch, cfg, synchRender ? renderTrigger : null);
    viewer.setPickSystem(new AABBPickSystem());
    
    return viewer;
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
    
    beanShell.setCurrViewer(currViewer);
    beanShell.setViewerSwitch(viewerSwitch);
    beanShell.setSelf(sceneRoot);
    
    uiFactory.setBeanShell(beanShell.getJTerm());
  }
  
  
  /**
   * Set up the navigator (sceneTree and inspector).
   */
  private void setupNavigator() {
    navigator = new Navigator(sceneRoot);
    if (attachBeanShell) navigator.setBeanShell(beanShell);
    
    uiFactory.setInspector(navigator.getInspector());
    uiFactory.setSceneTree(navigator.getSceneTree());
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
    
    return uiFactory.getViewer();
  }
  
   
  /**
   * Returns true iff a navigator is attached to the viewer.
   */
  public boolean isAttachBeanShell() {
    return attachBeanShell;
  }

  
  /**
   * Returns true iff a bean shell is attached to the viewer. 
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
   * Use to include a MenuBar and context menus in ViewerApp.
   * @param b true iff menu is to be shown
   */
  public void setShowMenu(boolean b) {
    showMenu = b;
  }
  
  public boolean isShowMenu() {
	  return showMenu;
  }
  
//-- MENU METHODS -------------
  
  public JMenuBar getMenuBar() {
	  return menuBar;
  }
  
  public void addMenu(JMenu menu) {
    addMenu(menu, menuBar.getComponentCount());  //add to end of menuBar
  }
  
  public void addMenu(JMenu menu, int index) {
    if (index < 0 || index > menuBar.getComponentCount())
      throw new IllegalArgumentException("invalid index");

    menuBar.add(menu, index);
  }
  
  public JMenu getMenu(String menuName) {
	JMenu menu = null;
	for (int i = 0; i < menuBar.getComponentCount(); i++) {
		if ( ((JMenu)menuBar.getComponent(i)).getText().equals(menuName) )
			menu = (JMenu)menuBar.getComponent(i);
	}
	return menu;
  }
  
  public void addMenuItem(JMenuItem item, String menuName) {
    addMenuItem(item, menuName, menuBar.getComponentCount());  //add to end of menu
  }
  
  public void addMenuItem(JMenuItem item, String menuName, int index) {
	JMenu menu = getMenu(menuName);
	if (menu != null) menu.insert(item, index);
  }
  
  public void addAction(Action a, String menuName) {
    addAction(a, menuName, menuBar.getComponentCount());  //add to end of menu
  }
  
  public void addAction(Action a, String menuName, int index) {
    addMenuItem(new JMenuItem(a), menuName, index);
  }
  
  

  /**
   * Get the SelectionManager managing selections in the ViewerApp
   * @return the SelectionManager
   */
  public SelectionManager getSelectionManager() {
    return selectionManager;
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
	va.setShowMenu(true);
	va.setAttachNavigator(true);
	va.setAttachBeanShell(true);
	va.update();
	va.display();
  }
  
}