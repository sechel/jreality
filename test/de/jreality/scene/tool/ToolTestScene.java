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
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.pick.*;
import de.jreality.soft.SoftPickSystem;
import de.jreality.scene.pick.bounding.AABBTree;
import de.jreality.util.SceneGraphUtility;

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
    SceneGraphComponent l0 = SceneGraphUtility
        .createFullSceneGraphComponent("light0");
    l0.setLight(pl);
    lights.addChild(l0);
    DirectionalLight dl = new DirectionalLight();
    dl.setColor(new Color(200, 150, 200));
    dl.setIntensity(0.6);
    l0 = SceneGraphUtility.createFullSceneGraphComponent("light1");
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
    root.addChild(scene);
   
    /************ CREATE SCENE ***********/
    //scene.addChild(new JOGLSkyBox().makeWorld());
    
    IndexedFaceSet ifs = //Primitives.icosahedron(); 
      new CatenoidHelicoid(7);
    
    GeometryUtility.calculateAndSetFaceNormals(ifs);
    AABBTree obbTree = AABBTree.construct(ifs, 1);
    ifs.setGeometryAttributes(Attribute.attributeForName("AABBTree"), obbTree);
    SceneGraphComponent comp = new SceneGraphComponent();
    comp.setGeometry(ifs);
    comp.addChild(obbTree.display());
    
    scene.addChild(comp);
    
    /********** SCENE DONE ***********/
    
    root.addChild(avatarNode);
    
    scene.addTool(new DraggingTool());
    scene.addTool(new PickShowTool("PrimaryAction"));
    RotateTool rotateTool = new RotateTool();
    rotateTool.setMoveChildren(true);
    scene.addTool(rotateTool);

    SceneGraphPath camPath = new SceneGraphPath();
    camPath.push(root);
    camPath.push(avatarNode);
    camPath.push(camNode);
    camPath.push(camera);
    ShipNavigationTool shipNavigationTool = new ShipNavigationTool();
    shipNavigationTool.setGravity(0);
    shipNavigationTool.setJumpSpeed(0);
    avatarNode.addTool(shipNavigationTool);
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
      PickSystem ps = new AABBPickSystem();
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
