package de.jreality.jogl3;

import de.jreality.jogl3.geom.JOGLGeometryEntity;
import de.jreality.jogl3.geom.JOGLGeometryInstance;
import de.jreality.jogl3.light.JOGLLightCollection;
import de.jreality.jogl3.light.JOGLLightEntity;
import de.jreality.jogl3.light.JOGLLightInstance;
import de.jreality.jogl3.shader.PolygonShader;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;

public class JOGLSceneGraphComponentInstance extends SceneTreeNode {

	protected JOGLSceneGraphComponentInstance(SceneGraphComponent cmp) {
		super(cmp);
	}
	
	//have to do this, as we have no means in determining whether an
	//AppearanceEntity could be visited another time during the scene
	//graph traversal
	void setAppearanceEntitiesUpToDate(){
		
		JOGLAppearanceInstance app = (JOGLAppearanceInstance) getAppearanceTreeNode();
		if(app != null){
			((JOGLAppearanceEntity)(app.getEntity())).dataUpToDate = true;
		}
		
		for (SceneTreeNode child : getChildren()) {
			if (!child.isComponent())
				continue;
			JOGLSceneGraphComponentInstance childInstance = (JOGLSceneGraphComponentInstance) child;
			childInstance.setAppearanceEntitiesUpToDate();
		}
	}

	void render(JOGLRenderState parentState) {
		
		
		JOGLAppearanceInstance app = (JOGLAppearanceInstance) getAppearanceTreeNode();
		boolean upToDate = false;
		if(app != null){
			if (((JOGLAppearanceEntity)app.getEntity()).dataUpToDate && parentState.appearanceUpToDate){
				
				upToDate = true;
			}
		}
		if(app == null && parentState.appearanceUpToDate){
			upToDate = true;
		}
		JOGLRenderState state = new JOGLRenderState(parentState, getTrafo());
		state.appearanceUpToDate = upToDate;
		
		
		
		SceneTreeNode gtn =  getGeometryTreeNode();
		
		if (gtn instanceof JOGLGeometryInstance) {
		
			JOGLGeometryInstance geom = (JOGLGeometryInstance) gtn;
			if (geom != null) {
				JOGLGeometryEntity geomEntity = (JOGLGeometryEntity) geom.getEntity();
				geomEntity.updateData(state.getGL());
				if(!state.appearanceUpToDate)
					geom.updateAppearance(this.toPath(), state.getGL());
				//geom.eap = EffectiveAppearance.create(this.toPath());
				//rather update only when appearance has changed.
				//PolygonShader.setFromEffectiveAppearance(EffectiveAppearance.create(this.toPath()), CommonAttributes.POLYGON_SHADER);
				geom.render(state);
			}
		}
		
		for (SceneTreeNode child : getChildren()) {
			if (!child.isComponent())
				continue;
			JOGLSceneGraphComponentInstance childInstance = (JOGLSceneGraphComponentInstance) child;
			SceneGraphComponent sgc = (SceneGraphComponent)child.getNode();
			if(sgc.isVisible())
				childInstance.render(state);
		}
	}
	
	public void collectLights(double[] trafo, JOGLLightCollection collection) {

		double[] newMatrix = new double[16];
		
		double[] matrix = getTrafo();
		if (matrix != null)
			Rn.times(newMatrix, trafo, matrix);
		else
			System.arraycopy(trafo, 0, newMatrix, 0, 16);
		
		JOGLLightInstance light = (JOGLLightInstance)getLightTreeNode();
		
		
		if (light != null) {
			JOGLLightEntity lightEntity = (JOGLLightEntity) light.getEntity();
			lightEntity.updateData();
			//System.out.println(light.getEntity().getNode().getName() + light.getEntity().getNode().getClass());
			//copy transformation to light.trafo
			light.trafo = newMatrix;
			light.addToList(collection);
		}
		
		for (SceneTreeNode child : getChildren()) {
			if (!child.isComponent()) continue;
			JOGLSceneGraphComponentInstance childInstance = (JOGLSceneGraphComponentInstance) child;
			SceneGraphComponent sgc = (SceneGraphComponent)child.getNode();
			if(sgc.isVisible())
				childInstance.collectLights(newMatrix, collection);
		}
	}
	
	private double[] getTrafo() {
		SceneTreeNode tn = getTransformationTreeNode();
		if (tn != null) {
			JOGLTransformationEntity te = (JOGLTransformationEntity) tn.getEntity();
			return te.matrix;
		}
		return null;
	}


	
//	@Override
//	public int addChild(SceneTreeNode child) {
//		if (child.getNode() instanceof Appearance) {
//			propagateAppearanceChanged(null);
//		}
//		return super.addChild(child);
//	}
//
//	@Override
//	public int removeChild(SceneTreeNode child) {
//		if (child.getNode() instanceof Appearance) {
//			propagateAppearanceChanged(null);
//		}
//		return super.removeChild(child);
//	}

//	protected void propagateAppearanceChanged(String key) {
//		EffectiveAppearance eap = EffectiveAppearance.create();
//		propagateAppearanceChanged(eap, key);
//	}
//	
//	protected void propagateAppearanceChanged(EffectiveAppearance parentEap, String key) {
//		EffectiveAppearance eap = parentEap;
//		if (getAppearanceTreeNode() != null) {
//			eap = parentEap.create((Appearance) getAppearanceTreeNode().getNode());
//		}
//		PolygonShader.setFromEffectiveAppearance(parentEap, "polygonShader");
//		for (SceneTreeNode n : getChildren()) {
//			if (!n.isComponent()) continue;
//			JOGLSceneGraphComponentInstance ci = (JOGLSceneGraphComponentInstance) n;
//			ci.propagateAppearanceChanged(eap, key);
//		}
//	}
}
