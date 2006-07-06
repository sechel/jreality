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
import java.lang.reflect.Proxy;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import jterm.BshEvaluator;
import jterm.JTerm;
import jterm.Session;
import bsh.EvalError;
import de.jreality.io.JrScene;
import de.jreality.io.JrSceneFactory;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.toolsystem.ToolSystemViewer;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.ui.beans.InspectorPanel;
import de.jreality.ui.treeview.JTreeRenderer;
import de.jreality.ui.treeview.SceneTreeModel;
import de.jreality.ui.treeview.SceneTreeModel.TreeTool;
import de.jreality.util.RenderTrigger;
import de.jreality.util.ViewerSwitch;


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
 * 
 * @author msommer
 */
public class ViewerApp {
  
  private SceneGraphNode displayedNode;  //the node which is displayed in viewer
  
  private UIFactory uiFactory;  //frame layout factory depending on viewer
  
  private Viewer[] viewers;  //containing possible viewers (jogl, soft, portal)
  private ViewerSwitch viewerSwitch;
  private ToolSystemViewer currViewer;  //the current viewer
  
  private SceneGraphComponent sceneRoot;
  private SceneGraphComponent scene;
  //private SceneGraphComponent currSceneNode;
  private InspectorPanel inspector;
  private BshEvaluator bshEval;
  private JTerm jterm;
  private SimpleAttributeSet infoStyle;
  
  private boolean attachNavigator = false;  //default
  private boolean attachBeanShell = false;  //default
  
  private JrScene jrScene;

  /**
   * @param node the SceneGraphNode (SceneGraphComponent or Geometry) to be displayed in the viewer
   */
  public ViewerApp(SceneGraphNode node) {
    this(node, null);
  }
  
  public ViewerApp(SceneGraphNode node, JrScene jrScene) {

    if (node != null)  //create default scene if null
      if (!(node instanceof Geometry) && !(node instanceof SceneGraphComponent))
        throw new IllegalArgumentException("Only Geometry or SceneGraphComponent allowed!");
    
    if (jrScene == null) this.jrScene = getDefaultScene();
    else this.jrScene = jrScene;
    
    displayedNode = node;
  }
  
  
  /**
   * Displays the scene in a JFrame.
   */
  public void display() {
    
    Component content = getViewerComponent();
    
    //set general properties of UI
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) {}
    System.setProperty("sun.awt.noerasebackground", "true");
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    
    //init frame
    JFrame frame = new JFrame("jReality Viewer");
    if (!Beans.isDesignTime()) 
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    Dimension size = frame.getToolkit().getScreenSize();
    size.width*=.7;
    size.height*=.7;
    frame.setSize(size);
    
    //set content of frame
    frame.getContentPane().add(content);
    frame.validate();
    frame.setVisible(true);
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

  public static ViewerApp display(SceneGraphComponent root, SceneGraphPath cameraPath, SceneGraphPath emptyPick, SceneGraphPath avatar) {
    JrScene s = new JrScene();
    s.setSceneRoot(root);
    if (cameraPath!= null) s.addPath("cameraPath", cameraPath);
    if (avatar != null) s.addPath("avatarPath", avatar);
    if (emptyPick != null) s.addPath("emptyPickPath", emptyPick);

    ViewerApp app = new ViewerApp(null, s);
    app.setAttachNavigator(false);
    app.setAttachBeanShell(false);
    app.update();
    app.display();
    
    return app;

  }
  
  /**
   * Calls ViewerAppOld.display(), use if menu is needed.
   */
  public static void displayOld(SceneGraphNode node) {
    
    ViewerAppOld.display(node);
  }
  
  
  /**
   * Update the factory (needs to be invoked before calling display or getter methods). 
   */
  public void update() {
    
    //load the default scene depending on environment (desktop | portal)
    //and with chosen options (attachNavigator | attachBeanShell)
    setupViewer(jrScene);
    
    uiFactory.setAttachNavigator(attachNavigator);
    uiFactory.setAttachBeanShell(attachBeanShell);
  }
  

