package de.jreality.jogl3.optimization;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

import javax.media.opengl.GL3;

import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLFaceSetInstance;
import de.jreality.jogl3.shader.GLVBO;
import de.jreality.jogl3.shader.GLVBOFloat;
import de.jreality.jogl3.shader.GLVBOInt;

/**
 * A collection of up to MAX_TEXTURE_DIMENSION FaceSetInstances
 * @author benjamin
 *
 */
public class InstanceCollection {
	
	GL3 gl;
	
	/**
	 * initial size in floats
	 */
	private static final int START_SIZE = 1000;
	
	//the number of dead bytes, needed to decide, when to defragment
	/**
	 * number of dead floats
	 */
	private int dead_count = 0;
	//1000 is the starting size of the VBOs
	/**
	 * available size in floats
	 */
	private int availableFloats = START_SIZE;
	/**
	 * current size in floats
	 */
	private int current_vbo_size = START_SIZE;
	private int numAliveInstances = 0;
	
	private LinkedList<Instance> instances = new LinkedList<Instance>();
	private LinkedList<Instance> newInstances = new LinkedList<Instance>();
	//This is needed, because we might delete the deadInstances in defragmentation.
	//if not so, we need to manually null these.
	private LinkedList<Instance> dyingInstances = new LinkedList<Instance>();
	
	private WeakHashMap<String, GLVBO> gpuData = new WeakHashMap<String, GLVBO>();
	
	
	private void nullInstance(Instance i){
		this.nullFromTo(i.posInVBOs, i.length+i.posInVBOs);
	}
	private void nullRest(){
		nullFromTo(current_vbo_size-availableFloats, current_vbo_size);
	}
	/**
	 * 
	 * @param start in Floats
	 * @param end in Floats
	 */
	private void nullFromTo(int start, int end){
		//null vertex_coordinates.w
		GLVBOFloat vertexData = (GLVBOFloat) gpuData.get("vertex_coordinates");
		float[] subdata = new float[(end-start)];
		//here we have to set some coordinate (e.g. x-coord) to 1, so that w=0 will have the effect of sending
		//the vertex to infinity
		for(int j = 0; j < (end-start); j+=4){
			subdata[j] = 1;
		}
		vertexData.updateSubData(gl, subdata, 4*start, 4*(end-start));
	}
	
	/**
	 * put a new VBO into gpuData
	 * @param name
	 * @param type
	 * @param elementSize
	 */
	private void putNewVBO(String name, int type, int elementSize){
		if(type == GL3.GL_FLOAT){
			gpuData.put(name, new GLVBOFloat(gl, new float[current_vbo_size], name, elementSize));
		}else if(type == GL3.GL_INT){
			gpuData.put(name, new GLVBOInt(gl, new int[current_vbo_size], name));
		}else{
			System.err.println("unknown type of elements in VBO (InstanceCollection.java)");
		}
	}
	/**
	 * changes the size of the gpuData VBOs. All data in them is being lost by this process.
	 * @param powerofTwo decides the new size by the formula 1000*2^(powerofTwo)
	 */
	private void changeVBOSize(int powerofTwo){
		current_vbo_size = START_SIZE*(int)Math.round(Math.pow(2, powerofTwo));
		Set<String> keys = gpuData.keySet();
		String[] keyArray = keys.toArray(new String[0]);
		for(String s : keyArray){
			GLVBO vbo = gpuData.get(s);
			putNewVBO(vbo.getName(), vbo.getType(), vbo.getElementSize());
			
		}
	}
	
	/**
	 * This method sends the data to VBOs in GPU.
	 * The size of the VBOs in GPU are not changed by this method.
	 * @param i the instance being added
	 */
	private void pushInstanceToGPU(Instance i){
		//add to all vbos
		JOGLFaceSetEntity fse = (JOGLFaceSetEntity)i.fsi.getEntity();
		GLVBO[] vbos = fse.getAllVBOs();
		for(GLVBO vbo : vbos){
			//create new GLVBO for this name, if not present
			if(gpuData.get(vbo.getName()) == null){
				putNewVBO(vbo.getName(), vbo.getType(), vbo.getElementSize());
				//needs not be nulled, because it's not vertex_coordinates
			}
			//fill in vbo data
			GLVBO largevbo = gpuData.get(vbo.getName());
			if(largevbo.getType() == GL3.GL_FLOAT){
				GLVBOFloat f = (GLVBOFloat)largevbo;
				f.updateSubData(gl, ((GLVBOFloat)vbo).getData(), 4*i.posInVBOs, 4*i.length);
			}else if(largevbo.getType() == GL3.GL_INT){
				GLVBOInt f = (GLVBOInt)largevbo;
				//vbo.getLength()*4 must equal i.length
				f.updateSubData(gl, ((GLVBOInt)vbo).getData(), 4*i.posInVBOs, 4*i.length);
			}else{
				System.err.println("largevbo has unknown type (InstanceCollection.java 3)");
			}
		}
	}
	
