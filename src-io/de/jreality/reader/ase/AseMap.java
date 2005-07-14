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



/* example
 *MAP_DIFFUSE {
                               public static final Category LOG =
                                   Category.getInstance(AseMap.class.getName());
 *MAP_NAME "Map #7"
 *MAP_CLASS "Bitmap"
 *MAP_SUBNO 1
 *MAP_AMOUNT 1.0000
 *BITMAP "E:\3dsmax4\Maps\COSM 256 maps\cornervine2.png"
 *MAP_TYPE Screen
 *UVW_U_OFFSET 0.0000
 *UVW_V_OFFSET 0.0000
 *UVW_U_TILING 1.0000
 *UVW_V_TILING 1.0000
 *UVW_ANGLE 0.0000
 *UVW_BLUR 1.0000
 *UVW_BLUR_OFFSET 0.0000
 *UVW_NOUSE_AMT 1.0000
 *UVW_NOISE_SIZE 1.0000
 *UVW_NOISE_LEVEL 1
 *UVW_NOISE_PHASE 0.0000
 *BITMAP_FILTER Pyramidal
                                 }
 */
/**
 * This is an ASE map node
 *
 * @author David Yazel
 *
 */
public class AseMap extends AseNode {
    public String name;
    public String mapClass;
    public int subNo;
    public float amount;
    public String bitmap;
    public String mapType;
    public float uOffset;
    public float vOffset;
    public float uTiling;
    public float vTiling;
    public float angle;
    public float blur;
    public float blurOffset;
    public float noiseAmt;
    public float noiseSize;
    public int noiseLevel;
    public float noisePhase;
    public String filter;

    public AseMap() {
        properties.put("*MAP_NAME", "name");
        properties.put("*MAP_CLASS", "mapClass");
        properties.put("*MAP_SUBNO", "subNo");
        properties.put("*MAP_AMOUNT", "amount");
        properties.put("*BITMAP", "bitmap");
        properties.put("*MAP_TYPE", "mapType");
        properties.put("*UVW_U_OFFSET", "uOffset");
        properties.put("*UVW_V_OFFSET", "vOffset");
        properties.put("*UVW_U_TILING", "uTiling");
        properties.put("*UVW_V_TILING", "vTiling");
        properties.put("*UVW_ANGLE", "angle");
        properties.put("*UVW_BLUR", "blur");
        properties.put("*UVW_BLUR_OFFSET", "blurOffset");
        properties.put("*UVW_NOUSE_AMT", "noiseAmt");
        properties.put("*UVW_NOISE_SIZE", "noiseSize");
        properties.put("*UVW_NOISE_LEVEL", "noiseLevel");
        properties.put("*UVW_NOISE_PHASE", "noisePhase");
        properties.put("*BITMAP_FILTER", "filter");
    }
}
