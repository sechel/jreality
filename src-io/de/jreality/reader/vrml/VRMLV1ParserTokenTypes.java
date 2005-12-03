// $ANTLR 2.7.4: "vrml-v1.0.g" -> "VRMLV1Lexer.java"$

/*
 *	@author gunn
 *  Nov. 30, 2005
 */
package de.jreality.reader.vrml;
import java.awt.Color;
import java.util.*;
import de.jreality.scene.*;
import de.jreality.math.*;
import de.jreality.geometry.*;
import de.jreality.shader.*;
import de.jreality.scene.data.*;

public interface VRMLV1ParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int HEADER = 4;
	int LITERAL_DEF = 5;
	int LITERAL_USE = 6;
	int LITERAL_Separator = 7;
	int OPEN_BRACE = 8;
	int CLOSE_BRACE = 9;
	int LITERAL_Info = 10;
	int LITERAL_string = 11;
	int LITERAL_Transform = 12;
	int LITERAL_rotation = 13;
	int LITERAL_center = 14;
	int LITERAL_MatrixTransform = 15;
	int LITERAL_Translation = 16;
	int LITERAL_Rotation = 17;
	int LITERAL_Scale = 18;
	int LITERAL_scaleFactor = 19;
	int LITERAL_ShapeHints = 20;
	int LITERAL_vertexOrdering = 21;
	int LITERAL_COUNTERCLOCKWISE = 22;
	int LITERAL_CLOCKWISE = 23;
	int LITERAL_shapeType = 24;
	int LITERAL_SOLID = 25;
	int LITERAL_faceType = 26;
	int LITERAL_CONVEX = 27;
	int LITERAL_UNKNOWN_FACE_TYPE = 28;
	int LITERAL_creaseAngle = 29;
	int LITERAL_Material = 30;
	int LITERAL_ambientColor = 31;
	int LITERAL_diffuseColor = 32;
	int LITERAL_specularColor = 33;
	int LITERAL_emissiveColor = 34;
	int LITERAL_transparency = 35;
	int LITERAL_shininess = 36;
	// "Coordinate3" = 37
	int LITERAL_point = 38;
	int LITERAL_Normal = 39;
	int LITERAL_vector = 40;
	int LITERAL_NormalBinding = 41;
	int LITERAL_value = 42;
	int LITERAL_MaterialBinding = 43;
	int LITERAL_DEFAULT = 44;
	int LITERAL_OVERALL = 45;
	int LITERAL_PER_PART = 46;
	int LITERAL_PER_PART_INDEXED = 47;
	int LITERAL_PER_FACE = 48;
	int LITERAL_PER_FACE_INDEXED = 49;
	int LITERAL_PER_VERTEX = 50;
	int LITERAL_PER_VERTEX_INDEXED = 51;
	int LITERAL_IndexedFaceSet = 52;
	int LITERAL_coordIndex = 53;
	int LITERAL_normalIndex = 54;
	int LITERAL_IndexedLineSet = 55;
	int LITERAL_PerspectiveCamera = 56;
	int LITERAL_position = 57;
	int LITERAL_orientation = 58;
	int LITERAL_focalDistance = 59;
	int LITERAL_heightAngle = 60;
	int ID = 61;
	int INT32 = 62;
	int FLOAT = 63;
	int LITERAL_true = 64;
	int LITERAL_TRUE = 65;
	int LITERAL_false = 66;
	int LITERAL_FALSE = 67;
	int STRING = 68;
	int OPEN_BRACKET = 69;
	int CLOSE_BRACKET = 70;
	int PERIOD = 71;
	int ID_LETTER = 72;
	int INT_OR_FLOAT = 73;
	int DIGIT = 74;
	int DECIMAL_BEGIN = 75;
	int EXPONENT = 76;
	int ESC = 77;
	int RESTLINE = 78;
	int HEADER1 = 79;
	int COMMENT = 80;
	int WS_ = 81;
	int IGNORE = 82;
}
