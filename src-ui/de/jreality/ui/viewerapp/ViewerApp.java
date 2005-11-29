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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
import de.jreality.shader.TextureUtility;
import de.jreality.ui.treeview.JListRenderer;
import de.jreality.util.RenderTrigger;

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
  private SceneGraphPath emptyPick;
  private SceneGraphPath avatarPath;

  public static void main(String[] args) throws Exception
  {
    System.setProperty("sun.awt.noerasebackground", "true");
    new ViewerApp();
  }
  
  ViewerApp() throws Exception {
    root=buildRoot();
    currSceneNode = scene;
	
    inspector=new InspectorPanel();

    currViewer=createViewer();
    currViewer.setSceneRoot(root);
    currViewer.setCameraPath(cameraPath);
    currViewer.setAvatarPath(avatarPath);
    currViewer.setEmptyPickPath(emptyPick);
    
    uiFactory=new UIFactory();
    uiFactory.setViewer(currViewer.getViewingComponent());
    uiFactory.setInspector(inspector);
    uiFactory.setRoot(root);
    frame=uiFactory.createFrame();
    TreeSelectionModel sm=uiFactory.sceneTree.getSelectionModel();
    sm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    sm.addTreeSelectionListener(new TreeSelectionListener() {
    
      	public void valueChanged(TreeSelectionEvent e) {
        Object o=null;
        TreePath p= e.getNewLeadSelectionPath();
        if(p!=null) {
          if (p.getLastPathComponent() instanceof SceneTreeNode) {
            o=((SceneTreeNode)p.getLastPathComponent()).getNode();
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
    createMenu();
    frame.show();
    RenderTrigger rt = new RenderTrigger();
    rt.addViewer(currViewer);
    rt.addSceneGraphComponent(root);
  }
  
  private void createMenu() {
	  JMenuBar mb = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem mi = new JMenuItem("Load...");
    mi.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent arg0) {
    		  File[] files = FileLoaderDialog.loadFiles(frame);
          for (int i = 0; i < files.length; i++) {
    				try {
    					SceneGraphComponent f = Readers.read(files[i]);
              f.setName(files[i].getName());
    					currSceneNode.addChild(f);
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
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
    mb.add(compMenu);
    
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
    avatarNode.setName("avatar Node");
    Transformation at= new Transformation();
    at.setName("avatar Trafo");
    avatarNode.setTransformation(at);
    //
    // camera
    //
    SceneGraphComponent cameraNode= new SceneGraphComponent();
    cameraNode.setName("camera Node");
    Transformation ct= new Transformation();
    ct.setName("cam Trafo");
    ct.setTranslation(0, 0, 16);
    cameraNode.setTransformation(ct);
    Camera firstCamera= new Camera();
    firstCamera.setName("cam");
    firstCamera.setFieldOfView(30);
    firstCamera.setFar(50);
    firstCamera.setNear(3);
    cameraNode.setCamera(firstCamera);

    SceneGraphComponent lightNode= new SceneGraphComponent();
    lightNode.setName("lightComp 1");
    Transformation lt= new Transformation();
    lt.setName("lightTrafo 1");
    lt.setRotation(-Math.PI / 4, 1, 1, 0);
    lightNode.setTransformation(lt);
    DirectionalLight light= new DirectionalLight();
    light.setName("light 1");
    lightNode.setLight(light);
    root.addChild(lightNode);

    SceneGraphComponent lightNode2= new SceneGraphComponent();
    lightNode2.setName("lightComp 2");
    Transformation lt2= new Transformation();
    lt.setName("lightTrafo 2");
    // lt2.assignScale(-1);
    lt.setRotation(-Math.PI / 4, 1, 1, 0);
    lightNode2.setTransformation(lt2);
    DirectionalLight light2= new DirectionalLight();
    light2.setName("light 2");
    lightNode2.setLight(light2);
    root.addChild(lightNode2);


    Appearance ap= new Appearance();
    ap.setName("root appearance");
    DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
    RootAppearance ra = ShaderUtility.createRootAppearance(ap);
    ra.setBackgroundColor(Color.blue);
    DefaultLineShader dls = (DefaultLineShader) dgs.getLineShader();
    dls.setTubeDraw(Boolean.FALSE);
    DefaultPolygonShader dps = (DefaultPolygonShader) dgs.getPolygonShader();
    dps.setDiffuseColor(new Color(1f, 0f, 0f));
// ap.setAttribute("lightingEnabled", true);
    
    try {
      TextureUtility.createTexture(ap, "polygonShader", "/Users/gollwas/Sunflower.gif");
    } catch (IOException e) {}
    
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
    String viewer=System.getProperty("de.jreality.scene.Viewer",
      "de.jreality.jogl.Viewer");
    //  "de.jreality.soft.DefaultViewer");
    ToolSystemViewer v = new ToolSystemViewer(createViewer(viewer), ToolSystemConfiguration.loadDefaultDesktopConfiguration());
    v.setPickSystem(new AABBPickSystem());
    return v;
  }
  private static Viewer createViewer(String viewer) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
  {
    return (Viewer)Class.forName(viewer).newInstance();
  }
}
