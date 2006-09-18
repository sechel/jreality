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

package de.jreality.softviewer;

import java.util.Vector;

public class IntersectingPipeline extends TrianglePipeline {

    public IntersectingPipeline(TriangleRasterizer rasterizer) {
        super(rasterizer, true);
        // TODO Auto-generated constructor stub
    }

    public void finish() {
       //super.finish();
        
        rasterRemaining();
    }

    private void rasterRemaining() {
        System.out.println("rastering rest");
        // hope this helps
        // we need to dehomogenize for the intersection to work
        int n = triangles.getSize();
        // Triangle[] array = triangles.getArray();
        for (int i = 0; i < n; i++) {
            PolygonUtility.dehomogenize(triangles.get(i));
        }
        sortTriangles();
        Vector<Triangle> loopKiller = new Vector<Triangle>();
        retry: while (!triangles.isEmpty()) {
//            System.out.println(".."+triangles.getSize());
            Triangle a = triangles.pop();
            n = triangles.getSize();
            for (int i = n - 1; i >= 0; i--) {
                Triangle b = triangles.get(i);
                int test = PolygonUtility.liesBehind(a, b);
                // System.out.println("....
                // (a,b)"+PolygonUtility.liesBehind(a,b));
                // System.out.println(".... b
                // (b,a)"+PolygonUtility.liesBehind(b,a));

                if (test == 1) {
                    // System.out.println(".... through "+i);
                    continue;
                }
                if (test == -1) {
                     System.out.println(".... blocked by "+i+" retry");
                    // System.out.println("a = "+a);
                    // System.out.println("b = "+b);

                    if ( !loopKiller.contains(a)) {
                    triangles.set(i, a);
                        loopKiller.add(a);
                        triangles.push(b);
                        continue retry;
                    } else {
//                        System.err.println("loop found. continue anyway");

                        Triangle[] array = PolygonUtility.cutOut(b,a);
                        System.err.println(" by adding "+array.length+" triangles (to "+triangles.getSize()+")");
                        for (int j = 1; j < array.length; j++) {
                            triangles.push(array[j]);
                            // System.out.println("-> "+result[j]);
                        }
                        loopKiller.remove(a);
                        if(array.length>0)
                            triangles.set(i, array[0]);
                        freeTriangles.push(b);
                        //triangles.push(a);
                        //continue retry;
                        
                    }
                } else {
                    //intersect
                    // first get the triangles that are result of intersection
                    // and that are in front of b
                     System.out.println("....intersect");
                    // System.out.println("....(a,b)
                    // "+PolygonUtility.liesBehind(a,b));
                    // System.out.println("a = "+a);
                    // System.out.println("b = "+b);
                    // System.out.println("gives");
                    tmpPolygon.setLength(0);
                    Triangle[] result = PolygonUtility.intersect(a, b, -1,
                            tmpPolygon, freeTriangles);
                    PolygonUtility.liesBehind(a, b);
                    for (int j = 0; j < result.length; j++) {
                        triangles.push(result[j]);
                        // System.out.println("-> "+result[j]);
                    }
                    // then get the triangles that are behind b and use the
                    // first as new a:
                    tmpPolygon.setLength(0);
                    result = PolygonUtility.intersect(a, b, 1, tmpPolygon,
                            freeTriangles);
                    // System.out.println(" and ");
                    for (int j = 0; j < result.length; j++) {
                        triangles.push(result[j]);
                        // System.out.println("-> "+result[j]);
                    }
                    // if(result.length>0) // intersection might fail :-(
                    // a = result[0];
                    // continue;
                    freeTriangles.push(a);
                    continue retry;
                }
            }
            rasterizer.renderTriangle(a, false);
//            System.out.println("rastered no "+n);
            freeTriangles.push(a);
            loopKiller.removeAllElements();
            // renderer.renderPolygon(p, vertexData, p.getShader().isOutline());
        }
    }
    
}
