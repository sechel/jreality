// $ANTLR 2.7.5 (20050128): "vrml-v1.0.g" -> "VRMLV1Parser.java"$

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
 * The VRML version 1.0 Parser
 *****************************************************************************
 */
public class VRMLV1Parser extends antlr.LLkParser       implements VRMLV1ParserTokenTypes
 {
	
	// Entwurfsentscheidung:
	// - hier werden keine Indexed*(-Set) zusammengefast!
	// - es wird auf globale Variablen weitgehend verzichtet
	//		statdessen gibt es die Klasse State die alle wichtigen Daten enthaelt
	// - das einhaengen der Geometrieen geschieht in shapeNode
	//		colorieren und die Normalen werden direkt bei der Geometrie behandelt
	// - die durchgereichten States werden stets direkt geaendert.
	// 		nur beim erschaffen eines neuen Knotens und beim erstellen einer Geometrie 
	// 		(oder DEF & USE) werden die States Kopiert ((als Kopie abgezweigt))
	// - Regeln die den State veraendern haben deshalb KEINEN Rueckgabewert 
	// 		da sie auf dem Original arbeiten.
	// - leider koennen einige Geometrieen nicht direkt erstellt werden:
	//		fuer ihre Richtigen Abmessungen muss man (noch) eine Transformationsmatrix
	//		in ihren Eltern-Knoten legen (Bsp: Sphere mit RADIUS = 3).
	// 		Desshalb gibt es im State eine Matrix extraGeoTrans die dufuer ungleich 
	//		der Identitaet gesetzt wird. Sie wird im ShapeNode behandelt.
	// - sfbitmaskValue werden eine Liste mit zulaessigen Namen uebergeben
	//		zurueckgegebewn wird ein neuer BooleanArray der fuer die  
	//		geparsten Namen den entsprechenden Flag auf true gesetzt hat
	// - Erkennungs-Problem zwischen double(float) und int(long):
	//		betrachte filefragment: " 2   .5    1.5 "
	//		der Lexer erkennt entweder [2] [.5] [1.5] als  Double-Double-Double
	//      oder [2] [.] [5] [1] [.] [5] als  int-DOT-int-int-DOT-int
	//		Da die Zeichen nur durch WhiteSpaces getrennt sind,
	//		muessen wir entweder:
	//			- alles als Dezimalzahlen lexen und im Kontext wissen
	//				ob wir ein double oder int erwarten.
	//			- oder beim Lexen aufwendig erkennen ob es ein double oder int ist.
	//				wobei ein int ja stets auch ein double ist!
	//				Dann im Gebrauch erfragen ob int oder Double vorhanden ist.
	//			- Zusammenparsen von (int-Dot-int) fuehrt zu Fehlern!
	//				Bsp oben: [2.5] [1.5] als double double
	// 		Entscheidung: wir lexen nur	positive Dezimalzahlen da hier im Kontext stets
	//				klar ist, ob int, double oder boolean gefordert ist
	//				'+' und '-' und 'e' werden hinzugeparst,
	//				der Exponent wird als double gelext und hinzugeparst 
	//				bsp1: -0.34e+12 
	//				gelext: [-] [0.34] [e] [+] [12] 
	//						als '-' DOUBLE 'e' '+' DOUBLE
	//				bsp2: 12  .3 4 e12.2
	//				gelext:  [12] [.3] [4] [e] [12.2]
	//				geparst: [12] [0.3] [4*10^(12.2)] 
	//		    Ein dezimaler Exponent wird also wirklich einer. Aber mit Warnung.
	//				bsp3: "12.3e4 .3" 
	//				gelext: [12.3] [e] [4] [.3]
	//				geparst: [12.3*10^4] [0.3]
	// 	- Weil '.' als Teil einer Zahl gelext wird kann es nicht als Token zur verfuegung stehen!

	// TODO: 
	// Texture


	// TODO: mehrerfache Kameras(-Pfade)
	// hier : die erste Kamera gilt
	SceneGraphPath camPath = null;
	SceneGraphComponent root = null;

//	int currentNormalBinding = VRMLHelper.DEFAULT;
//	int currentMaterialBinding = VRMLHelper.DEFAULT;
	
	// we use a dynamic allocation scheme, beginning with arrays of length 10000
	final int INITIAL_SIZE = 10000;
	double[] ds = new double[INITIAL_SIZE];			// for storing double arrays
	Color[] cs = new Color[INITIAL_SIZE];			// for storing Color arrays
	int[] ls = new int[INITIAL_SIZE];				// for storing int arrays
	String [] strs = new String [INITIAL_SIZE]; 	// for storing vec2 & vec3 arrays
	double [][] vecs = new double [INITIAL_SIZE][]; // for storing vec2 & vec3 arrays
	
//	double[] tempVector3 = new double[3];
//	boolean collectingMFVec3 = false;		
	
	DefUseData defs= new DefUseData(); // class that manages the Def & Use
	int numOfSwitchNodes=0;

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
		SceneGraphComponent r=null;
		
		
		try {      // for error handling
			match(HEADER);
			
						// Start-State initialisieren:
						root = new SceneGraphComponent();
						SceneGraphPath p = new SceneGraphPath();
						p.push(root);
						Transformation t = null;
						State state = new State();
						state.diffuse=new Color[]{new Color(0,0,1f)};
						state.trafo=t;
						state.currNode=root;
						state.camPath=p;
						state.transparency= new double[]{1};
						root.setAppearance(VRMLHelper.defaultApp());
						
			{
			switch ( LA(1)) {
			case LITERAL_DEF:
			case LITERAL_USE:
			case LITERAL_WWWInline:
			case LITERAL_WWWAnchor:
			case LITERAL_LOD:
			case LITERAL_Separator:
			case LITERAL_Switch:
			case LITERAL_AsciiText:
			case LITERAL_Cone:
			case LITERAL_Cube:
			case LITERAL_Cylinder:
			case LITERAL_IndexedFaceSet:
			case LITERAL_IndexedLineSet:
			case LITERAL_PointSet:
			case LITERAL_Sphere:
			case LITERAL_FontStyle:
			case 38:
			case LITERAL_Info:
			case LITERAL_Material:
			case LITERAL_MaterialBinding:
			case LITERAL_Normal:
			case LITERAL_NormalBinding:
			case 53:
			case 58:
			case 63:
			case LITERAL_ShapeHints:
			case LITERAL_MatrixTransform:
			case LITERAL_Rotation:
			case LITERAL_Scale:
			case LITERAL_Transform:
			case LITERAL_Translation:
			case LITERAL_PerspectiveCamera:
			case LITERAL_OrthographicCamera:
			case LITERAL_PointLight:
			case LITERAL_SpotLight:
			case LITERAL_DirectionalLight:
			case ID:
			{
				statement(state);
				state.history = state.history+"*";
				{
				_loop4:
				do {
					if ((_tokenSet_0.member(LA(1)))) {
						statement(state);
						state.history = state.history+"*";
												   System.err.println("Warning: multiple basic Nodes"
													 +"found (and processed). This is not VRML-Standard !");
					}
					else {
						break _loop4;
					}
					
				} while (true);
				}
				break;
			}
			case EOF:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			r = root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
		return r;
	}
	
	private final void statement(
		State state
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_DEF:
			{
				defNode(state);
				break;
			}
			case LITERAL_USE:
			{
				useNode(state);
				break;
			}
			case LITERAL_WWWInline:
			case LITERAL_WWWAnchor:
			case LITERAL_LOD:
			case LITERAL_Separator:
			case LITERAL_Switch:
			case LITERAL_AsciiText:
			case LITERAL_Cone:
			case LITERAL_Cube:
			case LITERAL_Cylinder:
			case LITERAL_IndexedFaceSet:
			case LITERAL_IndexedLineSet:
			case LITERAL_PointSet:
			case LITERAL_Sphere:
			case LITERAL_FontStyle:
			case 38:
			case LITERAL_Info:
			case LITERAL_Material:
			case LITERAL_MaterialBinding:
			case LITERAL_Normal:
			case LITERAL_NormalBinding:
			case 53:
			case 58:
			case 63:
			case LITERAL_ShapeHints:
			case LITERAL_MatrixTransform:
			case LITERAL_Rotation:
			case LITERAL_Scale:
			case LITERAL_Transform:
			case LITERAL_Translation:
			case LITERAL_PerspectiveCamera:
			case LITERAL_OrthographicCamera:
			case LITERAL_PointLight:
			case LITERAL_SpotLight:
			case LITERAL_DirectionalLight:
			case ID:
			{
				node(state);
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
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void defNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.print(state.history+"Def Node(");
		String name="";
		State fake = DefUseData.defState(state);
		
		
		try {      // for error handling
			match(LITERAL_DEF);
			name=sfstringValue();
			if (VRMLHelper.verbose) System.err.println(name+"):");
			statement(fake);
			
					defs.addDef(fake,name);
					if (VRMLHelper.verbose) System.err.print("DEF End(");
					defs.use(state,name); 
					if (VRMLHelper.verbose) System.err.println("DEF used)");
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void useNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.print(state.history+"Use Node(");
		String name="";
		
		
		try {      // for error handling
			match(LITERAL_USE);
			name=sfstringValue();
			if (VRMLHelper.verbose) System.err.print(name);
			
					defs.use(state,name);
					if (VRMLHelper.verbose) System.err.println(")");
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void node(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.print(state.history+"Got Node: ");
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_WWWAnchor:
			case LITERAL_LOD:
			case LITERAL_Separator:
			case LITERAL_Switch:
			{
				groupNode(state);
				state.defTyp=DefUseData.KNOT;
				break;
			}
			case LITERAL_AsciiText:
			case LITERAL_Cone:
			case LITERAL_Cube:
			case LITERAL_Cylinder:
			case LITERAL_IndexedFaceSet:
			case LITERAL_IndexedLineSet:
			case LITERAL_PointSet:
			case LITERAL_Sphere:
			{
				shapeNode(state);
				state.defTyp=DefUseData.KNOT;
				break;
			}
			case LITERAL_FontStyle:
			case 38:
			case LITERAL_Info:
			case LITERAL_Material:
			case LITERAL_MaterialBinding:
			case LITERAL_Normal:
			case LITERAL_NormalBinding:
			case 53:
			case 58:
			case 63:
			case LITERAL_ShapeHints:
			{
				propertyGeoNAppNode(state);
				break;
			}
			case LITERAL_MatrixTransform:
			case LITERAL_Rotation:
			case LITERAL_Scale:
			case LITERAL_Transform:
			case LITERAL_Translation:
			{
				propertyMatrixTransformNode(state);
				state.defTyp=DefUseData.TRAFO;
				break;
			}
			case LITERAL_WWWInline:
			case LITERAL_PerspectiveCamera:
			case LITERAL_OrthographicCamera:
			case LITERAL_PointLight:
			case LITERAL_SpotLight:
			case LITERAL_DirectionalLight:
			{
				specialNode(state);
				break;
			}
			case ID:
			{
				strangeNode();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final String  sfstringValue() throws RecognitionException, TokenStreamException {
		String s="";
		
		Token  g = null;
		Token  h = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				g = LT(1);
				match(ID);
				s = g.getText();
				break;
			}
			case STRING:
			{
				h = LT(1);
				match(STRING);
				s = h.getText();
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
		return s;
	}
	
	private final void groupNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.print("group Node: ");
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_Separator:
			{
				separatorNode(state);
				break;
			}
			case LITERAL_Switch:
			{
				switchNode(state);
				break;
			}
			case LITERAL_WWWAnchor:
			{
				match(LITERAL_WWWAnchor);
				egal();
				break;
			}
			case LITERAL_LOD:
			{
				match(LITERAL_LOD);
				egal();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void shapeNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		
		if (VRMLHelper.verbose) System.err.print("Shape Node: ");
		State state2= new State(state);
		PointSet geo=null;
		Appearance app= new Appearance();
		SceneGraphComponent sgc= new SceneGraphComponent();
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_AsciiText:
			{
				geo=asciiTextNode(state2,app);
				break;
			}
			case LITERAL_Cone:
			{
				geo=coneNode(state2,app);
				break;
			}
			case LITERAL_Cube:
			{
				geo=cubeNode(state2,app);
				break;
			}
			case LITERAL_Cylinder:
			{
				geo=cylinderNode(state2,app);
				break;
			}
			case LITERAL_IndexedFaceSet:
			{
				geo=indexedFaceSetNode(state2,app);
				break;
			}
			case LITERAL_IndexedLineSet:
			{
				geo=indexedLineSetNode(state2,app);
				break;
			}
			case LITERAL_PointSet:
			{
				geo=pointSetNode(state2,app);
				break;
			}
			case LITERAL_Sphere:
			{
				geo=sphereNode(state2,app);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
				
						if (geo==null){System.out.println("failure in geometry. Abort Node!");}
						else{
							sgc.setName(geo.getName());
							state2.currNode.addChild(sgc);
							sgc.setGeometry(geo);
							state2.setColorApp(app,false);
							sgc.setAppearance(app);
							state2.setTrafo(sgc);
						}
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void propertyGeoNAppNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.print("prop Geo Node: ");
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case 38:
			{
				coordinate3Node(state);
				state.defTyp=DefUseData.COORDS;
				break;
			}
			case LITERAL_FontStyle:
			{
				match(LITERAL_FontStyle);
				egal();
				break;
			}
			case LITERAL_Info:
			{
				infoNode(state);
				break;
			}
			case LITERAL_Material:
			{
				materialNode(state);
				state.defTyp=DefUseData.MATERIAL;
				break;
			}
			case LITERAL_MaterialBinding:
			{
				materialBindingNode(state);
				state.defTyp=DefUseData.BIND_M;
				break;
			}
			case LITERAL_Normal:
			{
				normalNode(state);
				state.defTyp=DefUseData.NORMALS;
				break;
			}
			case LITERAL_NormalBinding:
			{
				normalBindingNode(state);
				state.defTyp=DefUseData.BIND_N;
				break;
			}
			case 53:
			{
				texture2Node(state);
				state.defTyp=DefUseData.TEXTURE;
				break;
			}
			case 58:
			{
				texture2TransformNode(state);
				state.defTyp=DefUseData.TEXTURE_TRAFO;
				break;
			}
			case 63:
			{
				textureCoordinate2Node(state);
				state.defTyp=DefUseData.TEXTURE_COORDS;
				break;
			}
			case LITERAL_ShapeHints:
			{
				shapeHintsNode(state);
				state.defTyp=DefUseData.SHAPE_HINTS;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void propertyMatrixTransformNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		
		if (VRMLHelper.verbose) System.err.print("Prop Matrix Node: ");
		Transformation m= new Transformation();
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_MatrixTransform:
			{
				m=matrixTransformNode();
				break;
			}
			case LITERAL_Rotation:
			{
				m=rotationNode();
				break;
			}
			case LITERAL_Scale:
			{
				m=scaleNode();
				break;
			}
			case LITERAL_Transform:
			{
				m=transformNode();
				break;
			}
			case LITERAL_Translation:
			{
				m=translationNode();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
				
					 if (state.trafo==null)
					 	state.trafo= new Transformation();
					 state.trafo.multiplyOnRight(m.getMatrix());
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void specialNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.print("special Node: ");
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_PerspectiveCamera:
			case LITERAL_OrthographicCamera:
			{
				camNode(state);
				state.defTyp=DefUseData.KNOT; System.out.println("def-Cam Problem?");
				break;
			}
			case LITERAL_PointLight:
			case LITERAL_SpotLight:
			case LITERAL_DirectionalLight:
			{
				lightNode(state);
				state.defTyp=DefUseData.KNOT;
				break;
			}
			case LITERAL_WWWInline:
			{
				match(LITERAL_WWWInline);
				egal();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void strangeNode() throws RecognitionException, TokenStreamException {
		
		String s;
		
		try {      // for error handling
			s=id();
			egal();
			System.out.println("unknown Node:"+s+" -Node ignored!");
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void camNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.print("Cam Node: ");
		Camera c=null;
		State state2= new State(state);
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_OrthographicCamera:
			{
				c=orthographicCameraNode(state2);
				break;
			}
			case LITERAL_PerspectiveCamera:
			{
				c=perspectiveCameraNode(state2);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			
						SceneGraphComponent sgc= new SceneGraphComponent();
						state.currNode.addChild(sgc);
						sgc.setName(c.getName());
						if (c!=null){
							//TODO3 mehrere KameraPfade
							if (camPath==null) 	camPath=state.camPath;
							sgc.setCamera(c);
							state2.setTrafo(sgc);
						}
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void lightNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.print("Light Node: ");
		Light l= null;
		State state2= new State(state);
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_DirectionalLight:
			{
				l=directionalLightNode(state2);
				break;
			}
			case LITERAL_PointLight:
			{
				l=pointLightNode(state2);
				break;
			}
			case LITERAL_SpotLight:
			{
				l=spotLightNode(state2);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			
					SceneGraphComponent sgc= new SceneGraphComponent();
					state2.currNode.addChild(sgc);
					sgc.setName(l.getName());
					sgc.setLight(l);
					state2.setTrafo(sgc);
					
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void egal() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			{
				{
				match(LPAREN);
				{
				_loop200:
				do {
					if ((_tokenSet_4.member(LA(1)))) {
						dumb();
					}
					else {
						break _loop200;
					}
					
				} while (true);
				}
				match(RPAREN);
				}
				break;
			}
			case OPEN_BRACE:
			{
				{
				match(OPEN_BRACE);
				{
				_loop203:
				do {
					if ((_tokenSet_4.member(LA(1)))) {
						dumb();
					}
					else {
						break _loop203;
					}
					
				} while (true);
				}
				match(CLOSE_BRACE);
				}
				break;
			}
			case OPEN_BRACKET:
			{
				{
				match(OPEN_BRACKET);
				{
				_loop206:
				do {
					if ((_tokenSet_4.member(LA(1)))) {
						dumb();
					}
					else {
						break _loop206;
					}
					
				} while (true);
				}
				match(CLOSE_BRACKET);
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
			recover(ex,_tokenSet_2);
		}
	}
	
	private final String  id() throws RecognitionException, TokenStreamException {
		String s;
		
		Token  n = null;
		s = "";
		
		try {      // for error handling
			n = LT(1);
			match(ID);
			s=n.getText();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
		return s;
	}
	
	private final void separatorNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		Token  g = null;
		
		if (VRMLHelper.verbose) System.err.println("Separator"); 
		State state2= new State(state);
		Transformation t= state2.trafo;
		state2.trafo=null;
		state2.history=state.history+"|";
		{ if (VRMLHelper.verbose) System.err.println(state.history+"\\"); }
				
		
		
		try {      // for error handling
			g = LT(1);
			match(LITERAL_Separator);
			
							SceneGraphComponent sgc = new SceneGraphComponent();
							if (t!=null)
								sgc.setTransformation(t);
							sgc.setName("Knot LineNo "+g.getLine()); // for looking up later
							state2.currNode.addChild(sgc);
							state2.currNode=sgc;
							state2.camPath.push(sgc);
						
			match(OPEN_BRACE);
			{
			_loop17:
			do {
				if ((_tokenSet_0.member(LA(1)))) {
					statement(state2);
				}
				else {
					break _loop17;
				}
				
			} while (true);
			}
			match(CLOSE_BRACE);
			if (VRMLHelper.verbose) System.err.println(state.history+"/");
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void switchNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		Token  g = null;
		
		if (VRMLHelper.verbose) System.err.println("switch"); 
		State state2= new State(state);
		Transformation t= state2.trafo;
		state2.trafo=null;
		state2.history=state.history+"-";
		{ if (VRMLHelper.verbose) System.err.println(state.history+"\\"); }
				
		
		
		try {      // for error handling
			g = LT(1);
			match(LITERAL_Switch);
			
							SceneGraphComponent sgc = new SceneGraphComponent();
							if (t!=null)
								sgc.setTransformation(t);
							state2.currNode.addChild(sgc);
							state2.currNode=sgc;
							state2.camPath.push(sgc);
							if (numOfSwitchNodes>0) 
								sgc.setVisible(false);
							numOfSwitchNodes++;
							sgc.setName("Switch Nr "+numOfSwitchNodes);
						
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case LITERAL_DEF:
			case LITERAL_USE:
			case LITERAL_WWWInline:
			case LITERAL_WWWAnchor:
			case LITERAL_LOD:
			case LITERAL_Separator:
			case LITERAL_Switch:
			case LITERAL_AsciiText:
			case LITERAL_Cone:
			case LITERAL_Cube:
			case LITERAL_Cylinder:
			case LITERAL_IndexedFaceSet:
			case LITERAL_IndexedLineSet:
			case LITERAL_PointSet:
			case LITERAL_Sphere:
			case LITERAL_FontStyle:
			case 38:
			case LITERAL_Info:
			case LITERAL_Material:
			case LITERAL_MaterialBinding:
			case LITERAL_Normal:
			case LITERAL_NormalBinding:
			case 53:
			case 58:
			case 63:
			case LITERAL_ShapeHints:
			case LITERAL_MatrixTransform:
			case LITERAL_Rotation:
			case LITERAL_Scale:
			case LITERAL_Transform:
			case LITERAL_Translation:
			case LITERAL_PerspectiveCamera:
			case LITERAL_OrthographicCamera:
			case LITERAL_PointLight:
			case LITERAL_SpotLight:
			case LITERAL_DirectionalLight:
			case ID:
			{
				statement(state2);
				break;
			}
			case CLOSE_BRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(CLOSE_BRACE);
			if (VRMLHelper.verbose) System.err.println(state.history+"/");
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final PointSet  asciiTextNode(
		State state, Appearance app
	) throws RecognitionException, TokenStreamException {
		PointSet label=null;
		
		
		if (VRMLHelper.verbose) System.err.print("Label( ");
		String just="LEFT";
		String [] text = new String[]{""};
		double spacing=1;
		String[] code= new String[]{ 	"LEFT","CENTER","RIGHT"	};
		double[] width =new double[]{0};
		
		
		try {      // for error handling
			match(LITERAL_AsciiText);
			match(OPEN_BRACE);
			{
			_loop24:
			do {
				switch ( LA(1)) {
				case LITERAL_string:
				{
					match(LITERAL_string);
					if (VRMLHelper.verbose) System.err.print("String ");
					text=mfstringValue();
					break;
				}
				case LITERAL_spacing:
				{
					match(LITERAL_spacing);
					if (VRMLHelper.verbose) System.err.print("spacing ");
					spacing=sffloatValue();
					break;
				}
				case LITERAL_justification:
				{
					match(LITERAL_justification);
					if (VRMLHelper.verbose) System.err.print("justif. ");
					just=sfenumValue();
					break;
				}
				case LITERAL_width:
				{
					match(LITERAL_width);
					if (VRMLHelper.verbose) System.err.print("width ");
					width=mffloatValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop24;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
						int justif = VRMLHelper.getEnum(code, just);
						String[] text2= new String[]{VRMLHelper.mergeStrings(text)};
						label = new PointSet();
						label.setNumPoints(1);
						double[][] d=new double[][]{{0,0,0}};
						label.setVertexAttributes(Attribute.COORDINATES,new DoubleArrayArray.Array(d));
						label.setVertexAttributes(Attribute.LABELS, new StringArray(text2));
						label.setName("Label");
			
						if (VRMLHelper.verbose) System.err.println(")");
						state.extraGeoTrans = new Transformation();		
						state.edgeDraw=2;
						state.vertexDraw=0;
						state.faceDraw=2;
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return label;
	}
	
	private final IndexedFaceSet  coneNode(
		State state, Appearance app
	) throws RecognitionException, TokenStreamException {
		IndexedFaceSet cone=null;
		
		//TODO3: material,texture
		if (VRMLHelper.verbose) System.err.print("Cone( ");
		String[]  parts = new String[]{"SIDES","BOTTOM","ALL"};
		boolean[] partsMask=null;
		double r=1;
		double h=2;
		boolean sideDraw=false;
		boolean bottomDraw=false;
		
		
		try {      // for error handling
			match(LITERAL_Cone);
			match(OPEN_BRACE);
			{
			_loop30:
			do {
				switch ( LA(1)) {
				case LITERAL_parts:
				{
					{
					match(LITERAL_parts);
					if (VRMLHelper.verbose) System.err.print("parts ");
					partsMask=sfbitmaskValue(parts);
					}
					break;
				}
				case LITERAL_bottomRadius:
				{
					{
					match(LITERAL_bottomRadius);
					if (VRMLHelper.verbose) System.err.print("bottomRadius ");
					r=sffloatValue();
					}
					break;
				}
				case LITERAL_height:
				{
					{
					match(LITERAL_height);
					if (VRMLHelper.verbose) System.err.print("height ");
					h=sffloatValue();
					}
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop30;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
				if (partsMask==null)		{bottomDraw=true; sideDraw=true;}
					else{
						if ((partsMask[2])|	!(partsMask[0]|partsMask[1]))
						    	{bottomDraw=true; sideDraw=true;}
						if (partsMask[0])	{sideDraw=true;	 }
						if (partsMask[1])	{bottomDraw=true;}
					}
					cone = VRMLHelper.cone(sideDraw,bottomDraw,20);
					cone.setName("cone"); 
					state.extraGeoTrans = new Transformation();
					state.edgeDraw=2;
					state.vertexDraw=2;
					state.faceDraw=0;
					MatrixBuilder.euclidean().scale(r,h,r).translate(0,-0.5,0).assignTo(state.extraGeoTrans);
					if (VRMLHelper.verbose) System.err.println(")");
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return cone;
	}
	
	private final IndexedFaceSet  cubeNode(
		State state, Appearance app
	) throws RecognitionException, TokenStreamException {
		IndexedFaceSet cube=null;
		
		//TODO3: material,texture
		if (VRMLHelper.verbose) System.err.print("Cube( ");
		double w=2;
		double h=2;
		double d=2;
		
		
		try {      // for error handling
			match(LITERAL_Cube);
			match(OPEN_BRACE);
			{
			_loop33:
			do {
				switch ( LA(1)) {
				case LITERAL_width:
				{
					match(LITERAL_width);
					if (VRMLHelper.verbose) System.err.print("width ");
					w=sffloatValue();
					break;
				}
				case LITERAL_height:
				{
					match(LITERAL_height);
					if (VRMLHelper.verbose) System.err.print("height ");
					h=sffloatValue();
					break;
				}
				case LITERAL_depth:
				{
					match(LITERAL_depth);
					if (VRMLHelper.verbose) System.err.print("depth ");
					d=sffloatValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop33;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
					cube = Primitives.cube(false);
					cube.setName("cube");
					state.extraGeoTrans = new Transformation();
					state.edgeDraw=2;
					state.vertexDraw=2;
					state.faceDraw=0;
					MatrixBuilder.euclidean().scale(w/2,h/2,d/2).assignTo(state.extraGeoTrans);
					if (VRMLHelper.verbose) System.err.println(")");
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return cube;
	}
	
	private final IndexedFaceSet  cylinderNode(
		State state, Appearance app
	) throws RecognitionException, TokenStreamException {
		IndexedFaceSet cylinder=null;
		
		//TODO3: material,texture
		if (VRMLHelper.verbose) System.err.print("Cylinder( ");
		String[]  parts = new String[]{"SIDES","TOP","BOTTOM","ALL"};
		boolean[] partsMask=null;
		double r=1;
		double h=2;
		
		
		try {      // for error handling
			match(LITERAL_Cylinder);
			match(OPEN_BRACE);
			{
			_loop36:
			do {
				switch ( LA(1)) {
				case LITERAL_parts:
				{
					match(LITERAL_parts);
					if (VRMLHelper.verbose) System.err.print("parts ");
					partsMask=sfbitmaskValue(parts);
					break;
				}
				case LITERAL_radius:
				{
					match(LITERAL_radius);
					if (VRMLHelper.verbose) System.err.print("radius ");
					r=sffloatValue();
					break;
				}
				case LITERAL_height:
				{
					match(LITERAL_height);
					if (VRMLHelper.verbose) System.err.print("height ");
					h=sffloatValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop36;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
					if (partsMask==null) 		
						cylinder = VRMLHelper.cylinder(true,true,true,20);
					else{
						if (partsMask[3])
							cylinder = VRMLHelper.cylinder(true,true,true,20);
						else 
							if (!(partsMask[0]|partsMask[1]|partsMask[2]))
								cylinder = VRMLHelper.cylinder(true,true,true,20);
							else
								cylinder = VRMLHelper.cylinder(partsMask[0],partsMask[1],partsMask[2],20);
					}
					cylinder.setName("cylinder");
					state.extraGeoTrans = new Transformation();
					state.edgeDraw=2;
					state.vertexDraw=2;
					state.faceDraw=0;
					MatrixBuilder.euclidean().scale(r,h,r).assignTo(state.extraGeoTrans);
					if (VRMLHelper.verbose) System.err.println(")");
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return cylinder;
	}
	
	private final IndexedFaceSet  indexedFaceSetNode(
		State state, Appearance app
	) throws RecognitionException, TokenStreamException {
		IndexedFaceSet ifs=null;
		
		//TODO3: texture
		if (VRMLHelper.verbose) System.err.print("IndexedFaceSet( "); 
		int[] coordIndex	= new int[]{0};
		int[] materialIndex	= new int[]{};
		int[] normalIndex	= new int[]{};
		int[] textureCoordIndex	= new int[]{};
		
		
		try {      // for error handling
			match(LITERAL_IndexedFaceSet);
			match(OPEN_BRACE);
			{
			_loop39:
			do {
				switch ( LA(1)) {
				case LITERAL_coordIndex:
				{
					match(LITERAL_coordIndex);
					if (VRMLHelper.verbose) System.err.print("coordIndex ");
					coordIndex=mflongValue();
					break;
				}
				case LITERAL_materialIndex:
				{
					match(LITERAL_materialIndex);
					if (VRMLHelper.verbose) System.err.print("materialIndex ");
					materialIndex=mflongValue();
					break;
				}
				case LITERAL_normalIndex:
				{
					match(LITERAL_normalIndex);
					if (VRMLHelper.verbose) System.err.print("normalIndex ");
					normalIndex=mflongValue();
					break;
				}
				case LITERAL_textureCoordIndex:
				{
					match(LITERAL_textureCoordIndex);
					if (VRMLHelper.verbose) System.err.print("textureCoordIndex ");
					textureCoordIndex=mflongValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop39;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
				int[][] coordIndex2 = VRMLHelper.convertIndexList(coordIndex);
			int[][] materialIndex2 = VRMLHelper.convertIndexList(materialIndex);
				int[][] normalIndex2 = VRMLHelper.convertIndexList(normalIndex);
				int[][] textureCoordIndex2 = VRMLHelper.convertIndexList(textureCoordIndex);
				
				IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
				ifsf.setVertexCount(state.coords.length);
				ifsf.setFaceCount(coordIndex2.length);
				ifsf.setVertexAttribute(Attribute.COORDINATES,new DoubleArrayArray.Array(state.coords) );
				ifsf.setFaceIndices(coordIndex2);
				if (state.normalBinding >=6 | state.materialBinding>=6 
					| state.textureFile.equals("")	|state.textureData.length!=0 ){
					// have to separate the vertices!
					double[][] vnormals=null;
					
						// make Normals now!		
					//TODO:
					//	 do we support this?: ifsf.setGenerateVertexNormals(true);
					// see also: VRMLHelper.setNormals
					//ifsf.update();
					//vnormals=ifsf.getIndexedFaceSet()
					//	.getVertexAttributes(Attribute.NORMALS)
					//	.toDoubleArrayArray(null);
					
						//	separate vertices
					int[] reffTab=VRMLHelper.separateVerticesAndVNormals(coordIndex2,state);
						// make new Factory
					ifsf = new IndexedFaceSetFactory();
					ifsf.setVertexCount(reffTab.length);//(new)
					ifsf.setFaceCount(coordIndex2.length);//(new)
					ifsf.setVertexAttribute(Attribute.COORDINATES,new DoubleArrayArray.Array(state.coords) );//new
					ifsf.setFaceIndices(coordIndex2);//new
						// set modified old normals
					//double[][] vnormalsNew= new double[reffTab.length][];
					//for(int i=0;i<reffTab.length;i++){
					//	vnormalsNew[i]=new double[]{
					//		vnormals[reffTab[i]][0],
					//		vnormals[reffTab[i]][1],
					//		vnormals[reffTab[i]][2]};
					//}
					// ifsf.setVertexAttribute(Attribute.NORMALS,new DoubleArrayArray.Array(vnormalsNew) );//new
					
					// now all indices of texture, color, normals, ect are unique because 
					// they are based on face indices and coords
				}
				VRMLHelper.setNormals(ifsf,coordIndex2,normalIndex2,state);//werden nicht generiert wenn vorhanden
				VRMLHelper.setColors(ifsf,coordIndex2,materialIndex2,state);
				ifsf.setGenerateEdgesFromFaces(false);
				ifsf.update();
				ifs = ifsf.getIndexedFaceSet();
				state.assignTexture(app, ifs);
				ifs.setName("Face Set");
				state.extraGeoTrans = new Transformation();
				state.edgeDraw=2;
				state.vertexDraw=2;
				state.faceDraw=0;
				if (VRMLHelper.verbose) System.err.println(")");
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return ifs;
	}
	
	private final IndexedLineSet  indexedLineSetNode(
		State state, Appearance app
	) throws RecognitionException, TokenStreamException {
		IndexedLineSet ils=null;
		
		//TODO3: normal,texture
		State state2= new State(state);
		if (VRMLHelper.verbose) System.err.print("IndexedLineSet( "); 
		int[] coordIndex	= new int[]{0};
		int[] materialIndex	= new int[]{};
		int[] normalIndex	= new int[]{};
		int[] textureCoordIndex	= new int[]{};
		
		
		try {      // for error handling
			match(LITERAL_IndexedLineSet);
			match(OPEN_BRACE);
			{
			_loop42:
			do {
				switch ( LA(1)) {
				case LITERAL_coordIndex:
				{
					match(LITERAL_coordIndex);
					if (VRMLHelper.verbose) System.err.print("coordIndex ");
					coordIndex=mflongValue();
					break;
				}
				case LITERAL_materialIndex:
				{
					match(LITERAL_materialIndex);
					if (VRMLHelper.verbose) System.err.print("materialIndex ");
					materialIndex=mflongValue();
					break;
				}
				case LITERAL_normalIndex:
				{
					match(LITERAL_normalIndex);
					if (VRMLHelper.verbose) System.err.print("normalIndex ");
					normalIndex=mflongValue();
					break;
				}
				case LITERAL_textureCoordIndex:
				{
					match(LITERAL_textureCoordIndex);
					if (VRMLHelper.verbose) System.err.print("textureCoordIndex ");
					textureCoordIndex=mflongValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop42;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
				int[][] coordIndex2 = VRMLHelper.convertIndexList(coordIndex);
			int[][] materialIndex2 = VRMLHelper.convertIndexList(materialIndex);
				int[][] normalIndex2 = VRMLHelper.convertIndexList(normalIndex);
				int[][] textureCoordIndex2 = VRMLHelper.convertIndexList(textureCoordIndex);
				
				IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
				ilsf.setVertexCount(state.coords.length);
				ilsf.setLineCount(coordIndex2.length);
				ilsf.setVertexAttribute(Attribute.COORDINATES,new DoubleArrayArray.Array(state2.coords) );
				ilsf.setEdgeIndices(coordIndex2);
				VRMLHelper.setColors(ilsf,coordIndex2,materialIndex2,state2);
				// TODO2: handle Normals
				// Normals:	if (normalIndex2.length>0){}else {}
				// Texture:	if (textureCoordIndex2.length>0){}else {}
			
				ilsf.update();
				ils = ilsf.getIndexedLineSet();
				ils.setName("Line Set");
				state.extraGeoTrans = new Transformation();
				state.vertexDraw=2;
				state.edgeDraw=0;
				state.faceDraw=0;
				if (VRMLHelper.verbose) System.err.println(")");
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return ils;
	}
	
	private final PointSet  pointSetNode(
		State state, Appearance app
	) throws RecognitionException, TokenStreamException {
		PointSet ps=null;
		
		//TODO3: material,normal
		if (VRMLHelper.verbose) System.err.print("PointSet( "); 
		int start=0;
		int num=-1;
		
		
		try {      // for error handling
			match(LITERAL_PointSet);
			match(OPEN_BRACE);
			{
			_loop45:
			do {
				switch ( LA(1)) {
				case LITERAL_startIndex:
				{
					match(LITERAL_startIndex);
					if (VRMLHelper.verbose) System.err.print("startIndex ");
					start=sflongValue();
					break;
				}
				case LITERAL_numPoints:
				{
					match(LITERAL_numPoints);
					if (VRMLHelper.verbose) System.err.print("numPoints ");
					num=sflongValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop45;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
				double[][] coords2 = new double[num][];
				System.arraycopy(state.coords,start,coords2,0,num);
				ps = new PointSet();
				ps.setNumPoints(num);
				ps.setVertexAttributes(Attribute.COORDINATES,new DoubleArrayArray.Array(coords2));
				// TODO2: handle Normals
				VRMLHelper.setColors(ps,state,start,num);
				ps.setName("Point Set");
				state.extraGeoTrans = new Transformation();
				state.vertexDraw=0;
				state.edgeDraw=0;
				state.faceDraw=0;
				{if (VRMLHelper.verbose) System.err.println(")");}
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return ps;
	}
	
	private final IndexedFaceSet  sphereNode(
		State state, Appearance app
	) throws RecognitionException, TokenStreamException {
		IndexedFaceSet sphere=null;
		
		//TODO3:texture
		if (VRMLHelper.verbose) System.err.print("Sphere( ");
		double r=1;
		
		
		try {      // for error handling
			match(LITERAL_Sphere);
			match(OPEN_BRACE);
			{
			_loop48:
			do {
				switch ( LA(1)) {
				case LITERAL_radius:
				{
					match(LITERAL_radius);
					if (VRMLHelper.verbose) System.err.print("radius ");
					r=sffloatValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop48;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
					sphere = Primitives.sphere(20);
					sphere.setName("Sphere");
					state.extraGeoTrans=new Transformation();
					MatrixBuilder.euclidean().scale(r).assignTo(state.extraGeoTrans);
					state.edgeDraw=2;
					state.vertexDraw=2;
					state.faceDraw=0;
					{if (VRMLHelper.verbose) System.err.println(")");}
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return sphere;
	}
	
	private final String []  mfstringValue() throws RecognitionException, TokenStreamException {
		String [] strl={};
		
		String str; 
		strs= new String[INITIAL_SIZE];
		int i=0;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case OPEN_BRACKET:
			{
				{
				match(OPEN_BRACKET);
				{
				switch ( LA(1)) {
				case CLOSE_BRACKET:
				{
					{
					match(CLOSE_BRACKET);
					strl=new String[]{};
					}
					break;
				}
				case ID:
				case STRING:
				{
					{
					str=sfstringValue();
					strs[0]=str;i++;
					{
					_loop162:
					do {
						if ((LA(1)==COLON) && (LA(2)==ID||LA(2)==STRING)) {
							match(COLON);
							str=sfstringValue();
							
										  		if (i==strs.length)	strs=VRMLHelper.reallocate(strs);
										  			strs[i]=str;i++;
									  		
						}
						else {
							break _loop162;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case COLON:
					{
						match(COLON);
						break;
					}
					case CLOSE_BRACKET:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(CLOSE_BRACKET);
					
								  	strl= new String[i];
								 	System.arraycopy(strs, 0, strl, 0, i);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
				break;
			}
			case ID:
			case STRING:
			{
				{
				str=sfstringValue();
				strl = new String[]{str};
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
			recover(ex,_tokenSet_6);
		}
		return strl;
	}
	
	private final double  sffloatValue() throws RecognitionException, TokenStreamException {
		double d=0;
		
		
		try {      // for error handling
			d=doubleThing();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_7);
		}
		return d;
	}
	
	private final String  sfenumValue() throws RecognitionException, TokenStreamException {
		String s="";
		
		
		try {      // for error handling
			s=id();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_8);
		}
		return s;
	}
	
	private final double[]  mffloatValue() throws RecognitionException, TokenStreamException {
		double[] dl=null;
		
		double d; 
		ds= new double[INITIAL_SIZE];
		int i=0;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case OPEN_BRACKET:
			{
				{
				match(OPEN_BRACKET);
				{
				switch ( LA(1)) {
				case CLOSE_BRACKET:
				{
					{
					match(CLOSE_BRACKET);
					dl=new double[]{};
					}
					break;
				}
				case NUMBER:
				case PLUS:
				case MINUS:
				{
					{
					d=sffloatValue();
					ds[0]=d;i++;
					{
					_loop135:
					do {
						if ((LA(1)==COLON) && (LA(2)==NUMBER||LA(2)==PLUS||LA(2)==MINUS)) {
							match(COLON);
							d=sffloatValue();
								if (i==ds.length)	ds=VRMLHelper.reallocate(ds);
									  								ds[i]=d;i++;
						}
						else {
							break _loop135;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case COLON:
					{
						match(COLON);
						break;
					}
					case CLOSE_BRACKET:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(CLOSE_BRACKET);
					
								  	dl=new double[i];
								  	System.arraycopy(ds, 0, dl, 0, i);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
				break;
			}
			case NUMBER:
			case PLUS:
			case MINUS:
			{
				{
				d=sffloatValue();
				dl = new double[]{d};
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
			recover(ex,_tokenSet_9);
		}
		return dl;
	}
	
	private final void wrongAttribute() throws RecognitionException, TokenStreamException {
		
		Token  g = null;
		
		try {      // for error handling
			g = LT(1);
			match(ID);
			{
			_loop196:
			do {
				if ((_tokenSet_4.member(LA(1))) && ((LA(2) >= HEADER && LA(2) <= WS_))) {
					dumb();
				}
				else {
					break _loop196;
				}
				
			} while (true);
			}
			System.out.println("unknown Attribute:"+g.getText()+" -following Attributes ignored!");
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_10);
		}
	}
	
	private final boolean[]  sfbitmaskValue(
		String [] code
	) throws RecognitionException, TokenStreamException {
		boolean[] mask;
		
		mask= new boolean[code.length];
		for(int i=0;i<code.length;i++)	mask[i]=false;
		String b="";
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				b=id();
				VRMLHelper.checkFlag(code,mask,b);
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				b=id();
				VRMLHelper.checkFlag(code,mask,b);
				{
				_loop111:
				do {
					if ((LA(1)==T1) && (LA(2)==ID)) {
						match(T1);
						b=id();
						VRMLHelper.checkFlag(code,mask,b);
					}
					else {
						break _loop111;
					}
					
				} while (true);
				}
				{
				switch ( LA(1)) {
				case T1:
				{
					match(T1);
					break;
				}
				case RPAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(RPAREN);
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
			recover(ex,_tokenSet_11);
		}
		return mask;
	}
	
	private final int[]  mflongValue() throws RecognitionException, TokenStreamException {
		int[] ll=null;
		
		int l; 
		ls= new int[INITIAL_SIZE];
		int i=0;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case OPEN_BRACKET:
			{
				{
				match(OPEN_BRACKET);
				{
				switch ( LA(1)) {
				case CLOSE_BRACKET:
				{
					{
					match(CLOSE_BRACKET);
					ll=new int[]{};
					}
					break;
				}
				case NUMBER:
				case PLUS:
				case MINUS:
				{
					{
					l=sflongValue();
					ls[0]=l;i++;
					{
					_loop150:
					do {
						if ((LA(1)==COLON) && (LA(2)==NUMBER||LA(2)==PLUS||LA(2)==MINUS)) {
							match(COLON);
							l=sflongValue();
								if (i==ls.length)	ls=VRMLHelper.reallocate(ls);
									  								ls[i]=l;i++;
						}
						else {
							break _loop150;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case COLON:
					{
						match(COLON);
						break;
					}
					case CLOSE_BRACKET:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(CLOSE_BRACKET);
					
								  	ll= new int[i];
								  	System.arraycopy(ls, 0, ll, 0, i);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
				break;
			}
			case NUMBER:
			case PLUS:
			case MINUS:
			{
				{
				l=sflongValue();
				ll = new int[]{l};
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
			recover(ex,_tokenSet_12);
		}
		return ll;
	}
	
	private final int  sflongValue() throws RecognitionException, TokenStreamException {
		int i;
		
		i = 0;
		
		try {      // for error handling
			i=intThing();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_13);
		}
		return i;
	}
	
	private final void coordinate3Node(
		State state
	) throws RecognitionException, TokenStreamException {
		
		
		if (VRMLHelper.verbose) System.err.println("Coordinate3");
		double[][] d={};
		
		
		try {      // for error handling
			match(38);
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case LITERAL_point:
			{
				match(LITERAL_point);
				d=mfvec3fValue();
				break;
			}
			case ID:
			{
				wrongAttribute();
				break;
			}
			case CLOSE_BRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(CLOSE_BRACE);
			state.coords=d;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void infoNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.println("Info:{ "); 
		String s = null;
		
		try {      // for error handling
			match(LITERAL_Info);
			match(OPEN_BRACE);
			{
			_loop55:
			do {
				switch ( LA(1)) {
				case LITERAL_string:
				{
					match(LITERAL_string);
					s=sfstringValue();
					if (VRMLHelper.verbose) System.err.println(state.history+"    "+s);
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop55;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			if (VRMLHelper.verbose) System.err.println(state.history+"}");
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void materialNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		
		if (VRMLHelper.verbose) System.err.print("Material( ");
		Color[] c=null;
		double[] d=null;
		Color[] amb=new Color[]{};
		Color[] dif=new Color[]{};
		Color[] spe=new Color[]{};
		Color[] emi=new Color[]{};
		double [] shi= new double[]{};
		double [] tra= new double[]{};
		
		
		try {      // for error handling
			match(LITERAL_Material);
			match(OPEN_BRACE);
			{
			_loop58:
			do {
				switch ( LA(1)) {
				case LITERAL_ambientColor:
				{
					match(LITERAL_ambientColor);
					if (VRMLHelper.verbose) System.err.print("ambientColor ");
					c=mfcolorValue();
					amb=c;
					break;
				}
				case LITERAL_diffuseColor:
				{
					match(LITERAL_diffuseColor);
					if (VRMLHelper.verbose) System.err.print("diffuseColor ");
					c=mfcolorValue();
					dif=c;
					break;
				}
				case LITERAL_specularColor:
				{
					match(LITERAL_specularColor);
					if (VRMLHelper.verbose) System.err.print("specularColor ");
					c=mfcolorValue();
					spe=c;
					break;
				}
				case LITERAL_emissiveColor:
				{
					match(LITERAL_emissiveColor);
					if (VRMLHelper.verbose) System.err.print("emissiveColor ");
					c=mfcolorValue();
					emi=c;
					break;
				}
				case LITERAL_shininess:
				{
					match(LITERAL_shininess);
					if (VRMLHelper.verbose) System.err.print("shininess ");
					d=mffloatValue();
					shi=d;
					break;
				}
				case LITERAL_transparency:
				{
					match(LITERAL_transparency);
					if (VRMLHelper.verbose) System.err.print("transparency ");
					d=mffloatValue();
					tra=d;
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop58;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
					  state.ambient=amb;
					  state.diffuse=dif;
					  state.specular=spe;
					  state.emissive=emi; 
					  state.shininess=shi;
					  state.transparency=tra;
				      if (VRMLHelper.verbose) System.err.println(")");
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void materialBindingNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.println("Material Binding");
		String mb="OVERALL";
		
		try {      // for error handling
			match(LITERAL_MaterialBinding);
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case LITERAL_value:
			{
				match(LITERAL_value);
				mb=sfenumValue();
				break;
			}
			case ID:
			{
				wrongAttribute();
				break;
			}
			case CLOSE_BRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(CLOSE_BRACE);
			state.materialBinding = State.getBinding(mb);	
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void normalNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose)	System.err.println("Normals"); 
		double[][] normals={};
		
		
		try {      // for error handling
			match(LITERAL_Normal);
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case LITERAL_vector:
			{
				match(LITERAL_vector);
				normals=mfvec3fValue();
				break;
			}
			case ID:
			{
				wrongAttribute();
				break;
			}
			case CLOSE_BRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(CLOSE_BRACE);
			state.normals=normals;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void normalBindingNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.print("normalBinding( ");
		String nb ="DEFAULT";
		
		try {      // for error handling
			match(LITERAL_NormalBinding);
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case LITERAL_value:
			{
				match(LITERAL_value);
				if (VRMLHelper.verbose) System.err.print("value ");
				nb=sfenumValue();
				break;
			}
			case ID:
			{
				wrongAttribute();
				break;
			}
			case CLOSE_BRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(CLOSE_BRACE);
			state.normalBinding = State.getBinding(nb);
				 	 if (VRMLHelper.verbose) System.err.println(")");
				 	
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void texture2Node(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.print("texture2( ");
			String file="";
			int[][][] image = new int[][][]{{{}}};
			String[] code = new String[]{"REPEAT","CLAMP"};
			String wrapS="REPEAT";
			String wrapT="REPEAT";
		
		
		try {      // for error handling
			match(53);
			match(OPEN_BRACE);
			{
			_loop67:
			do {
				switch ( LA(1)) {
				case LITERAL_filename:
				{
					match(LITERAL_filename);
					if (VRMLHelper.verbose) System.err.print("filename ");
					file=sfstringValue();
					break;
				}
				case LITERAL_image:
				{
					match(LITERAL_image);
					if (VRMLHelper.verbose) System.err.print("image ");
					image=sfimageValue();
					break;
				}
				case LITERAL_wrapS:
				{
					match(LITERAL_wrapS);
					if (VRMLHelper.verbose) System.err.print("wrapS ");
					wrapS=sfenumValue();
					break;
				}
				case LITERAL_wrapT:
				{
					match(LITERAL_wrapT);
					if (VRMLHelper.verbose) System.err.print("wrapT ");
					wrapT=sfenumValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop67;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
				 state.textureFile=file;
				 state.textureData=image;
				 state.wrapS=(VRMLHelper.getEnum(code,wrapS)!=0);
				 state.wrapT=(VRMLHelper.getEnum(code,wrapT)!=0);
				 if (VRMLHelper.verbose) System.err.println(")");
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void texture2TransformNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.print("texture2Transform( ");
			double[] trans= new double[]{0,0};
			double 	 rot =	0;
			double[] scale= new double[]{1,1};
			double[] center=new double[]{0,0};
		
		
		try {      // for error handling
			match(58);
			match(OPEN_BRACE);
			{
			_loop70:
			do {
				switch ( LA(1)) {
				case LITERAL_translation:
				{
					match(LITERAL_translation);
					if (VRMLHelper.verbose) System.err.print("translation ");
					trans=sfvec2fValue();
					break;
				}
				case LITERAL_rotation:
				{
					match(LITERAL_rotation);
					if (VRMLHelper.verbose) System.err.print("rotation ");
					rot=sffloatValue();
					break;
				}
				case LITERAL_ScaleFactor:
				{
					match(LITERAL_ScaleFactor);
					if (VRMLHelper.verbose) System.err.print("ScaleFactor ");
					scale=sfvec2fValue();
					break;
				}
				case LITERAL_Center:
				{
					match(LITERAL_Center);
					if (VRMLHelper.verbose) System.err.print("Center ");
					center=sfvec2fValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop70;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
				 MatrixBuilder.euclidean()
					.translate(trans[0],trans[1],0)
					.translate(center[0],center[1],0)
					.rotate(rot,0,0,1)
					.scale(scale[0],scale[1],1)
					.translate(-center[0],-center[1],0)
					.assignTo(state.textureTrafo);
				 if (VRMLHelper.verbose) System.err.println(")");
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void textureCoordinate2Node(
		State state
	) throws RecognitionException, TokenStreamException {
		
		
		if (VRMLHelper.verbose) System.err.print("TextureCoordinate2( ");
		double [][] point= new double[][]{{0,0}};
		
		
		try {      // for error handling
			match(63);
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case LITERAL_point:
			{
				match(LITERAL_point);
				if (VRMLHelper.verbose) System.err.print("point ");
				point=mfvec2fValue();
				break;
			}
			case ID:
			{
				wrongAttribute();
				break;
			}
			case CLOSE_BRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(CLOSE_BRACE);
			state.textureCoords=point;
					  if (VRMLHelper.verbose) System.err.println(")"); 
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final void shapeHintsNode(
		State state
	) throws RecognitionException, TokenStreamException {
		
		if (VRMLHelper.verbose) System.err.println("ShapeHints");
		int vertOrd=0;
		int shape=0;
		int face=0;
		double crease=0.5;
		String s;
		
		
		try {      // for error handling
			match(LITERAL_ShapeHints);
			match(OPEN_BRACE);
			{
			_loop75:
			do {
				switch ( LA(1)) {
				case LITERAL_vertexOrdering:
				{
					match(LITERAL_vertexOrdering);
					s=sfenumValue();
					state.vertOrder=VRMLHelper.getEnum(State.VERTORDER,s);
					break;
				}
				case LITERAL_shapeType:
				{
					match(LITERAL_shapeType);
					s=sfenumValue();
					state.shapeType=VRMLHelper.getEnum(State.SHAPETYPE,s);
					break;
				}
				case LITERAL_faceType:
				{
					match(LITERAL_faceType);
					s=sfenumValue();
					state.faceType=VRMLHelper.getEnum(State.FACETYPE,s);
					break;
				}
				case LITERAL_creaseAngle:
				{
					match(LITERAL_creaseAngle);
					crease=sffloatValue();
					state.creaseAngle=crease;
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop75;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			// TODO3: handle Hints;
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final double[][]  mfvec3fValue() throws RecognitionException, TokenStreamException {
		double[][] vecl=null;
		
		double[] vec; 
		vecs= new double[INITIAL_SIZE][];
		int i=0;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case OPEN_BRACKET:
			{
				{
				match(OPEN_BRACKET);
				{
				switch ( LA(1)) {
				case CLOSE_BRACKET:
				{
					{
					match(CLOSE_BRACKET);
					vecl=new double[][]{};
					}
					break;
				}
				case NUMBER:
				case PLUS:
				case MINUS:
				{
					{
					vec=sfvec3fValue();
					vecs[0]=vec;i++;
					{
					_loop182:
					do {
						if ((LA(1)==COLON) && (LA(2)==NUMBER||LA(2)==PLUS||LA(2)==MINUS)) {
							match(COLON);
							vec=sfvec3fValue();
							if (i==vecs.length)	vecs=VRMLHelper.reallocate(vecs);
									  								 vecs[i]=vec;i++;
						}
						else {
							break _loop182;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case COLON:
					{
						match(COLON);
						break;
					}
					case CLOSE_BRACKET:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(CLOSE_BRACKET);
					
								  		vecl= new double[i][];
								   		System.arraycopy(vecs, 0, vecl, 0, i);
								  		
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
				break;
			}
			case NUMBER:
			case PLUS:
			case MINUS:
			{
				{
				vec=sfvec3fValue();
				vecl = new double[][]{vec};
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
			recover(ex,_tokenSet_14);
		}
		return vecl;
	}
	
	private final Color[]  mfcolorValue() throws RecognitionException, TokenStreamException {
		Color[] cl=null;
		
		Color c = null; 
		cs= new Color[INITIAL_SIZE];
		int i=0;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case OPEN_BRACKET:
			{
				{
				match(OPEN_BRACKET);
				{
				switch ( LA(1)) {
				case CLOSE_BRACKET:
				{
					{
					match(CLOSE_BRACKET);
					cl=new Color[]{};
					}
					break;
				}
				case NUMBER:
				case PLUS:
				case MINUS:
				{
					{
					c=sfcolorValue();
					cs[0]=c;i++;
					{
					_loop124:
					do {
						if ((LA(1)==COLON) && (LA(2)==NUMBER||LA(2)==PLUS||LA(2)==MINUS)) {
							match(COLON);
							c=sfcolorValue();
								if (i==cs.length)	cs=VRMLHelper.reallocate(cs);
									  								cs[i]=c;i++;
						}
						else {
							break _loop124;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case COLON:
					{
						match(COLON);
						break;
					}
					case CLOSE_BRACKET:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(CLOSE_BRACKET);
					
								  	cl=new Color[i];
								  	System.arraycopy(cs, 0, cl, 0, i);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
				break;
			}
			case NUMBER:
			case PLUS:
			case MINUS:
			{
				{
				c=sfcolorValue();
				cl = new Color[]{c};
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
			recover(ex,_tokenSet_15);
		}
		return cl;
	}
	
	private final int[][][]  sfimageValue() throws RecognitionException, TokenStreamException {
		int[][][] colors = new int[][][]{{{}}} ;
		
		int width=0;
		int height=0;
		int colorDim=0;
		int[][] colL=null;
		int size=0;
		
		try {      // for error handling
			width=intThing();
			height=intThing();
			colorDim=intThing();
			size=width*height;
			colL=hexList(size,colorDim);
			
					 colors=new int[width][height][colorDim];
					 for (int i=0;i<width;i++)
					 	for (int j=0;j<height;j++)
					 		for (int k=0;k<colorDim;k++)
					 			colors[i][j][k]=colL[i*width+j][k];
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_16);
		}
		return colors;
	}
	
	private final double[]  sfvec2fValue() throws RecognitionException, TokenStreamException {
		double[] vec=null;
		
		double fx,fy;
		
		try {      // for error handling
			fx=doubleThing();
			fy=doubleThing();
			vec= new double[]{fx,fy};
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_17);
		}
		return vec;
	}
	
	private final double[][]  mfvec2fValue() throws RecognitionException, TokenStreamException {
		double[][] vecl=null;
		
		double[] vec; 
		vecs= new double[INITIAL_SIZE][];
		int i=0;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case OPEN_BRACKET:
			{
				{
				match(OPEN_BRACKET);
				{
				switch ( LA(1)) {
				case CLOSE_BRACKET:
				{
					{
					match(CLOSE_BRACKET);
					vecl=new double[][]{};
					}
					break;
				}
				case NUMBER:
				case PLUS:
				case MINUS:
				{
					{
					vec=sfvec2fValue();
					vecs[0]=vec;i++;
					{
					_loop173:
					do {
						if ((LA(1)==COLON) && (LA(2)==NUMBER||LA(2)==PLUS||LA(2)==MINUS)) {
							match(COLON);
							vec=sfvec2fValue();
							if (i==vecs.length)	vecs=VRMLHelper.reallocate(vecs);
									  								 vecs[i]=vec;i++;
						}
						else {
							break _loop173;
						}
						
					} while (true);
					}
					{
					switch ( LA(1)) {
					case COLON:
					{
						match(COLON);
						break;
					}
					case CLOSE_BRACKET:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(CLOSE_BRACKET);
					
								  		vecl= new double[i][];
								  		System.arraycopy(vecs, 0, vecl, 0, i);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
				break;
			}
			case NUMBER:
			case PLUS:
			case MINUS:
			{
				{
				vec=sfvec2fValue();
				vecl = new double[][]{vec};
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
			recover(ex,_tokenSet_14);
		}
		return vecl;
	}
	
	private final Transformation  matrixTransformNode() throws RecognitionException, TokenStreamException {
		Transformation t= new Transformation();
		
		if (VRMLHelper.verbose) System.err.println("Matrix Transform");
		double[] mat = null;
		
		try {      // for error handling
			match(LITERAL_MatrixTransform);
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case LITERAL_matrix:
			{
				match(LITERAL_matrix);
				mat=sfmatrixValue();
				break;
			}
			case ID:
			{
				wrongAttribute();
				break;
			}
			case CLOSE_BRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(CLOSE_BRACE);
			t= new Transformation(mat);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return t;
	}
	
	private final Transformation  rotationNode() throws RecognitionException, TokenStreamException {
		Transformation m =new Transformation() ;
		
		if (VRMLHelper.verbose) System.err.println("Rotation");
		double[] d=new double[]{0,0,1,0};
		
		
		try {      // for error handling
			match(LITERAL_Rotation);
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case LITERAL_rotation:
			{
				match(LITERAL_rotation);
				d=sfrotationValue();
				break;
			}
			case ID:
			{
				wrongAttribute();
				break;
			}
			case CLOSE_BRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(CLOSE_BRACE);
			
					double[] axis=new double[3];
					System.arraycopy(d,0,axis,0,3);
					MatrixBuilder.euclidean().rotate(d[3],axis).assignTo(m);
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return m;
	}
	
	private final Transformation  scaleNode() throws RecognitionException, TokenStreamException {
		Transformation m =new Transformation() ;
		
		if (VRMLHelper.verbose) System.err.println("Scale");
		double[] d = new double[]{1,1,1};
		
		
		try {      // for error handling
			match(LITERAL_Scale);
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case LITERAL_scaleFactor:
			{
				match(LITERAL_scaleFactor);
				d=sfvec3fValue();
				break;
			}
			case ID:
			{
				wrongAttribute();
				break;
			}
			case CLOSE_BRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(CLOSE_BRACE);
			MatrixBuilder.euclidean().scale(d).assignTo(m); 
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return m;
	}
	
	private final Transformation  transformNode() throws RecognitionException, TokenStreamException {
		Transformation m =new Transformation() ;
		
			
		if (VRMLHelper.verbose) System.err.println("transform Node");
		double[] trans=new 	double[]{0,0,0};
		double[] rot=new 		double[]{0,0,1,0};
		double[] scaleF=new 	double[]{1,1,1};
		double[] scaleO=new 	double[]{0,0,1,0};
		double[] center=new 	double[]{0,0,0};
		
		
		try {      // for error handling
			match(LITERAL_Transform);
			match(OPEN_BRACE);
			{
			_loop86:
			do {
				switch ( LA(1)) {
				case LITERAL_translation:
				{
					match(LITERAL_translation);
					trans=sfvec3fValue();
					break;
				}
				case LITERAL_rotation:
				{
					match(LITERAL_rotation);
					rot=sfrotationValue();
					break;
				}
				case LITERAL_scaleFactor:
				{
					match(LITERAL_scaleFactor);
					scaleF=sfvec3fValue();
					break;
				}
				case LITERAL_scaleOrientation:
				{
					match(LITERAL_scaleOrientation);
					scaleO=sfrotationValue();
					break;
				}
				case LITERAL_center:
				{
					match(LITERAL_center);
					center=sfvec3fValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop86;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
				 MatrixBuilder.euclidean()
				  .translate(trans)
				  .translate(center)
				  .rotate(rot[3],rot[0],rot[1],rot[2])
				  .rotate(scaleO[3],scaleO[0],scaleO[1],scaleO[2])
				  .scale(scaleF)
				  .rotate(-scaleO[3],scaleO[0],scaleO[1],scaleO[2])
				  .translate(-center[0],-center[1],-center[2])
				  .assignTo(m);
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return m;
	}
	
	private final Transformation  translationNode() throws RecognitionException, TokenStreamException {
		Transformation m =new Transformation() ;
		
		if (VRMLHelper.verbose) System.err.println("Translation");
		double[] d = new double[]{0,0,0};
		
		
		try {      // for error handling
			match(LITERAL_Translation);
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case LITERAL_translation:
			{
				match(LITERAL_translation);
				d=sfvec3fValue();
				break;
			}
			case ID:
			{
				wrongAttribute();
				break;
			}
			case CLOSE_BRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(CLOSE_BRACE);
			MatrixBuilder.euclidean().translate(d).assignTo(m);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return m;
	}
	
	private final double[]  sfmatrixValue() throws RecognitionException, TokenStreamException {
		double[] m;
		
		m=new double[16];
		double d;
		int i=0;
		
		
		try {      // for error handling
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
			d=doubleThing();
			m[i]=d;i++;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_14);
		}
		return m;
	}
	
	private final double[]  sfrotationValue() throws RecognitionException, TokenStreamException {
		double[] rv=null;
		
		double x,y,z,ang;
		
		try {      // for error handling
			x=doubleThing();
			y=doubleThing();
			z=doubleThing();
			ang=doubleThing();
			rv = new double[]{x,y,z,ang};
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_18);
		}
		return rv;
	}
	
	private final double[]  sfvec3fValue() throws RecognitionException, TokenStreamException {
		double[] vec=null;
		
		double fx,fy,fz;
		
		try {      // for error handling
			fx=doubleThing();
			fy=doubleThing();
			fz=doubleThing();
			vec= new double[]{fx,fy,fz};
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_19);
		}
		return vec;
	}
	
	private final Camera  orthographicCameraNode(
		State state
	) throws RecognitionException, TokenStreamException {
		Camera cam=null;
		
		
		if (VRMLHelper.verbose) System.err.print("orthographic Cam( ");
			double[] pos = new double[]{0,0,1};
			double[] orient = new double[]{0,0,1,0};
			double fDist = 5;
			double height = 2; // defaults
		
		
		try {      // for error handling
			match(LITERAL_OrthographicCamera);
			match(OPEN_BRACE);
			{
			_loop98:
			do {
				switch ( LA(1)) {
				case LITERAL_position:
				{
					match(LITERAL_position);
					if (VRMLHelper.verbose) System.err.print("position ");
					pos=sfvec3fValue();
					break;
				}
				case LITERAL_orientation:
				{
					match(LITERAL_orientation);
					if (VRMLHelper.verbose) System.err.print("Orientation ");
					orient=sfrotationValue();
					break;
				}
				case LITERAL_focalDistance:
				{
					match(LITERAL_focalDistance);
					if (VRMLHelper.verbose) System.err.print("focalDistance ");
					fDist=sffloatValue();
					break;
				}
				case LITERAL_height:
				{
					match(LITERAL_height);
					if (VRMLHelper.verbose) System.err.print("height ");
					height=sffloatValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop98;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
					cam= new Camera();
					cam.setPerspective(false);
					cam.setFieldOfView(45);
					cam.setFocus((height*6)/5);
					cam.setName("orthographic Cam");
					cam.setFocalLength(fDist);
					state.extraGeoTrans = new Transformation();
					MatrixBuilder.euclidean()
						.rotate(orient[3],orient[0],orient[1],orient[2])
						.translate(pos)
						.assignTo(state.extraGeoTrans);
					if (VRMLHelper.verbose) System.err.println(")");	
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return cam;
	}
	
	private final Camera  perspectiveCameraNode(
		State state
	) throws RecognitionException, TokenStreamException {
		Camera cam=null;
		
			
		if (VRMLHelper.verbose) System.err.print("perspective Cam( ");
			double[] pos = new double[]{0,0,1};
			double[] orient = new double[]{0,0,1,0};
			double fDist = 5;
			double heightA = 0.785398; // defaults
		
		
		try {      // for error handling
			match(LITERAL_PerspectiveCamera);
			match(OPEN_BRACE);
			{
			_loop95:
			do {
				switch ( LA(1)) {
				case LITERAL_position:
				{
					match(LITERAL_position);
					if (VRMLHelper.verbose) System.err.print("position ");
					pos=sfvec3fValue();
					break;
				}
				case LITERAL_orientation:
				{
					match(LITERAL_orientation);
					if (VRMLHelper.verbose) System.err.print("Orientation ");
					orient=sfrotationValue();
					break;
				}
				case LITERAL_focalDistance:
				{
					match(LITERAL_focalDistance);
					if (VRMLHelper.verbose) System.err.print("focalDistance ");
					fDist=sffloatValue();
					break;
				}
				case LITERAL_heightAngle:
				{
					match(LITERAL_heightAngle);
					if (VRMLHelper.verbose) System.err.print("heightAngle ");
					heightA=sffloatValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop95;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
					double[] rotAx=new double[3];
				  	System.arraycopy(orient, 0, rotAx, 0, 3);	
					cam= new Camera();
					cam.setPerspective(true);
					double hAngle=heightA*180/Math.PI;
					cam.setFieldOfView(hAngle);// default=45
					cam.setFocus(3);
					cam.setName("perspective Cam");
					cam.setFocalLength(fDist);
					state.extraGeoTrans = new Transformation();
					MatrixBuilder.euclidean()
						.rotate(orient[3],rotAx)
						.translate(pos)
						.assignTo(state.extraGeoTrans);
					if (VRMLHelper.verbose) System.err.println(")");
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return cam;
	}
	
	private final DirectionalLight  directionalLightNode(
		State state
	) throws RecognitionException, TokenStreamException {
		DirectionalLight l=null;
		
		if (VRMLHelper.verbose) System.err.print("Dir Light( ");
		boolean on = true;
		double intens = 1;
		Color c = new Color(1f,1f,1f);
		double[] dir = new double[]{0,0,-1};
		
		
		try {      // for error handling
			match(LITERAL_DirectionalLight);
			match(OPEN_BRACE);
			{
			_loop107:
			do {
				switch ( LA(1)) {
				case LITERAL_on:
				{
					match(LITERAL_on);
					if (VRMLHelper.verbose) System.err.print("on ");
					on=sfboolValue();
					break;
				}
				case LITERAL_intensity:
				{
					match(LITERAL_intensity);
					if (VRMLHelper.verbose) System.err.print("intensity ");
					intens=sffloatValue();
					break;
				}
				case LITERAL_color:
				{
					match(LITERAL_color);
					if (VRMLHelper.verbose) System.err.print("color ");
					c=sfcolorValue();
					break;
				}
				case LITERAL_direction:
				{
					match(LITERAL_direction);
					if (VRMLHelper.verbose) System.err.print("direction ");
					dir=sfvec3fValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop107;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
				    l = new DirectionalLight();
					if (on)	l.setIntensity(intens);
					else 	l.setIntensity(0);
					l.setColor(c);
					l.setGlobal(false);
					l.setName("Directional Light");
					state.extraGeoTrans = new Transformation();
					MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,-1},dir)
						.assignTo(state.extraGeoTrans);
					if (VRMLHelper.verbose) System.err.println(")");
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return l;
	}
	
	private final PointLight  pointLightNode(
		State state
	) throws RecognitionException, TokenStreamException {
		PointLight l=null;
		
		if (VRMLHelper.verbose) System.err.print("Point Light( ");
		boolean on = true;
		double intens = 1;
		Color c = new Color(1f,1f,1f);
		double[] loc = new double[]{0,0,1};
		
		
		try {      // for error handling
			match(LITERAL_PointLight);
			match(OPEN_BRACE);
			{
			_loop101:
			do {
				switch ( LA(1)) {
				case LITERAL_on:
				{
					match(LITERAL_on);
					if (VRMLHelper.verbose) System.err.print("on ");
					on=sfboolValue();
					break;
				}
				case LITERAL_intensity:
				{
					match(LITERAL_intensity);
					if (VRMLHelper.verbose) System.err.print("intensity ");
					intens=sffloatValue();
					break;
				}
				case LITERAL_color:
				{
					match(LITERAL_color);
					if (VRMLHelper.verbose) System.err.print("color ");
					c=sfcolorValue();
					break;
				}
				case LITERAL_location:
				{
					match(LITERAL_location);
					if (VRMLHelper.verbose) System.err.print("location ");
					loc=sfvec3fValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop101;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			l = new PointLight();
					if (on)	l.setIntensity(intens);
					else 	l.setIntensity(0);
					l.setColor(c);
					l.setGlobal(false);
					l.setName("Point Light");
					state.extraGeoTrans = new Transformation();
					MatrixBuilder.euclidean().translate(loc)
						.assignTo(state.extraGeoTrans);
					if (VRMLHelper.verbose) System.err.println(")");
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return l;
	}
	
	private final SpotLight  spotLightNode(
		State state
	) throws RecognitionException, TokenStreamException {
		SpotLight l=null;
		
		if (VRMLHelper.verbose) System.err.print("Spot Light( ");
		boolean on = true;
		double intens = 1;
		Color c = new Color(1f,1f,1f);
		double[] loc = new double[]{0,0,1};
		double[] dir = new double[]{0,0,-1};
		double dropR=0;
		double cutA=0.785398;
		
		
		try {      // for error handling
			match(LITERAL_SpotLight);
			match(OPEN_BRACE);
			{
			_loop104:
			do {
				switch ( LA(1)) {
				case LITERAL_on:
				{
					match(LITERAL_on);
					if (VRMLHelper.verbose) System.err.print("on ");
					on=sfboolValue();
					break;
				}
				case LITERAL_intensity:
				{
					match(LITERAL_intensity);
					if (VRMLHelper.verbose) System.err.print("intensity ");
					intens=sffloatValue();
					break;
				}
				case LITERAL_color:
				{
					match(LITERAL_color);
					if (VRMLHelper.verbose) System.err.print("color ");
					c=sfcolorValue();
					break;
				}
				case LITERAL_location:
				{
					match(LITERAL_location);
					if (VRMLHelper.verbose) System.err.print("location ");
					loc=sfvec3fValue();
					break;
				}
				case LITERAL_direction:
				{
					match(LITERAL_direction);
					if (VRMLHelper.verbose) System.err.print("direction ");
					dir=sfvec3fValue();
					break;
				}
				case LITERAL_dropOffRate:
				{
					match(LITERAL_dropOffRate);
					if (VRMLHelper.verbose) System.err.print("dropOffRate ");
					dropR=sffloatValue();
					break;
				}
				case LITERAL_cutOffAngle:
				{
					match(LITERAL_cutOffAngle);
					if (VRMLHelper.verbose) System.err.print("cutOffAngle ");
					cutA=sffloatValue();
					break;
				}
				case ID:
				{
					wrongAttribute();
					break;
				}
				default:
				{
					break _loop104;
				}
				}
			} while (true);
			}
			match(CLOSE_BRACE);
			
				    l = new SpotLight();
					if (on)	l.setIntensity(intens);
					else 	l.setIntensity(0);
					l.setColor(c);
					l.setGlobal(false);
					l.setName("Spot Light");
					state.extraGeoTrans = new Transformation();
					MatrixBuilder.euclidean()
						.translate(loc)
						.rotateFromTo(new double[]{0,0,1},dir)
						.assignTo(state.extraGeoTrans);
					l.setConeAngle(cutA);
					l.setDistribution(dropR);
					l.setFalloffA0(0);
					l.setFalloffA1(0);
					l.setFalloffA2(0);
					if (VRMLHelper.verbose) System.err.println(")");
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return l;
	}
	
	private final boolean  sfboolValue() throws RecognitionException, TokenStreamException {
		boolean b;
		
		b = false;
		int n=0;
		
		try {      // for error handling
			switch ( LA(1)) {
			case NUMBER:
			case PLUS:
			case MINUS:
			{
				{
				n=intThing();
				
							if (n==0) b=false;
							else b=true; // TODO3: hier Fehler ausgeben bei n!=1
					
				}
				break;
			}
			case LITERAL_TRUE:
			{
				{
				match(LITERAL_TRUE);
				b = true;
				}
				break;
			}
			case LITERAL_FALSE:
			{
				{
				match(LITERAL_FALSE);
				b = false;
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
			recover(ex,_tokenSet_20);
		}
		return b;
	}
	
	private final Color  sfcolorValue() throws RecognitionException, TokenStreamException {
		Color c;
		
		c = null;
		double r, g, b;
		int ro, ge, bl;
		
		
		try {      // for error handling
			r=doubleThing();
			g=doubleThing();
			b=doubleThing();
			ro= (int)Math.round(r*255); ge=(int)Math.round(g*255); bl=(int)Math.round(b*255);
					 c = new Color(ro,ge,bl);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_21);
		}
		return c;
	}
	
	private final int  intThing() throws RecognitionException, TokenStreamException {
		int i;
		
		Token  s = null;
		i=0; String sig="";
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case PLUS:
			{
				match(PLUS);
				break;
			}
			case MINUS:
			{
				match(MINUS);
				sig="-";
				break;
			}
			case NUMBER:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			s = LT(1);
			match(NUMBER);
			double d=Double.parseDouble(sig + s.getText());
				    i=(int)Math.round(d);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_22);
		}
		return i;
	}
	
	private final double  doubleThing() throws RecognitionException, TokenStreamException {
		double d=0;
		
		Token  s = null;
		double e=0; String sig="";
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case PLUS:
			{
				match(PLUS);
				break;
			}
			case MINUS:
			{
				match(MINUS);
				sig="-";
				break;
			}
			case NUMBER:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			s = LT(1);
			match(NUMBER);
			d=Double.parseDouble(sig + s.getText());
			{
			switch ( LA(1)) {
			case LITERAL_E:
			case LITERAL_e:
			{
				e=expThing();
				d=d*Math.pow(10,e);
				break;
			}
			case CLOSE_BRACE:
			case LITERAL_string:
			case LITERAL_spacing:
			case LITERAL_justification:
			case LITERAL_width:
			case LITERAL_parts:
			case LITERAL_bottomRadius:
			case LITERAL_height:
			case LITERAL_depth:
			case LITERAL_radius:
			case LITERAL_ambientColor:
			case LITERAL_diffuseColor:
			case LITERAL_specularColor:
			case LITERAL_emissiveColor:
			case LITERAL_shininess:
			case LITERAL_transparency:
			case LITERAL_translation:
			case LITERAL_rotation:
			case LITERAL_ScaleFactor:
			case LITERAL_Center:
			case LITERAL_vertexOrdering:
			case LITERAL_shapeType:
			case LITERAL_faceType:
			case LITERAL_creaseAngle:
			case LITERAL_scaleFactor:
			case LITERAL_scaleOrientation:
			case LITERAL_center:
			case LITERAL_position:
			case LITERAL_orientation:
			case LITERAL_focalDistance:
			case LITERAL_heightAngle:
			case LITERAL_on:
			case LITERAL_intensity:
			case LITERAL_color:
			case LITERAL_location:
			case LITERAL_direction:
			case LITERAL_dropOffRate:
			case LITERAL_cutOffAngle:
			case ID:
			case CLOSE_BRACKET:
			case COLON:
			case NUMBER:
			case PLUS:
			case MINUS:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_23);
		}
		return d;
	}
	
	private final int[][]  hexList(
		int size, int dim
	) throws RecognitionException, TokenStreamException {
		int[][] cL= new int[size][dim];
		
		Token  g = null;
		Token  k = null;
		int i=0;
		String s="";
		
		try {      // for error handling
			{
			_loop142:
			do {
				if ((LA(1)==HEXDEC||LA(1)==NUMBER)) {
					{
					switch ( LA(1)) {
					case HEXDEC:
					{
						g = LT(1);
						match(HEXDEC);
						s=g.getText();
						break;
					}
					case NUMBER:
					{
						k = LT(1);
						match(NUMBER);
						s=k.getText();
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
						if(i<size)
							 	cL[i]=VRMLHelper.decodeColorFromString(dim,g.getText());
						 	i++;
				}
				else {
					break _loop142;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_16);
		}
		return cL;
	}
	
	private final int  hexValue() throws RecognitionException, TokenStreamException {
		int hexVal;
		
		Token  g = null;
		hexVal=0;
		
		try {      // for error handling
			g = LT(1);
			match(HEXDEC);
			hexVal= Integer.parseInt(g.getText());
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
		return hexVal;
	}
	
	private final double  expThing() throws RecognitionException, TokenStreamException {
		double e;
		
		Token  s = null;
		e=0; String sig="";
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_E:
			{
				match(LITERAL_E);
				break;
			}
			case LITERAL_e:
			{
				match(LITERAL_e);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case PLUS:
			{
				match(PLUS);
				break;
			}
			case MINUS:
			{
				match(MINUS);
				sig="-";
				break;
			}
			case NUMBER:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			s = LT(1);
			match(NUMBER);
			e=Double.parseDouble(sig + s.getText() );
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_23);
		}
		return e;
	}
	
	private final void dumb() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case HEADER:
			case LITERAL_DEF:
			case LITERAL_USE:
			case LITERAL_WWWInline:
			case LITERAL_WWWAnchor:
			case LITERAL_LOD:
			case LITERAL_Separator:
			case LITERAL_Switch:
			case LITERAL_AsciiText:
			case LITERAL_string:
			case LITERAL_spacing:
			case LITERAL_justification:
			case LITERAL_width:
			case LITERAL_Cone:
			case LITERAL_parts:
			case LITERAL_bottomRadius:
			case LITERAL_height:
			case LITERAL_Cube:
			case LITERAL_depth:
			case LITERAL_Cylinder:
			case LITERAL_radius:
			case LITERAL_IndexedFaceSet:
			case LITERAL_coordIndex:
			case LITERAL_materialIndex:
			case LITERAL_normalIndex:
			case LITERAL_textureCoordIndex:
			case LITERAL_IndexedLineSet:
			case LITERAL_PointSet:
			case LITERAL_startIndex:
			case LITERAL_numPoints:
			case LITERAL_Sphere:
			case LITERAL_FontStyle:
			case 38:
			case LITERAL_point:
			case LITERAL_Info:
			case LITERAL_Material:
			case LITERAL_ambientColor:
			case LITERAL_diffuseColor:
			case LITERAL_specularColor:
			case LITERAL_emissiveColor:
			case LITERAL_shininess:
			case LITERAL_transparency:
			case LITERAL_MaterialBinding:
			case LITERAL_value:
			case LITERAL_Normal:
			case LITERAL_vector:
			case LITERAL_NormalBinding:
			case 53:
			case LITERAL_filename:
			case LITERAL_image:
			case LITERAL_wrapS:
			case LITERAL_wrapT:
			case 58:
			case LITERAL_translation:
			case LITERAL_rotation:
			case LITERAL_ScaleFactor:
			case LITERAL_Center:
			case 63:
			case LITERAL_ShapeHints:
			case LITERAL_vertexOrdering:
			case LITERAL_shapeType:
			case LITERAL_faceType:
			case LITERAL_creaseAngle:
			case LITERAL_MatrixTransform:
			case LITERAL_matrix:
			case LITERAL_Rotation:
			case LITERAL_Scale:
			case LITERAL_scaleFactor:
			case LITERAL_Transform:
			case LITERAL_scaleOrientation:
			case LITERAL_center:
			case LITERAL_Translation:
			case LITERAL_PerspectiveCamera:
			case LITERAL_position:
			case LITERAL_orientation:
			case LITERAL_focalDistance:
			case LITERAL_heightAngle:
			case LITERAL_OrthographicCamera:
			case LITERAL_PointLight:
			case LITERAL_on:
			case LITERAL_intensity:
			case LITERAL_color:
			case LITERAL_location:
			case LITERAL_SpotLight:
			case LITERAL_direction:
			case LITERAL_dropOffRate:
			case LITERAL_cutOffAngle:
			case LITERAL_DirectionalLight:
			case ID:
			case T1:
			case LITERAL_TRUE:
			case LITERAL_FALSE:
			case COLON:
			case HEXDEC:
			case NUMBER:
			case STRING:
			case PLUS:
			case MINUS:
			case LITERAL_E:
			case LITERAL_e:
			case ID_LETTER:
			case HEXDIGIT:
			case DIGIT:
			case ESC:
			case RESTLINE:
			case HEADER1:
			case COMMENT:
			case WS_:
			{
				{
				{
				int _cnt211=0;
				_loop211:
				do {
					if ((_tokenSet_24.member(LA(1))) && ((LA(2) >= HEADER && LA(2) <= WS_))) {
						{
						match(_tokenSet_24);
						}
					}
					else {
						if ( _cnt211>=1 ) { break _loop211; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt211++;
				} while (true);
				}
				{
				if ((LA(1)==OPEN_BRACE) && (_tokenSet_25.member(LA(2)))) {
					match(OPEN_BRACE);
					{
					_loop214:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop214;
						}
						
					} while (true);
					}
					match(CLOSE_BRACE);
				}
				else if ((LA(1)==LPAREN) && (_tokenSet_26.member(LA(2)))) {
					match(LPAREN);
					{
					_loop216:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop216;
						}
						
					} while (true);
					}
					match(RPAREN);
				}
				else if ((LA(1)==OPEN_BRACKET) && (_tokenSet_27.member(LA(2)))) {
					match(OPEN_BRACKET);
					{
					_loop218:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop218;
						}
						
					} while (true);
					}
					match(CLOSE_BRACKET);
				}
				else if (((LA(1) >= HEADER && LA(1) <= WS_)) && (_tokenSet_28.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				}
				break;
			}
			case OPEN_BRACE:
			case LPAREN:
			case OPEN_BRACKET:
			{
				{
				switch ( LA(1)) {
				case OPEN_BRACE:
				{
					match(OPEN_BRACE);
					{
					_loop221:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop221;
						}
						
					} while (true);
					}
					match(CLOSE_BRACE);
					break;
				}
				case OPEN_BRACKET:
				{
					match(OPEN_BRACKET);
					{
					_loop223:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop223;
						}
						
					} while (true);
					}
					match(CLOSE_BRACKET);
					break;
				}
				case LPAREN:
				{
					match(LPAREN);
					{
					_loop225:
					do {
						if ((_tokenSet_4.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop225;
						}
						
					} while (true);
					}
					match(RPAREN);
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
			recover(ex,_tokenSet_29);
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
		"\"WWWInline\"",
		"\"WWWAnchor\"",
		"\"LOD\"",
		"\"Separator\"",
		"OPEN_BRACE",
		"CLOSE_BRACE",
		"\"Switch\"",
		"\"AsciiText\"",
		"\"string\"",
		"\"spacing\"",
		"\"justification\"",
		"\"width\"",
		"\"Cone\"",
		"\"parts\"",
		"\"bottomRadius\"",
		"\"height\"",
		"\"Cube\"",
		"\"depth\"",
		"\"Cylinder\"",
		"\"radius\"",
		"\"IndexedFaceSet\"",
		"\"coordIndex\"",
		"\"materialIndex\"",
		"\"normalIndex\"",
		"\"textureCoordIndex\"",
		"\"IndexedLineSet\"",
		"\"PointSet\"",
		"\"startIndex\"",
		"\"numPoints\"",
		"\"Sphere\"",
		"\"FontStyle\"",
		"\"Coordinate3\"",
		"\"point\"",
		"\"Info\"",
		"\"Material\"",
		"\"ambientColor\"",
		"\"diffuseColor\"",
		"\"specularColor\"",
		"\"emissiveColor\"",
		"\"shininess\"",
		"\"transparency\"",
		"\"MaterialBinding\"",
		"\"value\"",
		"\"Normal\"",
		"\"vector\"",
		"\"NormalBinding\"",
		"\"Texture2\"",
		"\"filename\"",
		"\"image\"",
		"\"wrapS\"",
		"\"wrapT\"",
		"\"Texture2Transform\"",
		"\"translation\"",
		"\"rotation\"",
		"\"ScaleFactor\"",
		"\"Center\"",
		"\"TextureCoordinate2\"",
		"\"ShapeHints\"",
		"\"vertexOrdering\"",
		"\"shapeType\"",
		"\"faceType\"",
		"\"creaseAngle\"",
		"\"MatrixTransform\"",
		"\"matrix\"",
		"\"Rotation\"",
		"\"Scale\"",
		"\"scaleFactor\"",
		"\"Transform\"",
		"\"scaleOrientation\"",
		"\"center\"",
		"\"Translation\"",
		"\"PerspectiveCamera\"",
		"\"position\"",
		"\"orientation\"",
		"\"focalDistance\"",
		"\"heightAngle\"",
		"\"OrthographicCamera\"",
		"\"PointLight\"",
		"\"on\"",
		"\"intensity\"",
		"\"color\"",
		"\"location\"",
		"\"SpotLight\"",
		"\"direction\"",
		"\"dropOffRate\"",
		"\"cutOffAngle\"",
		"\"DirectionalLight\"",
		"an identifier",
		"LPAREN",
		"T1",
		"RPAREN",
		"\"TRUE\"",
		"\"FALSE\"",
		"OPEN_BRACKET",
		"CLOSE_BRACKET",
		"COLON",
		"HEXDEC",
		"NUMBER",
		"STRING",
		"PLUS",
		"MINUS",
		"\"E\"",
		"\"e\"",
		"ID_LETTER",
		"HEXDIGIT",
		"DIGIT",
		"ESC",
		"RESTLINE",
		"HEADER1",
		"COMMENT",
		"WS_"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { -8920219694304565280L, 1645766049L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { -8920219694304561182L, 1645766049L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { -8650003716661839902L, 413962626465L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[8];
		data[0]=-4112L;
		data[1]=18014252480593919L;
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 270215977717176320L, 84825604126L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 495616L, 1073741824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 8647188361573273600L, 413892313118L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 270215977642725376L, 1073741854L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 277076930695168L, 1073741824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 8917404394781642752L, 1575459358L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 74452992L, 1073741824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 4026535936L, 1073741824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 55566143488L, 413390602240L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 4096L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 277076930203648L, 1073741824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 270215977642233856L, 1073741824L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 8646911284551356416L, 413390602240L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 1729382256914468864L, 1074240000L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 1729382256914468864L, 413892319744L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 4096L, 1574961152L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 277076930203648L, 413891821568L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 270216033208373248L, 15257298796544L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 8647188361573273600L, 14707543480862L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = new long[8];
		data[0]=-6160L;
		data[1]=18014181613633535L;
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = new long[8];
		data[0]=-16L;
		data[1]=18014252480593919L;
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = new long[8];
		data[0]=-4112L;
		data[1]=18014261070528511L;
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = new long[8];
		data[0]=-4112L;
		data[1]=18014389919547391L;
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = new long[8];
		data[0]=-14L;
		data[1]=18014398509481983L;
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = new long[8];
		data[0]=-16L;
		data[1]=18014398509481983L;
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	
	}
