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
import de.jreality.soft.DefaultViewer;
import de.jreality.util.P3;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author brinkman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ToolTestScene {

	final Viewer viewer = new de.jreality.jogl.Viewer();
	//final Viewer viewer = new DefaultViewer();
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
      l0.getTransformation().setMatrix(
              P3.makeRotationMatrix(null, zaxis, other));
      l0.setLight(dl);
      lights.addChild(l0);

      return lights;
  }

  void createScene() {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("test root");
		SceneGraphComponent camNode = new SceneGraphComponent();
		camNode.setName("test camera");
        camNode.setTransformation(new Transformation());
        camNode.getTransformation().setTranslation(0, 0, 3);
        camNode.addChild(makeLights());
//        dummy.addChild(camNode);

        Camera view = new Camera();
        view.setFar(20);
        view.setNear(0.1f);
        camNode.setCamera(view);
		
		SceneGraphComponent scene = new SceneGraphComponent();
    SceneGraphComponent cath = new SceneGraphComponent();
    SceneGraphComponent sphere = new SceneGraphComponent();
    sphere.addTool(new DraggingTool());
		sphere.setGeometry(new Sphere());
    cath.addTool(new TestTool());
    cath.addTool(new DraggingTool() {
		public void enforceConstraints(double[] matrix) {
			matrix[3]=0;
			matrix[7]=0;
			matrix[11]=0;
		}});
		cath.setGeometry(new CatenoidHelicoid(40));
		root.addChild(scene);
		scene.addChild(cath);
    scene.addChild(sphere);
		root.addChild(camNode);
    scene.addTool(new TranslateTool());
		SceneGraphPath camPath = new SceneGraphPath();
		camPath.push(root);
		camPath.push(camNode);
		camPath.push(view);
		camNode.addTool(new EgoShooterTool());
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
        ToolSystem ts = new ToolSystem(viewer);
    try {
        PickSystem ps = (PickSystem) Class.forName("de.jreality.jme.intersection.proxy.JmePickSystem").newInstance();
        ts.setPickSystem(ps);
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
