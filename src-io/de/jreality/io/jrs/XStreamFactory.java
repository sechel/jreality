package de.jreality.io.jrs;

import com.thoughtworks.xstream.XStream;

import de.jreality.scene.*;
import de.jreality.scene.data.*;

public class XStreamFactory {

  public static XStream forVersion(double version) {
    if (version < 1) return simpleXStream();
    else throw new IllegalArgumentException("no such version");
  }
  
  static XStream simpleXStream() {
    XStream ret = new XStream();
    
    // primitives
    ret.alias("d", double.class);
    ret.alias("b", byte.class);
    ret.alias("i", int.class);

    // scene package
    ret.alias("Appearance", Appearance.class);
    ret.alias("Camera", Camera.class);
    ret.alias("ClippingPlane", ClippingPlane.class);
    ret.alias("Cylinder", Cylinder.class);
    ret.alias("DirectionalLight", DirectionalLight.class);
    ret.alias("IndexedFaceSet", IndexedFaceSet.class);
    ret.alias("IndexedLineSet", IndexedLineSet.class);
    ret.alias("Light", Light.class);
    ret.alias("PointLight", PointLight.class);
    ret.alias("PointSet", PointSet.class);
    ret.alias("SceneGraphComponent", SceneGraphComponent.class);
    ret.alias("Sphere", Sphere.class);
    ret.alias("SpotLight", SpotLight.class);    
    ret.alias("Transformation", Transformation.class);

    // data package
    ret.alias("Attribute", Attribute.class);
    ret.alias("DataListSet", DataListSet.class);
    ret.alias("DoubleArray", DoubleArray.class);
    ret.alias("DoubleArrayArray", DoubleArrayArray.class);
    ret.alias("IntArray", IntArray.class);
    ret.alias("IntArrayArray", IntArrayArray.class);
    ret.alias("StorageModel", StorageModel.class);
    
    return ret;
  }
  
}
