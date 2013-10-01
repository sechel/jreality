package de.jreality.jogl3.optimization;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import javax.media.opengl.GL3;

import de.jreality.jogl3.GlTexture;
import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.JOGLSceneGraphComponentInstance.RenderableObject;
import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLFaceSetInstance;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlReflectionMap;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniform;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniformInt;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniformMat4;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.glsl.GLShader.ShaderVar;
import de.jreality.jogl3.shader.GLVBO;
import de.jreality.jogl3.shader.ShaderVarHash;
import de.jreality.math.Rn;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.Texture2D;

public class RenderableUnit {
	
	private GlTexture texture;
	private GLShader shader;
	private boolean shaderInited = false;
	private GlReflectionMap reflMap;
	GlUniformInt combineMode;
	GlUniformMat4 texMatrix;
	
	//an Instance collection contains upto MAX_NUMBER_OBJ_IN_COLLECTION small objects.
	//This limitation is due to the maximum texture size.
	private LinkedList<InstanceCollection> instanceCollections = new LinkedList<InstanceCollection>();
	
	//contains the current Instances for all FaceSetInstances, with dead!
	private WeakHashMap<RenderableObject, Instance> instances = new WeakHashMap<RenderableObject, Instance>();
	
	//a simple set of all the new FaceSetInstances to be registered
	private HashSet<RenderableObject> registered = new HashSet<RenderableObject>();
	
	/**
	 * create a new entity containing all small geometries (below 1000 verts) with one equal texture/shader pair
	 * no local lights allowed
	 * @param t
	 */
	public RenderableUnit(GlTexture t, GLShader s, GlReflectionMap reflMap){
		texture = t;
		shader = s;
		shaderInited = false;
		this.reflMap = reflMap;
	}
	/**
	 * register a {@link JOGLFaceSetInstance} for sending to GPU
	 * @param f
	 */
	public void register(RenderableObject o){
		registered.add(o);
		state = o.state;
		if(!shaderInited)
			shader.init(state.getGL());
	}
	JOGLRenderState state;
	
	private void killAndRemove(RenderableObject o){
		Instance ins = instances.get(o);
		//we know it's alive, because instances only contains alive elements
		ins.collection.kill(ins);
		instances.remove(o);
	}
	
	/**
	 * writes all registered data (see {@link #register(JOGLFaceSetInstance)}) to the GPU
	 */
	public void update(){
		
		//check which fsi are missing in the registered ones
		//if there is an Instance in instances not belonging to a FaceSetInstance in registered, kill it
		Set<RenderableObject> set = instances.keySet();
		for(RenderableObject o : set){
			if(!registered.contains(o)){
				Instance ins = instances.get(o);
				ins.collection.kill(ins);
			}
		}
		
		//order into sets newSet, lengthSet and posASet for FaceSetInstances that are completely new,
		//have changed their length or changed either positions or attributes respectively.
		HashSet<RenderableObject> newSet = new HashSet<RenderableObject>();
		HashSet<RenderableObject> lengthSet = new HashSet<RenderableObject>();
		HashSet<RenderableObject> posASet = new HashSet<RenderableObject>();
		for(RenderableObject f : registered){
			instances.get(f).upToDate = true;
			//check if new
			if(instances.get(f) == null){
				//in fact it's new
				newSet.add(f);
			}else if(f.geom.oChangedLength()){
				//is old, but changed its length
				lengthSet.add(f);
			}else if(f.geom.oChangedPositionsOrAttributes()){
				//changed only positions or attributes
				posASet.add(f);
				instances.get(f).upToDate = false;
			}else{
				//nothing changed, needs not be touched if not neccessary
				//do nothing here!
			}
			f.geom.resetOChangedLength();
			f.geom.resetOChangedPositionsOrAttributes();
		}
		//kill all Instances from lengthSet and move them to newSet
		for(RenderableObject f : lengthSet){
			//kill
			killAndRemove(f);
			//and add to newSet
			newSet.add(f);
		}
		//fill up InstanceCollections with all the elements from newSet
		boolean fillingUp = true;
		int insCollNumber = -1;
		while(fillingUp){
			insCollNumber++;
			InstanceCollection currentCollection = instanceCollections.get(insCollNumber);
//			//defragment
//			if(currentCollection.isFragmented()){
//				//remove all current collection's dead from instances
//				currentCollection.defragment();
//			}
			//fill up with elements from newSet
			int free = currentCollection.getNumberFreeInstances();
			if(free > 0){
				for(int i = 0; i < free; i++){
					if(newSet.iterator().hasNext()){
						RenderableObject fsi = newSet.iterator().next();
						currentCollection.registerNewInstance((JOGLFaceSetInstance)fsi.geom, fsi.state);
						newSet.remove(fsi);
					}else{
						fillingUp = false;
					}
				}
			}
			currentCollection.update();
		}
		
		//TODO merge the rest...
		//...not so important right now
		
		//clean up the registration hash map
		registered = new HashSet<RenderableObject>();
	}
	
	public void render(){
		if(texture.getTexture2D() == null){
			System.out.println("tex is null!!!");
			return;
		}
		GL3 gl = state.getGL();
		
		float[] projection = Rn.convertDoubleToFloatArray(state.getProjectionMatrix());
		float[] inverseCamMatrix = Rn.convertDoubleToFloatArray(state.inverseCamMatrix);
		shader.useShader(gl);
		
		ShaderVarHash.bindUniform(shader, "_combineMode", texture.getTexture2D().getApplyMode(), gl);
		
		//matrices
		ShaderVarHash.bindUniformMatrix(shader, "textureMatrix", Rn.convertDoubleToFloatArray(texture.getTexture2D().getTextureMatrix().getArray()), gl);
		ShaderVarHash.bindUniformMatrix(shader, "projection", projection, gl);
		ShaderVarHash.bindUniformMatrix(shader, "_inverseCamRotation", inverseCamMatrix, gl);
		
		//global lights in a texture
    	ShaderVarHash.bindUniform(shader, "sys_globalLights", 0, gl);
    	ShaderVarHash.bindUniform(shader, "sys_numGlobalDirLights", state.getLightHelper().getNumGlobalDirLights(), gl);
    	ShaderVarHash.bindUniform(shader, "sys_numGlobalPointLights", state.getLightHelper().getNumGlobalPointLights(), gl);
    	ShaderVarHash.bindUniform(shader, "sys_numGlobalSpotLights", state.getLightHelper().getNumGlobalSpotLights(), gl);
    	
		ShaderVarHash.bindUniform(shader, "sys_numLocalDirLights", 0, gl);
		ShaderVarHash.bindUniform(shader, "sys_numLocalPointLights", 0, gl);
		ShaderVarHash.bindUniform(shader, "sys_numLocalSpotLights", 0, gl);
		
		//bind shader uniforms not necessary

		texture.bind(shader, gl);
		reflMap.bind(shader, gl);

    	//new way to do lights
		state.getLightHelper().bindGlobalLightTexture(gl);
    	
		for(InstanceCollection insColl : instanceCollections){
			insColl.render(gl);
		}
		
		shader.dontUseShader(gl);
	}
	
}
