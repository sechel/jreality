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

public class Lights extends Plugin  {

	protected SceneGraphComponent lights;
	private View view;
	private SceneGraphComponent sceneRoot;
	private SceneGraphComponent lightParent = null;

	private static final double DEFAULT_SUN_LIGHT_INTENSITY = .75;
	private static final double DEFAULT_SKY_LIGHT_INTENSITY = .25;

	private DirectionalLight sunLight;
	private DirectionalLight skyLight;
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

	public void install(View v) {

		this.view = v;
		sceneRoot = view.getSceneRoot();
		if (lightParent == null) lightParent = sceneRoot;
		view.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				if (e.getSource() == view) {
					if (sceneRoot != lightParent) return;
					if (view.getSceneRoot() != sceneRoot) {
						sceneRoot.removeChild(lights);
						sceneRoot = view.getSceneRoot();
						sceneRoot.addChild(lights);
					}
				}
			}
		});

		lightParent.addChild(getLightComponent());
		System.err.println("install adding lights to "+lightParent.getName());
	}

	public SceneGraphComponent getLightComponent() {
		return lights;
	}

	public void setLights(SceneGraphComponent lights) {
		if (this.lights != null && this.lightParent.isDirectAncestor(this.lights))
			this.lightParent.removeChild(this.lights);
		this.lights = lights;
		this.lightParent.addChild(this.lights);
		System.err.println("stlights adding lights to "+this.lightParent.getName());
	}

	public void setLightParent(SceneGraphComponent lightParent) {
		if (lightParent == this.lightParent) return;
		if (this.lightParent != null && 
				lights != null && 
				this.lightParent.isDirectAncestor(lights)) 
			this.lightParent.removeChild(lights);
		this.lightParent = lightParent;
		if (lights != null) {
			this.lightParent.addChild(lights);
			System.err.println("setlp adding lights to "+lightParent.getName());
		}
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "AbstractLights";
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
		lightParent.removeChild(lights);
	}

}