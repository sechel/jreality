/*
 * Created on Dec 5, 2003
 *
 * This file is part of the jReality package.
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

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class Geometries {
    public static class IndexedFaceSet {
        public double vertices[];
        public double normals[];
        public int faces[][];
        public int normalIndices[][];

        public IndexedFaceSet() {
            super();
        }
        public void apply(PolygonProcessor p) {
            for (int i= 0; i < faces.length; i++) {
                p.processPolygon(vertices, faces[i], normals, normalIndices[i]);
            }
        }
    }
    private static final int SPHERE_DETAIL= 20;
    private static IndexedFaceSet sphere;
    private static final int Cylinder_DETAIL= 20;
    private static IndexedFaceSet cylinder;
    /**
     * 
     */
    public static IndexedFaceSet unitSphere() {
        if (sphere == null) {
            sphere= new IndexedFaceSet();
            int d= SPHERE_DETAIL + 1;
            double r= 1;
            sphere.vertices= new double[d * d * 3];
            sphere.normals= new double[d * d * 3];
            sphere.faces= new int[(d - 1) * (d - 1)][4];
            sphere.normalIndices= sphere.faces;
            for (int i= 0; i < d; i++) {
                for (int j= 0; j < d; j++) {
                    double phi= i * Math.PI * 2. / (d - 1.);
                    double theta= j * Math.PI / (d - 1.) - Math.PI / 2.;
                    int pos= 3 * (i + d * j);
                    sphere.vertices[pos]= r * (sphere.normals[pos]=
                                              Math.cos(phi) * Math.cos(theta));
                    sphere.vertices[pos + 1]= r * (sphere.normals[pos + 1]=
                                              Math.sin(phi) * Math.cos(theta));
                    sphere.vertices[pos + 2]= r * (sphere.normals[pos + 2]=
                                              Math.sin(theta));
                }
            }
            for (int i= 0; i < d - 1; i++) {
                for (int j= 0; j < d - 1; j++) {
                    int pos= i + (d - 1) * j;
                    sphere.faces[pos][0]= 3 * (i + j * d);
                    sphere.faces[pos][1]= 3 * (i + 1 + j * d);
                    sphere.faces[pos][2]= 3 * (i + 1 + (j + 1) * d);
                    sphere.faces[pos][3]= 3 * (i + (j + 1) * d);
                }
            }
        }
        return sphere;
    }

    public static IndexedFaceSet cylinder() {
        int k = Cylinder_DETAIL;
        double r = 1;
        double h = 2;
        //rGeom.disk(k);
        //rGeom.vertices[k][2] = c.getHeight();
        
        if(cylinder == null) {
            cylinder = new IndexedFaceSet();
        
        cylinder.faces = new int[3*k][]; // k for the tube and 2 * (k) for the two caps
        for(int i = 0; i<k; i++) cylinder.faces[i]= new int[4]; // first the tube
        for(int i = k; i<3*k; i++) cylinder.faces[i]= new int[3];// and then the caps
        
        cylinder.vertices = new double[3*(2+4*k)]; // 2*k for the tube and 2 * (k + 1) for the two caps
        cylinder.normals = new double[3*(2+4*k)];
        
        double al = Math.PI/2. -Math.asin(r/Math.sqrt(r*r + h*h));
        double cosA = Math.cos(al);
        
        for (int i = 0 ; i < k ; i++) {
//            int ppos = 2*i;
//            cylinder.faces[i][0] = 3*ppos;
//            cylinder.faces[i][1] = 3*ppos+1;         
//            cylinder.faces[i][2] = 3*((ppos+3) % (2*k));
//            cylinder.faces[i][3] = 3*((ppos+2) %(2*k));
//            cylinder.faces[i][0] = 3*ppos;
            int ppos = 6*i;
            cylinder.faces[i][0] = ppos;         
            cylinder.faces[i][1] = ppos+3;         
            cylinder.faces[i][2] = (ppos+9) % (6*k);
            cylinder.faces[i][3] = (ppos+6) % (6*k);
            double theta = 2 * Math.PI * i / k;
            double cosT = Math.cos(theta);
            double sinT = Math.sin(theta);
            int pos = 6*i;
            cylinder.vertices[pos+0] = r*cosT;
            cylinder.vertices[pos+1] = r*sinT;
            cylinder.vertices[pos+2] =h/2;
            cylinder.vertices[pos+3] = r*cosT;
            cylinder.vertices[pos+4] = r*sinT;
            cylinder.vertices[pos+5] =-h/2;
            
            cylinder.normals[pos+0] =cosT ;
            cylinder.normals[pos+1] =sinT ;
            cylinder.normals[pos+2] = 0;
            cylinder.normals[pos+3] =cosT ;
            cylinder.normals[pos+4] =sinT ;
            cylinder.normals[pos+5] = 0;
        }
        // The caps:
        for (int i = 0 ; i < k ; i++) {
            int ppos = 2*k+i;
            cylinder.faces[i+k][0] = 3*(2*k + i);
            cylinder.faces[i+k][1] = 3*(2*k + ((i+1)%k));
            cylinder.faces[i+k][2] = 3*(4*k);

            cylinder.faces[i+2*k][0] = 3*(3*k + i);
            cylinder.faces[i+2*k][1] = 3*(3*k + ((i+1)%k));
            cylinder.faces[i+2*k][2] = 3*(4*k +1);

            double theta = 2 * Math.PI * i / k;
            double cosT = Math.cos(theta);
            double sinT = Math.sin(theta);

            int pos = 3*(2*k +i);           
            cylinder.vertices[pos+0] = r*cosT;
            cylinder.vertices[pos+1] = r*sinT;
            cylinder.vertices[pos+2] =h/2;
            cylinder.normals[pos+0] =0 ;
            cylinder.normals[pos+1] =0 ;
            cylinder.normals[pos+2] = 1;

            pos += 3*k;
            cylinder.vertices[pos+0] = r*cosT;
            cylinder.vertices[pos+1] = r*sinT;
            cylinder.vertices[pos+2] =-h/2;
            cylinder.normals[pos+0] =0 ;
            cylinder.normals[pos+1] =0 ;
            cylinder.normals[pos+2] = -1;

        }
        cylinder.vertices[3*(4*k)] = 0;
        cylinder.vertices[3*(4*k)+1] = 0;
        cylinder.vertices[3*(4*k)+2] = h/2;
        cylinder.vertices[3*(4*k+1)] = 0;
        cylinder.vertices[3*(4*k+1)+1] = 0;
        cylinder.vertices[3*(4*k+1)+2] = -h/2;
        cylinder.normals[3*(4*k)] = 0;
        cylinder.normals[3*(4*k)+1] = 0;
        cylinder.normals[3*(4*k)+2] = 1;
        cylinder.normals[3*(4*k+1)] = 0;
        cylinder.normals[3*(4*k+1)+1] = 0;
        cylinder.normals[3*(4*k+1)+2] = -1;
        
        cylinder.normalIndices = cylinder.faces;
    }
    return cylinder;
    }
 }
