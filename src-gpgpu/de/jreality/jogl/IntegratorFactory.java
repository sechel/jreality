/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.jreality.jogl;

import java.util.HashSet;
import java.util.Iterator;

/**
 * factory for easy creation of glsl code for runge kutta integration on the gpu
 * @author weissman
 *
 */
public class IntegratorFactory {

  private static final String EULER =
    "void main(void) {" +
    "  vec2 pos = gl_TexCoord[0].st;\n" + 
    "  vec4 pt = textureRect(values, pos);\n" + 
    "  vec4 res = pt + h*evaluateT0(pt);\n" + 
    "  if (r3) res.w = 1.;\n" + 
    "  gl_FragColor = res;\n" +
    "}\n";

  private static final String RK2 =
    "void main(void) {" +
    "  vec2 pos = gl_TexCoord[0].st;\n" + 
    "  vec4 pt = textureRect(values, pos);\n" + 
    "  vec4 k1 = h * evaluateT0(pt);\n" + 
    "  vec4 k2 = h * evaluateT0_H2(pt+k1/2.);\n" + 
    "  vec4 res = pt + k2;\n" + 
    "  if (r3) res.w = 1.;\n" + 
    "  gl_FragColor = res;\n" +
    "}\n";

  private static final String RK4 =
    "void main(void) {" +
    "  vec2 pos = gl_TexCoord[0].st;\n" +
    "  vec4 pt = textureRect(values, pos);\n" +
    "  vec4 k1 = h * evaluateT0(pt);\n" +
    "  vec4 k2 = h * evaluateT0_H2(pt+k1/2.);\n" +
    "  vec4 k3 = h * evaluateT0_H2(pt+k2/2.);\n" +
    "  vec4 k4 = h * evaluateT0_H(pt+k3);\n" +
    "  vec4 res = pt + (k1 + 2.*(k2 + k3) + k4)/6.;\n" +
    "  if (r3) res.w = 1.;\n" + 
    "  gl_FragColor = res;\n" +
    "}\n";
  
  private int order;
  private HashSet uniforms=new HashSet();
  private HashSet signatures=new HashSet();
  private HashSet methods=new HashSet();

  private IntegratorFactory(int order) {
    this.order=order;
    addUniform("h", "float");
    addUniform("r3", "bool");
    addUniform("values", "samplerRect");
  }
  

  /**
   * create a factory for runge kutta integration of order 2
   * @return the factory
   */
  public static IntegratorFactory euler() {
    return new IntegratorFactory(1);
  }

  /**
   * create a factory for runge kutta integration of order 2
   * @return the factory
   */
  public static IntegratorFactory rk2() {
    return new IntegratorFactory(2);
  }
  
  /**
   * create a factory for runge kutta integration of order 2
   * @return the factory
   */
  public static IntegratorFactory rk4() {
    return new IntegratorFactory(4);
  }

  /**
   * add a uniform parameter to the src code
   * @param name the name of the uniform param
   * @param type the type of the param
   */
  public void addUniform(String name, String type) {
    uniforms.add("uniform "+type+" "+name+";");
  }
  
  public void addMethod(String name, String retType, String params, String implementation) {
    String signature = retType+" "+name+"("+params+")";
    String impl = signature+"{\n"+implementation+"}\n";
    signatures.add(signature);
    methods.add(impl);
  }
  
  public void srcT0(String impl) {
    addMethod("evaluateT0", "vec4", "const vec4 point", impl);
  }

  public void srcT0_H2(String impl) {
    if (order < 2) throw new IllegalStateException("no such method for euler"); 
    addMethod("evaluateT0_H2", "vec4", "const vec4 point", impl);
  }

  public void srcT0_H(String impl) {
    if (order < 4) throw new IllegalStateException("no such method for order 2"); 
    addMethod("evaluateT0_H", "vec4", "const vec4 point", impl);
  }

  public void srcAll(String impl) {
    srcT0(impl);
    if (order > 1) srcT0_H2(impl);
    if (order > 2) srcT0_H(impl);
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (Iterator i = uniforms.iterator(); i.hasNext(); )
      sb.append(i.next()).append('\n');
    sb.append('\n');
    for (Iterator i = signatures.iterator(); i.hasNext(); )
      sb.append(i.next()).append(';').append('\n');
    sb.append('\n');
    sb.append(order < 4 ? order < 2 ? EULER : RK2 : RK4).append('\n');
    for (Iterator i = methods.iterator(); i.hasNext(); )
      sb.append(i.next()).append('\n');
    sb.append('\n');
    return sb.toString();
  }
}
