package de.jreality.jogl3.geom;

import java.awt.Color;
import java.util.LinkedList;

import javax.media.opengl.GL3;

import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.JOGLTexture2D;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.glsl.GLShader.ShaderVar;
import de.jreality.jogl3.shader.ShaderVarHash;
import de.jreality.jogl3.shader.Texture2DLoader;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;

public abstract class JOGLGeometryInstance extends SceneTreeNode {

	public class GlTexture{
		boolean hasTexture = false;
		public GlTexture(){
			
		}
		private Texture2D tex = null;
		public void setTexture(Texture2D tex){
			this.tex = tex;
			hasTexture = true;
		}
		public void removeTexture(){
			hasTexture = false;
		}
		public void bind(GLShader shader, GL3 gl){
			if(hasTexture){
				//GL_TEXTURE0 and GL_TEXTURE1 reserved for lights.
				Texture2DLoader.load(gl, tex, gl.GL_TEXTURE8);
				ShaderVarHash.bindUniform(shader, "image", 8, gl);
				ShaderVarHash.bindUniform(shader, "has_Tex", 1, gl);
				//gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "image"), 8);
				//gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_Tex"), 1);
			}else{
				ShaderVarHash.bindUniform(shader, "has_Tex", 0, gl);
				//gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_Tex"), 0);
			}
		}
	}
	
	public class GlReflectionMap{
		boolean hasReflectionMap = false;
		public GlReflectionMap(){
			
		}
		private JOGLTexture2D[] jogltex = new JOGLTexture2D[6];
		public void setCubeMap(CubeMap cm){
			ImageData[] imgs=TextureUtility.getCubeMapImages(cm);
			for(int i = 0; i < 6; i++){
//				  String name = "right";
//				  if(i == 1)
//					  name = "left";
//				  else if(i == 2)
//					  name = "up";
//				  else if(i == 3)
//					  name = "down";
//				  else if(i == 4)
//					  name = "back";
//				  else if(i == 5)
//					  name = "front";
				  Texture2D tex=(Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, "", new Appearance(), true);
				  tex.setRepeatS(de.jreality.shader.Texture2D.GL_CLAMP_TO_EDGE);
				  tex.setRepeatT(de.jreality.shader.Texture2D.GL_CLAMP_TO_EDGE);
				  jogltex[i] = new JOGLTexture2D(tex);
				  
				  //jogltex[i].setBlendColor(cm.getBlendColor());
				  jogltex[i].setImage(imgs[i]);
				  
				  
			}
			hasReflectionMap = true;
		}
		public void removeTexture(){
			hasReflectionMap = false;
		}
		public void bind(GLShader shader, GL3 gl){
			if(hasReflectionMap){
				//GL_TEXTURE0 and GL_TEXTURE1 reserved for lights.
				for(int i = 0; i < 6; i++){
					 String name = "right";
					  if(i == 1)
						  name = "left";
					  else if(i == 2)
						  name = "up";
					  else if(i == 3)
						  name = "down";
					  else if(i == 4)
						  name = "back";
					  else if(i == 5)
						  name = "front";
					ShaderVarHash.bindUniform(shader, name, 2+i, gl);
					//gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, name), 2+i);
					ShaderVarHash.bindUniform(shader, "has_reflectionMap", 1, gl);
					//gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_reflectionMap"), 1);
					Texture2DLoader.load(gl, jogltex[i], gl.GL_TEXTURE2+i);
				}
			}else{
				ShaderVarHash.bindUniform(shader, "has_reflectionMap", 0, gl);
				//gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_reflectionMap"), 0);
			}
		}
	}
	
	public abstract class GlUniform<T>{
		public GlUniform(String name, T value){
			this.name = name;
			this.value = value;
		}
		public String name;
		public T value;
		
