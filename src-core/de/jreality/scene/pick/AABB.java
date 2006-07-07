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


package de.jreality.scene.pick;

import de.jreality.math.Rn;

/**
 * 
 * An Axis Aligned Bounding Box.
 * 
 * @author Steffen Weissmann
 *
 */
class AABB {

	/** Center of the Box. */
  final double[] center = new double[]{0, 0, 0};

	/** X axis of the Box. */
  private final double[] xAxis = new double[]{1, 0, 0, 0};

	/** Y axis of the Box. */
  private final double[] yAxis = new double[]{0, 1, 0, 0};

	/** Z axis of the Box. */
  private final double[] zAxis = new double[]{0, 0, 1, 0};

	/** Extents of the box along the x,y,z axis. */
  final double[] extent = new double[]{0, 0, 0};

/*	AABB transform(Matrix mat, AABB store) {
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
    return store;
	}*/
  
  void computeFromTris(AABBTree.TreePolygon[] tris, int start, int end) {
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
    
	}

  private final double[] fAWdU = new double[3];
  private final double[] fAWxDdU = new double[3];

  boolean intersects(double[] from, double[] dir) {
    
    double rhs;    
    double[] diff = Rn.subtract(null, from, center);

    if (Math.abs(diff[0]) > extent[0] && diff[0] * dir[0] >= 0.0) {
      return false;
    }
    if (Math.abs(diff[1]) > extent[1] && diff[1] * dir[1] >= 0.0) {
      return false;
    }
    if (Math.abs(diff[2]) > extent[2] && diff[2] * dir[2] >= 0.0) {
      return false;
    }
    
    fAWdU[0] = Math.abs(dir[0]);
    fAWdU[1] = Math.abs(dir[1]);
    fAWdU[2] = Math.abs(dir[2]);    
    
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