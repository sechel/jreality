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

package de.jreality.reader.quake3;


import de.jreality.reader.quake3.lumps.tBSPVertex;
import de.jreality.scene.IndexedFaceSet;
//import com.xith3d.scenegraph.IndexedTriangleArray;

/**
 * Insert package comments here
 * <p/>
 * Originally Coded by David Yazel on Jan 9, 2004 at 11:31:25 PM.
 */
public class BezierPatch {

    private tBSPVertex vertex[];
    private int level;

    public tBSPVertex controls[];

    public BezierPatch() {
        controls = new tBSPVertex[9];
    }

    public void tesselate(int L) {

        level = L;

        // The number of vertices along a side is 1 + num edges
        final int L1 = L + 1;

        vertex = new tBSPVertex[L1 * L1];

        // Compute the vertices
        int i;

        for (i = 0; i <= L; ++i) {
            float a = (float) i / (float)L;
            float b = 1 - a;

            tBSPVertex v1 = controls[0].copy();
            v1.scale(b * b);

            tBSPVertex v2 = controls[3].copy();
            v2.scale(2 * b * a);

            tBSPVertex v3 = controls[6].copy();
            v3.scale(a * a);

            vertex[i] = v1;
            vertex[i].add(v2);
            vertex[i].add(v3);
        }

        for (i = 1; i <= L; ++i) {
            float a = (float) i / (float)L;
            float b = 1.0f - a;

            tBSPVertex temp[] = new tBSPVertex[3];

            int j;
            for (j = 0; j < 3; ++j) {
                int k = 3 * j;

                tBSPVertex v1 = controls[k + 0].copy();
                v1.scale(b * b);

                tBSPVertex v2 = controls[k + 1].copy();
                v2.scale(2 * b * a);

                tBSPVertex v3 = controls[k + 2].copy();
                v3.scale(a * a);

                temp[j] = v1;
                temp[j].add(v2);
                temp[j].add(v3);

            }

            for (j = 0; j <= L; ++j) {
                float aa = (float) j / (float)L;
                float bb = 1.0f - aa;

                tBSPVertex v1 = temp[0].copy();
                v1.scale(bb * bb);

                tBSPVertex v2 = temp[1].copy();
                v2.scale(2 * bb * aa);

                tBSPVertex v3 = temp[2].copy();
                v3.scale(aa * aa);

                v1.add(v2);
                v1.add(v3);

                vertex[i * L1 + j] = v1;
            }
        }
    }

    public int addToIndexedArray( IndexedFaceSet tri, int index[], int startIndex, int startVertex, float scale) {

        // step through and convert the vertices


//        for (int i=0;i<vertex.length;i++) {
//            int v = tri.newVertex();
//            vertex[i].swizzle();
//            vertex[i].position.scale(scale);
//            tri.setCoordinate(vertex[i].position.x,vertex[i].position.y,vertex[i].position.z);
//            tri.setTexCoord(0,vertex[i].texCoord);
//            tri.setTexCoord(1,vertex[i].lightTexCoord);
//        }

        // step through and assign the indexes

        final int L = level;
        final int L1 = L+1;

        for (int row = 0; row < L; row++) {
            for(int col = 0; col < L; col++)	{

                index[startIndex++] = row * L1 + col + startVertex;
                index[startIndex++] = (row+1) * L1 + (col+1) + startVertex;
                index[startIndex++] = (row+1) * L1 + col + startVertex;

                index[startIndex++] = row * L1 + col + startVertex;
                index[startIndex++] = (row) * L1 + (col+1) + startVertex;
                index[startIndex++] = (row+1) * L1 + (col+1) + startVertex;

            }
        }

        return startIndex;
    }

    public int getNumTriangles() {
        return (level+1) * (level+1) * 2;
    }
}
