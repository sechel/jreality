package de.jreality.openhaptics;

import de.jreality.jogl.JOGLPeerComponent;
import de.jreality.shader.CommonAttributes;
import de.varylab.openhaptics.HL;

public class OHPeerComponent extends JOGLPeerComponent{

	int shapeId = -1;
	private float stiffness, damping, staticFriction, dynamicFriction;
	
	public int getShapeId() {
		if(shapeId == -1){
			shapeId = HL.hlGenShapes(1); 
		}
		return shapeId;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		if(shapeId != -1)
			HL.hlDeleteShapes(shapeId, 1);
	}
	
	@Override
	protected void handleAppearanceChanged() {
		super.handleAppearanceChanged();
		stiffness = (float)eAp.getAttribute(CommonAttributes.HAPTIC_SHADER+"."+CommonAttributes.HAPTIC_STIFFNESS, CommonAttributes.HAPTIC_STIFFNESS_DEFAULT);
		damping = (float)eAp.getAttribute(CommonAttributes.HAPTIC_SHADER+"."+CommonAttributes.HAPTIC_DAMPING, CommonAttributes.HAPTIC_DAMPING_DEFAULT);
		staticFriction = (float)eAp.getAttribute(CommonAttributes.HAPTIC_SHADER+"."+CommonAttributes.HAPTIC_STATIC_FRICTION, CommonAttributes.HAPTIC_STATIC_FRICTION_DEFAULT);
		dynamicFriction = (float)eAp.getAttribute(CommonAttributes.HAPTIC_SHADER+"."+CommonAttributes.HAPTIC_DYNAMIC_FRICTION, CommonAttributes.HAPTIC_DYNAMIC_FRICTION_DEFAULT);
	}
	
	void callHlMaterial(){
		HL.hlMaterialf(HL.HL_FRONT_AND_BACK, HL.HL_STIFFNESS, stiffness);
		HL.hlMaterialf(HL.HL_FRONT_AND_BACK, HL.HL_DAMPING, damping);
		HL.hlMaterialf(HL.HL_FRONT_AND_BACK, HL.HL_STATIC_FRICTION, staticFriction);
		HL.hlMaterialf(HL.HL_FRONT_AND_BACK, HL.HL_DYNAMIC_FRICTION, dynamicFriction);
	}

	@Override
	protected OHGeometryShader createGeometryShader() {
		OHGeometryShader dgs = new OHGeometryShader();
		dgs.setFromEffectiveAppearance(eAp, "");
		return dgs;
	}
}
