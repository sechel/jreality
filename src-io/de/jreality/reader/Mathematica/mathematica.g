//**************************************************
// * Mathematica Parser
// */

header {
package de.jreality.reader.Mathematica;
import java.awt.*;
import java.util.*;
import de.jreality.geometry.*;
import de.jreality.math.*;
import de.jreality.scene.data.*;
import de.jreality.scene.*;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.*;
}

class MathematicaParser extends Parser;
options {
	k = 2;							// two token lookahead
}
{
	// this is what is returned from the parsing process
	public SceneGraphComponent root = new SceneGraphComponent();	
	SceneGraphComponent current = root;
	Appearance globalApp =new Appearance();
	Color plCDefault= new Color(255,0,0);
	Color fCDefault = new Color(0,255,0);
	
//	System.err.println("Color is "+Rn.toString(colorData[i]));
	
	public  double[] getRGBColor(Color c){
	// retuns a array that represents a color (needed for colored faces)
		double[] fl= new double[3];
		fl[0]=c.getRed()/255.0;
		fl[1]=c.getGreen()/255.0;
		fl[2]=c.getBlue()/255.0;
		return fl ;
	}
}


start returns [SceneGraphComponent r]
{ r = null;	
	globalApp.setName("global");
	root.setAppearance(globalApp);
	root.setName("Mathematica");
}
	:"Graphics3D"
	  OPEN_BRACKET  
	  	( 	  list[plCDefault,fCDefault]
	  		| fCDefault=faceThing[fCDefault]
	  		| plCDefault=plThing[plCDefault]
	  		| appThing
	  	)
	  	(optionen)? 
	  CLOSE_BRACKET 
		{ r = root;}
	;

// Objects ---------------------------------------------
protected
list[Color plC, Color fC]
	:	OPEN_BRACE				// Liste von Graphischen Objekten
			{
			SceneGraphComponent newPart = new SceneGraphComponent();
			newPart.setName("Object");
			SceneGraphComponent oldPart = current;
			current.addChild(newPart);
			current=newPart;
			}
	        (objectList[plC,fC])?
	    CLOSE_BRACE
			{current=oldPart;}
	;
protected
faceThing [Color fCgiven] returns[Color fC]
{fC=fCgiven;
 }
	:	cubic[fC]					// Wuerfel 
	|	fC=polygonBlock[fC]			// Abfolge von Polygonen (indexed FaceSetFactory)
	|	fC=faceColor
	;


protected
plThing [Color plCgiven] returns[Color plC]
{plC=plCgiven;}
	:	plC= color			// Farbe fuer Punkte Linien und Texte
	|	plC= lineBlock [plC]	// Abfolge von Linien (LineSetFactory)
	|	plC= pointBlock [plC]	// Abfolge von Punkten (PointSetFactory)
	|	text [plC]					// Text an einem Punkt im Raum
	;
	
protected
appThing
	{}
	:	directiveBlock			// Abfolge von graphischen Direktiven (erzeugt eine Appearance)
	;
	
protected
faceColor returns[Color fC]
{Color specular; double d; fC= new Color(255,0,0);}
	: "SurfaceColor" OPEN_BRACKET
			fC=color
			( COLON specular=color	(	COLON 	d=doublething )?)?	// ignore !
		CLOSE_BRACKET 
	;

protected
objectList [Color plC, Color fC]
	:(
		  list[ plC, fC]
		| fC=faceThing[fC]
		| plC=plThing[plC]
		| appThing
	 )
	 ( COLON 
		(	
			  list[ plC, fC]
			| fC =faceThing[fC]
			| plC=plThing[plC]
			| appThing
	 	)
	 )*
	;	
	
