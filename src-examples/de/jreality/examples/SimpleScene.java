/*
 * Created on Jul 14, 2005
 */
package de.jreality.examples;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.soft.DefaultViewer;
import de.jreality.util.MatrixBuilder;

public class SimpleScene {

	public static void display(Geometry geom, double dist) {
        SceneGraphComponent geometryNode=new SceneGraphComponent();
        geometryNode.setGeometry(geom);
        display(geometryNode, dist);
	}
        
	public static void display(SceneGraphComponent geom, double dist) {
        SceneGraphComponent rootNode=new SceneGraphComponent();
        SceneGraphComponent cameraNode=new SceneGraphComponent();
        SceneGraphComponent lightNode=new SceneGraphComponent();
        
        rootNode.addChild(geom);
        rootNode.addChild(cameraNode);
        cameraNode.addChild(lightNode);
        
        Camera camera=new Camera();
        Light light=new DirectionalLight();
        
        cameraNode.setCamera(camera);
        lightNode.setLight(light);
        
        MatrixBuilder.euclidian().translate(0, 0, dist).assignTo(cameraNode);
        MatrixBuilder.euclidian().rotate(-Math.PI/4, 1, 1, 0).assignTo(lightNode);

        DefaultViewer viewer=new DefaultViewer();
        viewer.setSceneRoot(rootNode);
        
        SceneGraphPath cameraPath=new SceneGraphPath();
        cameraPath.push(rootNode);
        cameraPath.push(cameraNode);
        cameraPath.push(camera);
        viewer.setCameraPath(cameraPath);

        Frame frame=new Frame();
        frame.add(viewer);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setSize(780, 580);
        frame.validate();
        frame.setVisible(true);
        
        viewer.render();
	}
	
	public static void main(String[] args) {
		SimpleScene.display(new Sphere(), 4);
	}

}
