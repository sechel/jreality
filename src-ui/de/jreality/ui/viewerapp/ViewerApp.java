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
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
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
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.io.JrScene;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.ReaderJRS;
import de.jreality.reader.Readers;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.scene.tool.DraggingTool;
import de.jreality.scene.tool.FlyTool;
import de.jreality.scene.tool.HeadTransformationTool;
import de.jreality.scene.tool.PointerDisplayTool;
import de.jreality.scene.tool.RotateTool;
import de.jreality.scene.tool.ScaleTool;
import de.jreality.scene.tool.ShipNavigationTool;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolSystemViewer;
import de.jreality.scene.tool.config.ToolSystemConfiguration;
import de.jreality.ui.treeview.JListRenderer;
import de.jreality.ui.treeview.SceneTreeModel.TreeTool;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;
import de.jreality.util.PickUtility;
import de.jreality.util.RenderTrigger;
import de.jreality.util.ViewerSwitch;
import de.jreality.writer.WriterJRS;

/**
 * TODO: comment ViewerApp
 */
public class ViewerApp
{
  
  public static final String ABOUT_MESSAGE="<html><body><center><b>jReality viewer</b></center><br>preview version</body></html>";
  public static final String HELP_MESSAGE="<html>jReality viewer help<ul>"+"<li>left mouse - rotate</li>"+"<li>middle mouse - drag</li>"+"<li>CRTL + middle mouse - drag along view direction</li>"+"<li>e - encompass</li>"+"<li>BACKSPACE - toggle fullscreen</li>"+"</ul></html>";
  private static Viewer[] viewers;
  private static ViewerSwitch viewerSwitch;
  
  private InspectorPanel inspector;
  private SceneGraphComponent currSceneNode;
  private SceneGraphComponent scene;
  private SceneGraphComponent root;
  private UIFactory uiFactory;
  
  private JFrame frame;
  
  private ToolSystemViewer currViewer;
  
  private SceneGraphPath emptyPick;
  private RenderTrigger renderTrigger = new RenderTrigger();

  boolean autoRender=true;
  
  JTerm jterm;
  private BshEvaluator bshEval;
  private SimpleAttributeSet infoStyle;
  
  public static void display(final SceneGraphNode n) {
    initAWT();
    final ViewerApp app;
    try {
      app = new ViewerApp(true);
    } catch (Exception e) {
      throw new RuntimeException("creating viewer failed: "+e.getMessage());
    }
    n.accept(new SceneGraphVisitor() {
      public void visit(SceneGraphComponent sc) {
        app.scene.addChild(sc);
      }
      public void visit(Geometry g) {
        app.scene.setGeometry(g);
      }
    });
  }
  
  public static void main(String[] args) throws Exception
  {
    initAWT();
    new ViewerApp(true);
  }

