
package de.jreality.scene.data;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Holger
 */
public class Attribute implements Serializable {
  String attrName;
  StorageModel storageModel;
  public static final Attribute COORDINATES
    =new Attribute("coordinates", StorageModel.DOUBLE3_INLINED);
  public static final Attribute NORMALS
	=new Attribute("normals", StorageModel.DOUBLE3_INLINED);
  public static final Attribute COLORS
	=new Attribute("colors", StorageModel.DOUBLE3_INLINED);
  public static final Attribute INDICES
    =new Attribute("indices", StorageModel.INT_ARRAY_ARRAY);
  public static final Attribute TEXTURE_COORDINATES
  =new Attribute("texture coordinates", StorageModel.DOUBLE_ARRAY.inlined(2)) {
      public void toStringImpl(Object data, int index, StringBuffer target) {
          double[] d=(double[])data;
          index*=2;
          target.append('{').append(d[index]).append(", ")
          .append(d[index+1]).append(", ").append(d[index+2]).append(" }");
      }
  };
  // TODO: makes .inlined(1) sense?
  public static final Attribute POINT_SIZE
  =new Attribute("pointSize", StorageModel.DOUBLE_ARRAY.inlined(1)) {
      public void toStringImpl(Object data, int index, StringBuffer target) {
          double[] d=(double[])data;
          target.append('{').append(d[index]).append(" }");
      }
  };
  public static final Attribute LABELS = new Attribute("labels", StorageModel.STRING_ARRAY);
  
  private Attribute(String name, StorageModel defStore) {
    attrName= name;
    storageModel= defStore;
  }
  public static HashMap addedAttributes = new HashMap();

  public static Attribute attributeForName(String name)	{
    // HACK - TODO: remove default storage model
    if(name.equals("coordinates")) return Attribute.COORDINATES;
    if(name.equals("normals")) return Attribute.NORMALS;
    if(name.equals("colors")) return Attribute.COLORS;
    if(name.equals("indices")) return Attribute.INDICES;
    if(name.equals("texture coordinates")) return Attribute.TEXTURE_COORDINATES;
    if(name.equals("pointSize")) return Attribute.POINT_SIZE;
    if(name.equals("labels")) return Attribute.LABELS;
  	return attributeForName(name, null);
  }
  public static Attribute attributeForName(String name, StorageModel defaultSM)	{
  	if (addedAttributes.get(name) != null) return (Attribute) addedAttributes.get(name);
  	Attribute att = new Attribute(name, defaultSM);
  	addedAttributes.put(name, att);
  	return att;
  }
  public StorageModel getDefaultStorageModel() {
    return storageModel;
  }
  public String getName() {
    return attrName;
  }
  public String toString() {
    return attrName;
  }
  Object readResolve() throws ObjectStreamException
  {
    if(attrName.equals("coordinates")) return COORDINATES;
    if(attrName.equals("normals")) return NORMALS;
    if(attrName.equals("colors")) return COLORS;
    if(attrName.equals("indices")) return INDICES;
    if(attrName.equals("texture coordinates")) return TEXTURE_COORDINATES;
    if(attrName.equals("pointSize")) return POINT_SIZE;
    if(attrName.equals("labels")) return LABELS;
    return this;
  }
}
