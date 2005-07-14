/*
 * $Id: HelloFancySphere.java,v 1.1 2005/07/14 13:29:07 brinkman Exp $
 * 
 * A fancier "Hello World" for jReality. The idea is to show how to use appearances
 * as well as the JOGL viewer, and eventually picking and tools.
 * 
 * If this doesn't work because Java can't find libjogl.so, add
 *	-Djava.library.path=/usr/local/lib (or wherever your libjogl.so lives)
 * as an argument to your java virtual machine (use the Run.. menu item to
 * do this if you're using eclipse).
 */

package de.jreality.examples;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import de.jreality.jogl.Viewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.util.MatrixBuilder;


public class HelloFancySphere {
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

    	Appearance rootApp= new Appearance();
        rootApp.setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0f, .1f, .1f));
        rootApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1f, 0f, 0f));
        
    	Appearance geometryApp= new Appearance();
        geometryApp.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
        geometryApp.setAttribute(CommonAttributes.TRANSPARENCY, .5);
    	
        rootNode.setAppearance(rootApp);
        geometryNode.setAppearance(geometryApp);
        
        Viewer viewer=new Viewer();
        viewer.setSceneRoot(rootNode);
        viewer.setCameraPath(SceneGraphPath.getFirstPathBetween(rootNode, camera));

        Frame frame=new Frame();
        frame.add(viewer.getViewingComponent());
        
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