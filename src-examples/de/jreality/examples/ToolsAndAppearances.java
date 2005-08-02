/*
 * $Id: ToolsAndAppearances.java,v 1.2 2005/08/02 15:07:33 pinkall Exp $
 * As simple example using appearances and tools.
 */
package de.jreality.examples;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

import de.jreality.geometry.Primitives;
import de.jreality.jogl.Viewer;
import de.jreality.soft.SoftPickSystem;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.pick.PickSystem;
import de.jreality.scene.tool.RotateTool;
import de.jreality.scene.tool.ToolSystemViewer;
import de.jreality.util.math.MatrixBuilder;

public class ToolsAndAppearances {
  public static void main(String[] args) {
    SceneGraphComponent rootNode = new SceneGraphComponent();
    SceneGraphComponent cameraNode = new SceneGraphComponent();
    SceneGraphComponent geometryNode = new SceneGraphComponent();
    SceneGraphComponent lightNode = new SceneGraphComponent();
    
    rootNode.addChild(geometryNode);
    rootNode.addChild(cameraNode);
    cameraNode.addChild(lightNode);
    
    Light dl=new DirectionalLight();
    lightNode.setLight(dl);
    
    Camera camera = new Camera();
    cameraNode.setCamera(camera);

    IndexedFaceSet ifs = Primitives.icosahedron(); 
    geometryNode.setGeometry(ifs);
    
    RotateTool rotateTool = new RotateTool();
    geometryNode.addTool(rotateTool);

    MatrixBuilder.euclidian().translate(0, 0, 3).assignTo(cameraNode);

	Appearance rootApp= new Appearance();
    rootApp.setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0f, .1f, .1f));
    rootApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1f, 0f, 0f));
    rootNode.setAppearance(rootApp);
        
    SceneGraphPath camPath = new SceneGraphPath();
    camPath.push(rootNode);
    camPath.push(cameraNode);
    camPath.push(camera);
    
    ToolSystemViewer viewer = new ToolSystemViewer(new Viewer());
    viewer.setSceneRoot(rootNode);
    viewer.setCameraPath(camPath);
    PickSystem ps = new SoftPickSystem();
    viewer.setPickSystem(ps);
    
    JFrame frame = new JFrame();
    frame.setVisible(true);
    frame.setSize(640, 480);
    frame.getContentPane().add(viewer.getViewingComponent());
    frame.validate();
    System.out.println(viewer.getViewingComponent().getSize());
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent arg0) {
        System.exit(0);
      }
    });
    
    while (true) {
      viewer.render();
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
