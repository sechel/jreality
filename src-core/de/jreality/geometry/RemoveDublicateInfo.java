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

import java.util.LinkedList;
import java.util.List;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StringArray;

public class RemoveDublicateInfo {
	public static double eps= 0.000001;
	
	private static boolean compare(double[] p1,double[] p2,double eps){
				// vergleicht Punkte in R^n bis auf eps als Tolleranz
		double delta= 0;
		for (int i=0;i<p1.length;i++)
			delta+=(p1[i]-p2[i])*(p1[i]-p2[i]);
		return (delta<eps*eps);
	}
	private static int[][] makeNewIndicees(int [][] indicesOld,int [] refference){
		int len=indicesOld.length;
		int[][] indicesNew= new int[len][];
		int k=0;
		for (int i=0;i<len;i++){
			k=indicesOld[i].length;
			indicesNew[i]=new int[k];
			for (int j=0;j<k;j++){
				indicesNew[i][j]=refference[indicesOld[i][j]];
			}
		}
		return indicesNew;
	}
	
	
	/** retains only vertices which differs enough in the given
	 * attributes. 
	 * <i>enough</i> means the euclidean distanz is smaler than <code>eps</code> 
	 * retains only the standard Vertex Attributes.
	 * face- and edge- attributes stay the same.
	 * only Face and Edge Indices changes.
	 * @param ps       can be <code>IndexedFaceSet,IndexedLineSet or PointSet</code>
	 * @param atts	   some <code>doubleArrayArrayAttributes</code> 
	 * @return IndexedFaceSet  
	 */
	public static IndexedFaceSet removeDublicateVertices(PointSet ps, Attribute ... atts){
		IndexedFaceSet ifs= IndexedFaceSetUtility.pointSetToIndexedFaceSet(ps);
		List<Attribute> attrs=new LinkedList<Attribute>();
		for (int i = 0; i < atts.length; i++) {
			attrs.add(atts[i]);
		}
		if(!attrs.contains(Attribute.COORDINATES))// Koordinaten muessen dabei sein!
			attrs.add(Attribute.COORDINATES);
		int numOfVertices	=ifs.getNumPoints();
		
		// compareData [Attr][Vertex][dim]
		List<double[][]> compareDataTemp = new LinkedList<double[][]>();
		List<Attribute> goodAttrs = new LinkedList<Attribute>();
		
		//compareData auslesen und nur funktionierende Attribute merken:
		int totalDim=0;			// gesammelte dimension der zu vergl. Attribute
		for(Attribute a:attrs){
			try {
				double[][] temp=ifs.getVertexAttributes (a).toDoubleArrayArray(null);
				int dim=temp[0].length;
				compareDataTemp.add(temp);
				totalDim+=dim;
				goodAttrs.add(a);
			}catch (Exception e) {}
		}
		int numOfAttr=goodAttrs.size();
		// compareData[vertex][attr][dim]
		double[][][] compareData = new double[numOfVertices][numOfAttr][];
		for (int i = 0; i < numOfAttr; i++) { // change sizing
			for (int j = 0; j < numOfVertices; j++) {
				compareData[j][i]= compareDataTemp.get(i)[j];
			}
		}
		
		// die alten Daten auslesen	
		int[][]    oldVertexIndizeesArray=null;
		String[]   oldVertexLabelsArray=null;
		
		DataList temp;
		temp=ifs.getVertexAttributes ( Attribute.INDICES );
		if (temp !=null)oldVertexIndizeesArray 		= temp.toIntArrayArray(null);
		temp= ifs.getVertexAttributes( Attribute.LABELS );
		if (temp!=null)	oldVertexLabelsArray 		= temp.toStringArray(null);
		
		// anders regeln!!! <<=>---<<<
		double [][] oldVertexCoordsArray=null;
		double[][] oldVertexColorArray=null;
		double[][] oldVertexNormalsArray=null;
		double[]   oldVertexSizeArray= null;
		double[][] oldVertexTextureCoordsArray=null;
		temp= ifs.getVertexAttributes( Attribute.NORMALS );
		if (temp!=null) oldVertexNormalsArray 		= temp.toDoubleArrayArray(null);
		temp= ifs.getVertexAttributes( Attribute.POINT_SIZE);
		if (temp!=null) oldVertexSizeArray 			= temp.toDoubleArray(null);
		temp= ifs.getVertexAttributes( Attribute.TEXTURE_COORDINATES );
		if (temp!=null) oldVertexTextureCoordsArray = temp.toDoubleArrayArray(null);
		temp= ifs.getVertexAttributes ( Attribute.COORDINATES );
		if (temp!=null) oldVertexCoordsArray 		= temp.toDoubleArrayArray(null);
		temp= ifs.getVertexAttributes ( Attribute.COLORS );
		if (temp!=null)	oldVertexColorArray 		= temp.toDoubleArrayArray(null);
		
		
		// refferenceTable.[i] verweist auf den neuen i.Index (fuer umindizierung)
		int[] refferenceTabel =new int[numOfVertices];
		
		// hier werden die Punkte neu gelesen und die Verweise in RefferenceTable gemerkt
		// neue Attribute der Punkte zwischenspeichern:
		int curr=0; // : aktuell einzufuegender Index 
		int index;
 		DimTreeStart dTree=new RemoveDublicateInfo().new DimTreeStart(totalDim);

		if (numOfVertices>0){
			for (int i=0; i<numOfVertices;i++){
				// Trick :benutze durchgelaufenen Teil der Datenliste fuer neue Daten
				index=dTree.put(compareData[i]);	// pruefe ob Vertex doppelt 
				refferenceTabel[i]=index; //Indizes vermerken 
				if(curr==index){
					// nur notwendige Daten uebertragen: 
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
		double[][] newVertexColorArray= 		new double[numOfVerticesNew][];
		double[][] newVertexCoordsArray= 		new double[numOfVerticesNew][];
		String[]   newVertexLabelsArray= 		new String[numOfVerticesNew];
		double[][] newVertexNormalsArray= 		new double[numOfVerticesNew][];
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
		
		// die Indices angleichen:		
		int [][] faceIndicesOld=null;
		int [][] edgeIndicesOld=null;
		temp=ifs.getFaceAttributes( Attribute.INDICES );
		if (temp !=null){
			faceIndicesOld = temp.toIntArrayArray(null);
			int [][] faceIndicesNew= makeNewIndicees(faceIndicesOld,refferenceTabel);
			if((numOfFaces>0)&(numOfVertices>0))
				result.setFaceAttributes(Attribute.INDICES, new IntArrayArray.Array(faceIndicesNew));
				}
		temp=ifs.getEdgeAttributes( Attribute.INDICES );
		if (temp !=null){
			edgeIndicesOld = temp.toIntArrayArray(null);
			int [][] edgesIndicesNew=makeNewIndicees(edgeIndicesOld,refferenceTabel);
			if((numOfEdges>0)&(numOfVertices>0))
				result.setEdgeAttributes(Attribute.INDICES, new IntArrayArray.Array(edgesIndicesNew));
			}
		return result;		
	}
		
	private class DimTreeStart{
		private int DimTreeCurrNumToGive;
//      private static double[] DimTreeTolerance; //[dim]
		private int totalDim;
		DimTree d;
		public DimTreeStart(int dim) {
			double[] tol= new double[dim];
			for (int i=0;i<dim;i++)
				tol[i]=0.0001;
			DimTreeCurrNumToGive=0;
			totalDim=dim;	
		}
		public int put(double[][] a){
			// bei beginn initialisieren:
			if (d==null){
				d=new DimTree(a,this);
				return 0;
			}
			int n=d.put(a);
			return n;
		}
	}
	private class DimTree{
		DimTree[] children; // [(dim-1)^2]
		double[][] val;// [attr][dim of attr]
		int number;
		DimTreeStart root;
		
		public DimTree(double[][] value, DimTreeStart rootObject) {
			root= rootObject;
			val=value;
			number=root.DimTreeCurrNumToGive;
			root.DimTreeCurrNumToGive++;
			children= new DimTree[(int)Math.pow(2,root.totalDim)];	
		}
		/**@param a
		 * @return -1   : is the same element
		 *  	   n>=0 : n is the number of the subtree
		 *  				 in which 'a' should be put	 
		 *  a[j] has same size as val[j] (std is 3)*/
		int whichChild(double[][] a){
			int n=0; 		// number of childtree which is to use
			int k=0;		// how many dimensions have i handled bevore
			boolean hit=true;// a is equal to val
			for (int j = 0; j < a.length; j++) {
				if (!(compare(a[j],val[j] , eps)))hit=false;
				// calc n
				for (int i = 0; i < a[j].length; i++) {
					if(a[j][i]>val[j][i]) n+=Math.pow(2, k+i);
				}
				k+=a[j].length;
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
				children[n]=new DimTree(a,root);
				return root.DimTreeCurrNumToGive-1;
			}
			return children[n].put(a);
		}
	}

}
