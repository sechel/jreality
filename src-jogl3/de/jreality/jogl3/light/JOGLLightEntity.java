package de.jreality.jogl3.light;

import java.awt.Color;

import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.event.LightEvent;
import de.jreality.scene.event.LightListener;
import de.jreality.scene.proxy.tree.SceneGraphNodeEntity;

public abstract class JOGLLightEntity extends SceneGraphNodeEntity implements LightListener {

	protected JOGLLightEntity(SceneGraphNode node) {
		super(node);
		// TODO Auto-generated constructor stub
	}
	protected Color color;
	boolean dataUpToDate = false;
	boolean global = true;
	protected double intensity;
	
	public double getIntensity(){
		return intensity;
	}
	
	public float[] getColor(){
		return color.getComponents(new float[]{0, 0, 0, 0});
	}
	
	public void lightChanged(LightEvent ev) {
		System.out.println("JOGLPointSetEntity.geometryChanged()");
		dataUpToDate = false;
	}
	
	public abstract void updateData();
	
	public boolean isGlobal() {
		// TODO Auto-generated method stub
		return global;
	}
}
