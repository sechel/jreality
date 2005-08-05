/*
 * $Id: HelloSphere.java,v 1.3 2005/08/05 10:22:28 pinkall Exp $
 * 
 * Some sort of "Hello World" for jReality. The idea is to put something meaningful
 * on the screen, in as few lines as possible.
 */

package de.jreality.examples;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.soft.DefaultViewer;


public class HelloSphere {
    public static void main(String[] args) {
        SceneGraphComponent rootNode=new SceneGraphComponent();
        SceneGraphComponent geometryNode=new SceneGraphComponent();
        SceneGraphComponent cameraNode=new SceneGraphComponent();
        SceneGraphComponent lightNode=new SceneGraphComponent();
        
        rootNode.addChild(geometryNode);
        rootNode.addChild(cameraNode);
        cameraNode.addChild(lightNode);
        
        Geometry sphere=new Sphere();
        Camera camera=new Camera();
        Light light=new DirectionalLight();
        
        geometryNode.setGeometry(sphere);
        cameraNode.setCamera(camera);
        lightNode.setLight(light);
        
        MatrixBuilder.euclidian().translate(0, 0, 4).assignTo(cameraNode);
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
}