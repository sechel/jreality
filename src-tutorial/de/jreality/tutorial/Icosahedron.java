package de.jreality.tutorial;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.geometry.Primitives;
import de.jreality.ui.viewerapp.ViewerApp;

public class Icosahedron {
  
  public static void main(String[] args) {
    
    SceneGraphComponent sgc = new SceneGraphComponent();
    sgc.setGeometry(Primitives.icosahedron());
    
    ViewerApp.display(sgc);
  }
}
