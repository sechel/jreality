package de.jreality.jogl3.optimization;

import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import javax.media.opengl.GL3;

import com.itextpdf.text.LargeElement;

import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLFaceSetInstance;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniform;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniformInt;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniformMat4;
import de.jreality.jogl3.glsl.GLShader.ShaderVar;
import de.jreality.jogl3.shader.GLVBO;
import de.jreality.jogl3.shader.GLVBOFloat;
import de.jreality.jogl3.shader.GLVBOInt;
import de.jreality.jogl3.shader.ShaderVarHash;
import de.jreality.math.Rn;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;

/**
 * A collection of up to MAX_TEXTURE_DIMENSION FaceSetInstances
 * @author benjamin
 *
 */
public class InstanceCollection {
	
	private int textureID;
	private int texUnit = 1;
	private float data[];
	
	private void initUniformTexture(GL3 gl){
		textureID = generateNewTexID(gl);
		
		data = new float[shader.getNumFloatsNecessary()*MAX_NUMBER_OBJ_IN_COLLECTION];
		
		updateUniformsTexture(gl);
	}
	
	private int offset;
	
	private void DataWriteHelper(LinkedList<String[]> Uniforms, WeakHashMap<String, GlUniform> uniforms, int j, JOGLRenderState state){
		for(String[] s : Uniforms){
			
			GlUniform u = uniforms.get(s[1]);
			if(u == null && !s[1].equals("modelview"))
				continue;
			
			if(s[0].equals("mat4")){
				float[] f;
				if(s[1].equals("modelview")){
					f = new float[16];
					Rn.transposeD2F(f, state.getModelViewMatrix());
				}else
					f = (float[])u.value;
				for(int l = 0; l < 16; l++)
					data[offset+j*shader.getNumFloatsNecessary()+l] = f[l];
				offset += 16;
			}else if(s[0].equals("vec4")){
				float[] f = (float[])u.value;
				for(int l = 0; l < 4; l++)
					data[offset+j*shader.getNumFloatsNecessary()+l] = f[l];
				offset += 4;
			}else if(s[0].equals("float")){
				float f = (Float)u.value;
				data[offset+j*shader.getNumFloatsNecessary()] = f;
				offset += 1;
			}else if(s[0].equals("int")){
				int f = (Integer)u.value;
				data[offset+j*shader.getNumFloatsNecessary()] = Float.intBitsToFloat(f);
				offset += 1;
			}else if(s[0].equals("vec2")){
				float[] f = (float[])u.value;
				for(int l = 0; l < 2; l++)
					data[offset+j*shader.getNumFloatsNecessary()+l] = f[l];
				offset += 2;
			}else if(s[0].equals("vec3")){
				float[] f = (float[])u.value;
				for(int l = 0; l < 3; l++)
					data[offset+j*shader.getNumFloatsNecessary()+l] = f[l];
				offset += 3;
			}
		}
	}
	
	private void updateUniformsTexture(GL3 gl){
//		System.out.println("update uniforms texture");
		//update data
		LinkedList<String[]> vertUniforms = shader.getVertUniforms();
		LinkedList<String[]> fragUniforms = shader.getFragUniforms();
		
		for(JOGLFaceSetInstance fsi : instances.keySet()){
			Instance i = instances.get(fsi);
			//retrieve appearance data from fsi uniforms
//			JOGLFaceSetInstance fsi = i.fsi;
			WeakHashMap<String, GlUniform> uniforms = fsi.faceSetUniformsHash;
			//the width offset
			offset = 0;
			JOGLRenderState state = i.state;
			DataWriteHelper(vertUniforms, uniforms, i.id, state);
			if (offset%4 != 0)
				offset = 4*(offset/4) + 4;
			DataWriteHelper(fragUniforms, uniforms, i.id, state);
		}
		
		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glActiveTexture(gl.GL_TEXTURE0+texUnit);
		
		gl.glBindTexture(gl.GL_TEXTURE_2D, textureID);
		
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST); 
	    gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
	    
