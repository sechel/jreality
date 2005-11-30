package de.jreality.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import de.jreality.io.JrScene;
import de.jreality.scene.SceneGraphComponent;

public interface SceneWriter {

  void writeScene(JrScene scene, OutputStream out) throws IOException;
  void writeScene(JrScene scene, Writer out) throws IOException;
  
}