// 	Farben
protected
color returns[Color c]
{c= new Color(0,255,0);
}

		: "RGBColor" OPEN_BRACKET 
					{double r,g,b; r=b=g=0;}
					r=doublething COLON g=doublething COLON b=doublething
				CLOSE_BRACKET 
					{
					 float red,green,blue;
					 red=(float) r; green=(float) g; blue=(float) b;
					 c= new Color(red,green,blue);
					 }
		| "Hue" 	OPEN_BRACKET 
					{double h; double s; double b; h=s=b=0.5;}
					h= doublething 
					(COLON s=doublething COLON b=doublething )?
				CLOSE_BRACKET 
					{
					 float hue,sat,bri;	 // konvert to float
					 hue=(float) h; sat=(float) s; bri=(float) b;
					 c = Color.getHSBColor(hue,sat,bri);
					}
		| "GrayLevel" OPEN_BRACKET 
					{double gr=0;} gr=doublething 
				CLOSE_BRACKET 
					{
					float grey=(float) gr;
					c= new Color(grey,grey,grey);
					}
		| "CMYKColor" OPEN_BRACKET 
					{double cy,ma,ye,k; cy=ma=ye=k=0; }
					cy=doublething COLON 
					ma=doublething COLON 
					ye=doublething COLON 
					k=doublething 
				CLOSE_BRACKET 
					{
					 float r,g,b;
					 r=(float) ((1-cy)*(1-k));
					 g=(float) ((1-ma)*(1-k));
					 b=(float) ((1-ye)*(1-k));
					 c= new Color(r,g,b);
					}
		;
		
// Koordinaten in einer Liste 
protected
lineset returns[Vector v]
{double [] point=new double[3];
v=new Vector();}
		: OPEN_BRACE
		  point=vektor
			{v.add(point);}
		  (COLON 
			{point= new double[3];}
			point=vektor
			{v.add(point);}
		  )*
		 CLOSE_BRACE
		;

// ein KoordinatenTripel
protected
vektor returns[double[] res]
{res =new double [3];
double res1,res2,res3;}
	: 	OPEN_BRACE 
			res1=doublething COLON res2=doublething COLON res3=doublething 
		CLOSE_BRACE
			{res[0]=res1;
			res[1]=res2;
			res[2]=res3;}
	;

protected
pointBlock [Color plCgiven] returns [Color plC]
{Vector points= new Vector(); 
 double[] v;
 Vector colors= new Vector();
 plC=plCgiven;
}
	:
	( "Point"
	   OPEN_BRACKET
				{v=new double[3];}
				v=vektor
				{points.add(v);
				 colors.add(getRGBColor(plC));}
	   CLOSE_BRACKET 
	)
	(
	COLON
	(
	
	   plC=color
	 |( "Point"
	   OPEN_BRACKET
				{v=new double [3];}
				v=vektor
				{points.add(v);
				 colors.add(getRGBColor(plC));}
	   CLOSE_BRACKET 
	   )
	 )
	)*
	{
		PointSetFactory psf = new PointSetFactory();
		double [][] data = new double [points.size()][];
		double [][] colorData = new double[points.size()][];
		for(int i=0;i<points.size();i++){
			data[i]=(double [])points.get(i);
			colorData[i]=(double [])colors.get(i);
		}

		psf.setVertexCount(points.size());
		psf.setVertexCoordinates(data);
		psf.setVertexColors(colorData);
		psf.update();
		
		SceneGraphComponent geo=new SceneGraphComponent();		
		Appearance pointApp =new Appearance();
		pointApp.setAttribute(CommonAttributes.VERTEX_DRAW, true);
	    pointApp.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		geo.setAppearance(pointApp);
		geo.setGeometry(psf.getPointSet());
		geo.setName("Points");
		current.addChild(geo);
	}
	;  

