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
