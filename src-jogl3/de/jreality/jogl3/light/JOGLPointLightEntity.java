package de.jreality.jogl3.light;

import java.awt.Color;

import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphNode;

public class JOGLPointLightEntity extends JOGLLightEntity {
	
	protected Color color;
	protected double intensity;
	protected boolean isShadowmap;
	protected String shadowMap;
	
	public JOGLPointLightEntity(PointLight node) {
		super(node);
	}

	@Override
	public void updateData() {
		if (!dataUpToDate) {
			PointLight l = (PointLight) getNode();
			color = l.getColor();
			intensity = l.getIntensity();
			shadowMap = l.getShadowMap();
			isShadowmap = l.isUseShadowMap();
			
			dataUpToDate = true;
		}
	}

}
