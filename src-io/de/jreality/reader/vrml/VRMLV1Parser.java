// $ANTLR 2.7.4: "vrml-v1.0.g" -> "VRMLV1Parser.java"$

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
 * The VRMLV1 Parser
 *****************************************************************************
 */
public class VRMLV1Parser extends antlr.LLkParser       implements VRMLV1ParserTokenTypes
 {

	// current state of the parsing process
	SceneGraphComponent currentSGC = null;
	SceneGraphComponent root = null;
	SceneGraphPath currentPath = new SceneGraphPath();
	Transformation currentTransform = new Transformation();
	Appearance currentAp = null;
	DataList currentCoordinate3 = null;
	DataList currentNormal = null;
	int[][] currentCoordinateIndex = null;
	int[][] currentNormalIndex = null;
	final int MAXSIZE = 100000;
	double[] ds = new double[MAXSIZE];
	int[] is = new int[MAXSIZE];
	double[] evil3Vec = new double[3];
	boolean collectingMFVec3 = false;
	int primitiveCount, polygonCount;

protected VRMLV1Parser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public VRMLV1Parser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected VRMLV1Parser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public VRMLV1Parser(TokenStream lexer) {
  this(lexer,2);
}

public VRMLV1Parser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
}

	public final SceneGraphComponent  vrmlFile() throws RecognitionException, TokenStreamException {
		SceneGraphComponent r;
		
		r = null;
		
		try {      // for error handling
			match(HEADER);
			r=vrmlScene();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		return r;
	}
	
	public final SceneGraphComponent  vrmlScene() throws RecognitionException, TokenStreamException {
		SceneGraphComponent r;
		
		r = null;
		
		try {      // for error handling
			
					root = new SceneGraphComponent();
					currentSGC = root;
					currentPath.push(root);
					primitiveCount = polygonCount = 0;
					
			{
			_loop4:
			do {
				if ((_tokenSet_1.member(LA(1)))) {
					statement();
				}
				else {
					break _loop4;
				}
				
			} while (true);
			}
			
						r = root;
						System.err.println("Read in "+primitiveCount+" primitives with a total of "+polygonCount+" faces.");
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		return r;
	}
	
	public final void statement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_DEF:
			{
				match(LITERAL_DEF);
				id();
				statement();
				break;
			}
			case LITERAL_USE:
			{
				match(LITERAL_USE);
				id();
				break;
			}
			case LITERAL_Separator:
			case LITERAL_Info:
			case LITERAL_Transform:
			case LITERAL_MatrixTransform:
			case LITERAL_Rotation:
			case LITERAL_Scale:
			case LITERAL_ShapeHints:
			case LITERAL_Material:
			case 37:
			case LITERAL_Normal:
			case LITERAL_IndexedFaceSet:
			case LITERAL_IndexedLineSet:
			case ID:
			{
				atomicStatement();
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
			consume();
			consumeUntil(_tokenSet_2);
		}
	}
	
	public final String  id() throws RecognitionException, TokenStreamException {
		String s;
		
		Token  n = null;
		s = null;
		
		try {      // for error handling
			n = LT(1);
			match(ID);
			if (VRMLHelper.verbose)	System.err.println("Id matched: "+n.getText()); s=n.getText();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		return s;
	}
	
	public final void atomicStatement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_Separator:
			{
				separatorStatement();
				break;
			}
			case LITERAL_Info:
			{
				infoStatement();
				break;
			}
			case LITERAL_Transform:
			{
				transformStatement();
				break;
			}
			case LITERAL_Rotation:
			{
				rotationStatement();
				break;
			}
			case LITERAL_Scale:
			{
				scaleStatement();
				break;
			}
			case LITERAL_MatrixTransform:
			{
				matrixTransformStatement();
				break;
			}
			case LITERAL_ShapeHints:
			{
				shapeHintsStatement();
				break;
			}
			case LITERAL_Material:
			{
				currentAp=materialStatement();
				break;
			}
			case 37:
			{
				coordinate3Statement();
				break;
			}
			case LITERAL_Normal:
			{
				normalStatement();
				break;
			}
			case LITERAL_IndexedFaceSet:
			{
				indexedFaceSetStatement();
				break;
			}
			case LITERAL_IndexedLineSet:
			{
				indexedLineSetStatement();
				break;
			}
			case ID:
			{
				unknownStatement();
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
			consume();
			consumeUntil(_tokenSet_2);
		}
	}
	
	public final void separatorStatement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_Separator);
			
							SceneGraphComponent sgc = new SceneGraphComponent();
							currentSGC.addChild(sgc);
							currentSGC = sgc;
							currentPath.push(sgc);
						
			match(OPEN_BRACE);
			{
			_loop9:
			do {
				if ((_tokenSet_1.member(LA(1)))) {
					statement();
				}
				else {
					break _loop9;
				}
				
			} while (true);
			}
			match(CLOSE_BRACE);
			
							currentPath.pop();
							currentSGC = currentPath.getLastComponent();
							if (VRMLHelper.verbose) System.err.println("Got Separator"); 
						
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
	}
	
	public final void infoStatement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_Info);
			match(OPEN_BRACE);
			{
			_loop12:
			do {
				if ((LA(1)==LITERAL_string)) {
					infoAttribute();
				}
				else {
					break _loop12;
				}
				
			} while (true);
			}
			match(CLOSE_BRACE);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
	}
	
	public final void transformStatement() throws RecognitionException, TokenStreamException {
		
		FactoredMatrix fm = new FactoredMatrix();
		
		try {      // for error handling
			match(LITERAL_Transform);
			match(OPEN_BRACE);
			{
			_loop16:
			do {
				if ((LA(1)==LITERAL_rotation||LA(1)==LITERAL_center)) {
					transformAttribute(fm);
				}
				else {
					break _loop16;
				}
				
			} while (true);
			}
			match(CLOSE_BRACE);
			
					// TODO check to be sure the transform not already set; if it is,
					// make child
					currentSGC.setTransformation(new Transformation( fm.getArray())); 
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
	}
	
	public final double[]  rotationStatement() throws RecognitionException, TokenStreamException {
		double[] mat;
		
		mat = null; double[] t = null;
		
		try {      // for error handling
			match(LITERAL_Rotation);
			match(OPEN_BRACE);
			match(LITERAL_rotation);
			t=sfrotationValue();
			match(CLOSE_BRACE);
			
						if (VRMLHelper.verbose) System.err.println("Got Rotation");
						double[] axis = new double[]{t[0], t[1], t[2]};
						mat = P3.makeRotationMatrix(null, axis, t[3]);
						if (currentSGC.getTransformation() == null)	
							currentSGC.setTransformation( new Transformation());
						currentSGC.getTransformation().multiplyOnRight(mat); 
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
		return mat;
	}
	
	public final double[]  scaleStatement() throws RecognitionException, TokenStreamException {
		double[] mat;
		
		mat = null; double[] t = null;
		
		try {      // for error handling
			match(LITERAL_Scale);
			match(OPEN_BRACE);
			match(LITERAL_scaleFactor);
			t=sfvec3fValue();
			match(CLOSE_BRACE);
			
						if (VRMLHelper.verbose) System.err.println("Got Scale");
						mat = P3.makeStretchMatrix(null, t);
						if (currentSGC.getTransformation() == null)	
							currentSGC.setTransformation( new Transformation());
						currentSGC.getTransformation().multiplyOnRight(mat); 
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
		return mat;
	}
	
	public final double[]  matrixTransformStatement() throws RecognitionException, TokenStreamException {
		double[] mat;
		
		mat = null;
		
		try {      // for error handling
			match(LITERAL_MatrixTransform);
			match(OPEN_BRACE);
			mat=sffloatValues();
			match(CLOSE_BRACE);
			
					// TODO move this up to the "invoking" rule
					currentSGC.setTransformation(new Transformation( mat)); 
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
		return mat;
	}
	
	public final void shapeHintsStatement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_ShapeHints);
			match(OPEN_BRACE);
			{
			_loop24:
			do {
				if ((_tokenSet_4.member(LA(1)))) {
					shapeHintAttribute();
				}
				else {
					break _loop24;
				}
				
			} while (true);
			}
			match(CLOSE_BRACE);
			if (VRMLHelper.verbose) System.err.println("Got ShapeHints");
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
	}
	
	public final Appearance  materialStatement() throws RecognitionException, TokenStreamException {
		Appearance ap;
		
		ap = new Appearance();
		
		try {      // for error handling
			match(LITERAL_Material);
			match(OPEN_BRACE);
			{
			_loop30:
			do {
				if (((LA(1) >= LITERAL_ambientColor && LA(1) <= LITERAL_shininess))) {
					materialAttribute(ap);
				}
				else {
					break _loop30;
				}
				
			} while (true);
			}
			match(CLOSE_BRACE);
			
					if (VRMLHelper.verbose) System.err.println("Got Material"); 
					currentSGC.setAppearance(ap);
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
		return ap;
	}
	
	public final DataList  coordinate3Statement() throws RecognitionException, TokenStreamException {
		DataList dl;
		
		dl = null;  double[] points = null;
		
		try {      // for error handling
			match(37);
			match(OPEN_BRACE);
			match(LITERAL_point);
			points=mfvec3fValue();
			match(CLOSE_BRACE);
			
						if (VRMLHelper.verbose)	{
							System.err.println("Got Coordinate3");
							System.err.println("Points: "+Rn.toString(points));
						}
						
						dl = currentCoordinate3 = StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(points);
					 	
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
		return dl;
	}
	
	public final DataList  normalStatement() throws RecognitionException, TokenStreamException {
		DataList dl;
		
		dl = null;  double[] normals = null;
		
		try {      // for error handling
			match(LITERAL_Normal);
			match(OPEN_BRACE);
			match(LITERAL_vector);
			normals=mfvec3fValue();
			match(CLOSE_BRACE);
			
					if (VRMLHelper.verbose)	System.err.println("Got Normal"); 
					dl = currentNormal = StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(normals);
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
		return dl;
	}
	
	public final IndexedFaceSet  indexedFaceSetStatement() throws RecognitionException, TokenStreamException {
		IndexedFaceSet ifs;
		
		ifs = null;
		
		try {      // for error handling
			match(LITERAL_IndexedFaceSet);
			match(OPEN_BRACE);
			{
			int _cnt36=0;
			_loop36:
			do {
				if ((LA(1)==LITERAL_coordIndex||LA(1)==LITERAL_normalIndex)) {
					indexedFaceSetAttribute();
				}
				else {
					if ( _cnt36>=1 ) { break _loop36; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt36++;
			} while (true);
			}
			match(CLOSE_BRACE);
			
				if (VRMLHelper.verbose) System.err.println("Got IndexedFaceSet"); 
				IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
				ifsf.setVertexCount(currentCoordinate3.size());
				ifsf.setFaceCount(currentCoordinateIndex.length);
				ifsf.setVertexCoordinates(currentCoordinate3);
				ifsf.setFaceIndices(currentCoordinateIndex);
				// TODO handle other attributes, decide whether they are face/vertex attributes, etc.
				ifsf.setGenerateEdgesFromFaces(false);
				ifsf.setGenerateFaceNormals(true); // depends on whether face normals were set above!
				ifsf.setGenerateVertexNormals(true); // depends on whether face normals were set above!
				ifsf.refactor();
				ifs = ifsf.getIndexedFaceSet();
				primitiveCount++;
				polygonCount += ifs.getNumFaces();
				if (currentSGC.getGeometry() != null) currentSGC.setGeometry(ifs);
				else		{
					SceneGraphComponent sgc = new SceneGraphComponent();
					sgc.setGeometry(ifs);
					sgc.setAppearance(currentAp);
					currentSGC.addChild(sgc);
				}
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
		return ifs;
	}
	
	public final void indexedLineSetStatement() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_IndexedLineSet);
			match(OPEN_BRACE);
			{
			int _cnt40=0;
			_loop40:
			do {
				if ((LA(1)==LITERAL_coordIndex)) {
					indexedLineSetAttribute();
				}
				else {
					if ( _cnt40>=1 ) { break _loop40; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt40++;
			} while (true);
			}
			match(CLOSE_BRACE);
			if (VRMLHelper.verbose)	System.err.println("Got IndexedLineSet");
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
	}
	
	public final void unknownStatement() throws RecognitionException, TokenStreamException {
		
		String n = null;
		
		try {      // for error handling
			n=id();
			match(OPEN_BRACE);
			{
			_loop44:
			do {
				if ((LA(1)==ID)) {
					unknownAttribute();
				}
				else {
					break _loop44;
				}
				
			} while (true);
			}
			match(CLOSE_BRACE);
			System.err.println("Unrecognized keyword "+	n);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
	}
	
	public final void infoAttribute() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_string);
			sfstringValue();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_5);
		}
	}
	
	public final void sfstringValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(STRING);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_6);
		}
	}
	
	public final void transformAttribute(
		FactoredMatrix fm
	) throws RecognitionException, TokenStreamException {
		
		double[] rr = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_rotation:
			{
				match(LITERAL_rotation);
				rr=sfrotationValue();
				fm.setRotation(rr[3], rr[0], rr[1], rr[2]);
				break;
			}
			case LITERAL_center:
			{
				match(LITERAL_center);
				rr=sfvec3fValue();
				fm.setCenter(rr);
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
			consume();
			consumeUntil(_tokenSet_7);
		}
	}
	
	public final double[]  sfrotationValue() throws RecognitionException, TokenStreamException {
		double[] rv;
		
			double a,b,c,d; rv = null;
		
		try {      // for error handling
			a=number();
			b=number();
			c=number();
			d=number();
			rv = new double[]{a,b,c,d};
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_8);
		}
		return rv;
	}
	
	public final double[]  sfvec3fValue() throws RecognitionException, TokenStreamException {
		double[] vec3;
		
		vec3 = null;
		double a, b, c;
		
		try {      // for error handling
			a=number();
			b=number();
			c=number();
				
						if (collectingMFVec3)	{
							evil3Vec[0] = a;  evil3Vec[1] = b; evil3Vec[2] = c;
							vec3 = evil3Vec;
						} else 
							vec3 = new double[]{a,b,c}; 
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_9);
		}
		return vec3;
	}
	
	public final double[]  sffloatValues() throws RecognitionException, TokenStreamException {
		double[] dl;
		
		
			dl = null;
			Vector vl = new Vector();
			double d = 0;
		
		
		try {      // for error handling
			{
			int _cnt65=0;
			_loop65:
			do {
				if ((LA(1)==INT32||LA(1)==FLOAT)) {
					d=sffloatValue();
					vl.add(new Double(d));
				}
				else {
					if ( _cnt65>=1 ) { break _loop65; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt65++;
			} while (true);
			}
			dl = VRMLHelper.listToDoubleArray(vl);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_10);
		}
		return dl;
	}
	
	public final double[]  translationStatement() throws RecognitionException, TokenStreamException {
		double[] mat;
		
		mat = null;  double[] t = null;
		
		try {      // for error handling
			match(LITERAL_Translation);
			match(OPEN_BRACE);
			t=sfvec3fValue();
			match(CLOSE_BRACE);
			
						if (VRMLHelper.verbose) System.err.println("Got Translation");
						mat = P3.makeTranslationMatrix(null, t, Pn.EUCLIDEAN);
						if (currentSGC.getTransformation() == null)	
							currentSGC.setTransformation( new Transformation());
						currentSGC.getTransformation().multiplyOnRight(mat); 
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		return mat;
	}
	
	public final void shapeHintAttribute() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_vertexOrdering:
			{
				match(LITERAL_vertexOrdering);
				{
				switch ( LA(1)) {
				case LITERAL_COUNTERCLOCKWISE:
				{
					match(LITERAL_COUNTERCLOCKWISE);
					break;
				}
				case LITERAL_CLOCKWISE:
				{
					match(LITERAL_CLOCKWISE);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case LITERAL_shapeType:
			{
				match(LITERAL_shapeType);
				match(LITERAL_SOLID);
				break;
			}
			case LITERAL_faceType:
			{
				match(LITERAL_faceType);
				{
				switch ( LA(1)) {
				case LITERAL_CONVEX:
				{
					match(LITERAL_CONVEX);
					break;
				}
				case LITERAL_UNKNOWN_FACE_TYPE:
				{
					match(LITERAL_UNKNOWN_FACE_TYPE);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case LITERAL_creaseAngle:
			{
				match(LITERAL_creaseAngle);
				number();
				break;
			}
			case ID:
			{
				unknownAttribute();
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
			consume();
			consumeUntil(_tokenSet_11);
		}
	}
	
	public final double  number() throws RecognitionException, TokenStreamException {
		double d;
		
		Token  f = null;
		Token  g = null;
		d = 0;
		
		try {      // for error handling
			switch ( LA(1)) {
			case INT32:
			{
				{
				f = LT(1);
				match(INT32);
				d=Double.parseDouble(f.getText());
				}
				break;
			}
			case FLOAT:
			{
				{
				g = LT(1);
				match(FLOAT);
				d=Double.parseDouble(g.getText());
				}
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
			consume();
			consumeUntil(_tokenSet_12);
		}
		return d;
	}
	
	public final void unknownAttribute() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			id();
			value();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_11);
		}
	}
	
	public final void materialAttribute(
		Appearance ap
	) throws RecognitionException, TokenStreamException {
		
			Color[] c=null; double d = 0.0;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_ambientColor:
			{
				match(LITERAL_ambientColor);
				c=mfcolorValue();
				ap.setAttribute(CommonAttributes.AMBIENT_COLOR, c[0]);
				break;
			}
			case LITERAL_diffuseColor:
			{
				match(LITERAL_diffuseColor);
				c=mfcolorValue();
				ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, c[0]);
				break;
			}
			case LITERAL_specularColor:
			{
				match(LITERAL_specularColor);
				c=mfcolorValue();
				ap.setAttribute(CommonAttributes.SPECULAR_COLOR, c[0]);
				break;
			}
			case LITERAL_emissiveColor:
			{
				match(LITERAL_emissiveColor);
				c=mfcolorValue();
				break;
			}
			case LITERAL_transparency:
			{
				match(LITERAL_transparency);
				d=sffloatValue();
				ap.setAttribute(CommonAttributes.TRANSPARENCY, d);
				break;
			}
			case LITERAL_shininess:
			{
				match(LITERAL_shininess);
				d=sffloatValue();
				ap.setAttribute(CommonAttributes.SPECULAR_EXPONENT, d);
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
			consume();
			consumeUntil(_tokenSet_13);
		}
	}
	
	public final Color[]  mfcolorValue() throws RecognitionException, TokenStreamException {
		Color[] cl;
		
		cl = null;Color c = null;
		
		try {      // for error handling
			if ((LA(1)==INT32||LA(1)==FLOAT)) {
				c=sfcolorValue();
				cl = new Color[1];	cl[0] = c;
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==INT32||LA(2)==FLOAT)) {
				match(OPEN_BRACKET);
				cl=sfcolorValues();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_13);
		}
		return cl;
	}
	
	public final double  sffloatValue() throws RecognitionException, TokenStreamException {
		double d;
		
		d = 0;
		
		try {      // for error handling
			d=number();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_14);
		}
		return d;
	}
	
	public final double[]  mfvec3fValue() throws RecognitionException, TokenStreamException {
		double[] vec3array;
		
		vec3array = null;
		double[] onevec = null;
		
		
		try {      // for error handling
			if ((LA(1)==INT32||LA(1)==FLOAT)) {
				vec3array=sfvec3fValue();
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==INT32||LA(2)==FLOAT)) {
				match(OPEN_BRACKET);
				vec3array=sfvec3fValues();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_15);
		}
		return vec3array;
	}
	
	public final void indexedFaceSetAttribute() throws RecognitionException, TokenStreamException {
		
		int[] indices = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_coordIndex:
			{
				match(LITERAL_coordIndex);
				indices=mfint32Value();
				
									if (VRMLHelper.verbose) System.err.println("Got "+indices.length+"indices");
									currentCoordinateIndex = VRMLHelper.convertIndices(indices); 
								
				break;
			}
			case LITERAL_normalIndex:
			{
				match(LITERAL_normalIndex);
				indices=mfint32Value();
				
									if (VRMLHelper.verbose) System.err.println("Got "+indices.length+"indices");
									currentNormalIndex = VRMLHelper.convertIndices(indices); 
								
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
			consume();
			consumeUntil(_tokenSet_16);
		}
	}
	
	public final int[]  mfint32Value() throws RecognitionException, TokenStreamException {
		int[] i;
		
		i = null; int t = 0;
		
		try {      // for error handling
			if ((LA(1)==INT32)) {
				t=sfint32Value();
				i = new int[1];  i[0] = t;
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==INT32)) {
				match(OPEN_BRACKET);
				i=sfint32Values();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_16);
		}
		return i;
	}
	
	public final void indexedLineSetAttribute() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_coordIndex);
			mfint32Value();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_17);
		}
	}
	
	public final void value() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				id();
				break;
			}
			case LITERAL_true:
			case LITERAL_TRUE:
			case LITERAL_false:
			case LITERAL_FALSE:
			{
				sfboolValue();
				break;
			}
			case STRING:
			{
				sfstringValue();
				break;
			}
			case OPEN_BRACE:
			{
				match(OPEN_BRACE);
				{
				_loop49:
				do {
					if ((LA(1)==ID)) {
						unknownAttribute();
					}
					else {
						break _loop49;
					}
					
				} while (true);
				}
				match(CLOSE_BRACE);
				break;
			}
			default:
				if ((LA(1)==INT32||LA(1)==FLOAT) && (_tokenSet_18.member(LA(2)))) {
					sffloatValues();
				}
				else if ((LA(1)==INT32||LA(1)==FLOAT||LA(1)==OPEN_BRACKET) && (_tokenSet_19.member(LA(2)))) {
					mffloatValue();
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_11);
		}
	}
	
	public final boolean  sfboolValue() throws RecognitionException, TokenStreamException {
		boolean b;
		
		b = false;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_true:
			case LITERAL_TRUE:
			{
				{
				switch ( LA(1)) {
				case LITERAL_true:
				{
					match(LITERAL_true);
					break;
				}
				case LITERAL_TRUE:
				{
					match(LITERAL_TRUE);
					b = true;
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case LITERAL_false:
			case LITERAL_FALSE:
			{
				{
				switch ( LA(1)) {
				case LITERAL_false:
				{
					match(LITERAL_false);
					break;
				}
				case LITERAL_FALSE:
				{
					match(LITERAL_FALSE);
					b = false;
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
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
			consume();
			consumeUntil(_tokenSet_11);
		}
		return b;
	}
	
	public final double[]  mffloatValue() throws RecognitionException, TokenStreamException {
		double[] dl;
		
		double d = 0; dl = null;
		
		try {      // for error handling
			if ((LA(1)==INT32||LA(1)==FLOAT)) {
				d=sffloatValue();
				dl = new double[]{d};
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==CLOSE_BRACKET)) {
				match(OPEN_BRACKET);
				match(CLOSE_BRACKET);
			}
			else if ((LA(1)==OPEN_BRACKET) && (LA(2)==INT32||LA(2)==FLOAT)) {
				match(OPEN_BRACKET);
				dl=sffloatValues();
				match(CLOSE_BRACKET);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_11);
		}
		return dl;
	}
	
	public final Color  sfcolorValue() throws RecognitionException, TokenStreamException {
		Color c;
		
		c = null; double r, g, b;
		
		try {      // for error handling
			r=number();
			b=number();
			g=number();
			c = new Color( (float)r, (float) g, (float) b);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_20);
		}
		return c;
	}
	
	public final Color[]  sfcolorValues() throws RecognitionException, TokenStreamException {
		Color[] cl;
		
		cl = null; 	Color c = null; Vector collect = new Vector();
		
		try {      // for error handling
			{
			int _cnt60=0;
			_loop60:
			do {
				if ((LA(1)==INT32||LA(1)==FLOAT)) {
					c=sfcolorValue();
					collect.add(c);
				}
				else {
					if ( _cnt60>=1 ) { break _loop60; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt60++;
			} while (true);
			}
			cl = VRMLHelper.listToColorArray(collect);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_21);
		}
		return cl;
	}
	
	public final int  sfint32Value() throws RecognitionException, TokenStreamException {
		int i;
		
		Token  f = null;
		i = 0;
		
		try {      // for error handling
			f = LT(1);
			match(INT32);
			i = Integer.parseInt(f.getText());
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_22);
		}
		return i;
	}
	
	public final int[]  sfint32Values() throws RecognitionException, TokenStreamException {
		int[] il;
		
		
			il = null;
			Vector vl = new Vector();
			int t = 0;
			int count = 0;
		
		
		try {      // for error handling
			{
			int _cnt70=0;
			_loop70:
			do {
				if ((LA(1)==INT32)) {
					t=sfint32Value();
					is[count++] = t;
				}
				else {
					if ( _cnt70>=1 ) { break _loop70; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt70++;
			} while (true);
			}
			il = new int[count];
					System.arraycopy(is,0,il,0,count);
					//VRMLHelper.listToIntArray(vl); 
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_21);
		}
		return il;
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
			consume();
			consumeUntil(_tokenSet_21);
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
			consume();
			consumeUntil(_tokenSet_0);
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
			consume();
			consumeUntil(_tokenSet_0);
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
			consume();
			consumeUntil(_tokenSet_21);
		}
	}
	
	public final void sfvec2fValue() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			number();
			number();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_23);
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
			consume();
			consumeUntil(_tokenSet_21);
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
			consume();
			consumeUntil(_tokenSet_0);
		}
	}
	
	public final double[]  sfvec3fValues() throws RecognitionException, TokenStreamException {
		double[] vec3array;
		
		vec3array = null;
		//List collect = new Vector();
		double[] onevec  = null;
		collectingMFVec3 = true;
		int count = 0;
			
		
		try {      // for error handling
			{
			int _cnt89=0;
			_loop89:
			do {
				if ((LA(1)==INT32||LA(1)==FLOAT)) {
					onevec=sfvec3fValue();
						
								if (count + 3 >= ds.length)	{
									// Reallocate!
									ds = VRMLHelper.reallocate(ds);
								}
								for (int i=0; i<3; ++i)	ds[count+i] = onevec[i];
								count += 3;
								//collect.add(onevec);
							
				}
				else {
					if ( _cnt89>=1 ) { break _loop89; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt89++;
			} while (true);
			}
			
						vec3array = new double[count];
						System.arraycopy(ds, 0, vec3array, 0, count);
						//vec3array = VRMLHelper.listToDoubleArrayArray(collect);
						collectingMFVec3 = false; 
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_21);
		}
		return vec3array;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"HEADER",
		"\"DEF\"",
		"\"USE\"",
		"\"Separator\"",
		"OPEN_BRACE",
		"CLOSE_BRACE",
		"\"Info\"",
		"\"string\"",
		"\"Transform\"",
		"\"rotation\"",
		"\"center\"",
		"\"MatrixTransform\"",
		"\"Translation\"",
		"\"Rotation\"",
		"\"Scale\"",
		"\"scaleFactor\"",
		"\"ShapeHints\"",
		"\"vertexOrdering\"",
		"\"COUNTERCLOCKWISE\"",
		"\"CLOCKWISE\"",
		"\"shapeType\"",
		"\"SOLID\"",
		"\"faceType\"",
		"\"CONVEX\"",
		"\"UNKNOWN_FACE_TYPE\"",
		"\"creaseAngle\"",
		"\"Material\"",
		"\"ambientColor\"",
		"\"diffuseColor\"",
		"\"specularColor\"",
		"\"emissiveColor\"",
		"\"transparency\"",
		"\"shininess\"",
		"\"Coordinate3\"",
		"\"point\"",
		"\"Normal\"",
		"\"vector\"",
		"\"IndexedFaceSet\"",
		"\"coordIndex\"",
		"\"normalIndex\"",
		"\"IndexedLineSet\"",
		"an identifier",
		"INT32",
		"FLOAT",
		"\"true\"",
		"\"TRUE\"",
		"\"false\"",
		"\"FALSE\"",
		"STRING",
		"OPEN_BRACKET",
		"CLOSE_BRACKET",
		"PERIOD",
		"ID_LETTER",
		"INT_OR_FLOAT",
		"DIGIT",
		"DECIMAL_BEGIN",
		"EXPONENT",
		"ESC",
		"RESTLINE",
		"HEADER1",
		"COMMENT",
		"WS_",
		"IGNORE"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 55663851377888L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 55663851378402L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 17999694239537122L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 35184994942976L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 2560L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 22553183131798018L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 25088L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 18225504742040066L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 18225504742040064L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 18049583504425472L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 35184994943488L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 18260825028452866L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 135291470336L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 18260825028428288L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 512L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 13194139533824L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 4398046511616L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 246291227476480L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 18260689736958464L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 18225640033485312L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 18014398509481984L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 18097961393193472L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 18225504742014978L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	
	}