protected
lineBlock [Color plCgiven] returns[Color plC]			// liest erst eine, dann alle direkt folgenden Lines ein
{plC=plCgiven;
 Vector coordinates= new Vector();			// alle Punkte in einer Liste
 Vector line=new Vector();					// alle Punkte einer Linie
 Vector colors= new Vector();
 int count=0;								// Anzahl aller bisher gesammelten Punkte
 int[] lineIndices;							// liste aller Indices einer Linie
 Vector linesIndices= new Vector();}		// Liste aller IndiceeListen
 :
	"Line"
	 OPEN_BRACKET
				line=lineset 			// das ist ein Vector von double[3]
				{
					lineIndices=new int[line.size()];
					for(int i=0;i<line.size();i++){
						coordinates.add(line.get(i));  //Punkte zu einer Liste machen
				    	lineIndices[i]=i;			   // indizirung merken
				    }
			    	count=line.size();
					linesIndices.add(lineIndices);
				    colors.add(getRGBColor(plC));
				    System.err.println("Color is "+Rn.toString(getRGBColor(plC)));
				}
	 CLOSE_BRACKET 
	(
	   plC=color
	 | COLON "Line"	
	   OPEN_BRACKET
				line=lineset 			// das ist ein Vector von double[3]
				{
					lineIndices=new int[line.size()];
					for(int i=0;i<line.size();i++){			// mithilfe von 'count' weiterzaehlen
						coordinates.add(line.get(i));  		// Punkte zu einer Liste machen
				    	lineIndices[i]=i+count;			    // indizirung merken
				    }
			    	count+=line.size();
					linesIndices.add(lineIndices);
					colors.add(getRGBColor(plC));
					System.err.println("Color is "+Rn.toString(getRGBColor(plC)));
				}
	   CLOSE_BRACKET 
	)*
	{
			double [][] data= new double[coordinates.size()][];
			double [][] colorData = new double[linesIndices.size()][];
			for(int i=0;i<coordinates.size();i++){
				data[i]= (double[])coordinates.get(i);
			}
			int[][] indices= new int[linesIndices.size()][];
			
			for(int i=0;i<linesIndices.size();i++){		// Indices als doppelListe von Doubles machen
				indices[i]=(int [])linesIndices.get(i);
				colorData[i]=(double [])colors.get(i);
			}

			IndexedLineSetFactory lineset=new IndexedLineSetFactory();
			lineset.setLineCount(linesIndices.size());
			lineset.setVertexCount(coordinates.size());
			lineset.setEdgeIndices(indices);
			lineset.setVertexCoordinates(data);
			lineset.setVertexColors(colorData);
			lineset.update();
			
			SceneGraphComponent geo=new SceneGraphComponent();
			Appearance lineApp =new Appearance();
			lineApp.setAttribute(CommonAttributes.EDGE_DRAW, true);
			lineApp.setAttribute(CommonAttributes.TUBES_DRAW, true);
	//	<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Punkte an den Enden anzeigen >
			lineApp.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		    lineApp.setAttribute(CommonAttributes.SPHERES_DRAW, true);
	//
			geo.setAppearance(lineApp);
			geo.setGeometry(lineset.getIndexedLineSet());
			geo.setName("Lines");
			current.addChild(geo);
		}
	;  


protected 
polygonBlock [Color fCgiven] returns[Color fC]
{fC=fCgiven;
 Vector coordinates= new Vector(); 	// alle PunktListen vereint in einer
 Vector poly=new Vector();			// alle Punkte in einem Polygon
 int[] polyIndices;					// alle indices eines Polygons
 Vector colors= new Vector();
 Vector polysIndices= new Vector();
 int count=0;						// zaehlt die Punkte mit
 }
	:"Polygon"{System.out.println("ok1");}
	 OPEN_BRACKET
				poly=lineset 			// das ist ein Vector von double[3]
				{
					polyIndices=new int[poly.size()+1];
					for(int i=0;i<poly.size();i++){
						coordinates.add(poly.get(i));  //Punkte zu einer Liste machen
				    	polyIndices[i]=i;			   // indizirung merken
				    }
				    polyIndices[poly.size()]=0;
			    	count=poly.size();
					polysIndices.add(polyIndices);
				    colors.add(getRGBColor(fC));
				}
	 CLOSE_BRACKET 
	( COLON
	 (
	   (fC=faceColor {System.out.println("ok");})
	  |("Polygon"{System.out.println("ok2");}
	    OPEN_BRACKET
				poly=lineset 			// das ist ein Vector von double[3]
				{
					polyIndices=new int[poly.size()+1];
					for(int i=0;i<poly.size();i++){
						coordinates.add(poly.get(i));  //Punkte zu einer Liste machen
				    	polyIndices[i]=i+count;			   // indizirung merken
				    }
				    polyIndices[poly.size()]=count;
			    	count+=poly.size();
					polysIndices.add(polyIndices);
					colors.add(getRGBColor(fC));
				}
	    CLOSE_BRACKET
	  ))
	)*
	{
		double [][] data= new double[count][];
		double [][] colorData = new double[polysIndices.size()][];
		for(int i=0;i<count;i++){				// Punkte zum flachen DoubleArray machen
			data[i]=((double[]) coordinates.get(i));
		}
		int[][] indices= new int[polysIndices.size()][];
		for(int i=0;i<polysIndices.size();i++){		// Indices als doppelListe von Doubles machen
			indices[i]=(int[])polysIndices.get(i);
			colorData[i]=(double [])colors.get(i);
		}
		IndexedFaceSetFactory faceSet = new IndexedFaceSetFactory();
		faceSet.setVertexCount(count);
		faceSet.setFaceCount(polysIndices.size());
		faceSet.setFaceIndices(indices);
		faceSet.setVertexCoordinates(data);
		faceSet.setFaceColors(colorData);
		faceSet.setGenerateFaceNormals(true);
		faceSet.update();
		
		
		SceneGraphComponent geo=new SceneGraphComponent();	// Komponenten erstellen und einhaengen
		current.addChild(geo);
		geo.setName("Faces");
		geo.setGeometry(faceSet.getIndexedFaceSet());
	}
	;

