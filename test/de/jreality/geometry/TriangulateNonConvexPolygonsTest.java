/*
 * Created on Mar 17, 2005
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
package de.jreality.geometry;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class TriangulateNonConvexPolygonsTest extends TestCase {

  public static void main(String[] args) {
    junit.swingui.TestRunner.run(TriangulateNonConvexPolygonsTest.class);
  }
  
  public void testSearchIndex() {
    // IFS with vertexes per face: 3, 5, 4
    int[] triagIndex=new int[]{1,4,6};
    assertEquals(TriangulateNonConvexPolygons.resolveFaceForTriangle(triagIndex, 0), 0);
    assertEquals(TriangulateNonConvexPolygons.resolveFaceForTriangle(triagIndex, 3), 1);
    assertEquals(TriangulateNonConvexPolygons.resolveFaceForTriangle(triagIndex, 5), 2);
    assertEquals(TriangulateNonConvexPolygons.resolveFaceForTriangle(triagIndex, 6), 2);
  }
  public void testCreateIndex() {
    IndexedFaceSet ifs = new IndexedFaceSet();
    int[][] index = new int[][]{
        {1, 2, 3},
        {3, 6, 7, 5, 2},
        {7, 3, 5, 6}
    };
    ifs.setFaceCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(index));
    int[] triagIndex = TriangulateNonConvexPolygons.calculateTriangleIndex(ifs);
    assertEquals(triagIndex, new int[]{1,4,6});
  }

  public static void assertEquals(int[] i1, int[] i2) {
    if (i1 == null || i2 == null) if (i1 != i2) throw new AssertionFailedError("one array is null");
    if (i1.length != i2.length) throw new AssertionFailedError("lengths differ");
    for (int i = 0; i < i1.length; i++)
      assertEquals(i1[i], i2[i]);
  }

}
