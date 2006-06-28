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


package de.jreality.reader.mathematica;
import java.awt.*;
import java.util.*;
import de.jreality.geometry.*;
import de.jreality.math.*;
import de.jreality.scene.data.*;
import de.jreality.scene.*;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.*;
import de.jreality.util.LoggingSystem;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;

public interface MathematicaParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	// "Graphics3D" = 4
	int OPEN_BRACKET = 5;
	int CLOSE_BRACKET = 6;
	int OPEN_BRACE = 7;
	int CLOSE_BRACE = 8;
	int COLON = 9;
	int LITERAL_Cuboid = 10;
	int LITERAL_Text = 11;
	int STRING = 12;
	int LITERAL_Point = 13;
	int LITERAL_Line = 14;
	int LITERAL_Polygon = 15;
	int LITERAL_SurfaceColor = 16;
	int LITERAL_RGBColor = 17;
	int LITERAL_Hue = 18;
	int LITERAL_GrayLevel = 19;
	int LITERAL_CMYKColor = 20;
	int LITERAL_EdgeForm = 21;
	int LITERAL_AbsolutePointSize = 22;
	int LITERAL_AbsoluteThickness = 23;
	int LITERAL_Dashing = 24;
	int LITERAL_FaceForm = 25;
	int LITERAL_PointSize = 26;
	int LITERAL_Thickness = 27;
	int LITERAL_AbsoluteDashing = 28;
	int Option = 29;
	int LITERAL_Boxed = 30;
	int MINUS = 31;
	int LARGER = 32;
	int LITERAL_True = 33;
	int LITERAL_False = 34;
	int DDOT = 35;
	int LITERAL_Axes = 36;
	int LITERAL_Automatic = 37;
	int LITERAL_AxesLabel = 38;
	int LITERAL_Prolog = 39;
	int LITERAL_Epilog = 40;
	int LITERAL_ViewPoint = 41;
	int LITERAL_ViewCenter = 42;
	int LITERAL_FaceGrids = 43;
	int LITERAL_Ticks = 44;
	int LITERAL_TextStyle = 45;
	int LITERAL_BoxRatios = 46;
	int LITERAL_Lighting = 47;
	int LITERAL_LightSources = 48;
	int LITERAL_AmbientLight = 49;
	int LITERAL_AxesEdge = 50;
	int LITERAL_PlotRange = 51;
	int LITERAL_DefaultColor = 52;
	int LITERAL_Background = 53;
	int LITERAL_ColorOutput = 54;
	int LITERAL_AxesStyle = 55;
	int LITERAL_BoxStyle = 56;
	int LITERAL_PlotLabel = 57;
	int LITERAL_AspectRatio = 58;
	int LITERAL_DefaultFont = 59;
	int LITERAL_PlotRegion = 60;
	int LITERAL_ViewVertical = 61;
	int LITERAL_SphericalRegion = 62;
	int LITERAL_Shading = 63;
	int LITERAL_RenderAll = 64;
	int LITERAL_PolygonIntersections = 65;
	int LITERAL_DisplayFunction = 66;
	// "Plot3Matrix" = 67
	int LITERAL_ImageSize = 68;
	int LITERAL_FormatType = 69;
	int LPAREN = 70;
	int RPAREN = 71;
	int PLUS = 72;
	int INTEGER_THING = 73;
	int DOT = 74;
	int STAR = 75;
	int HAT = 76;
	int BACKS = 77;
	int SLASH = 78;
	int DOLLAR = 79;
	int SMALER = 80;
	int T1 = 81;
	int T2 = 82;
	int T3 = 83;
	int T4 = 84;
	int T5 = 85;
	int T6 = 86;
	int T7 = 87;
	int T8 = 88;
	int T9 = 89;
	int ID = 90;
	int ID_LETTER = 91;
	int DIGIT = 92;
	int ESC = 93;
	int WS_ = 94;
}
