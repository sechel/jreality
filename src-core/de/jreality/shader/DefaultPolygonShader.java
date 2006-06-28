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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
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


package de.jreality.shader;

import java.awt.Color;

public interface DefaultPolygonShader extends PolygonShader {

  Object CREATE_DEFAULT=new Object();

  final static boolean SMOOTH_SHADING_DEFAULT = true;
  final static double TRANSPARENCY_DEFAULT = 0.5;
  final static Color AMBIENT_COLOR_DEFAULT = Color.WHITE;
  final static Color DIFFUSE_COLOR_DEFAULT = Color.BLUE;
  final static Color SPECULAR_COLOR_DEFAULT = Color.WHITE;
  final static double SPECULAR_EXPONENT_DEFAULT = 60.;
  final static double AMBIENT_COEFFICIENT_DEFAULT = .0;
  final static double DIFFUSE_COEFFICIENT_DEFAULT = 1.0;
  final static double SPECULAR_COEFFICIENT_DEFAULT = .7;

  Boolean getSmoothShading();
  void setSmoothShading(Boolean b);

  Double getTransparency();
  void setTransparency(Double d);

  Color getAmbientColor();
  void setAmbientColor(Color c);

  Double getAmbientCoefficient();
  void setAmbientCoefficient(Double d);

  Color getDiffuseColor();
  void setDiffuseColor(Color c);

  Double getDiffuseCoefficient();
  void setDiffuseCoefficient(Double d);

  Color getSpecularColor();
  void setSpecularColor(Color c);

  Double getSpecularCoefficient();
  void setSpecularCoefficient(Double d);

  Double getSpecularExponent();
  void setSpecularExponent(Double d);

  Texture2D getTexture2d();
  Texture2D createTexture2d();

  TextShader getTextShader();
  TextShader createTextShader(String name);
}