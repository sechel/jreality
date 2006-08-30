package de.jreality.swing.test;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import de.jreality.examples.PaintComponent;
import de.jreality.geometry.Primitives;
import de.jreality.io.JrScene;
import de.jreality.io.JrSceneFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.swing.jrwindows.JRWindowManager;
import de.jreality.tools.RotateTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.PickUtility;

public class TestJRWindows {
  public static void main(String[] args) {
    
    JrScene scene = JrSceneFactory.getDefaultDesktopScene();
    SceneGraphComponent root = scene.getSceneRoot();
    SceneGraphPath cameraPath = scene.getPath("cameraPath");
    SceneGraphPath emptyPickPath = scene.getPath("emptyPickPath");
    SceneGraphPath avatarPath = scene.getPath("avatarPath");
    
    SceneGraphComponent sgc=new SceneGraphComponent();
    IndexedFaceSet torus = Primitives.torus(2,1,100,100);
    PickUtility.assignFaceAABBTree(torus, 5);
    //sgc.setGeometry(torus);
    sgc.setGeometry(Primitives.cube());
    MatrixBuilder.euclidean().rotate(Math.PI/4,1,0,0).assignTo(sgc);
    sgc.addTool(new RotateTool());
    root.addChild(sgc);

    //System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
    
//    ViewerApp va = new ViewerApp(Primitives.icosahedron());
//    va.update();
//
//    
//    JRWindowManager wm=new JRWindowManager(va.getViewer().getAvatarPath());   
//        
//    va.display();

//  ShowPropertiesTool spt=new ShowPropertiesTool(false);
//  sgc.addTool(spt);
//  JFrame frame1=wm.createFrame();
//  frame1.getContentPane().add(spt.getLog());
 
    JRWindowManager wm=new JRWindowManager(avatarPath.getLastComponent());
    
    //wm.setBorderRadius(0.05);
    
  JFrame frame2=wm.createFrame();
  frame2.getContentPane().add(new JTextArea("testarea",10,20));

  JFrame frame3=wm.createFrame();
  frame3.getContentPane().add("North",new JButton("test"));
  frame3.getContentPane().add(new PaintComponent());
  frame3.pack();
  
  JFrame frame4=wm.createFrame();
  frame4.getContentPane().add("North",new JCheckBox());
  frame4.getContentPane().add(new JCheckBox());
  frame4.getContentPane().add("South",new JCheckBox());    
  
  JFrame frame5=wm.createFrame();
  frame5.getContentPane().add("North",new JCheckBox());
  frame5.getContentPane().add("South",new JCheckBox());    
  
  JFrame frame6=wm.createFrame();
  frame6.getContentPane().add("North",new JCheckBox());  
  
  JFrame frame7=wm.createFrame();
  frame7.getContentPane().add(new JLabel("labellabellabel"));   
    
  wm.pack();
  
//  ViewerApp va = new ViewerApp(root, cameraPath, emptyPickPath, avatarPath);
//  va.setShowMenu(true);
//  va.setAttachNavigator(true);
//  va.setAttachBeanShell(true);
//  va.update();
//  va.display();
  
  
  
  ViewerApp.display(root);
  }
}
