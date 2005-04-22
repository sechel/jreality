/*
 * Created on Apr 20, 2005
 *
 * This file is part of the de.jreality.util package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.util;

import java.awt.Color;

import de.jreality.geometry.TubeUtility;

/**
 * @author weissman
 *
 **/
public interface DefaultShaderInterface extends AttributeEntityWriter {

  public final static Color  BACKGROUND_COLOR_DEFAULT = new java.awt.Color(225, 225, 225);
  public final static boolean FACE_DRAW_DEFAULT = true;
  public final static double LEVEL_OF_DETAIL_DEFAULT =      1.0;
  public final static boolean EDGE_DRAW_DEFAULT = true;
  public final static boolean VERTEX_DRAW_DEFAULT = false;
  public final static boolean SPHERES_DRAW_DEFAULT = true;
  public final static boolean NORMALS_DRAW_DEFAULT = false;
  public final static double POINT_RADIUS_DEFAULT = 0.025;
  public final static double POINT_SIZE_DEFAULT = 3.0;
  public final static Color POINT_DIFFUSE_COLOR_DEFAULT = Color.RED;
  public final static boolean TUBES_DRAW_DEFAULT = true;
  public final static double TUBE_RADIUS_DEFAULT = 0.025;
  public final static int TUBE_STYLE_DEFAULT = TubeUtility.PARALLEL;
  public final static boolean VERTEX_COLORS_ENABLED_DEFAULT = false;
  public final static double LINE_WIDTH_DEFAULT = 1.0;
  public final static Color LINE_DIFFUSE_COLOR_DEFAULT = Color.BLACK;
  public final static boolean INTERPOLATE_VERTEX_COLORS_DEFAULT = false;  // if true, then interpolate vertex colors
  public final static boolean SMOOTH_SHADING_DEFAULT = true;
  public final static double TRANSPARENCY_DEFAULT =  0.5;
  public final static Color  AMBIENT_COLOR_DEFAULT = Color.WHITE;
  public final static Color  DIFFUSE_COLOR_DEFAULT = Color.BLUE;
  public final static Color SPECULAR_COLOR_DEFAULT = Color.WHITE;    
  public final static double SPECULAR_EXPONENT_DEFAULT =  60.;
  public final static double AMBIENT_COEFFICIENT_DEFAULT =  .1;
  public final static double DIFFUSE_COEFFICIENT_DEFAULT =  1.0;
  public final static double SPECULAR_COEFFICIENT_DEFAULT =  .7;
  
  
  public abstract void setBackgroundColor(Color bgColor);
  public abstract Color getBackgroundColor();

  public abstract void setName(String name);
  public abstract String getName();
  // rendering hints
  public abstract void setLighting(boolean value);
  public abstract void setAntiAliasing(boolean value);
  public abstract void setBackFaceCulling(boolean value);
  public abstract void setIsFastAndDirty(boolean value);
  public abstract void setTransparent(boolean value);
  public abstract void setAtInfinity(boolean value);
  public abstract void setLevelOfDetail(double value);
  public abstract void setDepthFudgeFactor(double value);
  
  public abstract boolean getLighting();
  public abstract boolean getAntiAliasing();
  public abstract boolean getBackFaceCulling();
  public abstract boolean getIsFastAndDirty();
  public abstract boolean getTransparent();
  public abstract boolean getAtInfinity();
  public abstract double getLevelOfDetail();
  public abstract double getDepthFudgeFactor();
  
   // default geometry shader
  public abstract void setShowFaces(boolean value);
  public abstract void setShowLines(boolean value);
  public abstract void setShowPoints(boolean value);
  public abstract void setPointShader(Object shader);
  public abstract void setLineShader(Object shader);
  public abstract void setPolygonShader(Object shader);

  public abstract boolean getShowFaces();
  public abstract boolean getShowLines();
  public abstract boolean getShowPoints();
  //public abstract void setPointShader(Object shader);
  //public abstract void setLineShader(Object shader);
  //public abstract void setPolygonShader(Object shader);

  // default point shader
  public abstract void setSpheresDraw(boolean value);
  public abstract void setNormalsDraw(boolean value);
  public abstract void setPointRadius(double value);      // object coordinates
  public abstract void setPointSize(double value);        // pixel coordinates

  public abstract boolean getSpheresDraw();
  public abstract boolean getNormalsDraw();
  public abstract double getPointRadius();
  public abstract double getPointSize();
  
  // default line shader
  public abstract void setTubeDraw(boolean value);
  public abstract void setTubeRadius(double value);   // object coordinates
  public abstract void setTubeStyle(int style);    // object coordinates
  public abstract void setVertexColorsEnabled(boolean value);      // pixel coordinates
  public abstract void setLineWidth(double value);      // pixel coordinates
  public abstract void setNormalScale(double value);  // TODO: ???
  public abstract void setLineStipple(boolean value);
  public abstract void setLineFactor(double value);
  public abstract void setLineStipplePattern(int pattern);
  public abstract void setInterpolateVertexColors(boolean value); 
  
  public abstract boolean getTubeDraw();
  public abstract double getTubeRadius();   // object coordinates
  public abstract int getTubeStyle();    // object coordinates
  public abstract boolean getVertexColorsEnabled();      // pixel coordinates
  public abstract double getLineWidth();      // pixel coordinates
  public abstract double getNormalScale();
  public abstract boolean getLineStipple();
  public abstract double getLineFactor();
  public abstract int getLineStipplePattern();
  public abstract boolean getInterpolateVertexColors(); 
  
  // default polygon shader
  public abstract void setSmoothShading(boolean value);    // interpolate vertex shading values?
  public abstract void setTexture2d(Texture2DInterface tex);
  public abstract void setTransparency(double value);
  public abstract void setAmbientColor(Color color);
  public abstract void setDiffuseColor(Color color);
  public abstract void setSpecularColor(Color color);
  public abstract void setSpecularExponent(double value);
  public abstract void setAmbientCoefficient(double value);
  public abstract void setDiffuseCoefficient(double value);
  public abstract void setSpecularCoefficient(double value);
  
  public abstract boolean getSmoothShading();
  public abstract Texture2DInterface getTexture2d();
  public abstract double getTransparency();
  public abstract Color getAmbientColor();
  public abstract Color getDiffuseColor();
  public abstract Color getSpecularColor();
  public abstract double getSpecularExponent();
  public abstract double getAmbientCoefficient();
  public abstract double getDiffuseCoefficient();
  public abstract double getSpecularCoefficient();
  
  // namespace methods
  public DefaultShaderInterface polygonShader();
  public DefaultShaderInterface lineShader();
  public DefaultShaderInterface pointShader();
  public DefaultShaderInterface front();
  public DefaultShaderInterface back();
}