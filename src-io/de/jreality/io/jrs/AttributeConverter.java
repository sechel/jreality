package de.jreality.io.jrs;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

public class AttributeConverter implements Converter {

  Mapper mapper;
  
  public AttributeConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == Attribute.class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    writer.addAttribute("name", source.toString());
  }

  public Object unmarshal(HierarchicalStreamReader reader,
      UnmarshallingContext context) {
    String name = reader.getAttribute("name");
    return Attribute.attributeForName(name);
  }
  
}
