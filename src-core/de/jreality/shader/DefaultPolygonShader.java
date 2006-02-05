/*
 * Author	gunn
 * Created on Oct 19, 2005
 *
 */
package de.jreality.shader;

import java.awt.Color;

import de.jreality.scene.data.AttributeEntity;

public interface DefaultPolygonShader extends PolygonShader {

  Object CREATE_DEFAULT=new Object();

  final static boolean SMOOTH_SHADING_DEFAULT = true;
  final static double TRANSPARENCY_DEFAULT = 0.5;
  final static Color AMBIENT_COLOR_DEFAULT = Color.WHITE;
  final static Color DIFFUSE_COLOR_DEFAULT = Color.BLUE;
  final static Color SPECULAR_COLOR_DEFAULT = Color.WHITE;
  final static double SPECULAR_EXPONENT_DEFAULT = 60.;
  final static double AMBIENT_COEFFICIENT_DEFAULT = .0;
  final static double DIFFUSE_COEFFICIENT_DEFAULT = 1.0;
  final static double SPECULAR_COEFFICIENT_DEFAULT = .7;

  Boolean getSmoothShading();
  void setSmoothShading(Boolean b);

  Double getTransparency();
  void setTransparency(Double d);

  Color getAmbientColor();
  void setAmbientColor(Color c);

  Double getAmbientCoefficient();
  void setAmbientCoefficient(Double d);

  Color getDiffuseColor();
  void setDiffuseColor(Color c);

  Double getDiffuseCoefficient();
  void setDiffuseCoefficient(Double d);

  Color getSpecularColor();
  void setSpecularColor(Color c);

  Double getSpecularCoefficient();
  void setSpecularCoefficient(Double d);

  Double getSpecularExponent();
  void setSpecularExponent(Double d);

  Texture2D getTexture2d();
  Texture2D createTexture2d();
}