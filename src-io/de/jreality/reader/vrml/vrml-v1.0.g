//**************************************************
// * VRML 1.0 Parser
// */

header {
/*
 *	@author gunn
 *  Nov. 30, 2005
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
}

/*****************************************************************************
 * The VRML version 1.0 Parser
 *****************************************************************************
 */
class VRMLV1Parser extends Parser;
options {
	k = 2;							// two token lookahead
}
{
	// this is what is returned from the parsing process
	SceneGraphComponent root = null;
	// current state of the parsing process
	SceneGraphComponent currentSGC = null;
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
	
	// we use a dynamic allocation scheme, beginning with arrays of length 10000
	final int INITIAL_SIZE = 10000;
	double[] ds = new double[INITIAL_SIZE];		// for storing double arrays
	int[] is = new int[INITIAL_SIZE];				// for storing int arrays
	double[] tempVector3 = new double[3];
	boolean collectingMFVec3 = false;
	// for collecting statistics
	int primitiveCount, polygonCount, coordinate3Count;
	int[] ngons = new int[10];
	SceneGraphComponent cameraNode = null;
}

// the "main" rule for the parser
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
			System.err.println("Read in "+primitiveCount+" indexed face sets with a total of "+polygonCount+" faces.");
			System.err.println("There were "+coordinate3Count+" vertex lists");
		}
	;

statement:
		"DEF"	id	statement		//TODO statements must return values, and then get put into a map
	|	"USE"	id					// TODO pull out the value from the map
	|	atomicNode
	;

// TODO put the global assignments into this rule, and pull out of the node rules themselves
atomicNode:
		separatorNode
	|	infoNode
	| 	transformNode
	| 	translationNode
	|	rotationNode
	|	scaleNode
	|	matrixTransformNode
	| 	shapeHintsNode
	|	directionalLightNode
 	|	currentAp=materialNode
	|	currentCoordinate3=coordinate3Node		{coordinate3Count++;}
	|	currentNormal=normalNode
	|	currentNormalBinding=normalBindingNode
	|	currentMaterialBinding=materialBindingNode
	|	cubeNode
	|	indexedFaceSetNode
	|	indexedLineSetNode
	|	cameraNode=perspectiveCameraNode
	|	unknownNode
	;