  private static void initAWT() {
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) {
      LoggingSystem.getLogger(ViewerApp.class).config("loading cross platform Look & Feel failed: "+e.getMessage());
    }
    System.setProperty("sun.awt.noerasebackground", "true");
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
  }
  
  public ViewerApp(boolean initScene) throws Exception {
    
    bshEval = new BshEvaluator();
    
    bshEval.getInterpreter().eval("import de.jreality.scene.*;");
    bshEval.getInterpreter().eval("import de.jreality.scene.tool.*;");
    bshEval.getInterpreter().eval("import de.jreality.scene.data.*;");
    bshEval.getInterpreter().eval("import de.jreality.geometry.*;");
    bshEval.getInterpreter().eval("import de.jreality.math.*;");    
    bshEval.getInterpreter().eval("import de.jreality.shader.*;");
    bshEval.getInterpreter().eval("import de.jreality.util.*;");
    
    jterm = new JTerm(new Session(bshEval));
    jterm.setMaximumSize(new Dimension(10, 10));

    infoStyle = new SimpleAttributeSet();
    StyleConstants.setForeground(infoStyle, new Color(165, 204, 0));
    StyleConstants.setFontFamily(infoStyle, "Monospaced");
    StyleConstants.setBold(infoStyle, true);
    StyleConstants.setFontSize(infoStyle, 12);
    
    inspector=new InspectorPanel();

    currViewer = createViewer();
    
    uiFactory=new UIFactory();
    
    uiFactory.setViewer(currViewer.getViewingComponent());
    uiFactory.setInspector(inspector);
    uiFactory.setConsole(jterm);

    String env = System.getProperty("de.jreality.viewerapp.env", "desktop");
    initDefaultScene(env);

    createFrame(uiFactory.createViewerContent());
    initFrame();
    initTree();
    createMenu();
    frame.show();
    
    String autoRenderProp = System.getProperty("de.jreality.ui.viewerapp.autorender", "true");
    if (autoRenderProp.equalsIgnoreCase("false")) {
      autoRender = false;
    }

  }

  void createFrame(Container content)
  {
    if (frame == null) {
      frame=new JFrame("jReality Viewer");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    frame.setContentPane(content);
  }
  
  void initFrame() {
    frame.pack();
    Dimension size = frame.getToolkit().getScreenSize();
    size.width*=.7;
    size.height*=.7;
    frame.setSize(size);
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
      frame.setJMenuBar(null);
      frame.setContentPane(new Container());
      frame.getContentPane().setLayout(new BorderLayout());
      frame.getContentPane().add("Center", currViewer.getViewingComponent());
    } else {
      frame.setJMenuBar(mb);
      frame.setContentPane(uiFactory.createViewerContent());
      initTree();
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
    currViewer.render();
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
        if (o instanceof SceneGraphNode) {
          try {
            SceneGraphNode sn = (SceneGraphNode) o;
            bshEval.getInterpreter().set("self", sn);
            String info="\nself="+sn.getName()+"["+sn.getClass().getName()+"]\n";
            try {
              jterm.getSession().displayAndPrompt(info, infoStyle);
              jterm.setCaretPosition(jterm.getDocument().getLength());
            } catch (Exception ex) {
              // unpatched jterm
            }
          } catch (EvalError e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        }
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

    mi = new JMenuItem("Load merged ...");
    mi.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent arg0) {
    		  File[] files = FileLoaderDialog.loadFiles(frame);
          for (int i = 0; i < files.length; i++) {
    				try {
    					SceneGraphComponent f = Readers.read(files[i]);
    					f=IndexedFaceSetUtility.mergeIndexedFaceSets(f);
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
            loadScene(s);
          } catch (IOException ioe) {
            JOptionPane.showMessageDialog(frame, "Load failed: "+ioe.getMessage());
          }
      }
    });
    fileMenu.add(mi);

    fileMenu.addSeparator();

    mi = new JMenuItem("Desktop Scene");
    mi.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
          initDefaultScene("desktop");
      }
    });
    fileMenu.add(mi);

    mi = new JMenuItem("Portal Scene");
    mi.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
          initDefaultScene("portal");
      }
    });
    fileMenu.add(mi);

    fileMenu.addSeparator();
    
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

    fileMenu.addSeparator();
    
    mi = new JMenuItem("Quit");
    mi.addActionListener(new ActionListener(){
		  public void actionPerformed(ActionEvent arg0) {
			  System.exit(0);
		  }
    });
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

    mi = new JMenuItem("Add Tool...");
    mi.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
          List tools = new LinkedList();
          tools.add(RotateTool.class);
          tools.add(DraggingTool.class);
          tools.add(ScaleTool.class);
          tools.add(FlyTool.class);
          tools.add(HeadTransformationTool.class);
          tools.add(ShipNavigationTool.class);
          tools.add(PointerDisplayTool.class);
          try {
            tools.add(Class.forName("de.jreality.scene.tool.PortalHeadMoveTool"));
          } catch (ClassNotFoundException e) {}
          JList list = new JList(tools.toArray());
          //list.setCellRenderer(new JListRenderer());
          list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
          int ret = JOptionPane.showConfirmDialog(frame, uiFactory.scroll(list), "Add Tool", JOptionPane.OK_CANCEL_OPTION);
          if (ret == JOptionPane.OK_OPTION) {
            int[] idx = list.getSelectedIndices();
            for (int i = 0; i < idx.length; i++) {
              try {
                Tool t = (Tool) ((Class)tools.get(idx[i])).newInstance();
                currSceneNode.addTool(t);
              } catch (Exception e) {
                LoggingSystem.getLogger(ViewerApp.this).warning("could not add tool!");
              }
            }
          }
        }
    });
    compMenu.add(mi);

    mi = new JMenuItem("Remove Tool...");
    mi.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
          List children = currSceneNode.getTools();
          JList list = new JList(children.toArray());
          //list.setCellRenderer(new JListRenderer());
          list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
          int ret = JOptionPane.showConfirmDialog(frame, uiFactory.scroll(list), "Remove child", JOptionPane.OK_CANCEL_OPTION);
          if (ret == JOptionPane.OK_OPTION) {
            Object[] tools = list.getSelectedValues();
            for (int i = 0; i < tools.length; i++) {
              currSceneNode.removeTool((Tool) tools[i]);
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

    compMenu.addSeparator();
    
    mi = new JMenuItem("Make Geometry pickable");
    mi.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
          Geometry geom = currSceneNode.getGeometry();
          if (geom != null && geom instanceof IndexedFaceSet) {
            PickUtility.assignFaceAABBTree((IndexedFaceSet)geom);
          } else {
            JOptionPane.showMessageDialog(frame, "need IndexedFaceSet");
          }
        }
    });

    compMenu.add(mi);
    mi = new JMenuItem("Make Subgraph pickable");
    mi.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent arg0) {
          
          if (currSceneNode != null) {
            String trisPerBox = JOptionPane.showInputDialog(frame, "maxTrisPerBox");
            int numTris = 10;
            try {
              numTris = Integer.parseInt(trisPerBox);
            } catch (Exception e) {}
            PickUtility.assignFaceAABBTrees(currSceneNode, numTris);
          } else JOptionPane.showMessageDialog(frame, "Select a component fist!");
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
    viewerMenu.addSeparator();
    mi = new JMenuItem("force render");
    mi.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent arg0) {
        currViewer.render();
      }
    });
    viewerMenu.add(mi);
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
    
    mb.add(javax.swing.Box.createHorizontalGlue());
    
    JMenu helpMenu = new JMenu("Help");
    
    mi = new JMenuItem("About");
    mi.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent arg0) {
        JOptionPane.showMessageDialog(frame, ABOUT_MESSAGE);
      }
    });

    helpMenu.add(mi);
    mi = new JMenuItem("Help...");
    mi.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent arg0) {
        JOptionPane.showMessageDialog(frame, HELP_MESSAGE);
      }
    });
    helpMenu.add(mi);

    mb.add(helpMenu);

    frame.setJMenuBar(mb);
  }
  
  private void loadScene(JrScene s) {
    try {
      currViewer = createViewer();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    if (autoRender && root != null)
      renderTrigger.removeSceneGraphComponent(root);

    root = s.getSceneRoot();
    currViewer.setSceneRoot(root);
    SceneGraphPath p = s.getPath("cameraPath");
    if (p != null) currViewer.setCameraPath(p);
    p = s.getPath("avatarPath");
    if (p != null) currViewer.setAvatarPath(p);
    emptyPick = s.getPath("emptyPickPath");
    if (emptyPick != null) {
      currSceneNode = scene = emptyPick.getLastComponent();
      currViewer.setEmptyPickPath(emptyPick);
    }

    uiFactory.setViewer(currViewer.getViewingComponent());
    uiFactory.setRoot(root);
    createFrame(uiFactory.createViewerContent());
    initFrame();
    initTree();
    
    if (autoRender)
      renderTrigger.addSceneGraphComponent(root);

    renderTrigger.forceRender();

//    frame.repaint();
//    CameraUtility.encompass(currViewer.getAvatarPath(), emptyPick, currViewer.getCameraPath());
  }
  
  private ToolSystemViewer createViewer() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
  {
    if (viewers == null) {

      String viewer=System.getProperty("de.jreality.scene.Viewer", "de.jreality.jogl.Viewer de.jreality.soft.DefaultViewer"); // de.jreality.portal.DesktopPortalViewer");
      StringTokenizer st = new StringTokenizer(viewer);
      viewers = new Viewer[st.countTokens()];
      for (int i = 0; i < viewers.length; i++) {
        viewers[i] = createViewer(st.nextToken());
      }
      viewerSwitch = new ViewerSwitch(viewers);
      try {
        bshEval.getInterpreter().set("_viewer", viewerSwitch);
      } catch (EvalError e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      renderTrigger.addViewer(viewerSwitch);
      viewerSwitch.getViewingComponent().addKeyListener(new KeyListener() {
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
    ToolSystemConfiguration cfg = null;
    String config=System.getProperty("de.jreality.scene.tool.Config", "default");
    if (config.equals("default")) cfg = ToolSystemConfiguration.loadDefaultDesktopConfiguration();
    if (config.equals("portal")) cfg = ToolSystemConfiguration.loadDefaultPortalConfiguration();
    if (config.equals("default+portal")) cfg = ToolSystemConfiguration.loadDefaultDesktopAndPortalConfiguration();
    if (cfg == null) throw new IllegalStateException("couldn't load config ["+config+"]");
    ToolSystemViewer v = new ToolSystemViewer(viewerSwitch, cfg);
    v.setPickSystem(new AABBPickSystem());
    return v;
  }
  private void initDefaultScene(String environment) {
    if (!environment.equals("desktop") && !environment.equals("portal"))
      throw new IllegalArgumentException("unknown environment!");
    try {
      ReaderJRS r = new ReaderJRS();
      r.setInput(new Input(ViewerApp.class.getResource(environment+"-scene.jrs")));
      loadScene(r.getScene());
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(frame, "Load failed: "+ioe.getMessage());
    }
  }

  private static Viewer createViewer(String viewer) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
  {
    return (Viewer)Class.forName(viewer).newInstance();
  }
}
