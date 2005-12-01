package de.jreality.io.jrs;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class DoubleArrayConverter implements Converter {

  Mapper mapper;
  
  Pattern pattern = Pattern.compile("(([0-9eE\\+\\-\\.])+ )*");
  
  public DoubleArrayConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == double[].class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    if (source == null) return;
    double[] d = (double[]) source;
    writer.startNode("len");
    writer.setValue(""+d.length);
    writer.endNode();
    StringBuffer sb = new StringBuffer();
    for (int i = 0, n = d.length; i < n; i++) {
      sb.append(d[i]).append(' ');
    }
    sb.delete(sb.length()-1, sb.length());
    writer.setValue(sb.toString());
  }

  public Object unmarshal(HierarchicalStreamReader reader,
      UnmarshallingContext context) {

    if (!reader.hasMoreChildren()) return null;
    
    reader.moveDown();
    int len = Integer.parseInt(reader.getValue());
    reader.moveUp();
    double[] data = new double[len];
    
    String str = reader.getValue();
    StringTokenizer toki = new StringTokenizer(str, " ");
    for (int i = 0; toki.hasMoreTokens(); i++) data[i]=Double.parseDouble(toki.nextToken());
    return data;
  }
  
}
