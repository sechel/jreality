package de.jreality.shader;

public interface TwoSidePolygonShader extends PolygonShader {

  PolygonShader getFront();
  PolygonShader createFront(String shaderName);
  
  PolygonShader getBack();
  PolygonShader createBack(String shaderName);
  
}
