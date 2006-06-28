/*
 * Created on May 6, 2004
 *
 * This file is part of the de.jreality.scene package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.shader;

import java.awt.Color;

import de.jreality.geometry.TubeUtility;
import de.jreality.scene.Appearance;

public class CommonAttributes {
    
	private CommonAttributes() {}

	// goes in root appearance: first set controls how the background of generated image appears
	public final static String BACKGROUND_COLOR = 	"backgroundColor";
    public final static Color  BACKGROUND_COLOR_DEFAULT = new java.awt.Color(225, 225, 225);
	public final static String BACKGROUND_COLORS = 	"backgroundColors";		// array of four colors for corners of background
	public final static String BACKGROUND_TEXTURE2D = "backgroundTexture2D";		// value is a Texture2D
	public final static String SKY_BOX = "skyBox";	// the value is a de.jreality.shader.CubeMap
	
	public final static String FOG_ENABLED = 	"fogEnabled";
    public final static boolean  FOG_ENABLED_DEFAULT = false;
    public final static String FOG_COLOR = "fogColor";
    public final static String FOG_DENSITY = "fogDensity";
    public final static double FOG_DENSITY_DEFAULT = 0.1;
    
    public final static String NAME ="name";
	// rendering hints
	public final static String LIGHTING_ENABLED 	= 		"lightingEnabled";
	public final static String ANTIALIASING_ENABLED = 	"antiAliasing";
	public final static String FAST_AND_DIRTY_ENABLED = 	"isFastAndDirty";
	public final static String TRANSPARENCY_ENABLED = 	"transparencyEnabled";
	public final static String Z_BUFFER_ENABLED = 		"zBufferEnabled";
	public final static String AT_INFINITY = 				"atInfinity";
	public final static String LEVEL_OF_DETAIL = 			"levelOfDetail";
	public final static double LEVEL_OF_DETAIL_DEFAULT = 			1.0;
	public final static boolean DIFFUSE_COLOR_ONLY	= false;
	public static final String DEEP_TRANSFORMATION_STACK = "deepTransformationStack";
	
	// these hints are heavily OpenGL specific
	public final static String DEPTH_FUDGE_FACTOR = 		"depthFudgeFactor";
	public final static String IGNORE_ALPHA0	=			"ignoreAlpha0";	// reject pixel writes for pixels with alpha == 0
	public final static boolean IGNORE_ALPHA0_DEFAULT = true;
	public final static String BACK_FACE_CULLING_ENABLED = 		"backFaceCulling";
	public final static String FORCE_RESIDENT_TEXTURES = "forceResidentTextures";
	public final static String MANY_DISPLAY_LISTS = "manyDisplayLists";		// if true, one display list per scene graph path
	public final static String ANY_DISPLAY_LISTS = "anyDisplayLists";		// if true, use no Display lists.
	public static final String CLEAR_COLOR_BUFFER = "clearColorBuffer";
   // default geometry shader
	public final static String FACE_DRAW = 		"showFaces";
	public final static boolean FACE_DRAW_DEFAULT = true;
	public final static String EDGE_DRAW = 		"showLines";
	public final static boolean EDGE_DRAW_DEFAULT = true;
	public final static String VERTEX_DRAW = 		"showPoints";
	public final static boolean VERTEX_DRAW_DEFAULT = false;
	public final static String POINT = 		"point";
	public final static String LINE = 		"line";
	public final static String POLYGON = 	"polygon";
	public final static String VERTEX = 	"vertex";
	public final static String VOLUME = 	"volume";
	private final static String SHADER = "Shader";
	public final static String POINT_SHADER = 		POINT+SHADER;
	public final static String LINE_SHADER = 		LINE+SHADER;
	public final static String POLYGON_SHADER = 	POLYGON+SHADER;
	public final static String VERTEX_SHADER = 	VERTEX+SHADER;
	public final static String VOLUME_SHADER = 	VOLUME+SHADER;
	// default point shader
	public final static String SPHERES_DRAW = 	"spheresDraw";
	public final static boolean SPHERES_DRAW_DEFAULT = true;
	public final static String POINT_RADIUS = 	"pointRadius";			// object coordinates
    public final static double POINT_RADIUS_DEFAULT = 0.025;
	public final static String POINT_SIZE = 	"pointSize";				// pixel coordinates
    public final static double POINT_SIZE_DEFAULT = 3.0;
	public static final Color POINT_DIFFUSE_COLOR_DEFAULT = Color.RED;
	public final static String SPHERE_RESOLUTION = "sphereResolution";
  // default line shader
	public final static String TUBES_DRAW = 		"tubeDraw";
	public final static boolean TUBES_DRAW_DEFAULT = true;
	public final static String TUBE_RADIUS = 		"tubeRadius";		// object coordinates
    public final static double TUBE_RADIUS_DEFAULT = 0.025;
	public final static String TUBE_STYLE = 		"tubeStyle";		// parallel or frenet?
    public final static int TUBE_STYLE_DEFAULT = TubeUtility.PARALLEL;
	public final static String VERTEX_COLORS_ENABLED = 		"vertexColorsEnabled";	// get colors from vertices?
    public final static boolean VERTEX_COLORS_ENABLED_DEFAULT = false;
	public static final String INTERPOLATE_VERTEX_COLORS = "interpolateVertexColors";	
	public static final boolean INTERPOLATE_VERTEX_COLORS_DEFAULT = false;	// if true, then interpolate vertex colors
	public final static String LINE_WIDTH = 		"lineWidth";			// pixel coordinates
    public final static double LINE_WIDTH_DEFAULT = 1.0;
	public final static String NORMAL_SCALE = 		"normalScale";
	public final static String LINE_STIPPLE = 		"lineStipple";		// openGL line drawing options
	public final static String LINE_FACTOR = 		"lineFactor";
	public final static String LINE_STIPPLE_PATTERN = "lineStipplePattern";
	public static final Color LINE_DIFFUSE_COLOR_DEFAULT = Color.BLACK;
	// default polygon shader
	public final static String SMOOTH_SHADING = 	"smoothShading";		// interpolate vertex shading values?
	public final static boolean SMOOTH_SHADING_DEFAULT = true;
	public final static String TEXTURE_2D = 		"texture2d";		
	public final static String TRANSPARENCY = 		"transparency";		
    public final static double TRANSPARENCY_DEFAULT =  0.0;
	public final static String AMBIENT_COLOR = 	"ambientColor";
    public final static Color  AMBIENT_COLOR_DEFAULT = Color.WHITE;
	public final static String DIFFUSE_COLOR = 	"diffuseColor";
    public final static Color  DIFFUSE_COLOR_DEFAULT = Color.BLUE;
	public final static String SPECULAR_COLOR = 	"specularColor";
    public final static Color SPECULAR_COLOR_DEFAULT = Color.WHITE;    
    public final static String SPECULAR_EXPONENT =  "specularExponent";
    public final static double SPECULAR_EXPONENT_DEFAULT =  60.;
    public final static String AMBIENT_COEFFICIENT =  "ambientCoefficient";
    public final static double AMBIENT_COEFFICIENT_DEFAULT =  .0;
    public final static String DIFFUSE_COEFFICIENT =  "diffuseCoefficient";
    public final static double DIFFUSE_COEFFICIENT_DEFAULT =  1.0;
    public final static String SPECULAR_COEFFICIENT =  "specularCoefficient";
    public final static double SPECULAR_COEFFICIENT_DEFAULT =  .7;
	public static final String LIGHT_DIRECTION = "lightDirection";

	// implode polygon shader
	public static final String IMPLODE = "implode";
	public static final String IMPLODE_FACTOR = "implodeFactor";
	public static final double IMPLODE_FACTOR_DEFAULT = 0.6;
	
	// miscellaneous
	public static final String PICKABLE = "pickable";
	public static final String SIGNATURE= "signature";
	
	// renderman backend
	public final static String RMAN_SURFACE = "rendermanSurface";
	public final static String RMAN_DISPLACEMENT = "rendermanDisplacement";
	public final static String RMAN_TEXTURE_FILE = "rendermanTexFile";
	public final static String RMAN_GLOBAL_INCLUDE_FILE = "rendermanGlobalIncludeFile";
	public final static String RMAN_SEARCHPATH_SHADER = "rendermanSearchpathShader";
	public static final String RMAN_OUTPUT_DISPLAY_FORMAT = "rendermanOutputDisplayFormat";
	public static final String RMAN_PREAMBLE = "rendermanPreamble";
    /**
     * 
     * @param ap
     * 
     * @deprecated
     */
	public static void setDefaultValues(Appearance ap)	{
		ap.setAttribute(BACKGROUND_COLOR,BACKGROUND_COLOR_DEFAULT);
		// rendering hints
		ap.setAttribute(LEVEL_OF_DETAIL,LEVEL_OF_DETAIL_DEFAULT);
		ap.setAttribute(ANTIALIASING_ENABLED,false);
		ap.setAttribute(TRANSPARENCY_ENABLED,true);
		ap.setAttribute(LIGHTING_ENABLED,true);
		ap.setAttribute(FAST_AND_DIRTY_ENABLED,true);
		ap.setAttribute(AT_INFINITY,false);
		
		ap.setAttribute(TRANSPARENCY, TRANSPARENCY_DEFAULT);
		// default geometry shader
		ap.setAttribute(FACE_DRAW,FACE_DRAW_DEFAULT);
		ap.setAttribute(EDGE_DRAW,EDGE_DRAW_DEFAULT);
		ap.setAttribute(VERTEX_DRAW,VERTEX_DRAW_DEFAULT);
		// default point shader
		// drawing normals is being phased out -cg
		ap.setAttribute(SPHERES_DRAW,SPHERES_DRAW_DEFAULT);
	    ap.setAttribute(POINT_RADIUS,POINT_RADIUS_DEFAULT);
	    ap.setAttribute(POINT_SIZE,POINT_SIZE_DEFAULT);
		ap.setAttribute(POINT_SHADER+"."+DIFFUSE_COLOR,Color.RED);
       // TODO: discuss wether such a big point radius makes sense!
        // it might be that jogl renderer uses this as pixels (like linewidth
        // and unlike tubeRadius...)
        // ap.setAttribute(POINT_RADIUS,POINT_RADIUS_VALUE);
        // default line shader
		ap.setAttribute(TUBES_DRAW,TUBES_DRAW_DEFAULT);
        ap.setAttribute(TUBE_RADIUS,TUBE_RADIUS_DEFAULT);
		ap.setAttribute(LINE_WIDTH,LINE_WIDTH_DEFAULT);
		ap.setAttribute(LINE_STIPPLE,false);
		ap.setAttribute(LINE_STIPPLE_PATTERN,0x1c47);
		//ap.setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR,Color.BLACK);
		ap.setAttribute(LINE_SHADER+"."+LIGHTING_ENABLED,false);
		ap.setAttribute(LINE_SHADER+"."+POLYGON_SHADER+"."+DIFFUSE_COLOR,Color.BLUE);
		ap.setAttribute(LINE_SHADER+"."+INTERPOLATE_VERTEX_COLORS, INTERPOLATE_VERTEX_COLORS_DEFAULT);
		// default polygon shader
		ap.setAttribute(POLYGON_SHADER+"."+SMOOTH_SHADING, SMOOTH_SHADING_DEFAULT);
		ap.setAttribute(POLYGON_SHADER+"."+AMBIENT_COLOR,AMBIENT_COLOR_DEFAULT);
		//ap.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR,DIFFUSE_COLOR_DEFAULT);
		ap.setAttribute(POLYGON_SHADER+"."+SPECULAR_COLOR,SPECULAR_COLOR_DEFAULT);
		ap.setAttribute(POLYGON_SHADER+"."+SPECULAR_EXPONENT,SPECULAR_EXPONENT_DEFAULT);
	}

}
