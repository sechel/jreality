/*
 * Created on Mar 22, 2005
 *
 */
package de.jreality.scene.tool;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.P3;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.pick.PickSystem;
import de.jreality.scene.pick.bounding.AABBTree;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.SceneGraphUtility;

/**
 * @author brinkman
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ToolTestScene {

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

  SceneGraphComponent createScene() {
    SceneGraphComponent scene = new SceneGraphComponent();
   
    /************ CREATE SCENE ***********/
    //scene.addChild(new JOGLSkyBox().makeWorld());
    
    IndexedFaceSet ifs = Primitives.torus(2, .5, 10, 10);
	IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(ifs);
    
    GeometryUtility.calculateAndSetFaceNormals(ifs);
    //AABBTree obbTree = AABBTree.constructEdgeAABB(ifs, 0.1);
    //ifs.setGeometryAttributes(Attribute.attributeForName("AABBTreeEdge"), obbTree);
    //AABBTree.constructAndRegister(ifs, null, 5);
    SceneGraphComponent comp = new SceneGraphComponent();
    comp.setGeometry(ifs);
    //comp.addChild(obbTree.display());
    
    scene.addChild(comp);
    return scene;
  }

  public static void main(String[] args) {
    ToolTestScene tts = new ToolTestScene();
	ViewerApp.display(tts.createScene());
  }
}
