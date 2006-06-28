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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
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

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import de.jreality.geometry.CoordinateSystemFactory;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.data.StringArray;

public class JoinGeometry {

	// sammelt doppelte Punkte raus 
	// 3 Versionen : 
	// 		Loesung hat alle Punkte verglichen Loesung hat keine Atribute (selber wieder setzen)
	// 		Loesung hat alle Punkte verglichen die gleiche Atribute haben
	// 		Loesung uebernimmt immer das erste Attribut
	
	// vier Klassen:
	// EdgeAttributes
	// VertexAttributes
	// FaceAttributes
	// GeometryAttributes
	
//	 COORDINATES	vereint (schon ok)
//	 NORMALS		Entscheiden: neu berechnen, was bei vorher per hand gesetzt?  
//	 COLORS			Entscheiden: erste Farbe, oder nur vereinen bei gleicher Farbe
//	 INDICES		angeglichen (schon ok)
//	 TEXTURE_COORDINATES	keine Ahnung was das soll(wenn das nur bei Flaechen geht ist mir das egal)
//	 POINT_SIZE		(gilt das nur fuer Punkte? waehre analog zu Farbe bei Punkten)
//	 LABELS 		einfach string ++
//
	
	public static double eps=0.000000001;
	//	eps  : Tolleranz bei der noch Gleichheit der Punkte angesehen wird 	(double)

	public static void main (String[] arg){System.out.println("JoinGeometry.main()");}
	private static boolean compare(double[] p1,double[] p2){
		// vergleicht Punkte bis auf sqrt(eps) als Tolleranz
		
		double delta = (p1[0]-p2[0])*(p1[0]-p2[0]) // quadrat der Differenz
					  +(p1[1]-p2[1])*(p1[1]-p2[1])
					  +(p1[2]-p2[2])*(p1[2]-p2[2]);
		//System.out.println(" x:"+p1[0]+","+p1[1]+","+p1[2]+") y:"+p2[0]+","+p2[1]+","+p2[2]+")-> "+(delta<eps));
		
		return (delta<eps);
	}
	/**
	 * 
	 * @param pNew
	 * @param p
	 * @param alt
	 * @return
	 * @TODO : besser vergleichen! im Momment: fast quadratischer Aufwand
	 * 			 (alles mit allem vergleichen) besser: HashTable oder so
	 */
	
	private static int searchIndex(Vector pNew, double[] p,int alt){
		//	 returnes the index of the first match of p in pNew
		//	 returnes alt for no match of the first alt-1 points in pNew
		//   alt should be at most the size of pNew
		int index= alt;
		for (int i= 0; i<alt;i++){
			if ( compare((double[])pNew.elementAt(i),p)){
				index=i;
				break;
			}			
		}
		return index;
	} 
	private static int searchIndex(double[][] coordsNew, double[] p,int alt){
		int index= alt;
		for (int i= 0; i<alt;i++){
			if ( compare(coordsNew[i],p)){
				index=i;
				break;
			}			
		}
		return index;
	}
	
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

	// @TODO:   Attribute ein und auslesen ist unvollstaendig 
	public static Vector test(double [][] coordsOld,int [][] indicesOld){
		IndexedFaceSetFactory fac =new IndexedFaceSetFactory();
		fac.setLineCount(0);
		fac.setVertexCount(coordsOld.length);
		fac.setFaceCount(indicesOld.length);
		fac.setVertexCoordinates(coordsOld);
		fac.setFaceIndices(indicesOld);
		fac.update();
		// calc new
		fac=removeDublicateVertices(fac.getIndexedFaceSet());
		fac.update();
		// einfuegen der neuen Indizees (Punkte sind schon drinn)
		Vector res=new Vector();
		res.add(fac.getIndexedFaceSet().getVertexAttributes(Attribute.COORDINATES));
		res.add(fac.getIndexedFaceSet().getVertexAttributes(Attribute.INDICES));		
		return res;
	}
	private static Attribute[] makeAttributeArray(DataListSet AttrDLS){
		//liefert einen Array mit den enthaltenen Attribute
		if (AttrDLS!= null){
			int numOfAttr=0;
			Set AttrSet =AttrDLS.storedAttributes();
			Iterator ite= AttrSet.iterator();
			Vector temp=new Vector();
			while (ite.hasNext()){
				temp.add((Attribute)ite.next());
				numOfAttr++;
			}
			Attribute[] AttributeList= new Attribute[numOfAttr];
			for (int i=0; i<numOfAttr;i++)
				AttributeList[i]= (Attribute)temp.get(i);
			return AttributeList;
		}
		return null;
	}
	
	private static int[][] makeNewIndicees(int [][] indicesOld,int [] refference){
		int len=indicesOld.length;
		int[][] indicesNew= new int[len][];
		int k=0;
		//System.out.println("start"+len);
		for (int i=0;i<len;i++){
			k=indicesOld[i].length;
			//System.out.println("			len:"+k);
			indicesNew[i]=new int[k];
			
			for (int j=0;j<k;j++){
				indicesNew[i][j]=refference[indicesOld[i][j]];
				//System.out.println("ind:"+refference[indicesOld[i][j]]);
			}
		}
		return indicesNew;
	}
	
