/*
 * Created on May 7, 2004
 *
 */
package de.jreality.renderman.shader;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.jogl.shader.AbstractJOGLShader;
import de.jreality.math.Rn;
import de.jreality.renderman.RIBViewer;
import de.jreality.renderman.RIBVisitor;
import de.jreality.renderman.Ri;
import de.jreality.scene.Appearance;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;

/**
 * @author Charles Gunn
 *
 */
public class DefaultPolygonShader extends AbstractRendermanShader {

	// maybe use these somedays for a two-sided shader
	public static final int FRONT_AND_BACK = GL.GL_FRONT_AND_BACK;
	public static final int FRONT = GL.GL_FRONT;
	public static final int BACK = GL.GL_BACK;
	
		
	static int count = 0;
	public Map getAttributes() {
		return map;
	}
	
	public void setFromEffectiveAppearance(RIBVisitor ribv, EffectiveAppearance eap, String name) {
		map.clear();
        float specularExponent =(float) eap.getAttribute(name+"."+CommonAttributes.SPECULAR_EXPONENT,CommonAttributes.SPECULAR_EXPONENT_DEFAULT);
        float Ks =(float) eap.getAttribute(name+"."+CommonAttributes.SPECULAR_COEFFICIENT,CommonAttributes.SPECULAR_COEFFICIENT_DEFAULT);
        float Kd =(float) eap.getAttribute(name+"."+CommonAttributes.DIFFUSE_COEFFICIENT,CommonAttributes.DIFFUSE_COEFFICIENT_DEFAULT);
        float Ka =(float) eap.getAttribute(name+"."+CommonAttributes.AMBIENT_COEFFICIENT,CommonAttributes.AMBIENT_COEFFICIENT_DEFAULT);
        Color specularcolor =(Color) eap.getAttribute(name+"."+CommonAttributes.SPECULAR_COLOR, CommonAttributes.SPECULAR_COLOR_DEFAULT);
        map.put("roughness",new Float(1/specularExponent));
        map.put("Ks",new Float(Ks));
        map.put("Kd",new Float(Kd));
        map.put("Ka",new Float(Ka));
        map.put("specularcolor",specularcolor);
       
        //System.out.println("has texture "+AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace("polygonShader","texture2d"), a));
        if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, "polygonShader.texture2d", eap)) {
            Texture2D tex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace("polygonShader","texture2d"), eap);
         
            String fname = null;
            if (ribv.getRendererType() == RIBViewer.TYPE_PIXAR)	{
            		fname = (String) eap.getAttribute(CommonAttributes.RMAN_TEXTURE_FILE,"");
            		if (fname == "")	{
            			fname = null;
            		} 
            } 
            if (fname == null) {
            	fname = ribv.writeTexture(tex);
            }
            map.put("string texturename",fname);
            double[] mat = tex.getTextureMatrix().getArray();
            if(mat != null) {
            	map.put("matrix textureMatrix",RIBVisitor.fTranspose(mat));
            }
            shaderName = "transformedpaintedplastic";
        } else {
            shaderName = "plastic";
        }
    }

	public String getType() {
		return "Surface";
	}
	public String getName() {
		return shaderName;
	}
}