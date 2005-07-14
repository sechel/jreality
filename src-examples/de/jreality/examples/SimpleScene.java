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
        SceneGraphComponent rootNode=new SceneGraphComponent();
        SceneGraphComponent geometryNode=new SceneGraphComponent();
        SceneGraphComponent cameraNode=new SceneGraphComponent();
        SceneGraphComponent lightNode=new SceneGraphComponent();
        
        rootNode.addChild(geometryNode);
        rootNode.addChild(cameraNode);
        cameraNode.addChild(lightNode);
        
        Camera camera=new Camera();
        Light light=new DirectionalLight();
        
        geometryNode.setGeometry(geom);
        cameraNode.setCamera(camera);
        lightNode.setLight(light);
        
        MatrixBuilder.euclidian().translate(0, 0, dist).assignTo(cameraNode);
        MatrixBuilder.euclidian().rotate(-Math.PI/4, 1, 1, 0).assignTo(lightNode);

        DefaultViewer viewer=new DefaultViewer();
        viewer.setSceneRoot(rootNode);
        viewer.setCameraPath(SceneGraphPath.getFirstPathBetween(rootNode, camera));

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
