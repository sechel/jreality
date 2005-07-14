/*
 * Created on May 12, 2004
 *
 */
package de.jreality.worlds;
import java.awt.Color;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Torus;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Pn;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author Charles Gunn
 *
 */
public class ImplodedTori extends AbstractJOGLLoadableScene {


	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = SceneGraphUtilities.createFullSceneGraphComponent("theWorld");
		root.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.RED);
		double r1 = 1.0, r2 = 0.2, r3 = 0.5;
		Torus torus= new Torus(r1, r2, 40, 60);
		torus.setName("torus");
		SceneGraphComponent mainOne = SceneGraphUtilities.createFullSceneGraphComponent("SGC");
		mainOne.getTransformation().setRotation(Math.PI/2, 1,0,0);
		GeometryUtility.calculateAndSetNormals(torus);
		mainOne.setGeometry(torus);
		mainOne.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		root.addChild(mainOne);
		for (int i = 0; i< 6; ++i)	{
			double angle = (Math.PI*2.0*i)/6.0;
			torus= new Torus(r3, r3-r2, 20, 30);
			torus.setName("torus"+i);
			GeometryUtility.calculateAndSetNormals(torus);
			SceneGraphComponent globeNode = SceneGraphUtilities.createFullSceneGraphComponent("SGC"+i);
			globeNode.getTransformation().setRotation(angle, 0,0,1);
			globeNode.getTransformation().setTranslation(Math.cos(angle), Math.sin(angle),0);
			if (i!=0) globeNode.setGeometry(IndexedFaceSetUtility.implode(torus, -.9 + .35 * i));
			else globeNode.setGeometry(IndexedFaceSetUtility.truncate(torus));
			root.addChild(globeNode);
		}
		//viewer.getCameraPath().getLastComponent().getTransformation().setTranslation(0.0d, 0.0d, 4.0d);
		return root;
	}
	
	public int getSignature() {
		// TODO Auto-generated method stub
		return Pn.EUCLIDEAN;
	}
	public void setConfiguration(ConfigurationAttributes config) {
		// TODO Auto-generated method stub

	}
	public boolean addBackPlane() {
		return false;
	}
	public boolean isEncompass() {
		return true;
	}

}

