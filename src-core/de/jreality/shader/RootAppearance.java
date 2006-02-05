package de.jreality.shader;

import java.awt.Color;

import de.jreality.scene.data.AttributeEntity;

public interface RootAppearance extends AttributeEntity {

  public final static Color  BACKGROUND_COLOR_DEFAULT = new java.awt.Color(225, 225, 225);
  public final static boolean  FOG_ENABLED_DEFAULT = false;
  public final static Color  FOG_COLOR_DEFAULT = new java.awt.Color(225, 225, 225);
  public final static double FOG_DENSITY_DEFAULT = 0.4;


  Color getBackgroundColor();
  void setBackgroundColor(Color color);
  
  Boolean getFogEnabled();
  void setFogEnabled(Boolean fog);
  
  Color getFogColor();
  void setFogColor(Color fogColor);
  
  Double getFogDensity();
  void setFogDensitiy(Double density);
}
