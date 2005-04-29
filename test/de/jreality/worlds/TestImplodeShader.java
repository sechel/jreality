/*
 * Created on Feb 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.worlds;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.Torus;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
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
public class TestImplodeShader extends AbstractJOGLLoadableScene {

	public SceneGraphComponent makeWorld() {
	
		SceneGraphComponent root = SceneGraphUtilities.createFullSceneGraphComponent("testImplodeShader");
		IndexedFaceSet torus = new Torus(1.0, 0.4, 30,22);
		GeometryUtility.calculateAndSetNormals(torus);
		root.setGeometry(torus); //Primitives.icosahedron());
		Appearance ap1 = root.getAppearance();
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER, "implode");
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"implodeFactor", 0.75);
//		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER, "default");
//		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER, "default");
		ap1.setAttribute(CommonAttributes.FACE_DRAW,true);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW,false);
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW,false);
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
