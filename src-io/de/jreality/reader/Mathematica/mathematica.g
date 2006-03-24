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
}


start returns [SceneGraphComponent r]
{ r = null;	
	globalApp.setName("global");
	root.setAppearance(globalApp);
	root.setName("Mathematica");
}
	:"Graphics3D"
	  OPEN_BRACKET  
	  	object
	  	(optionen)? 
	  CLOSE_BRACKET 
		{ r = root;}
	;

// Objects ---------------------------------------------

protected
object
	:	OPEN_BRACE				// Liste von Graphischen Objekten
			{
			SceneGraphComponent newPart = new SceneGraphComponent();
			newPart.setName("Object");
			SceneGraphComponent oldPart = current;
			current.addChild(newPart);
			current=newPart;
			}
	        (objectList)?
	    CLOSE_BRACE
			{current=oldPart;}
	|	text				// Text an einem Punkt im Raum
	|	cubic				// Wuerfel
	|	polygonBlock		// Abfolge von Polygonen
	|	lineBlock			// Abfolge von Linien
	|	pointBlock			// Abfolge von Punkten
	|	directiveBlock		// Abfolge von graphischen Direktiven
	;

protected
objectList 
	:	object ( COLLON object )*
	;	
	
protected
pointBlock 
{Vector points= new Vector(); double[] v;}
	:
	"Point"
	 OPEN_BRACKET
				{v=new double[3];}
				v=vektor
				{points.add(v);}
	 CLOSE_BRACKET 
	(
	 COLLON "Point"
	 OPEN_BRACKET
				{v=new double [3];}
				v=vektor
				{points.add(v);}
	 CLOSE_BRACKET 
	)*
	{
		PointSetFactory psf = new PointSetFactory();
		double[][] data = new double [points.size()][];
		for(int i=0;i<points.size();i++)
			data[i]=(double [])points.get(i);
		psf.setVertexCount(points.size());
		psf.setVertexCoordinates(data);
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
lineBlock				// liest erst eine, dann alle direkt folgenden Lines ein
{Vector coordinates= new Vector();			// alle Punkte in einer Liste
 Vector line=new Vector();					// alle Punkte einer Linie
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
				}
	 CLOSE_BRACKET 
	(
	 COLLON "Line"	
	 OPEN_BRACKET
				line=lineset 			// das ist ein Vector von double[3]
				{
					lineIndices=new int[line.size()];
					for(int i=0;i<line.size();i++){			// mithilfe von 'count' weiterzaehlen
						coordinates.add(line.get(i));  		//Punkte zu einer Liste machen
				    	lineIndices[i]=i+count;			    // indizirung merken
				    }
			    	count+=line.size();
					linesIndices.add(lineIndices);
				}
	 CLOSE_BRACKET 
	)*
	{
			double[][] data= new double[coordinates.size()][];
			
			for(int i=0;i<coordinates.size();i++){
				data[i]= (double[])coordinates.get(i);
			}
			int[][] indices= new int[linesIndices.size()][];
			
			for(int i=0;i<linesIndices.size();i++){		// Indices als doppelListe von Doubles machen
				indices[i]=(int [])linesIndices.get(i);
			}

			IndexedLineSetFactory lineset=new IndexedLineSetFactory();
			lineset.setLineCount(linesIndices.size());
			lineset.setVertexCount(coordinates.size());
			lineset.setEdgeIndices(indices);
			lineset.setVertexCoordinates(data);
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
lineset returns[Vector v]
{double [] point=new double[3];
v=new Vector();}
		: OPEN_BRACE
		  point=vektor
			{v.add(point);}
		  (COLLON 
			{point= new double[3];}
			point=vektor
			{v.add(point);}
		  )*
		 CLOSE_BRACE
		;

protected 
polygonBlock
{Vector coordinates= new Vector(); 	// alle PunktListen vereint in einer
 Vector poly=new Vector();			// alle Punkte in einem Polygon
 int[] polyIndices;					// alle indices eines Polygons
 Vector polysIndices= new Vector();
 int count=0;
 }
	:"Polygon"
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
				}
	 CLOSE_BRACKET 
	(
	 COLLON "Polygon"
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
				}
	 CLOSE_BRACKET 
	)*
	{
		double [] data= new double[coordinates.size()*3];
		for(int i=0;i<coordinates.size();i++){				// Punkte zum flachen DoubleArray machen
			data[i*3]=((double[])coordinates.get(i))[0];
			data[i*3+1]=((double[])coordinates.get(i))[1];
			data[i*3+2]=((double[])coordinates.get(i))[2];
		}
		int[][] indices= new int[polysIndices.size()][];
		for(int i=0;i<polysIndices.size();i++)		// Indices als doppelListe von Doubles machen
			indices[i]=(int[])polysIndices.get(i);
		IndexedFaceSet faceset=new IndexedFaceSet();
		faceset.setVertexCountAndAttributes(Attribute.COORDINATES,
				new DoubleArrayArray.Inlined(data, 3));
    	faceset.setFaceCountAndAttributes(Attribute.INDICES,
				new IntArrayArray.Array(indices));
    	GeometryUtility.calculateAndSetNormals(faceset);
		SceneGraphComponent geo=new SceneGraphComponent();	// Komponenten erstellen und einhaengen
		current.addChild(geo);
		geo.setName("Faces");
		geo.setGeometry(faceset);
	}
	;

