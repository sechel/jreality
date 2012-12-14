package light;

import java.awt.Color;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.scene.Terrain;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;

public class LightTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SceneGraphComponent cmp = new SceneGraphComponent();
		
		
		//a directional light
		DirectionalLight l1 = new DirectionalLight();
		l1.setGlobal(true);
		l1.setColor(Color.BLUE);
		l1.setIntensity(0.5);
		SceneGraphComponent light1 = new SceneGraphComponent();
		light1.setLight(l1);
		MatrixBuilder.euclidean().rotate(4, new double[]{1, 0, 0, 0}).assignTo(light1);
		cmp.addChild(light1);
		
		//a point light with attenuation
		PointLight l2 = new PointLight();
		l2.setGlobal(true);
		l2.setColor(Color.RED);
		l2.setIntensity(1.0);
		l2.setFalloff(0, 0, 1);
		SceneGraphComponent light2 = new SceneGraphComponent();
		light2.setLight(l2);
		MatrixBuilder.euclidean().translate(new double[]{2, 1, 2, 1}).assignTo(light2);
		cmp.addChild(light2);
		
		//a point light with attenuation
		SpotLight l3 = new SpotLight();
		l3.setGlobal(true);
		l3.setColor(Color.GREEN);
		l3.setIntensity(0.5);
		l3.setFalloff(1, 0, 0);
		l3.setDistribution(0);
		
		SceneGraphComponent light3 = new SceneGraphComponent();
		light3.setLight(l3);
		MatrixBuilder.euclidean().translate(new double[]{-2, 1, 2, 1}).rotate(1.5, new double[]{1, 0, 0}).assignTo(light3);
		cmp.addChild(light3);
		
		SceneGraphComponent torus = new SceneGraphComponent();
		torus.setGeometry(Primitives.torus(2, 1, 10, 10));
		cmp.addChild(torus);
		
		JRViewer vr = new JRViewer();
		vr.addVRSupport();
		vr.addBasicUI();
		vr.addContentUI();
		vr.setContent(cmp);
		vr.startup();
	}

}