	/**
	 * this method can easily be changed to facilitate fine tuning of the optimization
	 * @return
	 */
	private boolean isFragmented() {
		if(dead_count >= RenderableUnit.FRAGMENT_THRESHOLD)
			return true;
		else
			return false;
	}
	//_______________________****************PUBLIC METHODS****************__________________________
	/**
	 * get the number of instances, you can still add to this collection
	 * @return
	 */
	public int getNumberFreeInstances() {
		return RenderableUnit.MAX_NUMBER_OBJ_IN_COLLECTION-numAliveInstances;
	}
	/**
	 * only registers a new Instance to this InstanceCollection. To push changes to GPU, use update()
	 * @param fsi
	 */
	public void registerNewInstance(JOGLFaceSetInstance fsi){
		newInstances.add(new Instance(this, fsi, 0));
		numAliveInstances++;
	}
	/**
	 * register Instance for deletion
	 * @param i
	 */
	public void kill(Instance i){
		if(i.isAlive()){
			i.kill();
			dyingInstances.add(i);
			numAliveInstances--;
		}
	}
	/**
	 * defragment in RAM, but leave GPU data unchanged until update() is called.
	 * ignore dead, they will be removed by RenderableUnit.update()
	 */
	private void defragment() {
		
		
		
		// TODO don't forget to set instance.upToDate = true for all recreated instances;
		// TODO remove all dead from instances
		// TODO only register the changes, do not push to GPU yet, update() has to do this.
	}
	
	private void writeAllInstancesNewToVBO(){
		//write rest to gpu
		dead_count = 0;
		//delete all dyingInstances from instances
		for(Instance i : dyingInstances){
			instances.remove(i);
		}
		//move all newInstances to instances
		for(Instance i : newInstances){
			instances.add(i);
		}
		
		//push ALL instances to GPU
		int pos = 0;
		for(Instance i : instances){
			availableFloats -= i.length;
			i.posInVBOs = pos;
			pos += i.length;
			pushInstanceToGPU(i);
		}
		//null the rest of the VBO
		nullRest();
	}
	/**
	 * very important method! Pushes all changes to the GPU
	 */
	public void update(){
		
		//update dead_count
		for(Instance i : dyingInstances){
			dead_count += i.length;
		}
		//update availableFloats
		for(Instance i : newInstances){
			availableFloats -= i.length;
		}
		boolean mustResize = false;
		int numFloatsNeeded = current_vbo_size - availableFloats - dead_count;
		if(numFloatsNeeded > current_vbo_size || (numFloatsNeeded <= current_vbo_size/2 && current_vbo_size > START_SIZE))
			mustResize = true;
		if(mustResize){
			//resize
			float neededPow = numFloatsNeeded/1000f;
			int pow = (int)Math.ceil(Math.log(neededPow)/Math.log(2));
			changeVBOSize(pow);
			//write everything to vbo
			availableFloats = current_vbo_size;
			writeAllInstancesNewToVBO();
			
		}else{
			//do not resize
			if(availableFloats >= 0){
				//push ONLY the new instances to GPU
				int pos = current_vbo_size-availableFloats;
				for(Instance i : newInstances){
					i.posInVBOs = pos;
					pos += i.length;
					instances.add(i);
					pushInstanceToGPU(i);
				}
				//and null the dead ones
				for(Instance i : dyingInstances){
					nullInstance(i);
				}
				//and done!
			}else{
				//don't resize, but rewrite everything to vbo
				availableFloats = current_vbo_size;
				writeAllInstancesNewToVBO();
			}
		}
		dyingInstances = new LinkedList<Instance>();
		newInstances = new LinkedList<Instance>();
	}
	
}
