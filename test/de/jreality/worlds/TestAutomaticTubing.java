/*
 * Created on Feb 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.worlds;
import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.DiscreteSpaceCurve;
import de.jreality.jogl.shader.DefaultVertexShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Pn;
import de.jreality.util.SceneGraphUtilities;


/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestAutomaticTubing extends AbstractLoadableScene {

	static double[][] form = {{1,1,1}, {1,-1,1},{1,-1,-1},{1,1,-1},{-1,1,-1},{-1,-1,-1},{-1,-1,1},{-1,1,1}};

	public SceneGraphComponent makeWorld() {
		IndexedLineSet ils = GeometryUtility.createCurveFromPoints(form, true);
		ils = DiscreteSpaceCurve.discreteTorusKnot(1.0, .4, 5, 3, 50);
		SceneGraphComponent root = SceneGraphUtilities.createFullSceneGraphComponent("testAutomaticTubing");
		root.setGeometry(ils);
		Appearance ap1 = root.getAppearance();
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLUE);
//		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .03);
		ap1.setAttribute(CommonAttributes.FACE_DRAW,false);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW,true);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW,true);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS,.03);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE, 3.0);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
		return root;
	}
		 
	public void setConfiguration(ConfigurationAttributes config) {
	}

		public int getSignature() {
			return Pn.EUCLIDEAN;
		}

	 

	public boolean addBackPlane() {
		return true;
	}
	public boolean isEncompass() {
		return true;
	}
}
