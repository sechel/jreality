package de.jreality.io.jrs;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

import de.jreality.io.JrScene;
import de.jreality.scene.*;
import de.jreality.scene.data.*;

public class XStreamFactory {

  public static XStream forVersion(double version) {
    if (version < 1) return simpleXStream();
    else throw new IllegalArgumentException("no such version");
  }
  
  static XStream simpleXStream() {

    XStream ret = new XStream(new PureJavaReflectionProvider());
    
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
    ret.alias("DataList", DataList.class);
    ret.alias("DataListSet", DataListSet.class);
    ret.alias("StorageModel", StorageModel.class);
    
    // io package
    ret.alias("Scene", JrScene.class);
    
    ret.registerConverter(new DataListSetConverter(ret.getClassMapper()));
    ret.registerConverter(new DataListConverter(ret.getClassMapper()));
    ret.registerConverter(new AttributeConverter(ret.getClassMapper()));
    ret.registerConverter(new DoubleArrayConverter(ret.getClassMapper()));
    ret.registerConverter(new IntArrayConverter(ret.getClassMapper()));
    ret.registerConverter(new SceneGraphNodeConverter(ret.getClassMapper()));
    ret.registerConverter(new InputSlotConverter(ret.getClassMapper()));
    return ret;
  }
  
}