  /**
   * Get the default Scene depending on the environment (desktop or portal).
   * @return the default scene
   */
  private JrScene getDefaultScene() {
    String environment = System.getProperty("de.jreality.viewerapp.env", "desktop");
    
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

    uiFactory = new UIFactory();
    if (currViewer != null) currViewer.dispose();
    try { currViewer = createViewer(); } 
    catch (Exception exc) { exc.printStackTrace(); }
    
    //set sceneRoot and paths of viewer
    sceneRoot = sc.getSceneRoot();
    currViewer.setSceneRoot(sceneRoot);
    
    SceneGraphPath path = sc.getPath("cameraPath");
    if (path != null) currViewer.setCameraPath(path);
    path = sc.getPath("avatarPath");
    if (path != null) currViewer.setAvatarPath(path);
    path = sc.getPath("emptyPickPath");
    if (path != null) {
      //init scene and current scene node
      //currSceneNode = 
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
    
    //set up bshEval, jterm, infoStyle and uiFactory.beanShell
    //call before setting up navigator
    if (attachBeanShell) setupBeanShell();

    //setup inspector, uiFactory.inspector and uiFactory.sceneTree
    if (attachNavigator) setupNavigator();
  }
  
  
  private ToolSystemViewer createViewer() 
    throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
  {
    if (viewers == null) {
      
      //get list of viewers and create viewer switch
      String availableViewers = System.getProperty("de.jreality.scene.Viewer", "de.jreality.jogl.Viewer de.jreality.soft.DefaultViewer"); // de.jreality.portal.DesktopPortalViewer");
      StringTokenizer st = new StringTokenizer(availableViewers);
      viewers = new Viewer[st.countTokens()];
      for (int i = 0; i < viewers.length; i++) {
        viewers[i] = createViewer(st.nextToken());
      }
      viewerSwitch = new ViewerSwitch(viewers);
    }
    
    //create ToolSystemViewer with configuration corresp. to environment
    ToolSystemConfiguration cfg = null;
    String config = System.getProperty("de.jreality.scene.tool.Config", "default");
    if (config.equals("default")) cfg = ToolSystemConfiguration.loadDefaultDesktopConfiguration();
    if (config.equals("portal")) cfg = ToolSystemConfiguration.loadDefaultPortalConfiguration();
    if (config.equals("default+portal")) cfg = ToolSystemConfiguration.loadDefaultDesktopAndPortalConfiguration();
    if (cfg == null) throw new IllegalStateException("couldn't load config ["+config+"]");
    
    ToolSystemViewer viewer = new ToolSystemViewer(viewerSwitch, cfg);
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
    
    bshEval = new BshEvaluator();
    try {
      bshEval.getInterpreter().eval("import de.jreality.scene.*;");
      bshEval.getInterpreter().eval("import de.jreality.scene.tool.*;");
      bshEval.getInterpreter().eval("import de.jreality.scene.data.*;");
      bshEval.getInterpreter().eval("import de.jreality.geometry.*;");
      bshEval.getInterpreter().eval("import de.jreality.math.*;");    
      bshEval.getInterpreter().eval("import de.jreality.shader.*;");
      bshEval.getInterpreter().eval("import de.jreality.util.*;");
    } catch (EvalError error) {
      error.printStackTrace();
    }
    jterm = new JTerm(new Session(bshEval));
    jterm.setMaximumSize(new Dimension(10, 10));
    
    infoStyle = new SimpleAttributeSet();
    StyleConstants.setForeground(infoStyle, new Color(165, 204, 0));
    StyleConstants.setFontFamily(infoStyle, "Monospaced");
    StyleConstants.setBold(infoStyle, true);
    StyleConstants.setFontSize(infoStyle, 12);
    
    uiFactory.setBeanShell(jterm);
    
    if (!attachNavigator) {  //set self to sceneRoot
      try {
        bshEval.getInterpreter().set("self", sceneRoot);
        String info="\nself="+sceneRoot.getName()+"["+sceneRoot.getClass().getName()+"]\n";
        try {
          jterm.getSession().displayAndPrompt(info, infoStyle);
          jterm.setCaretPosition(jterm.getDocument().getLength());
        } catch (Exception exc) {}  // unpatched jterm
      } catch (EvalError error) { error.printStackTrace(); }
    }  // else self is set in setupNavigator()
    
    if (attachBeanShell) {
      try { 
        bshEval.getInterpreter().set("_viewer", viewerSwitch);
        bshEval.getInterpreter().set("_toolSystemViewer", currViewer);
      } 
      catch (EvalError error) { error.printStackTrace(); }
    }
  }
  
  
  /**
   * Set up the navigator (sceneTree and inspector).
   */
  private void setupNavigator() {
    
    inspector = new InspectorPanel();
    uiFactory.setInspector(inspector);

    JTree sceneTree = new JTree();
    SceneTreeModel model = new SceneTreeModel(sceneRoot);
    sceneTree.setModel(model);
    sceneTree.setCellRenderer(new JTreeRenderer());
    uiFactory.setSceneTree(sceneTree);
    
    
    TreeSelectionModel sm = sceneTree.getSelectionModel();
    sm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    sm.addTreeSelectionListener(new TreeSelectionListener() {
      
      public void valueChanged(TreeSelectionEvent e) {
        Object obj = null;
        TreePath path = e.getNewLeadSelectionPath();
        
        if (path!=null) {
          if (path.getLastPathComponent() instanceof SceneTreeNode) {
            obj = ((SceneTreeNode)path.getLastPathComponent()).getNode();
          } else if (path.getLastPathComponent() instanceof TreeTool) {
            obj = ((TreeTool)path.getLastPathComponent()).getTool();
          } else {
            obj = path.getLastPathComponent();
          }
        }
        
        inspector.setObject(obj);
        
        if ( attachBeanShell && obj!=null) { 
          try {
            bshEval.getInterpreter().set("self", obj);
            String name = (obj instanceof SceneGraphNode) ? ((SceneGraphNode)obj).getName() : "";
            String type = Proxy.isProxyClass(obj.getClass()) ? obj.getClass().getInterfaces()[0].getName() : obj.getClass().getName();
            String info="\nself="+name+"["+type+"]\n";
            try {
              jterm.getSession().displayAndPrompt(info, infoStyle);
              jterm.setCaretPosition(jterm.getDocument().getLength());
            } catch (Exception exc) {
              // unpatched jterm
            }
          } catch (EvalError error) {
            error.printStackTrace();
          }
        }
        
//        //needed for menu
//        if (obj instanceof SceneGraphComponent)
//          currSceneNode = (SceneGraphComponent)obj;
//        else currSceneNode = scene;
      }
    });
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
  public ToolSystemViewer getCurrentViewer() {
    if (currViewer == null)
      throw new UnsupportedOperationException("No viewer instantiated, call update()!");
    
    return currViewer;
  }
  

  /**
   * Get the viewer as a component.
   * @return the viewer component
   */
  public Component getViewerComponent() {
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

  public void dispose() {
    if (currViewer != null) currViewer.dispose();
  }
}