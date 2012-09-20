package de.jreality.jogl3.light;

import java.awt.Color;

import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphNode;

public class JOGLDirectionalLightEntity extends JOGLLightEntity {

	protected Color color;
	protected double intensity;
	
	public float[] getColor(){
		return color.getComponents(new float[]{0, 0, 0, 0});
	}
	
	public double getIntensity(){
		return intensity;
	}
	
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
			
			dataUpToDate = true;
		}
	}
}
