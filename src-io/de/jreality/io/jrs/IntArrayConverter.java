package de.jreality.io.jrs;

import java.util.StringTokenizer;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

class IntArrayConverter implements Converter {

  Mapper mapper;

  public IntArrayConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == int[].class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    int[] data = (int[]) source;
    StringBuffer sb = new StringBuffer();
    for (int i = 0, n = data.length; i<n; i++)
      sb.append(data[i]).append(' ');
    sb.delete(sb.length()-1, sb.length());
    writer.setValue(sb.toString());
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    StringTokenizer st = new StringTokenizer(reader.getValue());
    int[] data = new int[st.countTokens()];
    for (int i = 0, n = data.length; i<n; i++)
      data[i] = Integer.parseInt(st.nextToken());
    return data;
  }
}
