package de.jreality.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;

import de.jreality.io.JrScene;
import de.jreality.io.jrs.XStreamFactory;
import de.jreality.scene.SceneGraphComponent;

public class WriterJRS implements SceneWriter {
  
  public void writeScene(JrScene scene, OutputStream out) throws IOException {
    writeScene(scene, new OutputStreamWriter(out));
  }

  public void writeScene(JrScene scene, Writer out) throws IOException {
    XStream xstr = XStreamFactory.forVersion(0.1);
    out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xstr.toXML(scene, out);
  }

}
