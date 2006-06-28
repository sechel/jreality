/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.jreality.renderman.shader;

import java.awt.Color;
import java.util.Map;

import javax.media.opengl.GL;

import de.jreality.renderman.RIBViewer;
import de.jreality.renderman.RIBVisitor;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;

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