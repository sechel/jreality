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

/** 
 * @author Bernd Gonska
 */
package de.jreality.geometry;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StringArray;

public class JoinGeometry {
	public static double eps= 0.000001;
	
	private static boolean compare(double[] p1,double[] p2,double eps){
				// vergleicht Punkte in R^n bis auf eps als Tolleranz
		double delta= 0;
		for (int i=0;i<p1.length;i++)
			delta=(p1[i]-p2[i])*(p1[i]-p2[i]);
		return (delta<eps*eps);
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
	
	
	
	
	
	
	
	public static IndexedFaceSet removeRedundantGraphics(IndexedFaceSet ifs, Attribute ... atts){
		// collect all double Attributes
		// collect all int Attributes (without indices)
		
		
		return null;
		
	}
		
	
	
	
	
	
	
	
	/**
	 * entfernt alle doppelt angegebenen Punkte
	 * setzt die Indizees um
	 * erst genannte Punktattribute gelten
	 * @param ifs
	 * @return IndexedFaceSet
	 */
	public static IndexedFaceSet removeDublicateVertices(IndexedFaceSet ifs, Attribute ... atts){
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
		int dim3= atts.length+1;
		
		double[][][] data1=new double[dim3][numOfVertices][3];
		data1[0]=oldVertexCoordsArray;
		for (int i = 0; i < atts.length; i++)
			data1[i+1]=ifs.getVertexAttributes ( atts[i] ).toDoubleArrayArray(null);
		
		double[][][] data= new double[numOfVertices][dim3][3];
		for (int i = 0; i < numOfVertices; i++)
			for (int j = 0; j < dim3; j++)
				data[i][j]=data1[j][i];

		// refferenceTable.[i] verweist auf den neuen i.Index (fuer umindizierung)
		int[] refferenceTabel =new int[numOfVertices];
		
		// hier werden die Punkte neu gelesen und die Verweise in RefferenceTable gemerkt
		// neue Attribute der Punkte zwischenspeichern:
		int curr=0; // : aktuell einzufuegender Index 
		int index;
		DimTreeStart dTree=new JoinGeometry().new DimTreeStart(dim3);

		if (numOfVertices>0){
			for (int i=0; i<numOfVertices;i++){
				// benutze durchgelaufenen Teil der Datenliste fuer neue Daten 
				index=dTree.put(data[i]);
				refferenceTabel[i]=index; //Indizes vermerken 
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
				result.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
			}
			if (oldVertexColorArray!=null){
				System.out.println("color");
				result.setVertexAttributes(Attribute.COLORS, new DoubleArrayArray.Array(newVertexColorArray));
			}
			if (oldVertexLabelsArray!=null){
				System.out.println("labels");
				result.setVertexAttributes(Attribute.LABELS, new StringArray(newVertexLabelsArray));
			}
			if (oldVertexNormalsArray!=null){
				System.out.println("normals");
				result.setVertexAttributes(Attribute.NORMALS, new DoubleArrayArray.Array(newVertexNormalsArray));
			}
			if (oldVertexTextureCoordsArray!=null){
				System.out.println("texture");
				result.setVertexAttributes(Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
			}
			if (oldVertexSizeArray!=null){
				System.out.println("size");
				result.setVertexAttributes(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
			}
			if (oldVertexIndizeesArray!=null){
				System.out.println("indicees");
				result.setVertexAttributes(Attribute.INDICES, new IntArrayArray.Array(newVertexIndizeesArray));
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
	public static IndexedLineSet removeDublicateVertices(IndexedLineSet ils, Attribute ... atts){
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
		
//		 conect data:
		int dim3= atts.length+1;
		
		double[][][] data1=new double[dim3][numOfVertices][3];
		data1[0]=oldVertexCoordsArray;
		for (int i = 0; i < atts.length; i++)
			data1[i+1]=ils.getVertexAttributes ( atts[i] ).toDoubleArrayArray(null);
		
		double[][][] data= new double[numOfVertices][dim3][3];
		for (int i = 0; i < numOfVertices; i++)
			for (int j = 0; j < dim3; j++)
				data[i][j]=data1[j][i];
//		conect data end
		
		// refferenceTable.[i] verweist auf den neuen i.Index (fuer umindizierung)
		int[] refferenceTabel =new int[numOfVertices];
		
		// hier werden die Punkte neu gelesen und die Verweise in RefferenceTable gemerkt
		// neue Attribute der Punkte zwischenspeichern:
		int curr=0; // : aktuell einzufuegender Index 
		int index;
		DimTreeStart dTree=new JoinGeometry().new DimTreeStart(dim3);
		
		if (numOfVertices>0){
			for (int i=0; i<numOfVertices;i++){
				// benutze durchgelaufenen Teil der Datenliste fuer neue Daten 
				index=dTree.put(data[i]);
				refferenceTabel[i]=index; //Indizes vermerken 
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
				result.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
			}
			if (oldVertexColorArray!=null){
				System.out.println("color");
				result.setVertexAttributes(Attribute.COLORS, new DoubleArrayArray.Array(newVertexColorArray));
			}
			if (oldVertexLabelsArray!=null){
				System.out.println("labels");
				result.setVertexAttributes(Attribute.LABELS, new StringArray(newVertexLabelsArray));
			}
			if (oldVertexNormalsArray!=null){
				System.out.println("normals");
				result.setVertexAttributes(Attribute.NORMALS, new DoubleArrayArray.Array(newVertexNormalsArray));
			}
			if (oldVertexTextureCoordsArray!=null){
				System.out.println("texture");
				result.setVertexAttributes(Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
			}
			if (oldVertexSizeArray!=null){
				System.out.println("size");
				result.setVertexAttributes(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
			}
			if (oldVertexIndizeesArray!=null){
				System.out.println("indicees");
				result.setVertexAttributes(Attribute.INDICES, new IntArrayArray.Array(newVertexIndizeesArray));
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
	
	/** removes Vertices which do not differ much in vertexCoordinates
	 *  @param ps
	 *  @param atts  remove no vertices which differ in this attributes
	 *  				this attributes must be of the Type doubleArrayArray
	 * @return
	 */
	public static PointSet removeDublicateVertices(PointSet ps, Attribute ... atts){
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
		
		// conect data:
			int dim3= atts.length+1;
			
			double[][][] data1=new double[dim3][numOfVertices][3];
			data1[0]=oldVertexCoordsArray;
			for (int i = 0; i < atts.length; i++)
				data1[i+1]=ps.getVertexAttributes ( atts[i] ).toDoubleArrayArray(null);
			
			double[][][] data= new double[numOfVertices][dim3][3];
			for (int i = 0; i < numOfVertices; i++)
				for (int j = 0; j < dim3; j++)
					data[i][j]=data1[j][i];

		// conect data end
		
		// hier werden die Punkte neu gelesen und die Verweise in RefferenceTable gemerkt
		// neue Attribute der Punkte zwischenspeichern:
		int curr=0; // : aktuell einzufuegender Index 
		int index;
		DimTreeStart dTree=new JoinGeometry().new DimTreeStart(dim3);
		if (numOfVertices>0){
			for (int i=0; i<numOfVertices;i++){
				// benutze durchgelaufenen Teil der Datenliste fuer neue Daten
				index=dTree.put(data[i]);
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
				result.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
			}
			if (oldVertexColorArray!=null){
				System.out.println("color");
				result.setVertexAttributes(Attribute.COLORS, new DoubleArrayArray.Array(newVertexColorArray));
			}
			if (oldVertexLabelsArray!=null){
				System.out.println("labels");
				result.setVertexAttributes(Attribute.LABELS, new StringArray(newVertexLabelsArray));
			}
			if (oldVertexNormalsArray!=null){
				System.out.println("normals");
				result.setVertexAttributes(Attribute.NORMALS, new DoubleArrayArray.Array(newVertexNormalsArray));
			}
			if (oldVertexTextureCoordsArray!=null){
				System.out.println("texture");
				result.setVertexAttributes(Attribute.TEXTURE_COORDINATES, new DoubleArrayArray.Array(newVertexCoordsArray));
			}
			if (oldVertexSizeArray!=null){
				System.out.println("size");
				result.setVertexAttributes(Attribute.POINT_SIZE, new DoubleArray(newVertexSizeArray));
			}
			if (oldVertexIndizeesArray!=null){
				System.out.println("indicees");
				result.setVertexAttributes(Attribute.INDICES, new IntArrayArray.Array(newVertexIndizeesArray));
			}
		}
		
		// uebernehmen der alten Attribute		
		result.setGeometryAttributes(ps.getGeometryAttributes());
		return result;		
	}
	
	
	private static int DimTreeCurrNumToGive;
	private static double[] DimTreeTolerance; //[dim]
	private static int DimTreeDim;
	private class DimTreeStart{
		DimTree d;
		public DimTreeStart(int dim) {
			double[] tol= new double[dim];
			for (int i=0;i<dim;i++)
				tol[i]=0.0001;
			new DimTreeStart(dim,tol);
		}
		public DimTreeStart(int dim,double[] tol) {
			DimTreeCurrNumToGive=0;
			DimTreeDim=dim;
			DimTreeTolerance=tol;
		}
		public int put(double[][] a){
			if (d==null){
				d=new DimTree(a);
				return 0;
			}
		int n=d.put(a);
		return n;
		}
	}
	private class DimTree{
		DimTree[] children; // [(dim-1)^2]
		double[][] val;// [dim]
		int number;

		public DimTree(double[][] value) {
			val=value;
			number=DimTreeCurrNumToGive;
			DimTreeCurrNumToGive++;
			children= new DimTree[(int)Math.pow(DimTreeDim*3,2)];
		}
		/**@param a
		 * @return -1   : is the same element
		 *  	   n>=0 : n is the number of the subtree
		 *  				 in which a should be put	 */
		int whichChild(double[][] a){
			int n=0; 		 // number of childtree which is to use
			boolean hit=true;// a is equal to val
			for (int j = 0; j < DimTreeDim; j++) {
				if (!(compare(a[j],val[j] , eps)))hit=false;
				for (int i = 0; i < 3; i++)
					if(a[j][i]>val[j][i]) n+=Math.pow(2, j*3+i);
			}
			return (hit)? -1 : n;
		}
		/**@param a
		 * @return :  new counting number
		 * 		   :  number of existing doubled value
		 */
		int put(double[][] a){
			int n=whichChild(a);
			if (n==-1) return number;
			if (children[n]==null){
				children[n]=new DimTree(a);
				return DimTreeCurrNumToGive-1;
			}
			return children[n].put(a);
		}
	}

}