	public static IndexedFaceSetFactory makeCopyFac(IndexedFaceSet input){
		IndexedFaceSetFactory result= new IndexedFaceSetFactory();
		
		result.getIndexedFaceSet().setNumPoints(input.getNumPoints());
		result.getIndexedFaceSet().setNumEdges(input.getNumEdges());
		result.getIndexedFaceSet().setNumFaces(input.getNumFaces());
		
		result.getIndexedFaceSet().setGeometryAttributes(input.getGeometryAttributes());
		result.getIndexedFaceSet().setEdgeAttributes(input.getVertexAttributes());
		result.getIndexedFaceSet().setEdgeAttributes(input.getEdgeAttributes());
		result.getIndexedFaceSet().setFaceAttributes(input.getFaceAttributes());
		
		result.update();
		result.setGenerateVertexNormals(true);
		result.setGenerateFaceNormals(true);
		result.setGenerateEdgesFromFaces(true);
		result.update();
		return result;
	}
	
	
//public static IndexedLineSet joinIndexedLineSets(IndexedLineSet ils[]){
	// gesichertes! auslesen der Punkte 
	
	
	
	// gesichertes! auslesen der Indices
	
	
	// indices aneinander zaehlen (was bei leheren Listen-> egal: nix anfuegen :) )
	// Punkte und Linien anzahl mitzaehlen
	
	//-> Schleife:
	// 	neue Punkte -> neue Indices(+Offset)

	//	 gesichertes auslesen der anderen Attribute
	// anfuegen der anderen Attributlisten (was bei Leere?
	// zB Color?)
	// Einfach: keine Attribute, nur Indices!!!
	
	
//}  
	
	public static PointSet copyPointSet (PointSet input){
		PointSet result= new PointSet();
		result.setNumPoints(input.getNumPoints());
		result.setGeometryAttributes(input.getGeometryAttributes());
		result.setVertexAttributes(input.getVertexAttributes());
		return result;
	}
		
	public static IndexedFaceSet makeCopyFaceSet(IndexedFaceSet input){
		IndexedFaceSet result = new IndexedFaceSet();
		result.setNumPoints(input.getNumPoints());
		result.setNumEdges(input.getNumEdges());
		result.setNumFaces(input.getNumFaces());
		result.setGeometryAttributes(input.getGeometryAttributes());
		result.setVertexAttributes(input.getVertexAttributes());
		result.setEdgeAttributes(input.getEdgeAttributes());
		result.setFaceAttributes(input.getFaceAttributes());
		return result;
	}
	
