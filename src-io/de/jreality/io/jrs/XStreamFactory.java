package de.jreality.io.jrs;

import java.awt.Color;
import java.awt.Font;
import java.util.HashSet;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.io.JrScene;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.Quaternion;
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
import de.jreality.scene.tool.DraggingTool;
import de.jreality.scene.tool.EncompassTool;
import de.jreality.scene.tool.FlyTool;
import de.jreality.scene.tool.HeadTransformationTool;
import de.jreality.scene.tool.InputSlot;
//import de.jreality.scene.tool.PortalHeadMoveTool;
import de.jreality.scene.tool.RotateTool;
import de.jreality.scene.tool.ShipNavigationTool;
import de.jreality.shader.CubeMap;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;

public class XStreamFactory {

  private static HashSet knownClasses = new HashSet();
  
  static {
    // primitives are accepted anyway
    knownClasses.add(Boolean.class);
    knownClasses.add(Byte.class);
    knownClasses.add(Double.class);
    knownClasses.add(Integer.class);
    knownClasses.add(Character.class);
    knownClasses.add(Float.class);
    knownClasses.add(Short.class);
    
    knownClasses.add(String.class);
    knownClasses.add(Class.class);
    
    knownClasses.add(Color.class);
    knownClasses.add(Font.class);
    
    knownClasses.add(Matrix.class);
    knownClasses.add(FactoredMatrix.class);
    knownClasses.add(Quaternion.class);
    
    knownClasses.add(ImageData.class);
    
    knownClasses.add(RotateTool.class);
    knownClasses.add(EncompassTool.class);
    knownClasses.add(DraggingTool.class);
    knownClasses.add(ShipNavigationTool.class);
    knownClasses.add(HeadTransformationTool.class);
    knownClasses.add(FlyTool.class);
    
    try {
		knownClasses.add(Class.forName("de.jreality.scene.tool.PortalHeadMoveTool"));
	} catch (ClassNotFoundException e) {
	}
  }
  
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
    ret.alias("scene", JrScene.class);
    
    // math package
    ret.alias("Matrix", Matrix.class);
    ret.alias("FactoredMatrix", FactoredMatrix.class);
    ret.alias("Quaternion", Quaternion.class);
    
    // immutable types
    ret.addImmutableType(InputSlot.class);
    ret.addImmutableType(Attribute.class);
    ret.addImmutableType(ImageData.class);
    
    ret.registerConverter(new JrSceneConverter(ret.getClassMapper()));
    ret.registerConverter(new SceneGraphNodeConverter(ret.getClassMapper()));
    ret.registerConverter(new SceneGraphPathConverter(ret.getClassMapper()));
    ret.registerConverter(new DataListSetConverter(ret.getClassMapper()));
    ret.registerConverter(new DataListConverter(ret.getClassMapper()));
    ret.registerConverter(new DoubleArrayConverter(ret.getClassMapper()));
    ret.registerConverter(new IntArrayConverter(ret.getClassMapper()));
    ret.registerConverter(new StringArrayConverter(ret.getClassMapper()));
    ret.registerConverter(new InputSlotConverter());
    ret.registerConverter(new MatrixConverter(ret.getClassMapper()));
    ret.registerConverter(new FontConverter(ret.getClassMapper()));
    
    return ret;
  }

  static Object readUnknown(HierarchicalStreamReader reader, UnmarshallingContext context, Mapper mapper) {
    Object ret = null;
    reader.moveDown();
    Class type = mapper.realClass(reader.getNodeName());
    ret = context.convertAnother(null, type);
    reader.moveUp();
    return ret;
  }

  static void writeUnknown(Object src, HierarchicalStreamWriter writer, MarshallingContext context, Mapper mapper) {
    if (!canWrite(src)) throw new IllegalArgumentException("cannot write: ["+src.getClass()+"]");
    writer.startNode(mapper.serializedClass(src.getClass()));
    if (src != null) context.convertAnother(src);
    writer.endNode();
  }

  static boolean canWrite(Object val) {
    Class clazz = val.getClass();
    if (clazz.isPrimitive() || knownClasses.contains(clazz))
      return true;
    return false;
  }
  
}
