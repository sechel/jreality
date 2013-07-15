package de.jreality.jogl3.light;

import de.jreality.scene.DirectionalLight;

public class JOGLDirectionalLightEntity extends JOGLLightEntity {
	
	public JOGLDirectionalLightEntity(DirectionalLight node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void updateData() {
		// TODO Auto-generated method stub
		if (!dataUpToDate) {
			
			DirectionalLight l = (DirectionalLight) getNode();
			color = l.getColor();
			intensity = l.getIntensity();
			global = l.isGlobal();
			
			dataUpToDate = true;
		}
	}
}
