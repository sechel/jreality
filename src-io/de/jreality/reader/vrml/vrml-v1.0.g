//**************************************************
// * VRML 1.0 Parser
// * TODO: 
// * 	Read over vrml1.0 spec and figure out what it says
// *		Implement the unimplemented nodes/properties
// *		Some tricky areas:
// *			Difference between Group and Separator: Separator requires that we stack the state
// *				(Leaving a separator should pop this stack)
// *				The state is kept by fields in the parser beginning with "current"
// *			Implementation of DEF and USE
// *			
// */

header {
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
}

/*****************************************************************************
 * The VRMLV1 Parser
 *****************************************************************************
 */
class VRMLV1Parser extends Parser;
options {
	k = 2;							// two token lookahead
}
{
	// current state of the parsing process
	SceneGraphComponent currentSGC = null;
	SceneGraphComponent root = null;
	// this is the state, as it is currently maintained
	SceneGraphPath currentPath = new SceneGraphPath();
	Transformation currentTransform = new Transformation();
	Appearance currentAp = null;
	DataList currentCoordinate3 = null;
	DataList currentNormal = null;
	int[][] currentCoordinateIndex = null;
	int[][] currentNormalIndex = null;
	int[][] currentMaterialIndex = null;
	int currentNormalBinding = VRMLHelper.DEFAULT;
	int currentMaterialBinding = VRMLHelper.DEFAULT;
	
	final int MAXSIZE = 10000;
	double[] ds = new double[MAXSIZE];
	int[] is = new int[MAXSIZE];
	double[] evil3Vec = new double[3];
	boolean collectingMFVec3 = false;
	int primitiveCount, polygonCount, coordinate3Count;
	SceneGraphComponent cameraNode = null;
}
vrmlFile returns [SceneGraphComponent r]
{ r = null;}
		:
		HEADER
		r = vrmlScene
	;

vrmlScene returns [SceneGraphComponent r]
{r = null; }
		:
		{
		root = new SceneGraphComponent();
		currentSGC = root;
		currentPath.push(root);
		primitiveCount = polygonCount = 0;
		}
		(statement)*
		{
			r = root;
			System.err.println("Read in "+primitiveCount+" primitives with a total of "+polygonCount+" faces.");
			System.err.println("There were "+coordinate3Count+" vertex lists");
		}
	;

statement:
		"DEF"	id	statement		//TODO statements must return values, and then get put into a map
	|	"USE"	id					// TODO pull out the value from the map
	|	atomicStatement
	;
	
atomicStatement:
		separatorStatement
	|	infoStatement
	| 	transformStatement
	| 	translationStatement
	|	rotationStatement
	|	scaleStatement
	|	matrixTransformStatement
	| 	shapeHintsStatement
	|	currentAp=materialStatement
	|	coordinate3Statement
	|	normalStatement
	|	currentNormalBinding=normalBindingStatement
	|	currentMaterialBinding=materialBindingStatement
	|	cubeStatement
	|	indexedFaceSetStatement
	|	indexedLineSetStatement
	|	perspectiveCameraStatement
	|	unknownStatement
	;

separatorStatement:
		"Separator"			
			{
				SceneGraphComponent sgc = new SceneGraphComponent();
				currentSGC.addChild(sgc);
				currentSGC = sgc;
				currentPath.push(sgc);
			}
		OPEN_BRACE	
			(statement)* 	
		CLOSE_BRACE			
			{
				currentPath.pop();
				currentSGC = currentPath.getLastComponent();
				if (VRMLHelper.verbose) System.err.println("Got Separator"); 
			}
	;

infoStatement:
	"Info"	OPEN_BRACE	(infoAttribute)* 	CLOSE_BRACE
	;
	
infoAttribute:
	"string"	sfstringValue
	;
	
transformStatement
{FactoredMatrix fm = new FactoredMatrix();}
	:
	"Transform"	OPEN_BRACE	(transformAttribute[fm])*	CLOSE_BRACE
	{
		// TODO check to be sure the transform not already set; if it is,
		// make child
		currentSGC.setTransformation(new Transformation( fm.getArray())); 
	}
	;

transformAttribute[FactoredMatrix fm]
{ double[] rr = null; }
	:
		// TODO add more attributes
		"rotation"	rr=sfrotationValue	{fm.setRotation(rr[3], rr[0], rr[1], rr[2]);}
	|	"scale"		rr=sfvec3fValue		{fm.setStretch(rr[0], rr[1], rr[2]);}
	|	"translation"	rr=sfvec3fValue	{fm.setTranslation(rr[0], rr[1], rr[2]);}
	|	"center"		rr=sfvec3fValue		{fm.setCenter(rr);}
	;

