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
	
	double[][] borderValue= new double [3][];

// ---------------------------- total Sizes of Scene --------------------------
	boolean gotFirstPoint =false;
	public void resetBorder (double [] v){
		if (gotFirstPoint){ // set Borders for viewed size
			for (int i=0;i<3;i++){
				if (v[i] < borderValue[0][i])
					borderValue[0][i] = v[i];
				if (borderValue[1][i] < v[i])
					borderValue[1][i] = v[i];
			}
		}
		else{
			borderValue[0]=v;
			borderValue[1]=v;
			gotFirstPoint=true;		
		}
	}
// ------------------------------------------------------------------------------
	public static Appearance copyApp(Appearance appOld){// copiert eine Appearance
		Appearance appNew= new Appearance();
		Set s=appOld.getStoredAttributes();
		Iterator ite= s.iterator();
		while (ite.hasNext()){
			String key=(String)ite.next();
			appNew.setAttribute(key,appOld.getAttribute(key));
		}
	 	return appNew;
	}
	
	public double[] getRGBColor(Color c){
	// retuns a array that represents a color (needed for colored faces)
		double[] fl= new double[3];
		fl[0]=c.getRed()/255.0;
		fl[1]=c.getGreen()/255.0;
		fl[2]=c.getBlue()/255.0;
		return fl ;
	}
}


// ------------------------------------------------------------------------------
// -------------------------------- Parser --------------------------------------
// ------------------------------------------------------------------------------
start returns [SceneGraphComponent r]
{ r = null;	
	globalApp.setName("global");
	globalApp.setAttribute(CommonAttributes.VERTEX_DRAW, false);
	globalApp.setAttribute(CommonAttributes.SPHERES_DRAW, false);
	globalApp.setAttribute(CommonAttributes.EDGE_DRAW, false);
	globalApp.setAttribute(CommonAttributes.TUBES_DRAW, false);
	
	root.setAppearance(globalApp);
	root.setName("Mathematica");
}
	:"Graphics3D"
	  OPEN_BRACKET  
	  	( 	  list[plCDefault,fCDefault,globalApp]
	  		| fCDefault=faceThing[fCDefault,globalApp]
	  		| plCDefault=plThing[plCDefault,globalApp]
	  		| globalApp=appThing[globalApp]
	  	)
	  	(optionen)? 
	  CLOSE_BRACKET 
		{ r = root;}
	;

// Objects ---------------------------------------------
protected
list[Color plC, Color fC, Appearance app]
	:	OPEN_BRACE				// Liste von Graphischen Objekten
			{
			SceneGraphComponent newPart = new SceneGraphComponent();
			newPart.setName("Object");
			newPart.setAppearance(app);
			SceneGraphComponent oldPart = current;
			current.addChild(newPart);
			current=newPart;
			}
	        (objectList[plC,fC,app])?
	    CLOSE_BRACE
			{current=oldPart;}
	;
	
protected
objectList [Color plC, Color fC, Appearance app]

	:(
		  list[ plC, fC, app]
		| fC=faceThing[fC, app]
		| plC=plThing[plC, app]
		| app=appThing [app]
	 )
	 ( COLON 
		(	
			  list[ plC, fC, app]
			| fC =faceThing[fC,app]
			| plC=plThing[plC,app]
			| app=appThing[app]
	 	)
	 )*
	;	
	
protected
faceThing [Color fCgiven, Appearance app] returns[Color fC ]
{fC=fCgiven;
 }
	:	cuboid[fC, app]					// Wuerfel 
	|	fC=polygonBlock[fC, app]			// Abfolge von Polygonen (indexed FaceSetFactory)
	|	fC=faceColor
	;


protected
plThing [Color plCgiven, Appearance app] returns[Color plC]
{plC=plCgiven;}
	:	plC= color			// Farbe fuer Punkte Linien und Texte
	|	plC= lineBlock [plC, app]	// Abfolge von Linien (LineSetFactory)
	|	plC= pointBlock [plC, app]	// Abfolge von Punkten (PointSetFactory)
	|	text [plC, app]					// Text an einem Punkt im Raum
	;
	
protected
appThing [Appearance appOld] returns [ Appearance app]
{app=appOld;}
	:	app=directiveBlock[app]			// Abfolge von graphischen Direktiven (erzeugt eine Appearance)
	;


protected
pointBlock [Color plCgiven,Appearance app] returns [Color plC]
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
		Appearance pointApp =copyApp(app);
		pointApp.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		pointApp.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		geo.setAppearance(pointApp);
		geo.setGeometry(psf.getPointSet());
		geo.setName("Points");
		current.addChild(geo);
	}
	;  

