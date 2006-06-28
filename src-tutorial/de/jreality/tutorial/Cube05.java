package de.jreality.tutorial;

import java.awt.Color;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;

public class Cube05 {
  
  public static void main(String[] args) {
    
    IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
    
    double [][] vertices  = new double[][] {
      {0, 0, 0}, {1, 0, 0}, {1, 1, 0}, {0, 1, 0},
      {0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}
    };
    
    int [][] faceIndices = new int [][] {
      {0, 1, 2, 3}, {7, 6, 5, 4}, {0, 1, 5, 4}, 
      {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7} 
    };
    
    ifsf.setVertexCount( vertices.length );
    ifsf.setVertexCoordinates( vertices );
    ifsf.setFaceCount( faceIndices.length);
    ifsf.setFaceIndices( faceIndices );
    
    ifsf.setGenerateEdgesFromFaces( true );
    ifsf.setGenerateFaceNormals( true );

    ifsf.setFaceColors(new Color[]{
        Color.BLUE, Color.RED, Color.GREEN, Color.BLUE, Color.RED, Color.GREEN 
    });

        
    ifsf.update();
    
    SceneGraphComponent sgc = new SceneGraphComponent();
    sgc.setGeometry(ifsf.getIndexedFaceSet());
    
    ViewerApp.display(sgc);
  }
}
