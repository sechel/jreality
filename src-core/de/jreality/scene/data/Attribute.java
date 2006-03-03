
package de.jreality.scene.data;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Holger
 */
public class Attribute implements Serializable {
  
  private static HashMap addedAttributes = new HashMap();

  public static final Attribute COORDINATES=attributeForName("coordinates");
  public static final Attribute NORMALS=attributeForName("normals");
  public static final Attribute COLORS=attributeForName("colors");
  public static final Attribute INDICES=attributeForName("indices");
  public static final Attribute TEXTURE_COORDINATES=attributeForName("texture coordinates");
  public static final Attribute POINT_SIZE=attributeForName("pointSize");
  public static final Attribute LABELS=attributeForName("labels");
  
  /**
   * might be a threading problem
   */
  public static Attribute attributeForName(String name) {
    if (addedAttributes.get(name) != null) return (Attribute) addedAttributes.get(name);
    Attribute att = new Attribute(name);
    addedAttributes.put(name, att);
    return att;
  }

  String attrName;

  private Attribute(String name) {
    attrName=name;
  }

  public String getName() {
    return attrName;
  }

  public String toString() {
    return attrName;
  }
  
  Object readResolve() throws ObjectStreamException
  {
    return attributeForName(getName());
  }
}
