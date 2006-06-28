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

import de.jreality.geometry.TubeUtility;

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

  TextShader getTextShader();
  TextShader createTextShader(String name);
}