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


package de.jreality.util;

import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.pick.AABBTree;
import de.jreality.shader.CommonAttributes;

public class PickUtility {

  private static final int DEFAULT_TRIANGLES_PER_BOX = 10;

  private PickUtility() {}
  
  /*
   * Do thiese methods perhaps belong in the de.jreality.pick package
   */
  public static void assignFaceAABBTree(IndexedFaceSet ifs) {
    assignFaceAABBTree(ifs, DEFAULT_TRIANGLES_PER_BOX);
  }
  
  public static void assignFaceAABBTree(IndexedFaceSet ifs, int maxTrianglesPerBox) {
	if (ifs.getNumFaces() == 0) return;
    ifs.setGeometryAttributes("AABBTree", AABBTree.construct(ifs, maxTrianglesPerBox));
  }
  public static void assignFaceAABBTrees(final SceneGraphComponent comp) {
    assignFaceAABBTrees(comp, DEFAULT_TRIANGLES_PER_BOX);
  }
  
  public static void assignFaceAABBTrees(final SceneGraphComponent comp, final int maxTrianglesPerBox) {
    comp.accept(new SceneGraphVisitor() {
      public void visit(SceneGraphComponent c) {
        if (c.getGeometry() != null) {
          if (c.getGeometry() instanceof IndexedFaceSet)
            assignFaceAABBTree((IndexedFaceSet) c.getGeometry(), maxTrianglesPerBox);
        }
        c.childrenAccept(this);
      }
    });
  }

  /**
   * sets the pickable flag for the whole sub-tree of <code>cmp</code>.
   * 
   * @param cmp the root node
   * TODO: decide if this belongs in SceneGraphUtility
   */
  public static void setPickable(SceneGraphComponent cmp, final boolean pickable) {
    cmp.accept(new SceneGraphVisitor() {
      public void visit(SceneGraphComponent c) {
        c.childrenWriteAccept(this, false, false, false, false, true, false);
      };
      public void visit(de.jreality.scene.Geometry g) {
        setPickable(g, pickable);
      };
    });
  }

  public static void setPickable(Geometry g, boolean pickable) {
    g.setGeometryAttributes(CommonAttributes.PICKABLE, pickable);
  }

}
