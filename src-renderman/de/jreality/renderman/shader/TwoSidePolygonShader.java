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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
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
import java.io.File;
import java.util.Map;

import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.renderman.RIBHelper;
import de.jreality.renderman.RIBVisitor;
import de.jreality.renderman.SLShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;

/**
 * @author bleicher
 *
 */
public class TwoSidePolygonShader extends AbstractRendermanShader {
  
  CubeMap reflectionMap;  
  
  static int count = 0;
  public Map getAttributes() {
    return map;
  }
  
  public void setFromEffectiveAppearance(RIBVisitor ribv, EffectiveAppearance eap, String name) {    
    map.clear();
    setFromEffectiveAppearance(ribv, eap, name, "front");
    setFromEffectiveAppearance(ribv, eap, name, "back");
  }
  
  
  public void setFromEffectiveAppearance(RIBVisitor ribv, EffectiveAppearance eap, String name, String side) {

    int signature = eap.getAttribute(CommonAttributes.SIGNATURE, Pn.EUCLIDEAN);
    boolean lighting = (boolean) eap.getAttribute(name+"."+side+"."+CommonAttributes.LIGHTING_ENABLED, true);
    float specularExponent =(float) eap.getAttribute(name+"."+side+"."+CommonAttributes.SPECULAR_EXPONENT,CommonAttributes.SPECULAR_EXPONENT_DEFAULT);
    float Ks =(float) eap.getAttribute(name+"."+side+"."+CommonAttributes.SPECULAR_COEFFICIENT,CommonAttributes.SPECULAR_COEFFICIENT_DEFAULT);
    float Kd =(float) eap.getAttribute(name+"."+side+"."+CommonAttributes.DIFFUSE_COEFFICIENT,CommonAttributes.DIFFUSE_COEFFICIENT_DEFAULT);
    float Ka =(float) eap.getAttribute(name+"."+side+"."+CommonAttributes.AMBIENT_COEFFICIENT,CommonAttributes.AMBIENT_COEFFICIENT_DEFAULT);
    Color diffusecolor =(Color) eap.getAttribute(name+"."+side+"."+CommonAttributes.DIFFUSE_COLOR, CommonAttributes.DIFFUSE_COLOR_DEFAULT);
    Color specularcolor =(Color) eap.getAttribute(name+"."+side+"."+CommonAttributes.SPECULAR_COLOR, CommonAttributes.SPECULAR_COLOR_DEFAULT);
    map.put("float roughness"+side,new Float(1/specularExponent));
    map.put("float Ks"+side,new Float(Ks));
    map.put("float Kd"+side,new Float(Kd));
    map.put("float Ka"+side,new Float(Ka));
    
    map.put("color diffusecolor"+side,diffusecolor);
    map.put("color specularcolor"+side,specularcolor);
    map.put("float lighting"+side, new Float( lighting ? 1 : 0));
    
    if((boolean) eap.getAttribute(CommonAttributes.TRANSPARENCY_ENABLED,false))
      map.put("float transparencyenabled"+side,new Float(1));
    else
      map.put("float transparencyenabled"+side,new Float(0));    
    
    if (signature != Pn.EUCLIDEAN) {
      map.put("signature", signature);
      map.put("objectToCamera", RIBHelper.fTranspose(ribv.getCurrentObjectToCamera()));
      shaderName = "noneuclideantwosidepolygonshader";
    }
    else shaderName ="twosidepolygonshader" ;
    boolean ignoreTexture2d = eap.getAttribute(ShaderUtility.nameSpace(name+"."+side,"ignoreTexture2d"), false);	
    if (!ignoreTexture2d && AttributeEntityUtility.hasAttributeEntity(Texture2D.class, name+"."+side+".texture2d", eap)) {
      Texture2D tex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name+"."+side,"texture2d"), eap);
      
      String fname = null;
      fname = (String) eap.getAttribute(CommonAttributes.RMAN_TEXTURE_FILE,"");
      if (fname == "")	{
        fname = null;
      } 
      if (fname == null) {
        fname = new File(ribv.writeTexture(tex)).getName();
      }
      // removed texfile path stripping -> is just the filename without path now. 
      map.put("string texturename",fname);
      Matrix textureMatrix = tex.getTextureMatrix();
      double[] mat = textureMatrix.getArray();
      if(mat != null && !Rn.isIdentityMatrix(mat, 10E-8)) {
        map.put("float[16] tm", RIBHelper.fTranspose(mat));
      }
    }
    
    
    if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class, ShaderUtility.nameSpace(name+"."+side,"reflectionMap"), eap))
    {
      reflectionMap = TextureUtility.readReflectionMap(eap, ShaderUtility.nameSpace(name+"."+side,"reflectionMap"));
      if((boolean) eap.getAttribute(CommonAttributes.RMAN_RAY_TRACING_REFLECTIONS,false))
        map.put("float raytracedreflections", new Float(1));
      else{
        map.put("float raytracedreflections", new Float(0));
        String fname = (String) eap.getAttribute(CommonAttributes.RMAN_REFLECTIONMAP_FILE,"");
        if (fname == "") {
          fname = null;
        }
        if (fname == null) {
          fname = new File(ribv.writeCubeMap(reflectionMap)).getName();
        }
        map.put("string reflectionmap", fname);
      }
      map.put("float reflectionBlend"+side, new Float(reflectionMap.getBlendColor().getAlpha()/255.0));
    }    
    
    //volume shaders
    Object obj1 = eap.getAttribute(CommonAttributes.RMAN_VOLUME_INTERIOR_SHADER, Appearance.INHERITED,SLShader.class);
    Object obj2 = eap.getAttribute(CommonAttributes.RMAN_VOLUME_EXTERIOR_SHADER, Appearance.INHERITED,SLShader.class);     
    if((obj1 != Appearance.INHERITED && (boolean) eap.getAttribute(CommonAttributes.RMAN_RAY_TRACING_VOLUMES,false))||obj2 != Appearance.INHERITED)
      map.put("float raytracedvolumes", new Float(1));    
  }
  
  public String getType() {
    return "Surface";
  }
}