package de.jreality.io.jrs;

import java.awt.Font;
import java.util.LinkedList;
import java.util.StringTokenizer;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

class FontConverter implements Converter {

  Mapper mapper;

  public FontConverter(Mapper mapper) {
    this.mapper = mapper;
  }

  public boolean canConvert(Class type) {
    return type == Font.class;
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Font f = (Font) source;
    writer.addAttribute("size", ""+f.getSize());
    writer.addAttribute("family", ""+f.getFamily());
    writer.addAttribute("name", ""+f.getName());
    writer.addAttribute("bold", ""+f.isBold());
    writer.addAttribute("italic", ""+f.isItalic());
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    int size = Integer.parseInt(reader.getAttribute("size"));
    String family = reader.getAttribute("family");
    String name = reader.getAttribute("name");
    boolean bold = Boolean.parseBoolean(reader.getAttribute("bold"));
    boolean italic = Boolean.parseBoolean(reader.getAttribute("italic"));
    int style = Font.PLAIN;
    if (bold) style = Font.BOLD;
    if (italic) style |= Font.ITALIC;
    return new Font(name, style, size);
  }
}
