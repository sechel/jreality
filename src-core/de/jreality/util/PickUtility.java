package de.jreality.util;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.pick.bounding.AABBTree;

public class PickUtility {

  private static final int DEFAULT_TRIANGLES_PER_BOX = 10;

  private PickUtility() {}
  
  public static void assignFaceAABBTree(IndexedFaceSet ifs) {
    assignFaceAABBTree(ifs, DEFAULT_TRIANGLES_PER_BOX);
  }
  
  public static void assignFaceAABBTree(IndexedFaceSet ifs, int maxTrianglesPerBox) {
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

}
