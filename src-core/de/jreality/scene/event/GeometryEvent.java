
package de.jreality.scene.event;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.jreality.scene.Geometry;

/**
 * The event object containing information about the changed
 * geometry data. The event contains the attribute keys of changed
 * geometry features. The key can be used to query the actual values
 * from the source geometry.
 * @author pietsch
 */
public class GeometryEvent extends SceneEvent {
    private final Geometry geometry;
    private Set vertexKeys;
    private Set vertexView;
    private Set edgeKeys;
    private Set edgeView;
    private Set faceKeys;
    private Set faceView;
    private Set geomKeys;
    private Set geomView;
    /**
     * @param source
     */
    public GeometryEvent(Geometry source, Set chgVertexAttrKeys,
      Set chgEdgeAttrKeys, Set chgFaceAttrKeys, Set chgGeomAttrKeys) {
        super(source);
        geometry=source;
        vertexKeys=chgVertexAttrKeys!=null?
          new HashSet(chgVertexAttrKeys): Collections.EMPTY_SET;
        edgeKeys  =chgEdgeAttrKeys!=null?
          new HashSet(chgEdgeAttrKeys):   Collections.EMPTY_SET;
        faceKeys  =chgFaceAttrKeys!=null?
          new HashSet(chgFaceAttrKeys):   Collections.EMPTY_SET;
        geomKeys  =chgGeomAttrKeys!=null?
          new HashSet(chgGeomAttrKeys):   Collections.EMPTY_SET;
    }

    public Set getChangedVertexAttributes() {
      if(vertexView==null)
        vertexView=Collections.unmodifiableSet(vertexKeys);
      return vertexView;
    }

    public Set getChangedEdgeAttributes() {
      if(edgeView==null)
        edgeView=Collections.unmodifiableSet(edgeKeys);
      return edgeView;
    }
  
    public Set getChangedFaceAttributes() {
      if(faceView==null)
        faceView=Collections.unmodifiableSet(faceKeys);
      return faceView;
    }
  
    public Set getChangedGeometryAttributes() {
      if(geomView==null)
        geomView=Collections.unmodifiableSet(geomKeys);
      return geomView;
    }

    public Geometry getGeometry() {
      return geometry;
    }

}
