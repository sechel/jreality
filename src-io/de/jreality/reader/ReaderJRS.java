package de.jreality.reader;

import java.io.IOException;

import com.thoughtworks.xstream.XStream;

import de.jreality.io.jrs.XStreamFactory;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;

public class ReaderJRS extends AbstractReader {

  public void setInput(Input input) throws IOException {
    super.setInput(input);
    XStream xstr = XStreamFactory.forVersion(0.1);
    root = (SceneGraphComponent) xstr.fromXML(input.getReader());
  }
  
}
