/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.reader.mathematica;
import java.awt.*;
import java.util.*;
import de.jreality.geometry.*;
import de.jreality.math.*;
import de.jreality.scene.data.*;
import de.jreality.scene.*;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.*;
import de.jreality.util.LoggingSystem;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;

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

public class MathematicaParser extends antlr.LLkParser       implements MathematicaParserTokenTypes
 {

/**
* wandelt Graphic3D- Objekte die aus Mathematica mittels 
* 			<Graphics3D -Object> >> <Pfad>\ <fileName.m>
* herausgeschrieben wurden in einen SceneGraphen von JReality um.
*
* Die Daten haben im file folgende Form(sind uebrigens direkter Mathematica-Text):
* Graphics3D[ {} , {} ]
* in der ersten Klammer steht ein "{..}"-Klammer Baum von gemischten Listen aus 
* graphischen Objekten und graphischen Directiven( wie Farben ect.)
* Bsp: Graphics3D[ 		{o1,o2,d1,{d2,o3,{o4},d3,o5}} 			,{} ]   
*					(d1-d3 Directiven , o1-o5 graphische Objekte)
* in der 2. Klammer steht eine Liste von Optionen die die ganze Scene beinflussen.
* Bsp: Graphics3D[ 		{o1,o2,d1,{d2,o3,{o4},d3,o5}} 			,{Op1,Opt2,..} ]
* 			Von dieser Beschreibung sind manche Dinge Optional !
*
* Vorgehen: 
* erste Klammer:
*		Klammern werden zu SceneGraphComponents die den Inhalt als Unterbaeume tragen.
* 		die graphischen Objekte werden je zu einer SceneGr.C. mit entsprechender Geometrie
*			manche Objekte werden hierbei zusammengefast*
*		die graphischen Directiven werden in der jeweils betroffenen Geometrie gesetzt(Farben)
*			oder als Appearance in den zur Geom. gehoerenden Knoten gesetzt.(Punktgroesse ect.)
*					manche Directiven werden ueberlesen!!!
* zweite Klammer:
*		die meissten Optionen werden ueberlesen!!!
*		Ansonsten werden sie den Root Knoten beeinflussen.
*
* Polygone die in einer Liste bis auf Flaechen-Farb-Direktiven untereinander stehen,
*	 	werden zu einer indexedFaceSet
*   Linien die in einer Liste bis auf einfache-Farb-Direktiven untereinander stehen,
*	 	werden zu einer indexedLineSet
*   Punkte die in einer Liste bis auf einfache-Farb-Direktiven untereinander stehen,
*	 	werden zu einem PointSet
* 	 TexteMarken werden zu gelabelten EINELEMENTIGEN Pointsets.(sie werden nicht verbunden)
*
*   desweiteren werden doppelte Punkte aus Line- und Face- Sets aussortiert
* 		das ermoeglicht u.a. smoothshading bei Flaechen aus getrennt angegebenen Polygonen(das ist ueblich)
*
*   Farben werden je nach Bedarf als Farbliste in Line-, Point-, und Face- Sets eingebunden.
*   	Vorzugsweise aber als Farbe in der Appearance des Unterknotens.
*
* Internes:
* ueberall die aktuelle Appearance, FlaechenFarbe(fC), und Punkt/LinienFarbe(plC) durchreichen
*
* TO DO:
* viele Optionen sind noch unbehandelt (siehe dort: "optionPrimitives")
* Standard Licheter und Camera werden nicht uebernommen 
* 		(der Scenegraph hat keine Lichter oder Camera)
* ein paar Directiven werden auch ignoriert (siehe dort: "directive")
* die Root hat Linien und Punkte ausgeschaltet. 
*		Sie werden bei den entsprechenden Knoten der Geometrieen wieder eingeschaltet.
* Auch wenn Appearances wie Directiven fuer ganze gruppen gelten koennen,
*		werden sie erst im Knoten der Geometrie eingesetzt.
*/

	// this is what is returned from the parsing process
	private SceneGraphComponent root = new SceneGraphComponent();	
	
	private SceneGraphComponent current = root;		// aktuell zu erweiternder Knoten 
	private Logger log = LoggingSystem.getLogger(MathematicaParser.class);
	private CoordinateSystemFactory box;
	private Appearance globalApp =new Appearance();	// App. der root
	private Color plCDefault= new Color(255,0,0);	// default- Punkt und Linienfarbe
	private Color fCDefault = new Color(0,255,0);	// default- Flaechenfarbe
	

// ---------------------------- total Sizes of Scene --------------------------
	private double[][] borderValue= new double [3][]; // maximale Werte der Scenenkoordinaten in x-,y- und z-Richtung

	private boolean gotFirstPoint =false;
	private void resetBorder (double [] v){	  // erweitert den borderValue moeglicherweise durch diesen Punkt
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
	private static Appearance copyApp(Appearance appOld){
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
	private static Color copyColor(Color c){
		// kopiert eine Farbe um doppelt-Verzeigerung zu vermeiden
		return new Color(c.getRed(),c.getGreen(),c.getBlue()) ;
	}
	
	private double[] getRGBColor(Color c){
	// retuns a array that represents a color (needed for color-Arrays as double[][])
		double[] fl= new double[3];
		fl[0]=c.getRed()/255.0;
		fl[1]=c.getGreen()/255.0;
		fl[2]=c.getBlue()/255.0;
		return fl ;
	}
	
/**
* konstructs a parser who can translate a
* mathematica-file to the corresponding SceneGraph
* @param    see superclass
* example: MathematicaParser p=
*	    new MathematicaParser(new MathematicaLexer(
*	     new FileReader(new File("file.m"))));
*/

protected MathematicaParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public MathematicaParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected MathematicaParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public MathematicaParser(TokenStream lexer) {
  this(lexer,2);
}

public MathematicaParser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
}

/**
* starts the parsing Process
* @param none sourcefile set by creating the object
* @returns SceneGraphComponent root of generated scene
*/
	public final SceneGraphComponent  start() throws RecognitionException, TokenStreamException {
		SceneGraphComponent r;
		
		r = null;	
			globalApp.setName("global");	
			root.setAppearance(globalApp);
			root.setName("Mathematica");
			log.setLevel(Level.FINE);
		
		
		try {      // for error handling
			match(4);
			match(OPEN_BRACKET);
			{
			switch ( LA(1)) {
			case OPEN_BRACE:
			{
				firstList(plCDefault,fCDefault,globalApp);
				break;
			}
			case LITERAL_Cuboid:
			case LITERAL_Polygon:
			case LITERAL_SurfaceColor:
			{
				fCDefault=faceThing(fCDefault,globalApp);
				break;
			}
			case LITERAL_Text:
			case LITERAL_Point:
			case LITERAL_Line:
			case LITERAL_RGBColor:
			case LITERAL_Hue:
			case LITERAL_GrayLevel:
			case LITERAL_CMYKColor:
			{
				plCDefault=plThing(plCDefault,globalApp);
				break;
			}
			case LITERAL_EdgeForm:
			case LITERAL_AbsolutePointSize:
			case LITERAL_AbsoluteThickness:
			case LITERAL_Dashing:
			case LITERAL_FaceForm:
			case LITERAL_PointSize:
			case LITERAL_Thickness:
			case LITERAL_AbsoluteDashing:
			{
				globalApp=appThing(globalApp);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			
				  	// Box und Axen ermoeglichen:
				  	box=new CoordinateSystemFactory(root);
					
			{
			switch ( LA(1)) {
			case COLON:
			{
				optionen();
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
			
					globalApp.setAttribute(CommonAttributes.VERTEX_DRAW, false);
					globalApp.setAttribute(CommonAttributes.SPHERES_DRAW, false);
					globalApp.setAttribute(CommonAttributes.EDGE_DRAW, false);
					globalApp.setAttribute(CommonAttributes.TUBES_DRAW, false);
					r = root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		return r;
	}
	
	private final void firstList(
		Color plC, Color fC, Appearance app
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case OPEN_BRACE:
			case LITERAL_Cuboid:
			case LITERAL_Text:
			case LITERAL_Point:
			case LITERAL_Line:
			case LITERAL_Polygon:
			case LITERAL_SurfaceColor:
			case LITERAL_RGBColor:
			case LITERAL_Hue:
			case LITERAL_GrayLevel:
			case LITERAL_CMYKColor:
			case LITERAL_EdgeForm:
			case LITERAL_AbsolutePointSize:
			case LITERAL_AbsoluteThickness:
			case LITERAL_Dashing:
			case LITERAL_FaceForm:
			case LITERAL_PointSize:
			case LITERAL_Thickness:
			case LITERAL_AbsoluteDashing:
			{
				objectList(plC,fC,app);
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
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
	}
	
	private final Color  faceThing(
		Color fCgiven, Appearance app
	) throws RecognitionException, TokenStreamException {
		Color fC ;
		
		fC=copyColor(fCgiven);
		Appearance app2=copyApp(app);
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_Cuboid:
			{
				cuboid(fC, app2);
				break;
			}
			case LITERAL_Polygon:
			{
				fC=polygonBlock(fC, app2);
				break;
			}
			case LITERAL_SurfaceColor:
			{
				fC=faceColor();
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
		return fC ;
	}
	
	private final Color  plThing(
		Color plCgiven, Appearance app
	) throws RecognitionException, TokenStreamException {
		Color plC;
		
		plC=copyColor(plCgiven);
		Appearance app2=copyApp(app);
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_RGBColor:
			case LITERAL_Hue:
			case LITERAL_GrayLevel:
			case LITERAL_CMYKColor:
			{
				plC=color();
				break;
			}
			case LITERAL_Line:
			{
				plC=lineBlock(plC, app2);
				break;
			}
			case LITERAL_Point:
			{
				plC=pointBlock(plC, app2);
				break;
			}
			case LITERAL_Text:
			{
				text(plC, app2);
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
		return plC;
	}
	
	private final  Appearance  appThing(
		Appearance appOld
	) throws RecognitionException, TokenStreamException {
		 Appearance app;
		
		app=copyApp(appOld);
		
		try {      // for error handling
			app=directiveBlock(app);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return app;
	}
	
	private final void optionen() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(COLON);
			match(OPEN_BRACE);
			{
			switch ( LA(1)) {
			case OPEN_BRACE:
			case LITERAL_Boxed:
			case LITERAL_Axes:
			case LITERAL_AxesLabel:
			case LITERAL_Prolog:
			case LITERAL_Epilog:
			case LITERAL_ViewPoint:
			case LITERAL_ViewCenter:
			case LITERAL_FaceGrids:
			case LITERAL_Ticks:
			case LITERAL_TextStyle:
			case LITERAL_BoxRatios:
			case LITERAL_Lighting:
			case LITERAL_LightSources:
			case LITERAL_AmbientLight:
			case LITERAL_AxesEdge:
			case LITERAL_PlotRange:
			case LITERAL_DefaultColor:
			case LITERAL_Background:
			case LITERAL_ColorOutput:
			case LITERAL_AxesStyle:
			case LITERAL_BoxStyle:
			case LITERAL_PlotLabel:
			case LITERAL_AspectRatio:
			case LITERAL_DefaultFont:
			case LITERAL_PlotRegion:
			case LITERAL_ViewVertical:
			case LITERAL_SphericalRegion:
			case LITERAL_Shading:
			case LITERAL_RenderAll:
			case LITERAL_PolygonIntersections:
			case LITERAL_DisplayFunction:
			case 67:
			case LITERAL_ImageSize:
			case LITERAL_FormatType:
			{
				option();
				{
				_loop60:
				do {
					if ((LA(1)==COLON)) {
						match(COLON);
						option();
					}
					else {
						break _loop60;
					}
					
				} while (true);
				}
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
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
	}
	
	private final void objectList(
		Color plC, Color fC, Appearance app
	) throws RecognitionException, TokenStreamException {
		
		Appearance app2=copyApp(app);
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case OPEN_BRACE:
			{
				list( plC, fC, app2);
				break;
			}
			case LITERAL_Cuboid:
			case LITERAL_Polygon:
			case LITERAL_SurfaceColor:
			{
				fC=faceThing(fC, app2);
				break;
			}
			case LITERAL_Text:
			case LITERAL_Point:
			case LITERAL_Line:
			case LITERAL_RGBColor:
			case LITERAL_Hue:
			case LITERAL_GrayLevel:
			case LITERAL_CMYKColor:
			{
				plC=plThing(plC, app2);
				break;
			}
			case LITERAL_EdgeForm:
			case LITERAL_AbsolutePointSize:
			case LITERAL_AbsoluteThickness:
			case LITERAL_Dashing:
			case LITERAL_FaceForm:
			case LITERAL_PointSize:
			case LITERAL_Thickness:
			case LITERAL_AbsoluteDashing:
			{
				app2=appThing(app2);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop12:
			do {
				if ((LA(1)==COLON)) {
					match(COLON);
					{
					switch ( LA(1)) {
					case OPEN_BRACE:
					{
						list( plC, fC, app2);
						break;
					}
					case LITERAL_Cuboid:
					case LITERAL_Polygon:
					case LITERAL_SurfaceColor:
					{
						fC=faceThing(fC,app2);
						break;
					}
					case LITERAL_Text:
					case LITERAL_Point:
					case LITERAL_Line:
					case LITERAL_RGBColor:
					case LITERAL_Hue:
					case LITERAL_GrayLevel:
					case LITERAL_CMYKColor:
					{
						plC=plThing(plC,app2);
						break;
					}
					case LITERAL_EdgeForm:
					case LITERAL_AbsolutePointSize:
					case LITERAL_AbsoluteThickness:
					case LITERAL_Dashing:
					case LITERAL_FaceForm:
					case LITERAL_PointSize:
					case LITERAL_Thickness:
					case LITERAL_AbsoluteDashing:
					{
						app2=appThing(app2);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					break _loop12;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
	}
	
	private final void list(
		Color plC, Color fC, Appearance app
	) throws RecognitionException, TokenStreamException {
		
		Appearance app2=copyApp(app);
		
		try {      // for error handling
			match(OPEN_BRACE);
									
						// neuen Knoten erstellen der die Listenelemente haelt, 
						SceneGraphComponent newPart = new SceneGraphComponent();
						newPart.setName("Object");
			//			newPart.setAppearance(app2);
						SceneGraphComponent oldPart = current;
						current.addChild(newPart);
						current=newPart;
						
			{
			switch ( LA(1)) {
			case OPEN_BRACE:
			case LITERAL_Cuboid:
			case LITERAL_Text:
			case LITERAL_Point:
			case LITERAL_Line:
			case LITERAL_Polygon:
			case LITERAL_SurfaceColor:
			case LITERAL_RGBColor:
			case LITERAL_Hue:
			case LITERAL_GrayLevel:
			case LITERAL_CMYKColor:
			case LITERAL_EdgeForm:
			case LITERAL_AbsolutePointSize:
			case LITERAL_AbsoluteThickness:
			case LITERAL_Dashing:
			case LITERAL_FaceForm:
			case LITERAL_PointSize:
			case LITERAL_Thickness:
			case LITERAL_AbsoluteDashing:
			{
				objectList(plC,fC,app2);
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
			current=oldPart;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
	}
	
	private final void cuboid(
		 Color fC,Appearance app
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_Cuboid);
			match(OPEN_BRACKET);
			double[] v2=new double [3]; 
						v2[0]=v2[1]=v2[2]=1;
						double[] v=new double[3];
						
			v=vektor();
			{
			switch ( LA(1)) {
			case COLON:
			{
				match(COLON);
				v2=vektordata();
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
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final Color  polygonBlock(
		Color fCgiven, Appearance app
	) throws RecognitionException, TokenStreamException {
		Color fC;
		
		fC=copyColor(fCgiven);
		Vector coordinates= new Vector(); 	// alle PunktListen vereint in einer
		Vector poly=new Vector();			// alle Punkte in einem Polygon
		int[] polyIndices;					// alle indices eines Polygons
		Vector colors= new Vector();		// FarbListe
		Vector polysIndices= new Vector();	// IndexListen-Liste
		int count=0;						// zaehlt die Punkte mit
		boolean colorFlag=false;
		boolean colorNeeded =false;
		
		
		try {      // for error handling
			match(LITERAL_Polygon);
			match(OPEN_BRACKET);
			poly=lineset();
			
								polyIndices=new int[poly.size()+1];
								for(int i=0;i<poly.size();i++){
									coordinates.add(poly.get(i));  //Punkte zu einer Liste machen
							    	polyIndices[i]=i;			   // indizirung merken
							    }
							    polyIndices[poly.size()]=0;
						    	count=poly.size();
								polysIndices.add(polyIndices);
							    colors.add(fC);
							
			match(CLOSE_BRACKET);
			{
			_loop36:
			do {
				if ((LA(1)==COLON) && (LA(2)==LITERAL_Polygon||LA(2)==LITERAL_SurfaceColor)) {
					match(COLON);
					{
					switch ( LA(1)) {
					case LITERAL_SurfaceColor:
					{
						{
						fC=faceColor();
						colorFlag=true;
						}
						break;
					}
					case LITERAL_Polygon:
					{
						{
						match(LITERAL_Polygon);
						match(OPEN_BRACKET);
						poly=lineset();
						if (colorFlag) colorNeeded= true;
						
											polyIndices=new int[poly.size()+1];
											for(int i=0;i<poly.size();i++){
												coordinates.add(poly.get(i));  //Punkte zu einer Liste machen
										    	polyIndices[i]=i+count;			   // indizirung merken
										    }
										    polyIndices[poly.size()]=count;
									    	count+=poly.size();
											polysIndices.add(polyIndices);
											colors.add(fC);
										
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
				}
				else {
					break _loop36;
				}
				
			} while (true);
			}
			
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
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return fC;
	}
	
	private final Color  faceColor() throws RecognitionException, TokenStreamException {
		Color fC;
		
		Color specular; double d; fC= new Color(255,0,0);
		
		try {      // for error handling
			match(LITERAL_SurfaceColor);
			match(OPEN_BRACKET);
			fC=color();
			{
			switch ( LA(1)) {
			case COLON:
			{
				match(COLON);
				specular=color();
				{
				switch ( LA(1)) {
				case COLON:
				{
					match(COLON);
					d=doublething();
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
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return fC;
	}
	
	private final Color  color() throws RecognitionException, TokenStreamException {
		Color c;
		
		c= new Color(0,255,0);
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_RGBColor:
			{
				match(LITERAL_RGBColor);
				match(OPEN_BRACKET);
				double r,g,b; r=b=g=0;
				r=doublething();
				match(COLON);
				g=doublething();
				match(COLON);
				b=doublething();
				match(CLOSE_BRACKET);
				
									 float red,green,blue;
									 red=(float) r; green=(float) g; blue=(float) b;
									 c= new Color(red,green,blue);
									
				break;
			}
			case LITERAL_Hue:
			{
				match(LITERAL_Hue);
				match(OPEN_BRACKET);
				double h; double s; double b; h=s=b=0.5;
				h=doublething();
				{
				switch ( LA(1)) {
				case COLON:
				{
					match(COLON);
					s=doublething();
					match(COLON);
					b=doublething();
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
				
									 float hue,sat,bri;
									 hue=(float) h; sat=(float) s; bri=(float) b;
									 c = Color.getHSBColor(hue,sat,bri);
									
				break;
			}
			case LITERAL_GrayLevel:
			{
				match(LITERAL_GrayLevel);
				match(OPEN_BRACKET);
				double gr=0;
				gr=doublething();
				match(CLOSE_BRACKET);
				
									float grey=(float) gr;
									c= new Color(grey,grey,grey);
									
				break;
			}
			case LITERAL_CMYKColor:
			{
				match(LITERAL_CMYKColor);
				match(OPEN_BRACKET);
				double cy,ma,ye,k; cy=ma=ye=k=0;
				cy=doublething();
				match(COLON);
				ma=doublething();
				match(COLON);
				ye=doublething();
				match(COLON);
				k=doublething();
				match(CLOSE_BRACKET);
				
									 float r,g,b;
									 r=(float) ((1-cy)*(1-k));
									 g=(float) ((1-ma)*(1-k));
									 b=(float) ((1-ye)*(1-k));
									 c= new Color(r,g,b);
									
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
		return c;
	}
	
	private final Color  lineBlock(
		Color plCgiven, Appearance app
	) throws RecognitionException, TokenStreamException {
		Color plC;
		
		
		plC=copyColor(plCgiven);					// Punkt/Linienfarbe
		Vector coordinates= new Vector();			// alle Punkte in einer Liste
		Vector line=new Vector();					// alle Punkte einer Linie
		Vector colors= new Vector();				// FarbListe
		int count=0;								// Anzahl aller bisher gesammelten Punkte
		int[] lineIndices;							// liste aller Indices einer Linie
		Vector linesIndices= new Vector();			// Liste aller IndiceeListen
		boolean colorFlag=false;
		boolean colorNeeded =false;
		
		
		try {      // for error handling
			match(LITERAL_Line);
			match(OPEN_BRACKET);
			line=lineset();
			
								lineIndices=new int[line.size()];
								for(int i=0;i<line.size();i++){
									coordinates.add(line.get(i));  // Punkte zu einer Liste machen
							    	lineIndices[i]=i;			   // indizirung merken
							    }
						    	count=line.size();
								linesIndices.add(lineIndices);
							    colors.add(plC);
							
			match(CLOSE_BRACKET);
			{
			_loop30:
			do {
				if ((LA(1)==COLON) && (_tokenSet_6.member(LA(2)))) {
					match(COLON);
					{
					switch ( LA(1)) {
					case LITERAL_RGBColor:
					case LITERAL_Hue:
					case LITERAL_GrayLevel:
					case LITERAL_CMYKColor:
					{
						{
						plC=color();
						colorFlag=true;
						}
						break;
					}
					case LITERAL_Line:
					{
						{
						match(LITERAL_Line);
						match(OPEN_BRACKET);
						line=lineset();
						if (colorFlag) colorNeeded= true;
						
											lineIndices=new int[line.size()];
											for(int i=0;i<line.size();i++){			// mithilfe von 'count' weiterzaehlen
												coordinates.add(line.get(i));  		// Punkte zu einer Liste machen
										    	lineIndices[i]=i+count;			    // indizirung merken
										    }
									    	count+=line.size();
											linesIndices.add(lineIndices);
											colors.add(plC);
										
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
				}
				else {
					break _loop30;
				}
				
			} while (true);
			}
			
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
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return plC;
	}
	
	private final Color  pointBlock(
		Color plCgiven,Appearance app
	) throws RecognitionException, TokenStreamException {
		Color plC;
		
		Vector points= new Vector(); 
		double[] v;
		Vector colors= new Vector();
		plC=copyColor(plCgiven);
		boolean colorFlag=false;
		boolean colorNeeded =false;
		
		
		try {      // for error handling
			{
			match(LITERAL_Point);
			match(OPEN_BRACKET);
			v=new double[3];
			v=vektor();
			points.add(v);
							 colors.add(plC);
			match(CLOSE_BRACKET);
			}
			{
			_loop24:
			do {
				if ((LA(1)==COLON) && (_tokenSet_7.member(LA(2)))) {
					match(COLON);
					{
					switch ( LA(1)) {
					case LITERAL_RGBColor:
					case LITERAL_Hue:
					case LITERAL_GrayLevel:
					case LITERAL_CMYKColor:
					{
						plC=color();
						colorFlag=true;
						break;
					}
					case LITERAL_Point:
					{
						{
						match(LITERAL_Point);
						if (colorFlag) colorNeeded= true;
						match(OPEN_BRACKET);
						v=new double [3];
						v=vektor();
						points.add(v);
										 colors.add(plC);
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
				}
				else {
					break _loop24;
				}
				
			} while (true);
			}
			
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
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return plC;
	}
	
	private final void text(
		 Color plC,Appearance app
	) throws RecognitionException, TokenStreamException {
		
		Token  s = null;
		double[] v=new double[3]; String t;
		
		try {      // for error handling
			match(LITERAL_Text);
			match(OPEN_BRACKET);
			s = LT(1);
			match(STRING);
			match(COLON);
			v=vektordata();
			match(CLOSE_BRACKET);
			
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
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	private final  Appearance  directiveBlock(
		Appearance appOld
	) throws RecognitionException, TokenStreamException {
		 Appearance app;
		
		app =copyApp(appOld);
		
		try {      // for error handling
			app=directive(app);
			{
			_loop49:
			do {
				if ((LA(1)==COLON) && ((LA(2) >= LITERAL_EdgeForm && LA(2) <= LITERAL_AbsoluteDashing))) {
					match(COLON);
					app=directive(app);
				}
				else {
					break _loop49;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return app;
	}
	
	private final double[]  vektor() throws RecognitionException, TokenStreamException {
		double[] res;
		
		res =new double [3];
		double res1,res2,res3;
		
		try {      // for error handling
			match(OPEN_BRACE);
			res1=doublething();
			match(COLON);
			res2=doublething();
			match(COLON);
			res3=doublething();
			match(CLOSE_BRACE);
			
						 res[0]=res1;
						 res[1]=res2;
						 res[2]=res3;
						 resetBorder (res);
						
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return res;
	}
	
	private final double[]  vektordata() throws RecognitionException, TokenStreamException {
		double[] res;
		
		res =new double [3];
		double res1,res2,res3;
		
		try {      // for error handling
			match(OPEN_BRACE);
			res1=doublething();
			match(COLON);
			res2=doublething();
			match(COLON);
			res3=doublething();
			match(CLOSE_BRACE);
			
						 res[0]=res1;
						 res[1]=res2;
						 res[2]=res3;
						
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		return res;
	}
	
	private final Vector  lineset() throws RecognitionException, TokenStreamException {
		Vector v;
		
		
		double [] point =new double[3];
		double [] point2=new double[3];
		double [] point3=new double [3];
		v=new Vector();
		
		try {      // for error handling
			match(OPEN_BRACE);
			point=vektor();
			
					    v.addElement(point);
					    point3[0]=point[0];// have to save first point seperate and insert later again to first position (dont ask !)
					    point3[1]=point[1];
					    point3[2]=point[2];		    
						
			{
			_loop44:
			do {
				if ((LA(1)==COLON)) {
					match(COLON);
					point2= new double[3];
								
					point2=vektor();
					
							    v.addElement(point2);
								
				}
				else {
					break _loop44;
				}
				
			} while (true);
			}
			match(CLOSE_BRACE);
			
					 v.setElementAt(point3,0);
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		return v;
	}
	
	private final double  doublething() throws RecognitionException, TokenStreamException {
		double d;
		
		Token  s = null;
		Token  s2 = null;
		Token  s3 = null;
		d=0; double e=0; String sig="";
		
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
			case INTEGER_THING:
			case DOT:
			{
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
			case INTEGER_THING:
			{
				s = LT(1);
				match(INTEGER_THING);
				d=Double.parseDouble(sig + s.getText());
				{
				switch ( LA(1)) {
				case DOT:
				{
					match(DOT);
					{
					switch ( LA(1)) {
					case INTEGER_THING:
					{
						s2 = LT(1);
						match(INTEGER_THING);
						d=Double.parseDouble(sig + s.getText()+ "." + s2.getText());
						break;
					}
					case CLOSE_BRACKET:
					case CLOSE_BRACE:
					case COLON:
					case STAR:
					{
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
				case CLOSE_BRACKET:
				case CLOSE_BRACE:
				case COLON:
				case STAR:
				{
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
			case DOT:
			{
				match(DOT);
				s3 = LT(1);
				match(INTEGER_THING);
				d=Double.parseDouble(sig + "0." + s3.getText());
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
			case STAR:
			{
				e=exponent_thing();
				d=d*Math.pow(10,e);
				break;
			}
			case CLOSE_BRACKET:
			case CLOSE_BRACE:
			case COLON:
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
			recover(ex,_tokenSet_2);
		}
		return d;
	}
	
/** 
* TODO: interpret more directives(do this in mathematica.g)
*/
	private final Appearance  directive(
		Appearance appGiven
	) throws RecognitionException, TokenStreamException {
		Appearance app;
		
		app = copyApp(appGiven); 
		Color col;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_EdgeForm:
			{
				match(LITERAL_EdgeForm);
				match(OPEN_BRACKET);
				Color c= Color.BLACK;
				{
				switch ( LA(1)) {
				case LITERAL_RGBColor:
				case LITERAL_Hue:
				case LITERAL_GrayLevel:
				case LITERAL_CMYKColor:
				{
					c=color();
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
				
					 	app.setAttribute(CommonAttributes.EDGE_DRAW, true);
					 	app.setAttribute(CommonAttributes.TUBES_DRAW, true);
					 	app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c);
						
				match(CLOSE_BRACKET);
				break;
			}
			case LITERAL_AbsolutePointSize:
			{
				match(LITERAL_AbsolutePointSize);
				match(OPEN_BRACKET);
				double d=0;
				d=integerthing();
				
							app.setAttribute(CommonAttributes.POINT_RADIUS,d/40);
							app.setAttribute(CommonAttributes.POINT_SIZE,d);
							
				match(CLOSE_BRACKET);
				break;
			}
			case LITERAL_AbsoluteThickness:
			{
				match(LITERAL_AbsoluteThickness);
				match(OPEN_BRACKET);
				double d=0;
				d=integerthing();
				match(CLOSE_BRACKET);
				
							app.setAttribute(CommonAttributes.TUBE_RADIUS,d/40);
							app.setAttribute(CommonAttributes.LINE_WIDTH,d);
							
				break;
			}
			case LITERAL_Dashing:
			{
				match(LITERAL_Dashing);
				match(OPEN_BRACKET);
				{
				switch ( LA(1)) {
				case 4:
				case OPEN_BRACKET:
				case OPEN_BRACE:
				case COLON:
				case LITERAL_Cuboid:
				case LITERAL_Text:
				case STRING:
				case LITERAL_Point:
				case LITERAL_Line:
				case LITERAL_Polygon:
				case LITERAL_SurfaceColor:
				case LITERAL_RGBColor:
				case LITERAL_Hue:
				case LITERAL_GrayLevel:
				case LITERAL_CMYKColor:
				case LITERAL_EdgeForm:
				case LITERAL_AbsolutePointSize:
				case LITERAL_AbsoluteThickness:
				case LITERAL_Dashing:
				case LITERAL_FaceForm:
				case LITERAL_PointSize:
				case LITERAL_Thickness:
				case LITERAL_AbsoluteDashing:
				case Option:
				case LITERAL_Boxed:
				case MINUS:
				case LARGER:
				case LITERAL_True:
				case LITERAL_False:
				case DDOT:
				case LITERAL_Axes:
				case LITERAL_Automatic:
				case LITERAL_AxesLabel:
				case LITERAL_Prolog:
				case LITERAL_Epilog:
				case LITERAL_ViewPoint:
				case LITERAL_ViewCenter:
				case LITERAL_FaceGrids:
				case LITERAL_Ticks:
				case LITERAL_TextStyle:
				case LITERAL_BoxRatios:
				case LITERAL_Lighting:
				case LITERAL_LightSources:
				case LITERAL_AmbientLight:
				case LITERAL_AxesEdge:
				case LITERAL_PlotRange:
				case LITERAL_DefaultColor:
				case LITERAL_Background:
				case LITERAL_ColorOutput:
				case LITERAL_AxesStyle:
				case LITERAL_BoxStyle:
				case LITERAL_PlotLabel:
				case LITERAL_AspectRatio:
				case LITERAL_DefaultFont:
				case LITERAL_PlotRegion:
				case LITERAL_ViewVertical:
				case LITERAL_SphericalRegion:
				case LITERAL_Shading:
				case LITERAL_RenderAll:
				case LITERAL_PolygonIntersections:
				case LITERAL_DisplayFunction:
				case 67:
				case LITERAL_ImageSize:
				case LITERAL_FormatType:
				case LPAREN:
				case PLUS:
				case INTEGER_THING:
				case DOT:
				case STAR:
				case HAT:
				case BACKS:
				case SLASH:
				case DOLLAR:
				case SMALER:
				case T1:
				case T2:
				case T3:
				case T4:
				case T5:
				case T6:
				case T7:
				case T8:
				case T9:
				case ID:
				case ID_LETTER:
				case DIGIT:
				case ESC:
				case WS_:
				{
					dumb();
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
				log.fine("Dashing not implemented");
				break;
			}
			case LITERAL_FaceForm:
			{
				match(LITERAL_FaceForm);
				match(OPEN_BRACKET);
				{
				switch ( LA(1)) {
				case 4:
				case OPEN_BRACKET:
				case OPEN_BRACE:
				case COLON:
				case LITERAL_Cuboid:
				case LITERAL_Text:
				case STRING:
				case LITERAL_Point:
				case LITERAL_Line:
				case LITERAL_Polygon:
				case LITERAL_SurfaceColor:
				case LITERAL_RGBColor:
				case LITERAL_Hue:
				case LITERAL_GrayLevel:
				case LITERAL_CMYKColor:
				case LITERAL_EdgeForm:
				case LITERAL_AbsolutePointSize:
				case LITERAL_AbsoluteThickness:
				case LITERAL_Dashing:
				case LITERAL_FaceForm:
				case LITERAL_PointSize:
				case LITERAL_Thickness:
				case LITERAL_AbsoluteDashing:
				case Option:
				case LITERAL_Boxed:
				case MINUS:
				case LARGER:
				case LITERAL_True:
				case LITERAL_False:
				case DDOT:
				case LITERAL_Axes:
				case LITERAL_Automatic:
				case LITERAL_AxesLabel:
				case LITERAL_Prolog:
				case LITERAL_Epilog:
				case LITERAL_ViewPoint:
				case LITERAL_ViewCenter:
				case LITERAL_FaceGrids:
				case LITERAL_Ticks:
				case LITERAL_TextStyle:
				case LITERAL_BoxRatios:
				case LITERAL_Lighting:
				case LITERAL_LightSources:
				case LITERAL_AmbientLight:
				case LITERAL_AxesEdge:
				case LITERAL_PlotRange:
				case LITERAL_DefaultColor:
				case LITERAL_Background:
				case LITERAL_ColorOutput:
				case LITERAL_AxesStyle:
				case LITERAL_BoxStyle:
				case LITERAL_PlotLabel:
				case LITERAL_AspectRatio:
				case LITERAL_DefaultFont:
				case LITERAL_PlotRegion:
				case LITERAL_ViewVertical:
				case LITERAL_SphericalRegion:
				case LITERAL_Shading:
				case LITERAL_RenderAll:
				case LITERAL_PolygonIntersections:
				case LITERAL_DisplayFunction:
				case 67:
				case LITERAL_ImageSize:
				case LITERAL_FormatType:
				case LPAREN:
				case PLUS:
				case INTEGER_THING:
				case DOT:
				case STAR:
				case HAT:
				case BACKS:
				case SLASH:
				case DOLLAR:
				case SMALER:
				case T1:
				case T2:
				case T3:
				case T4:
				case T5:
				case T6:
				case T7:
				case T8:
				case T9:
				case ID:
				case ID_LETTER:
				case DIGIT:
				case ESC:
				case WS_:
				{
					dumb();
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
				log.fine("FaceForm not implemented");
				break;
			}
			case LITERAL_PointSize:
			{
				match(LITERAL_PointSize);
				match(OPEN_BRACKET);
				{
				switch ( LA(1)) {
				case 4:
				case OPEN_BRACKET:
				case OPEN_BRACE:
				case COLON:
				case LITERAL_Cuboid:
				case LITERAL_Text:
				case STRING:
				case LITERAL_Point:
				case LITERAL_Line:
				case LITERAL_Polygon:
				case LITERAL_SurfaceColor:
				case LITERAL_RGBColor:
				case LITERAL_Hue:
				case LITERAL_GrayLevel:
				case LITERAL_CMYKColor:
				case LITERAL_EdgeForm:
				case LITERAL_AbsolutePointSize:
				case LITERAL_AbsoluteThickness:
				case LITERAL_Dashing:
				case LITERAL_FaceForm:
				case LITERAL_PointSize:
				case LITERAL_Thickness:
				case LITERAL_AbsoluteDashing:
				case Option:
				case LITERAL_Boxed:
				case MINUS:
				case LARGER:
				case LITERAL_True:
				case LITERAL_False:
				case DDOT:
				case LITERAL_Axes:
				case LITERAL_Automatic:
				case LITERAL_AxesLabel:
				case LITERAL_Prolog:
				case LITERAL_Epilog:
				case LITERAL_ViewPoint:
				case LITERAL_ViewCenter:
				case LITERAL_FaceGrids:
				case LITERAL_Ticks:
				case LITERAL_TextStyle:
				case LITERAL_BoxRatios:
				case LITERAL_Lighting:
				case LITERAL_LightSources:
				case LITERAL_AmbientLight:
				case LITERAL_AxesEdge:
				case LITERAL_PlotRange:
				case LITERAL_DefaultColor:
				case LITERAL_Background:
				case LITERAL_ColorOutput:
				case LITERAL_AxesStyle:
				case LITERAL_BoxStyle:
				case LITERAL_PlotLabel:
				case LITERAL_AspectRatio:
				case LITERAL_DefaultFont:
				case LITERAL_PlotRegion:
				case LITERAL_ViewVertical:
				case LITERAL_SphericalRegion:
				case LITERAL_Shading:
				case LITERAL_RenderAll:
				case LITERAL_PolygonIntersections:
				case LITERAL_DisplayFunction:
				case 67:
				case LITERAL_ImageSize:
				case LITERAL_FormatType:
				case LPAREN:
				case PLUS:
				case INTEGER_THING:
				case DOT:
				case STAR:
				case HAT:
				case BACKS:
				case SLASH:
				case DOLLAR:
				case SMALER:
				case T1:
				case T2:
				case T3:
				case T4:
				case T5:
				case T6:
				case T7:
				case T8:
				case T9:
				case ID:
				case ID_LETTER:
				case DIGIT:
				case ESC:
				case WS_:
				{
					dumb();
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
				log.fine("PointSize not implemented");
				break;
			}
			case LITERAL_Thickness:
			{
				match(LITERAL_Thickness);
				match(OPEN_BRACKET);
				{
				switch ( LA(1)) {
				case 4:
				case OPEN_BRACKET:
				case OPEN_BRACE:
				case COLON:
				case LITERAL_Cuboid:
				case LITERAL_Text:
				case STRING:
				case LITERAL_Point:
				case LITERAL_Line:
				case LITERAL_Polygon:
				case LITERAL_SurfaceColor:
				case LITERAL_RGBColor:
				case LITERAL_Hue:
				case LITERAL_GrayLevel:
				case LITERAL_CMYKColor:
				case LITERAL_EdgeForm:
				case LITERAL_AbsolutePointSize:
				case LITERAL_AbsoluteThickness:
				case LITERAL_Dashing:
				case LITERAL_FaceForm:
				case LITERAL_PointSize:
				case LITERAL_Thickness:
				case LITERAL_AbsoluteDashing:
				case Option:
				case LITERAL_Boxed:
				case MINUS:
				case LARGER:
				case LITERAL_True:
				case LITERAL_False:
				case DDOT:
				case LITERAL_Axes:
				case LITERAL_Automatic:
				case LITERAL_AxesLabel:
				case LITERAL_Prolog:
				case LITERAL_Epilog:
				case LITERAL_ViewPoint:
				case LITERAL_ViewCenter:
				case LITERAL_FaceGrids:
				case LITERAL_Ticks:
				case LITERAL_TextStyle:
				case LITERAL_BoxRatios:
				case LITERAL_Lighting:
				case LITERAL_LightSources:
				case LITERAL_AmbientLight:
				case LITERAL_AxesEdge:
				case LITERAL_PlotRange:
				case LITERAL_DefaultColor:
				case LITERAL_Background:
				case LITERAL_ColorOutput:
				case LITERAL_AxesStyle:
				case LITERAL_BoxStyle:
				case LITERAL_PlotLabel:
				case LITERAL_AspectRatio:
				case LITERAL_DefaultFont:
				case LITERAL_PlotRegion:
				case LITERAL_ViewVertical:
				case LITERAL_SphericalRegion:
				case LITERAL_Shading:
				case LITERAL_RenderAll:
				case LITERAL_PolygonIntersections:
				case LITERAL_DisplayFunction:
				case 67:
				case LITERAL_ImageSize:
				case LITERAL_FormatType:
				case LPAREN:
				case PLUS:
				case INTEGER_THING:
				case DOT:
				case STAR:
				case HAT:
				case BACKS:
				case SLASH:
				case DOLLAR:
				case SMALER:
				case T1:
				case T2:
				case T3:
				case T4:
				case T5:
				case T6:
				case T7:
				case T8:
				case T9:
				case ID:
				case ID_LETTER:
				case DIGIT:
				case ESC:
				case WS_:
				{
					dumb();
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
				log.fine("Thicknes not implemented");
				break;
			}
			case LITERAL_AbsoluteDashing:
			{
				match(LITERAL_AbsoluteDashing);
				match(OPEN_BRACKET);
				{
				switch ( LA(1)) {
				case 4:
				case OPEN_BRACKET:
				case OPEN_BRACE:
				case COLON:
				case LITERAL_Cuboid:
				case LITERAL_Text:
				case STRING:
				case LITERAL_Point:
				case LITERAL_Line:
				case LITERAL_Polygon:
				case LITERAL_SurfaceColor:
				case LITERAL_RGBColor:
				case LITERAL_Hue:
				case LITERAL_GrayLevel:
				case LITERAL_CMYKColor:
				case LITERAL_EdgeForm:
				case LITERAL_AbsolutePointSize:
				case LITERAL_AbsoluteThickness:
				case LITERAL_Dashing:
				case LITERAL_FaceForm:
				case LITERAL_PointSize:
				case LITERAL_Thickness:
				case LITERAL_AbsoluteDashing:
				case Option:
				case LITERAL_Boxed:
				case MINUS:
				case LARGER:
				case LITERAL_True:
				case LITERAL_False:
				case DDOT:
				case LITERAL_Axes:
				case LITERAL_Automatic:
				case LITERAL_AxesLabel:
				case LITERAL_Prolog:
				case LITERAL_Epilog:
				case LITERAL_ViewPoint:
				case LITERAL_ViewCenter:
				case LITERAL_FaceGrids:
				case LITERAL_Ticks:
				case LITERAL_TextStyle:
				case LITERAL_BoxRatios:
				case LITERAL_Lighting:
				case LITERAL_LightSources:
				case LITERAL_AmbientLight:
				case LITERAL_AxesEdge:
				case LITERAL_PlotRange:
				case LITERAL_DefaultColor:
				case LITERAL_Background:
				case LITERAL_ColorOutput:
				case LITERAL_AxesStyle:
				case LITERAL_BoxStyle:
				case LITERAL_PlotLabel:
				case LITERAL_AspectRatio:
				case LITERAL_DefaultFont:
				case LITERAL_PlotRegion:
				case LITERAL_ViewVertical:
				case LITERAL_SphericalRegion:
				case LITERAL_Shading:
				case LITERAL_RenderAll:
				case LITERAL_PolygonIntersections:
				case LITERAL_DisplayFunction:
				case 67:
				case LITERAL_ImageSize:
				case LITERAL_FormatType:
				case LPAREN:
				case PLUS:
				case INTEGER_THING:
				case DOT:
				case STAR:
				case HAT:
				case BACKS:
				case SLASH:
				case DOLLAR:
				case SMALER:
				case T1:
				case T2:
				case T3:
				case T4:
				case T5:
				case T6:
				case T7:
				case T8:
				case T9:
				case ID:
				case ID_LETTER:
				case DIGIT:
				case ESC:
				case WS_:
				{
					dumb();
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
				log.fine("AbsoluteDashing not implemented");
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
		return app;
	}
	
	private final int  integerthing() throws RecognitionException, TokenStreamException {
		int i;
		
		Token  s = null;
		i=0;String sig="";
		
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
			case INTEGER_THING:
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
			match(INTEGER_THING);
			i=Integer.parseInt(sig + s.getText());
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		return i;
	}
	
	private final void dumb() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case 4:
			case COLON:
			case LITERAL_Cuboid:
			case LITERAL_Text:
			case STRING:
			case LITERAL_Point:
			case LITERAL_Line:
			case LITERAL_Polygon:
			case LITERAL_SurfaceColor:
			case LITERAL_RGBColor:
			case LITERAL_Hue:
			case LITERAL_GrayLevel:
			case LITERAL_CMYKColor:
			case LITERAL_EdgeForm:
			case LITERAL_AbsolutePointSize:
			case LITERAL_AbsoluteThickness:
			case LITERAL_Dashing:
			case LITERAL_FaceForm:
			case LITERAL_PointSize:
			case LITERAL_Thickness:
			case LITERAL_AbsoluteDashing:
			case Option:
			case LITERAL_Boxed:
			case MINUS:
			case LARGER:
			case LITERAL_True:
			case LITERAL_False:
			case DDOT:
			case LITERAL_Axes:
			case LITERAL_Automatic:
			case LITERAL_AxesLabel:
			case LITERAL_Prolog:
			case LITERAL_Epilog:
			case LITERAL_ViewPoint:
			case LITERAL_ViewCenter:
			case LITERAL_FaceGrids:
			case LITERAL_Ticks:
			case LITERAL_TextStyle:
			case LITERAL_BoxRatios:
			case LITERAL_Lighting:
			case LITERAL_LightSources:
			case LITERAL_AmbientLight:
			case LITERAL_AxesEdge:
			case LITERAL_PlotRange:
			case LITERAL_DefaultColor:
			case LITERAL_Background:
			case LITERAL_ColorOutput:
			case LITERAL_AxesStyle:
			case LITERAL_BoxStyle:
			case LITERAL_PlotLabel:
			case LITERAL_AspectRatio:
			case LITERAL_DefaultFont:
			case LITERAL_PlotRegion:
			case LITERAL_ViewVertical:
			case LITERAL_SphericalRegion:
			case LITERAL_Shading:
			case LITERAL_RenderAll:
			case LITERAL_PolygonIntersections:
			case LITERAL_DisplayFunction:
			case 67:
			case LITERAL_ImageSize:
			case LITERAL_FormatType:
			case PLUS:
			case INTEGER_THING:
			case DOT:
			case STAR:
			case HAT:
			case BACKS:
			case SLASH:
			case DOLLAR:
			case SMALER:
			case T1:
			case T2:
			case T3:
			case T4:
			case T5:
			case T6:
			case T7:
			case T8:
			case T9:
			case ID:
			case ID_LETTER:
			case DIGIT:
			case ESC:
			case WS_:
			{
				{
				{
				int _cnt130=0;
				_loop130:
				do {
					if ((_tokenSet_8.member(LA(1))) && ((LA(2) >= 4 && LA(2) <= WS_))) {
						{
						match(_tokenSet_8);
						}
					}
					else {
						if ( _cnt130>=1 ) { break _loop130; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt130++;
				} while (true);
				}
				{
				if ((LA(1)==OPEN_BRACE) && (_tokenSet_9.member(LA(2)))) {
					match(OPEN_BRACE);
					{
					_loop133:
					do {
						if ((_tokenSet_10.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop133;
						}
						
					} while (true);
					}
					match(CLOSE_BRACE);
				}
				else if ((LA(1)==OPEN_BRACKET) && (_tokenSet_11.member(LA(2)))) {
					match(OPEN_BRACKET);
					{
					_loop135:
					do {
						if ((_tokenSet_10.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop135;
						}
						
					} while (true);
					}
					match(CLOSE_BRACKET);
				}
				else if ((LA(1)==LPAREN) && (_tokenSet_12.member(LA(2)))) {
					match(LPAREN);
					{
					_loop137:
					do {
						if ((_tokenSet_10.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop137;
						}
						
					} while (true);
					}
					match(RPAREN);
				}
				else if (((LA(1) >= 4 && LA(1) <= WS_)) && ((LA(2) >= 4 && LA(2) <= WS_))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				}
				break;
			}
			case OPEN_BRACKET:
			case OPEN_BRACE:
			case LPAREN:
			{
				{
				switch ( LA(1)) {
				case OPEN_BRACE:
				{
					match(OPEN_BRACE);
					{
					_loop140:
					do {
						if ((_tokenSet_10.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop140;
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
					_loop142:
					do {
						if ((_tokenSet_10.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop142;
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
					_loop144:
					do {
						if ((_tokenSet_10.member(LA(1)))) {
							dumb();
						}
						else {
							break _loop144;
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
			recover(ex,_tokenSet_13);
		}
	}
	
	private final void option() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case OPEN_BRACE:
			{
				match(OPEN_BRACE);
				{
				switch ( LA(1)) {
				case Option:
				{
					match(Option);
					{
					_loop64:
					do {
						if ((LA(1)==COLON)) {
							match(COLON);
							match(Option);
						}
						else {
							break _loop64;
						}
						
					} while (true);
					}
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
				break;
			}
			case LITERAL_Boxed:
			case LITERAL_Axes:
			case LITERAL_AxesLabel:
			case LITERAL_Prolog:
			case LITERAL_Epilog:
			case LITERAL_ViewPoint:
			case LITERAL_ViewCenter:
			case LITERAL_FaceGrids:
			case LITERAL_Ticks:
			case LITERAL_TextStyle:
			case LITERAL_BoxRatios:
			case LITERAL_Lighting:
			case LITERAL_LightSources:
			case LITERAL_AmbientLight:
			case LITERAL_AxesEdge:
			case LITERAL_PlotRange:
			case LITERAL_DefaultColor:
			case LITERAL_Background:
			case LITERAL_ColorOutput:
			case LITERAL_AxesStyle:
			case LITERAL_BoxStyle:
			case LITERAL_PlotLabel:
			case LITERAL_AspectRatio:
			case LITERAL_DefaultFont:
			case LITERAL_PlotRegion:
			case LITERAL_ViewVertical:
			case LITERAL_SphericalRegion:
			case LITERAL_Shading:
			case LITERAL_RenderAll:
			case LITERAL_PolygonIntersections:
			case LITERAL_DisplayFunction:
			case 67:
			case LITERAL_ImageSize:
			case LITERAL_FormatType:
			{
				optionPrimitive();
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
	
/** 
* TODO: interpret more Options(do this in mathematica.g)
*/
	private final void optionPrimitive() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_Boxed:
			{
				match(LITERAL_Boxed);
				{
				switch ( LA(1)) {
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					{
					switch ( LA(1)) {
					case LITERAL_True:
					{
						match(LITERAL_True);
						box.showBox(true);
						break;
					}
					case LITERAL_False:
					{
						match(LITERAL_False);
						box.showBox(false);
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
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					log.fine(" 'Boxed :> $<name>' not implemented");
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
			case LITERAL_Axes:
			{
				match(LITERAL_Axes);
				{
				switch ( LA(1)) {
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					{
					switch ( LA(1)) {
					case LITERAL_True:
					{
						match(LITERAL_True);
						box.showAxes(true);
						break;
					}
					case LITERAL_False:
					{
						match(LITERAL_False);
						box.showAxes(false);
						break;
					}
					case LITERAL_Automatic:
					{
						match(LITERAL_Automatic);
						log.fine("Axes -> Automatic not implemented");
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
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					log.fine(" 'Axes :> $<name>' not implemented");
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
			case LITERAL_AxesLabel:
			{
				match(LITERAL_AxesLabel);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" AxesLabel not implemented");
				break;
			}
			case LITERAL_Prolog:
			{
				match(LITERAL_Prolog);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" Prolog not implemented");
				break;
			}
			case LITERAL_Epilog:
			{
				match(LITERAL_Epilog);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" Epilog not implemented");
				break;
			}
			case LITERAL_ViewPoint:
			{
				match(LITERAL_ViewPoint);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" ViewPoint not implemented");
				break;
			}
			case LITERAL_ViewCenter:
			{
				match(LITERAL_ViewCenter);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" ViewCenter not implemented");
				break;
			}
			case LITERAL_FaceGrids:
			{
				match(LITERAL_FaceGrids);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" FaceGrids not implemented");
				break;
			}
			case LITERAL_Ticks:
			{
				match(LITERAL_Ticks);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" Ticks not implemented");
				break;
			}
			case LITERAL_TextStyle:
			{
				match(LITERAL_TextStyle);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" TextStyle not implemented");
				break;
			}
			case LITERAL_BoxRatios:
			{
				match(LITERAL_BoxRatios);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" BoxRatios not implemented");
				break;
			}
			case LITERAL_Lighting:
			{
				match(LITERAL_Lighting);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" Lighting not implemented");
				break;
			}
			case LITERAL_LightSources:
			{
				match(LITERAL_LightSources);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" LightSources not implemented");
				break;
			}
			case LITERAL_AmbientLight:
			{
				match(LITERAL_AmbientLight);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" AmbientLight not implemented");
				break;
			}
			case LITERAL_AxesEdge:
			{
				match(LITERAL_AxesEdge);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" AxesEdge not implemented");
				break;
			}
			case LITERAL_PlotRange:
			{
				match(LITERAL_PlotRange);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" PlotRange not implemented");
				break;
			}
			case LITERAL_DefaultColor:
			{
				match(LITERAL_DefaultColor);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" DefaultColor not implemented");
				break;
			}
			case LITERAL_Background:
			{
				match(LITERAL_Background);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" Background not implemented");
				break;
			}
			case LITERAL_ColorOutput:
			{
				match(LITERAL_ColorOutput);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" ColorOutput not implemented");
				break;
			}
			case LITERAL_AxesStyle:
			{
				match(LITERAL_AxesStyle);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" AxesStyle not implemented");
				break;
			}
			case LITERAL_BoxStyle:
			{
				match(LITERAL_BoxStyle);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" BoxStyle not implemented");
				break;
			}
			case LITERAL_PlotLabel:
			{
				match(LITERAL_PlotLabel);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" PlotLabel not implemented");
				break;
			}
			case LITERAL_AspectRatio:
			{
				match(LITERAL_AspectRatio);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" AspectRatio not implemented");
				break;
			}
			case LITERAL_DefaultFont:
			{
				match(LITERAL_DefaultFont);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" DefaultFont not implemented");
				break;
			}
			case LITERAL_PlotRegion:
			{
				match(LITERAL_PlotRegion);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" PlotRegion not implemented");
				break;
			}
			case LITERAL_ViewVertical:
			{
				match(LITERAL_ViewVertical);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" ViewVertical not implemented");
				break;
			}
			case LITERAL_SphericalRegion:
			{
				match(LITERAL_SphericalRegion);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" SphericalRegion not implemented");
				break;
			}
			case LITERAL_Shading:
			{
				match(LITERAL_Shading);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" Shading not implemented");
				break;
			}
			case LITERAL_RenderAll:
			{
				match(LITERAL_RenderAll);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" RenderAll not implemented");
				break;
			}
			case LITERAL_PolygonIntersections:
			{
				match(LITERAL_PolygonIntersections);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" PolygonIntersections not implemented");
				break;
			}
			case LITERAL_DisplayFunction:
			{
				match(LITERAL_DisplayFunction);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" DisplayFunction not implemented");
				break;
			}
			case 67:
			{
				match(67);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" Plot3Matrix not implemented");
				break;
			}
			case LITERAL_ImageSize:
			{
				match(LITERAL_ImageSize);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" ImageSize not implemented");
				break;
			}
			case LITERAL_FormatType:
			{
				match(LITERAL_FormatType);
				{
				switch ( LA(1)) {
				case DDOT:
				{
					match(DDOT);
					match(LARGER);
					egal();
					break;
				}
				case MINUS:
				{
					match(MINUS);
					match(LARGER);
					egal();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				log.fine(" FormatType not implemented");
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
	
	private final void egal() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			_loop105:
			do {
				if ((_tokenSet_14.member(LA(1)))) {
					{
					match(_tokenSet_14);
					}
				}
				else {
					break _loop105;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case OPEN_BRACE:
			{
				match(OPEN_BRACE);
				{
				_loop108:
				do {
					if ((_tokenSet_10.member(LA(1)))) {
						dumb();
					}
					else {
						break _loop108;
					}
					
				} while (true);
				}
				match(CLOSE_BRACE);
				{
				if (((LA(1) >= 4 && LA(1) <= WS_)) && ((LA(2) >= 4 && LA(2) <= WS_))) {
					egal();
				}
				else if ((LA(1)==CLOSE_BRACE||LA(1)==COLON) && (_tokenSet_15.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				break;
			}
			case OPEN_BRACKET:
			{
				match(OPEN_BRACKET);
				{
				_loop111:
				do {
					if ((_tokenSet_10.member(LA(1)))) {
						dumb();
					}
					else {
						break _loop111;
					}
					
				} while (true);
				}
				match(CLOSE_BRACKET);
				{
				if (((LA(1) >= 4 && LA(1) <= WS_)) && ((LA(2) >= 4 && LA(2) <= WS_))) {
					egal();
				}
				else if ((LA(1)==CLOSE_BRACE||LA(1)==COLON) && (_tokenSet_15.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				{
				_loop114:
				do {
					if ((_tokenSet_10.member(LA(1)))) {
						dumb();
					}
					else {
						break _loop114;
					}
					
				} while (true);
				}
				match(RPAREN);
				{
				if (((LA(1) >= 4 && LA(1) <= WS_)) && ((LA(2) >= 4 && LA(2) <= WS_))) {
					egal();
				}
				else if ((LA(1)==CLOSE_BRACE||LA(1)==COLON) && (_tokenSet_15.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				break;
			}
			case CLOSE_BRACE:
			case COLON:
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
			recover(ex,_tokenSet_5);
		}
	}
	
	private final int  exponent_thing() throws RecognitionException, TokenStreamException {
		int e;
		
		Token  s = null;
		e=0; String sig="";
		
		try {      // for error handling
			match(STAR);
			match(HAT);
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
			case INTEGER_THING:
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
			match(INTEGER_THING);
			e=Integer.parseInt(sig + s.getText() );
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return e;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"Graphics3D\"",
		"OPEN_BRACKET",
		"CLOSE_BRACKET",
		"OPEN_BRACE",
		"CLOSE_BRACE",
		"COLON",
		"\"Cuboid\"",
		"\"Text\"",
		"STRING",
		"\"Point\"",
		"\"Line\"",
		"\"Polygon\"",
		"\"SurfaceColor\"",
		"\"RGBColor\"",
		"\"Hue\"",
		"\"GrayLevel\"",
		"\"CMYKColor\"",
		"\"EdgeForm\"",
		"\"AbsolutePointSize\"",
		"\"AbsoluteThickness\"",
		"\"Dashing\"",
		"\"FaceForm\"",
		"\"PointSize\"",
		"\"Thickness\"",
		"\"AbsoluteDashing\"",
		"Option",
		"\"Boxed\"",
		"MINUS",
		"LARGER",
		"\"True\"",
		"\"False\"",
		"DDOT",
		"\"Axes\"",
		"\"Automatic\"",
		"\"AxesLabel\"",
		"\"Prolog\"",
		"\"Epilog\"",
		"\"ViewPoint\"",
		"\"ViewCenter\"",
		"\"FaceGrids\"",
		"\"Ticks\"",
		"\"TextStyle\"",
		"\"BoxRatios\"",
		"\"Lighting\"",
		"\"LightSources\"",
		"\"AmbientLight\"",
		"\"AxesEdge\"",
		"\"PlotRange\"",
		"\"DefaultColor\"",
		"\"Background\"",
		"\"ColorOutput\"",
		"\"AxesStyle\"",
		"\"BoxStyle\"",
		"\"PlotLabel\"",
		"\"AspectRatio\"",
		"\"DefaultFont\"",
		"\"PlotRegion\"",
		"\"ViewVertical\"",
		"\"SphericalRegion\"",
		"\"Shading\"",
		"\"RenderAll\"",
		"\"PolygonIntersections\"",
		"\"DisplayFunction\"",
		"\"Plot3Matrix\"",
		"\"ImageSize\"",
		"\"FormatType\"",
		"LPAREN",
		"RPAREN",
		"PLUS",
		"INTEGER_THING",
		"DOT",
		"STAR",
		"HAT",
		"BACKS",
		"SLASH",
		"DOLLAR",
		"SMALER",
		"T1",
		"T2",
		"T3",
		"T4",
		"T5",
		"T6",
		"T7",
		"T8",
		"T9",
		"an identifier",
		"ID_LETTER",
		"DIGIT",
		"ESC",
		"WS_"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 576L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 832L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 64L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 256L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 768L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 1982464L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 1974272L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = new long[8];
		data[0]=-496L;
		data[1]=2147483455L;
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = new long[8];
		data[0]=-80L;
		data[1]=2147483519L;
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = new long[8];
		data[0]=-336L;
		data[1]=2147483519L;
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = new long[8];
		data[0]=-272L;
		data[1]=2147483519L;
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = new long[8];
		data[0]=-336L;
		data[1]=2147483647L;
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = new long[8];
		data[0]=-16L;
		data[1]=2147483647L;
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = new long[8];
		data[0]=-944L;
		data[1]=2147483583L;
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { -205084688192L, 63L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	
	}
