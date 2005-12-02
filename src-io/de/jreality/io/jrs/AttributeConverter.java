package de.jreality.io.jrs;

import com.thoughtworks.xstream.converters.basic.AbstractBasicConverter;

import de.jreality.scene.data.Attribute;

class AttributeConverter extends AbstractBasicConverter {

  public boolean canConvert(Class type) {
      return type.equals(Attribute.class);
  }

  protected String toString(Object obj) {
    return ((Attribute)obj).getName();
  }
  
  protected Object fromString(String str) {
    return Attribute.attributeForName(str);
  }

}