protected
lineBlock [Color plCgiven, Appearance app] returns[Color plC]			// liest erst eine, dann alle direkt folgenden Lines ein
{
 plC=plCgiven;								// Punkt und Linienfarbe
 Vector coordinates= new Vector();			// alle Punkte in einer Liste
 Vector line=new Vector();					// alle Punkte einer Linie
 Vector colors= new Vector();				// FarbListe
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
						coordinates.add(line.get(i));  // Punkte zu einer Liste machen
				    	lineIndices[i]=i;			   // indizirung merken
				    }
			    	count=line.size();
					linesIndices.add(lineIndices);
				    colors.add(getRGBColor(plC));
				}
	 CLOSE_BRACKET 
	(
	  COLON
	  (
	    (plC=color)
	   |( "Line"	
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
				}
	     CLOSE_BRACKET )
	  )   
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
			lineset.update();


			// Achtung ist gehackt, weil noch keine Methoden in der LineSetFactory dafuer da : 
			lineset.getIndexedLineSet().setEdgeAttributes(Attribute.COLORS,new DoubleArrayArray.Array( colorData ));
			// 


			SceneGraphComponent geo=new SceneGraphComponent();
			Appearance lineApp =copyApp(app);
			lineApp.setAttribute(CommonAttributes.EDGE_DRAW, true);
			lineApp.setAttribute(CommonAttributes.TUBES_DRAW, true);
			
			geo.setAppearance(lineApp);
			geo.setGeometry(lineset.getIndexedLineSet());
			geo.setName("Lines");
			current.addChild(geo);
		}
	;  


protected 
polygonBlock [Color fCgiven, Appearance app] returns[Color fC]
{fC=fCgiven;
 Vector coordinates= new Vector(); 	// alle PunktListen vereint in einer
 Vector poly=new Vector();			// alle Punkte in einem Polygon
 int[] polyIndices;					// alle indices eines Polygons
 Vector colors= new Vector();
 Vector polysIndices= new Vector();
 int count=0;						// zaehlt die Punkte mit
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
				    colors.add(getRGBColor(fC));
				}
	 CLOSE_BRACKET 
	( COLON
	 (
	   (fC=faceColor )
	  |("Polygon"
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
		Appearance faceApp =copyApp(app);
		geo.setAppearance(faceApp);
		current.addChild(geo);
		geo.setName("Faces");
		geo.setGeometry(faceSet.getIndexedFaceSet());
	}
	;

	
// 	Farben	
protected
faceColor returns[Color fC]
{Color specular; double d; fC= new Color(255,0,0);}
	: "SurfaceColor" OPEN_BRACKET
			fC=color
			( COLON specular=color	(	COLON 	d=doublething )?)?	// ignore !
		CLOSE_BRACKET 
	;

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
{
double [] point =new double[3];
double [] point2=new double[3];
double [] point3=new double [3];
v=new Vector();}
		: OPEN_BRACE
		  point=vektor
		    {
		    v.addElement(point);
		    point3[0]=point[0];// have to save first point seperate and insert later again to first position (dont ask !)
		    point3[1]=point[1];
		    point3[2]=point[2];		    
			}
		  (COLON 
			{point2= new double[3];
			}
			point2=vektor
			{
		    v.addElement(point2);
			}
		  )*
		 CLOSE_BRACE
		 {
		 v.setElementAt(point3,0);
		 }
		;

// ein KoordinatenTripel
protected
vektor returns[double[] res]
{res =new double [3];
double res1,res2,res3;}
	: 	OPEN_BRACE 
			res1=doublething COLON res2=doublething COLON res3=doublething 
		CLOSE_BRACE
			{
			 res[0]=res1;
			 res[1]=res2;
			 res[2]=res3;
			 resetBorder (res);
			}
	;

protected	// das ist dasselbe wie vektor, wird aber nicht in die Borderberechnung eingefuegt
vektordata returns[double[] res]
{res =new double [3];
double res1,res2,res3;}
	: 	OPEN_BRACE 
			res1=doublething COLON res2=doublething COLON res3=doublething 
		CLOSE_BRACE
			{
			 res[0]=res1;
			 res[1]=res2;
			 res[2]=res3;
			}
	;


// ----------------------------------------------- neu ---------------------------------------------
// -------------------------------------------------------------------------------------------------
// ----------------------------------------------- alt ---------------------------------------------


protected 
cuboid [ Color fC,Appearance app]
	:"Cuboid"
	 OPEN_BRACKET 
			{double[] v2=new double [3]; 
			v2[0]=v2[1]=v2[2]=1;
			double[] v=new double[3];
			}
			v=vektor ( COLON v2=vektordata )? 
	 CLOSE_BRACKET 
			{
			 SceneGraphComponent geo=new SceneGraphComponent();
			 current.addChild(geo);
			 geo.setGeometry(Primitives.cube());
	 		 geo.setName("Cuboid");
	 		 Appearance cubicApp =copyApp(app);
	 		 cubicApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, fC);
			 geo.setAppearance(cubicApp);
			 MatrixBuilder.euclidean().scale(v2[0],v2[1],v2[2])
			    .translate(v[0],v[1],v[2]).assignTo(geo);
 			}
 	;
 	
protected
text [ Color plC,Appearance app]
{double[] v=new double[3]; String t;}
	:"Text"		OPEN_BRACKET 
					s:STRING COLON v=vektordata 	
				CLOSE_BRACKET 
					{t=s.getText();}
	;
	
// Directives ---------------------------------------- 

protected 
directiveBlock[Appearance appOld] returns [ Appearance app]
	{
//	 SceneGraphComponent dir=new SceneGraphComponent();
//	 dir.setName("Directive");
//	 current.addChild(dir);
	 app =appOld;
	}
	: app=directive[app]
	  (
	  	COLON
	  	app=directive[app]
	  )*
	{
	 //current=dir;
	 //dir.setAppearance(app);
	}
	;

protected 
directive[Appearance appGiven] returns [Appearance app]
{app = copyApp(appGiven); 
Color col;}
	:"AbsoluteDashing" OPEN_BRACKET  dumb CLOSE_BRACKET 
	|"AbsolutePointSize" 
				OPEN_BRACKET
					{double d=0;} d=integerthing
					{
					app.setAttribute(CommonAttributes.POINT_RADIUS,d/40);
					app.setAttribute(CommonAttributes.POINT_SIZE,d);
					}
				CLOSE_BRACKET 
	|"AbsoluteThickness"
				OPEN_BRACKET  
					{double d=0;} d=integerthing
				CLOSE_BRACKET 
					{
					app.setAttribute(CommonAttributes.TUBE_RADIUS,d/40);
					app.setAttribute(CommonAttributes.LINE_WIDTH,d);
					}
	|"Dashing" OPEN_BRACKET dumb CLOSE_BRACKET	// only for 2 Dimensional
	|"EdgeForm" OPEN_BRACKET 
				{Color c= Color.MAGENTA;}
					c=color
				{
			 	app.setAttribute(CommonAttributes.EDGE_DRAW, true);
			 	app.setAttribute(CommonAttributes.TUBES_DRAW, true);
			 	app.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c);
        		}
				CLOSE_BRACKET 
	|"FaceForm" OPEN_BRACKET dumb CLOSE_BRACKET // kann keine Flaeche oben anders faerben als unten
	|"PointSize" OPEN_BRACKET // groese eines Punktes als Anteil an der Graphengroese
					{double d=0;} d=doublething
				 CLOSE_BRACKET 
				 	{// schlecht: brauche die Groese des Graphen in der Mitte der Auswertung
				 	}
	|"Thickness" OPEN_BRACKET // siehe Pointsize
					{double w=0;} w=doublething
				CLOSE_BRACKET 
				 	{// schlecht: brauche die Groese des Graphen in der Mitte der Auswertung
				 	}
					
	;

