/*
 * Created on May 19, 2004
 *
 * This file is part of the de.jreality.soft package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.soft;

import java.util.Arrays;

import de.jreality.math.Rn;

/**
 * This class capsules utility methods for intersecting polygons (well triangles
 * only at the moment...). It is <em> not</em> speed optimized. It is mainly for
 * file exports at the moment.
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class Intersector {
    private static final boolean DEBUG = false;
    
    private static final int CLIPPED_OUT     = -1;
    private static final int CLIPPED_PARTIAL =  0;
    private static final int CLIPPED_IN      =  1;
    /**
     * 
     */
    public Intersector() {
        super();
    }

    //the completely stupid and unoptimized way:
    
    public static void intersectPolygons(PolygonPipeline pipeline) {
        Polygon[] polygons = pipeline.polygons;
        for(int i = 0; i<pipeline.polygonCount;i++) {
            
            Arrays.sort(pipeline.polygons, i, pipeline.polygonCount, pipeline.comp);
            if(DEBUG) System.out.println("~~~~~~~~~ sorted "+pipeline.polygonCount+" ~~~~~~~");
            Polygon p = polygons[i];
            double minZ = p.getMinZ(pipeline.vertexData);
            if(DEBUG) {
                System.out.println("start with "+p.getCenterZ());
                System.out.println("-> i = "+i+" (of "+pipeline.polygonCount+")");
            }
            
            int j =i+1;
            //make sure that newly created polys are left out...
            int pc =pipeline.polygonCount; 
            while (j<pc&& polygons[j].getCenterZ() > minZ) {
                if(DEBUG) {
                    System.out.println("j = "+ j);
                    System.out.println("to intersect");
                }
                intersect(p,polygons[j],pipeline);
                polygons = pipeline.polygons; // might have changed after intersection...
                minZ = p.getMinZ(pipeline.vertexData);
                j++;
            } 

        }
    }
    
    public static boolean intersect(Polygon p1, Polygon p2, PolygonPipeline pipeline) {
        double[] vd =pipeline.vertexData;
        dehomogenize(p1,vd);
        dehomogenize(p2,vd);
        double xmin =-Double.MAX_VALUE;
        double xmax =Double.MIN_VALUE;
        double ymin =-Double.MAX_VALUE;
        double ymax =Double.MIN_VALUE;
        double zmin =-Double.MAX_VALUE;
        double zmax =Double.MIN_VALUE;
        for(int i = 0;i<p1.length;i++) {
            int v =p1.vertices[i];
            if(vd[v+Polygon.SX]<xmin) xmin =vd[v+Polygon.SX];
            if(vd[v+Polygon.SX]>xmax) xmax =vd[v+Polygon.SX];
            if(vd[v+Polygon.SY]<ymin) ymin =vd[v+Polygon.SY];
            if(vd[v+Polygon.SY]>ymax) ymax =vd[v+Polygon.SY];
            if(vd[v+Polygon.SZ]<zmin) zmin =vd[v+Polygon.SZ];
            if(vd[v+Polygon.SZ]>zmax) zmax =vd[v+Polygon.SZ];
        }
        double xmin2 =-Double.MAX_VALUE;
        double xmax2 =Double.MIN_VALUE;
        double ymin2 =-Double.MAX_VALUE;
        double ymax2 =Double.MIN_VALUE;
        double zmin2 =-Double.MAX_VALUE;
        double zmax2 =Double.MIN_VALUE;
        for(int i = 0;i<p2.length;i++) {
            int v =p2.vertices[i];
            if(vd[v+Polygon.SX]<xmin2) xmin2 =vd[v+Polygon.SX];
            if(vd[v+Polygon.SX]>xmax2) xmax2 =vd[v+Polygon.SX];
            if(vd[v+Polygon.SY]<ymin2) ymin2 =vd[v+Polygon.SY];
            if(vd[v+Polygon.SY]>ymax2) ymax2 =vd[v+Polygon.SY];
            if(vd[v+Polygon.SZ]<zmin2) zmin2 =vd[v+Polygon.SZ];
            if(vd[v+Polygon.SZ]>zmax2) zmax2 =vd[v+Polygon.SZ];
        }
        if(xmin>xmax2) return false;
        if(xmin2>xmax) return false;
        if(ymin>ymax2) return false;
        if(ymin2>ymax) return false;
        if(zmin>zmax2) return false;
        if(zmin2>zmax) return false;
        // bounding boxes hit. need to investigate further.
        // now we intersect the planes.
        
        
        double[] n1 = p1.getVertexNormal(0,vd);
        if(n1[2] < 0) {
            n1[0] *= -1;
            n1[1] *= -1;
            n1[2] *= -1;
        }
        double[] n2 = p2.getVertexNormal(0,vd);
        if(n2[2] < 0) {
            n2[0] *= -1;
            n2[1] *= -1;
            n2[2] *= -1;
        }
        
        double[] dir= new double[3];
        Rn.crossProduct(dir,n1,n2);
        double l =Rn.euclideanNorm(dir);
        
        // this is tricky: if l==0, the planes do not intersect, but we will
        // have to intersect anyways, if p2 is *behind* p1.
        if(l== 0) {
            //TODO intersect if p2 lies behind p1
            return false;
        }
        dir[0] /=l;
        dir[1] /=l;
        dir[2] /=l;

        double[] pt = p2.getVertex(0,pipeline.vertexData);
        double k2 = Rn.innerProduct(pt,n2);
        
        pt = p1.getVertex(0,pipeline.vertexData);
        double k1 = Rn.innerProduct(pt,n1);
        
        
        if(testClipToHalfspace(p2,n1,-1,k1,pipeline)!= CLIPPED_PARTIAL) return false;
        int second = pipeline.copyPolygon(p1);
        
        if(DEBUG) System.out.print("CLIP");
        int res = clipToHalfspace(p1,n2,-1,k2,pipeline);
        if(DEBUG) System.out.println(res==CLIPPED_IN?"in":res==CLIPPED_OUT?"out":"partial");
        if(once<2&&res ==CLIPPED_PARTIAL) {
            p1.computeMaxZ(pipeline.vertexData);
            p1 =pipeline.polygons[second];
            res = clipToHalfspace(p1,n2,1,k2,pipeline);
            if(DEBUG) {
                System.out.println("-->"+(res==CLIPPED_IN?"in":res==CLIPPED_OUT?"out":"partial"));
                if(res != CLIPPED_PARTIAL) System.out.println("WARNING different clip");
            }
            p1.computeMaxZ(pipeline.vertexData);
            //once++;
        } else {
            pipeline.freePolygon(second);
        }
        return true;
    }
