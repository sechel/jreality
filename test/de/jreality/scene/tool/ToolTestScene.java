/*
 * Created on Mar 22, 2005
 *
 */
package de.jreality.scene.tool;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;

import javax.swing.JFrame;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.jme.test.ColliderScene;
import de.jreality.scene.Camera;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.SceneGraphPath;
import de.jreality.soft.DefaultViewer;

/**
 * @author brinkman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ToolTestScene {

	//Viewer viewer = new de.jreality.jogl.Viewer();
	final Viewer viewer = new DefaultViewer();
	JFrame frame = new JFrame("viewer");
	
	void createScene() {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("test root");
		SceneGraphComponent camNode = new SceneGraphComponent();
		camNode.setName("test camera");
        camNode.setTransformation(new Transformation());
        camNode.getTransformation().setTranslation(0, 0, 3);
        camNode.addChild(ColliderScene.makeLights());
//        dummy.addChild(camNode);

        Camera view = new Camera();
        view.setFar(20);
        view.setNear(0.1f);
        camNode.setCamera(view);
		
		SceneGraphComponent scene = new SceneGraphComponent();
		SceneGraphComponent sphere = new SceneGraphComponent();
		sphere.setGeometry(new Sphere());
		//sphere.setGeometry(new CatenoidHelicoid(10));
		root.addChild(scene);
		scene.addChild(sphere);
		root.addChild(camNode);
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
	}
	
	public void render() {
		while (true) {
			viewer.render();
			try {
				Thread.sleep(10);
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
