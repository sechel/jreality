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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
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

package de.jreality.scene.pick.bounding;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.PointSet;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.util.Rectangle3D;

public class AABB {

	/** Center of the Box. */
	final double[] center = new double[]{0, 0, 0};

	/** X axis of the Box. */
	final double[] xAxis = new double[]{1, 0, 0};

	/** Y axis of the Box. */
	final double[] yAxis = new double[]{0, 1, 0};

	/** Z axis of the Box. */
	final double[] zAxis = new double[]{0, 0, 1};

	/** Extents of the box along the x,y,z axis. */
	final double[] extent = new double[]{0, 0, 0};

  public static AABB construct(Geometry geom) {
    AABB ret = new AABB();
    if (geom instanceof Sphere || geom instanceof Cylinder) {
      Rn.setToValue(ret.extent, 1, 1, 1);
      return ret;
    }
    if (!(geom instanceof PointSet)) throw new IllegalArgumentException("unknown geometry!");
    ret.computeFromBoundingBox(GeometryUtility.calculateBoundingBox(
       (PointSet)geom 
    ));
    return ret;
  }
  
  public static void constructAndRegister(final PointSet ps) {
    final AABB aabb = construct(ps);
    ps.addGeometryListener(
      new GeometryListener() {
        public void geometryChanged(GeometryEvent ev) {
          if (ev.getChangedVertexAttributes().contains(Attribute.COORDINATES)) {
            aabb.computeFromBoundingBox(
                GeometryUtility.calculateBoundingBox((PointSet)ev.getGeometry())
            );
          }
        }
      }
    );
    ps.setGeometryAttributes(Attribute.attributeForName("AABB"), aabb);
    
  }
  
	public AABB transform(Matrix mat, AABB store) {
    FactoredMatrix m = new FactoredMatrix();
    m.assignFrom(mat);
    double[] scale = m.getStretch();
		store.extent[0]=extent[0] * scale[0];
    store.extent[1]=extent[1] * scale[1];
    store.extent[2]=extent[2] * scale[2];
    Matrix rot = m.getRotation();
		Rn.copy(store.xAxis, rot.multiplyVector(xAxis));
    Rn.copy(store.yAxis, rot.multiplyVector(yAxis));
    Rn.copy(store.zAxis, rot.multiplyVector(zAxis));
    Rn.copy(store.center, m.multiplyVector(center));
//    Rn.copy(store.center, mat.multiplyVector(center));
//    Rn.subtract(store.xAxis, store.center, mat.multiplyVector(xAxis));
//    Rn.subtract(store.yAxis, store.center, mat.multiplyVector(yAxis));
//    Rn.subtract(store.zAxis, store.center, mat.multiplyVector(zAxis));
//    store.extent[0] = Rn.euclideanNorm(xAxis) * extent[0];
//    store.extent[1] = Rn.euclideanNorm(yAxis) * extent[1];
//    store.extent[2] = Rn.euclideanNorm(zAxis) * extent[2];
//    Rn.normalize(store.xAxis, store.xAxis);
//    Rn.normalize(store.yAxis, store.yAxis);
//    Rn.normalize(store.zAxis, store.zAxis);
    return store;
	}
  
	final private double[] Z_AXIS=new double[]{0,0,1};
  /**
   * give two points for start and end in object coordinates...
   * @param points
   * @param eps sidelength of box in object coordinates
   */
  void computeFromEdge(double[][] points, double eps) {
    if (points.length != 2) throw new IllegalArgumentException("only two points supported");
    double len = Rn.euclideanDistance(points[0], points[1]);
    // first we set the bos as if the line goes from zert to positive z 
    Rn.setToValue(center, 0, 0, len/2);
    Rn.setToValue(extent, eps, eps, len/2);
    double[] newZ_Axis = Rn.subtract(null, points[1], points[0]);
    Rn.normalize(newZ_Axis, newZ_Axis);
    Matrix m = MatrixBuilder.euclidean().translate(points[0]).rotateFromTo(Z_AXIS, newZ_Axis).getMatrix();
    transform(m, this);
  }

  void computeFromBoundingBox(Rectangle3D bb) {
    Rn.copy(center, bb.getCenter());
    Rn.setToValue(xAxis, 1, 0, 0);
    Rn.setToValue(yAxis, 0, 1, 0);
    Rn.setToValue(zAxis, 0, 0, 1);
    Rn.copy(extent, bb.getExtent());
  }
  
	void computeFromPoints(double[][] points) {
		double[] min = Rn.copy(new double[3], points[0]);
    double[] max = Rn.copy(new double[3], min);
    
    for (int i = 1; i < points.length; i++) {
    	if (points[i][0] < min[0])
    		min[0] = points[i][0];
    	else if (points[i][0] > max[0])
    		max[0] = points[i][0];
    
    	if (points[i][1] < min[1])
    		min[1] = points[i][1];
    	else if (points[i][1] > max[1])
    		max[1] = points[i][1];
    
    	if (points[i][2] < min[2])
    		min[2] = points[i][2];
    	else if (points[i][2] > max[2])
    		max[2] = points[i][2];
    }
    
    Rn.times(center, 0.5, Rn.add(center, min, max));
    Rn.setToValue(extent, max[0] - center[0], max[1] - center[1], max[2] - center[2]);
    
//    if (extent[0] < Rn.TOLERANCE) extent[0] = 0.001 * Math.max(extent[1], extent[2]);
//    if (extent[1] < Rn.TOLERANCE) extent[1] = 0.001 * Math.max(extent[0], extent[2]);
//    if (extent[2] < Rn.TOLERANCE) extent[2] = 0.001 * Math.max(extent[0], extent[1]);

    Rn.setToValue(xAxis, 1, 0, 0);
    Rn.setToValue(yAxis, 0, 1, 0);
    Rn.setToValue(zAxis, 0, 0, 1);
	}

