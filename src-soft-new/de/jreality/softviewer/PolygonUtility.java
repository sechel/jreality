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


/**
 * This class capsules utility methods for intersecting polygons (well triangles
 * only at the moment...). It is <em> not</em> speed optimized. It is mainly for
 * file exports at the moment.
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class PolygonUtility {
    private static final boolean DEBUG = false;
    
    private static final int CLIPPED_OUT     = -1;
    private static final int CLIPPED_PARTIAL =  1;
    private static final int CLIPPED_IN      =  0;
    /**
     * 
     */
    public PolygonUtility() {
        super();
    }


private static int once = 0;
    public static void dehomogenize(AbstractPolygon p) {
        for(int i = 0;i<p.getLength();i++) {
            double[] vd =p.getPoint(i);
            double w =1/vd[AbstractPolygon.SW];
            vd[AbstractPolygon.SX] *= w; 
            vd[AbstractPolygon.SY] *= w; 
            vd[AbstractPolygon.SZ] *= w; 
            vd[AbstractPolygon.SW]  = 1;
        }
        
    }
    private static final double EPS =0.0001;
    
    private static int testTriangleInHalfSpace(
            Triangle t,
            double[] planeNormal,
            int sign,
            double k
            //TrianglePipeline pipeline
            ) {
        if(DEBUG) {
            System.out.println(" Normal: ("+planeNormal[0]+", "+planeNormal[1]+", "+planeNormal[2]+")");
        }
        
        //****
        int hin=0;
        int hout=0;
        double[] vd = t.getP0();
        double test = sign*(VecMat.dot(vd,Triangle.SX,planeNormal,0)
                - k  * vd[Triangle.SW]);
        if (test < -EPS)
            hin++;
        else if (test > EPS)
            hout++;
        
        vd = t.getP1();
        test = sign*(VecMat.dot(vd,Triangle.SX,planeNormal,0)
                - k  * vd[Triangle.SW]);
        if (test < -EPS)
            hin++;
        else if (test > EPS)
            hout++;
        
        vd = t.getP2();
        test = sign*(VecMat.dot(vd,Triangle.SX,planeNormal,0)
                - k  * vd[Triangle.SW]);
        if (test < -EPS)
            hin++;
        else if (test > EPS)
            hout++;
        
        if (hin == 0) {
            return CLIPPED_OUT;
        }
        if (hout == 0) {
            return CLIPPED_IN;
        }
        return CLIPPED_PARTIAL;
    }
    
    
    public static int clipTriangleToHalfspace(
            //int polPos,
            Triangle tri,
            double[] planeNormal,
            int sign,
            double k,
            Triangle a, // for the result
            Triangle b // for the result
            ) {
        
        int testResult =testTriangleInHalfSpace(tri,planeNormal,sign,k);
        
        if(testResult!=CLIPPED_PARTIAL) 
            return testResult;
        
        int result = 1;
        
        double[] u = tri.getPoint(2);
        double[] v = tri.getPoint(1);
        double tu = sign*(VecMat.dot(u,Triangle.SX,planeNormal,0)
                - k  * u[Triangle.SW]);
        double tv = 0;
        //HERE!!!!!
        
        int newTriVertex = 0;
        Triangle newTri = a;
        for (int i = 0; i < 3; i++, u = v, tu = tv, v = tri.getPoint(i)) {
            tv = sign*(VecMat.dot(v,Triangle.SX,planeNormal,0)
            - k * v[Triangle.SW]);
            if(DEBUG) System.out.println(" new tv "+tv);
            if (tu <= 0. ^ tv <= 0.) { // edge crosses plane...
                double t = tu / (tu - tv);
 
                double[] vd = newTri.getPoint(newTriVertex);
                for (int j = 0; j < Triangle.VERTEX_LENGTH; j++) {
                    vd[j] = u[j] + t * (v[j] - u[j]);
                }
                newTriVertex++;
                }
            }
            if (tv <= 0.) { // vertex v is in ...
                newTri.setPointFrom(newTriVertex,v);
                
//                newP.vertices[newP.length++] = v;
            }
                if(newTriVertex == 3) {
                    b.setPointFrom(0,a.getP0());
                    b.setPointFrom(1,a.getP2());
                    newTri = b;
                    
                    newTriVertex = 2;
                    result = 2;
        }
        return result;
    }
    
    
    
    private static int testPolygonInHalfSpace(
            final Polygon p,
            final int off,
            final double[] planeNormal,
            final int sign,
            final double k
            ) {
        if(DEBUG) {
            System.out.println(" Normal: ("+planeNormal[0]+", "+planeNormal[1]+", "+planeNormal[2]+")");
            System.out.println(" p.length: "+p.getLength());
        }
        int length = p.getLength();
        if (length == 0)
            return CLIPPED_OUT;
        
        //****
        int hin=0;
        int hout=0;
        
        for (int i = 0; i < length; i++) {
            double[] v =p.getPoint(i);

            double test = sign*(VecMat.dot(v,off,planeNormal,0)
                    - k  * v[off+3]);
            if(DEBUG) System.out.println(" vertex: ("+v[off]+"," +
                    " "+v[off+1]+", "
                    +v[off+2]+") ~ "+test);
            if (test < -EPS)
                hin++;
            else if (test > EPS)
                hout++;
        }
        if (hin == 0) {
            return CLIPPED_OUT;
        }
        if (hout == 0) {
            return CLIPPED_IN;
        }
        return CLIPPED_PARTIAL;
    }
    
    /**
     * 
     * @param p the polygon to clip
     * @param off the offset into vertex data (AbstractPolygon.SX or AbstractPolygon.WX
     * @param planeNormal the normal of the plane to clip against
     * @param sign the side of the plane
     * @param k the distance of the plane from the origin
     * @param dst the polygon to place the clipped result in.
     * @return
     */
    public static int clipToHalfspace(
            final Polygon p,
            final int off,
            final double[] planeNormal,
            final int sign,
            final double k,
            final Polygon dst
            ) {
        
        int testResult =testPolygonInHalfSpace(p,off,planeNormal,sign,k);
        
        if(testResult!=CLIPPED_PARTIAL) 
            return testResult;
        // testResult == 1;
        int length = p.getLength();
        
        double[] u = p.getPoint(length - 1);
        double[] v = p.getPoint(0);
        double tu = sign*(VecMat.dot(u,off,planeNormal,0)
                - k  * u[off+3]);
        double tv = 0;
        //HERE!!!!!
        dst.setLength(0);
        int pos = 0;
        for (int i = 0; i < length; i++, u = v, tu = tv, v = p.getPoint(i)) {
            tv = sign*(VecMat.dot(v,off,planeNormal,0)
            - k * v[off+3]);
            if(DEBUG) System.out.println(" new tv "+tv);
            if (tu <= 0. ^ tv <= 0.) { // edge crosses plane...
                double t = tu / (tu - tv);
                double[] vd = dst.getPoint(pos++);
                if(DEBUG) System.out.println(" new vertex "+pos);
                for (int j = 0; j < Triangle.VERTEX_LENGTH; j++) {
                    vd[j] =
                        u[j] + t * (v[j] - u[j]);
                }
            }
            if (tv <= 0.) { // vertex v is in ...
                dst.setPointFrom(pos++,v);
            }
        }
        dst.setShadingFrom(p);
        return testResult;
    }
    
}
