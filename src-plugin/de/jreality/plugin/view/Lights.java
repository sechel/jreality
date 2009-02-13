package de.jreality.plugin.view;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class Lights extends Plugin {

	private static final double DEFAULT_SUN_LIGHT_INTENSITY = .75;
	private static final double DEFAULT_SKY_LIGHT_INTENSITY = .25;

	private SceneGraphComponent lights;
	private DirectionalLight sunLight;
	private DirectionalLight skyLight;
	private View view;
	private SceneGraphComponent sceneRoot;

	public Lights() {
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
	}

	public void install(View v) {

		this.view = v;
		sceneRoot = view.getSceneRoot();
		view.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				if (e.getSource() == view) {
					if (view.getSceneRoot() != sceneRoot) {
						sceneRoot.removeChild(lights);
						sceneRoot = view.getSceneRoot();
						sceneRoot.addChild(lights);
					}
				}
			}
		});

		sceneRoot.addChild(lights);
	}

	public SceneGraphComponent getLightComponent() {
		return lights;
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

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Lights";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("sonne.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		View sceneViewPlugin = c.getPlugin(View.class);
		install(sceneViewPlugin);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		view.getSceneRoot().removeChild(lights);
	}

}