/*
 * Created on Jan 12, 2005
 *
 */
package de.jreality.shader;

import java.awt.Color;

import de.jreality.scene.Appearance;
import de.jreality.scene.data.AttributeEntityUtility;

/**
 * @author gunn
 * 
 */
public class ShaderUtility {

  private ShaderUtility() {
  }

  public static Color combineDiffuseColorWithTransparency(Color diffuseColor,
      double transparency) {
    // LoggingSystem.getLogger().log(Level.FINE,"Input: c3, transparency:
    // "+diffuseColor.getAlpha()/255.0f+" "+transparency);
    double alpha = diffuseColor.getAlpha() / 255.0f;
    double alpha2 = 1.0 - transparency;
    alpha = alpha * alpha2;
    if (alpha < 0.0)
      alpha = 0.0;
    if (alpha > 1.0)
      alpha = 1.0;
    float[] f = diffuseColor.getRGBComponents(null);
    f[3] = (float) alpha;
    // LoggingSystem.getLogger().log(Level.FINE,"Alpha is "+alpha);
    Color ret = new Color(f[0], f[1], f[2], f[3]);
    // LoggingSystem.getLogger().log(Level.FINE,"f[3] is "+f[3]);
    // LoggingSystem.getLogger().log(Level.FINE,"Output: c3, alpha:
    // "+ret.getAlpha()/255.0f+" "+alpha);
    return ret;
  }

  public static String nameSpace(String s1, String s2) {
    return s1.length() == 0 ? s2 : s1 + '.' + s2;
  }

  public static DefaultGeometryShader createDefaultGeometryShader(Appearance a, boolean readDefaults) {
    return (DefaultGeometryShader) AttributeEntityUtility.createAttributeEntity(DefaultGeometryShader.class, "", a, readDefaults);
  }

  public static DefaultGeometryShader createDefaultGeometryShader(EffectiveAppearance ea) {
      return (DefaultGeometryShader) AttributeEntityUtility.createAttributeEntity(DefaultGeometryShader.class, "", ea);
    }

  public static RenderingHintsShader createDefaultRenderingHintsShader(Appearance a, boolean readDefaults) {
	    return (RenderingHintsShader) AttributeEntityUtility.createAttributeEntity(RenderingHintsShader.class, "", a, readDefaults);
	  }

  public static RenderingHintsShader createRenderingHintsShader(EffectiveAppearance ea) {
      return (RenderingHintsShader) AttributeEntityUtility.createAttributeEntity(RenderingHintsShader.class, "", ea);
    }

 public static RootAppearance createRootAppearance(Appearance a) {
      return (RootAppearance) AttributeEntityUtility.createAttributeEntity(RootAppearance.class, "", a, true);
    }
  
  public static Class resolveEntity(Class type, String name) {
    if (PointShader.class.isAssignableFrom(type)) {
      if (name.equals("default")) return DefaultPointShader.class;
      throw new IllegalArgumentException(" no such point shader ["+name+"]");
    }
    if (LineShader.class.isAssignableFrom(type)) {
      if (name.equals("default")) return DefaultLineShader.class;
      throw new IllegalArgumentException(" no such line shader ["+name+"]");
    }
    if (PolygonShader.class.isAssignableFrom(type)) {
      if (name.equals("twoSide")) return TwoSidePolygonShader.class;
      if (name.equals("default")) return DefaultPolygonShader.class;
      throw new IllegalArgumentException(" no such polygon shader ["+name+"]");
    }
    throw new IllegalArgumentException("unhandled entity class "+type);
  }
}
