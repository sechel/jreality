package de.jreality.jogl3.optimization;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

import de.jreality.jogl3.GlTexture;
import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLFaceSetInstance;
import de.jreality.jogl3.glsl.GLShader;

public class RenderableUnit {
	
	public static final int FRAGMENT_THRESHOLD = 100000;
	public static final int MAX_NUMBER_OBJ_IN_COLLECTION = 1000;
	
	private GlTexture texture;
	private GLShader shader;
	
	
	//an Instance collection contains upto MAX_NUMBER_OBJ_IN_COLLECTION small objects.
	//This limitation is due to the maximum texture size.
	private LinkedList<InstanceCollection> instanceCollections = new LinkedList<InstanceCollection>();
	
	//contains the current Instances for all FaceSetInstances, with dead!
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
	private void killAndRemove(JOGLFaceSetInstance f){
		Instance ins = instances.get(f);
		//we know it's alive, because instances only contains alive elements
		ins.collection.kill(ins);
		instances.remove(f);
	}
	/**
	 * writes all registered data (see {@link #register(JOGLFaceSetInstance)}) to the GPU
	 */
	public void update(){
		
		//check which fsi are missing in the registered ones
		//if there is an Instance in instances not belonging to a FaceSetInstance in registered, kill it
		Set<JOGLFaceSetInstance> set = instances.keySet();
		for(JOGLFaceSetInstance fsi : set){
			if(!registered.contains(fsi)){
				Instance ins = instances.get(fsi);
				ins.collection.kill(ins);
			}
		}
		
		//order into sets newSet, lengthSet and posASet for FaceSetInstances that are completely new,
		//have changed their length or changed either positions or attributes respectively.
		HashSet<JOGLFaceSetInstance> newSet = new HashSet<JOGLFaceSetInstance>();
		HashSet<JOGLFaceSetInstance> lengthSet = new HashSet<JOGLFaceSetInstance>();
		HashSet<JOGLFaceSetInstance> posASet = new HashSet<JOGLFaceSetInstance>();
		for(JOGLFaceSetInstance f : registered){
			//check if new
			if(instances.get(f) == null){
				//in fact it's new
				newSet.add(f);
			}else if(f.oChangedLength()){
				//is old, but changed its length
				lengthSet.add(f);
				f.resetOChangedLength();
			}else if(f.oChangedPositionsOrAttributes()){
				//changed only positions or attributes
				posASet.add(f);
				instances.get(f).upToDate = false;
				f.resetOChangedPositionsOrAttributes();
			}else{
				//nothing changed, needs not be touched if not neccessary
				//do nothing here!
			}
		}
		//kill all Instances from lengthSet and move them to newSet
		for(JOGLFaceSetInstance f : lengthSet){
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
						JOGLFaceSetInstance fsi = newSet.iterator().next();
						currentCollection.registerNewInstance(fsi);
						newSet.remove(fsi);
					}else{
						fillingUp = false;
					}
				}
			}
			//TODO currentCollectin.update() ?
		}
		
		
		
		//update all the elements from posASet, that have not been moved in a defragmentation or merge operation
		
		
		
		//clean up the registration hash map
		registered = new HashSet<JOGLFaceSetInstance>();
	}
}