separatorNode:
		g:"Separator"			
			{
				SceneGraphComponent sgc = new SceneGraphComponent();
				sgc.setName("LineNo "+g.getLine());		// for looking up later
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

infoNode returns [String[] info]
{ info = null; 
  String s = null;
  Vector v = new Vector();}
	:
	"Info"	OPEN_BRACE	(s=infoAttribute		{v.add(s);} )* 	CLOSE_BRACE
	{
		info = new String[v.size()];
		v.toArray(info);
		//LoggingSystem.getLogger(this).info("Got "+info.length+" info strings.");
		if (VRMLHelper.verbose) System.err.println("Got "+info.length+" info strings.");
	}
	;
	
infoAttribute returns [String s]
{s = null; }
	:
	"string"	s=sfstringValue
	;

//
//  *************** Transformation-related noded *******************
//	
transformNode
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

matrixTransformNode returns [double[] mat]
{ mat = null;}
	:
	"MatrixTransform"		OPEN_BRACE	mat = sffloatValues CLOSE_BRACE	
	{
		// TODO move this up to the "invoking" rule
		currentSGC.setTransformation(new Transformation( mat)); 
	}
	;
	
translationNode returns [double[] mat]
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

rotationNode returns [double[] mat]
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

scaleNode returns [double[] mat]
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
	
shapeHintsNode:
		"ShapeHints"	OPEN_BRACE	(shapeHintAttribute)* CLOSE_BRACE		
		{if (VRMLHelper.verbose) System.err.println("Got ShapeHints"); }
	;

shapeHintAttribute:
                "vertexOrdering"        ("COUNTERCLOCKWISE" | "CLOCKWISE")
     |        "shapeType"                ("SOLID" | "UNKNOWN_SHAPE_TYPE" )
     |        "faceType"                ("CONVEX" | "UNKNOWN_FACE_TYPE")
	|	"creaseAngle"	number
	|	unknownAttribute
	;
	
directionalLightNode returns [DirectionalLight dl]
{     dl = new DirectionalLight();
     SceneGraphComponent sgc = new SceneGraphComponent();
    sgc.setLight(dl);
        }
        :
        "DirectionalLight"        OPEN_BRACE        (directionalLightAttribute[sgc])* CLOSE_BRACE
       ;
        
directionalLightAttribute[SceneGraphComponent sgc]
{       boolean b = false;
     double d = 1.0;
     Color c = null;
     double[] dir = null;
     }
        :
                "on"                b=sfboolValue               { if (VRMLHelper.verbose)      System.err.println("Got on"); }
      |        "intensity"        d=sffloatValue                { if (VRMLHelper.verbose)      System.err.println("Got intensity"); sgc.getLight().setIntensity(d); }
     |        "color"        c=sfcolorValue                     { if (VRMLHelper.verbose)      System.err.println("Got color"); sgc.getLight().setColor(c);      }
        |        "direction" dir=sfvec3fValue               
                { 
                    Transformation tt = new Transformation();
                    tt.setMatrix(P3.makeLookatMatrix(null, P3.originP3, dir, 0.0, Pn.EUCLIDEAN));
                    sgc.setTransformation(tt);
                    if (VRMLHelper.verbose)      System.err.println("Got direction"); 
                }
        ;
// TODO fix this
// The Material node has values that are potentially arrays (mfcolorValue and mffloatValue)
materialNode returns [Appearance ap]
{ap = new Appearance();}
	:
	"Material" OPEN_BRACE  (materialAttribute[ap])*	CLOSE_BRACE			
	{
		if (VRMLHelper.verbose) System.err.println("Got Material"); 
		currentSGC.setAppearance(ap);
	}
	;
//TODO fix this
// remove mfcolorValue, replace with mfvec3fValue.
// look at current state of MaterialBinding to decide what to do with the return values
// If PER_PART, then values need to be converted to Color type
materialAttribute[Appearance ap]
{	Color[] c=null; double[] d = null; }
	:
	// TODO check whether there are multiple values returned; may need to set the color per face or vertex
	 	"ambientColor"	c=mfcolorValue 	{ap.setAttribute(CommonAttributes.AMBIENT_COLOR, c[0]);}
	 |	"diffuseColor"	c=mfcolorValue {ap.setAttribute(CommonAttributes.DIFFUSE_COLOR, c[0]);}
	 |	"specularColor"	c=mfcolorValue {ap.setAttribute(CommonAttributes.SPECULAR_COLOR, c[0]);}
	 | 	"emissiveColor" c=mfcolorValue //{ap.setAttribute(CommonAttributes.EMISSIVE_COLOR, c[0]);}
	 | 	"transparency"	d=mffloatValue {ap.setAttribute(CommonAttributes.TRANSPARENCY, d[0]);}
	 | 	"shininess"	d=mffloatValue {ap.setAttribute(CommonAttributes.SPECULAR_EXPONENT, d[0]);}
	 ;
	 
coordinate3Node returns [DataList dl]
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
			dl = StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(points);
		 	}
	;
	
normalNode returns [DataList dl]
{ dl = null;  double[] normals = null;}
	:
	"Normal"	OPEN_BRACE	"vector" 
			normals=mfvec3fValue
			CLOSE_BRACE
	{ 
		if (VRMLHelper.verbose)	System.err.println("Got Normal"); 
		dl =  StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(normals);
	}	
	;

normalBindingNode returns [int nb]
{ nb = 0;}
	:
	"NormalBinding"	OPEN_BRACE	"value" nb=bindingAttribute CLOSE_BRACE
	;
	
materialBindingNode returns [int mb]
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

cubeNode returns [SceneGraphComponent sgc]
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

sphereNode returns [SceneGraphComponent sgc]
{ sgc = null; double r=1, h=2, d=2;}	
	:
	"Sphere"	OPEN_BRACE	(
			("radius"		r = 	sffloatValue)
		)+ CLOSE_BRACE
	{
		sgc = new SceneGraphComponent();
		sgc.setName("vrml sphere");
		sgc.setGeometry(new Sphere());
		double[] scaleMatrix = P3.makeStretchMatrix(null, r);
		sgc.setTransformation(new Transformation(scaleMatrix));
		currentSGC.addChild(sgc);
	}
	;
		

