package de.jreality.io.jrs;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.StringTokenizer;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

class EncodedIntArrayConverter implements Converter {

  Mapper mapper;

  public EncodedIntArrayConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == int[].class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    int[] data = (int[]) source;
    if (data.length > XStreamFactory.PLAIN_ARRAY_LENGTH) {
      writer.startNode("bytes");
      ByteBuffer bb = ByteBuffer.allocate(data.length*4);
      IntBuffer ib = bb.asIntBuffer();
      ib.put(data);
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
      IntBuffer ib = ByteBuffer.wrap(bytes).asIntBuffer();
      int[] data = new int[ib.capacity()];
      ib.get(data);
      return data;
    } else {
      StringTokenizer st = new StringTokenizer(reader.getValue());
      int[] data = new int[st.countTokens()];
      for (int i = 0, n = data.length; i<n; i++)
        data[i] = Integer.parseInt(st.nextToken());
      return data;
    }
  }
}
