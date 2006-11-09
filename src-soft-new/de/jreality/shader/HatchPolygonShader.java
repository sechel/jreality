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

/**
 * The default point shader for jReality. 
 * @author Charles Gunn
 * @see DefaultPolygonShader  for general remarks on these shader interfaces.
 *
 */public interface HatchPolygonShader extends DefaultPolygonShader {

    final static double AMBIENT_COEFFICIENT_DEFAULT = .0;

    final static Color AMBIENT_COLOR_DEFAULT = Color.WHITE;
    final static double DIFFUSE_COEFFICIENT_DEFAULT = 1.0;
    final static Color DIFFUSE_COLOR_DEFAULT = Color.BLUE;
    final static boolean SMOOTH_SHADING_DEFAULT = true;
    final static double SPECULAR_COEFFICIENT_DEFAULT = .7;
    final static Color SPECULAR_COLOR_DEFAULT = Color.WHITE;
    final static double SPECULAR_EXPONENT_DEFAULT = 60.;
    final static double TRANSPARENCY_DEFAULT = 0.0;
    Object CREATE_DEFAULT=new Object();

    TextShader createTextShader(String name);
    Texture2D createTexture2d();

    Double getAmbientCoefficient();
    Color getAmbientColor();

    Double getDiffuseCoefficient();
    Color getDiffuseColor();

    Boolean getSmoothShading();
    Double getSpecularCoefficient();

    Color getSpecularColor();
    Double getSpecularExponent();

    TextShader getTextShader();
    Texture2D getTexture2d();

    Double getTransparency();
    void setAmbientCoefficient(Double d);

    void setAmbientColor(Color c);
    void setDiffuseCoefficient(Double d);

    void setDiffuseColor(Color c);
    void setSmoothShading(Boolean b);

    void setSpecularCoefficient(Double d);
    void setSpecularColor(Color c);

    void setSpecularExponent(Double d);
    void setTransparency(Double d);
}