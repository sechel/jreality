package de.jreality.io.jrs;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.math.Matrix;

class MatrixConverter implements Converter {

  Mapper mapper;

  public MatrixConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == Matrix.class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    double[] data = ((Matrix)source).getArray();
    context.convertAnother(data);
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    double[] data = (double[]) context.convertAnother(null, double[].class);
    return new Matrix(data);
  }

}