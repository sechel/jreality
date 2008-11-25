package de.jreality.ui.sceneview;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;

public class LightManager {
	
	private static final double DEFAULT_SUN_LIGHT_INTENSITY = 1;
	private static final double DEFAULT_SKY_LIGHT_INTENSITY = .2;
	
	private SceneGraphComponent lights;
	private DirectionalLight sunLight;
	private DirectionalLight skyLight;
	private final SceneView sceneView;
	private SceneGraphComponent sceneRoot;
	
	public LightManager(final SceneView sceneView) {
		
		this.sceneView = sceneView;
		sceneRoot = sceneView.getSceneRoot();
		sceneView.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() == sceneView) {
					if (sceneView.getSceneRoot() != sceneRoot) {
						sceneRoot.removeChild(lights);
						sceneRoot = sceneView.getSceneRoot();
						sceneRoot.addChild(lights);
					}
				}
			}
		});
		
		lights = new SceneGraphComponent("lights");

		SceneGraphComponent sun = new SceneGraphComponent("sun");
		sunLight = new DirectionalLight("sun light");
		sunLight.setIntensity(DEFAULT_SUN_LIGHT_INTENSITY);
		sun.setLight(sunLight);
		MatrixBuilder.euclidean().rotateFromTo(new double[] { 0, 0, 1 },
				new double[] { 0, 1, 1 }).assignTo(sun);
		lights.addChild(sun);

		SceneGraphComponent sky = new SceneGraphComponent("sky");
		skyLight = new DirectionalLight();
		skyLight.setIntensity(DEFAULT_SKY_LIGHT_INTENSITY);
		skyLight.setAmbientFake(true);
		skyLight.setName("sky light");
		sky.setLight(skyLight);
		MatrixBuilder.euclidean().rotateFromTo(new double[] { 0, 0, 1 },
				new double[] { 0, 1, 0 }).assignTo(sky);
		lights.addChild(sky);
		
		sceneRoot.addChild(lights);
	}
	
	public SceneGraphComponent getLightComponent() {
		return lights;
	}
	
	public void unInstall() {
		sceneView.getSceneRoot().removeChild(lights);
	}
	
	public double getSkyLightIntensity() {
		return skyLight.getIntensity();
	}

	public void setSkyLightIntensity(double x) {
		skyLight.setIntensity(x);
	}

	public void setLightIntensity(double intensity) {
		sunLight.setIntensity(intensity);
	}

	public double getLightIntensity() {
		return sunLight.getIntensity();
	}
}
