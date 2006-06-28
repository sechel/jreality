package de.jreality.tutorial;

import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;

public class Cube01 {
  
  public static void main(String[] args) {
    
    PointSetFactory psf = new PointSetFactory();
    
    double [][] vertices = new double[][] {
      {0, 0, 0}, {1, 0, 0}, {1, 1, 0}, {0, 1, 0}
    };
    
    psf.setVertexCount( vertices.length );
    psf.setVertexCoordinates( vertices );
    
    psf.update();
    
    SceneGraphComponent sgc = new SceneGraphComponent();
    sgc.setGeometry(psf.getPointSet());
    
    ViewerApp.display(sgc);
  }
}
