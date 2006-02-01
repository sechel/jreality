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
    "  vec4 pt = textureRect(values, pos);\n" + 
    "  vec4 k1 = h * evaluateT0(pt);\n" + 
    "  vec4 k2 = h * evaluateT0_H2(pt+k1/2.);\n" + 
    "  gl_FragColor = pt + k2;\n" + 
    "}\n";
  }
  
  static String rk4Main() {
    return
    "void main(void) {\n" +
    "  vec2 pos = gl_TexCoord[0].st;\n" +
    "  vec4 pt = textureRect(values, pos);\n" +
    "  vec4 k1 = h * evaluateT0(pt);\n" +
    "  vec4 k2 = h * evaluateT0_H2(pt+k1/2.);\n" +
    "  vec4 k3 = h * evaluateT0_H2(pt+k2/2.);\n" +
    "  vec4 k4 = h * evaluateT0_H(pt+k3);\n" +
    "  gl_FragColor = pt + (k1 + 2.*(k2 + k3) + k4)/6.;\n" +
    "}\n";
  }
  
  static String rkUniforms() {
    return
    "uniform float h;\n"+
    "uniform samplerRect values;\n";
  }
  
  static String rk2MethodDeclarations() {
    return
    "vec4 evaluateT0(const vec4 point);\n"+
    "vec4 evaluateT0_H2(const vec4 point);\n";
  }
  
  static String rk4MethodDeclarations() {
    return rk2MethodDeclarations() + 
    "vec4 evaluateT0_H(const vec4 point);\n";
  }
}
