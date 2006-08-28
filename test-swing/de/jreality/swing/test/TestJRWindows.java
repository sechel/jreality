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
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.swing.jrwindows.JRWindowManager;
import de.jreality.tools.PointerDisplayTool;
import de.jreality.ui.viewerapp.ViewerApp;

public class TestJRWindows {
	public static void main(String[] args) {
    
    JrScene scene = JrSceneFactory.getDefaultPortalScene();
    SceneGraphComponent root = scene.getSceneRoot();
    SceneGraphPath cameraPath = scene.getPath("cameraPath");
    SceneGraphPath emptyPickPath = scene.getPath("emptyPickPath");
    SceneGraphPath avatarPath = scene.getPath("avatarPath");
    
    avatarPath.getLastComponent().addTool(new PointerDisplayTool());
    
    SceneGraphComponent geoSgc=new SceneGraphComponent();
    geoSgc.setGeometry(Primitives.icosahedron());
    emptyPickPath.getLastComponent().addChild(geoSgc);

    JRWindowManager wm=new JRWindowManager(avatarPath);
    
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
  
    ViewerApp va = new ViewerApp(root, cameraPath, emptyPickPath, avatarPath);
    va.setAttachNavigator(true);
    va.setShowMenu(true);
    va.update();
    va.display();
	}
}
