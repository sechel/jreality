/*
 * Created on May 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.worlds;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Torus;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Pn;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestSphereDrawing extends AbstractLoadableScene {

	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = SceneGraphUtilities.createFullSceneGraphComponent("theWorld");
		Appearance ap1 = root.getAppearance();
		ap1.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .006);
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, true);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLUE);
		for (int i = 0; i< 1; ++i)	{
			Torus torus= new Torus(0.5, 0.3, 20,30);
			torus.setName("torus"+i);
			GeometryUtility.calculateAndSetNormals(torus);
			SceneGraphComponent globeNode = new SceneGraphComponent();
			globeNode.setName("comp"+i);
			Transformation gt= new Transformation();
			//gt.setTranslation(-5.0 + 2.0* i, 0, 0.0);
			globeNode.setTransformation(gt);
			//if (i!=0) globeNode.setGeometry(GeometryUtility.implode(torus, -.9 + .4 * i));
			//else globeNode.setGeometry(GeometryUtility.truncate(torus));
			globeNode.setGeometry(torus);
			root.addChild(globeNode);
		}
		//CameraUtility.getCameraNode(viewer).getTransformation().setTranslation(0.0d, 0.0d, 4.0d);
		return root;
	}
 
	public boolean addBackPlane()	{return false;}
	public void setConfiguration(ConfigurationAttributes config) {
	}

	public int getSignature() {
		return Pn.EUCLIDEAN;
	}
	public boolean isEncompass() {
		return true;
	}
}