// Optionen ------------------------------------------

protected
optionen
	: COLON 
//		dumb
	  OPEN_BRACE 
	  		( option (COLON option)* )? 
	  CLOSE_BRACE
	;

protected
option
	: OPEN_BRACE 
	  		( Option (COLON Option)* )? 
	  CLOSE_BRACE
	| optionPrimitive
	;

protected
optionPrimitive
	:	"PlotRange" 			MINUS LARGER	// Anpassen der Groesse
//	
								egal
//	
//				( "Automatic" 	
//				   {double[][] v=borderValue;
//				    double eps=0.01;
//					MatrixBuilder.euclidean()
//					    .scale(  1/((v[1][0]-v[0][0])+eps),1/((v[1][1]-v[0][1])+eps),1/((v[1][2]-v[0][2])+eps))
//						.translate((v[1][0]-v[0][0])/2,(v[1][1]-v[0][1])/2,(v[1][2]-v[0][2])/2)
//						.assignTo(root);
//					}
//				 | "ALL" // wie Automatic
//				 | OPEN_BRACKET 
//				 	{double [] v;}
//				    v=vektor
//				    {
//				      MatrixBuilder.euclidean().scale(1/v[0],1/v[1],1/v[2]).assignTo(root);
//			  		}
//				   CLOSE_BRACKET		)
	|	"Boxed"					MINUS LARGER 			egal
	|	"Axes" 					MINUS LARGER 			egal
	|	"PlotLabel" 			MINUS LARGER			egal
	|	"AxesLabel"				MINUS LARGER			egal
	|	"AmbientLight"			MINUS LARGER			egal
	|	"DefaultColor"			MINUS LARGER			egal // Problem:kann nicht mehr die Farbe in einem Block aendern

	|	"DisplayFunction"		(":>" 		DOLLAR ID | MINUS LARGER	egal)
	|	"ColorOutput" 			MINUS LARGER			egal
	|	"Ticks"					MINUS LARGER			egal
	|	"Prolog"				MINUS LARGER			egal
	|	"Epilog"				MINUS LARGER			egal
	|	"AxesStyle"				MINUS LARGER			egal
	|	"Background"				MINUS LARGER			egal
	|	"DefaultFont"			(":>" 		DOLLAR ID | MINUS LARGER	egal)
	|	"AspectRatio"			MINUS LARGER			egal
	|	"ViewPoint"				MINUS LARGER			egal
	|	"BoxRatios"				MINUS LARGER			egal
	|	"Plot3Matrix"			MINUS LARGER			egal
	|	"Lighting"				MINUS LARGER			egal
	|	"LightSources"			MINUS LARGER			egal
	|	"ViewCenter"			MINUS LARGER			egal
	|	"PlotRegion"			MINUS LARGER			egal
	|	"ImageSize"				MINUS LARGER			egal
	|	"TextStyle"				(":>" 		DOLLAR ID | MINUS LARGER	egal)
	|	"FormatType"			(":>" 		DOLLAR ID | MINUS LARGER	egal)
	|	"ViewVertical"			MINUS LARGER			egal
	|	"FaceGrids"				MINUS LARGER			egal
	|	"Shading"				MINUS LARGER			egal
	|	"RenderAll"				MINUS LARGER			egal
	|	"PolygonIntersections"	MINUS LARGER			egal
	|	"AxesEdge"				MINUS LARGER			egal
	|	"BoxStyle"				MINUS LARGER			egal
	|	"SphericalRegion"		MINUS LARGER			egal
	;


