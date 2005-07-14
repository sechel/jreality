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

import de.jreality.reader.vecmath.Color3f;




/*
 *SCENE {
                                   public static final Category LOG =
                                       Category.getInstance(AseScene.class.getName());
 *SCENE_FILENAME "castle34_recover11.max"
 *SCENE_FIRSTFRAME 0
 *SCENE_LASTFRAME 100
 *SCENE_FRAMESPEED 30
 *SCENE_TICKSPERFRAME 160
 *SCENE_ENVMAP {
 *MAP_NAME "Map #3"
 *MAP_CLASS "Bitmap"
 *MAP_SUBNO 0
 *MAP_AMOUNT 1.0000
 *BITMAP "C:\cosm\screenshots\cosm0.jpg"
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
 *SCENE_AMBIENT_STATIC 0.2863        0.2863        0.2863
                               }
 */
/**
 * Scene node for a MAX ASE file
 *
 * @author David Yazel
 *
 */
public class AseScene extends AseNode {
    public String filename = "";
    public int firstFrame = 0;
    public int lastFrame = 0;
    public int frameSpeed = 0;
    public int ticksPerFrame = 0;
    public Color3f ambient = new Color3f(0.75f, 0.75f, 0.75f);

    public AseScene() {
        properties.put("*SCENE_FILENAME", "filename");
        properties.put("*SCENE_FIRSTFRAME", "firstFrame");
        properties.put("*SCENE_LASTFRAME", "lastFrame");
        properties.put("*SCENE_FRAMESPEED", "frameSpeed");
        properties.put("*SCENE_TICKSPERFRAME", "ticksPerFrame");
        properties.put("*SCENE_AMBIENT_STATIC", "ambient");
    }
}
