package de.jreality.jogl3.optimization;

import java.util.WeakHashMap;

import de.jreality.jogl3.GlTexture;
import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.JOGLSceneGraphComponentInstance;
import de.jreality.jogl3.JOGLSceneGraphComponentInstance.RenderableObject;
import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLFaceSetInstance;
import de.jreality.jogl3.geom.JOGLGeometryInstance;

public class RenderableUnitCollection{
	WeakHashMap<GlTexture, RenderableUnit> units = new WeakHashMap<GlTexture, RenderableUnit>();
	
	public void addRenderableObject(RenderableObject o){
		JOGLGeometryInstance geom = o.geom;
		JOGLRenderState state = o.state;
		if(geom instanceof JOGLFaceSetInstance){
			JOGLFaceSetInstance f = (JOGLFaceSetInstance)geom;
			JOGLFaceSetEntity fse = (JOGLFaceSetEntity) f.getEntity();
			if(fse.getAllVBOs()[0].getLength()<1000){
				if(units.get(f.faceTexture) == null)
					units.put(f.faceTexture, new RenderableUnit(f.faceTexture));
				units.get(f.faceTexture).register(f);
			}
		}
	}
	
	
}