protected 		// ueberliest den Rest bis zur naechsten Option. Laest das Komma stehen!
egal
	: (~(  COLON | OPEN_BRACE | OPEN_BRACKET | CLOSE_BRACE | LPAREN ))*
		(   OPEN_BRACE	 	(dumb)*		CLOSE_BRACE   		(egal)?
		 |	OPEN_BRACKET 	(dumb)*		CLOSE_BRACKET   	(egal)?
		 |  LPAREN			(dumb)*		RPAREN				(egal)?  	)?
  ;
	
integerthing returns[int i]
{i=0;String sig="";}
	: (PLUS | MINUS {sig="-";} )?
	  s:INTEGER_THING {i=Integer.parseInt(sig + s.getText());}
	;
	
doublething returns[double d]
	{d=0; double e=0; String sig="";}
    : (PLUS | MINUS {sig="-";} )?
    ( s:INTEGER_THING 
    		{d=Double.parseDouble(sig + s.getText());}
      (DOT
      	(s2:INTEGER_THING 
    		{ d=Double.parseDouble(sig + s.getText()+ "." + s2.getText());}
         )?
      )?
	| DOT s3:INTEGER_THING 
			{d=Double.parseDouble(sig + "0." + s3.getText());}
    )
    (e=exponent_thing {d=d*Math.pow(10,e);})?
    ;
protected 
exponent_thing returns[int e]
{e=0; String sig="";}
    : STAR HAT 
    (PLUS | MINUS {sig="-";} )?
     s:INTEGER_THING
     	{e=Integer.parseInt(sig + s.getText() );}
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
	:(  (~(	LPAREN | RPAREN | OPEN_BRACE | OPEN_BRACKET | CLOSE_BRACE | CLOSE_BRACKET))+
		(   OPEN_BRACE	 	(dumb)*	CLOSE_BRACE   
		 |	OPEN_BRACKET 	(dumb)*	CLOSE_BRACKET 
		 |  LPAREN			(dumb)*		RPAREN	)?  )
	 |  (   OPEN_BRACE	 	(dumb)*	CLOSE_BRACE   
		 |  OPEN_BRACKET 	(dumb)*	CLOSE_BRACKET
		 |  LPAREN			(dumb)*		RPAREN  )
  ;
	
	
// Doubles werden hier geparst!	
// Es gibt nur Nummern
// Integers koennen aus doubles in Java erkannt, und geparst werden!!!

/** **********************************************************************************
 * The Mathematica Lexer
 ************************************************************************************
*/
class MathematicaLexer extends Lexer;
options {
	charVocabulary = '\3'..'\377';
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
DOLLAR: 		'$';
MINUS:			'-';
PLUS:			'+';
LARGER:			'>';
SMALER:			'<';
DOT:			'.';
HAT:			'^';
STAR:			'*';

T1: '!';
T2: '@';
T3: '#';
T5: '%';
T7: '&';
T13: '=';
T15: ':';
T16: ';';
T17: '"';
T19: '?';

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
	
INTEGER_THING
	: (DIGIT)+
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