private static int once = 0;
    public static void dehomogenize(Polygon p, double[] vd) {
        for(int i = 0;i<p.length;i++) {
            int v =p.vertices[i];
//            if(vd[v+Polygon.SW]== 0)
//                System.out.println("WARNING DIV BY ZERO "+v);
            double w =1/vd[v+Polygon.SW];
            vd[v+Polygon.SX] *= w; 
            vd[v+Polygon.SY] *= w; 
            vd[v+Polygon.SZ] *= w; 
            vd[v+Polygon.SW]  = 1;
        }
        
    }
    private static final double EPS =0.0001;
    private static int testClipToHalfspace(
            Polygon p,
            double[] planeNormal,
            int sign,
            double k, PolygonPipeline pipeline) {
        if(DEBUG) {
            System.out.println(" Normal: ("+planeNormal[0]+", "+planeNormal[1]+", "+planeNormal[2]+")");
            System.out.println(" p.length: "+p.length);
        }
        if (p.length == 0)
            return CLIPPED_OUT;
        
        //****
        int hin=0;
        int hout=0;
        
        for (int i = 0; i < p.length; i++) {
            int v =p.vertices[i];

            double test = sign*(VecMat.dot(pipeline.vertexData,v+Polygon.SX,planeNormal,0)
                    - k  * pipeline.vertexData[v+Polygon.SW]);
            if(DEBUG) System.out.println(" vertex: ("+pipeline.vertexData[v+Polygon.SX]+"," +
                    " "+pipeline.vertexData[v+Polygon.SY]+", "
                    +pipeline.vertexData[v+Polygon.SZ]+") ~ "+test);
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
    public static int clipToHalfspace(
            //int polPos,
            Polygon p,
            //int tmpPos,
            //Polygon polygons[],
            //double[] vertexData,
            double[] planeNormal,
            int sign,
            double k, PolygonPipeline pipeline) {
        
        int testResult =testClipToHalfspace(p,planeNormal,sign,k,pipeline);
        
        if(testResult!=CLIPPED_PARTIAL) 
            return testResult;
        
        
        int u = p.vertices[p.length - 1];
        int v = p.vertices[0];
        double tu = sign*(VecMat.dot(pipeline.vertexData,u+Polygon.SX,planeNormal,0)
                - k  * pipeline.vertexData[u+Polygon.SW]);
        double tv = 0;
        //HERE!!!!!
        int newPolygonPos = pipeline.getFreePolygon();
        Polygon newP = pipeline.polygons[newPolygonPos];
        newP.length = 0;
        for (int i = 0; i < p.length; i++, u = v, tu = tv, v = p.vertices[i]) {
            tv = sign*(VecMat.dot(pipeline.vertexData,v+Polygon.SX,planeNormal,0)
            - k * pipeline.vertexData[v+Polygon.SW]);
            if(DEBUG) System.out.println(" new tv "+tv);
            if (tu <= 0. ^ tv <= 0.) { // edge crosses plane...
                double t = tu / (tu - tv);
                int pos = pipeline.getFreeVertex();
                if(DEBUG) System.out.println(" new vertex "+pos);
                for (int j = 0; j < Polygon.VERTEX_LENGTH; j++) {
                    pipeline.vertexData[pos + j] =
                        pipeline.vertexData[u
                                   + j]
                                   + t * (pipeline.vertexData[v + j] - pipeline.vertexData[u + j]);
//                    if(j ==Polygon.SW) 
//                        System.out.println("WWWWW "+pipeline.vertexData[pos + j]+ " "+pipeline.vertexData[v + j]+ " " );
                }
                newP.vertices[newP.length++] = pos;
                //newP.vertices[newP.length++] = vertexCount;
                //vertexCount += Polygon.VERTEX_LENGTH;
            }
            if (tv <= 0.) { // vertex v is in ...
                //              for(int j = 0;j<Polygon.VERTEX_LENGTH;j++) {
                //                  vertexData[vc+j] = vertexData[v+j];
                //              }
                newP.vertices[newP.length++] = v;
                //              vc+= Polygon.VERTEX_LENGTH;
            }
        }
        // It is left to swap and free the temp polygon...:
        int vp[] = p.vertices;
        p.vertices = newP.vertices;
        p.length = newP.length;
        newP.vertices = vp;
        //newP.length = 0;
        pipeline.freePolygon(newPolygonPos);
        if(DEBUG) {
        System.out.println("clipped length "+p.length);
        for (int i = 0; i < p.length; i++) {
            v =p.vertices[i];
            System.out.println(" after vertex: ("+pipeline.vertexData[v+Polygon.SX]+"," +
                    " "+pipeline.vertexData[v+Polygon.SY]+", "
                    +pipeline.vertexData[v+Polygon.SZ]+")");
        }
        }
        //System.out.println("clip partial poly" +p);
        return CLIPPED_PARTIAL;
    }
    
}