		public abstract void bindToShader(GLShader shader, GL3 gl);
		
		
	}
//	public class GlUniformSampler extends GlUniform<Integer>{
//		public Texture2D tex;
//		public GlUniformSampler(String name, Integer value, Texture2D tex) {
//			super(name, value);
//			this.tex = tex;
//		}
//
//		@Override
//		public void bindToShader(GLShader shader, GL3 gl) {
//			Texture2DLoader.load(gl, tex, gl.GL_TEXTURE2);
//			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, name), value);
//		}
//	}
//	
	public class GlUniformInt extends GlUniform<Integer>{

		public GlUniformInt(String name, Integer value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			ShaderVarHash.bindUniform(shader, name, value, gl);
			//gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, name), value);
		}
	}
	public class GlUniformFloat extends GlUniform<Float>{

		public GlUniformFloat(String name, Float value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			//System.out.println("binding " + name + "= " + value);
			ShaderVarHash.bindUniform(shader,  name, value, gl);
			//gl.glUniform1f(gl.glGetUniformLocation(shader.shaderprogram, name), value);
		}
	}
	public class GlUniformVec4 extends GlUniform<float[]>{

		public GlUniformVec4(String name, float[] value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			ShaderVarHash.bindUniform(shader, name, value, gl);
			//gl.glUniform4fv(gl.glGetUniformLocation(shader.shaderprogram, name), 1, value, 0);
		}
	}
	public class GlUniformMat4 extends GlUniform<float[]>{

		public GlUniformMat4(String name, float[] value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			ShaderVarHash.bindUniformMatrix(shader, name, value, gl);
			//gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, name), 1, true, value, 0);
        }
	}
	public class GlUniformVec3 extends GlUniform<float[]>{

		public GlUniformVec3(String name, float[] value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			ShaderVarHash.bindUniform(shader, name, value, gl);
			//gl.glUniform3fv(gl.glGetUniformLocation(shader.shaderprogram, name), 1, value, 0);
		}
	}
//	public class UniformCollection{
//		public LinkedList<GlUniform<Integer>> intUniforms = new LinkedList<GlUniform<Integer>>();
//		public LinkedList<GlUniform<Float>> floatUniforms = new LinkedList<GlUniform<Float>>();
//		public LinkedList<GlUniform<float[]>> vec3Uniforms = new LinkedList<GlUniform<float[]>>();
//		public LinkedList<GlUniform<float[]>> vec4Uniforms = new LinkedList<GlUniform<float[]>>();
//		public LinkedList<GlUniform<Integer>> sampler2DUniforms = new LinkedList<GlUniform<Integer>>();
//	}
	
	//TODO make private
	public EffectiveAppearance eap;
	
	//TODO
	//public GLShader;
	//public Appearance Attributes for this shader;
	
	protected JOGLGeometryInstance(Geometry node) {
		super(node);
	}

