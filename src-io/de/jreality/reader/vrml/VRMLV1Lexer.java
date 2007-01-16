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


import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

/************************************************************************************
 * The VRML Lexer
 ************************************************************************************
 */
public class VRMLV1Lexer extends antlr.CharScanner implements VRMLV1ParserTokenTypes, TokenStream
 {
public VRMLV1Lexer(InputStream in) {
	this(new ByteBuffer(in));
}
public VRMLV1Lexer(Reader in) {
	this(new CharBuffer(in));
}
public VRMLV1Lexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public VRMLV1Lexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = true;
	setCaseSensitive(true);
	literals = new Hashtable();
	literals.put(new ANTLRHashString("normalIndex", this), new Integer(30));
	literals.put(new ANTLRHashString("Cylinder", this), new Integer(25));
	literals.put(new ANTLRHashString("bottomRadius", this), new Integer(21));
	literals.put(new ANTLRHashString("vertexOrdering", this), new Integer(65));
	literals.put(new ANTLRHashString("TextureCoordinate2", this), new Integer(63));
	literals.put(new ANTLRHashString("textureCoordIndex", this), new Integer(31));
	literals.put(new ANTLRHashString("IndexedFaceSet", this), new Integer(27));
	literals.put(new ANTLRHashString("filename", this), new Integer(54));
	literals.put(new ANTLRHashString("Info", this), new Integer(40));
	literals.put(new ANTLRHashString("MaterialBinding", this), new Integer(48));
	literals.put(new ANTLRHashString("Transform", this), new Integer(74));
	literals.put(new ANTLRHashString("color", this), new Integer(87));
	literals.put(new ANTLRHashString("Cube", this), new Integer(23));
	literals.put(new ANTLRHashString("Center", this), new Integer(62));
	literals.put(new ANTLRHashString("MatrixTransform", this), new Integer(69));
	literals.put(new ANTLRHashString("IndexedLineSet", this), new Integer(32));
	literals.put(new ANTLRHashString("shininess", this), new Integer(46));
	literals.put(new ANTLRHashString("location", this), new Integer(88));
	literals.put(new ANTLRHashString("wrapT", this), new Integer(57));
	literals.put(new ANTLRHashString("specularColor", this), new Integer(44));
	literals.put(new ANTLRHashString("WWWAnchor", this), new Integer(8));
	literals.put(new ANTLRHashString("transparency", this), new Integer(47));
	literals.put(new ANTLRHashString("startIndex", this), new Integer(34));
	literals.put(new ANTLRHashString("Coordinate3", this), new Integer(38));
	literals.put(new ANTLRHashString("shapeType", this), new Integer(66));
	literals.put(new ANTLRHashString("WWWInline", this), new Integer(7));
	literals.put(new ANTLRHashString("faceType", this), new Integer(67));
	literals.put(new ANTLRHashString("FontStyle", this), new Integer(37));
	literals.put(new ANTLRHashString("e", this), new Integer(109));
	literals.put(new ANTLRHashString("diffuseColor", this), new Integer(43));
	literals.put(new ANTLRHashString("DirectionalLight", this), new Integer(93));
	literals.put(new ANTLRHashString("on", this), new Integer(85));
	literals.put(new ANTLRHashString("string", this), new Integer(15));
	literals.put(new ANTLRHashString("creaseAngle", this), new Integer(68));
	literals.put(new ANTLRHashString("wrapS", this), new Integer(56));
	literals.put(new ANTLRHashString("emissiveColor", this), new Integer(45));
	literals.put(new ANTLRHashString("Sphere", this), new Integer(36));
	literals.put(new ANTLRHashString("ShapeHints", this), new Integer(64));
	literals.put(new ANTLRHashString("intensity", this), new Integer(86));
	literals.put(new ANTLRHashString("SpotLight", this), new Integer(89));
	literals.put(new ANTLRHashString("center", this), new Integer(76));
	literals.put(new ANTLRHashString("NormalBinding", this), new Integer(52));
	literals.put(new ANTLRHashString("spacing", this), new Integer(16));
	literals.put(new ANTLRHashString("OrthographicCamera", this), new Integer(83));
	literals.put(new ANTLRHashString("justification", this), new Integer(17));
	literals.put(new ANTLRHashString("image", this), new Integer(55));
	literals.put(new ANTLRHashString("position", this), new Integer(79));
	literals.put(new ANTLRHashString("direction", this), new Integer(90));
	literals.put(new ANTLRHashString("Texture2Transform", this), new Integer(58));
	literals.put(new ANTLRHashString("E", this), new Integer(108));
	literals.put(new ANTLRHashString("heightAngle", this), new Integer(82));
	literals.put(new ANTLRHashString("FALSE", this), new Integer(99));
	literals.put(new ANTLRHashString("Cone", this), new Integer(19));
	literals.put(new ANTLRHashString("dropOffRate", this), new Integer(91));
	literals.put(new ANTLRHashString("translation", this), new Integer(59));
	literals.put(new ANTLRHashString("TRUE", this), new Integer(98));
	literals.put(new ANTLRHashString("scaleFactor", this), new Integer(73));
	literals.put(new ANTLRHashString("PerspectiveCamera", this), new Integer(78));
	literals.put(new ANTLRHashString("Texture2", this), new Integer(53));
	literals.put(new ANTLRHashString("ambientColor", this), new Integer(42));
	literals.put(new ANTLRHashString("parts", this), new Integer(20));
	literals.put(new ANTLRHashString("orientation", this), new Integer(80));
	literals.put(new ANTLRHashString("AsciiText", this), new Integer(14));
	literals.put(new ANTLRHashString("width", this), new Integer(18));
	literals.put(new ANTLRHashString("value", this), new Integer(49));
	literals.put(new ANTLRHashString("DEF", this), new Integer(5));
	literals.put(new ANTLRHashString("focalDistance", this), new Integer(81));
	literals.put(new ANTLRHashString("Switch", this), new Integer(13));
	literals.put(new ANTLRHashString("depth", this), new Integer(24));
	literals.put(new ANTLRHashString("height", this), new Integer(22));
	literals.put(new ANTLRHashString("coordIndex", this), new Integer(28));
	literals.put(new ANTLRHashString("Scale", this), new Integer(72));
	literals.put(new ANTLRHashString("matrix", this), new Integer(70));
	literals.put(new ANTLRHashString("point", this), new Integer(39));
	literals.put(new ANTLRHashString("vector", this), new Integer(51));
	literals.put(new ANTLRHashString("PointLight", this), new Integer(84));
	literals.put(new ANTLRHashString("materialIndex", this), new Integer(29));
	literals.put(new ANTLRHashString("cutOffAngle", this), new Integer(92));
	literals.put(new ANTLRHashString("USE", this), new Integer(6));
	literals.put(new ANTLRHashString("scaleOrientation", this), new Integer(75));
	literals.put(new ANTLRHashString("PointSet", this), new Integer(33));
	literals.put(new ANTLRHashString("Normal", this), new Integer(50));
	literals.put(new ANTLRHashString("Material", this), new Integer(41));
	literals.put(new ANTLRHashString("LOD", this), new Integer(9));
	literals.put(new ANTLRHashString("numPoints", this), new Integer(35));
	literals.put(new ANTLRHashString("Translation", this), new Integer(77));
	literals.put(new ANTLRHashString("ScaleFactor", this), new Integer(61));
	literals.put(new ANTLRHashString("Separator", this), new Integer(10));
	literals.put(new ANTLRHashString("radius", this), new Integer(26));
	literals.put(new ANTLRHashString("Rotation", this), new Integer(71));
	literals.put(new ANTLRHashString("rotation", this), new Integer(60));
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '{':
				{
					mOPEN_BRACE(true);
					theRetToken=_returnToken;
					break;
				}
				case '}':
				{
					mCLOSE_BRACE(true);
					theRetToken=_returnToken;
					break;
				}
				case '[':
				{
					mOPEN_BRACKET(true);
					theRetToken=_returnToken;
					break;
				}
				case ']':
				{
					mCLOSE_BRACKET(true);
					theRetToken=_returnToken;
					break;
				}
				case '(':
				{
					mLPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case ')':
				{
					mRPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case '-':
				{
					mMINUS(true);
					theRetToken=_returnToken;
					break;
				}
				case '+':
				{
					mPLUS(true);
					theRetToken=_returnToken;
					break;
				}
				case ',':
				{
					mCOLON(true);
					theRetToken=_returnToken;
					break;
				}
				case '|':
				{
					mT1(true);
					theRetToken=_returnToken;
					break;
				}
				case 'A':  case 'B':  case 'C':  case 'D':
				case 'E':  case 'F':  case 'G':  case 'H':
				case 'I':  case 'J':  case 'K':  case 'L':
				case 'M':  case 'N':  case 'O':  case 'P':
				case 'Q':  case 'R':  case 'S':  case 'T':
				case 'U':  case 'V':  case 'W':  case 'X':
				case 'Y':  case 'Z':  case '_':  case 'a':
				case 'b':  case 'c':  case 'd':  case 'e':
				case 'f':  case 'g':  case 'h':  case 'i':
				case 'j':  case 'k':  case 'l':  case 'm':
				case 'n':  case 'o':  case 'p':  case 'q':
				case 'r':  case 's':  case 't':  case 'u':
				case 'v':  case 'w':  case 'x':  case 'y':
				case 'z':
				{
					mID(true);
					theRetToken=_returnToken;
					break;
				}
				case '"':
				{
					mSTRING(true);
					theRetToken=_returnToken;
					break;
				}
				default:
					if ((LA(1)=='0') && (LA(2)=='X'||LA(2)=='x')) {
						mHEXDEC(true);
						theRetToken=_returnToken;
					}
					else if (((LA(1)=='#') && (LA(2)=='V'))&&(getLine()==1)) {
						mHEADER(true);
						theRetToken=_returnToken;
					}
					else if ((_tokenSet_0.member(LA(1))) && (true)) {
						mNUMBER(true);
						theRetToken=_returnToken;
					}
					else if ((_tokenSet_1.member(LA(1))) && (true)) {
						mWS_(true);
						theRetToken=_returnToken;
					}
				else {
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mOPEN_BRACE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = OPEN_BRACE;
		int _saveIndex;
		
		match('{');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCLOSE_BRACE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CLOSE_BRACE;
		int _saveIndex;
		
		match('}');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mOPEN_BRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = OPEN_BRACKET;
		int _saveIndex;
		
		match('[');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCLOSE_BRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CLOSE_BRACKET;
		int _saveIndex;
		
		match(']');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LPAREN;
		int _saveIndex;
		
		match('(');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RPAREN;
		int _saveIndex;
		
		match(')');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS;
		int _saveIndex;
		
		match('-');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS;
		int _saveIndex;
		
		match('+');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOLON(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COLON;
		int _saveIndex;
		
		match(',');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mT1(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = T1;
		int _saveIndex;
		
		match('|');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mID(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ID;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			matchRange('a','z');
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':
		{
			matchRange('A','Z');
			break;
		}
		case '_':
		{
			match('_');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		_loop239:
		do {
			if ((_tokenSet_2.member(LA(1)))) {
				mID_LETTER(false);
			}
			else {
				break _loop239;
			}
			
		} while (true);
		}
		_ttype = testLiteralsTable(_ttype);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mID_LETTER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ID_LETTER;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			matchRange('a','z');
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':  case 'G':  case 'H':
		case 'I':  case 'J':  case 'K':  case 'L':
		case 'M':  case 'N':  case 'O':  case 'P':
		case 'Q':  case 'R':  case 'S':  case 'T':
		case 'U':  case 'V':  case 'W':  case 'X':
		case 'Y':  case 'Z':
		{
			matchRange('A','Z');
			break;
		}
		case '_':
		{
			match('_');
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			matchRange('0','9');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mHEXDEC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HEXDEC;
		int _saveIndex;
		
		match('0');
		{
		switch ( LA(1)) {
		case 'x':
		{
			match('x');
			break;
		}
		case 'X':
		{
			match('X');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		_loop245:
		do {
			if ((_tokenSet_3.member(LA(1)))) {
				mHEXDIGIT(false);
			}
			else {
				break _loop245;
			}
			
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mHEXDIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HEXDIGIT;
		int _saveIndex;
		
		switch ( LA(1)) {
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			{
			matchRange('0','9');
			}
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':
		{
			{
			matchRange('A','F');
			}
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIGIT;
		int _saveIndex;
		
		{
		matchRange('0','9');
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNUMBER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NUMBER;
		int _saveIndex;
		
		switch ( LA(1)) {
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			{
			int _cnt253=0;
			_loop253:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					mDIGIT(false);
				}
				else {
					if ( _cnt253>=1 ) { break _loop253; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt253++;
			} while (true);
			}
			{
			if ((LA(1)=='.')) {
				match('.');
				{
				_loop256:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						mDIGIT(false);
					}
					else {
						break _loop256;
					}
					
				} while (true);
				}
			}
			else {
			}
			
			}
			break;
		}
		case '.':
		{
			match('.');
			{
			int _cnt258=0;
			_loop258:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					mDIGIT(false);
				}
				else {
					if ( _cnt258>=1 ) { break _loop258; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt258++;
			} while (true);
			}
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTRING(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING;
		int _saveIndex;
		
		_saveIndex=text.length();
		match('"');
		text.setLength(_saveIndex);
		{
		_loop262:
		do {
			if ((LA(1)=='\\')) {
				mESC(false);
			}
			else if ((_tokenSet_4.member(LA(1)))) {
				{
				match(_tokenSet_4);
				}
			}
			else {
				break _loop262;
			}
			
		} while (true);
		}
		_saveIndex=text.length();
		match('"');
		text.setLength(_saveIndex);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mESC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESC;
		int _saveIndex;
		
		match('\\');
		{
		switch ( LA(1)) {
		case '\\':
		{
			match('\\');
			break;
		}
		case '"':
		{
			match('"');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mRESTLINE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RESTLINE;
		int _saveIndex;
		
		{
		_loop268:
		do {
			if ((_tokenSet_5.member(LA(1)))) {
				{
				match(_tokenSet_5);
				}
			}
			else {
				break _loop268;
			}
			
		} while (true);
		}
		{
		match('\n');
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mHEADER1(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HEADER1;
		int _saveIndex;
		
		match("#VRML V1.0 ascii");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mHEADER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HEADER;
		int _saveIndex;
		
		if (!(getLine()==1))
		  throw new SemanticException("getLine()==1");
		mHEADER1(false);
		mRESTLINE(false);
		System.err.println("Got header");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mCOMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMMENT;
		int _saveIndex;
		
		match('#');
		{
		_loop275:
		do {
			if ((_tokenSet_5.member(LA(1)))) {
				{
				match(_tokenSet_5);
				}
			}
			else {
				break _loop275;
			}
			
		} while (true);
		}
		{
		match('\n');
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mWS_(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS_;
		int _saveIndex;
		
		{
		int _cnt280=0;
		_loop280:
		do {
			switch ( LA(1)) {
			case ' ':
			{
				match(' ');
				break;
			}
			case '\t':
			{
				match('\t');
				break;
			}
			case '\u000c':
			{
				match('\f');
				break;
			}
			case '#':
			{
				mCOMMENT(false);
				break;
			}
			case '\n':  case '\r':
			{
				{
				if ((LA(1)=='\r') && (LA(2)=='\n')) {
					match("\r\n");
				}
				else if ((LA(1)=='\r') && (true)) {
					match('\r');
				}
				else if ((LA(1)=='\n')) {
					match('\n');
					newline();
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				
				}
				break;
			}
			default:
			{
				if ( _cnt280>=1 ) { break _loop280; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			}
			_cnt280++;
		} while (true);
		}
		_ttype = Token.SKIP;
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 288019269919178752L, 0L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 38654719488L, 0L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 287948901175001088L, 576460745995190270L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 287948901175001088L, 126L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[8];
		data[0]=-17179869192L;
		data[1]=-268435457L;
		for (int i = 2; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = new long[8];
		data[0]=-1032L;
		for (int i = 1; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	
	}
