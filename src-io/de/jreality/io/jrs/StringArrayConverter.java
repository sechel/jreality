package de.jreality.io.jrs;

import java.util.LinkedList;
import java.util.StringTokenizer;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

class StringArrayConverter implements Converter {

  Mapper mapper;

  public StringArrayConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == String[].class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    String[] data = (String[]) source;
    for (int i = 0, n=data.length; i<n; i++) {
      writer.startNode(mapper.serializedClass(String.class));
      context.convertAnother(data[i]);
      writer.endNode();
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    LinkedList ll = new LinkedList();
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      String str = (String) context.convertAnother(null, String.class);
      ll.add(str);
      reader.moveUp();
    }
    return ll.toArray(new String[ll.size()]);
  }
}
