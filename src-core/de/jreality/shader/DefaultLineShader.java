/*
 * Author	gunn
 * Created on Oct 19, 2005
 *
 */
package de.jreality.shader;

import java.awt.Color;

import de.jreality.geometry.TubeUtility;
import de.jreality.scene.data.AttributeEntity;

public interface DefaultLineShader extends LineShader {

  Object CREATE_DEFAULT=new Object();
  
	public final static boolean TUBE_DRAW_DEFAULT = true;
  public final static double TUBE_RADIUS_DEFAULT = 0.025;
  public final static int TUBE_STYLE_DEFAULT = TubeUtility.PARALLEL;
  public final static boolean VERTEX_COLORS_ENABLED_DEFAULT = false;
	public static final boolean INTERPOLATE_VERTEX_COLORS_DEFAULT = false;	// if true, then interpolate vertex colors
  public final static double LINE_WIDTH_DEFAULT = 1.0;
	public final static boolean LINE_STIPPLE_DEFAULT = false;
	public final static int LINE_STIPPLE_PATTERN_DEFAULT = 0x7e7e;
	public final static int LINE_FACTOR_DEFAULT = 	1;
	public static final Color DIFFUSE_COLOR_DEFAULT = Color.BLACK;
	
	public abstract Boolean getTubeDraw();
	public abstract void setTubeDraw(Boolean b);
	public abstract Double getTubeRadius();
	public abstract void setTubeRadius(Double d);
	public abstract Integer getTubeStyle();
	public abstract void setTubeStyle(Integer i);
	public abstract Double getLineWidth();
	public abstract void setLineWidth(Double d);
	public abstract Boolean getLineStipple();
	public abstract void setLineLineStipple(Boolean b);
	public abstract Integer getLineStipplePattern();
	public abstract void setLineLineStipplePattern(Integer i);
	public abstract Integer getLineFactor();
	public abstract void setLineLineFactor(Integer i);
	public abstract Boolean isVertexColorsEnabled();
	public abstract void setVertexColorsEnabled(Boolean d);
	public abstract Boolean getInterpolateVertexColors();
	public abstract void setInterpolateVertexColors(Boolean b);
	public abstract Color getDiffuseColor();
	public abstract void setDiffuseColor(Color c);
  
  PolygonShader getPolygonShader();
  PolygonShader createPolygonShader(String shaderName);

}