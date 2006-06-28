/*
 * Copyright (c) 2000,2001 David Yazel, Teseract Software, LLP
 * Copyright (c) 2003-2004, Xith3D Project Group
 * All rights reserved.
 *
 * Portions based on the Java3D interface, Copyright by Sun Microsystems.
 * Many thanks to the developers of Java3D and Sun Microsystems for their
 * innovation and design.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the 'Xith3D Project Group' nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
 * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE
 *
 */
package de.jreality.reader.ase;



/* Example
 *MATERIAL_NAME "Masonry"
 *MATERIAL_CLASS "Standard"
 *MATERIAL_AMBIENT 0.5843        0.5843        0.5843
 *MATERIAL_DIFFUSE 0.5843        0.5843        0.5843
 *MATERIAL_SPECULAR 0.8980        0.8980        0.8980
 *MATERIAL_SHINE 0.0000
 *MATERIAL_SHINESTRENGTH 0.0000
 *MATERIAL_TRANSPARENCY 0.0000
 *MATERIAL_WIRESIZE 1.0000
 *MATERIAL_SHADING Phong
 *MATERIAL_XP_FALLOFF 0.0000
 *MATERIAL_SELFILLUM 0.0000
 *MATERIAL_FALLOFF Out
 *MATERIAL_XP_TYPE Filter
 */

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;


/**
 * This node holds information from a Max ASE Material node
 *
 * @author David Yazel
 *
 */
public class AseMaterial extends AseNode {
    public String className;
    public String name;
    public Color ambient = new Color(0.75f, 0.75f, 0.75f);
    public Color diffuse = new Color(0.75f, 0.75f, 0.75f);
    public Color specular = new Color(1f, 1f, 1f);
    public float shine;
    public float shineStrength;
    public float transparency;
    public float wireSize;
    public String shading;
    public float xpFalloff;
    public float selfIllum;
    public String fallOff;
    public String xpType;
    public AseMap opacityMap = null;
    public AseMap diffuseMap = null;
    public ArrayList subMaterials = new ArrayList();

    private Input input;
    
    public AseMaterial(Input in) {
        input = in;
        properties.put("*MATERIAL_NAME", "name");
        properties.put("*MATERIAL_CLASS", "className");
        properties.put("*MATERIAL_AMBIENT", "ambient");
        properties.put("*MATERIAL_DIFFUSE", "diffuse");
        properties.put("*MATERIAL_SPECULAR", "specular");
        properties.put("*MATERIAL_SHINE", "shine");
        properties.put("*MATERIAL_SHINESTRENGTH", "shineStrength");
        properties.put("*MATERIAL_TRANSPARENCY", "transparency");
        properties.put("*MATERIAL_WIRESIZE", "wireSize");
        properties.put("*MATERIAL_SHADING", "shading");
        properties.put("*MATERIAL_XP_FALLOFF", "xpFalloff");
        properties.put("*MATERIAL_SELFILLUM", "selfIllum");
        properties.put("*MATERIAL_FALLOFF", "fallOff");
        properties.put("*MATERIAL_XP_TYPE", "xpType");
    }

