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
 * Originally Coded by David Yazel on Jan 4, 2004 at 10:42:32 AM.
 */
public class tBSPDirectory {

    public final static int kEntities = 0;         // Stores player/object positions, etc...
    public final static int kTextures = 1;         // Stores texture information
    public final static int kPlanes = 2;           // Stores the splitting planes
    public final static int kNodes = 3;            // Stores the BSP nodes
    public final static int kLeafs = 4;            // Stores the leafs of the nodes
    public final static int kLeafFaces = 5;        // Stores the leaf's indices into the faces
    public final static int kLeafBrushes = 6;      // Stores the leaf's indices into the brushes
    public final static int kModels = 7;           // Stores the info of world models
    public final static int kBrushes = 8;          // Stores the brushes info (for collision)
    public final static int kBrushSides = 9;       // Stores the brush surfaces info
    public final static int kVertices = 10;        // Stores the level vertices
    public final static int kMeshVerts = 11;       // Stores the model vertices offsets
    public final static int kShaders = 12;         // Stores the shader files (blending, anims..)
    public final static int kFaces = 13;           // Stores the faces for the level
    public final static int kLightmaps = 14;       // Stores the lightmaps for the level
    public final static int kLightVolumes = 15;    // Stores extra world lighting information
    public final static int kVisData = 16;         // Stores PVS and cluster info (visibility)
    public final static int kMaxLumps = 17;        // A constant to store the number of lumps

    public tBSPLump directory[];

    public tBSPDirectory() {
        directory = new tBSPLump[17];
    }

}
