package de.jreality.openhaptics;

import de.jreality.jogl.JOGLPeerComponent;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.jogl.shader.DefaultGeometryShader;
import de.varylab.openhaptics.HL;

public class OHGeometryShader extends DefaultGeometryShader {

	@Override
	public void postRender(JOGLRenderingState jrs) {
		super.postRender(jrs);
		if(OHRenderingState.isHapticRendering(jrs)){
			HL.hlEndShape();
			OHRenderer.checkHLError();
		}
	}
	
	@Override
	public void preRender(JOGLRenderingState jrs, JOGLPeerComponent jpc) {
		if(OHRenderingState.isHapticRendering(jrs)){
			HL.hlBeginShape(HL.HL_SHAPE_FEEDBACK_BUFFER, ((OHPeerComponent) jpc).getShapeId());
			OHRenderer.checkHLError();
			
			((OHPeerComponent) jpc).callHlMaterial();
			OHRenderer.checkHLError();
		}
		super.preRender(jrs, jpc);
	}
}