    /**
     * Converts the material into an extended appearance object for a java3d
     * scenegraph.
     */
    public Appearance getAppearance(AseGeom geom) {
        System.out.println("Shader string:"+shading);
        //      if (cachedAppearance != null) return cachedAppearance;
        Appearance a = new Appearance();
        //        a.name = name;

        // calculate the rendering attributes
        //        RenderingAttributes ra = new RenderingAttributes();
        //        ra.setDepthBufferEnable(true);
        //        ra.setDepthBufferWriteEnable(true);
        //        a.setRenderingAttributes(ra);

        // calculate material
        //        Material m = new Material();

        a.setAttribute(CommonAttributes.POLYGON_SHADER + "."
                + CommonAttributes.AMBIENT_COLOR,
                ambient);
        a.setAttribute(CommonAttributes.POLYGON_SHADER + "."
                + CommonAttributes.DIFFUSE_COLOR,
                diffuse);
        a.setAttribute(CommonAttributes.POLYGON_SHADER + "."
                + CommonAttributes.SPECULAR_COLOR, specular);
        a.setAttribute(CommonAttributes.POLYGON_SHADER + "."
                + CommonAttributes.LIGHTING_ENABLED, false);
        a.setAttribute(CommonAttributes.EDGE_DRAW, false);
        // calculate transparency
        // create the texture
//        if (false) {
//            opacityMap = new AseMap();
//            opacityMap.bitmap = "bigtreetex.png";
//        }

        if (opacityMap != null) {
//            String tname = opacityMap.bitmap;
//            LoggingSystem.getLogger(this).log(Level.INFO,
//                    "opacity texture name is " + tname);
//            try {
//              ImageData id = ImageData.load(input.getRelativeInput(tname));
//                Texture2D t = new Texture2D();
//                LoggingSystem.getLogger(this).log(Level.INFO,
//                        "setting name to " + tname);
//                //                LoggingSystem.getLogger(this).log(Level.FINER,
//                // "checked name is
//                // "+t.getName());
//
//                a.setAttribute(CommonAttributes.POLYGON_SHADER + "."
//                        + CommonAttributes.TEXTURE_2D, t);
//
//                // we need to set the blending so that the opacity map blends
//                //                ra.setAlphaTestFunction(RenderingAttributes.GREATER);
//                //                ra.setAlphaTestValue(0.05f);
//                //                pa.setCullFace(PolygonAttributes.CULL_NONE);
//
//                //                TransparencyAttributes ta = new TransparencyAttributes();
//                //                ta.setTransparencyMode(TransparencyAttributes.BLENDED);
//                //                a.setTransparencyAttributes(ta);
//
//                hasTexture = true;
//            } catch (IOException nfee) {
//                LoggingSystem.getLogger(this).log(Level.INFO,
//                        "Cannot load texture " + tname);
//            }
        } else if ((diffuseMap != null) && (diffuseMap.bitmap != null)) {
            String tname = diffuseMap.bitmap;            
            LoggingSystem.getLogger(this).log(Level.INFO,
                    "texture name is " + tname);

            //           if (TextureLoader.tf.findImageFile(tname) != null ||
            // TextureLoader.tf.findImageFileJar(tname) != null) {
            //                Texture t = TextureLoader.tf.loadTexture(tname, "RGB", true,
            //                        Texture.BASE_LEVEL_LINEAR, Texture.MULTI_LEVEL_LINEAR,
            //                        Texture.WRAP, false);
            //
            //                t.setName(tname);
            //                a.setTexture(t);
            try {
              ImageData id = ImageData.load(input.getRelativeInput(tname));
              
              Texture2D t = TextureUtility.createTexture(a,
                CommonAttributes.POLYGON_SHADER + "."
                + CommonAttributes.TEXTURE_2D, id, false);
                
                // Tries to load the texture extracting the file name from the path
                // (the path for example could be "C:\Models\textures\grass.jpg" and
                // all we want is "grass.jpg").
            } catch (IOException e) {
                LoggingSystem.getLogger(this).log(
                        Level.INFO,
                        "Cannot load texture " + tname + " (or " + tname
                                + ") file not found");
                // no path separators in the filename
            }
        }
//        if (!hasTexture) {
//            a.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, 
//                    geom.wireframeColor);
//        }
//        if (!hasTexture) {
//            ColoringAttributes ca = new ColoringAttributes();
//            ca.setColor(geom.wireframeColor);
//            ca.setShadeModel(ColoringAttributes.SHADE_GOURAUD);
//            a.setColoringAttributes(ca);
////            ra.setIgnoreVertexColors(true);
//        } else {
//            TextureAttributes ta = new TextureAttributes();
////            ta.setPerspectiveCorrectionMode(ta.NICEST);
//            ta.setTextureMode(TextureAttributes.MODULATE);
//            ta.setTextureBlendColor(new Color4f(1f, 0.5f, 0.5f, 0.1f));
//            a.setTextureAttributes(ta);
//        }


        //      cachedAppearance = a;
        return a;
    }

    /**
     * Override the default parse method because we are going to parse the
     * entire mesh in thos node, rather than recusing into further node types.
     */
    public void parse(AseReader in) {
        // for this to work, blocks have to open on the same line as the
        // property definition.
        while (in.readAseLine()) {
            if (!parseProperty(in)) {
                // check for the various special types
                if (in.key.equalsIgnoreCase("*MAP_OPACITY")) {
                    opacityMap = new AseMap();
                    opacityMap.parse(in);
                    LoggingSystem.getLogger(this).log(Level.FINER, "      Parsed opacity map "+opacityMap.name);
                } else if (in.key.equalsIgnoreCase("*MAP_DIFFUSE")) {
                    diffuseMap = new AseMap();
                    diffuseMap.parse(in);
                    LoggingSystem.getLogger(this).log(Level.FINER, "      Parsed diffuse map "+diffuseMap);
                } else if (in.key.equalsIgnoreCase("*SUBMATERIAL")) {
                    int n = Integer.parseInt(in.params[0]);

                    if (n != subMaterials.size()) {
                        throw new Error(
                            "Sub-Material index does not match material list");
                    }

                    AseMaterial m = new AseMaterial(input);
                    m.parse(in);
                    LoggingSystem.getLogger(this).log(Level.FINER, "   Parsed sub-material " + m.name);
                    subMaterials.add(m);
                    in.endBlock = false;
                    LoggingSystem.getLogger(this).log(Level.FINER, "      Parsed diffuse map "+diffuseMap.name);
                } else if (in.startBlock) {
                    trashBlock(in);
                }
            }

            if (in.endBlock) {
                break;
            }
        }
    }

    public String toString() {
        return name + " (Appearance)";
    }
}