// TODO
// Decide whether to "inline" the indexedFaceSetAttribute's so that no global variables
// have to be used. (See perspectiveCameraNode below).	
indexedFaceSetNode returns [IndexedFaceSet ifs]
{ifs = null;}
	:
	g:"IndexedFaceSet"		OPEN_BRACE	(indexedFaceSetAttribute)+ CLOSE_BRACE	
	{
	// TODO move this into VRMLHelper somehow
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
	ifsf.update();
	ifs = ifsf.getIndexedFaceSet();
	ifs.setName("IFS:LineNo "+g.getLine());
	// collect some statistics
	primitiveCount++;
	polygonCount += ifs.getNumFaces();
	if (currentSGC.getGeometry() != null) currentSGC.setGeometry(ifs);
	else		{
		SceneGraphComponent sgc = new SceneGraphComponent();
		sgc.setName("LineNo "+g.getLine());		// for looking up later
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

indexedLineSetNode:
	"IndexedLineSet"	OPEN_BRACE	(indexedLineSetAttribute)+ CLOSE_BRACE	
	{if (VRMLHelper.verbose)	System.err.println("Got IndexedLineSet"); }
	;
	
indexedLineSetAttribute:
		"coordIndex"	mfint32Value
	;

// TODO hook this up
perspectiveCameraNode returns [SceneGraphComponent cn]
{	cn = new SceneGraphComponent();
	FactoredMatrix fm = new FactoredMatrix();
	// TODO set default position/orientation for fm?
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
	}
	;
		
unknownNode
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

// ********************** Field parsing rules *******************
// 		From here on, parsing of basic field value types	
//
number returns [double d]	
{d = 0; }
	:
		(f:INT {d=Double.parseDouble(f.getText()); } )
	|	(g:FLOAT {d=Double.parseDouble(g.getText()); } )
	{ if (VRMLHelper.verbose) System.err.println("Got number "+d); }		
	;
		
sfboolValue returns [boolean b]
{ b = false;}
	:
    ("true" | "TRUE"	{b = true;} ) | ( "false" | "FALSE"  {b = false;} )
	;

sfstringValue returns [String s]
{ s = null; }
	:
	g:STRING		{ s = g.getText();}
	;

// TODO fix this 
// should return double[] since that's how IndexedFaceSets define colors
// in that case, can be omitted and use instead sfvec3fValue, sfvec3fValues, and mfvec3fValue
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

// TODO decide whether to optimize this as mfvec3fValue (probably should)
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
	    f:INT	{i = Integer.parseInt(f.getText()); }
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
				tempVector3[0] = a;  tempVector3[1] = b; tempVector3[2] = c;
				vec3 = tempVector3;
			} else 
				vec3 = new double[]{a,b,c}; 
		}
	;

sfvec3fValues returns [double[] vec3array]
{vec3array = null;
//List collect = new Vector();
double[] onevec  = null;
collectingMFVec3 = true;			// optimized reading into a big double[] array
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
		} )+
		{
			vec3array = new double[count];
			System.arraycopy(ds, 0, vec3array, 0, count);
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

//INT_OR_FLOAT : 
//   		((('+'|'-')? DECIMAL_FRACTION (EXPONENT)?) {$setType(FLOAT);})
//	|	(DECIMAL_INT) {$setType(INT32);}  (DECIMAL_FRACTION (EXPONENT)? {$setType(FLOAT);} )?
//INT_OR_FLOAT : (DECIMAL_INT) {$setType(INT32);}  
//       ( (('.' (DIGIT)+ (EXPONENT)? ) | EXPONENT) {$setType(FLOAT);} )?
//       ;

//INT:
//	(('+'|'-')? (DIGIT)+) | ('0' ('x'|'X') ('0'..'9' | 'a'..'f' | 'A'..'F')+)
//	;

INT_OR_FLOAT:
//   optional sign   either integer w/exponent or   real number with optional exponent
//  this rule doesn't accept simple integers, only integers with exponents
//	('+'|'-')? ( ( (DIGIT)+ EXPONENT) | ( (DIGIT)* '.' (DIGIT)+   (EXPONENT)?) )
	('+'|'-')? ((DIGIT)* (EXPONENT)? {$setType(INT);}) ('.' {$setType(FLOAT); }  (DIGIT)+   (EXPONENT)?)?
	;
	
protected
DIGIT:	
	('0'..'9')
	;
	
protected 
DECIMAL_INT:
	('+'|'-')? (DIGIT)+
	;

protected
DECIMAL_FRACTION:
	'.' (DIGIT)+
	;
	
protected
EXPONENT:
	(('e'|'E') ('+'|'-')? (DIGIT)+) 
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
			{newline(); } )	
		)+ { $setType(Token.SKIP); }
	;

protected
IGNORE:
	'{' (~('}'))* '}'
	;
