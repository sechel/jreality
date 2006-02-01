package de.jreality.jogl;

/**
 * TODO: make a nice Builder-like factory to create a runge-kutta program
 * @author weissman
 *
 */
public class RungeKuttaGlslCode {

  private RungeKuttaGlslCode() {}
  
  static String rk2Main() {
    return
    "void main(void) {\n" + 
    "  vec2 pos = gl_TexCoord[0].st;\n" + 
    "  vec3 pt = textureRect(values, pos).xyz;\n" + 
    "  vec3 k1 = h * evaluateT0(pt);\n" + 
    "  vec3 k2 = h * evaluateT0_H2(pt+k1/2.);\n" + 
    "  gl_FragColor = vec4(pt + k2, 1);\n" + 
    "}\n";
  }
  
  static String rk4Main() {
    return
    "void main(void) {\n" +
    "  vec2 pos = gl_TexCoord[0].st;\n" +
    "  vec3 pt = textureRect(values, pos).xyz;\n" +
    "  vec3 k1 = h * evaluateT0(pt);\n" +
    "  vec3 k2 = h * evaluateT0_H2(pt+k1/2.);\n" +
    "  vec3 k3 = h * evaluateT0_H2(pt+k2/2.);\n" +
    "  vec3 k4 = h * evaluateT0_H(pt+k3);\n" +
    "  gl_FragColor = vec4(pt + (k1 + 2.*(k2 + k3) + k4)/6., 1);\n" +
    "}\n";
  }
  
  static String rkUniforms() {
    return
    "uniform float h;\n"+
    "uniform samplerRect values;\n";
  }
  
  static String rk2MethodDeclarations() {
    return
    "vec3 evaluateT0(const vec3 point);\n"+
    "vec3 evaluateT0_H2(const vec3 point);\n";
  }
  
  static String rk4MethodDeclarations() {
    return rk2MethodDeclarations() + 
    "vec3 evaluateT0_H(const vec3 point);\n";
  }
}