  public double[] getCenter(double[] store) {
    return Rn.copy(store, center);
  }

  public double[] getExtent(double[] store) {
    return Rn.copy(store, extent);
  }

	public void computeFromTris(AABBTree.TreePolygon[] tris, int start, int end) {
		double[] min = Rn.copy(null, tris[start].getVertices()[0]);
		double[] max = Rn.copy(null, min);
		double[] point;
		for (int i = start; i <= end; i++) {
		  double[][] points = tris[i].getVertices();
      for (int j = 0; j < points.length; j++) {
  			point = points[j];
  			if (point[0] < min[0])
  				min[0] = point[0];
  			else if (point[0] > max[0])
  				max[0] = point[0];
  			if (point[1] < min[1])
  				min[1] = point[1];
  			else if (point[1] > max[1])
  				max[1] = point[1];
  			if (point[2] < min[2])
  				min[2] = point[2];
  			else if (point[2] > max[2])
  				max[2] = point[2];
      }
    }
    Rn.times(center, 0.5, Rn.add(center, min, max));
    Rn.setToValue(extent, max[0] - center[0], max[1] - center[1], max[2] - center[2]);
    
    Rn.setToValue(xAxis, 1, 0, 0);
    Rn.setToValue(yAxis, 0, 1, 0);
    Rn.setToValue(zAxis, 0, 0, 1);
	}

  public void computeFromTris(AABBTree.TreePolygon[] tris, int start, int end, double eps) {
    double[] epsVec = new double[3];
    for (int i = 0; i < epsVec.length; i++) epsVec[i]=eps;
    computeFromTris(tris, start, end);
    Rn.add(extent, extent, epsVec);
  }

  double[] fWdU = new double[3];
  double[] fAWdU = new double[3];
  double[] fDdU = new double[3];
  double[] fADdU = new double[3];
  double[] fAWxDdU = new double[3];

  public boolean intersects(double[] from, double[] dir) {
    
    double rhs;    
    double[] diff = Rn.subtract(null, from, center);

    fWdU[0] = Rn.innerProduct(dir, xAxis);
    fAWdU[0] = Math.abs(fWdU[0]);
    fDdU[0] = Rn.innerProduct(diff, xAxis);
    fADdU[0] = Math.abs(fDdU[0]);

    fWdU[1] = Rn.innerProduct(dir, yAxis);
    fAWdU[1] = Math.abs(fWdU[1]);
    fDdU[1] = Rn.innerProduct(diff, yAxis);
    fADdU[1] = Math.abs(fDdU[1]);

    fWdU[2] = Rn.innerProduct(dir, zAxis);
    fAWdU[2] = Math.abs(fWdU[2]);
    fDdU[2] = Rn.innerProduct(diff, zAxis);
    fADdU[2] = Math.abs(fDdU[2]);

    if (fADdU[0] > extent[0] && fDdU[0] * fWdU[0] >= 0.0) {
      return false;
    }
    if (fADdU[1] > extent[1] && fDdU[1] * fWdU[1] >= 0.0) {
      return false;
    }
    if (fADdU[2] > extent[2] && fDdU[2] * fWdU[2] >= 0.0) {
      return false;
    }
    
    double[] wCrossD = Rn.crossProduct(null, dir, diff);
    fAWxDdU[0] = Math.abs(Rn.innerProduct(wCrossD, xAxis));
    rhs = extent[1] * fAWdU[2] + extent[2] * fAWdU[1];
    if (fAWxDdU[0] > rhs) {
      return false;
    }

    fAWxDdU[1] = Math.abs(Rn.innerProduct(wCrossD, yAxis));
    rhs = extent[0] * fAWdU[2] + extent[2] * fAWdU[0];
    if (fAWxDdU[1] > rhs) {
      return false;
    }

    fAWxDdU[2] = Math.abs(Rn.innerProduct(wCrossD, zAxis));
    rhs = extent[0] * fAWdU[1] + extent[1] * fAWdU[0];
    if (fAWxDdU[2] > rhs) {
      return false;
    }
    return true;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("AABB: \n").append("\tcenter: "+Rn.toString(center)).append('\n');
    sb.append("\textent: "+Rn.toString(extent)).append('\n');
    sb.append("\txAxis: "+Rn.toString(xAxis)).append('\n');
    sb.append("\tyAxis: "+Rn.toString(yAxis)).append('\n');
    sb.append("\tzAxis: "+Rn.toString(zAxis)).append('\n');
    return sb.toString();
  }

}