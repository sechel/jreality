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

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestSphereDrawing extends AbstractLoadableScene {

	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setTransformation(new Transformation());
		root.setName("theWorld");
		Appearance ap1 = new Appearance();
		root.setAppearance(ap1);
		root.getAppearance().setAttribute("normalScale",0.05);
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
			ap1 = new Appearance();
			ap1.setAttribute(CommonAttributes.FACE_DRAW, false);
			ap1.setAttribute(CommonAttributes.VERTEX_DRAW, true);
			ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, true);
			ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLUE);
			//ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, .033);
			globeNode.setAppearance(ap1);
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
}

