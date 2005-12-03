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
	literals.put(new ANTLRHashString("normalIndex", this), new Integer(54));
	literals.put(new ANTLRHashString("DEFAULT", this), new Integer(44));
	literals.put(new ANTLRHashString("vertexOrdering", this), new Integer(21));
	literals.put(new ANTLRHashString("IndexedFaceSet", this), new Integer(52));
	literals.put(new ANTLRHashString("COUNTERCLOCKWISE", this), new Integer(22));
	literals.put(new ANTLRHashString("Info", this), new Integer(10));
	literals.put(new ANTLRHashString("MaterialBinding", this), new Integer(43));
	literals.put(new ANTLRHashString("Transform", this), new Integer(12));
	literals.put(new ANTLRHashString("PER_PART", this), new Integer(46));
	literals.put(new ANTLRHashString("CONVEX", this), new Integer(27));
	literals.put(new ANTLRHashString("MatrixTransform", this), new Integer(15));
	literals.put(new ANTLRHashString("IndexedLineSet", this), new Integer(55));
	literals.put(new ANTLRHashString("false", this), new Integer(66));
	literals.put(new ANTLRHashString("shininess", this), new Integer(36));
	literals.put(new ANTLRHashString("true", this), new Integer(64));
	literals.put(new ANTLRHashString("specularColor", this), new Integer(33));
	literals.put(new ANTLRHashString("transparency", this), new Integer(35));
	literals.put(new ANTLRHashString("Coordinate3", this), new Integer(37));
	literals.put(new ANTLRHashString("shapeType", this), new Integer(24));
	literals.put(new ANTLRHashString("faceType", this), new Integer(26));
	literals.put(new ANTLRHashString("diffuseColor", this), new Integer(32));
	literals.put(new ANTLRHashString("UNKNOWN_FACE_TYPE", this), new Integer(28));
	literals.put(new ANTLRHashString("string", this), new Integer(11));
	literals.put(new ANTLRHashString("creaseAngle", this), new Integer(29));
	literals.put(new ANTLRHashString("emissiveColor", this), new Integer(34));
	literals.put(new ANTLRHashString("ShapeHints", this), new Integer(20));
	literals.put(new ANTLRHashString("SOLID", this), new Integer(25));
	literals.put(new ANTLRHashString("center", this), new Integer(14));
	literals.put(new ANTLRHashString("NormalBinding", this), new Integer(41));
	literals.put(new ANTLRHashString("PER_VERTEX", this), new Integer(50));
	literals.put(new ANTLRHashString("position", this), new Integer(57));
	literals.put(new ANTLRHashString("heightAngle", this), new Integer(60));
	literals.put(new ANTLRHashString("FALSE", this), new Integer(67));
	literals.put(new ANTLRHashString("TRUE", this), new Integer(65));
	literals.put(new ANTLRHashString("scaleFactor", this), new Integer(19));
	literals.put(new ANTLRHashString("CLOCKWISE", this), new Integer(23));
	literals.put(new ANTLRHashString("PerspectiveCamera", this), new Integer(56));
	literals.put(new ANTLRHashString("ambientColor", this), new Integer(31));
	literals.put(new ANTLRHashString("orientation", this), new Integer(58));
	literals.put(new ANTLRHashString("OVERALL", this), new Integer(45));
	literals.put(new ANTLRHashString("value", this), new Integer(42));
	literals.put(new ANTLRHashString("DEF", this), new Integer(5));
	literals.put(new ANTLRHashString("PER_VERTEX_INDEXED", this), new Integer(51));
	literals.put(new ANTLRHashString("focalDistance", this), new Integer(59));
	literals.put(new ANTLRHashString("coordIndex", this), new Integer(53));
	literals.put(new ANTLRHashString("Scale", this), new Integer(18));
	literals.put(new ANTLRHashString("point", this), new Integer(38));
	literals.put(new ANTLRHashString("vector", this), new Integer(40));
	literals.put(new ANTLRHashString("USE", this), new Integer(6));
	literals.put(new ANTLRHashString("Normal", this), new Integer(39));
	literals.put(new ANTLRHashString("Material", this), new Integer(30));
	literals.put(new ANTLRHashString("PER_FACE", this), new Integer(48));
	literals.put(new ANTLRHashString("PER_PART_INDEXED", this), new Integer(47));
	literals.put(new ANTLRHashString("Translation", this), new Integer(16));
	literals.put(new ANTLRHashString("PER_FACE_INDEXED", this), new Integer(49));
	literals.put(new ANTLRHashString("Separator", this), new Integer(7));
	literals.put(new ANTLRHashString("Rotation", this), new Integer(17));
	literals.put(new ANTLRHashString("rotation", this), new Integer(13));
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		setCommitToPath(false);
		int _m;
		_m = mark();
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '.':
				{
					mPERIOD(true);
					theRetToken=_returnToken;
					break;
				}
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
				case '\t':  case '\n':  case '\u000c':  case '\r':
				case ' ':  case ',':
				{
					mWS_(true);
					theRetToken=_returnToken;
					break;
				}
				default:
					if (((LA(1)=='#') && (LA(2)=='V'))&&(getLine()==1)) {
						mHEADER(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='#') && ((LA(2) >= '\u0003' && LA(2) <= '\u00ff'))) {
						mCOMMENT(true);
						theRetToken=_returnToken;
					}
					else if ((_tokenSet_0.member(LA(1))) && (true)) {
						mINT_OR_FLOAT(true);
						theRetToken=_returnToken;
					}
					else if ((_tokenSet_0.member(LA(1))) && (true)) {
						mINT32(true);
						theRetToken=_returnToken;
					}
				else {
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {
					commit();
					try {mIGNORE(false);}
					catch(RecognitionException e) {
						// catastrophic failure
						reportError(e);
						consume();
					}
					continue tryAgain;
				}
				}
				}
				commit();
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				if ( !getCommitToPath() ) {
					rewind(_m);
					resetText();
					try {mIGNORE(false);}
					catch(RecognitionException ee) {
						// horrendous failure: error in filter rule
						reportError(ee);
						consume();
					}
					continue tryAgain;
				}
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

	public final void mPERIOD(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PERIOD;
		int _saveIndex;
		
		match('.');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
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
		_loop106:
		do {
			if ((_tokenSet_1.member(LA(1)))) {
				mID_LETTER(false);
			}
			else {
				break _loop106;
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
	
	public final void mINT_OR_FLOAT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = INT_OR_FLOAT;
		int _saveIndex;
		
		{
		mDECIMAL_BEGIN(false);
		}
		_ttype = INT32;
		{
		if ((LA(1)=='.'||LA(1)=='E'||LA(1)=='e')) {
			{
			switch ( LA(1)) {
			case '.':
			{
				{
				match('.');
				{
				int _cnt115=0;
				_loop115:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						mDIGIT(false);
					}
					else {
						if ( _cnt115>=1 ) { break _loop115; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt115++;
				} while (true);
				}
				{
				if ((LA(1)=='E'||LA(1)=='e')) {
					mEXPONENT(false);
				}
				else {
				}
				
				}
				}
				break;
			}
			case 'E':  case 'e':
			{
				mEXPONENT(false);
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			_ttype = FLOAT;
		}
		else {
		}
		
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDECIMAL_BEGIN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DECIMAL_BEGIN;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '+':
		{
			match('+');
			break;
		}
		case '-':
		{
			match('-');
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		int _cnt127=0;
		_loop127:
		do {
			if (((LA(1) >= '0' && LA(1) <= '9'))) {
				mDIGIT(false);
			}
			else {
				if ( _cnt127>=1 ) { break _loop127; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt127++;
		} while (true);
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
	
	protected final void mEXPONENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EXPONENT;
		int _saveIndex;
		
		{
		{
		switch ( LA(1)) {
		case 'e':
		{
			match('e');
			break;
		}
		case 'E':
		{
			match('E');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		switch ( LA(1)) {
		case '+':
		{
			match('+');
			break;
		}
		case '-':
		{
			match('-');
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		int _cnt133=0;
		_loop133:
		do {
			if (((LA(1) >= '0' && LA(1) <= '9'))) {
				matchRange('0','9');
			}
			else {
				if ( _cnt133>=1 ) { break _loop133; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			
			_cnt133++;
		} while (true);
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mINT32(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = INT32;
		int _saveIndex;
		
		if ((LA(1)=='0') && (LA(2)=='X'||LA(2)=='x')) {
			{
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
			int _cnt121=0;
			_loop121:
			do {
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					matchRange('0','9');
					break;
				}
				case 'a':  case 'b':  case 'c':  case 'd':
				case 'e':  case 'f':
				{
					matchRange('a','f');
					break;
				}
				case 'A':  case 'B':  case 'C':  case 'D':
				case 'E':  case 'F':
				{
					matchRange('A','F');
					break;
				}
				default:
				{
					if ( _cnt121>=1 ) { break _loop121; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				_cnt121++;
			} while (true);
			}
			}
		}
		else if ((_tokenSet_0.member(LA(1))) && (true)) {
			mDECIMAL_BEGIN(false);
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
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
		
		match('"');
		{
		_loop137:
		do {
			if ((LA(1)=='\\')) {
				mESC(false);
			}
			else if ((_tokenSet_2.member(LA(1)))) {
				{
				match(_tokenSet_2);
				}
			}
			else {
				break _loop137;
			}
			
		} while (true);
		}
		match('"');
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
		_loop143:
		do {
			if ((_tokenSet_3.member(LA(1)))) {
				{
				match(_tokenSet_3);
				}
			}
			else {
				break _loop143;
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
		
		match("#VRML V1.0");
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
	
	public final void mCOMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMMENT;
		int _saveIndex;
		
		match('#');
		{
		_loop150:
		do {
			if ((_tokenSet_3.member(LA(1)))) {
				{
				match(_tokenSet_3);
				}
			}
			else {
				break _loop150;
			}
			
		} while (true);
		}
		{
		match('\n');
		}
		System.err.println("Skipping comment "); _ttype = Token.SKIP;
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
		int _cnt155=0;
		_loop155:
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
			case ',':
			{
				match(',');
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
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				
				}
				break;
			}
			default:
			{
				if ( _cnt155>=1 ) { break _loop155; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			}
			_cnt155++;
		} while (true);
		}
		_ttype = Token.SKIP;
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mIGNORE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = IGNORE;
		int _saveIndex;
		
		match('{');
		{
		_loop159:
		do {
			if ((_tokenSet_4.member(LA(1)))) {
				{
				match(_tokenSet_4);
				}
			}
			else {
				break _loop159;
			}
			
		} while (true);
		}
		match('}');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 287992881640112128L, 0L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 287948901175001088L, 576460745995190270L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[8];
		data[0]=-17179869192L;
		data[1]=-268435457L;
		for (int i = 2; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[8];
		data[0]=-1032L;
		for (int i = 1; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[8];
		data[0]=-8L;
		data[1]=-2305843009213693953L;
		for (int i = 2; i<=3; i++) { data[i]=-1L; }
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	
	}