	    gl.glPixelStorei(gl.GL_UNPACK_ROW_LENGTH, shader.getNumFloatsNecessary()/4);
    	gl.glPixelStorei(gl.GL_UNPACK_SKIP_ROWS, 0);
    	gl.glPixelStorei(gl.GL_UNPACK_SKIP_PIXELS, 0);
	    gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_RGBA32F, shader.getNumFloatsNecessary()/4, MAX_NUMBER_OBJ_IN_COLLECTION, 0, gl.GL_RGBA, gl.GL_FLOAT, FloatBuffer.wrap(data));
	}
	
	private int generateNewTexID(GL3 gl){
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		return textures[0];
	}
	
	private void bindUniformsTexture(GL3 gl) {
		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glActiveTexture(gl.GL_TEXTURE0+texUnit);
		gl.glBindTexture(gl.GL_TEXTURE_2D, textureID);
	}
	
	private OptimizedGLShader shader;
	
	public InstanceCollection(OptimizedGLShader shader){
		this.shader = shader;
	}
	
	public void init(GL3 gl){
		this.gl = gl;
		shader.init(gl);
		putNewVBO("vertex_coordinates", GL3.GL_FLOAT, 4);
		putNewVBO("vertex_id", GL3.GL_INT, 1);
		nullRest();
		initUniformTexture(gl);
	}
	
	GL3 gl;
	
	/**
	 * depends on the maximum texture dimension
	 */
	public static final int MAX_NUMBER_OBJ_IN_COLLECTION = 100;
	
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
	
