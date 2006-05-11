//**************************************************
// * Mathematica Parser
// */

// wandelt Graphic3D- Objekte die aus Mathematica mittels 
// 			<Graphics3D -Object> >> <Pfad>\ <fileName.m>
// herausgeschrieben wurden in einen SceneGraphen von JReality um.

// Die Daten haben im file folgende Form(sind uebrigens direkter Mathematica-Text):
// Graphics3D[ {} , {} ]
// in der ersten Klammer steht ein "{..}"-Klammer Baum von gemischten Listen aus 
// graphischen Objekten und graphischen Directiven( wie Farben ect.)
// Bsp: Graphics3D[ 		{o1,o2,d1,{d2,o3,{o4},d3,o5}} 			,{} ]   
//					(d1-d3 Directiven , o1-o5 graphische Objekte)
// in der 2. Klammer steht eine Liste von Optionen die die ganze Scene beinflussen.
// Bsp: Graphics3D[ 		{o1,o2,d1,{d2,o3,{o4},d3,o5}} 			,{Op1,Opt2,..} ]
// 			Von dieser Beschreibung sind manche Dinge Optional !

// Vorgehen: 
// erste Klammer:
//		Klammern werden zu SceneGraphComponents die den Inhalt als Unterbaeume tragen.
// 		die graphischen Objekte werden je zu einer SceneGr.C. mit entsprechender Geometrie
//			manche Objekte werden hierbei zusammengefast*
//		die graphischen Directiven werden in der jeweils betroffenen Geometrie gesetzt(Farben)
//			oder als Appearance in den zur Geom. gehoerenden Knoten gesetzt.(Punktgroesse ect.)
//					manche Directiven werden ueberlesen!!!
// zweite Klammer:
//		die meissten Optionen werden ueberlesen!!!
//		Ansonsten werden sie den Root Knoten beeinflussen.

// * Polygone die in einer Liste bis auf Flaechen-Farb-Direktiven untereinander stehen,
//	 	werden zu einer indexedFaceSet
//   Linien die in einer Liste bis auf einfache-Farb-Direktiven untereinander stehen,
//	 	werden zu einer indexedLineSet
//   Punkte die in einer Liste bis auf einfache-Farb-Direktiven untereinander stehen,
//	 	werden zu einem PointSet
// 	 TexteMarken werden zu gelabelten EINELEMENTIGEN Pointsets.(sie werden nicht verbunden)

//   desweiteren werden doppelte Punkte aus Line- und Face- Sets aussortiert
// 		das ermoeglicht u.a. smoothshading bei Flaechen aus getrennt angegebenen Polygonen(das ist ueblich)

//   Farben werden je nach Bedarf als Farbliste in Line-, Point-, und Face- Sets eingebunden.
//   	Vorzugsweise aber als Farbe in der Appearance des Unterknotens.

// Internes:
// ueberall die aktuelle Appearance, FlaechenFarbe(fC), und Punkt/LinienFarbe(plC) durchreichen

// TO DO:
// viele Optionen sind noch unbehandelt (siehe dort: "optionPrimitives")
// Standard Licheter und Camera werden nicht uebernommen 
// 		(der Scenegraph hat keine Lichter oder Camera)
// ein paar Directiven werden auch ignoriert (siehe dort: "directive")
// die Root hat Linien und Punkte ausgeschaltet. 
//		Sie werden bei den entsprechenden Knoten der Geometrieen wieder eingeschaltet.
// Auch wenn Appearances wie Directiven fuer ganze gruppen gelten koennen,
//		werden sie erst im Knoten der Geometrie eingesetzt.

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
	
	SceneGraphComponent current = root;		// aktuell zu erweiternder Knoten 
	CoordinateSystemFactory box;
	Appearance globalApp =new Appearance();	// App. der root
	Color plCDefault= new Color(255,0,0);	// default- Punkt und Linienfarbe
	Color fCDefault = new Color(0,255,0);	// default- Flaechenfarbe
	

