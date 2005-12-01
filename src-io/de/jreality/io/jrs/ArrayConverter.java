package de.jreality.io.jrs;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;

public class ArrayConverter implements Converter {

  Mapper mapper;
  
  Pattern pattern = Pattern.compile("(([0-9eE\\+\\-\\.])+ )*");
  
  public ArrayConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == double[].class ||
      type == int[].class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    if (source instanceof double[]) {
      double[] d = (double[]) source;
      StringBuffer sb = new StringBuffer();
      for (int i = 0, n = d.length; i < n; i++) {
        sb.append(d[i]).append(' ');
      }
      writer.startNode("d");
      writer.addAttribute("len", ""+d.length);
      writer.setValue(sb.toString());
      writer.endNode();
    } else if (source instanceof int[]) {
      int[] d = (int[]) source;
      StringBuffer sb = new StringBuffer();
      for (int i = 0, n = d.length; i < n; i++) {
        sb.append(d[i]).append(' ');
      }
      writer.startNode("i");
      writer.addAttribute("len", ""+d.length);
      writer.setValue(sb.toString());
      writer.endNode();
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader,
      UnmarshallingContext context) {
    String str = reader.getValue();
    StringTokenizer toki = new StringTokenizer(str, " ");
    int len = toki.countTokens();
    if (reader.getNodeName().equals("d")) {
      double[] data = new double[len];
      for (int i = 0; toki.hasMoreTokens(); i++) data[i]=Double.parseDouble(toki.nextToken());
      return data;
   } else if (reader.getNodeName().equals("i")) {
      int[] data = new int[len];
      for (int i = 0; toki.hasMoreTokens(); i++) data[i]=Integer.parseInt(toki.nextToken());
      return data;
    }
    throw new Error();
  }
  
}