// ----------------------------------------------- neu ---------------------------------------------
// -------------------------------------------------------------------------------------------------
// ----------------------------------------------- alt ---------------------------------------------


protected 
cubic [ Color fC]
	:"Cubic"
	 OPEN_BRACKET 
			{double[] v2=new double [3]; 
			v2[0]=v2[1]=v2[2]=1;
			double[] v=new double[3];
			}
			v=vektor ( COLON v2=vektor )? 
	 CLOSE_BRACKET 
			{
			 SceneGraphComponent geo=new SceneGraphComponent();
			 current.addChild(geo);
			 geo.setGeometry(Primitives.cube());
	 		 geo.setName("Cube");
			 MatrixBuilder.euclidian().scale(v2[0],v2[1],v2[2])
			    .translate(v[0],v[1],v[2]).assignTo(geo);
 			}
 	;
 	
protected
text [ Color plC ]
{double[] v=new double[3]; String t;}
	:"Text"		OPEN_BRACKET 
					s:STRING COLON v=vektor 	
				CLOSE_BRACKET 
					{t=s.getText();}
	;

	
	
// Directives ---------------------------------------- Phase 2: keine neuen Knoten, sondern app mitschleifen

protected 
directiveBlock
	{
	 SceneGraphComponent dir=new SceneGraphComponent();
	 dir.setName("Directive");
	 current.addChild(dir);
	 Appearance app =new Appearance();
	}
	: app=directive[app]
	  (
	  	COLON
	  	app=directive[app]
	  )*
	{current=dir;
	 dir.setAppearance(app);}
	;

protected 
directive[Appearance appGiven] returns [Appearance app]
{app = appGiven; 
Color col;}
	:"AbsoluteDashing" OPEN_BRACKET  dumb CLOSE_BRACKET 
	|"AbsolutePointsize" 
				OPEN_BRACKET
					{int d=0;} d=integerthing 
				CLOSE_BRACKET 
					{}
	|"AbsoluteThickness"
				OPEN_BRACKET  
					{int w=0;} w=integerthing
				CLOSE_BRACKET 
					{}
	|"Dashing" OPEN_BRACKET dumb CLOSE_BRACKET
	|"EdgeForm" OPEN_BRACKET dumb CLOSE_BRACKET
	|"FaceForm" OPEN_BRACKET dumb CLOSE_BRACKET
	|"Pointsize" OPEN_BRACKET 
					{double d=0;} d=doublething
				 CLOSE_BRACKET 
				 	{			 	
				 	}
	|"Thickness" OPEN_BRACKET 
					{double w=0;} w=doublething
				CLOSE_BRACKET 
					{}
	;

// Optionen ------------------------------------------

protected
optionen
	: COLON 
//
		dumb
//	  OPEN_BRACE 
//	  		( option (COLON option)* )? 
//	  CLOSE_BRACE
	;

protected
option
	: OPEN_BRACE 
	  		( Option (COLON Option)* )? 
	  CLOSE_BRACE
	| OptionPrimitive
	;