matrixTransformStatement returns [double[] mat]
{ mat = null;}
	:
	"MatrixTransform"		OPEN_BRACE	mat = sffloatValues CLOSE_BRACE	
	{
		// TODO move this up to the "invoking" rule
		currentSGC.setTransformation(new Transformation( mat)); 
	}
	;
	
translationStatement returns [double[] mat]
{mat = null;  double[] t = null; }
		:
		"Translation"	OPEN_BRACE "translation" t=sfvec3fValue CLOSE_BRACE		
		{
			if (VRMLHelper.verbose) System.err.println("Got Translation");
			mat = P3.makeTranslationMatrix(null, t, Pn.EUCLIDEAN);
			if (currentSGC.getTransformation() == null)	
				currentSGC.setTransformation( new Transformation());
			currentSGC.getTransformation().multiplyOnRight(mat); 
		}
		;

rotationStatement returns [double[] mat]
{mat = null; double[] t = null; }
		:
		"Rotation"	OPEN_BRACE	"rotation" t=sfrotationValue CLOSE_BRACE		
		{
			if (VRMLHelper.verbose) System.err.println("Got Rotation");
			double[] axis = new double[]{t[0], t[1], t[2]};
			mat = P3.makeRotationMatrix(null, axis, t[3]);
			if (currentSGC.getTransformation() == null)	
				currentSGC.setTransformation( new Transformation());
			currentSGC.getTransformation().multiplyOnRight(mat); 
		}
		;

scaleStatement returns [double[] mat]
{mat = null; double[] t = null; }
		:
		"Scale"	OPEN_BRACE	"scaleFactor" t=sfvec3fValue CLOSE_BRACE		
		{
			if (VRMLHelper.verbose) System.err.println("Got Scale");
			mat = P3.makeStretchMatrix(null, t);
			if (currentSGC.getTransformation() == null)	
				currentSGC.setTransformation( new Transformation());
			currentSGC.getTransformation().multiplyOnRight(mat); 
		}
		;
	
shapeHintsStatement:
		"ShapeHints"	OPEN_BRACE	(shapeHintAttribute)* CLOSE_BRACE		
		{if (VRMLHelper.verbose) System.err.println("Got ShapeHints"); }
	;

shapeHintAttribute:
		"vertexOrdering"	("COUNTERCLOCKWISE" | "CLOCKWISE")
	|	"shapeType"		"SOLID"
	|	"faceType"		("CONVEX" | "UNKNOWN_FACE_TYPE")
	|	"creaseAngle"	number
	|	unknownAttribute
	;
	
materialStatement returns [Appearance ap]
{ap = new Appearance();}
	:
	"Material" OPEN_BRACE  (materialAttribute[ap])*	CLOSE_BRACE			
	{
		if (VRMLHelper.verbose) System.err.println("Got Material"); 
		currentSGC.setAppearance(ap);
	}
	;

materialAttribute[Appearance ap]
{	Color[] c=null; double d = 0.0; }
	:
	// TODO check whether there are multiple values returned; may need to set the color per face or vertex
	 	"ambientColor"	c=mfcolorValue 	{ap.setAttribute(CommonAttributes.AMBIENT_COLOR, c[0]);}
	 |	"diffuseColor"	c=mfcolorValue {ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, c[0]);}
	 |	"specularColor"	c=mfcolorValue {ap.setAttribute(CommonAttributes.SPECULAR_COLOR, c[0]);}
	 | 	"emissiveColor" c=mfcolorValue //{ap.setAttribute(CommonAttributes.EMISSIVE_COLOR, c[0]);}
	 | 	"transparency"	d=sffloatValue {ap.setAttribute(CommonAttributes.TRANSPARENCY, d);}
	 | 	"shininess"	d=sffloatValue {ap.setAttribute(CommonAttributes.SPECULAR_EXPONENT, d);}
	 ;
	 
coordinate3Statement returns [DataList dl]
{ dl = null;  double[] points = null;}
	:				
	"Coordinate3"	OPEN_BRACE	"point" 
			points = mfvec3fValue
			CLOSE_BRACE							
			{
			if (VRMLHelper.verbose)	{
				System.err.println("Got Coordinate3");
				System.err.println("Points: "+Rn.toString(points));
			}
			
			dl = currentCoordinate3 = StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(points);
		 	coordinate3Count++;
		 	}
	;
	
