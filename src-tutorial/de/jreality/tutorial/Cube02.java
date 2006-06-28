package de.jreality.tutorial;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;

public class Cube02 {
  
  public static void main(String[] args) {
    
    IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
    
    double [][] vertices = new double[][] {
      {0, 0, 0}, {1, 0, 0}, {1, 1, 0}, {0, 1, 0}
    };
    
    int[][] edgeIndices = new int[][]{
      {0, 1}, {1, 2}, {2, 3}, {3, 0}  
    };
    
    ilsf.setVertexCount( vertices.length );
    ilsf.setVertexCoordinates( vertices );
    ilsf.setLineCount(edgeIndices.length);
    ilsf.setEdgeIndices(edgeIndices);
    
    ilsf.update();
    
    SceneGraphComponent sgc = new SceneGraphComponent();
    sgc.setGeometry(ilsf.getIndexedLineSet());
    
    ViewerApp.display(sgc);
  }
}
