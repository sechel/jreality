/*
 * Created on Apr 22, 2004
 *
 */
package de.jreality.jogl.shader;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.LoggingSystem;

/**
 * Utility class encapsulating the shader lookup algorithm.
 * Currently it will look for shader foo of type bar by looking for a class
 * named <code>de.jreality.jogl.FooBarShader</code>. There's no caching yet.
 */
public class ShaderLookup
{
			
  private ShaderLookup(){}
  private static Object lookup2(String shaderName, String type) {
	    Object ps;
	    try
	    {
	      final String clName="de.jreality.jogl.shader."+Character.toUpperCase(
	        shaderName.charAt(0))+shaderName.substring(1)+Character.toUpperCase(type.charAt(0))+type.substring(1);
	      JOGLConfiguration.getLogger().log(Level.FINEST, "attempt to load {0}", clName);
	      final Class cl= Class.forName(clName);
	      JOGLConfiguration.getLogger().log(Level.FINEST, "loaded {0}", cl);
	      ps=cl.newInstance();
	      JOGLConfiguration.getLogger().log(Level.FINEST, "instantiated {0}", cl);
	    }
	    catch(ClassNotFoundException ex)
	    {
	      type=Character.toUpperCase(type.charAt(0))+type.substring(1);
	      LoggingSystem.getLogger(ShaderLookup.class).warning("unsupported shader "+shaderName);
	      ps=new DefaultPolygonShader();
	    }
	    catch(Exception ex)
	    {
	      type=Character.toUpperCase(type.charAt(0))+type.substring(1);
	    	  LoggingSystem.getLogger(ShaderLookup.class).warning("shader "+shaderName+" failed");
	      ps=new DefaultPolygonShader();
	    }
	    return ps;
	  }
	  public static Shader getShaderAttr(
  	          EffectiveAppearance eAppearance, String base,  String type) {
		  return getShaderAttr(eAppearance, base, type, type);
	  }
	  
 	  public static Shader getShaderAttr(
  	          EffectiveAppearance eAppearance, String base,  String type, String attr) {
   	      // This returns the value of the string base+attr in the current effective appearance, or "default" if not set
 		  String vShader = (String)eAppearance.getAttribute(ShaderUtility.nameSpace(base, attr), "default");
  	      Shader vShaderImpl= (Shader) ShaderLookup.lookup2(vShader, type );
		  // Returns the value of base+attr+name, if it's set, or if not, gives base+attr  back.
  	      String vShaderName = (String)eAppearance.getAttribute(ShaderUtility.nameSpace(base, attr+"name"),
  	              ShaderUtility.nameSpace(base, attr));
  	      // initialize the shader with the prefix stem vShaderName
  	      vShaderImpl.setFromEffectiveAppearance(eAppearance, vShaderName);
  	      return vShaderImpl;
  	  }
}
