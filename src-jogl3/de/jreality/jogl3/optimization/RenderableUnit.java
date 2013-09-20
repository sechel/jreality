package de.jreality.jogl3.optimization;

import de.jreality.jogl3.GlTexture;
import de.jreality.jogl3.geom.JOGLFaceSetInstance;

public class RenderableUnit {
	GlTexture texture;
	
	public RenderableUnit(GlTexture t){
		texture = t;
	}
	
	public void register(JOGLFaceSetInstance f){
		
	}
}
