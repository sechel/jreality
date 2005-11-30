package de.jreality.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import de.jreality.scene.SceneGraphComponent;

public interface SceneWriter {

  void writeScene(SceneGraphComponent root, OutputStream out) throws IOException;
  void writeScene(SceneGraphComponent root, Writer out) throws IOException;
  
}
