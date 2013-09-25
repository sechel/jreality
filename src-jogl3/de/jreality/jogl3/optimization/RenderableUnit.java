package de.jreality.jogl3.optimization;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

import de.jreality.jogl3.GlTexture;
import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLFaceSetInstance;
import de.jreality.jogl3.glsl.GLShader;

public class RenderableUnit {
	
	private static final int FRAGMENT_THRESHOLD = 100000;
	
	private GlTexture texture;
	private GLShader shader;
	
	private LinkedList<InstanceCollection> instanceCollections = new LinkedList<InstanceCollection>();
	
	//contains the current Instances for all FaceSetInstances
	private WeakHashMap<JOGLFaceSetInstance, Instance> instances = new WeakHashMap<JOGLFaceSetInstance, Instance>();
	
	//a simple set of all the new FaceSetInstances to be registered
	private HashSet<JOGLFaceSetInstance> registered = new HashSet<JOGLFaceSetInstance>();
	
	
	
	/**
	 * create a new entity containing all small geometries (below 1000 verts) with one equal texture/shader pair
	 * no local lights allowed
	 * @param t
	 */
	public RenderableUnit(GlTexture t, GLShader s){
		texture = t;
		shader = s;
		//TODO create starting vbo
	}
	/**
	 * register a {@link JOGLFaceSetInstance} for sending to GPU
	 * @param f
	 */
	public void register(JOGLFaceSetInstance f){
		registered.add(f);
	}
	/**
	 * writes all registered data (see {@link #register(JOGLFaceSetInstance)}) to the GPU
	 */
	public void update(){
		
		//check which fsi are missing in the registered ones
		//if there is an Instance in instances that is neither dead nor belongs to a FaceSetInstance in registered, kill it
		Set<JOGLFaceSetInstance> set = instances.keySet();
		for(JOGLFaceSetInstance fsi : set){
			if(!registered.contains(fsi)){
				Instance ins = instances.get(fsi);
				//if already dead, stays dead
				ins.kill();
				//TODO have to null all w-coordinates on GPU
				
				//increase dead_count
				ins.collection.dead_count += ins.length;
			}
		}
		
		
		
		//TODO CARRY ON WORKING HERE-------------------------------------------****************--------------------
		//check, which ones are new and how many they are.
		int new_count = 0;
		set = registered;
		for(JOGLFaceSetInstance fsi : set){
			if(instances.get(fsi) == null){
				//found new fsi, count its length towards new_count
				JOGLFaceSetEntity fse = (JOGLFaceSetEntity)fsi.getEntity();
				new_count += fse.getVBO("vertex_coordinates").getLength();
			}
		}
		
		if(new_count < available){
			//TODO append new data to vbo in GPU
			//TODO process the changed face set instances
		}else if(new_count < dead_count + available){
			//TODO recreate with equal size
		}else{
			//TODO recreate with enough size (START_SIZE*2^n)
		}
		
		//clean up the registration hash map
		registered = new HashSet<JOGLFaceSetInstance>();
	}
}