normalStatement returns [DataList dl]
{ dl = null;  double[] normals = null;}
	:
	"Normal"	OPEN_BRACE	"vector" 
			normals=mfvec3fValue
			CLOSE_BRACE
	{ 
		if (VRMLHelper.verbose)	System.err.println("Got Normal"); 
		dl = currentNormal = StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(normals);
	}	
	;

normalBindingStatement returns [int nb]
{ nb = 0;}
	:
	"NormalBinding"	OPEN_BRACE	"value" nb=bindingAttribute CLOSE_BRACE
	;
	
materialBindingStatement returns [int mb]
{ mb = 0;}
	:
	"MaterialBinding"	OPEN_BRACE	"value" mb=bindingAttribute CLOSE_BRACE
	;
	
bindingAttribute returns [int which]
{ which = VRMLHelper.DEFAULT; }
	:
		"DEFAULT"			{which = VRMLHelper.DEFAULT;	}
	|	"OVERALL"			{which = VRMLHelper.OVERALL;	}
	|	"PER_PART"			{which = VRMLHelper.PER_PART;	}
	|	"PER_PART_INDEXED"	{which = VRMLHelper.PER_PART_INDEXED;	}
	|	"PER_FACE"			{which = VRMLHelper.PER_FACE;	}
	|	"PER_FACE_INDEXED"	{which = VRMLHelper.PER_FACE_INDEXED;	}
	|	"PER_VERTEX"			{which = VRMLHelper.PER_VERTEX;	}
	|	"PER_VERTEX_INDEXED"	{which = VRMLHelper.PER_VERTEX_INDEXED;	}
	;

cubeStatement returns [SceneGraphComponent sgc]
{ sgc = null; double w=2, h=2, d=2;}	
	:
	"Cube"	OPEN_BRACE	(
			("width"		w = 	sffloatValue)
		| 	("height"	h = 	sffloatValue)
		| 	("depth"		d = 	sffloatValue)
		)+ CLOSE_BRACE
	{
		IndexedFaceSet cube = Primitives.cube(false);
		sgc = new SceneGraphComponent();
		sgc.setName("cube");
		sgc.setGeometry(cube);
		double[] scaleMatrix = P3.makeStretchMatrix(null, new double[]{w,d,h});
		sgc.setTransformation(new Transformation(scaleMatrix));
		currentSGC.addChild(sgc);
	}
	;
		
