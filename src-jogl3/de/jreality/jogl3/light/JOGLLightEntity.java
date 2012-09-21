package de.jreality.jogl3.light;

import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.event.LightEvent;
import de.jreality.scene.event.LightListener;
import de.jreality.scene.proxy.tree.SceneGraphNodeEntity;

public abstract class JOGLLightEntity extends SceneGraphNodeEntity implements LightListener {

	protected JOGLLightEntity(SceneGraphNode node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	boolean dataUpToDate = false;
	
	public void lightChanged(LightEvent ev) {
		System.out.println("JOGLPointSetEntity.geometryChanged()");
		dataUpToDate = false;
	}
	
	public abstract void updateData();

}
