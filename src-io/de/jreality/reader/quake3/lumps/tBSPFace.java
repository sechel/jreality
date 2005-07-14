/**
 * Copyright (c) 2003, Xith3D Project Group
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
 * Neither the name of the 'Xith3D Project Group' nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
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

package de.jreality.reader.quake3.lumps;

/**
 * Insert package comments here
 * <p/>
 * Originally Coded by David Yazel on Jan 4, 2004 at 10:33:19 AM.
 */
public class tBSPFace {

    public int textureID;        // The index into the texture array
    public int effect;           // The index for the effects (or -1 = n/a)
    public int type;             // 1=polygon, 2=patch, 3=mesh, 4=billboard
    public int vertexIndex;      // The index into this face's first vertex
    public int numOfVerts;       // The number of vertices for this face
    public int meshVertIndex;    // The index into the first meshvertex
    public int numMeshVerts;     // The number of mesh vertices
    public int lightmapID;       // The texture index for the lightmap
    public int lMapCorner[];    // The face's lightmap corner in the image
    public int lMapSize[];      // The size of the lightmap section
    public float lMapPos[];     // The 3D origin of lightmap.
    public float lMapBitsets[][]; // The 3D space for s and t unit vectors.
    public float vNormal[];     // The face normal.
    public int size[];          // The bezier patch dimensions.

    public tBSPFace() {
        lMapCorner = new int[2];
        lMapSize = new int[2];
        lMapPos = new float[3];
        lMapBitsets = new float[2][3];
        vNormal = new float[3];
        size = new int [2];
    }

}
