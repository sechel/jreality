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

public class InstanceCollection {
	
	GL3 gl;
	
	public static final int START_SIZE = 1000;
	
	//the number of dead bytes, needed to decide, when to defragment
	public int dead_count = 0;
	//1000 is the starting size of the VBOs
	public int available = START_SIZE;
	public int current_vbo_size = START_SIZE;
	private int numAliveInstances = 0;
	
	public LinkedList<Instance> instances = new LinkedList<Instance>();
	public LinkedList<Instance> newInstances = new LinkedList<Instance>();
	//This is needed, because we might delete the deadInstances in defragmentation.
	//if not so, we need to manually null these.
	public LinkedList<Instance> deadInstances = new LinkedList<Instance>();
	
	private WeakHashMap<String, GLVBO> gpuData = new WeakHashMap<String, GLVBO>();
	
	public void kill(Instance i){
		if(i.isAlive()){
			i.kill();
			dead_count += i.length;
			deadInstances.add(i);
			numAliveInstances--;
		}
	}
	
	private void nullInstance(Instance i){
		this.nullFromTo(i.posInVBOs, i.length+i.posInVBOs);
	}
	private void nullRest(){
		nullFromTo(current_vbo_size-available, current_vbo_size);
	}
	/**
	 * 
	 * @param start in Bytes
	 * @param end in Bytes
	 */
	private void nullFromTo(int start, int end){
		//null vertex_coordinates.w
		GLVBOFloat vertexData = (GLVBOFloat) gpuData.get("vertex_coordinates");
		float[] subdata = new float[(end-start)/4];
		//here we have to set some coordinate (e.g. x-coord) to 1, so that w=0 will have the effect of sending
		//the vertex to infinity
		for(int j = 0; j < (end-start)/4; j+=4){
			subdata[j*4] = 1;
		}
		vertexData.updateSubData(gl, subdata, start, end-start);
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
			System.err.println("largevbo has unknown type (InstanceCollection.java)");
		}
	}
	
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
	 * @param position in bytes
	 */
	private void pushInstanceToGPU(Instance i){
		//add to all vbos
		JOGLFaceSetEntity fse = (JOGLFaceSetEntity)i.fsi.getEntity();
		GLVBO[] vbos = fse.getAllVBOs();
		for(GLVBO vbo : vbos){
			//create new GLVBO for this name, if not present
			if(gpuData.get(vbo.getName()) == null){
				putNewVBO(vbo.getName(), vbo.getType(), vbo.getElementSize());
			}
			//fill in vbo data
			GLVBO largevbo = gpuData.get(vbo.getName());
			if(largevbo.getType() == GL3.GL_FLOAT){
				GLVBOFloat f = (GLVBOFloat)largevbo;
				f.updateSubData(gl, ((GLVBOFloat)vbo).getData(), i.posInVBOs, i.length);
			}else if(largevbo.getType() == GL3.GL_INT){
				GLVBOInt f = (GLVBOInt)largevbo;
				//vbo.getLength()*4 must equal i.length
				f.updateSubData(gl, ((GLVBOInt)vbo).getData(), i.posInVBOs, i.length);
			}else{
				System.err.println("largevbo has unknown type (InstanceCollection.java 3)");
			}
		}
	}
	/**
	 * defragment in RAM, but leave GPU data unchanged until update() is called.
	 * ignore dead, they will be removed by RenderableUnit.update()
	 */
	public void defragment() {
		
		
		
		// TODO don't forget to set instance.upToDate = true for all recreated instances;
		// TODO remove all dead from instances
		// TODO only register the changes, do not push to GPU yet, update() has to do this.
	}
	
	/**
	 * this method can easily be changed to facilitate fine tuning of the optimization
	 * @return
	 */
	public boolean isFragmented() {
		if(dead_count >= RenderableUnit.FRAGMENT_THRESHOLD)
			return true;
		else
			return false;
	}

	public int getNumberFreeInstances() {
		return RenderableUnit.MAX_NUMBER_OBJ_IN_COLLECTION-numAliveInstances;
	}

	/**
	 * only adds an Instance to this InstanceCollection. To push changes to GPU, use update()
	 * @param fsi
	 */
	public void add(JOGLFaceSetInstance fsi) {
		// TODO only add, not push to GPU
		Instance i = new Instance(this, fsi, 0);
		available -= i.length;
	}
	/**
	 * very important method! Pushes all changes to the GPU
	 */
	public void update(){
		//TODO if defragmentable, defragment!
		
		if(available < 0){
			//if not defragmentable
			if(-available > dead_count){
				int needed = current_vbo_size - available - dead_count;
				float neededPow = needed/1000f;
				int pow = (int)Math.ceil(Math.log(neededPow)/Math.log(2));
				changeVBOSize(pow);
			}
			//defragment
			
			dead_count = 0;
			for(Instance i : newInstances){
				instances.add(i);
			}
			//push ALL instances to GPU
			int pos = 0;
			for(Instance i : instances){
				i.posInVBOs = pos;
				pos += i.length;
				pushInstanceToGPU(i);
			}
			//null the rest of the VBO
			nullRest();
		}else{
			//push ONLY the new instances to GPU
			int pos = current_vbo_size-available;
			for(Instance i : newInstances){
				i.posInVBOs = pos;
				pos += i.length;
				pushInstanceToGPU(i);
			}
			//and null the dead ones
			for(Instance i : deadInstances){
				nullInstance(i);
			}
		}
		for(Instance i : deadInstances){
			instances.remove(i);
		}
		deadInstances = new LinkedList<Instance>();
		newInstances = new LinkedList<Instance>();
		// TODO use the method private add() and private changeVBOSize()
	}
	
}
