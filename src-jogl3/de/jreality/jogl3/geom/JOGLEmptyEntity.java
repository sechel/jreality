package de.jreality.jogl3.geom;

import javax.media.opengl.GL3;

import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.event.GeometryEvent;

public class JOGLEmptyEntity extends JOGLGeometryEntity {

	public JOGLEmptyEntity(SceneGraphNode node) {
		super(node);
	}

	@Override
	public void updateData(GL3 gl) {
	}

	public void geometryChanged(GeometryEvent ev) {
		// TODO Auto-generated method stub
		
	}

}
