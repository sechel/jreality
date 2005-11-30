package de.jreality.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;

import de.jreality.io.jrs.XStreamFactory;
import de.jreality.scene.SceneGraphComponent;

public class WriterJRS implements SceneWriter {
  
  public void writeScene(SceneGraphComponent root, OutputStream out) throws IOException {
    writeScene(root, new OutputStreamWriter(out));
  }

  public void writeScene(SceneGraphComponent root, Writer out) throws IOException {
    XStream xstr = XStreamFactory.forVersion(0.1);
    xstr.toXML(root, out);
  }

}
