package de.jreality.io.jrs;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.StringTokenizer;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

class EncodedDoubleArrayConverter implements Converter {

  Mapper mapper;

  public EncodedDoubleArrayConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == double[].class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    double[] data = (double[]) source;
    if (data.length > XStreamFactory.PLAIN_ARRAY_LENGTH) {
      writer.startNode("bytes");
      ByteBuffer bb = ByteBuffer.allocate(data.length*8);
      DoubleBuffer db = bb.asDoubleBuffer();
      db.put(data);
      context.convertAnother(bb.array());
      writer.endNode();
    } else {
      StringBuffer sb = new StringBuffer();
      for (int i = 0, n = data.length; i<n; i++)
        sb.append(data[i]).append(' ');
      sb.delete(sb.length()-1, sb.length());
      writer.setValue(sb.toString());
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    if (reader.hasMoreChildren()) {
      reader.moveDown();
      byte[] bytes = (byte[]) context.convertAnother(null, byte[].class);
      reader.moveUp();
      DoubleBuffer db = ByteBuffer.wrap(bytes).asDoubleBuffer();
      double[] data = new double[db.capacity()];
      db.get(data);
      return data;
    } else {
      StringTokenizer st = new StringTokenizer(reader.getValue());
      double[] data = new double[st.countTokens()];
      for (int i = 0, n = data.length; i<n; i++)
        data[i] = Double.parseDouble(st.nextToken());
      return data;
    }
  }

}