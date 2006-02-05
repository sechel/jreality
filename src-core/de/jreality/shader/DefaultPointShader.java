/*
 * Author	gunn
 * Created on Oct 19, 2005
 *
 */
package de.jreality.shader;

import java.awt.Color;

import de.jreality.geometry.TubeUtility;
import de.jreality.scene.data.AttributeEntity;

public interface DefaultPointShader extends PointShader {

  Object CREATE_DEFAULT=new Object();

	public static final Color DIFFUSE_COLOR_DEFAULT = Color.RED;
  public final static boolean SPHERES_DRAW_DEFAULT = true;
  public final static boolean NORMALS_DRAW_DEFAULT = false;
  public final static double POINT_RADIUS_DEFAULT = 0.025;
  public final static double POINT_SIZE_DEFAULT = 3.0;
  
	public abstract Color getDiffuseColor();
	public abstract void setDiffuseColor(Color c);
  
  Boolean getSpheresDraw();
  void setShperesDraw(Boolean value);
  
  Boolean getNormalsDraw();
  void setNormalsDraw(Boolean value);
  
  Double getPointRadius();
  void setPointRadius(Double radius);
  
  Double getPointSize();
  void setPointSize(Double size);
  
  PolygonShader getPolygonShader();
  PolygonShader createPolygonShader(String shaderName);
  
}