	public abstract void render(JOGLRenderState state, int width, int height);
	public abstract void renderDepth(JOGLRenderState state, int width, int height);
	public abstract void addOneLayer(JOGLRenderState state, int width, int height, float alpha);

	
	//this method copies appearance attributes to a list of uniform variables for later use in the openGL shader
	//it furthermore returns the openGL shader to use
//	protected GLShader updateAppearance(GLShader defaultShader, SceneGraphPath sgp, GL3 gl, LinkedList<GlUniform> c, GlTexture texture, GlReflectionMap reflMap, String type) {
//		
//		GLShader shader = defaultShader;
//		
//		eap = EffectiveAppearance.create(sgp);
//		
//		//retrieve shader source if existent
//		String[] source = new String[]{};
//		
//		source = (String[])eap.getAttribute(type + "::glsl330-source", source);
//		// has attribute key like "polygonShader::glsl330-source"
//		// and an array of two Strings
//		if(source != null && source.length == 2){
//			//TODO problem here! we are not passing back the pointer...
//			shader = new GLShader(source[0], source[1]);
//			shader.init(gl);
//		}
//		//TODO retrieve and save shader attributes in a sensible
//		//fashion
//		boolean hasTexture = false;
//		boolean hasReflectionMap = false;
//		for(ShaderVar v : shader.shaderUniforms){
//			//if(type.equals(CommonAttributes.POINT_SHADER))
//				//System.out.println("shader var is " + v.getName() + ", type is " + v.getType());
//			if(v.getName().equals("projection"))
//    			continue;
//    		if(v.getName().equals("modelview")){
//    			continue;
//    		}
//    		if(v.getName().equals("screenSize")){
//    			continue;
//    		}
//    		if(v.getName().equals("screenSizeInSceneOverScreenSize")){
//    			continue;
//    		}
////    		if(v.getName().equals("front")){
////    			continue;
////    		}
//    		if(v.getName().equals("back")){
//    			continue;
//    		}
//    		if(v.getName().equals("left")){
//    			continue;
//    		}
//    		if(v.getName().equals("right")){
//    			continue;
//    		}
//    		if(v.getName().equals("up")){
//    			continue;
//    		}
//    		if(v.getName().equals("down")){
//    			continue;
//    		}
//    		if(v.getName().length() > 3 && v.getName().substring(0, 4).equals("_")){
//    			continue;
//    		}
//    		if(v.getName().length() > 3 && v.getName().substring(0, 4).equals("has_")){
//    			continue;
//    		}
//    		if(v.getName().length() > 3 && v.getName().substring(0, 4).equals("sys_")){
//    			continue;
//    		}
//    		//System.out.println("updateAppearance " + v.getName());
//    		//TODO exclude some more like light samplers, camPosition
//    		//retrieve corresponding attribute from eap
//			if(v.getType().equals("int")){
//    			Object value = new Object();
////    			Set keys = eap.getApp().getAttributes().keySet();
////    			for(Object o : keys){
////    				String s = (String)o;
////    				System.out.println(s);
////    			}
//    			value = eap.getAttribute(ShaderUtility.nameSpace(type,v.getName()),  CommonAttributes.getDefault(v.getName(), value));
////    			System.out.println("" + v.getName() + ", " + v.getType() + ", " + value.getClass());
//    			if(value.getClass().equals(Integer.class)){
//    				c.add(new GlUniformInt(v.getName(), (Integer)value));
//    				//c.intUniforms.add(new GlUniform<Integer>(v.getName(), (Integer)value));
//    				//gl.glUniform1i(gl.glGetUniformLocation(polygonShader.shaderprogram, v.getName()), (Integer)value);
//    			}else if(value.getClass().equals(Boolean.class)){
//    				boolean b = (Boolean)value;
//    				int valueInt = 0;
//        			if(b){
//        				valueInt = 1;
//        			}
//        			c.add(new GlUniformInt(v.getName(), valueInt));
//        			//gl.glUniform1i(gl.glGetUniformLocation(polygonShader.shaderprogram, v.getName()), valueInt);
//    			}else{
//    				//c.add(new GlUniformInt(v.getName(), 0));
//    			}
//    		}
//    		else if(v.getType().equals("vec4")){
////    			System.out.println(v.getName());
//    			Object value = new Object();
//    			//System.out.println(v.getName());
//    			//TODO retrieve default value somehow...
//    			
//    			value = eap.getAttribute(ShaderUtility.nameSpace(type,v.getName()), CommonAttributes.getDefault(v.getName(), value));
//    			
//    			if(value.getClass().equals(Color.class)){
//    				float[] color = ((Color)value).getRGBComponents(null);
//    				//System.out.println(sgp.getLastComponent().getName() + type + "." + v.getName() + color[0] + " " + color[1] + " " + color[2]);
//    				c.add(new GlUniformVec4(v.getName(), color));
//    			}else if(value.getClass().equals(float[].class)){
//    				c.add(new GlUniformVec4(v.getName(), (float[])value));
//    			}else if(value.getClass().equals(double[].class)){
//    				double[] value2 = (double[])value;
//    				c.add(new GlUniformVec4(v.getName(), Rn.convertDoubleToFloatArray(value2)));
//    			}else{
//    				//default value
//    				//c.add(new GlUniformVec4(v.getName(), new float[]{0, 0, 0, 1}));
//    			}
//    		}
//    		else if(v.getType().equals("float")){
////    			System.out.println(v.getName());
//    			Object value = new Object();
//    			//System.out.println(v.getName());
//    			value = eap.getAttribute(ShaderUtility.nameSpace(type,v.getName()),  CommonAttributes.getDefault(v.getName(), value));
//    			
//    			if(value.getClass().equals(Double.class)){
//    				Double value2 = (Double)value;
//    				c.add(new GlUniformFloat(v.getName(), value2.floatValue()));
//    			}else if(value.getClass().equals(Float.class)){
//    				c.add(new GlUniformFloat(v.getName(), (Float)value));
//    			}else{
//    				//c.add(new GlUniformFloat(v.getName(), 0f));
//    			}
//    		}else if(v.getType().equals("sampler2D") && v.getName().equals("image")){
//    			//ImageData value = new Object();
//    			//value = eap.getAttribute(ShaderUtility.nameSpace(type, "texture2d:image"), value);
//    			//MyEntityInterface mif = (MyEntityInterface) AttributeEntityFactory.createAttributeEntity(MyEntityInterface.class, &quot;myEntityName&quot;, ea);
//    			//Texture2D tex = (Texture2D)
//    			if(AttributeEntityUtility.hasAttributeEntity(Texture2D.class, type + ".texture2d", eap)){
//    				Texture2D tex = (Texture2D)AttributeEntityUtility.createAttributeEntity(Texture2D.class, type + ".texture2d", eap);
//    				texture.setTexture(tex);
//    				c.add(new GlUniformInt("_combineMode", tex.getApplyMode()));
//    				c.add(new GlUniformMat4("textureMatrix", Rn.convertDoubleToFloatArray(tex.getTextureMatrix().getArray())));
//    				//System.out.println("sampler2D: "+ v.getName());
//    				hasTexture = true;
//    			}
//    		}else if(v.getType().equals("sampler2D") && v.getName().equals("front")){
//    			if(AttributeEntityUtility.hasAttributeEntity(CubeMap.class, type + ".reflectionMap", eap)){
//    				CubeMap reflectionMap = TextureUtility.readReflectionMap(eap, type + ".reflectionMap");
//    				reflMap.setCubeMap(reflectionMap);
//    				c.add(new GlUniformFloat("_reflectionMapAlpha", reflectionMap.getBlendColor().getRGBComponents(null)[3]));
//    				hasReflectionMap = true;
//    			}
//    		}else if(v.getName().equals("textureMatrix")){
//    			//do nothing
//    		}else{
//    			System.err.println(v.getType() + " " + v.getName() + " not implemented this type yet. have to do so in JOGLGeometryInstance.updateAppearance(...).");
//    		}
//    		//TODO other possible types, textures
//    	}
//		if(!hasTexture){
//			texture.removeTexture();
//		}
//		if(!hasReflectionMap){
//			reflMap.removeTexture();
//		}
//		return shader;
//	}
	
