package de.jreality.io.jrs;

import com.thoughtworks.xstream.converters.basic.AbstractBasicConverter;

import de.jreality.scene.tool.InputSlot;

class InputSlotConverter extends AbstractBasicConverter {

  public boolean canConvert(Class type) {
      return type.equals(InputSlot.class);
  }

  protected String toString(Object obj) {
    return ((InputSlot)obj).getName();
  }
  
  protected Object fromString(String str) {
    return InputSlot.getDevice(str);
  }

}