protected
optionPrimitive
	:	"PlotRange" 		PFEIL 			SPECIAL
	|	"DisplayFunction"	PFEIL_NACH 		DOLLAR ID
	|	"ColorOutput" 		PFEIL 			SPECIAL
	|	"Axes" 				PFEIL 			SPECIAL
	|	"PlotLabel" 		PFEIL 			SPECIAL
	|	"AxesLabel"			PFEIL 			SPECIAL
	|	"Ticks"				PFEIL 			SPECIAL
	|	"Prolog"			PFEIL 			SPECIAL_SET
	|	"Epilog"			PFEIL 			SPECIAL_SET
	|	"AxesStyle"			PFEIL 			SPECIAL
	|	"Backround"			PFEIL 			SPECIAL
	|	"DefaultColor"		PFEIL 			SPECIAL
	|	"DefaultFond"		PFEIL_NACH 		DOLLAR ID
	|	"AspectRatio"		PFEIL 			SPECIAL
	|	"ViewPoint"			PFEIL 			SPECIAL_SET
	|	"Boxed"				PFEIL 			SPECIAL
	|	"BoxRatios"			PFEIL 			SPECIAL
	|	"Plot3Matrix"		PFEIL 			SPECIAL
	|	"Lighting"			PFEIL 			SPECIAL
	|	"AmbientLight"		PFEIL 			SPECIAL
	|	"LightSources"		PFEIL 			SPECIAL_SET
	|	"ViewCenter"		PFEIL 			SPECIAL
	|	"PlotRegion"		PFEIL 			SPECIAL
	|	"Imagesize"			PFEIL 			SPECIAL
	|	"TextStyle"			PFEIL_NACH 		DOLLAR ID
	|	"FormatType"		PFEIL_NACH 		DOLLAR ID
	|	"ViewVertical"		PFEIL 			SPECIAL_SET
	|	"FaceGrids"			PFEIL 			SPECIAL
	|	"Shading"			PFEIL 			SPECIAL
	|	"RenderAll"			PFEIL 			SPECIAL
	|	"PolygonIntersections"	PFEIL 		SPECIAL
	|	"AxesEdge"			PFEIL 			SPECIAL
	|	"BoxStyle"			PFEIL 			SPECIAL
	|	"SphericalRegion"	PFEIL 			SPECIAL
	;
	
	
integerthing returns[int i]
{i=0;}
	: s:DOUBLETHING {i=Integer.parseInt(s.getText());}
	;
	
doublething returns[double d]
	{d=0; double e=0;}
    : s:DOUBLE_THING {d=Double.parseDouble(s.getText());}
      (s2:EXPONENT_THING {e=Double.parseDouble(s2.getText()); d=d*Math.pow(10,e);})?
    ;
	
protected 
spec
	: OPEN_BRACE 
	  		( Option (COLON Option)* )? 
	  CLOSE_BRACE
	| OptionPrimitive
	;
	
	
protected
dumb
	:(  (~(		  OPEN_BRACE | OPEN_BRACKET | CLOSE_BRACE | CLOSE_BRACKET))+
		(   OPEN_BRACE	 	(dumb)*	CLOSE_BRACE   
		 |	OPEN_BRACKET 	(dumb)*	CLOSE_BRACKET )?  )
	 |  (   OPEN_BRACE	 	(dumb)*	CLOSE_BRACE   
		 |  OPEN_BRACKET 	(dumb)*	CLOSE_BRACKET )
  ;
	
	
// Doubles werden hier geparst!	
// Es gibt nur Doubles!
// Integers koennen aus doubles in Java erkannt, und geparst werden!!!

/** **********************************************************************************
 * The Mathematica Lexer
 ************************************************************************************
*/
class MathematicaLexer extends Lexer;
options {
	k=2;
	testLiterals=false;
}
	/** Terminal Symbols */
OPEN_BRACE:		'{';
CLOSE_BRACE:	'}';
OPEN_BRACKET:	'[';
CLOSE_BRACKET:	']';
LPAREN:			'(';
RPAREN:			')';
BACKS:			'\\';
SLASH:			'/';
COLON:			',';

T1: '!';
T2: '@';
T3: '#';
T4: '$';
T5: '%';
T6: '^';
T7: '&';
T8: '*';
T13: '=';
T15: ':';
T16: ';';
T17: '"';
T19: '?';
T20: '<';
T21: '>';

//protected
//dumb
//	:  (  ~('{' | '}' | '[' | '}' )!)+
//	  (	
//			OPEN_BRACE!	 	(dumb)*	CLOSE_BRACE!	
//		|	OPEN_BRACKET! 	(dumb)*	CLOSE_BRACKET!
//		|
//	  )
//	;

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
	
DOUBLE_THING
	: ('-' | '+'!)?
	  ( 
	  	  (DIGIT)+ ('.' (DIGIT)* )?
		| '.' (DIGIT)+	
	  )
	;
	
EXPONENT_THING
	: "*^"! ('-'|'+'!)? (DIGIT)+
	;
	
protected
DIGIT:
	('0'..'9')
	;
	
STRING:
		'"'! (ESC | ~('"'|'\\'))* '"'!
	;
protected
ESC:
		'\\'! ('\\' | '"')
	;

WS_:
		( ' '
		| '\t'
		| '\f'
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