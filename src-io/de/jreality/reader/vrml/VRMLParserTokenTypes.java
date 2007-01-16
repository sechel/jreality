// $ANTLR 2.7.5 (20050128): "vrml-v2.0.g" -> "VRMLLexer.java"$

package de.jreality.reader.vrml;
import java.awt.Color;
import java.util.*;

public interface VRMLParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int HEADER = 4;
	int LITERAL_DEF = 5;
	int LITERAL_USE = 6;
	int LITERAL_PROTO = 7;
	int OPEN_BRACKET = 8;
	int CLOSE_BRACKET = 9;
	int OPEN_BRACE = 10;
	int CLOSE_BRACE = 11;
	int LITERAL_eventIn = 12;
	int LITERAL_eventOut = 13;
	int LITERAL_field = 14;
	int LITERAL_exposedField = 15;
	int LITERAL_EXTERNPROTO = 16;
	int FIELDTYPE = 17;
	int LITERAL_ROUTE = 18;
	int PERIOD = 19;
	int LITERAL_TO = 20;
	int Script = 21;
	int LITERAL_IS = 22;
	int ID = 23;
	int LITERAL_SFColor = 24;
	int LITERAL_SFFloat = 25;
	int LITERAL_SFImage = 26;
	// "SFInt32" = 27
	int LITERAL_SFNode = 28;
	int LITERAL_SFRotation = 29;
	int LITERAL_SFString = 30;
	int LITERAL_SFBool = 31;
	int LITERAL_SFTime = 32;
	// "SFVec2f" = 33
	// "SFVec3f" = 34
	int LITERAL_MFColor = 35;
	int LITERAL_MFFloat = 36;
	// "MFInt32" = 37
	int LITERAL_MFNode = 38;
	int LITERAL_MFRotation = 39;
	int LITERAL_MFString = 40;
	int LITERAL_MFTime = 41;
	// "MFVec2f" = 42
	// "MFVec3f" = 43
	int INT32 = 44;
	int FLOAT = 45;
	int LITERAL_TRUE = 46;
	int LITERAL_FALSE = 47;
	int LITERAL_NULL = 48;
	int STRING = 49;
	int HEADER1 = 50;
	int ID_LETTER = 51;
	int INT_OR_FLOAT = 52;
	int DECIMAL_BEGIN = 53;
	int EXPONENT = 54;
	int ESC = 55;
	int RESTLINE = 56;
	int COMMENT = 57;
	int WS_ = 58;
}
