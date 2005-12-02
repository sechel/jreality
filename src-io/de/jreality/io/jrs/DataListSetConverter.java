package de.jreality.io.jrs;

import java.util.Iterator;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

class DataListSetConverter implements Converter {

  Mapper mapper;

  public DataListSetConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return DataListSet.class.isAssignableFrom(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer,
      MarshallingContext context) {
    DataListSet dls = (DataListSet) source;
    writer.addAttribute("size", ""+dls.getListLength());
    for (Iterator iterator = dls.storedAttributes().iterator(); iterator.hasNext();) {
      
      Attribute a = (Attribute) iterator.next();

      DataList dl = dls.getList(a);
      writer.startNode(mapper.serializedClass(DataList.class));
      writer.addAttribute("attribute", a.toString());
      context.convertAnother(dl);
      writer.endNode();
      
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader,
      UnmarshallingContext context) {
    int len = Integer.parseInt(reader.getAttribute("size"));
    DataListSet ret = new DataListSet(len);
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      Attribute a = (Attribute) Attribute.attributeForName(reader.getAttribute("attribute"));
      DataList dl = (DataList) context.convertAnother(null, DataList.class);
      reader.moveUp();
      dl.copyTo(ret.addWritable(a, dl.getStorageModel()));
    }
    return ret;
  }

}
