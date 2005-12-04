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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.jreality.io.JrScene;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.ReaderJRS;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.scene.tool.DraggingTool;
import de.jreality.scene.tool.EncompassTool;
import de.jreality.scene.tool.RotateTool;
import de.jreality.scene.tool.ToolSystemViewer;
import de.jreality.scene.tool.config.ToolSystemConfiguration;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.RootAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.treeview.JListRenderer;
import de.jreality.ui.treeview.SceneTreeModel.TreeTool;
import de.jreality.util.Input;
import de.jreality.util.RenderTrigger;
import de.jreality.util.ViewerSwitch;
import de.jreality.writer.WriterJRS;

/**
 * TODO: comment ViewerApp
 */
public class ViewerApp
{
  private InspectorPanel inspector;
  private SceneGraphComponent currSceneNode;
  private SceneGraphComponent scene;
  private SceneGraphComponent root;
  private SceneGraphPath cameraPath;
  private UIFactory uiFactory;
  
  private JFrame frame;
  
  private ToolSystemViewer currViewer;
  private ViewerSwitch viewerSwitch;
  
  private SceneGraphPath emptyPick;
  private SceneGraphPath avatarPath;

  public static void main(String[] args) throws Exception
  {
    //UIManager.setLookAndFeel("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
    System.setProperty("sun.awt.noerasebackground", "true");
    new ViewerApp(createViewer(), true);
  }
  
  public ViewerApp(ToolSystemViewer viewer, boolean initScene) throws Exception {
    inspector=new InspectorPanel();

    currViewer=viewer;
    viewerSwitch = (ViewerSwitch) currViewer.getDelegatedViewer();
    
    if (initScene) {
      root=buildRoot();
      currSceneNode = scene;
      currViewer.setSceneRoot(root);
      currViewer.setCameraPath(cameraPath);
      currViewer.setAvatarPath(avatarPath);
      currViewer.setEmptyPickPath(emptyPick);
    } else {
      root = viewer.getSceneRoot();
    }
    uiFactory=new UIFactory();
    uiFactory.setViewer(currViewer.getViewingComponent());
    uiFactory.setInspector(inspector);
    uiFactory.setRoot(root);
    createFrame(uiFactory.createViewerContent());
    initFrame();
    initTree();
    createMenu();
    frame.show();
    RenderTrigger rt = new RenderTrigger();
    rt.addViewer(currViewer);
    rt.addSceneGraphComponent(root);
  }
  
  void createFrame(Container content)
  {
    if (frame == null) {
      frame=new JFrame("jReality Viewer");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.addKeyListener(new KeyListener() {
        public void keyTyped(KeyEvent arg0) {
        }
        public void keyPressed(KeyEvent arg0) {
          if (arg0.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            toggleFullScreen();
          }
        }
        public void keyReleased(KeyEvent arg0) {
        }
      });
    }
    frame.setContentPane(content);
  }
  
  void initFrame() {
    frame.pack();
    frame.setSize(Math.max(900, frame.getWidth()), Math.max(600, frame.getHeight()));
    frame.validate();
  }

  boolean isFullScreen;
  private JMenuBar mb;
  
  void toggleFullScreen() {
    isFullScreen = !isFullScreen;
    if(isFullScreen) {
      frame.dispose();
      frame.setUndecorated(true);
    }
    if (isFullScreen) {
      frame.setContentPane(new Container());
      frame.getContentPane().setLayout(new BorderLayout());
      frame.getContentPane().add("Center", currViewer.getViewingComponent());
      frame.setMenuBar(null);
    } else {
      frame.setJMenuBar(mb);
      frame.setContentPane(uiFactory.createViewerContent());
    }

    frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(
      isFullScreen ? frame : null
    );
    if(!isFullScreen) {
      frame.dispose();
      frame.setUndecorated(false);
    }
    frame.validate();
    frame.show();
  }
  
  private void initTree() {
    TreeSelectionModel sm=uiFactory.sceneTree.getSelectionModel();
    sm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    sm.addTreeSelectionListener(new TreeSelectionListener() {
    
      	public void valueChanged(TreeSelectionEvent e) {
        Object o=null;
        TreePath p= e.getNewLeadSelectionPath();
        if(p!=null) {
          if (p.getLastPathComponent() instanceof SceneTreeNode) {
            o=((SceneTreeNode)p.getLastPathComponent()).getNode();
          } else if (p.getLastPathComponent() instanceof TreeTool) {
            o = ((TreeTool)p.getLastPathComponent()).getTool();
          } else {
            o = p.getLastPathComponent();
          }
        }
        System.out.println("setting "+(o==null? "null": o.getClass().getName()));
        inspector.setObject(o);
        if (o instanceof SceneGraphComponent) {
        	  currSceneNode = (SceneGraphComponent) o;
        } else {
        	  currSceneNode = scene;
        }
      }
    });
  }
  
