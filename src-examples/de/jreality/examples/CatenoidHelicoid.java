/*
 * Created on Dec 30, 2003
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
package de.jreality.examples;

import java.util.Collections;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

/**
 * x = cos alpha sinh v sin u + sin alpha cosh v cos u
 * y = -cos alpha sinh v cos u + sin alpha cosh v sin u 
 * z = u cos alpha + v sin alpha
 * 
 * u \in [0,2 Pi[
 * 
 * alpha = 0 helicoid
 * alpha = Pi/2 catenoid
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class CatenoidHelicoid extends IndexedFaceSet {
  double[] vertices;
  double[] normals;
  double[] texCoords;
  
  private int d;

  double alpha= 0 * Math.PI / 2.4;
private int[][] faceIndices;
  /**
   * @param pointsProvider
   * @param vertexNormalsProvider
   * @param edgesProvider
   * @param facesProvider
   */
  public CatenoidHelicoid(int detail) {
	super(detail*detail, (detail-1)*(detail-1));
    int numPoints=getNumPoints(), numFaces=getNumFaces();
    this.d= detail;
    double r= 1;
    vertices= new double[numPoints * 3];
    normals= new double[numPoints * 3];
    texCoords= new double[numPoints * 2];
	faceIndices = new int[(detail-1)*(detail-1)][4];
	
    computeVertices();
    computeNormals();
    computeTexCoords();
    
    vertexAttributes.addWritable(Attribute.COORDINATES,
      StorageModel.DOUBLE3_INLINED, vertices);
    vertexAttributes.addWritable(Attribute.NORMALS,
            StorageModel.DOUBLE3_INLINED, normals);
    vertexAttributes.addWritable(Attribute.TEXTURE_COORDINATES,
            Attribute.TEXTURE_COORDINATES.getDefaultStorageModel(), texCoords);
	faceAttributes.addWritable(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY, faceIndices);
	
	IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(this);

  }

  private void generateFaceIndices() {
	  
    final int uLineCount = d;
	final int vLineCount = d;
	
	final int numUFaces = uLineCount-1;
	final int numVFaces = vLineCount-1;
	
	final int numPoints = d*d;
	
	for (int i = 0, k=0; i<numVFaces; ++i) {
		for (int j = 0; j< numUFaces; ++j, k++)	{
			final int [] face = faceIndices[k];
			face[0] = (i * uLineCount + j);
			face[1] = (((i+1) * uLineCount) + j) % numPoints;
			face[2] = (((i+1) * uLineCount) + (j+1)%uLineCount) % numPoints;
			face[3] = ((i* uLineCount) + (j+1)%uLineCount) % numPoints;				
		}
	}
  }
  
  private void computeTexCoords() {
    for (int i= 0; i < d; i++) {
        for (int j= 0; j < d; j++) {
            int pos= 2 * (i + d * j);
            texCoords[pos] = i/(double)d;
            texCoords[pos+1] = j/(double)d;
        }
    }
  }

  private static final double sinh(final double x) {
    return .5 * (Math.exp(x) - Math.exp(-x));
  }
  private static final double cosh(final double x) {
    return .5 * (Math.exp(x) + Math.exp(-x));
  }

  private void computeVertices() {
    final double[] vert=vertices;
    startWriter();
    try {
      final double cosalpha=Math.cos(alpha);
      final double sinalpha=Math.sin(alpha);
      for (int i= 0; i < d; i++) {
        for (int j= 0; j < d; j++) {
          double u= i * Math.PI * 2. / (d - 1.);
          double v= 4. * j / (d - 1.) - 2;
          int pos= 3 * (i + d * j);
          /*
           * x = cos alpha sinh v sin u + sin alpha cosh v cos u
           * y = -cos alpha sinh v cos u + sin alpha cosh v sin u 
           * z = u cos alpha + v sin alpha
           */
          final double sinhV   =      sinh(v);
          final double coshV   =      cosh(v);
          final double cosU    = Math.cos( u);
          final double sinU    = Math.sin( u);
          vert[pos]=      cosalpha * sinhV * sinU + sinalpha * coshV * cosU;
          vert[pos + 1]= -cosalpha * sinhV * cosU + sinalpha * coshV * sinU;
          vert[pos + 2]= u * cosalpha + v * sinalpha;
        }
      }
      fireGeometryChanged(Collections.singleton(Attribute.COORDINATES), null, null, null);
    } finally {
      finishWriter();
    }	
  }

  private void computeNormals() {
    final double[] nn=new double[3];
    startWriter();
    try {
      for (int i= 0; i < d; i++) {
        for (int j= 0; j < d; j++) {
          double u= i * Math.PI * 2. / (d - 1.);
          double v= 4. * j / (d - 1.) - 2;
          int pos= 3 * (i + d * j);
      
          final double coshV= cosh(v);
          nn[0]= -Math.cos(u) * coshV;
          nn[1]= -Math.sin(u) * coshV;
          nn[2]= coshV * sinh(v);
          Rn.normalize(nn, nn);
          normals[pos  ] = nn[0];
          normals[pos+1] = nn[1];
          normals[pos+2] = nn[2];
        }
      }
      fireGeometryChanged(Collections.singleton(Attribute.NORMALS), null, null, null);
    } finally {
      finishWriter();
    }
  }

  public double getAlpha() {
    return alpha;
  }

  public void setAlpha(double alpha) {
    this.alpha= alpha;
    computeVertices();
  }

}