indexedFaceSetStatement returns [IndexedFaceSet ifs]
{ifs = null;}
	:
	"IndexedFaceSet"		OPEN_BRACE	(indexedFaceSetAttribute)+ CLOSE_BRACE	
	{
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
	;
	
indexedFaceSetAttribute
{ int[] indices = null; }
	:
		"coordIndex"	indices = mfint32Value		
				{
					if (VRMLHelper.verbose) System.err.println("Got coord "+indices.length+"indices");
					currentCoordinateIndex = VRMLHelper.convertIndices(indices); 
				}
	|	"normalIndex" indices=mfint32Value
				{
					if (VRMLHelper.verbose) System.err.println("Got normal "+indices.length+"indices");
					currentNormalIndex = VRMLHelper.convertIndices(indices); 
				}
	|	"materialIndex" indices=mfint32Value
				{
					if (VRMLHelper.verbose) System.err.println("Got material "+indices.length+"indices");
					currentMaterialIndex = VRMLHelper.convertIndices(indices); 
				}
	;

indexedLineSetStatement:
	"IndexedLineSet"	OPEN_BRACE	(indexedLineSetAttribute)+ CLOSE_BRACE	
	{if (VRMLHelper.verbose)	System.err.println("Got IndexedLineSet"); }
	;
	
indexedLineSetAttribute:
		"coordIndex"	mfint32Value
	;

perspectiveCameraStatement returns [SceneGraphComponent cn]
{	cn = new SceneGraphComponent();
	FactoredMatrix fm = new FactoredMatrix();
	Camera c = new Camera();
	double[] d = null;
	double a = 0.0;
	}
	:
	("PerspectiveCamera"	OPEN_BRACE	(
			"position"		d=sfvec3fValue		{fm.setTranslation(d); }		
		|	"orientation"	d=sfrotationValue	{fm.setRotation(d[3], d[0], d[1], d[2]);}
		|	"focalDistance"	a=sffloatValue		{c.setFocus(a);}
		|	"heightAngle"	a=sffloatValue		{c.setFieldOfView(180.0*a/Math.PI);} )+	CLOSE_BRACE )
	{	
		fm.update();
		cn.setTransformation(new Transformation(fm.getArray()));
		cn.setCamera(c);
		cameraNode = cn;
	}
	;
		
unknownStatement
{String n = null; }
	:
	n=id		OPEN_BRACE  	(unknownAttribute)* CLOSE_BRACE	
	{System.err.println("Unrecognized keyword "+	n); }
	;

id returns [String s]
{ s = null; }
	:	
	n:ID  	{if (VRMLHelper.verbose)	System.err.println("Id matched: "+n.getText()); s=n.getText();}
	;

unknownAttribute:
	id	value
	;
	
value:				// TODO extend this list while keeping the grammar unambiguous
		id
	|	sfboolValue
	| 	sffloatValues
	| 	sfstringValue
	|	mffloatValue
	|	OPEN_BRACE	(unknownAttribute)* CLOSE_BRACE
	;

// From here on, parsing of basic value types	
number returns [double d]	
{d = 0; }
	:
	(f:INT32 {d=Double.parseDouble(f.getText()); } )
	| (g:FLOAT {d=Double.parseDouble(g.getText()); })		
	;
		
sfboolValue returns [boolean b]
{ b = false;}
	:
    ("true" | "TRUE"	{b = true;} ) | ( "false" | "FALSE"  {b = false;} )
	;

sfstringValue
	:
	STRING	
	;

sfcolorValue returns [Color c]
{ c = null; double r, g, b;}
	:
		r=number g=number b=number	{c = new Color( (float)r, (float) g, (float) b); }
	;

sfcolorValues returns [Color[] cl]
{cl = null; 	Color c = null; Vector collect = new Vector(); }
	:
		(c=sfcolorValue	{collect.add(c);}	)+
		{cl = VRMLHelper.listToColorArray(collect);}
	;

mfcolorValue returns [Color[] cl]
{ cl = null;Color c = null;}
	:
		c = sfcolorValue				{ cl = new Color[1];	cl[0] = c; }
	|	OPEN_BRACKET CLOSE_BRACKET
	|	OPEN_BRACKET cl=sfcolorValues CLOSE_BRACKET
	;

sffloatValue returns [double d]
{ d = 0; }
	:
	d = number
	;

sffloatValues returns [double[] dl]
{
	dl = null;
	Vector vl = new Vector();
	double d = 0;
}
	:
		(d=sffloatValue	{vl.add(new Double(d)); } )+
		{dl = VRMLHelper.listToDoubleArray(vl);}
	;

mffloatValue returns [double[] dl]
{ double d = 0; dl = null; }
	:
		d=sffloatValue		{dl = new double[]{d}; }
	|	OPEN_BRACKET CLOSE_BRACKET
	|	OPEN_BRACKET dl=sffloatValues CLOSE_BRACKET	{}
	;


sfint32Value returns [int i]
{ i = 0;}
	:
	    f:INT32	{i = Integer.parseInt(f.getText()); }
	;

sfint32Values returns [int[] il]
{
	il = null;
	Vector vl = new Vector();
	int t = 0;
	int count = 0;
}
	:
		(t=sfint32Value	
			{
				if (count +1 > is.length)	{
					is=VRMLHelper.reallocate(is);
				}
				is[count++] = t; 
			} 
		)+
		{il = new int[count];
		System.arraycopy(is,0,il,0,count);
		//VRMLHelper.listToIntArray(vl); 
		}
	;

mfint32Value returns [int[] i]
{i = null; int t = 0; }
	:
		t = sfint32Value			{ i = new int[1];  i[0] = t; }
	|	OPEN_BRACKET CLOSE_BRACKET
	|	OPEN_BRACKET i=sfint32Values CLOSE_BRACKET
	;


sfrotationValue returns [double[] rv]
{	double a,b,c,d; rv = null;}
	:
	a=number  b=number c=number d=number	{rv = new double[]{a,b,c,d}; }
	;

sfrotationValues:
		(sfrotationValue)+
	;

mfrotationValue:
		sfrotationValue
	|	OPEN_BRACKET CLOSE_BRACKET
	|	OPEN_BRACKET sfrotationValues CLOSE_BRACKET
	;

mfstringValue:
		sfstringValue
	|	OPEN_BRACKET CLOSE_BRACKET
	|	OPEN_BRACKET sfstringValues CLOSE_BRACKET
	;

sfstringValues:
		(sfstringValue)+
	;

sfvec2fValue:
		number number
	;

sfvec2fValues:
		(sfvec2fValue)+
	;

mfvec2fValue:
		sfvec2fValue
	|	OPEN_BRACKET CLOSE_BRACKET
	|	OPEN_BRACKET sfvec2fValues CLOSE_BRACKET
	;

sfvec3fValue returns [double[] vec3]
{vec3 = null;
double a, b, c;}
	:
		a=number b=number c=number	
		{	
			if (collectingMFVec3)	{
				evil3Vec[0] = a;  evil3Vec[1] = b; evil3Vec[2] = c;
				vec3 = evil3Vec;
			} else 
				vec3 = new double[]{a,b,c}; 
		}
	;

sfvec3fValues returns [double[] vec3array]
{vec3array = null;
//List collect = new Vector();
double[] onevec  = null;
collectingMFVec3 = true;
int count = 0;
	}
	:
		(onevec = sfvec3fValue	
		{	
			if (count + 3 >= ds.length)	{
				// Reallocate!
				ds = VRMLHelper.reallocate(ds);
			}
			for (int i=0; i<3; ++i)	ds[count+i] = onevec[i];
			count += 3;
			//collect.add(onevec);
		} )+
		{
			vec3array = new double[count];
			System.arraycopy(ds, 0, vec3array, 0, count);
			//vec3array = VRMLHelper.listToDoubleArrayArray(collect);
			collectingMFVec3 = false; 
		}
	;

mfvec3fValue returns [double[] vec3array]
{vec3array = null;
double[] onevec = null;
}
	:
		vec3array=sfvec3fValue				
	|	OPEN_BRACKET CLOSE_BRACKET
	|	OPEN_BRACKET vec3array = sfvec3fValues CLOSE_BRACKET
	;

/************************************************************************************
 * The VRML Lexer
 ************************************************************************************
 */
class VRMLV1Lexer extends Lexer;
options {
	charVocabulary = '\3'..'\377';
	k=2;
	testLiterals=false;
	filter=IGNORE;
}
	/* Terminal Symbols */
PERIOD:			'.';
OPEN_BRACE:		'{';
CLOSE_BRACE:		'}';
OPEN_BRACKET:	'[';
CLOSE_BRACKET:	']';

ID
options {
	paraphrase = "an identifier";
	testLiterals=true;
}
	:	('a'..'z'|'A'..'Z'|'_') (ID_LETTER)*
	;

protected 
ID_LETTER:
	('a'..'z'|'A'..'Z'|'_'|'0'..'9')
	;

INT_OR_FLOAT : (DECIMAL_BEGIN) {$setType(INT32);}  
	( (('.' (DIGIT)+ (EXPONENT)? ) | EXPONENT) {$setType(FLOAT);} )?;

INT32:
		DECIMAL_BEGIN | ('0' ('x'|'X') ('0'..'9' | 'a'..'f' | 'A'..'F')+)
	;

protected
DIGIT:	
	('0'..'9')
	;
	
protected 
DECIMAL_BEGIN:
	('+'|'-')? (DIGIT)+
	;
	
protected
EXPONENT:
	(('e'|'E') ('+'|'-')? ('0'..'9')+) 
	;
	
	/* ".*" ... double-quotes must be \", backslashes must be \\... */
STRING:
		'"' (ESC | ~('"'|'\\'))* '"'
	;

protected
ESC:
		'\\' ('\\' | '"')
	;

protected
RESTLINE:
	 (~('\n'))* ('\n')
	 ;
	
protected
HEADER1:	"#VRML V1.0";

HEADER:	{getLine()==1}?	HEADER1 RESTLINE
	{System.err.println("Got header");}
	;
	
COMMENT:	
 	'#'  (~('\n'))* ('\n')
	{ System.err.println("Skipping comment "); $setType(Token.SKIP); }
	;

WS_:
		( ' '
		| '\t'
		| '\f'
		| ','
		// handle newlines
		|	(options {
					generateAmbigWarnings=false;
				}
		: "\r\n"	// Evil DOS
			| '\r'		// MacINTosh
			| '\n'		// Unix (the right way)
			)
		)+ { $setType(Token.SKIP); }
	;

protected
IGNORE:
	'{' (~('}'))* '}'
	;
