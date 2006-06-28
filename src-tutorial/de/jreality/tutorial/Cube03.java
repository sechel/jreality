package de.jreality.tutorial;

import de.jreality.scene.IndexedLineSet;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;

public class Cube03 {
  
  public static void main(String[] args) {
    
    double [][] vertices = new double[][] {
      {0, 0, 0}, {1, 0, 0}, {1, 1, 0}, {0, 1, 0}
    };
    
    IndexedLineSet ils = IndexedLineSetUtility.createCurveFromPoints(vertices, true);
    
    SceneGraphComponent sgc = new SceneGraphComponent();
    sgc.setGeometry(ils);
    
    ViewerApp.display(sgc);
  }
}
