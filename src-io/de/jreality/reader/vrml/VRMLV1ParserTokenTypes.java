// $ANTLR 2.7.5 (20050128): "vrml-v1.0.g" -> "VRMLV1Lexer.java"$

/*
 *	@author gonska
 *  Nov. october 12 2006
 */
/* TODO: 
 * 	Read over vrml1.0 spec and figure out exactly what it says
 *		Replace calls to System.err.println to de.jreality.util.LoggingSystem.getLogger(this).log()
 *		Implement the unimplemented nodes/properties
 *			Priority: Implement  DEF and USE.  That means all node rules need to return an
 *				instance of a jReality object that can be put into a hash map for later lookup.
 *				That is not yet the case.
 *		Move as much of the Java code into separate classes where eclipse can help
 *			Keep an eye on Antlr Studio (www.antlrstudio.com) for direct editing of .g file in eclipse.
 *		Some tricky areas:
 *			Difference between Group and Separator: Separator requires that we stack the state
 *				(Leaving a separator should pop this stack)
 *				The state is kept by fields in the parser beginning with "current"
 *			Appearance currently accumulates within a Separator clause. It's then assigned to each 
 *				child that is created within the Separator (such as for each geometric primitive).
 *				I don't think that's correct; the state of the appearance should be copied to a new
 *				instance before being assigned to a child, so later changes to the appearance don't 
 *				effect this child.
 *		 	We need a Material class to store all the info in a Material node.  If MaterialBinding 
 *				is per face or per vertex, then we have a problem, since currently only the diffuse
 *				color can be easily assigned per face or per vertex in jReality.
 *	General comment:
 *		This parser probably needs to be rewritten from the ground up to be a two-stage process.
 *		First, parse the VRML into a parse tree which knows nothing of specific keywords, but
 *		works purely with key-value pairs.  The big problem here is to dis-ambiguate all the possible
 *		value types.  
 */
package de.jreality.reader.vrml;
import java.awt.Color;
import java.util.*;
import de.jreality.util.*;
import de.jreality.scene.*;
import de.jreality.math.*;
import de.jreality.geometry.*;
import de.jreality.shader.*;
import de.jreality.scene.data.*;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.SpotLight;


public interface VRMLV1ParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int HEADER = 4;
	int LITERAL_DEF = 5;
	int LITERAL_USE = 6;
	int LITERAL_WWWInline = 7;
	int LITERAL_WWWAnchor = 8;
	int LITERAL_LOD = 9;
	int LITERAL_Separator = 10;
	int OPEN_BRACE = 11;
	int CLOSE_BRACE = 12;
	int LITERAL_Switch = 13;
	int LITERAL_AsciiText = 14;
	int LITERAL_string = 15;
	int LITERAL_spacing = 16;
	int LITERAL_justification = 17;
	int LITERAL_width = 18;
	int LITERAL_Cone = 19;
	int LITERAL_parts = 20;
	int LITERAL_bottomRadius = 21;
	int LITERAL_height = 22;
	int LITERAL_Cube = 23;
	int LITERAL_depth = 24;
	int LITERAL_Cylinder = 25;
	int LITERAL_radius = 26;
	int LITERAL_IndexedFaceSet = 27;
	int LITERAL_coordIndex = 28;
	int LITERAL_materialIndex = 29;
	int LITERAL_normalIndex = 30;
	int LITERAL_textureCoordIndex = 31;
	int LITERAL_IndexedLineSet = 32;
	int LITERAL_PointSet = 33;
	int LITERAL_startIndex = 34;
	int LITERAL_numPoints = 35;
	int LITERAL_Sphere = 36;
	int LITERAL_FontStyle = 37;
	// "Coordinate3" = 38
	int LITERAL_point = 39;
	int LITERAL_Info = 40;
	int LITERAL_Material = 41;
	int LITERAL_ambientColor = 42;
	int LITERAL_diffuseColor = 43;
	int LITERAL_specularColor = 44;
	int LITERAL_emissiveColor = 45;
	int LITERAL_shininess = 46;
	int LITERAL_transparency = 47;
	int LITERAL_MaterialBinding = 48;
	int LITERAL_value = 49;
	int LITERAL_Normal = 50;
	int LITERAL_vector = 51;
	int LITERAL_NormalBinding = 52;
	// "Texture2" = 53
	int LITERAL_filename = 54;
	int LITERAL_image = 55;
	int LITERAL_wrapS = 56;
	int LITERAL_wrapT = 57;
	// "Texture2Transform" = 58
	int LITERAL_translation = 59;
	int LITERAL_rotation = 60;
	int LITERAL_ScaleFactor = 61;
	int LITERAL_Center = 62;
	// "TextureCoordinate2" = 63
	int LITERAL_ShapeHints = 64;
	int LITERAL_vertexOrdering = 65;
	int LITERAL_shapeType = 66;
	int LITERAL_faceType = 67;
	int LITERAL_creaseAngle = 68;
	int LITERAL_MatrixTransform = 69;
	int LITERAL_matrix = 70;
	int LITERAL_Rotation = 71;
	int LITERAL_Scale = 72;
	int LITERAL_scaleFactor = 73;
	int LITERAL_Transform = 74;
	int LITERAL_scaleOrientation = 75;
	int LITERAL_center = 76;
	int LITERAL_Translation = 77;
	int LITERAL_PerspectiveCamera = 78;
	int LITERAL_position = 79;
	int LITERAL_orientation = 80;
	int LITERAL_focalDistance = 81;
	int LITERAL_heightAngle = 82;
	int LITERAL_OrthographicCamera = 83;
	int LITERAL_PointLight = 84;
	int LITERAL_on = 85;
	int LITERAL_intensity = 86;
	int LITERAL_color = 87;
	int LITERAL_location = 88;
	int LITERAL_SpotLight = 89;
	int LITERAL_direction = 90;
	int LITERAL_dropOffRate = 91;
	int LITERAL_cutOffAngle = 92;
	int LITERAL_DirectionalLight = 93;
	int ID = 94;
	int LPAREN = 95;
	int T1 = 96;
	int RPAREN = 97;
	int LITERAL_TRUE = 98;
	int LITERAL_FALSE = 99;
	int OPEN_BRACKET = 100;
	int CLOSE_BRACKET = 101;
	int COLON = 102;
	int HEXDEC = 103;
	int NUMBER = 104;
	int STRING = 105;
	int PLUS = 106;
	int MINUS = 107;
	int LITERAL_E = 108;
	int LITERAL_e = 109;
	int ID_LETTER = 110;
	int HEXDIGIT = 111;
	int DIGIT = 112;
	int ESC = 113;
	int RESTLINE = 114;
	int HEADER1 = 115;
	int COMMENT = 116;
	int WS_ = 117;
}