//	private LinkedList<Instance> instances = new LinkedList<Instance>();
	private WeakHashMap<JOGLFaceSetInstance, Instance> instances = new WeakHashMap<JOGLFaceSetInstance, Instance>();
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
		if(type == 5126)
			System.out.println("putting vbo " + name + " with type glFloat");
		if(type == 5124)
			System.out.println("putting vbo " + name + " with type glInt");
		if(type == GL3.GL_FLOAT){
			gpuData.put(name, new GLVBOFloat(gl, new float[current_vbo_size], name, elementSize));
		}else if(type == GL3.GL_INT){
			gpuData.put(name, new GLVBOInt(gl, new int[current_vbo_size], name, elementSize));
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
		System.out.println("new size = " + current_vbo_size);
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
				if(largevbo.getName().equals("vertex_id")){
					int id = i.id;
					int[] vertex_id = new int[i.length];
					for(int g = 0; g < i.length; g++){
						vertex_id[g] = id;
					}
					f.updateSubData(gl, vertex_id, 4*i.posInVBOs, 4*i.length);
				}else{
					//vbo.getLength()*4 must equal i.length
					f.updateSubData(gl, ((GLVBOInt)vbo).getData(), 4*i.posInVBOs, 4*i.length);
				}
			}else{
				System.err.println("largevbo has unknown type (InstanceCollection.java 3)");
			}
		}
	}
	
	private void pushOnlyInstanceIDToGPU(Instance i){
		if(gpuData.get("vertex_id") == null)
			System.err.println("vertex_id = null in InstanceCollection.pushOnlyInstanceIDToGPU");
		if(!gpuData.get("vertex_id").getClass().equals(GLVBOInt.class))
			System.err.println("vertex_id not of class GLVBOInt but " + gpuData.get("vertex_id").getClass().getName());
		GLVBOInt largevbo = (GLVBOInt)gpuData.get("vertex_id");
		int id = i.id;
		int[] vertex_id = new int[i.length];
		for(int g = 0; g < i.length; g++){
			vertex_id[g] = id;
		}
		largevbo.updateSubData(gl, vertex_id, 4*i.posInVBOs, 4*i.length);
	}
	
	//_______________________****************PUBLIC METHODS****************__________________________
	/**
	 * get the number of instances, you can still add to this collection
	 * @return
	 */
	public int getNumberFreeInstances() {
		return MAX_NUMBER_OBJ_IN_COLLECTION-numAliveInstances;
	}
	
	public boolean contains(JOGLFaceSetInstance fsi){
		if(instances.get(fsi) == null)
			return false;
		return true;
	}
	
	/**
	 * only registers a new Instance to this InstanceCollection. To push changes to GPU, use update()
	 * @param fsi
	 */
	public Instance registerNewInstance(JOGLFaceSetInstance fsi, JOGLRenderState state){
		if(fsi.eap==null){
			System.err.println("effective appearance of fsi is null, not registering this instance");
			return null;
		}
		boolean visible = (boolean)fsi.eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POLYGON_SHADER, CommonAttributes.FACE_DRAW), CommonAttributes.FACE_DRAW_DEFAULT);
		if(!visible){
			System.err.println("FSI not visible -> not registering");
			return null;
		}
		Instance ret = new Instance(this, fsi, state, 0);
		newInstances.add(ret);
		numAliveInstances++;
		return ret;
	}
	/**
	 * register Instance for deletion
	 * @param i
	 */
	public void kill(Instance i){
		if(!instances.containsKey(i.fsi))
			System.err.println("cannot kill this Instance, because it is not in this InstanceCollection");
		if(i.isAlive()){
			i.kill();
			dyingInstances.add(i);
			numAliveInstances--;
		}
	}
	
	private void writeAllInstancesNewToVBO(){
		//write rest to gpu
		dead_count = 0;
		//delete all dyingInstances from instances
		for(Instance i : dyingInstances){
//			System.out.println("removing dying instance?...");
//			System.out.println(i.fsi);
//			if(instances.containsKey(i.fsi))
//				System.out.println("...for real!");
			instances.remove(i.fsi);
		}
		//move all newInstances to instances
		for(Instance i : newInstances){
//			System.out.println("putting new instance");
//			System.out.println(i.fsi);
			instances.put(i.fsi, i);
		}
		
		//push ALL instances to GPU
		int pos = 0;
		int counter = 0;
		for(JOGLFaceSetInstance fsi : instances.keySet()){
			System.out.println("writing still alive instances to gpu");
			Instance i = instances.get(fsi);
			i.id = counter;
			counter++;
			availableFloats -= i.length;
			i.posInVBOs = pos;
			i.upToDate = true;
			pos += i.length;
			pushInstanceToGPU(i);
		}
		//null the rest of the VBO
		nullRest();
	}
	/**
	 * very important method! Pushes all changes to the GPU
	 * !only necessary to call this, if actually changes have been made!
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
//		System.out.println("vbo_size = " + current_vbo_size);
//		System.out.println("avail = " + availableFloats);
//		System.out.println("dead_count = " + dead_count);
		int numFloatsNeeded = current_vbo_size - availableFloats - dead_count;
		if(numFloatsNeeded > current_vbo_size || (numFloatsNeeded <= current_vbo_size/2 && current_vbo_size > START_SIZE))
			mustResize = true;
		if(mustResize){
			//resize
			float neededPow = numFloatsNeeded/1000f;
			int pow = (int)Math.ceil(Math.log(neededPow)/Math.log(2));
			if(pow < 0)
				pow = 0;
//			System.out.println("pow = " + pow);
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
					instances.put(i.fsi, i);
					i.upToDate = true;
					pushInstanceToGPU(i);
				}
				//and null (and remove?) the dead ones
				for(Instance i : dyingInstances){
					nullInstance(i);
					i.upToDate = true;
					//TODO is this right?
					instances.remove(i);
				}
				//and update the !up_to_date ones
				int counter = 0;
				for(JOGLFaceSetInstance fsi : instances.keySet()){
					Instance i = instances.get(fsi);
					if(!i.upToDate){
						pushInstanceToGPU(i);
						i.upToDate = true;
					}
					i.id = counter;
					counter++;
					pushOnlyInstanceIDToGPU(i);
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
		updateUniformsTexture(gl);
	}
	
	public void render(GL3 gl){
//		System.out.println("IC.render(gl)");
		bindUniformsTexture(gl);
		
		//bind vbos to corresponding shader variables
    	List<ShaderVar> l = shader.vertexAttributes;
    	for(String s : gpuData.keySet()){
    		GLVBO vbo = gpuData.get(s);
    		if(vbo != null){
//    			System.out.println(s);
    			ShaderVarHash.bindUniform(shader, "has_" + s, 1, gl);
//    			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_" + v.getName()), 1);
    			gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo.getID());
            	gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, s), vbo.getElementSize(), vbo.getType(), false, 0, 0);
            	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, s));
    		}else{
    			ShaderVarHash.bindUniform(shader, "has_" + s, 0, gl);
//    			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_" + v.getName()), 0);
    		}
    	}
//    	System.out.println("___");
    	//actual draw command
    	gl.glDrawArrays(gl.GL_TRIANGLES, 0, gpuData.get("vertex_coordinates").getLength()/4);
		
    	//disable all vbos
    	for(String s : gpuData.keySet()){
    		GLVBO vbo = gpuData.get(s);
    		if(vbo != null){
    			gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, s));
    		}
    	}
    	
	}
}