// ---------------------------- total Sizes of Scene --------------------------
	double[][] borderValue= new double [3][]; // maximale Werte der Scenenkoordinaten in x-,y- und z-Richtung

	boolean gotFirstPoint =false;
	public void resetBorder (double [] v){	  // erweitert den borderValue moeglicherweise durch diesen Punkt
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
	public static Appearance copyApp(Appearance appOld){
		// kopiert eine Appearance um doppelt-Verzeigerung zu vermeiden
		Appearance appNew= new Appearance();
		Set s=appOld.getStoredAttributes();
		Iterator ite= s.iterator();
		while (ite.hasNext()){
			String key=(String)ite.next();
			appNew.setAttribute(key,appOld.getAttribute(key));
		}
	 	return appNew;
	}
	public static Color copyColor(Color c){
		// kopiert eine Farbe um doppelt-Verzeigerung zu vermeiden
		return new Color(c.getRed(),c.getGreen(),c.getBlue()) ;
	}
	
	public double[] getRGBColor(Color c){
	// retuns a array that represents a color (needed for color-Arrays as double[][])
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
	root.setAppearance(globalApp);
	root.setName("Mathematica");
}
	:"Graphics3D"
	  OPEN_BRACKET  
	  	( 	  firstList[plCDefault,fCDefault,globalApp]	// bei nur einem Objekt sind Klammern optional
	  		| fCDefault=faceThing[fCDefault,globalApp]	// ein Flaechen-beeinflussendes Object
	  		| plCDefault=plThing[plCDefault,globalApp]  // ein Punkt/Linien-beeinflussendes Object
	  		| globalApp=appThing[globalApp]				// ein App.-beeinflussendes Object
	  	)
	  	{
	  	// Box und Axen ermoeglichen:
	  	box=new CoordinateSystemFactory(root);
		}
	  	(optionen)? 									// Die in der 2."{}"-Klammer folgenden Optionen sind optional
	  CLOSE_BRACKET 
		{
		globalApp.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		globalApp.setAttribute(CommonAttributes.SPHERES_DRAW, false);
		globalApp.setAttribute(CommonAttributes.EDGE_DRAW, false);
		globalApp.setAttribute(CommonAttributes.TUBES_DRAW, false);
		r = root;}
	;

// ---------------------------------- 3D Objects ---------------------------------------------
protected
firstList[Color plC, Color fC, Appearance app]
// firstList ist wie list, nur das hier kein neuer Sc.Gr.Co. erstellt wird da wir ja schon die Root haben
// (wird nur am Anfang benutzt!)
	:	OPEN_BRACE
			(objectList[plC,fC,app])?	// eine Abfolge graphischer Objecte
	    CLOSE_BRACE
	;

protected
list[Color plC, Color fC, Appearance app]
{Appearance app2=copyApp(app);}
	// eine Klammer mit mglw. einer Abfolge von 3d-Objekten
	:	OPEN_BRACE						
			{						
			// neuen Knoten erstellen der die Listenelemente haelt, 
			SceneGraphComponent newPart = new SceneGraphComponent();
			newPart.setName("Object");
//			newPart.setAppearance(app2);
			SceneGraphComponent oldPart = current;
			current.addChild(newPart);
			current=newPart;
			}
	        (objectList[plC,fC,app2])?	// das innere der Klammer einhaengen
	    CLOSE_BRACE
			{current=oldPart;}
	;
	
protected
objectList [Color plC, Color fC, Appearance app]
// abarbeiten einer Abfolge von 3d-Objecten(und Directiven)
{Appearance app2=copyApp(app);}
	:(
		  list[ plC, fC, app2]		// Listen koennen Listen enthalten
		| fC=faceThing[fC, app2]	// FlaechenElemente
		| plC=plThing[plC, app2]	// Punkt/Linien Elemente
		| app2=appThing [app2]		// Directiven die Appearance beeinflussen sollen
	 )
	 ( COLON 
		(	
			  list[ plC, fC, app2]
			| fC =faceThing[fC,app2]
			| plC=plThing[plC,app2]
			| app2=appThing[app2]
	 	)
	 )*
	;	
	
protected
faceThing [Color fCgiven, Appearance app] returns[Color fC ]
{fC=copyColor(fCgiven);
 Appearance app2=copyApp(app);}
	:	cuboid[fC, app2]					// Wuerfel 
	|	fC=polygonBlock[fC, app2]			// Abfolge von Polygonen (IndexedFaceSet)
	|	fC=faceColor						// Farbe die Flaechen beeinflusst
	;


protected
plThing [Color plCgiven, Appearance app] returns[Color plC]
{plC=copyColor(plCgiven);
 Appearance app2=copyApp(app);}
	:	plC= color							// Farbe fuer folgende Punkte, Linien und Texte
	|	plC= lineBlock [plC, app2]			// Abfolge von Linien (IndexedLineSet)
	|	plC= pointBlock [plC, app2]			// Abfolge von Punkten (PointSet)
	|	text [plC, app2]					// Text an einem Punkt im Raum (einelementiges labeld PointSet)
	;
	
protected
appThing [Appearance appOld] returns [ Appearance app]
{app=copyApp(appOld);}
	:	app=directiveBlock[app]			// Abfolge von graphischen Direktiven (aendert eine Appearance)
	;
	
// ----------------------------- Graphic Primitives ------------------------------------------------------------
protected 
cuboid [ Color fC,Appearance app]
	// ein achsenparalleler Wuerfel, gegeben durch Zentrum(Kantenlaenge 1) oder zusaetslich durch leangen
	:"Cuboid"
	 OPEN_BRACKET 
			{double[] v2=new double [3]; 
			v2[0]=v2[1]=v2[2]=1;
			double[] v=new double[3];
			}
			v=vektor ( COLON v2=vektordata )? 
	 CLOSE_BRACKET 
			{
			// realisiert durch gestreckten kantenlosen Einheitswuerfel
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
// ein Stueck Text im Raum 
	:"Text"		OPEN_BRACKET 
					s:STRING COLON v=vektordata 	
				CLOSE_BRACKET 
					{
					 // realisiert durch ein einelementiges Pointset mit einem LabelPunkt 
					 // mit verschwindend kleinem Radius!
					 t=s.getText();
					 PointSetFactory psf = new PointSetFactory();
					 double [][] data = new double [1][];
					 data[0]=v;
					 psf.setVertexCount(1);
					 psf.setVertexCoordinates(data);
					 String [] labs= new String[1];
					 labs[0]=s.getText();
					 psf.setVertexLabels(labs);
					 psf.update();
		
					 SceneGraphComponent geo=new SceneGraphComponent();
					 Appearance pointApp =copyApp(app);
					 pointApp.setAttribute(CommonAttributes.SPHERES_DRAW, true);
					 pointApp.setAttribute(CommonAttributes.VERTEX_DRAW, true);
					 pointApp.setAttribute(CommonAttributes.POINT_RADIUS, 0.0001);
				
					 geo.setAppearance(pointApp);
					 geo.setGeometry(psf.getPointSet());
					 geo.setName("Label");
					 current.addChild(geo);
					}
	;

protected
pointBlock [Color plCgiven,Appearance app] returns [Color plC]
// eine Abfolge von Punkten wird zu einer PointSet
// je nach dem ob Farben ZWISCHEN den Punkten stehen wird eine 
// Farbliste eingelesen, oder die App. eingefaerbt
{Vector points= new Vector(); 
 double[] v;
 Vector colors= new Vector();
 plC=copyColor(plCgiven);
 boolean colorFlag=false;
 boolean colorNeeded =false;
}
	:
	( "Point"
	   OPEN_BRACKET
				{v=new double[3];}
				v=vektor
				{points.add(v);
				 colors.add(plC);}
	   CLOSE_BRACKET 
	)
	(
	COLON
	(
	
	   plC=color {colorFlag=true;}
	 |( "Point"  {if (colorFlag) colorNeeded= true;}
	   OPEN_BRACKET
				{v=new double [3];}
				v=vektor
				{points.add(v);
				 colors.add(plC);}
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
			colorData[i]=getRGBColor((Color)colors.get(i));
		}
		psf.setVertexCount(points.size());
		psf.setVertexCoordinates(data);
		Appearance pointApp =copyApp(app);
	    if (colorNeeded) 			// brauchen wir eine Farbliste?
			psf.setVertexColors(colorData);
		else
			pointApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, colors.get(0));
		psf.update();
		SceneGraphComponent geo=new SceneGraphComponent();
		pointApp.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		pointApp.setAttribute(CommonAttributes.SPHERES_DRAW, true);
		geo.setAppearance(pointApp);
		geo.setGeometry(psf.getPointSet());
		geo.setName("Points");
		current.addChild(geo);
	}
	;  

protected
lineBlock [Color plCgiven, Appearance app] returns[Color plC]
// liest eine Abfolge von Linien in eine IndexedLineSet.
// schmeist doppelte Punkte durch umindizierung raus.
// Farben wie bei pointBlock.
{
 plC=copyColor(plCgiven);					// Punkt/Linienfarbe
 Vector coordinates= new Vector();			// alle Punkte in einer Liste
 Vector line=new Vector();					// alle Punkte einer Linie
 Vector colors= new Vector();				// FarbListe
 int count=0;								// Anzahl aller bisher gesammelten Punkte
 int[] lineIndices;							// liste aller Indices einer Linie
 Vector linesIndices= new Vector();			// Liste aller IndiceeListen
 boolean colorFlag=false;
 boolean colorNeeded =false;
 }
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
				    colors.add(plC);
				}
	 CLOSE_BRACKET 
	(
	  COLON
	  (
	    (plC=color  {colorFlag=true;} )
	   |( "Line"
	     OPEN_BRACKET
				line=lineset 			// das ist ein Vector von double[3]
				{if (colorFlag) colorNeeded= true;}
				{
					lineIndices=new int[line.size()];
					for(int i=0;i<line.size();i++){			// mithilfe von 'count' weiterzaehlen
						coordinates.add(line.get(i));  		// Punkte zu einer Liste machen
				    	lineIndices[i]=i+count;			    // indizirung merken
				    }
			    	count+=line.size();
					linesIndices.add(lineIndices);
					colors.add(plC);
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
			//System.out.println("Farben der Linien:");
			for(int i=0;i<linesIndices.size();i++){		// Indices als doppelListe von Doubles machen
				indices[i]=(int [])linesIndices.get(i);
				colorData[i]=getRGBColor((Color)colors.get(i));		
			}
			// -- verschmelzen der Punkte
			Vector temp= FaceMelt.meltCoords(data,indices);
			data= (double[][]) temp.elementAt(0);
			indices= (int[][]) temp.elementAt(1);
			count=data.length;
			// -- verschmelzen der Punkte Ende
			IndexedLineSetFactory lineset=new IndexedLineSetFactory();
			lineset.setLineCount(linesIndices.size());
			lineset.setVertexCount(count);
			lineset.setEdgeIndices(indices);
			lineset.setVertexCoordinates(data);
			lineset.update();
			Appearance lineApp =copyApp(app);
		    if (colorNeeded){
					// Achtung ist gehackt, weil noch keine Methoden in der LineSetFactory dafuer da : 
					lineset.getIndexedLineSet().setEdgeAttributes(Attribute.COLORS,new DoubleArrayArray.Array( colorData ));

					//lineset.setEdgeColors(colorData); //das funktioniert noch nicht richtig
			}
			else
				lineApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, colors.get(0));
			lineset.update();
			// for (int i=0;i<linesIndices.size();i++)
			// System.out.println(colorData[i][0]+"|"+colorData[i][1]+"|"+colorData[i][2]);
			SceneGraphComponent geo=new SceneGraphComponent();
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
// liest eine Abfolge von Polygonen 
// schmeist doppelte Punkte durch umindizierung raus
// Farben wie pointBlock und lineBlock
{fC=copyColor(fCgiven);
 Vector coordinates= new Vector(); 	// alle PunktListen vereint in einer
 Vector poly=new Vector();			// alle Punkte in einem Polygon
 int[] polyIndices;					// alle indices eines Polygons
 Vector colors= new Vector();		// FarbListe
 Vector polysIndices= new Vector();	// IndexListen-Liste
 int count=0;						// zaehlt die Punkte mit
 boolean colorFlag=false;
 boolean colorNeeded =false;
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
				    colors.add(fC);
				}
	 CLOSE_BRACKET 
	( COLON
	 (
	   (fC=faceColor {colorFlag=true;})
	  |("Polygon"
	    OPEN_BRACKET
				poly=lineset 			// das ist ein Vector von double[3]
				{if (colorFlag) colorNeeded= true;}
				{
					polyIndices=new int[poly.size()+1];
					for(int i=0;i<poly.size();i++){
						coordinates.add(poly.get(i));  //Punkte zu einer Liste machen
				    	polyIndices[i]=i+count;			   // indizirung merken
				    }
				    polyIndices[poly.size()]=count;
			    	count+=poly.size();
					polysIndices.add(polyIndices);
					colors.add(fC);
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
			colorData[i]=getRGBColor((Color)colors.get(i));
		}
		//	melt:	  if it dos not work simply take it out
			Vector temp= FaceMelt.meltCoords(data,indices);
			data= (double[][]) temp.elementAt(0);
			indices= (int[][]) temp.elementAt(1);
			count=data.length;
		//  end melt
		IndexedFaceSetFactory faceSet = new IndexedFaceSetFactory();
		faceSet.setVertexCount(count);
		faceSet.setFaceCount(polysIndices.size());
		faceSet.setFaceIndices(indices);
		faceSet.setVertexCoordinates(data);
		Appearance faceApp =copyApp(app);
		//faceApp.setAttribute(CommonAttributes.SMOOTH_SHADING, true);// smoth ist eh default
	    if (colorNeeded)
			faceSet.setFaceColors(colorData);
		else
			faceApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, colors.get(0));
		faceSet.setGenerateFaceNormals(true);
		faceSet.setGenerateVertexNormals(true);
		faceSet.setGenerateEdgesFromFaces(true);
		faceSet.update();
		SceneGraphComponent geo=new SceneGraphComponent();	// Komponenten erstellen und einhaengen
		geo.setAppearance(faceApp);
		current.addChild(geo);
		geo.setName("Faces");
		geo.setGeometry(faceSet.getIndexedFaceSet());
	}
	;
	
// -------------------------------------------------- Farben --------------------------------------------
protected
faceColor returns[Color fC]
// Farben fuer Flaechen sind in 'SurfaceColor[]' gekapselt
{Color specular; double d; fC= new Color(255,0,0);}
	: "SurfaceColor" OPEN_BRACKET
			fC=color
			( COLON specular=color	(	COLON 	d=doublething )?)?	// ignore !
		CLOSE_BRACKET 
	;

protected
color returns[Color c]
// liest eine Farbe 
// Farben haben verschiedene Darstellungen
{c= new Color(0,255,0);}
		: "RGBColor" OPEN_BRACKET  // Red-Green-Blue
					{double r,g,b; r=b=g=0;}
					r=doublething COLON g=doublething COLON b=doublething
				CLOSE_BRACKET 
					{
					 float red,green,blue;
					 red=(float) r; green=(float) g; blue=(float) b;
					 c= new Color(red,green,blue);
					 }
		| "Hue" 	OPEN_BRACKET // Hue-Saturation-Brightness
					{double h; double s; double b; h=s=b=0.5;}
					h= doublething 
					(COLON s=doublething COLON b=doublething )?
				CLOSE_BRACKET 
					{
					 float hue,sat,bri;
					 hue=(float) h; sat=(float) s; bri=(float) b;
					 c = Color.getHSBColor(hue,sat,bri);
					}
		| "GrayLevel" OPEN_BRACKET // Schwarz-Weiss
					{double gr=0;} gr=doublething 
				CLOSE_BRACKET 
					{
					float grey=(float) gr;
					c= new Color(grey,grey,grey);
					}
		| "CMYKColor" OPEN_BRACKET // Cyan-Magenta-Yellow-black
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
// -------------------------------------------- Daten ------------------------------------
protected
lineset returns[Vector v]
// Koordinaten in einer Liste zu Vector(double[3])
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

protected
vektor returns[double[] res]
// ein KoordinatenTripel(Punkt) zu double[3]
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

protected	
vektordata returns[double[] res]
// das gleiche wie vektor, beeinflusst aber nicht die Borderberechnung(Scenengroese)
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

// --------------------------------graphische Directiven ---------------------------------------- 

protected 
directiveBlock[Appearance appOld] returns [ Appearance app]
// eine Abfolge von Direktiven die die Appearance beeinflussen(keine Farben)
{app =copyApp(appOld);}
	: app=directive[app]
	  (
	  	COLON
	  	app=directive[app]
	  )*
	;

protected 
directive[Appearance appGiven] returns [Appearance app]
// Direktiven die die Appearance beeinflussen(keine Farben)
// Bemerkung: Der Aufruf 'dumb' ignoriert alles in der Klammer.
{app = copyApp(appGiven); 
Color col;}
	:"EdgeForm" OPEN_BRACKET 							
		// Zeichnet die Randlinien eines Polygons in der angegebenen Farbe
		{Color c= Color.BLACK;}
			(c=color)?
		{
	 	app.setAttribute(CommonAttributes.EDGE_DRAW, true);
	 	app.setAttribute(CommonAttributes.TUBES_DRAW, true);
	 	app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c);
   		}
				CLOSE_BRACKET 
	|"AbsolutePointSize" 								
		// Dicke der Punkte
		OPEN_BRACKET
			{double d=0;} d=integerthing
			{
			app.setAttribute(CommonAttributes.POINT_RADIUS,d/40);
			app.setAttribute(CommonAttributes.POINT_SIZE,d);
			}
			CLOSE_BRACKET 
	|"AbsoluteThickness"	
		// Dicke der Linien
		OPEN_BRACKET  
			{double d=0;} d=integerthing
		CLOSE_BRACKET 
			{
			app.setAttribute(CommonAttributes.TUBE_RADIUS,d/40);
			app.setAttribute(CommonAttributes.LINE_WIDTH,d);
			}
// -- ab hier nicht behandelte directives
	|"Dashing" OPEN_BRACKET dumb CLOSE_BRACKET
		// 2D: strichelt Linien (unnoetig da wir im 3d sind)
	|"FaceForm" OPEN_BRACKET dumb CLOSE_BRACKET
		// faerbt Flaechen 2-seitig verschieden (koennen wir garnicht)
	|"PointSize" OPEN_BRACKET
		// Groese eines Punktes als Anteil an der Graphengroese (zu aufwendig)
					{double d=0;} d=doublething
				 CLOSE_BRACKET 
				 	{// schlecht: brauche die Groese des Graphen in der Mitte der Auswertung
				 	}
	|"Thickness" OPEN_BRACKET 
		// Dicke einer Linie als Anteil an der Graphengroese (zu aufwendig)
					{double w=0;} w=doublething
				CLOSE_BRACKET 
				 	{// schlecht: brauche die Groese des Graphen in der Mitte der Auswertung
				 	}
	|"AbsoluteDashing" OPEN_BRACKET  dumb CLOSE_BRACKET 
		// 3D: strichelt Lienien (sehr aufwendig)
	;

// ----------------------------------------------- Optionen ------------------------------------------

protected
optionen
// liest einen Block bestehend aus einer Abfolge von Optionen
	: COLON 
	  OPEN_BRACE 
	  		( option (COLON option)* )? 
	  CLOSE_BRACE
	;

protected
option
// Optionen beeinflussen die gesammte Scene
	: OPEN_BRACE 			// Block von Optionen
	  		( Option (COLON Option)* )? 
	  CLOSE_BRACE
	| optionPrimitive		// eine einfache Option
	;

protected
optionPrimitive
// einfache Optionen
// die meisten werden schlicht ignoriert
// Bemerkung: egal ueberspring alles bis zur naechsten Option bzw dem Ende des Blocks
// Bemerkung: man kann offensichtlich auch auf Quellen verweisen (option :> $Identifier)
//		ich habe aber keine Ahnung was das bedeutet. ignoriere es also
// -- geht bereits:
	:	"Boxed"			(		MINUS LARGER 
			// eine Box um die Scene
						 (	 "True"	{box.displayBox();}	
							|"False"{box.hideBox();}
						)| DDOT LARGER	egal)
	|	"Axes" 			(		MINUS LARGER
			// Achsen an der Scene
						 (	 "True"	{box.displayAxes();}	
							|"False" {box.hideAxes();}
							|"Automatic"{}
						)| DDOT LARGER	egal)

// -- moeglich/Sinnvoll:
	|	"AxesLabel"				(DDOT LARGER	egal | MINUS LARGER	egal)
			// 3D: Label an den Achsen (waere moeglich)
	|	"Prolog"				(DDOT LARGER	egal | MINUS LARGER	egal)
			// irgendwelche graphischen objekte die zuerst berechnet werden
			// (gut koennte ich auch noch Parsen und extra an die Root haengen)
	|	"Epilog"				(DDOT LARGER	egal | MINUS LARGER	egal)
			// irgendwelche graphischen objekte die zuletzt berechnet werden
			// (gut koennte ich auch noch Parsen und extra an die Root haengen)
			
	|	"ViewPoint"				(DDOT LARGER	egal | MINUS LARGER	egal)
			// CameraFokus (sollen wir das ueberhaupt uebernehmen)
	|	"ViewCenter"			(DDOT LARGER	egal | MINUS LARGER	egal)
			//	???
	|	"FaceGrids"				(DDOT LARGER	egal | MINUS LARGER	egal)
			//	3d Linien-Gitter in der bounding-box
	|	"Ticks"					(DDOT LARGER	egal | MINUS LARGER	egal)
			// Ticks sind die Markierungen an den Achsen.
			// Farbe, Dicke, Abstaende, Labels... alles einstellbar
			// (sehr viele moegliche Angaben, manche ganz sinnvoll)
	|	"TextStyle"				(DDOT LARGER	egal | MINUS LARGER	egal)
			//	SchriftDarstellung(brauch man das?)
	|	"BoxRatios"				(DDOT LARGER	egal | MINUS LARGER	egal)
			//3D: Laengenverzerrung der Achsen in der Darstellung(linear)

 	|	"Lighting"				(DDOT LARGER	egal | MINUS LARGER	egal)
 			// soll es Lichtquellen geben (ja/nein), oder gar eine Farbfunktionen
	|	"LightSources"			(DDOT LARGER	egal | MINUS LARGER	egal)
			// Liste von orientierten Lichtquellen
	|	"AmbientLight"			(DDOT LARGER	egal | MINUS LARGER	egal)
			// 
	|	"AxesEdge"				(DDOT LARGER	egal | MINUS LARGER	egal)
			//	welche Axen der Box sollen gezeichnet werden

// -- eher schwierig:
	|	"PlotRange" 			(DDOT LARGER	egal | MINUS LARGER	egal)
			// Abschneiden bei zu extremen Werten. Wie soll man abschneiden?(durch die Polygone?)
	|	"DefaultColor"			(DDOT LARGER	egal | MINUS LARGER	egal)
			// StandardFarbe (kann nicht mehr die gesetzten Farben in einem Block aendern)
	|	"Background"			(DDOT LARGER	egal | MINUS LARGER	egal)
			// Hintergrundfarbe (fuer den Viewer schlecht da file.m nur Teil der Scene ist)
			// wie sollte ich das Aendern?
	|	"ColorOutput" 			(DDOT LARGER	egal | MINUS LARGER	egal)
			// wie Farben ausgegeben werden sollen (sehr speziell)
	|	"AxesStyle"				(DDOT LARGER	egal | MINUS LARGER	egal)
			// gibt den Axen Directiven(Farbe,dicke ...) brauchen wir das wir das?
	|	"BoxStyle"				(DDOT LARGER	egal | MINUS LARGER	egal)
			// gibt wie "AxesStyle" der Bounding Box graphische Direktiven

// -- unsinnig/unmoeglich
	|	"PlotLabel" 			(DDOT LARGER	egal | MINUS LARGER	egal)
			// 2D: Label an den Achsen (wir sind im 3d !!!)
	|	"AspectRatio"			(DDOT LARGER	egal | MINUS LARGER	egal)
			//2D: Laengenverzerrung der Achsen in der Darstellung(2d ist egal)
	|	"DefaultFont"			(DDOT LARGER	egal | MINUS LARGER	egal)
			// SchriftArt (veraltete Mathematica Version)
	|	"PlotRegion"			(DDOT LARGER	egal | MINUS LARGER	egal)
			// groese des Fensters in Mathematica (ist egal) 			
	|	"ViewVertical"			(DDOT LARGER	egal | MINUS LARGER	egal)
			// dreht irgendwie das dargestellte Bild
	|	"SphericalRegion"		(DDOT LARGER	egal | MINUS LARGER	egal)
			//	merkwuerdige Art die mit Graphic ausgefuellte Flaeche im Fenster zu setzen
			// (ist mathematica speziefisch, brauchen wir nicht)
	|	"Shading"				(DDOT LARGER	egal | MINUS LARGER	egal)
			// irgendwas mit Farbe anzeigen (eigentlich fuer SurfaceGraphics nicht fuer Graphics3D)
	|	"RenderAll"				(DDOT LARGER	egal | MINUS LARGER	egal)
			// berechnet Alles oder nur sichtbare Teile bei der Bilderzeugung in Mathematica
			// (ist egal da wir ja keine Bilder sondern eine Scene erzeugen wollen)
	|	"PolygonIntersections"	(DDOT LARGER	egal | MINUS LARGER	egal)
			// Teilt Polygone so das sie sich nicht mehr schneiden.
			// Fuer die PostScript-Erzeugung fuer das Bild.

// -- keine Ahnung
	|	"DisplayFunction"		(DDOT LARGER	egal | MINUS LARGER	egal)
			// ???
	|	"Plot3Matrix"			(DDOT LARGER	egal | MINUS LARGER	egal)
			// (Alte mathematica Version)
	|	"ImageSize"				(DDOT LARGER	egal | MINUS LARGER	egal)
			// Render Informationen
	|	"FormatType"			(DDOT LARGER	egal | MINUS LARGER	egal)
			// irgendwas fuer OutputStreams 			
	;
	
// -------------------------------------------------- Kleinkram -------------------------------------------

protected
egal
// ueberliest den Rest bis zur naechsten Option. Laest das Komma stehen!
// endet auch beim Klammerende
	: (~(  COLON | OPEN_BRACE | OPEN_BRACKET | CLOSE_BRACE | LPAREN ))*
		(   OPEN_BRACE	 	(dumb)*		CLOSE_BRACE   		(egal)?
		 |	OPEN_BRACKET 	(dumb)*		CLOSE_BRACKET   	(egal)?
		 |  LPAREN			(dumb)*		RPAREN				(egal)?  	)?
  ;
	
integerthing returns[int i]
// liest ein Integer aus
{i=0;String sig="";}
	: (PLUS | MINUS {sig="-";} )?
	  s:INTEGER_THING {i=Integer.parseInt(sig + s.getText());}
	;
	
doublething returns[double d]
// liest ein double aus
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
// liest den exponenten fuer double_thing
{e=0; String sig="";}
    : STAR HAT 
    (PLUS | MINUS {sig="-";} )?
     s:INTEGER_THING
     	{e=Integer.parseInt(sig + s.getText() );}
	;
	
protected
dumb
// ueberliset alles bis zum Klammerende auch mit Unterklammern
	:(  (~(	LPAREN | RPAREN | OPEN_BRACE | OPEN_BRACKET | CLOSE_BRACE | CLOSE_BRACKET))+
		(   OPEN_BRACE	 	(dumb)*	CLOSE_BRACE   
		 |	OPEN_BRACKET 	(dumb)*	CLOSE_BRACKET 
		 |  LPAREN			(dumb)*		RPAREN	)?  )
	 |  (   OPEN_BRACE	 	(dumb)*	CLOSE_BRACE   
		 |  OPEN_BRACKET 	(dumb)*	CLOSE_BRACKET
		 |  LPAREN			(dumb)*		RPAREN  )
  ;
	
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
DDOT: 			':';

T1: '!';
T2: '@';
T3: '#';
T4: '%';
T5: '&';
T6: '=';
T7: ';';
T8: '"';
T9: '?';

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