  private void createMenu() {
	  mb = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem mi = new JMenuItem("Load...");
    mi.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent arg0) {
    		  File[] files = FileLoaderDialog.loadFiles(frame);
          for (int i = 0; i < files.length; i++) {
    				try {
    					SceneGraphComponent f = Readers.read(files[i]);
              f.setName(files[i].getName());
              System.out.println("READ finished.");
    					currSceneNode.addChild(f);
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
      }
    });
    
    fileMenu.add(mi);

    mi = new JMenuItem("Save...");
    mi.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
          File file = FileLoaderDialog.selectTargetFile(frame);
          if (file == null) return;
          try {
            FileWriter fw = new FileWriter(file);
            WriterJRS writer = new WriterJRS();
            JrScene s = new JrScene(root);
            s.addPath("cameraPath", currViewer.getCameraPath());
            s.addPath("avatarPath", currViewer.getAvatarPath());
            s.addPath("emptyPickPath", currViewer.getEmptyPickPath());
            writer.writeScene(s, fw);
            fw.close();
          } catch (IOException ioe) {
            JOptionPane.showMessageDialog(frame, "Save failed: "+ioe.getMessage());
          }
        }
      });
    
    fileMenu.add(mi);
    
    mi = new JMenuItem("Load Scene...");
    
    mi.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
          File[] fs = FileLoaderDialog.loadFiles(frame);
          if (fs == null || fs.length == 0) return;
          File f = fs[0];
          JrScene s = null;
          try {
            ReaderJRS r = new ReaderJRS();
            r.setInput(new Input(f));
            s = r.getScene();
            try {
              currViewer = createViewer();
            } catch (Exception e) {
              e.printStackTrace();
            }
            root = s.getSceneRoot();
            currViewer.setSceneRoot(root);
            SceneGraphPath p = s.getPath("cameraPath");
            if (p != null) currViewer.setCameraPath(p);
            p = s.getPath("avatarPath");
            if (p != null) currViewer.setAvatarPath(p);
            emptyPick = s.getPath("emptyPickPath");
            if (emptyPick != null) {
              scene = emptyPick.getLastComponent();
              currViewer.setEmptyPickPath(emptyPick);
            }
 
            uiFactory.setViewer(currViewer.getViewingComponent());
            uiFactory.setRoot(root);
            createFrame(uiFactory.createViewerContent());
            initFrame();
            initTree();
            RenderTrigger rt = new RenderTrigger();
            rt.addViewer(currViewer);
            rt.addSceneGraphComponent(root);
            rt.forceRender();
          } catch (IOException ioe) {
            JOptionPane.showMessageDialog(frame, "Load failed: "+ioe.getMessage());
          }
      }
    });
    
    fileMenu.add(mi);
    mi = new JMenuItem("Quit");
    mi.addActionListener(new ActionListener(){
		  public void actionPerformed(ActionEvent arg0) {
			  System.exit(0);
		  }
    });
    fileMenu.addSeparator();
    fileMenu.add(mi);
    mb.add(fileMenu);

    JMenu compMenu = new JMenu("Component");
    mi = new JMenuItem("Remove child...");
    mi.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
          List children = currSceneNode.getChildNodes();
          JList list = new JList(children.toArray());
          list.setCellRenderer(new JListRenderer());
          list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
          int ret = JOptionPane.showConfirmDialog(frame, uiFactory.scroll(list), "Remove child", JOptionPane.OK_CANCEL_OPTION);
          if (ret == JOptionPane.OK_OPTION) {
            int[] idx = list.getSelectedIndices();
            for (int i = 0; i < idx.length; i++) {
              currSceneNode.removeChildNode((SceneGraphNode)children.get(idx[i]));
            }
          }
        }
    });
    compMenu.add(mi);

    mi = new JMenuItem("Scale...");
    mi.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
          try {
            if (currSceneNode != null) {
              double factor = Double.parseDouble(JOptionPane.showInputDialog(frame, "Scale factor"));
              MatrixBuilder.euclidean(currSceneNode.getTransformation()).scale(factor).assignTo(currSceneNode);
            } else {
              JOptionPane.showMessageDialog(frame, "no component selected!");
            }
          } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "illegal input!");
          }
        }
    });

    compMenu.add(mi);
    mb.add(compMenu);

    JMenu viewerMenu = new JMenu("Viewer");
    String[] viewerNames = viewerSwitch.getViewerNames();
    for (int i = 0; i < viewerSwitch.getNumViewers(); i++) {
      mi = new JMenuItem(viewerNames[i]);
      final int ind = i;
      mi.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
          viewerSwitch.selectViewer(ind);
          frame.validate();
          currViewer.render();
          frame.repaint();
        }
      });
      viewerMenu.add(mi);
    }  
    mb.add(viewerMenu);

    JMenu frameMenu = new JMenu("Frame");
    mi = new JMenuItem("Fullscreen");
    mi.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent arg0) {
        toggleFullScreen();
      }
    });
    frameMenu.add(mi);

    mb.add(frameMenu);
    
    frame.setJMenuBar(mb);
  }
  
  private SceneGraphComponent buildRoot()
  {

    SceneGraphComponent root=new SceneGraphComponent();
    root.setName("root");

    scene = new SceneGraphComponent();
  	scene.setName("scene");
	  root.addChild(scene);

    SceneGraphComponent avatarNode= new SceneGraphComponent();
    avatarNode.setName("avatar");
    Transformation at= new Transformation();
    at.setName("avatar Trafo");
    avatarNode.setTransformation(at);
    //
    // camera
    //
    SceneGraphComponent cameraNode= new SceneGraphComponent();
    cameraNode.setName("camera Node");
    MatrixBuilder.euclidean().translate(0, 0, 16).assignTo(cameraNode);
    Camera firstCamera= new Camera();
    firstCamera.setName("camera");
    firstCamera.setFieldOfView(30);
    firstCamera.setFar(50);
    firstCamera.setNear(3);
    cameraNode.setCamera(firstCamera);

    SceneGraphComponent lightNode= new SceneGraphComponent();
    lightNode.setName("lightComp 1");
    MatrixBuilder.euclidean().rotate(-Math.PI / 4, 1, 1, 0).assignTo(lightNode);
    DirectionalLight light= new DirectionalLight();
    light.setName("light 1");
    lightNode.setLight(light);
    root.addChild(lightNode);

    SceneGraphComponent lightNode2= new SceneGraphComponent();
    lightNode2.setName("lightComp 2");
    MatrixBuilder.euclidean().rotate(-Math.PI / 4, 1, 1, 0).assignTo(lightNode2);
    DirectionalLight light2= new DirectionalLight();
    light2.setName("light 2");
    lightNode2.setLight(light2);
    root.addChild(lightNode2);


    Appearance ap= new Appearance();
    ap.setName("root appearance");
    DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
    RootAppearance ra = ShaderUtility.createRootAppearance(ap);
    DefaultLineShader dls = (DefaultLineShader) dgs.getLineShader();
    DefaultPolygonShader dps = (DefaultPolygonShader) dgs.getPolygonShader();

    root.setAppearance(ap);

    root.addChild(avatarNode);
    avatarNode.addChild(cameraNode);
    
    avatarPath=new SceneGraphPath();
    avatarPath.push(root);
    avatarPath.push(avatarNode);
    
    cameraPath = avatarPath.pushNew(cameraNode);
    cameraPath.push(firstCamera);
    
    emptyPick = new SceneGraphPath();
    emptyPick.push(root);
    emptyPick.push(scene);
    
	scene.addTool(new EncompassTool());
	scene.addTool(new RotateTool());
	scene.addTool(new DraggingTool());

    return root;
  }
  private static ToolSystemViewer createViewer() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
  {
    String viewer=System.getProperty("de.jreality.scene.Viewer", "de.jreality.jogl.Viewer de.jreality.soft.DefaultViewer");
    StringTokenizer st = new StringTokenizer(viewer);
    Viewer[] viewers = new Viewer[st.countTokens()];
    for (int i = 0; i < viewers.length; i++) {
      viewers[i] = createViewer(st.nextToken());
    }
    ViewerSwitch vs = new ViewerSwitch(viewers);
    ToolSystemViewer v = new ToolSystemViewer(vs, ToolSystemConfiguration.loadDefaultDesktopConfiguration());
    v.setPickSystem(new AABBPickSystem());
    return v;
  }
  private static Viewer createViewer(String viewer) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
  {
    return (Viewer)Class.forName(viewer).newInstance();
  }
}
