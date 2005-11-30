package de.jreality.reader;

import java.io.IOException;

import com.thoughtworks.xstream.XStream;

import de.jreality.io.JrScene;
import de.jreality.io.jrs.XStreamFactory;
import de.jreality.util.Input;

public class ReaderJRS extends AbstractReader {

  JrScene read;
  
  public void setInput(Input input) throws IOException {
    super.setInput(input);
    XStream xstr = XStreamFactory.forVersion(0.1);
    read = (JrScene) xstr.fromXML(input.getReader());
    root = read.getSceneRoot();
  }
  
  public JrScene getScene() {
    return read;
  }
}
