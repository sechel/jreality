package de.jreality.jogl3.optimization;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

import javax.media.opengl.GL3;

import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.shader.GLVBO;
import de.jreality.jogl3.shader.GLVBOFloat;
import de.jreality.jogl3.shader.GLVBOInt;

public class InstanceCollection {
	
	GL3 gl;

	public static final int START_SIZE = 1000;
	
	public int dead_count = 0;
	//1000 is the starting size of the VBOs
	public int available = START_SIZE;
	public int current_vbo_size = START_SIZE;
	
	public LinkedList<Instance> instances = new LinkedList<Instance>();
	
	private WeakHashMap<String, GLVBO> gpuData = new WeakHashMap<String, GLVBO>();
	
	public void kill(Instance i){
		if(i.isAlive()){
			i.kill();
			//TODO null all vbos
			GLVBOFloat vertexData = (GLVBOFloat) gpuData.get("vertex_coordinates");
			float[] subdata = new float[i.length/4];
			//here we have to set the some coordinate (e.g. x-coord) to 1, so that w=0 will have the effect of sending
			//the vertex to infinity
			for(int j = 0; j < i.length/4; j+=4){
				subdata[j*4] = 1;
			}
			vertexData.updateSubData(gl, subdata, i.posInVBOs, i.length);
			dead_count += i.length;
		}
	}
	
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
		current_vbo_size = (int)Math.round(Math.pow(2, powerofTwo));
		Set<String> keys = gpuData.keySet();
		for(String s : keys){
			GLVBO vbo = gpuData.get(s);
			putNewVBO(vbo.getName(), vbo.getType(), vbo.getElementSize());
			
		}
	}
	
	/**
	 * Add an Instance to this InstanceCollection at the specified position.
	 * This method saves the Instance in a HashMap in RAM and sends the data to VBOs in GPU.
	 * The size of the VBOs in GPU are not changed by this method.
	 * @param i the instance being added
	 * @param position in bytes
	 */
	private void add(Instance i, int position){
		instances.add(i);
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
				f.updateSubData(gl, ((GLVBOFloat)vbo).getData(), position, i.length);
			}else if(largevbo.getType() == GL3.GL_INT){
				GLVBOInt f = (GLVBOInt)largevbo;
				//vbo.getLength()*4 must equal i.length
				f.updateSubData(gl, ((GLVBOInt)vbo).getData(), position, i.length);
			}else{
				System.err.println("largevbo has unknown type (InstanceCollection.java 3)");
			}
		}
	}
	
}
