package de.jreality.io.jrs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StringArray;
import de.jreality.scene.data.StringArrayArray;

class DataListConverter implements Converter {

  Mapper mapper;
  
  Pattern arrayPattern = Pattern.compile("[^\\[\\]]*\\[\\]");
  Pattern arrayArrayPattern = Pattern.compile("[^\\[\\]]*\\[\\]\\[\\]");
  Pattern arrayArrayInlinedPattern = Pattern.compile("[^\\[\\]]*\\[\\]\\[([0-9])+\\]");
  
  
  public DataListConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return DataList.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    DataList dl = (DataList) source;
    String sm = dl.getStorageModel().toString();
    writer.addAttribute("data", sm);
    Object dst=null;
    if (sm.startsWith("double")) {
      if (isArray(sm) || isInlined(sm)) {
        double[] data = dl.toDoubleArray(null);
        dst = data;
      } else if (isArrayArray(sm)) {
        double[][] data = dl.toDoubleArrayArray(null);
        dst = data;
      }
    } else if (sm.startsWith("int")) {
      if (isArray(sm) || isInlined(sm)) {
        int[] data = dl.toIntArray(null);
        dst = data;
      } else if (isArrayArray(sm)) {
        int[][] data = dl.toIntArrayArray(null);
        dst = data;
      }
    } else if (sm.startsWith("String")) {
      if (isArray(sm) || isInlined(sm)) {
        String[] data = dl.toStringArray(null);
        dst = data;
      } else if (isArrayArray(sm)) {
        String[][] data = dl.toStringArrayArray(null);
        dst = data;
      }
    } else {
      throw new UnsupportedOperationException("cannot write: "+sm);
    }
    writer.startNode(mapper.serializedClass(DataList.class));
    context.convertAnother(dst);
    writer.endNode();
  }

  public Object unmarshal(HierarchicalStreamReader reader,
      UnmarshallingContext context) {
    String sm = reader.getAttribute("data");
    Object ret = null;
    reader.moveDown();
    if (sm.startsWith("double")) {
      if (isArray(sm)) {
        double[] data = (double[]) context.convertAnother(null, double[].class);
        ret = new DoubleArray(data);
      } else if (isInlined(sm)) {
        double[] data = (double[]) context.convertAnother(null, double[].class);
        ret =  new DoubleArrayArray.Inlined(data, slotLength(sm));
      } else if (isArrayArray(sm)) {
        double[][] data = (double[][]) context.convertAnother(null, double[][].class);
        ret = new DoubleArrayArray.Array(data);
      }
    } else if (sm.startsWith("int")) {
      if (isArray(sm)) {
        int[] data = (int[]) context.convertAnother(null, int[].class);
        ret = new IntArray(data);
      } else if (isInlined(sm)) {
        int[] data = (int[]) context.convertAnother(null, int[].class);
        ret = new IntArrayArray.Inlined(data, slotLength(sm));
      } else if (isArrayArray(sm)) {
        int[][] data = (int[][]) context.convertAnother(null, int[][].class);
        ret = new IntArrayArray.Array(data);
      }
    } else if (sm.startsWith("String")) {
      if (isArray(sm)) {
        String[] data = (String[]) context.convertAnother(null, String[].class);
        ret = new StringArray(data);
      } else if (isInlined(sm)) {
        String[] data = (String[]) context.convertAnother(null, String[].class);
        ret = new StringArrayArray.Inlined(data, slotLength(sm));
      } else if (isArrayArray(sm)) {
        String[][] data = (String[][]) context.convertAnother(null, String[][].class);
        ret = new StringArrayArray.Array(data);
      }
    } else {
      throw new UnsupportedOperationException("cannot read: "+sm);
    }
    reader.moveUp();
    return ret;
  }

  private int slotLength(String sm) {
    Matcher m = arrayArrayInlinedPattern.matcher(sm);
    if (!m.find()) throw new IllegalArgumentException("no length!");
    return Integer.parseInt(m.group(1));
  }

  private boolean isArrayArray(String sm) {
    return arrayArrayPattern.matcher(sm).matches();
  }

  private boolean isInlined(String sm) {
    return arrayArrayInlinedPattern.matcher(sm).matches();
  }

  private boolean isArray(String sm) {
    return arrayPattern.matcher(sm).matches();
  }
  
}