protected 
cubic
	:"Cubic"
	 OPEN_BRACKET 
			{double[] v2=new double [3]; 
			v2[0]=v2[1]=v2[2]=1;
			double[] v=new double[3];
			}
			v=vektor ( COLLON v2=vektor )? 
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
text
{double[] v=new double[3]; String t;}
	:"Text"		OPEN_BRACKET 
					s:STRING COLLON v=vektor 	
				CLOSE_BRACKET 
					{t=s.getText();}
	;
	
protected
vektor returns[double[] res]
{res =new double [3];
double res1,res2,res3;}
	: 	OPEN_BRACE 
			res1=doublething COLLON res2=doublething COLLON res3=doublething 
		CLOSE_BRACE
			{res[0]=res1;
			res[1]=res2;
			res[2]=res3;}
	;
	
	
// Directives ----------------------------------------
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
	  	COLLON
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


	|"SurfaceColor" OPEN_BRACKET
					{ Color diff,spec; double d;int n;}
					diff=color
						{
						app.setAttribute(CommonAttributes.POLYGON_SHADER+"."
								+CommonAttributes.DIFFUSE_COLOR, diff);
						}
					( COLLON spec=color
						{
						 app.setAttribute(CommonAttributes.POLYGON_SHADER+"."
						 		+CommonAttributes.SPECULAR_COLOR, spec);
						}
					  (	COLLON 	d=doublething
					  	{
					  		n=(int) d;
			  				app.setAttribute(CommonAttributes.POLYGON_SHADER+"."
			  					+CommonAttributes.SPECULAR_EXPONENT, n);			  		
					  	}
					  )?
					 )?
					CLOSE_BRACKET 
	|"Thickness" OPEN_BRACKET 
					{double w=0;} w=doublething
				CLOSE_BRACKET 
					{}
	|col=color {	
			 app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, col);
			 app.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, col);
			 }
	;


protected
color returns[Color c]
{c= new Color(100,100,100);
}

		: "RGBColor" OPEN_BRACKET 
					{double r,g,b; r=b=g=0;}
					r=doublething COLLON g=doublething COLLON b=doublething
				CLOSE_BRACKET 
					{
					 float red,green,blue;
					 red=(float) r; green=(float) g; blue=(float) b;
					 c= new Color(red,green,blue);
					 }
		| "Hue" 	OPEN_BRACKET 
					{double h; double s; double b; h=s=b=0.5;}
					h= doublething 
					(COLLON s=doublething COLLON b=doublething )?
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
					cy=doublething COLLON 
					ma=doublething COLLON 
					ye=doublething COLLON 
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

// Optionen ------------------------------------------
protected
optionen
	: COLLON 
//
		dumb
//	  OPEN_BRACE 
//	  		( option (COLLON option)* )? 
//	  CLOSE_BRACE
	;

protected
option
	: OPEN_BRACE 
	  		( Option (COLLON Option)* )? 
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
	  		( Option (COLLON Option)* )? 
	  CLOSE_BRACE
	| OptionPrimitive
	;
	
	
protected
dumb
		:(~(	  OPEN_BRACE
				| OPEN_BRACKET 
		  		| CLOSE_BRACE 
		  		| CLOSE_BRACKET))+
		|	OPEN_BRACE	 	(dumb)*	CLOSE_BRACE   
		|	OPEN_BRACKET 	(dumb)*	CLOSE_BRACKET 
	  ;
	
	
// Doubles werden hier geparst!	
// Integers unten gelext!
// Problem: ein Integer sieht bis zum Ende aus wie ein Double !!!!!
// Moeglichkeit: es gibt nur Doubles Integers koennen aus doubles geparst werden!!!!!

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
DOLLAR:			'$';
HASH:			'#';
AND:			'&';

PFEIL :			"->";
PFEIL_NACH :	":>";
COLLON:			',';

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