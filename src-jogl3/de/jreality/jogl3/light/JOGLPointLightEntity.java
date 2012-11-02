package de.jreality.jogl3.light;

import java.awt.Color;

import de.jreality.scene.PointLight;

public class JOGLPointLightEntity extends JOGLLightEntity {
	
	public boolean isShadowmap;
	public String shadowMap;
	
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
			global = l.isGlobal();
			
			dataUpToDate = true;
		}
	}
}