	public static IndexedFaceSetFactory removeDublicateVertices(IndexedFaceSet ifs){
		// die alten Daten auslesen	
		double [][] oldVertexCoordsArray=null;
		double[][] oldVertexColorArray=null;
		int[][]    oldVertexIndizeesArray=null;
		String[]   oldVertexLabelsArray=null;
		double[][] oldVertexNormalsArray=null;
		double[]   oldVertexSizeArray= null;
		double[][] oldVertexTextureCoordsArray=null;
		int [][] faceIndicesOld=null;
		int [][] edgeIndicesOld=null;
				
		DataList temp= ifs.getVertexAttributes ( Attribute.COORDINATES );
		if (temp!=null) oldVertexCoordsArray 		= temp.toDoubleArrayArray(null);
		temp= ifs.getVertexAttributes ( Attribute.COLORS );
		if (temp!=null)	oldVertexColorArray 		= temp.toDoubleArrayArray(null);
		temp=ifs.getVertexAttributes ( Attribute.INDICES );
		if (temp !=null)oldVertexIndizeesArray 		= temp.toIntArrayArray(null);
		temp= ifs.getVertexAttributes( Attribute.LABELS );
		if (temp!=null)	oldVertexLabelsArray 		= temp.toStringArray(null);
		temp= ifs.getVertexAttributes( Attribute.NORMALS );
		if (temp!=null) oldVertexNormalsArray 		= temp.toDoubleArrayArray(null);
		temp= ifs.getVertexAttributes( Attribute.POINT_SIZE);
		if (temp!=null) oldVertexSizeArray 			= temp.toDoubleArray(null);
		temp= ifs.getVertexAttributes( Attribute.TEXTURE_COORDINATES );
		if (temp!=null) oldVertexTextureCoordsArray = temp.toDoubleArrayArray(null);
		
		int numOfVertices	=0;
		int numOfEdges		=0;
		int numOfFaces		=0;
		
		if (oldVertexCoordsArray!=null)	numOfVertices= 	oldVertexCoordsArray.length;
		if (edgeIndicesOld!=null)		numOfEdges= 	edgeIndicesOld.length;
		if (faceIndicesOld!=null)		numOfFaces= 	faceIndicesOld.length;		
		
		// refferenceTable.[i] verweist auf den neuen i.Index (fuer umindizierung)
		int[] refferenceTabel =new int[numOfVertices];
		
		// hier werden die Punkte neu gelesen und die Verweise in RefferenceTable gemerkt
		// neue Attribute der Punkte zwischenspeichern:
		int curr=0; // : aktuell einzufuegender Index 
		int index;
		if (numOfVertices>0){
			for (int i=0; i<numOfVertices;i++){
				// benutze durchgelaufenen Teil der Datenliste fuer neue Daten 
				index=searchIndex(oldVertexCoordsArray,oldVertexCoordsArray[i],curr);
				refferenceTabel[i]=index; //Indizes vermerken 
				//System.out.println("reff["+i+"]="+index);
				if(curr==index){
					oldVertexCoordsArray[curr]=oldVertexCoordsArray[i];
					if (oldVertexColorArray!=null)
						oldVertexColorArray[curr]=oldVertexColorArray[i];
					if (oldVertexIndizeesArray!=null)
						oldVertexIndizeesArray[curr]=oldVertexIndizeesArray[i];
					if (oldVertexLabelsArray!=null)
						oldVertexLabelsArray[curr]=oldVertexLabelsArray[i];
					if (oldVertexNormalsArray!=null)
						oldVertexNormalsArray[curr]=oldVertexNormalsArray[i];
					if (oldVertexSizeArray!=null)
						oldVertexSizeArray[curr]=oldVertexSizeArray[i];
					if (oldVertexTextureCoordsArray!=null)
						oldVertexTextureCoordsArray[curr]=oldVertexTextureCoordsArray[i];
					curr++;
				}
			}	
		}
		int numOfVerticesNew = curr;
		
		// Die VertexAttributVektoren kuerzen		
		double[][] newVertexColorArray= 		new double[numOfVerticesNew][3];
		double[][] newVertexCoordsArray= 		new double[numOfVerticesNew][3];
		String[]   newVertexLabelsArray= 		new String[numOfVerticesNew];
		double[][] newVertexNormalsArray= 		new double[numOfVerticesNew][3];
		double[][] newVertexTextureCoordsArray= new double[numOfVerticesNew][];
		double[]   newVertexSizeArray= 			new double[numOfVerticesNew];
		int[][]    newVertexIndizeesArray= 		new int[numOfVerticesNew][];
		
		for(int i=0;i<numOfVerticesNew;i++){
			if (oldVertexCoordsArray!=null)			newVertexCoordsArray[i]=oldVertexCoordsArray[i];
			if (oldVertexColorArray!=null)			newVertexColorArray[i]=oldVertexColorArray[i];
			if (oldVertexIndizeesArray!=null)		newVertexIndizeesArray[i]=oldVertexIndizeesArray[i];
			if (oldVertexLabelsArray!=null)			newVertexLabelsArray[i]=oldVertexLabelsArray[i];
			if (oldVertexNormalsArray!=null)		newVertexNormalsArray[i]=oldVertexNormalsArray[i];
			if (oldVertexSizeArray!=null)			newVertexSizeArray[i]=oldVertexSizeArray[i];
			if (oldVertexTextureCoordsArray!=null)	newVertexTextureCoordsArray[i]=oldVertexTextureCoordsArray[i];
		}
						
		// Die Vertex Attribute wieder einfuegen
		IndexedFaceSetFactory result=new IndexedFaceSetFactory();
		result.setVertexCount(numOfVerticesNew);
		result.setLineCount(numOfEdges);
		result.setFaceCount(numOfFaces);
		result.update();
		if (numOfVerticesNew>0){
			if (oldVertexCoordsArray!=null){
				System.out.println("coords");
				System.out.println("vorm update VertAttr:"+result.getIndexedFaceSet().getVertexAttributes().toString());
				result.getIndexedFaceSet().setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
				//result.setVertexCoordinates(newVertexCoordsArray); // does not work
				System.out.println("vorm update VertAttr:"+result.getIndexedFaceSet().getVertexAttributes().toString());
			}
			if (oldVertexColorArray!=null){
				System.out.println("color");
				//result.setVertexColors(newVertexColorArray);
				result.getIndexedFaceSet().setVertexAttributes(Attribute.COLORS, new DoubleArrayArray.Array(newVertexColorArray));
			}
			if (oldVertexLabelsArray!=null){
				System.out.println("labels");
				result.getIndexedFaceSet().setVertexAttributes(Attribute.LABELS, new StringArray(newVertexLabelsArray));
				//result.setVertexLabels(newVertexLabelsArray);
			}
			if (oldVertexNormalsArray!=null){
				System.out.println("normals");
				result.getIndexedFaceSet().setVertexAttributes(Attribute.NORMALS, new DoubleArrayArray.Array(newVertexNormalsArray));
				//result.setVertexNormals(newVertexNormalsArray);
			}
			if (oldVertexTextureCoordsArray!=null){
				System.out.println("texture");
				result.getIndexedFaceSet().setVertexAttributes(Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
				//result.setVertexTextureCoordinates(newVertexTextureCoordsArray);
			}
			if (oldVertexSizeArray!=null){
				System.out.println("size");
				result.getIndexedFaceSet().setVertexAttributes(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
				//result.setVertexAttribute(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
			}
			if (oldVertexIndizeesArray!=null){
				System.out.println("indicees");
				result.getIndexedFaceSet().setVertexAttributes(Attribute.INDICES, new IntArrayArray.Array(newVertexIndizeesArray));
				//result.setVertexAttribute(Attribute.INDICES,new IntArrayArray.Array(newVertexIndizeesArray));
			}
		}
		
		// uebernehmen der alten Attribute
		result.getIndexedFaceSet().setGeometryAttributes(ifs.getGeometryAttributes());
		result.getIndexedFaceSet().setEdgeAttributes(ifs.getEdgeAttributes());
		result.getIndexedFaceSet().setFaceAttributes(ifs.getFaceAttributes());

		// die Indicees angleichen		
		temp=ifs.getFaceAttributes( Attribute.INDICES );
		if (temp !=null)	faceIndicesOld = temp.toIntArrayArray(null);
		temp=ifs.getEdgeAttributes( Attribute.INDICES );
		if (temp !=null)	edgeIndicesOld = temp.toIntArrayArray(null);
		int [][] faceIndicesNew= makeNewIndicees(faceIndicesOld,refferenceTabel);
		int [][] edgesIndicesNew=makeNewIndicees(edgeIndicesOld,refferenceTabel);

		if((numOfEdges>0)&(numOfVertices>0))
			result.getIndexedFaceSet().setEdgeAttributes(Attribute.INDICES, new IntArrayArray.Array(edgesIndicesNew));
			//result.setEdgeIndices(edgesIndicesNew);// geht schief
			
		if((numOfFaces>0)&(numOfVertices>0))
			result.getIndexedFaceSet().setFaceAttributes(Attribute.INDICES, new IntArrayArray.Array(faceIndicesNew));
			//result.setFaceIndices(faceIndicesNew);// geht schief
//		System.out.println("vorm update VertAttr:"+result.getIndexedFaceSet().getVertexAttributes().toString());
//		System.out.println("#Points"+numOfVerticesNew+" #Edges:"+numOfEdges+" #Faces:"+numOfFaces);
		
		result.update();
//		System.out.println("nachm Update FaceAttr:"+result.getIndexedFaceSet().getFaceAttributes().toString());
		return result;
	}
	/**
	 * reduces the points:
	 * makes corresponding Lists to the params
	 *  with no point twice set in the new coords-List 
	 * @param coordsOld
	 * @param indicesOld
	 * @return a Vector of two elements:
	 *	 double[][3]  : points
	 *	 int[][]      : indicesVector of  
	 */
	public static Vector meltCoords( double [][] coordsOld,int [][] indicesOld){
		// returns a Vector of two elements:
		// double[][3]  : points
		// int[][]      : indices
		System.out.println("JoinGeometry.meltCoords()");
		return test(coordsOld,indicesOld);}
		/**
		int iOldSize=indicesOld.length;
		//	 die neuen Typen Daten erstellen
		int [][] indicesNew = new int[iOldSize][];
		Vector coordsNew = new Vector();
	
		// hilfsVariablen
		int curr=0; // : aktuell einzufuegender Index(= length(pNew) )
		int n=0;
		int index;
		double[] currPoint;
	
		for (int i=0;i<iOldSize;i++){	
			n=indicesOld[i].length;
			indicesNew[i]= new int[n];
			for (int j=0;j<n;j++){
				currPoint=coordsOld[indicesOld[i][j]];
				index= searchIndex(coordsNew, currPoint,curr);
				indicesNew[i][j]=index;
				if (index==curr){
					curr++;
					coordsNew.add(currPoint);
				} 
			}
		}
		
		//konvertieren
		double [][] points= new double[curr][];
		for (int i=0;i<curr;i++)
			points[i]=(double[])coordsNew.elementAt(i);

		Vector res=new Vector();
		res.add(points);
		res.add(indicesNew);
		return res;
	}
	/**
	 * reduces the points:
	 * makes a simple IndexedFaceSet which has no Point
	 *  twice set in the CoordinateList
	 *  this IFS has no Attributes set
	 * @param indexedFaceSet to smaler
	 * @return indexedFaceSet with less Points but no Attributes  
	 */
	public static IndexedFaceSet meltFace (IndexedFaceSet ifs){
		// die alten Daten auslesen
		System.out.println("JoinGeometry.meltFace()");
		int [][] indicesOld = ifs.getFaceAttributes( Attribute.INDICES ).toIntArrayArray(null);
		double [][] coordsOld = ifs.getFaceAttributes( Attribute.COORDINATES ).toDoubleArrayArray(null);
		int iOldSize=indicesOld.length;
		//	 die neuen Typen Daten erstellen
		int [][] indicesNew = new int[iOldSize][];
		Vector coordsNew = new Vector();
	
		// hilfsVariablen
		int curr=0; // : aktuell einzufuegender Index(= length(pNew) )
		int n=0;
		int index;
		double[] currPoint;
	
		for (int i=0;i<iOldSize;i++){	
			n=indicesOld[i].length;
			indicesNew[i]= new int[n];
			for (int j=0;j<n;j++){
				currPoint=coordsOld[indicesOld[i][j]];
				index= searchIndex(coordsNew, currPoint,curr);
				indicesNew[i][j]=index;
				if (index==curr){
					curr++;
					coordsNew.add(currPoint);
				} 
			}
		}
		
		//konvertieren
		double [][] Points= new double[curr][];
		for (int i=0;i<curr;i++)
			Points[i]=(double[])coordsNew.elementAt(i);
		// einfuegen der neuen Punkte & Indizees
		IndexedFaceSetFactory ifsf=new IndexedFaceSetFactory();
		
		//	 uebernehmen der Face Atribute:
		ifsf.setFaceAttributes(ifs.getFaceAttributes());	
		ifsf.setFaceCount(iOldSize);
		ifsf.setVertexCount(curr);
		ifsf.setFaceIndices(indicesNew);
		ifsf.setVertexCoordinates(Points);
		
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}
	
	/**
	 * entfernt alle doppelt angegebenen Punkte
	 * setzt die Indizees um
	 * erst genannte Punktattribute gelten
	 * @param ifs
	 * @return IndexedFaceSet
	 */
	public static IndexedFaceSet removeDublicatePoints(IndexedFaceSet ifs){
		// die alten Daten auslesen	
		double [][] oldVertexCoordsArray=null;
		double[][] oldVertexColorArray=null;
		int[][]    oldVertexIndizeesArray=null;
		String[]   oldVertexLabelsArray=null;
		double[][] oldVertexNormalsArray=null;
		double[]   oldVertexSizeArray= null;
		double[][] oldVertexTextureCoordsArray=null;
				
		DataList temp= ifs.getVertexAttributes ( Attribute.COORDINATES );
		if (temp!=null) oldVertexCoordsArray 		= temp.toDoubleArrayArray(null);
		temp= ifs.getVertexAttributes ( Attribute.COLORS );
		if (temp!=null)	oldVertexColorArray 		= temp.toDoubleArrayArray(null);
		temp=ifs.getVertexAttributes ( Attribute.INDICES );
		if (temp !=null)oldVertexIndizeesArray 		= temp.toIntArrayArray(null);
		temp= ifs.getVertexAttributes( Attribute.LABELS );
		if (temp!=null)	oldVertexLabelsArray 		= temp.toStringArray(null);
		temp= ifs.getVertexAttributes( Attribute.NORMALS );
		if (temp!=null) oldVertexNormalsArray 		= temp.toDoubleArrayArray(null);
		temp= ifs.getVertexAttributes( Attribute.POINT_SIZE);
		if (temp!=null) oldVertexSizeArray 			= temp.toDoubleArray(null);
		temp= ifs.getVertexAttributes( Attribute.TEXTURE_COORDINATES );
		if (temp!=null) oldVertexTextureCoordsArray = temp.toDoubleArrayArray(null);
		
		int numOfVertices	=ifs.getNumPoints();
		
		// refferenceTable.[i] verweist auf den neuen i.Index (fuer umindizierung)
		int[] refferenceTabel =new int[numOfVertices];
		
		// hier werden die Punkte neu gelesen und die Verweise in RefferenceTable gemerkt
		// neue Attribute der Punkte zwischenspeichern:
		int curr=0; // : aktuell einzufuegender Index 
		int index;
		if (numOfVertices>0){
			for (int i=0; i<numOfVertices;i++){
				// benutze durchgelaufenen Teil der Datenliste fuer neue Daten 
				index=searchIndex(oldVertexCoordsArray,oldVertexCoordsArray[i],curr);
				refferenceTabel[i]=index; //Indizes vermerken 
				//System.out.println("reff["+i+"]="+index);
				if(curr==index){
					oldVertexCoordsArray[curr]=oldVertexCoordsArray[i];
					if (oldVertexColorArray!=null)
						oldVertexColorArray[curr]=oldVertexColorArray[i];
					if (oldVertexIndizeesArray!=null)
						oldVertexIndizeesArray[curr]=oldVertexIndizeesArray[i];
					if (oldVertexLabelsArray!=null)
						oldVertexLabelsArray[curr]=oldVertexLabelsArray[i];
					if (oldVertexNormalsArray!=null)
						oldVertexNormalsArray[curr]=oldVertexNormalsArray[i];
					if (oldVertexSizeArray!=null)
						oldVertexSizeArray[curr]=oldVertexSizeArray[i];
					if (oldVertexTextureCoordsArray!=null)
						oldVertexTextureCoordsArray[curr]=oldVertexTextureCoordsArray[i];
					curr++;
				}
			}	
		}
		int numOfVerticesNew = curr;
		
		// Die VertexAttributVektoren kuerzen		
		double[][] newVertexColorArray= 		new double[numOfVerticesNew][3];
		double[][] newVertexCoordsArray= 		new double[numOfVerticesNew][3];
		String[]   newVertexLabelsArray= 		new String[numOfVerticesNew];
		double[][] newVertexNormalsArray= 		new double[numOfVerticesNew][3];
		double[][] newVertexTextureCoordsArray= new double[numOfVerticesNew][];
		double[]   newVertexSizeArray= 			new double[numOfVerticesNew];
		int[][]    newVertexIndizeesArray= 		new int[numOfVerticesNew][];
		
		for(int i=0;i<numOfVerticesNew;i++){
			if (oldVertexCoordsArray!=null)			newVertexCoordsArray[i]=oldVertexCoordsArray[i];
			if (oldVertexColorArray!=null)			newVertexColorArray[i]=oldVertexColorArray[i];
			if (oldVertexIndizeesArray!=null)		newVertexIndizeesArray[i]=oldVertexIndizeesArray[i];
			if (oldVertexLabelsArray!=null)			newVertexLabelsArray[i]=oldVertexLabelsArray[i];
			if (oldVertexNormalsArray!=null)		newVertexNormalsArray[i]=oldVertexNormalsArray[i];
			if (oldVertexSizeArray!=null)			newVertexSizeArray[i]=oldVertexSizeArray[i];
			if (oldVertexTextureCoordsArray!=null)	newVertexTextureCoordsArray[i]=oldVertexTextureCoordsArray[i];
		}
						
		// Die Vertex Attribute wieder einfuegen
		IndexedFaceSet result=new IndexedFaceSet();
		result.setNumPoints(numOfVerticesNew);
		
		if (numOfVerticesNew>0){
			if (oldVertexCoordsArray!=null){
				System.out.println("coords");
				System.out.println("vorm update VertAttr:"+result.getVertexAttributes().toString());
				result.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
				System.out.println("vorm update VertAttr:"+result.getVertexAttributes().toString());
			}
			if (oldVertexColorArray!=null){
				System.out.println("color");
				//result.setVertexColors(newVertexColorArray);
				result.setVertexAttributes(Attribute.COLORS, new DoubleArrayArray.Array(newVertexColorArray));
			}
			if (oldVertexLabelsArray!=null){
				System.out.println("labels");
				result.setVertexAttributes(Attribute.LABELS, new StringArray(newVertexLabelsArray));
				//result.setVertexLabels(newVertexLabelsArray);
			}
			if (oldVertexNormalsArray!=null){
				System.out.println("normals");
				result.setVertexAttributes(Attribute.NORMALS, new DoubleArrayArray.Array(newVertexNormalsArray));
				//result.setVertexNormals(newVertexNormalsArray);
			}
			if (oldVertexTextureCoordsArray!=null){
				System.out.println("texture");
				result.setVertexAttributes(Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
				//result.setVertexTextureCoordinates(newVertexTextureCoordsArray);
			}
			if (oldVertexSizeArray!=null){
				System.out.println("size");
				result.setVertexAttributes(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
				//result.setVertexAttribute(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
			}
			if (oldVertexIndizeesArray!=null){
				System.out.println("indicees");
				result.setVertexAttributes(Attribute.INDICES, new IntArrayArray.Array(newVertexIndizeesArray));
				//result.setVertexAttribute(Attribute.INDICES,new IntArrayArray.Array(newVertexIndizeesArray));
			}
		}
		
		// uebernehmen der alten Attribute
		int numOfEdges		=ifs.getNumEdges();
		int numOfFaces		=ifs.getNumFaces();
		result.setNumEdges(numOfEdges);
		result.setNumFaces(numOfFaces);
		
		result.setGeometryAttributes(ifs.getGeometryAttributes());
		result.setEdgeAttributes(ifs.getEdgeAttributes());
		result.setFaceAttributes(ifs.getFaceAttributes());
		
		int [][] faceIndicesOld=null;
		int [][] edgeIndicesOld=null;
		temp=ifs.getFaceAttributes( Attribute.INDICES );
		if (temp !=null)	faceIndicesOld = temp.toIntArrayArray(null);
		temp=ifs.getEdgeAttributes( Attribute.INDICES );
		if (temp !=null)	edgeIndicesOld = temp.toIntArrayArray(null);

		// die Indices angleichen		
		int [][] faceIndicesNew= makeNewIndicees(faceIndicesOld,refferenceTabel);
		int [][] edgesIndicesNew=makeNewIndicees(edgeIndicesOld,refferenceTabel);
		if((numOfEdges>0)&(numOfVertices>0))
			result.setEdgeAttributes(Attribute.INDICES, new IntArrayArray.Array(edgesIndicesNew));
		if((numOfFaces>0)&(numOfVertices>0))
			result.setFaceAttributes(Attribute.INDICES, new IntArrayArray.Array(faceIndicesNew));
		return result;		
	}
	public static IndexedLineSet removeDublicatePoints(IndexedLineSet ils){
		// die alten Daten auslesen	
		double [][] oldVertexCoordsArray=null;
		double[][] oldVertexColorArray=null;
		int[][]    oldVertexIndizeesArray=null;
		String[]   oldVertexLabelsArray=null;
		double[][] oldVertexNormalsArray=null;
		double[]   oldVertexSizeArray= null;
		double[][] oldVertexTextureCoordsArray=null;
				
		DataList temp= ils.getVertexAttributes ( Attribute.COORDINATES );
		if (temp!=null) oldVertexCoordsArray 		= temp.toDoubleArrayArray(null);
		temp= ils.getVertexAttributes ( Attribute.COLORS );
		if (temp!=null)	oldVertexColorArray 		= temp.toDoubleArrayArray(null);
		temp=ils.getVertexAttributes ( Attribute.INDICES );
		if (temp !=null)oldVertexIndizeesArray 		= temp.toIntArrayArray(null);
		temp= ils.getVertexAttributes( Attribute.LABELS );
		if (temp!=null)	oldVertexLabelsArray 		= temp.toStringArray(null);
		temp= ils.getVertexAttributes( Attribute.NORMALS );
		if (temp!=null) oldVertexNormalsArray 		= temp.toDoubleArrayArray(null);
		temp= ils.getVertexAttributes( Attribute.POINT_SIZE);
		if (temp!=null) oldVertexSizeArray 			= temp.toDoubleArray(null);
		temp= ils.getVertexAttributes( Attribute.TEXTURE_COORDINATES );
		if (temp!=null) oldVertexTextureCoordsArray = temp.toDoubleArrayArray(null);
		
		int numOfVertices	=ils.getNumPoints();
		
		// refferenceTable.[i] verweist auf den neuen i.Index (fuer umindizierung)
		int[] refferenceTabel =new int[numOfVertices];
		
		// hier werden die Punkte neu gelesen und die Verweise in RefferenceTable gemerkt
		// neue Attribute der Punkte zwischenspeichern:
		int curr=0; // : aktuell einzufuegender Index 
		int index;
		if (numOfVertices>0){
			for (int i=0; i<numOfVertices;i++){
				// benutze durchgelaufenen Teil der Datenliste fuer neue Daten 
				index=searchIndex(oldVertexCoordsArray,oldVertexCoordsArray[i],curr);
				refferenceTabel[i]=index; //Indizes vermerken 
				//System.out.println("reff["+i+"]="+index);
				if(curr==index){
					oldVertexCoordsArray[curr]=oldVertexCoordsArray[i];
					if (oldVertexColorArray!=null)
						oldVertexColorArray[curr]=oldVertexColorArray[i];
					if (oldVertexIndizeesArray!=null)
						oldVertexIndizeesArray[curr]=oldVertexIndizeesArray[i];
					if (oldVertexLabelsArray!=null)
						oldVertexLabelsArray[curr]=oldVertexLabelsArray[i];
					if (oldVertexNormalsArray!=null)
						oldVertexNormalsArray[curr]=oldVertexNormalsArray[i];
					if (oldVertexSizeArray!=null)
						oldVertexSizeArray[curr]=oldVertexSizeArray[i];
					if (oldVertexTextureCoordsArray!=null)
						oldVertexTextureCoordsArray[curr]=oldVertexTextureCoordsArray[i];
					curr++;
				}
			}	
		}
		int numOfVerticesNew = curr;
		
		// Die VertexAttributVektoren kuerzen		
		double[][] newVertexColorArray= 		new double[numOfVerticesNew][3];
		double[][] newVertexCoordsArray= 		new double[numOfVerticesNew][3];
		String[]   newVertexLabelsArray= 		new String[numOfVerticesNew];
		double[][] newVertexNormalsArray= 		new double[numOfVerticesNew][3];
		double[][] newVertexTextureCoordsArray= new double[numOfVerticesNew][];
		double[]   newVertexSizeArray= 			new double[numOfVerticesNew];
		int[][]    newVertexIndizeesArray= 		new int[numOfVerticesNew][];
		
		for(int i=0;i<numOfVerticesNew;i++){
			if (oldVertexCoordsArray!=null)			newVertexCoordsArray[i]=oldVertexCoordsArray[i];
			if (oldVertexColorArray!=null)			newVertexColorArray[i]=oldVertexColorArray[i];
			if (oldVertexIndizeesArray!=null)		newVertexIndizeesArray[i]=oldVertexIndizeesArray[i];
			if (oldVertexLabelsArray!=null)			newVertexLabelsArray[i]=oldVertexLabelsArray[i];
			if (oldVertexNormalsArray!=null)		newVertexNormalsArray[i]=oldVertexNormalsArray[i];
			if (oldVertexSizeArray!=null)			newVertexSizeArray[i]=oldVertexSizeArray[i];
			if (oldVertexTextureCoordsArray!=null)	newVertexTextureCoordsArray[i]=oldVertexTextureCoordsArray[i];
		}
						
		// Die Vertex Attribute wieder einfuegen
		IndexedLineSet result=new IndexedLineSet();
		result.setNumPoints(numOfVerticesNew);
		
		if (numOfVerticesNew>0){
			if (oldVertexCoordsArray!=null){
				System.out.println("coords");
				System.out.println("vorm update VertAttr:"+result.getVertexAttributes().toString());
				result.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
				System.out.println("vorm update VertAttr:"+result.getVertexAttributes().toString());
			}
			if (oldVertexColorArray!=null){
				System.out.println("color");
				//result.setVertexColors(newVertexColorArray);
				result.setVertexAttributes(Attribute.COLORS, new DoubleArrayArray.Array(newVertexColorArray));
			}
			if (oldVertexLabelsArray!=null){
				System.out.println("labels");
				result.setVertexAttributes(Attribute.LABELS, new StringArray(newVertexLabelsArray));
				//result.setVertexLabels(newVertexLabelsArray);
			}
			if (oldVertexNormalsArray!=null){
				System.out.println("normals");
				result.setVertexAttributes(Attribute.NORMALS, new DoubleArrayArray.Array(newVertexNormalsArray));
				//result.setVertexNormals(newVertexNormalsArray);
			}
			if (oldVertexTextureCoordsArray!=null){
				System.out.println("texture");
				result.setVertexAttributes(Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
				//result.setVertexTextureCoordinates(newVertexTextureCoordsArray);
			}
			if (oldVertexSizeArray!=null){
				System.out.println("size");
				result.setVertexAttributes(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
				//result.setVertexAttribute(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
			}
			if (oldVertexIndizeesArray!=null){
				System.out.println("indicees");
				result.setVertexAttributes(Attribute.INDICES, new IntArrayArray.Array(newVertexIndizeesArray));
				//result.setVertexAttribute(Attribute.INDICES,new IntArrayArray.Array(newVertexIndizeesArray));
			}
		}
		
		// uebernehmen der alten Attribute
		int numOfEdges		=ils.getNumEdges();
		result.setNumEdges(numOfEdges);
		
		result.setGeometryAttributes(ils.getGeometryAttributes());
		result.setEdgeAttributes(ils.getEdgeAttributes());
		
		int [][] edgeIndicesOld=null;
		temp=ils.getEdgeAttributes( Attribute.INDICES );
		if (temp !=null)	edgeIndicesOld = temp.toIntArrayArray(null);

		// die Indices angleichen		
		int [][] edgesIndicesNew=makeNewIndicees(edgeIndicesOld,refferenceTabel);
		if((numOfEdges>0)&(numOfVertices>0))
			result.setEdgeAttributes(Attribute.INDICES, new IntArrayArray.Array(edgesIndicesNew));
		return result;		
	}
	public static PointSet removeDublicatePoints(PointSet ps){
		// die alten Daten auslesen	
		double [][] oldVertexCoordsArray=null;
		double[][] oldVertexColorArray=null;
		int[][]    oldVertexIndizeesArray=null;
		String[]   oldVertexLabelsArray=null;
		double[][] oldVertexNormalsArray=null;
		double[]   oldVertexSizeArray= null;
		double[][] oldVertexTextureCoordsArray=null;
				
		DataList temp= ps.getVertexAttributes ( Attribute.COORDINATES );
		if (temp!=null) oldVertexCoordsArray 		= temp.toDoubleArrayArray(null);
		temp= ps.getVertexAttributes ( Attribute.COLORS );
		if (temp!=null)	oldVertexColorArray 		= temp.toDoubleArrayArray(null);
		temp=ps.getVertexAttributes ( Attribute.INDICES );
		if (temp !=null)oldVertexIndizeesArray 		= temp.toIntArrayArray(null);
		temp= ps.getVertexAttributes( Attribute.LABELS );
		if (temp!=null)	oldVertexLabelsArray 		= temp.toStringArray(null);
		temp= ps.getVertexAttributes( Attribute.NORMALS );
		if (temp!=null) oldVertexNormalsArray 		= temp.toDoubleArrayArray(null);
		temp= ps.getVertexAttributes( Attribute.POINT_SIZE);
		if (temp!=null) oldVertexSizeArray 			= temp.toDoubleArray(null);
		temp= ps.getVertexAttributes( Attribute.TEXTURE_COORDINATES );
		if (temp!=null) oldVertexTextureCoordsArray = temp.toDoubleArrayArray(null);
		
		int numOfVertices	=ps.getNumPoints();
		
		
		// hier werden die Punkte neu gelesen und die Verweise in RefferenceTable gemerkt
		// neue Attribute der Punkte zwischenspeichern:
		int curr=0; // : aktuell einzufuegender Index 
		int index;
		if (numOfVertices>0){
			for (int i=0; i<numOfVertices;i++){
				// benutze durchgelaufenen Teil der Datenliste fuer neue Daten 
				index=searchIndex(oldVertexCoordsArray,oldVertexCoordsArray[i],curr);
				if(curr==index){
					oldVertexCoordsArray[curr]=oldVertexCoordsArray[i];
					if (oldVertexColorArray!=null)
						oldVertexColorArray[curr]=oldVertexColorArray[i];
					if (oldVertexIndizeesArray!=null)
						oldVertexIndizeesArray[curr]=oldVertexIndizeesArray[i];
					if (oldVertexLabelsArray!=null)
						oldVertexLabelsArray[curr]=oldVertexLabelsArray[i];
					if (oldVertexNormalsArray!=null)
						oldVertexNormalsArray[curr]=oldVertexNormalsArray[i];
					if (oldVertexSizeArray!=null)
						oldVertexSizeArray[curr]=oldVertexSizeArray[i];
					if (oldVertexTextureCoordsArray!=null)
						oldVertexTextureCoordsArray[curr]=oldVertexTextureCoordsArray[i];
					curr++;
				}
			}	
		}
		int numOfVerticesNew = curr;
		
		// Die VertexAttributVektoren kuerzen		
		double[][] newVertexColorArray= 		new double[numOfVerticesNew][3];
		double[][] newVertexCoordsArray= 		new double[numOfVerticesNew][3];
		String[]   newVertexLabelsArray= 		new String[numOfVerticesNew];
		double[][] newVertexNormalsArray= 		new double[numOfVerticesNew][3];
		double[][] newVertexTextureCoordsArray= new double[numOfVerticesNew][];
		double[]   newVertexSizeArray= 			new double[numOfVerticesNew];
		int[][]    newVertexIndizeesArray= 		new int[numOfVerticesNew][];
		
		for(int i=0;i<numOfVerticesNew;i++){
			if (oldVertexCoordsArray!=null)			newVertexCoordsArray[i]=oldVertexCoordsArray[i];
			if (oldVertexColorArray!=null)			newVertexColorArray[i]=oldVertexColorArray[i];
			if (oldVertexIndizeesArray!=null)		newVertexIndizeesArray[i]=oldVertexIndizeesArray[i];
			if (oldVertexLabelsArray!=null)			newVertexLabelsArray[i]=oldVertexLabelsArray[i];
			if (oldVertexNormalsArray!=null)		newVertexNormalsArray[i]=oldVertexNormalsArray[i];
			if (oldVertexSizeArray!=null)			newVertexSizeArray[i]=oldVertexSizeArray[i];
			if (oldVertexTextureCoordsArray!=null)	newVertexTextureCoordsArray[i]=oldVertexTextureCoordsArray[i];
		}
						
		// Die Vertex Attribute wieder einfuegen
		PointSet result=new PointSet();
		result.setNumPoints(numOfVerticesNew);
		
		if (numOfVerticesNew>0){
			if (oldVertexCoordsArray!=null){
				System.out.println("coords");
				System.out.println("vorm update VertAttr:"+result.getVertexAttributes().toString());
				result.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
				System.out.println("vorm update VertAttr:"+result.getVertexAttributes().toString());
			}
			if (oldVertexColorArray!=null){
				System.out.println("color");
				//result.setVertexColors(newVertexColorArray);
				result.setVertexAttributes(Attribute.COLORS, new DoubleArrayArray.Array(newVertexColorArray));
			}
			if (oldVertexLabelsArray!=null){
				System.out.println("labels");
				result.setVertexAttributes(Attribute.LABELS, new StringArray(newVertexLabelsArray));
				//result.setVertexLabels(newVertexLabelsArray);
			}
			if (oldVertexNormalsArray!=null){
				System.out.println("normals");
				result.setVertexAttributes(Attribute.NORMALS, new DoubleArrayArray.Array(newVertexNormalsArray));
				//result.setVertexNormals(newVertexNormalsArray);
			}
			if (oldVertexTextureCoordsArray!=null){
				System.out.println("texture");
				result.setVertexAttributes(Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
				//result.setVertexTextureCoordinates(newVertexTextureCoordsArray);
			}
			if (oldVertexSizeArray!=null){
				System.out.println("size");
				result.setVertexAttributes(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
				//result.setVertexAttribute(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
			}
			if (oldVertexIndizeesArray!=null){
				System.out.println("indicees");
				result.setVertexAttributes(Attribute.INDICES, new IntArrayArray.Array(newVertexIndizeesArray));
				//result.setVertexAttribute(Attribute.INDICES,new IntArrayArray.Array(newVertexIndizeesArray));
			}
		}
		
		// uebernehmen der alten Attribute		
		result.setGeometryAttributes(ps.getGeometryAttributes());
		return result;		
	}
}
