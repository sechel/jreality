package de.jreality.io.jrs;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

import de.jreality.io.JrScene;
import de.jreality.jogl.shader.DefaultPointShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.Cylinder;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CubeMap;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;

public class XStreamFactory {

  static final int PLAIN_ARRAY_LENGTH = 16;

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
    
    // shader
    ret.alias("DefaultPolygonShader", DefaultPolygonShader.class);
    ret.alias("DefaultLineShader", DefaultLineShader.class);
    ret.alias("DefaultPointShader", DefaultPointShader.class);
    ret.alias("Texture2D", Texture2D.class);
    ret.alias("CubeMap", CubeMap.class);
    
    ret.alias("ImageData", ImageData.class);
    
    // io package
    ret.alias("Scene", JrScene.class);
    
    ret.registerConverter(new JrSceneConverter(ret.getClassMapper()));
    ret.registerConverter(new SceneGraphNodeConverter(ret.getClassMapper()));
    ret.registerConverter(new SceneGraphPathConverter(ret.getClassMapper()));
    ret.registerConverter(new DataListSetConverter(ret.getClassMapper()));
    ret.registerConverter(new DataListConverter(ret.getClassMapper()));
    ret.registerConverter(new EncodedDoubleArrayConverter(ret.getClassMapper()));
    ret.registerConverter(new EncodedIntArrayConverter(ret.getClassMapper()));
    ret.registerConverter(new InputSlotConverter());
    
    return ret;
  }
  
}
