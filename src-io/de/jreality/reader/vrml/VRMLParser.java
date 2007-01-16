// $ANTLR 2.7.5 (20050128): "vrml-v2.0.g" -> "VRMLParser.java"$

package de.jreality.reader.vrml;
import java.awt.Color;
import java.util.*;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

/*****************************************************************************
 * The VRML Parser
 *****************************************************************************
 */
public class VRMLParser extends antlr.LLkParser       implements VRMLParserTokenTypes
 {

	public int[] listToIntArray(List l)		{
		int[] foo = new int[l.size()];
		int count = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext()	)	{
			foo[count++] = ((Integer)iter.next()).intValue();
		}
		return foo;
	}
	public double[] listToDoubleArray(List l)		{
		double[] foo = new double[l.size()];
		int count = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext()	)	{
			foo[count++] = ((Double)iter.next()).doubleValue();
		}
		return foo;
	}

protected VRMLParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public VRMLParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected VRMLParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public VRMLParser(TokenStream lexer) {
  this(lexer,2);
}

public VRMLParser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
}

	public final void vrmlFile() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(HEADER);
			vrmlScene();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
	}
	
	public final void vrmlScene() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			statements();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
	}
	
	public final void statements() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			_loop5:
			do {
				if ((_tokenSet_1.member(LA(1)))) {
					statement();
				}
				else {
					break _loop5;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	public final void statement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_DEF:
			case LITERAL_USE:
			case Script:
			case ID:
			{
				nodeStatement();
				break;
			}
			case LITERAL_PROTO:
			case LITERAL_EXTERNPROTO:
			{
				protoStatement();
				break;
			}
			case LITERAL_ROUTE:
			{
				routeStatement();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
	}
	
	public final void nodeStatement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case Script:
			case ID:
			{
				node();
				break;
			}
			case LITERAL_DEF:
			{
				match(LITERAL_DEF);
				nodeNameId();
				node();
				break;
			}
			case LITERAL_USE:
			{
				match(LITERAL_USE);
				nodeNameId();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
	}
	
	public final void protoStatement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_PROTO:
			{
				proto();
				break;
			}
			case LITERAL_EXTERNPROTO:
			{
				externproto();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
	}
	
	public final void routeStatement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_ROUTE);
			nodeNameId();
			match(PERIOD);
			eventOutId();
			match(LITERAL_TO);
			nodeNameId();
			match(PERIOD);
			eventInId();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
	}
	
	public final void node() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				nodeTypeId();
				match(OPEN_BRACE);
				nodeBody();
				match(CLOSE_BRACE);
				break;
			}
			case Script:
			{
				match(Script);
				match(OPEN_BRACE);
				scriptBody();
				match(CLOSE_BRACE);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
	}
	
	public final void nodeNameId() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			id();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_6);
		}
	}
	
	public final void rootNodeStatement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case Script:
			case ID:
			{
				node();
				break;
			}
			case LITERAL_DEF:
			{
				match(LITERAL_DEF);
				nodeNameId();
				node();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_7);
		}
	}
	
	public final void proto() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_PROTO);
			nodeTypeId();
			match(OPEN_BRACKET);
			interfaceDeclarations();
			match(CLOSE_BRACKET);
			match(OPEN_BRACE);
			protoBody();
			match(CLOSE_BRACE);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
	}
	
	public final void externproto() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_EXTERNPROTO);
			nodeTypeId();
			match(OPEN_BRACKET);
			externinterfaceDeclarations();
			match(CLOSE_BRACKET);
			urlList();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
	}
	
	public final void protoStatements() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			_loop12:
			do {
				if ((LA(1)==LITERAL_PROTO||LA(1)==LITERAL_EXTERNPROTO)) {
					protoStatement();
				}
				else {
					break _loop12;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_8);
		}
	}
	
	public final void nodeTypeId() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			id();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_9);
		}
	}
	
	public final void interfaceDeclarations() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			_loop17:
			do {
				if (((LA(1) >= LITERAL_eventIn && LA(1) <= LITERAL_exposedField))) {
					interfaceDeclaration();
				}
				else {
					break _loop17;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
		}
	}
	
	public final void protoBody() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			protoStatements();
			rootNodeStatement();
			statements();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_11);
		}
	}
	
	public final void interfaceDeclaration() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_eventIn:
			case LITERAL_eventOut:
			case LITERAL_field:
			{
				restrictedinterfaceDeclaration();
				break;
			}
			case LITERAL_exposedField:
			{
				match(LITERAL_exposedField);
				fieldTriple();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_12);
		}
	}
	
	public final void restrictedinterfaceDeclaration() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_eventIn:
			{
				match(LITERAL_eventIn);
				fieldType();
				eventInId();
				break;
			}
			case LITERAL_eventOut:
			{
				match(LITERAL_eventOut);
				fieldType();
				eventOutId();
				break;
			}
			case LITERAL_field:
			{
				match(LITERAL_field);
				fieldTriple();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void fieldType() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_MFColor:
			{
				match(LITERAL_MFColor);
				break;
			}
			case LITERAL_MFFloat:
			{
				match(LITERAL_MFFloat);
				break;
			}
			case 37:
			{
				match(37);
				break;
			}
			case LITERAL_MFNode:
			{
				match(LITERAL_MFNode);
				break;
			}
			case LITERAL_MFRotation:
			{
				match(LITERAL_MFRotation);
				break;
			}
			case LITERAL_MFString:
			{
				match(LITERAL_MFString);
				break;
			}
			case LITERAL_MFTime:
			{
				match(LITERAL_MFTime);
				break;
			}
			case 42:
			{
				match(42);
				break;
			}
			case 43:
			{
				match(43);
				break;
			}
			case LITERAL_SFBool:
			{
				match(LITERAL_SFBool);
				break;
			}
			case LITERAL_SFColor:
			{
				match(LITERAL_SFColor);
				break;
			}
			case LITERAL_SFFloat:
			{
				match(LITERAL_SFFloat);
				break;
			}
			case LITERAL_SFImage:
			{
				match(LITERAL_SFImage);
				break;
			}
			case 27:
			{
				match(27);
				break;
			}
			case LITERAL_SFNode:
			{
				match(LITERAL_SFNode);
				break;
			}
			case LITERAL_SFRotation:
			{
				match(LITERAL_SFRotation);
				break;
			}
			case LITERAL_SFString:
			{
				match(LITERAL_SFString);
				break;
			}
			case LITERAL_SFTime:
			{
				match(LITERAL_SFTime);
				break;
			}
			case 33:
			{
				match(33);
				break;
			}
			case 34:
			{
				match(34);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_14);
		}
	}
	
	public final void eventInId() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			id();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_15);
		}
	}
	
	public final void eventOutId() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			id();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_16);
		}
	}
	
	public final void fieldTriple() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_SFColor:
			{
				match(LITERAL_SFColor);
				fieldId();
				sfcolorValue();
				break;
			}
			case LITERAL_SFFloat:
			{
				match(LITERAL_SFFloat);
				fieldId();
				sffloatValue();
				break;
			}
			case LITERAL_SFImage:
			{
				match(LITERAL_SFImage);
				fieldId();
				sfimageValue();
				break;
			}
			case 27:
			{
				match(27);
				fieldId();
				sfInt32Value();
				break;
			}
			case LITERAL_SFNode:
			{
				match(LITERAL_SFNode);
				fieldId();
				sfnodeValue();
				break;
			}
			case LITERAL_SFRotation:
			{
				match(LITERAL_SFRotation);
				fieldId();
				sfrotationValue();
				break;
			}
			case LITERAL_SFString:
			{
				match(LITERAL_SFString);
				fieldId();
				sfstringValue();
				break;
			}
			case LITERAL_SFBool:
			{
				match(LITERAL_SFBool);
				fieldId();
				sfboolValue();
				break;
			}
			case LITERAL_SFTime:
			{
				match(LITERAL_SFTime);
				fieldId();
				sftimeValue();
				break;
			}
			case 33:
			{
				match(33);
				fieldId();
				sfvec2fValue();
				break;
			}
			case 34:
			{
				match(34);
				fieldId();
				sfvec3fValue();
				break;
			}
			case LITERAL_MFColor:
			{
				match(LITERAL_MFColor);
				fieldId();
				mfcolorValue();
				break;
			}
			case LITERAL_MFFloat:
			{
				match(LITERAL_MFFloat);
				fieldId();
				mffloatValue();
				break;
			}
			case 37:
			{
				match(37);
				fieldId();
				mfInt32Value();
				break;
			}
			case LITERAL_MFNode:
			{
				match(LITERAL_MFNode);
				fieldId();
				mfnodeValue();
				break;
			}
			case LITERAL_MFRotation:
			{
				match(LITERAL_MFRotation);
				fieldId();
				mfrotationValue();
				break;
			}
			case LITERAL_MFString:
			{
				match(LITERAL_MFString);
				fieldId();
				mfstringValue();
				break;
			}
			case LITERAL_MFTime:
			{
				match(LITERAL_MFTime);
				fieldId();
				mftimeValue();
				break;
			}
			case 42:
			{
				match(42);
				fieldId();
				mfvec2fValue();
				break;
			}
			case 43:
			{
				match(43);
				fieldId();
				mfvec3fValue();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void externinterfaceDeclarations() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			_loop23:
			do {
				if (((LA(1) >= LITERAL_eventIn && LA(1) <= LITERAL_exposedField))) {
					externinterfaceDeclaration();
				}
				else {
					break _loop23;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
		}
	}
	
	public final void urlList() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			mfstringValue();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
	}
	
	public final void externinterfaceDeclaration() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_eventIn:
			{
				match(LITERAL_eventIn);
				match(FIELDTYPE);
				eventInId();
				break;
			}
			case LITERAL_eventOut:
			{
				match(LITERAL_eventOut);
				match(FIELDTYPE);
				eventOutId();
				break;
			}
			case LITERAL_field:
			{
				match(LITERAL_field);
				match(FIELDTYPE);
				fieldId();
				break;
			}
			case LITERAL_exposedField:
			{
				match(LITERAL_exposedField);
				match(FIELDTYPE);
				fieldId();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_12);
		}
	}
	
	public final void fieldId() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			id();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_17);
		}
	}
	
	public final void mfstringValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			if ((LA(1)==STRING)) {
				sfstringValue();
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==STRING)) {
				match(OPEN_BRACKET);
				sfstringValues();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
	}
	
	public final void nodeBody() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			_loop30:
			do {
				if ((_tokenSet_18.member(LA(1)))) {
					nodeBodyElement();
				}
				else {
					break _loop30;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_11);
		}
	}
	
	public final void scriptBody() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			_loop33:
			do {
				if ((_tokenSet_19.member(LA(1)))) {
					scriptBodyElement();
				}
				else {
					break _loop33;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_11);
		}
	}
	
	public final void nodeBodyElement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_ROUTE:
			{
				routeStatement();
				break;
			}
			case LITERAL_PROTO:
			case LITERAL_EXTERNPROTO:
			{
				protoStatement();
				break;
			}
			default:
				if ((LA(1)==ID) && (LA(2)==LITERAL_IS)) {
					fieldId();
					match(LITERAL_IS);
					fieldId();
				}
				else if ((LA(1)==ID) && (LA(2)==LITERAL_IS)) {
					eventInId();
					match(LITERAL_IS);
					eventInId();
				}
				else if ((LA(1)==ID) && (LA(2)==LITERAL_IS)) {
					eventOutId();
					match(LITERAL_IS);
					eventOutId();
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_20);
		}
	}
	
	public final void scriptBodyElement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			if ((_tokenSet_18.member(LA(1)))) {
				nodeBodyElement();
			}
			else if (((LA(1) >= LITERAL_eventIn && LA(1) <= LITERAL_field)) && ((LA(2) >= LITERAL_SFColor && LA(2) <= 43))) {
				restrictedinterfaceDeclaration();
			}
			else if ((LA(1)==LITERAL_eventIn) && (LA(2)==FIELDTYPE)) {
				match(LITERAL_eventIn);
				match(FIELDTYPE);
				eventInId();
				match(LITERAL_IS);
				eventInId();
			}
			else if ((LA(1)==LITERAL_eventOut) && (LA(2)==FIELDTYPE)) {
				match(LITERAL_eventOut);
				match(FIELDTYPE);
				eventOutId();
				match(LITERAL_IS);
				eventOutId();
			}
			else if ((LA(1)==LITERAL_field) && (LA(2)==FIELDTYPE)) {
				match(LITERAL_field);
				match(FIELDTYPE);
				fieldId();
				match(LITERAL_IS);
				fieldId();
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_20);
		}
	}
	
	public final void id() throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
		try {      // for error handling
			n = LT(1);
			match(ID);
			System.err.println("Id matched: "+n.getText());
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_21);
		}
	}
	
	public final void sfcolorValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			number();
			number();
			number();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_22);
		}
	}
	
	public final void sffloatValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			number();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_22);
		}
	}
	
	public final void sfimageValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			sfInt32Values();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void sfInt32Value() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(INT32);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_23);
		}
	}
	
	public final void sfnodeValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_DEF:
			case LITERAL_USE:
			case Script:
			case ID:
			{
				nodeStatement();
				break;
			}
			case LITERAL_NULL:
			{
				match(LITERAL_NULL);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void sfrotationValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			number();
			number();
			number();
			number();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_22);
		}
	}
	
	public final void sfstringValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(STRING);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_24);
		}
	}
	
	public final void sfboolValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_TRUE:
			{
				match(LITERAL_TRUE);
				break;
			}
			case LITERAL_FALSE:
			{
				match(LITERAL_FALSE);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void sftimeValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			number();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_22);
		}
	}
	
	public final void sfvec2fValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			number();
			number();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_22);
		}
	}
	
	public final void sfvec3fValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			number();
			number();
			number();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_22);
		}
	}
	
	public final void mfcolorValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			if ((LA(1)==INT32||LA(1)==FLOAT)) {
				sfcolorValue();
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==INT32||LA(2)==FLOAT)) {
				match(OPEN_BRACKET);
				sfcolorValues();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void mffloatValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			if ((LA(1)==INT32||LA(1)==FLOAT)) {
				sffloatValue();
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==INT32||LA(2)==FLOAT)) {
				match(OPEN_BRACKET);
				sffloatValues();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void mfInt32Value() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			if ((LA(1)==INT32)) {
				sfInt32Value();
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==INT32)) {
				match(OPEN_BRACKET);
				sfInt32Values();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void mfnodeValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			if ((_tokenSet_25.member(LA(1)))) {
				nodeStatement();
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (_tokenSet_25.member(LA(2)))) {
				match(OPEN_BRACKET);
				nodeStatements();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void mfrotationValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			if ((LA(1)==INT32||LA(1)==FLOAT)) {
				sfrotationValue();
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==INT32||LA(2)==FLOAT)) {
				match(OPEN_BRACKET);
				sfrotationValues();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void mftimeValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			if ((LA(1)==INT32||LA(1)==FLOAT)) {
				sftimeValue();
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==INT32||LA(2)==FLOAT)) {
				match(OPEN_BRACKET);
				sftimeValues();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void mfvec2fValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			if ((LA(1)==INT32||LA(1)==FLOAT)) {
				sfvec2fValue();
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==INT32||LA(2)==FLOAT)) {
				match(OPEN_BRACKET);
				sfvec2fValues();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void mfvec3fValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			if ((LA(1)==INT32||LA(1)==FLOAT)) {
				sfvec3fValue();
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==INT32||LA(2)==FLOAT)) {
				match(OPEN_BRACKET);
				sfvec3fValues();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void number() throws RecognitionException, TokenStreamException {
		
		Token  f = null;
		Token  g = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case INT32:
			{
				f = LT(1);
				match(INT32);
				System.err.print("I "+f.getText());
				break;
			}
			case FLOAT:
			{
				g = LT(1);
				match(FLOAT);
				System.err.print("F "+g.getText());
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_22);
		}
	}
	
	public final void sfInt32Values() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			int _cnt67=0;
			_loop67:
			do {
				if ((LA(1)==INT32)) {
					sfInt32Value();
				}
				else {
					if ( _cnt67>=1 ) { break _loop67; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt67++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
	}
	
	public final void sftimeValues() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			int _cnt53=0;
			_loop53:
			do {
				if ((LA(1)==INT32||LA(1)==FLOAT)) {
					sftimeValue();
				}
				else {
					if ( _cnt53>=1 ) { break _loop53; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt53++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
		}
	}
	
	public final void sfcolorValues() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			int _cnt57=0;
			_loop57:
			do {
				if ((LA(1)==INT32||LA(1)==FLOAT)) {
					sfcolorValue();
				}
				else {
					if ( _cnt57>=1 ) { break _loop57; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt57++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
		}
	}
	
	public final void sffloatValues() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			int _cnt62=0;
			_loop62:
			do {
				if ((LA(1)==INT32||LA(1)==FLOAT)) {
					sffloatValue();
				}
				else {
					if ( _cnt62>=1 ) { break _loop62; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt62++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
		}
	}
	
	public final void nodeStatements() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			int _cnt72=0;
			_loop72:
			do {
				if ((_tokenSet_25.member(LA(1)))) {
					nodeStatement();
				}
				else {
					if ( _cnt72>=1 ) { break _loop72; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt72++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
		}
	}
	
	public final void sfrotationValues() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			int _cnt75=0;
			_loop75:
			do {
				if ((LA(1)==INT32||LA(1)==FLOAT)) {
					sfrotationValue();
				}
				else {
					if ( _cnt75>=1 ) { break _loop75; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt75++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
		}
	}
	
	public final void sfstringValues() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			int _cnt80=0;
			_loop80:
			do {
				if ((LA(1)==STRING)) {
					sfstringValue();
				}
				else {
					if ( _cnt80>=1 ) { break _loop80; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt80++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
		}
	}
	
	public final void sfvec2fValues() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			int _cnt84=0;
			_loop84:
			do {
				if ((LA(1)==INT32||LA(1)==FLOAT)) {
					sfvec2fValue();
				}
				else {
					if ( _cnt84>=1 ) { break _loop84; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt84++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
		}
	}
	
	public final void sfvec3fValues() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			int _cnt89=0;
			_loop89:
			do {
				if ((LA(1)==INT32||LA(1)==FLOAT)) {
					sfvec3fValue();
				}
				else {
					if ( _cnt89>=1 ) { break _loop89; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt89++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
		}
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"HEADER",
		"\"DEF\"",
		"\"USE\"",
		"\"PROTO\"",
		"OPEN_BRACKET",
		"CLOSE_BRACKET",
		"OPEN_BRACE",
		"CLOSE_BRACE",
		"\"eventIn\"",
		"\"eventOut\"",
		"\"field\"",
		"\"exposedField\"",
		"\"EXTERNPROTO\"",
		"FIELDTYPE",
		"\"ROUTE\"",
		"PERIOD",
		"\"TO\"",
		"Script",
		"\"IS\"",
		"an identifier",
		"\"SFColor\"",
		"\"SFFloat\"",
		"\"SFImage\"",
		"\"SFInt32\"",
		"\"SFNode\"",
		"\"SFRotation\"",
		"\"SFString\"",
		"\"SFBool\"",
		"\"SFTime\"",
		"\"SFVec2f\"",
		"\"SFVec3f\"",
		"\"MFColor\"",
		"\"MFFloat\"",
		"\"MFInt32\"",
		"\"MFNode\"",
		"\"MFRotation\"",
		"\"MFString\"",
		"\"MFTime\"",
		"\"MFVec2f\"",
		"\"MFVec3f\"",
		"INT32",
		"FLOAT",
		"\"TRUE\"",
		"\"FALSE\"",
		"\"NULL\"",
		"STRING",
		"HEADER1",
		"ID_LETTER",
		"INT_OR_FLOAT",
		"DECIMAL_BEGIN",
		"EXPONENT",
		"ESC",
		"RESTLINE",
		"COMMENT",
		"WS_"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 10813664L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 2050L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 10815714L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 10877666L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 10844386L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 11401954L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 10815712L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 10485792L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 1280L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 512L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 2048L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 61952L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 8780416L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 8388608L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 15071970L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 14023296L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 1108307735870432L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 8716416L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 8745088L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 8747136L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 1108307737444322L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 52776566913664L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 17592194824832L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { 562949964298978L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { 10485856L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	
	}
