package de.jreality.shader;

import de.jreality.scene.data.AttributeCollection;

public interface DefaultGeometryShader extends AttributeCollection {

  static final Class DEFAULT_ENTITY = DefaultGeometryShader.class;
  
  public static final boolean SHOW_FACES_DEFAULT=true;
  public static final boolean SHOW_LINES_DEFAULT=true;
  public static final boolean SHOW_POINTS_DEFAULT=true;
 
  Boolean getShowFaces();
  void setShowFaces(Boolean faceDraw);

  Boolean getShowLines();
  void setShowLines(Boolean edgeDraw);
  
  Boolean getShowPoints();
  void setShowPoints(Boolean vertexDraw);
  
  PointShader getPointShader();
  PointShader createPointShader(String name);
  
  LineShader getLineShader();
  LineShader createLineShader(String name);

  PolygonShader getPolygonShader();
  PolygonShader createPolygonShader(String name);
 
  TextShader getTextShader();
  TextShader createTextShader(String name);
}
