package de.jreality.jogl3.geom;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.media.opengl.GL3;
import javax.swing.SwingConstants;

import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.JOGLTexture2D;
import de.jreality.jogl3.geom.Label;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.glsl.GLShader.ShaderVar;
import de.jreality.jogl3.shader.GLVBOFloat;
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
			}else{
				ShaderVarHash.bindUniform(shader, "has_Tex", 0, gl);
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
					ShaderVarHash.bindUniform(shader, "has_reflectionMap", 1, gl);
					Texture2DLoader.load(gl, jogltex[i], gl.GL_TEXTURE2+i);
				}
			}else{
				ShaderVarHash.bindUniform(shader, "has_reflectionMap", 0, gl);
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
	public class GlUniformInt extends GlUniform<Integer>{

		public GlUniformInt(String name, Integer value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			ShaderVarHash.bindUniform(shader, name, value, gl);
		}
	}
	public class GlUniformFloat extends GlUniform<Float>{

		public GlUniformFloat(String name, Float value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			ShaderVarHash.bindUniform(shader,  name, value, gl);
		}
	}
	public class GlUniformVec4 extends GlUniform<float[]>{

		public GlUniformVec4(String name, float[] value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			ShaderVarHash.bindUniform(shader, name, value, gl);
		}
	}
	public class GlUniformMat4 extends GlUniform<float[]>{

		public GlUniformMat4(String name, float[] value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			ShaderVarHash.bindUniformMatrix(shader, name, value, gl);
        }
	}
	public class GlUniformVec3 extends GlUniform<float[]>{

		public GlUniformVec3(String name, float[] value) {
			super(name, value);
		}
		public void bindToShader(GLShader shader, GL3 gl){
			ShaderVarHash.bindUniform(shader, name, value, gl);
		}
	}
	
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
	
	public class InstanceFontData{
		public Font font;
		public double scale;
		public double[] offset;
		public int alignment;
		public Color color;
		public boolean drawLabels;
	}
	
	public class LabelRenderData{
		public Texture2D tex;//
		public GLVBOFloat points;//
		public GLVBOFloat ltwh;//
		//public float scale;//
		public float[] xyzOffsetScale = new float[4];//
		public float[] xyAlignmentTotalWH = new float[4];//
		public boolean drawLabels;
	}
	
	private static final FontRenderContext frc;
	  private static BufferedImage bi;
		//TODO is there a better way to get a FontRenderContext???
		static {
			bi = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
			frc = bi.createGraphics().getFontRenderContext();
		}
	
	public void updateLabelTextureAndVBOsAndUniforms(GL3 gl, LabelRenderData lrd, Label[] labels, InstanceFontData ifd){
		if(labels == null || labels.length == 0)
			return;
		System.out.println("updateLabelTextureAndVBOsAndUniforms called");
		lrd.drawLabels = ifd.drawLabels;
		lrd.xyzOffsetScale[3] = (float)ifd.scale;
		
		lrd.xyzOffsetScale[0] = (float)ifd.offset[0];
		lrd.xyzOffsetScale[1] = (float)ifd.offset[1];
		lrd.xyzOffsetScale[2] = (float)ifd.offset[2];
		float[] points = new float[4*labels.length];
		for(int i = 0; i < labels.length; i++){
			points[4*i+0] = (float)labels[i].position[0];
			points[4*i+1] = (float)labels[i].position[1];
			points[4*i+2] = (float)labels[i].position[2];
			points[4*i+3] = (float)labels[i].position[3];
		}
		lrd.points = new GLVBOFloat(gl, points, "centers");
		
		float[] ltwh = new float[4*labels.length];
		
		BufferedImage buf;
		
		int totalwidth = 0, totalheight = 0, hh[][] = new int[labels.length][];
		String[][] ss = new String[labels.length][];
		float border[] = new float[labels.length];
		int width[] = new int[labels.length];
		
		for(int j = 0; j < labels.length; j++){
			border[j] = 0.0f;
			ss[j] = labels[j].text.split("\n");
			width[j] = 0;
			int height = 0;
			hh[j] = new int[ss[j].length];
		  
			
			if (ifd.font == null)
				ifd.font = new Font("Sans Serif",Font.PLAIN,48);
			// process the strings to find out how large the image needs to be
			// I'm not sure if I'm handling the border correctly: should a new border
			// be added for each string?  Or only for the first or last?
		  
			//only measuring the size of the rectangle needed to draw all these lines of text
			for (int i = 0; i < ss[j].length; ++i) {
				String s = ss[j][i];
				if (s == null || s.length() == 0) {
					buf=bi;
				}
				TextLayout tl = new TextLayout(s, ifd.font, frc);
				Rectangle r = tl.getBounds().getBounds();
				hh[j][i] = (int) ifd.font.getLineMetrics(s, frc).getHeight();
				height += hh[j][i];
				int tmp = (r.width + 20);
				if (tmp > width[j]) width[j] = tmp;
				float ftmp = hh[j][i] - tl.getDescent();
				if (ftmp > border[j]) border[j] = ftmp;
			}
			if(height > totalheight)
				totalheight = height;
			totalwidth += width[j];
		}
			
			
		buf = new BufferedImage(totalwidth, totalheight,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) buf.getGraphics();
		g.setBackground(new Color(0,0,0,0));
		g.clearRect(0, 0, totalwidth, totalheight);
		g.setColor(ifd.color);
		g.setFont(ifd.font);
		// LineMetrics lineMetrics = f.getLineMetrics(s,frc).getHeight();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int widthOffset = 0;
		for(int j = 0; j < labels.length; j++){
			int height = 0;
			for (int i = 0; i < ss[j].length; ++i) {
				g.drawString(ss[j][i], widthOffset, height + border[j]);
				height += hh[j][i];
			}
			ltwh[4*j+0] = (widthOffset*1.0f)/totalwidth;
			ltwh[4*j+1] = 0;
			ltwh[4*j+2] = (width[j]*1.0f)/totalwidth;
			ltwh[4*j+3] = (height*1.0f)/totalheight;
			widthOffset += width[j];
		}
		lrd.ltwh = new GLVBOFloat(gl, ltwh, "ltwh");
		
		//TODO do alignment
		lrd.xyAlignmentTotalWH = new float[]{0, 0, totalwidth, totalheight};
		switch(ifd.alignment){
			case SwingConstants.NORTH  : lrd.xyAlignmentTotalWH[0] = -.5f; lrd.xyAlignmentTotalWH[1] = -1f; break;
			case SwingConstants.EAST   : lrd.xyAlignmentTotalWH[0] = 0; lrd.xyAlignmentTotalWH[1] = -.5f; break;
			case SwingConstants.SOUTH  : lrd.xyAlignmentTotalWH[0] = -.5f; lrd.xyAlignmentTotalWH[1] = 0; break;
			case SwingConstants.WEST   : lrd.xyAlignmentTotalWH[0] = -1f; lrd.xyAlignmentTotalWH[1] = -.5f; break;
			case SwingConstants.CENTER : lrd.xyAlignmentTotalWH[0] = -.5f; lrd.xyAlignmentTotalWH[1] = -.5f; break;
			case SwingConstants.NORTH_EAST : lrd.xyAlignmentTotalWH[0] = 0; lrd.xyAlignmentTotalWH[1] = -1f; break;
			//case SwingConstants.SOUTH_EAST : default
			case SwingConstants.SOUTH_WEST : lrd.xyAlignmentTotalWH[0] = -1f; lrd.xyAlignmentTotalWH[1] = 0f; break;
			case SwingConstants.NORTH_WEST : lrd.xyAlignmentTotalWH[0] = -1f; lrd.xyAlignmentTotalWH[1] = -1f; break;
		}
		
		ImageData img = new ImageData(buf);
		
		Texture2D labelTexture = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, "", new Appearance(), true);
		labelTexture.setImage(img);
		lrd.tex = labelTexture;
	}
	
	//in the new version we use type only to identify the shader source
	protected GLShader updateAppearance(InstanceFontData ifd, GLShader defaultShader, SceneGraphPath sgp, GL3 gl, LinkedList<GlUniform> c, GlTexture texture, GlReflectionMap reflMap, String shaderType) {
		
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
		
		
		//retrieval of uniform variables for labels
		Object va = new Object();
		va = eap.getAttribute(shaderType+".textShader.font",  CommonAttributes.getDefault("font", va));
		ifd.font = (Font)va;
		
		va = eap.getAttribute(shaderType+".textShader.scale",  CommonAttributes.getDefault("scale", va));
		ifd.scale = (Double)va;
		
		va = eap.getAttribute(shaderType+".textShader.offset",  CommonAttributes.getDefault("offset", va));
		ifd.offset = (double[])va;
		
		va = eap.getAttribute(shaderType+".textShader.alignment",  CommonAttributes.getDefault("alignment", va));
		ifd.alignment = (Integer)va;
		
		va = eap.getAttribute(shaderType+".textShader.diffuseColor",  CommonAttributes.getDefault("diffuseColor", va));
		ifd.color = (Color)va;
		
		va = eap.getAttribute(shaderType+".textShader.showLabels",  CommonAttributes.getDefault("showLabels", va));
		ifd.drawLabels = (Boolean)va;
		
//		System.out.println("" + shaderType+".textShader.alignment " + ifd.alignment);
		
		
		//Automatic retrieval of shader attributes from appearance object for vertex/fragment shaders
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
//    			System.out.println("" + ShaderUtility.nameSpace(type,name) + ", " + v.getType() + ", " + value.getClass());
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
