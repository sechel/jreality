/*
 * Created on Mar 22, 2005
 *
 */
package de.jreality.scene.tool;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;

import javax.swing.JFrame;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.scene.*;
import de.jreality.scene.Camera;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.pick.PickSystem;
import de.jreality.scene.pick.SoftPickSystem;
import de.jreality.util.Matrix;
import de.jreality.util.P3;
import de.jreality.util.SceneGraphUtilities;
import de.jreality.worlds.JOGLSkyBox;

/**
 * @author brinkman
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ToolTestScene {

  final ToolSystemViewer viewer = new ToolSystemViewer(new de.jreality.jogl.Viewer());

  //final Viewer viewer = new de.jreality.soft.DefaultViewer();
  JFrame frame = new JFrame("viewer");

  public static SceneGraphComponent makeLights() {
    SceneGraphComponent lights = new SceneGraphComponent();
    lights.setName("lights");
    //SpotLight pl = new SpotLight();
    de.jreality.scene.PointLight pl = new de.jreality.scene.PointLight();
    //DirectionalLight pl = new DirectionalLight();
    pl.setFalloff(1.0, 0.0, 0.0);
    pl.setColor(new Color(170, 170, 120));
    //pl.setConeAngle(Math.PI);

    pl.setIntensity(0.6);
    SceneGraphComponent l0 = SceneGraphUtilities
        .createFullSceneGraphComponent("light0");
    l0.setLight(pl);
    lights.addChild(l0);
    DirectionalLight dl = new DirectionalLight();
    dl.setColor(new Color(200, 150, 200));
    dl.setIntensity(0.6);
    l0 = SceneGraphUtilities.createFullSceneGraphComponent("light1");
    double[] zaxis = { 0, 0, 1 };
    double[] other = { 1, 1, 1 };
    l0.getTransformation().setMatrix(P3.makeRotationMatrix(null, zaxis, other));
    l0.setLight(dl);
    lights.addChild(l0);

    return lights;
  }

  void createScene() {
    SceneGraphComponent root = new SceneGraphComponent();
    root.setName("test root");
    SceneGraphComponent avatarNode = new SceneGraphComponent();
    avatarNode.setName("avatar motion");
    avatarNode.setTransformation(new Transformation());
    avatarNode.getTransformation().setTranslation(0, 0, 3);
    avatarNode.addChild(makeLights());
    //        dummy.addChild(camNode);

    SceneGraphComponent camNode = new SceneGraphComponent();
    camNode.setName("avatar look orientation");
    camNode.setTransformation(new Transformation());

    avatarNode.addChild(camNode);
    
    Camera camera = new Camera();
    camera.setFar(20);
    camera.setNear(0.1f);
    camNode.setCamera(camera);

    SceneGraphComponent scene = new SceneGraphComponent();
    SceneGraphComponent cath1 = new SceneGraphComponent();
    SceneGraphComponent cath2 = new SceneGraphComponent();
    SceneGraphComponent cath3 = new SceneGraphComponent();
    SceneGraphComponent sphere = new SceneGraphComponent();
    sphere.setGeometry(new Sphere());
//    cath.addTool(new TestTool());
//    cath.addTool(new DraggingTool() {
//      // only allow translation along y axis
//      public void enforceConstraints(Matrix matrix) {
//        matrix.setEntry(0, 3, 0.);
//        matrix.setEntry(2, 3, 0.);
//      }
//    });
    cath1.setGeometry(new CatenoidHelicoid(40));
    cath2.setGeometry(new CatenoidHelicoid(20));
    cath3.setGeometry(new CatenoidHelicoid(10));
    root.addChild(scene);
   
    
    //scene.addChild(cath1);
    //scene.addChild(cath2);
    //scene.addChild(cath3);
    //scene.addChild(sphere);
    
    scene.addChild(new JOGLSkyBox().makeWorld());
    root.addChild(avatarNode);
    
    scene.addTool(new TestTool());
    scene.addTool(new DraggingTool());
//    scene.addTool(new TranslateTool());
    RotateTool rotateTool = new RotateTool();
    rotateTool.setMoveChildren(true);
	scene.addTool(rotateTool);

    root.addTool(new EncompassTool());
    
    SceneGraphPath camPath = new SceneGraphPath();
    camPath.push(root);
    camPath.push(avatarNode);
    camPath.push(camNode);
    camPath.push(camera);
    avatarNode.addTool(new ShipNavigationTool());
    camNode.addTool(new HeadTransformationTool());
    frame.setVisible(true);
    frame.setSize(640, 480);
    frame.getContentPane().add(viewer.getViewingComponent());
    viewer.setSceneRoot(root);
    viewer.setCameraPath(camPath);
    frame.validate();

    System.out.println(viewer.getViewingComponent().getSize());
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent arg0) {
        System.exit(0);
      }
    });
    try {
      //PickSystem ps = (PickSystem) Class.forName("de.jreality.jme.intersection.proxy.JmePickSystem").newInstance();
      PickSystem ps = new SoftPickSystem();
      viewer.setPickSystem(ps);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void render() {
    while (true) {
      viewer.render();
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    ToolTestScene tts = new ToolTestScene();
    tts.createScene();
    tts.render();
  }
}
