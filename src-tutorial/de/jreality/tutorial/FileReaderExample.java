package de.jreality.tutorial;

import java.io.IOException;

import de.jreality.reader.Readers;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;

public class FileReaderExample {

  public static void main(String[] args) throws IOException {
    // see the documentation of Input.getInput(String resource)
    //
    // To read .bsh files, you need beanshell in the classpath, to
    // read .jrs files, you need xstream.jar and xpp3.jar.
    //
    SceneGraphComponent scp = Readers.read(Input.getInput(args[0]));
    ViewerApp.display(scp);
  }

}