	private String retrieveType(String name){
		String[] s = name.split("_");
		if(s.length > 1 && s[1].equals("polygonShader"))
			return s[0]+"."+s[1];
		return s[0];
	}
	
	private String retrieveName(String name){
		String[] s = name.split("_");
		if(s.length > 2 && s[1].equals("polygonShader"))
			return name.substring(s[0].length()+s[1].length()+2);
		if(s.length > 1)
			return name.substring(s[0].length()+1);
		else
			return s[0];
	}
	
	//in the new version we use type only to identify the shader source
	protected GLShader updateAppearance(GLShader defaultShader, SceneGraphPath sgp, GL3 gl, LinkedList<GlUniform> c, GlTexture texture, GlReflectionMap reflMap, String shaderType) {
		
		GLShader shader = defaultShader;
		
		eap = EffectiveAppearance.create(sgp);
		
		//retrieve shader source if existent
		String[] source = new String[]{};
		
		source = (String[])eap.getAttribute(shaderType + ".glsl330-source", source);
		// has attribute key like "polygonShader::glsl330-source"
		// and an array of two Strings
		if(source != null && source.length == 2){
			System.out.println("shader type is " + shaderType);
			System.out.println("creating custom shader. source is " + source[0]);
			//TODO problem here! we are not passing back the pointer...
			shader = new GLShader(source[0], source[1]);
			shader.init(gl);
		}
		//TODO retrieve and save shader attributes in a sensible
		//fashion
		boolean hasTexture = false;
		boolean hasReflectionMap = false;
		for(ShaderVar v : shader.shaderUniforms){
			
			String name = retrieveName(v.getName());
    		String type = retrieveType(v.getName());
    		
			//if(type.equals(CommonAttributes.POINT_SHADER))
				//System.out.println("shader var is " + v.getName() + ", type is " + v.getType());
			if(v.getName().equals("projection"))
    			continue;
    		if(v.getName().equals("modelview")){
    			continue;
    		}
    		if(v.getName().equals("screenSize")){
    			continue;
    		}
    		if(v.getName().equals("screenSizeInSceneOverScreenSize")){
    			continue;
    		}
//    		if(v.getName().equals("front")){
//    			continue;
//    		}
    		if(name.equals("back")){
    			continue;
    		}
    		if(name.equals("left")){
    			continue;
    		}
    		if(name.equals("right")){
    			continue;
    		}
    		if(name.equals("up")){
    			continue;
    		}
    		if(name.equals("down")){
    			continue;
    		}
    		if(v.getName().length() > 3 && v.getName().substring(0, 4).equals("_")){
    			continue;
    		}
    		if(v.getName().length() > 3 && type.equals("has")){
    			continue;
    		}
    		if(v.getName().length() > 3 && type.equals("sys")){
    			continue;
    		}
    		//System.out.println("updateAppearance " + v.getName());
    		//TODO exclude some more like light samplers, camPosition
    		//retrieve corresponding attribute from eap
    		
    		
			if(v.getType().equals("int")){
    			Object value = new Object();
//    			Set keys = eap.getApp().getAttributes().keySet();
//    			for(Object o : keys){
//    				String s = (String)o;
//    				System.out.println(s);
//    			}
    			
    			value = eap.getAttribute(ShaderUtility.nameSpace(type,name),  CommonAttributes.getDefault(retrieveName(v.getName()), value));
//    			System.out.println("" + v.getName() + ", " + v.getType() + ", " + value.getClass());
    			if(value.getClass().equals(Integer.class)){
    				c.add(new GlUniformInt(v.getName(), (Integer)value));
    				//c.intUniforms.add(new GlUniform<Integer>(v.getName(), (Integer)value));
    				//gl.glUniform1i(gl.glGetUniformLocation(polygonShader.shaderprogram, v.getName()), (Integer)value);
    			}else if(value.getClass().equals(Boolean.class)){
    				boolean b = (Boolean)value;
    				int valueInt = 0;
        			if(b){
        				valueInt = 1;
        			}
        			c.add(new GlUniformInt(v.getName(), valueInt));
        			//gl.glUniform1i(gl.glGetUniformLocation(polygonShader.shaderprogram, v.getName()), valueInt);
    			}else{
    				//c.add(new GlUniformInt(v.getName(), 0));
    			}
    		}
    		else if(v.getType().equals("vec4")){
//    			System.out.println(v.getName());
    			Object value = new Object();
    			//System.out.println(v.getName());
    			//TODO retrieve default value somehow...
    			
    			value = eap.getAttribute(ShaderUtility.nameSpace(type,name), CommonAttributes.getDefault(name, value));
    			
    			if(value.getClass().equals(Color.class)){
    				float[] color = ((Color)value).getRGBComponents(null);
    				//System.out.println(sgp.getLastComponent().getName() + type + "." + v.getName() + color[0] + " " + color[1] + " " + color[2]);
    				c.add(new GlUniformVec4(v.getName(), color));
    			}else if(value.getClass().equals(float[].class)){
    				c.add(new GlUniformVec4(v.getName(), (float[])value));
    			}else if(value.getClass().equals(double[].class)){
    				double[] value2 = (double[])value;
    				c.add(new GlUniformVec4(v.getName(), Rn.convertDoubleToFloatArray(value2)));
    			}else{
    				//default value
    				//c.add(new GlUniformVec4(v.getName(), new float[]{0, 0, 0, 1}));
    			}
    		}
    		else if(v.getType().equals("float")){
//    			System.out.println(v.getName());
    			Object value = new Object();
    			//System.out.println(v.getName());
    			value = eap.getAttribute(ShaderUtility.nameSpace(type,name),  CommonAttributes.getDefault(name, value));
    			
    			if(value.getClass().equals(Double.class)){
    				Double value2 = (Double)value;
    				c.add(new GlUniformFloat(v.getName(), value2.floatValue()));
    			}else if(value.getClass().equals(Float.class)){
    				c.add(new GlUniformFloat(v.getName(), (Float)value));
    			}else{
    				//c.add(new GlUniformFloat(v.getName(), 0f));
    			}
    		}else if(v.getType().equals("sampler2D") && name.equals("image")){
    			//ImageData value = new Object();
    			//value = eap.getAttribute(ShaderUtility.nameSpace(type, "texture2d:image"), value);
    			//MyEntityInterface mif = (MyEntityInterface) AttributeEntityFactory.createAttributeEntity(MyEntityInterface.class, &quot;myEntityName&quot;, ea);
    			//Texture2D tex = (Texture2D)
    			if(AttributeEntityUtility.hasAttributeEntity(Texture2D.class, shaderType + ".texture2d", eap)){
    				Texture2D tex = (Texture2D)AttributeEntityUtility.createAttributeEntity(Texture2D.class, shaderType + ".texture2d", eap);
    				texture.setTexture(tex);
    				c.add(new GlUniformInt("_combineMode", tex.getApplyMode()));
    				c.add(new GlUniformMat4("textureMatrix", Rn.convertDoubleToFloatArray(tex.getTextureMatrix().getArray())));
    				System.err.println("sampler2D: "+ v.getName());
    				hasTexture = true;
    			}
    		}else if(v.getType().equals("sampler2D") && name.equals("front")){
    			if(AttributeEntityUtility.hasAttributeEntity(CubeMap.class, shaderType + ".reflectionMap", eap)){
    				CubeMap reflectionMap = TextureUtility.readReflectionMap(eap, shaderType + ".reflectionMap");
    				reflMap.setCubeMap(reflectionMap);
    				c.add(new GlUniformFloat("_reflectionMapAlpha", reflectionMap.getBlendColor().getRGBComponents(null)[3]));
    				hasReflectionMap = true;
    			}
    		}else if(v.getName().equals("textureMatrix")){
    			//do nothing
    		}else{
    			System.err.println(v.getType() + " " + v.getName() + " not implemented this type yet. have to do so in JOGLGeometryInstance.updateAppearance(...).");
    		}
    		//TODO other possible types, textures
    	}
		if(!hasTexture){
			texture.removeTexture();
		}
		if(!hasReflectionMap){
			reflMap.removeTexture();
		}
		return shader;
	}

	public abstract void updateAppearance(SceneGraphPath sgp, GL3 gl);
}
