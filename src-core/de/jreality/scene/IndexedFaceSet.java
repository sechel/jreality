/*
 * Created on Dec 8, 2003
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
package de.jreality.scene;

import java.util.Collections;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.StorageModel;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class IndexedFaceSet extends IndexedLineSet {
  protected DataListSet faceAttributes;

  public IndexedFaceSet(int numVertices, int numFaces) {
    super(numVertices);
    faceAttributes= new DataListSet(numFaces);
  }

  public IndexedFaceSet() {
    this(8, 6);
    vertexAttributes.addWritable(Attribute.COORDINATES,
     StorageModel.DOUBLE3_ARRAY, new double[][] {
      { -1.00000e+00, -1.00000e+00, -1.00000e+00 },
      { 1.00000e+00, -1.00000e+00, -1.00000e+00 },
      { -1.00000e+00, 1.00000e+00, -1.00000e+00 },
      { 1.00000e+00, 1.00000e+00, -1.00000e+00 },
      { -1.00000e+00, -1.00000e+00, 1.00000e+00 },
      { 1.00000e+00, -1.00000e+00, 1.00000e+00 },
      { -1.00000e+00, 1.00000e+00, 1.00000e+00 },
      { 1.00000e+00, 1.00000e+00, 1.00000e+00 }});
    faceAttributes.addWritable(Attribute.INDICES,
      StorageModel.INT_ARRAY.array(4), new int[][] {
      { 2, 3, 1, 0 },
      { 7, 6, 4, 5 },
      { 6, 7, 3, 2 },
      { 5, 4, 0, 1 },
      { 3, 7, 5, 1 },
      { 4, 6, 2, 0 }});
  }

  public int getNumFaces() {
    startReader();
    try {
      return faceAttributes.getListLength();
    } finally {
      finishReader();
    }
  }

  /**
   * Sets the number of face, implies removal of all previously defined
   * face attributes.
   * @param numVertices the number of vertices to set >=0
   */
  public void setNumFaces(int numFaces) {
    checkReadOnly();
  	startWriter();
  	try {
        faceAttributes.reset(numFaces);
  	} finally {
  		finishWriter();
  	}
  }

  /**
   * Returns a read-only view to all currently defined face attributes.
   * You can copy all currently defined face attributes to another
   * IndexedFaceSet using
   * <code>target.setFaceAttributes(source.getFaceAttributes())</code>
   * These attributes are copied then, not shared. Thus modifying either
   * source or target afterwards will not affect the other.
   * @see setFaceAttributes(DataListSet)
   * @see getEdgeAttributes()
   * @see getVertexAttributes()
   * @see getGeometryAttributes()
   */
  public DataListSet getFaceAttributes() {
    startReader();
    try {
      return faceAttributes.readOnly();
    } finally {
      finishReader();
    }
  }

  public DataList getFaceAttributes(Attribute key) {
    startReader();
    try {
      return faceAttributes.getList(key);
    } finally {
      finishReader();
    }
  }

  public void setFaceAttributes(DataListSet dls) {
    checkReadOnly();
    startWriter();
    setAttrImpl(faceAttributes, dls, false);
    fireGeometryChanged(null, null, dls.storedAttributes(), null);
    finishWriter();
  }

  public void setFaceAttributes(Attribute attr, DataList dl) {
    checkReadOnly();
    startWriter();
    setAttrImpl(faceAttributes, attr, dl, false);
    fireGeometryChanged(null, null, Collections.singleton(attr), null);
    finishWriter();
  }

  public void setFaceCountAndAttributes(Attribute attr, DataList dl) {
    checkReadOnly();
    startWriter();
    setAttrImpl(faceAttributes, attr, dl, true);
    fireGeometryChanged(null, null, Collections.singleton(attr), null);
    finishWriter();
  }

  public void setFaceCountAndAttributes(DataListSet dls) {
    checkReadOnly();
    startWriter();
    setAttrImpl(faceAttributes, dls, true);
    fireGeometryChanged(null, null, dls.storedAttributes(), null);
    finishWriter();
  }

  public void accept(SceneGraphVisitor v) {
    startReader();
    try {
      v.visit(this);
    } finally {
      finishReader();
    }
  }
  static void superAccept(IndexedFaceSet ifs, SceneGraphVisitor v) {
    ifs.superAccept(v);
  }
  private void superAccept(SceneGraphVisitor v) {
    super.accept(v);
  }

}
