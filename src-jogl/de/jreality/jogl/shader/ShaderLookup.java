/*
 * Created on Apr 22, 2004
 *
 */
package de.jreality.jogl.shader;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * Utility class encapsulating the shader lookup algorithm.
 * Currently it will look for shader foo of type bar by looking for a class
 * named <code>de.jreality.soft.FooBarShader</code>. There's no caching yet.
 */
public class ShaderLookup
{
  private ShaderLookup(){}

  public static PolygonShader lookupPolygonShader(String name) {
    return (PolygonShader)lookup1(name, "Polygon");
  }
  public static VertexShader lookupVertexShader(String name) {
      return (VertexShader)lookup1(name, "Vertex");
  }
  
  public static LineShader lookupLineShader(String name) {
      return (LineShader)lookup1(name, "Line");
  }
  public static PointShader lookupPointShader(String name) {
      return (PointShader)lookup1(name, "Point");
  }
  
  private static Object lookup1(String shaderName, String type) {
    Object ps;
    try
    {
      final String clName="de.jreality.jogl.shader."+Character.toUpperCase(
        shaderName.charAt(0))+shaderName.substring(1)+type+"Shader";
      Logger.getLogger("de.jreality").log(Level.FINEST, "attempt to load {0}", clName);
      final Class cl= Class.forName(clName);
      Logger.getLogger("de.jreality").log(Level.FINEST, "loaded {0}", cl);
      ps=cl.newInstance();
      Logger.getLogger("de.jreality").log(Level.FINEST, "instantiated {0}", cl);
    }
    catch(ClassNotFoundException ex)
    {
      type=Character.toUpperCase(type.charAt(0))+type.substring(1);
      Logger.getLogger("de.jreality").log(Level.WARNING,
        "unsupported {0} shader {1}", new String[] {type, shaderName});
      ps=new DefaultPolygonShader();
    }
    catch(Exception ex)
    {
      type=Character.toUpperCase(type.charAt(0))+type.substring(1);
      Logger.getLogger("de.jreality").log(Level.WARNING,
        "{0} shader {1} failed {2}", new Object[] {type, shaderName, ex});
      ps=new DefaultPolygonShader();
    }
    return ps;
  }
  public static VertexShader getVertexShaderAttr(
  	    EffectiveAppearance eAppearance, String base, String attr) {

  	    String vShader = (String)eAppearance.getAttribute(ShaderUtility.nameSpace(base, attr), "default");
  	    String vShaderName = (String)eAppearance.getAttribute(ShaderUtility.nameSpace(base, attr+".name"),
  	      ShaderUtility.nameSpace(base, attr));
//  	    JOGLConfiguration.theLog.log(Level.FINE,vShaderName+" <= "+NameSpace.name(base, attr+".name")
//  	      +": "+NameSpace.name(base, attr));
  	    VertexShader vShaderImpl= ShaderLookup.lookupVertexShader(vShader);
  	    vShaderImpl.setFromEffectiveAppearance(eAppearance, vShaderName);
  	    return vShaderImpl;
  	  }

  	  public static PolygonShader getPolygonShaderAttr(
  	    EffectiveAppearance eAppearance, String base, String attr) {

  	    String vShader = (String)eAppearance.getAttribute(ShaderUtility.nameSpace(base, attr), "default");
  	    String vShaderName = (String)eAppearance.getAttribute(ShaderUtility.nameSpace(base, attr+".name"),
  	      ShaderUtility.nameSpace(base, attr));
  	    PolygonShader vShaderImpl= ShaderLookup.lookupPolygonShader(vShader);
  	    //JOGLConfiguration.theLog.log(Level.FINE,"base "+base+" attr "+attr+" vShaderName "+vShaderName+" vShader "+vShader);
  	    vShaderImpl.setFromEffectiveAppearance(eAppearance, vShaderName);
  	    return vShaderImpl;
  	  }

  	  public static LineShader getLineShaderAttr(
  	          EffectiveAppearance eAppearance, String base, String attr) {

  	      String vShader = (String)eAppearance.getAttribute(ShaderUtility.nameSpace(base, attr), "default");
  	      String vShaderName = (String)eAppearance.getAttribute(ShaderUtility.nameSpace(base, attr+".name"),
  	              ShaderUtility.nameSpace(base, attr));
  	      LineShader vShaderImpl= ShaderLookup.lookupLineShader(vShader);
  	      vShaderImpl.setFromEffectiveAppearance(eAppearance, vShaderName);
  	      return vShaderImpl;
  	  }
  	  public static PointShader getPointShaderAttr(
  	          EffectiveAppearance eAppearance, String base, String attr) {

  	      String vShader = (String)eAppearance.getAttribute(ShaderUtility.nameSpace(base, attr), "default");
  	      String vShaderName = (String)eAppearance.getAttribute(ShaderUtility.nameSpace(base, attr+".name"),
  	              ShaderUtility.nameSpace(base, attr));
  	      PointShader vShaderImpl= ShaderLookup.lookupPointShader(vShader);
  	      vShaderImpl.setFromEffectiveAppearance(eAppearance, vShaderName);
  	      return vShaderImpl;
  	  }
}
