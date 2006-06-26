package de.jreality.examples;

import java.awt.Color;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.ViewerApp;

public class SimpleExample {

  /**
   * @param args
   */
  public static void main(String[] args) {
    double[][] points = {{0, 1, 0},{1, 0, 0},{0, -1, 0}, {-1, 0, 0}};
    int[][] faces = {{0, 1, 2, 3}};

    IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();

    ifsf.setGenerateEdgesFromFaces(true); // or false
    ifsf.setGenerateFaceNormals(true);
    ifsf.setGenerateVertexNormals(false);

    ifsf.setVertexCount(4);
    ifsf.setFaceCount(1);
    ifsf.setVertexCoordinates(points);
    ifsf.setFaceIndices(faces);

    ifsf.update();

    IndexedFaceSet faceSet = ifsf.getIndexedFaceSet();
    
    //ViewerApp.display(faceSet);
    
    Appearance app = new Appearance();
//    app.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.green);
//    app.setAttribute(CommonAttributes.VERTEX_DRAW, true);
//    app.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, 0.07);
//    app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.yellow);
//    app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, 0.07);
    
    DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(app, true);
    dgs.setShowPoints(Boolean.TRUE);
    DefaultPolygonShader polyShader = (DefaultPolygonShader) dgs.getPolygonShader();
    DefaultPointShader pointShader = (DefaultPointShader) dgs.getPointShader();
    DefaultLineShader lineShader = (DefaultLineShader) dgs.getLineShader();
    
    polyShader.setDiffuseColor(Color.green);
    lineShader.setDiffuseColor(Color.yellow);
    lineShader.setTubeRadius(new Double(0.07));
    pointShader.setPointRadius(new Double(0.07));
    
    SceneGraphComponent cmp = new SceneGraphComponent();
    
    cmp.setAppearance(app);
    cmp.setGeometry(faceSet);
    
    System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
    ViewerApp.display(cmp);
  }

}
