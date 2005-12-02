package de.jreality.io.jrs;

import java.util.StringTokenizer;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

class DoubleArrayConverter implements Converter {

  Mapper mapper;

  public DoubleArrayConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == double[].class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    double[] data = (double[]) source;
    StringBuffer sb = new StringBuffer();
    for (int i = 0, n = data.length; i<n; i++)
      sb.append(data[i]).append(' ');
    sb.delete(sb.length()-1, sb.length());
    writer.setValue(sb.toString());
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    StringTokenizer st = new StringTokenizer(reader.getValue());
    double[] data = new double[st.countTokens()];
    for (int i = 0, n = data.length; i<n; i++)
      data[i] = Double.parseDouble(st.nextToken());
    return data;
  }

}