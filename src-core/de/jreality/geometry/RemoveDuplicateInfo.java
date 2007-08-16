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
import java.util.Set;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StringArray;
import de.jreality.scene.data.StringArrayArray;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;

public class RemoveDuplicateInfo {
	

	/** retains only vertices which differs enough in the coordinstes. 
	 * <i>enough</i> means the distance in the three directions is smaler than <code>eps</code> 
	 * retains the following Vertex Attribute Datatypes in the reduced way:
	 *  String-,Double- and Int-, -Array and -ArrayArray  
	 * face- and edge- attributes stay the same.
	 * only Face and Edge Indices changes.
	 * Remark:
	 *  In some rare cases(many near Vertices) Vertices 
	 *   within eps distance do not collapse to one.
	 *  and in some cases Vertices with euclidean
	 *   distance up to <i>5.2*eps</i> could be merged. 
	 * Remark: The GeometryAttribute 
	 * 			<code>quadmesh</code> will be deleted
	 * Remark: some other Attributes may collide with the new Geometry
	 * 
	 * @param ps       can be <code>IndexedFaceSet,IndexedLineSet or PointSet</code>
	 * @param atts	   this Attributes must be DoubleArray or DoubleArrayArray Attributes 
	 * 					(others will be ignored) they will be respected by testing equality of Vertices.
	 * @return IndexedFaceSet  
	 */
////---------- new start-----------------
	private int[] refferenceTable;
	private int[] mergeRefferenceTable;
	private int[] removeRefferenceTable;
	private int[] sublistTable;
	
	
	private IndexedFaceSet source;
	private IndexedFaceSet geo= new IndexedFaceSet();
	private double[][] points; // the vertices
	private double[][] attrVals; // the vertices
	
	private double eps; // Tolereanz for merging
	private int dim;// =points[x].length
	private int maxPointPerBoxCount=50;
	private int numSubBoxes;
	private int numNewVerts;
	// constr ----------------------------------------
	private RemoveDuplicateInfo(IndexedFaceSet ifs, Attribute ...attributes ){
		source=ifs;
		points=source.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
	}
	// methods----------------------------------------
	public static IndexedFaceSet removeDuplicateVertices(PointSet ps, Attribute ...attributes ) {
		return removeDuplicateVertices(ps,0.00000001,attributes);		
	}
	public static IndexedFaceSet removeDuplicateVertices(PointSet ps, double eps, Attribute ...attributes ) {
		IndexedFaceSet ifs= IndexedFaceSetUtility.pointSetToIndexedFaceSet(ps);		
		// inittialize some data
		RemoveDuplicateInfo r= new RemoveDuplicateInfo(ifs);
		{
			// TODO: make better output
			if(r.points.length==0) return null;
			if(r.points.length==1) return null;
		}
		r.eps=Math.max(0, eps);
		r.dim=r.points[0].length;
		r.mergeRefferenceTable=new int[r.points.length];
		for (int i = 0; i < r.points.length; i++) {
			r.mergeRefferenceTable[i]=i;
		}
		r.numSubBoxes=(int)Math.pow(2, r.dim);
		r.readOutAttributes(attributes);
		// create first box:
		Box b=r.fillFirstBox();
		// start sortBox(firstBox):
		r.processBox(b);
		// return:
		r.postCalulation();
		return r.geo;
	} 
	/** read out the attributes, which are given by the user, 
	 * to be compared for equality.
	 * @param attributes
	 */
	private void readOutAttributes(Attribute ... attributes ){
		List<double[][]> DAAList= new LinkedList<double[][]>();
		List<double[]> DAList= new LinkedList<double[]>();
		int dim= 0;
		// sort and remember meaningfull Atributes
		for(Attribute at: attributes){
			if(at.getName().equals(Attribute.COORDINATES.getName()))continue;
			DataList currDL=source.getVertexAttributes(at);
			if(currDL== null)continue;
			if(currDL instanceof DoubleArrayArray){
				if(currDL.item(0)== null)continue;
				DAAList.add(currDL.toDoubleArrayArray(null));
				dim+=currDL.item(0).size();
			}
			if(currDL instanceof DoubleArray){
				DAList.add(currDL.toDoubleArray(null));
				dim++;
			}
		}
		// put the doubles into an array
		attrVals= new double[source.getNumPoints()][dim];
		int dimCount=0;
		for (double[][] daa: DAAList) {
			int currDim=daa[0].length; 
			for (int i = 0; i < daa.length; i++) 
				System.arraycopy(daa[i], 0, attrVals[i], dimCount, currDim);
			dimCount+=currDim;
		}
		for (double[] da: DAList) {
			for (int i = 0; i < da.length; i++)
				attrVals[i][dimCount]=da[i];
			dimCount++;
		}
	}
	
	/** inserts all points in the first Box	 */
	private Box fillFirstBox(){// finished
		double[] max= new double[dim];
		double[] min= new double[dim];
		for (int i = 0; i < dim; i++) 
			min[i]=max[i]=points[0][i];
		for (int i = 1; i < points.length; i++) {
			for (int j = 0; j < dim; j++) {
				double[] p=points[i];
				if(p[j]>=max[j]) max[j]=p[j];
				if(p[j]<=min[j]) min[j]=p[j];
			}	
		}
		Box b=new Box(max,min,dim);
		for (int i = 0; i < points.length; i++) {
			b.addPointIfPossible(i);
		}
		return b;
	}
	
	
/** fills the refferences 
 * of the Vertices in the Box
 * in the refferenceTable
 */	
	private void processBox(Box b){// finished
		if(b.numOfPoints<=1) return;
		// case of to small Box:
		if(b.getSize()<=(3.0*eps)) {
			compareInBox(b);
			return;
		}
		// recursion case(subdivision needed):
		if(b.numOfPoints>maxPointPerBoxCount){
			Box[] subBoxes = createSubBoxes(b);
			for (int i = 0; i < subBoxes.length; i++) {
				processBox(subBoxes[i]);
			}
			return;
		}
		// comparing:
		compareInBox(b);
	}
	/** indicates if a Point is within the box given by 
	 *  min and max plus an eps. */
	private boolean inBetwen(double[]max,double[]min,double eps, double[] val){// finished
		for (int i = 0; i < val.length; i++) {
			if(val[i]>max[i]+eps) return false;
			if(val[i]<min[i]-eps) return false;
		}
		return true;
	}
	/** compares the points in every important attribute.
	 *  uses the same <code>eps</code> for every attribute.  */
	private boolean isEqualByEps(int p1, int p2){
		double[] c1=points[p1];// coords
		double[] c2=points[p2];
		double[] a1=attrVals[p1];//important double attributes (inlined)
		double[] a2=attrVals[p2];
		if(inBetwen(c1,c1, eps, c2)&&inBetwen(a1,a1, eps, a2))
			return true; 
		return false;
	}
	/** sets the refferences of all Vertices in the box	
	 */ 
	private void compareInBox(Box b) {// finished
		for (int p1: b.innerPoints){
			if(!isLegalPoint(p1)) continue;
			for (int p2: b.innerPoints){
				if(p1>=p2)continue;
				if(!isLegalPoint(p2)) continue;
				if (isEqualByEps(p1, p2))
					mergeRefferenceTable[p2]=p1;
			}
		}
	}
	/** indicates if a point is not refferenced to an other; */
	private boolean isLegalPoint(int p){// finished
		return (mergeRefferenceTable[p]==p);
	}
	
	private Box[] createSubBoxes(Box b) {// finished 
		Box[] result= new Box[numSubBoxes];
		for (int i = 0; i < result.length; i++) {
			// calc max & min:
			double[] min= new double[dim];
			double[] max= new double[dim];
			int k=i;
			for (int d = 0; d < dim; d++) {
				//new
				if(b.realMax[d]-b.realMin[d]<=2*eps){
					max[d]=min[d]=(b.realMax[d]+b.realMin[d])/2;
				}
				if(k%2==0){
					max[d]=(b.realMin[d]+b.realMax[d])/2;
					min[d]=b.realMin[d]+eps;
				}
				else{
					min[d]=(b.realMin[d]+b.realMax[d])/2;
					max[d]=b.realMax[d]-eps;
				}
				// new end
				k = k>>1;
			}
			// make new subBox
			result[i]=new Box(max,min,dim);			
		}
		// insert points
		for(int v: b.innerPoints)
			if(isLegalPoint(v))
				for (int i = 0; i < numSubBoxes; i++) 
					// bei allen probieren 
					result[i].addPointIfPossible(v);
		return result;
	}
	// DataStructures----------------------------------------
	/** holds a bucket full of points.
	 *  this points will be directly compared (O(n^2)) 
	 */
	private class Box{
		int numOfPoints=0; 
		double[] originalMax; // without eps
		double[] originalMin; // without eps
		double[] realMax;
		double[] realMin;
		boolean empty;
		List<Integer> innerPoints= new LinkedList<Integer>();
		/** box with boundarys,
		 *  can be filled with (double[dim]) Points 
		 */
		public Box(double[]max,double[]min,int dim) {// finished
			originalMax=max;
			originalMin=min;
			realMax=new double[dim];
			realMin=new double[dim];
			empty=true;
		}
		/** returnes if a point can be added
		 * (lies within the boundary)
		 */
		public boolean addable(int d){// finished
			double[] p=points[d];
			return inBetwen(originalMax, originalMin, eps, p);
		}
		/** adds a Point to the box if possible 
		 *  updates the real bounding
		 *  returns succes
		 */
		public boolean addPointIfPossible(int point){// finished
			if(!addable(point))return false;
			double[] p=points[point];
			if (empty){
				for (int i = 0; i < dim; i++) {
					realMax[i]=realMin[i]=p[i];
				}
				empty=false;
			}
			else{
				for (int i = 0; i < dim; i++) {
					if(p[i]>=realMax[i]) realMax[i]=p[i];
					if(p[i]<=realMin[i]) realMin[i]=p[i];
				}
			}
			innerPoints.add(point);
			numOfPoints++;
			return true;
		}
		double getSize(){
			double size=0;
			for (int i = 0; i < dim; i++) 
				if(realMax[i]-realMin[i]>size)
					size=realMax[i]-realMin[i];
			return size;
		}
	}
	
// post calculation -------------------------------------------------------
	
	private void postCalulation(){
		newTables();
		geo.setNumPoints(numNewVerts);		
		geo.setNumFaces(source.getNumFaces());
		geo.setNumEdges(source.getNumEdges());
		newDatalists();
		newIndices();
	} 
	/** calculates refferenceTable 
	 * new Vertices 
	 * (unused Vertices will be taken out) 
	 */
	private void newTables(){
		// remove Table:
		removeRefferenceTable= new int[points.length];
		int numUsedVerts=0;
		int pos=0;
		for (int i = 0; i < points.length; i++)
			if (mergeRefferenceTable[i]==i){
				removeRefferenceTable[i]=numUsedVerts;
				numUsedVerts++;
			}
			else{
				removeRefferenceTable[i]=-1;
			}
		// direct referenceTable:
		refferenceTable= new int[points.length];
		for (int i = 0; i < points.length; i++) {
			refferenceTable[i]=removeRefferenceTable[mergeRefferenceTable[i]];
		}
		numNewVerts=numUsedVerts;
		// sublist Table:
		sublistTable= new int[numUsedVerts];
		pos=0;
		for (int i = 0; i < points.length; i++) 
			if(removeRefferenceTable[i]!=-1){
				sublistTable[pos]=i;
				pos++;
			}
	}
	
	/** 
	 * @param oldRefferences (for ecx.: face indices)
	 * @param refferenceTable (result of start)
	 * @return new refferences (for ecx.: new face indices)
	 */
	private void newIndices(){
		// face Indices
		DataList data=source.getFaceAttributes(Attribute.INDICES);
		if(data!=null && data.size()>0 ){
			int[][] fIndis=data.toIntArrayArray(null);
			int[][] result= new int[fIndis.length][];
			for (int i = 0; i < result.length; i++) {
				result[i]=newIndices(fIndis[i], refferenceTable);
			}
			geo.setFaceAttributes(Attribute.INDICES,new IntArrayArray.Array(result));
		}
		// edge Indices
		data=source.getEdgeAttributes(Attribute.INDICES);
		if(data!=null && data.size()>0 ){
			int[][] eIndis=data.toIntArrayArray(null);
			int[][] result= new int[eIndis.length][];
			for (int i = 0; i < result.length; i++) {
				result[i]=newIndices(eIndis[i], refferenceTable);
			}
			geo.setEdgeAttributes(Attribute.INDICES,new IntArrayArray.Array(result));
		}
	}
	private static int[] newIndices(int[] oldRefferences, int[] refferenceTable){
		int[] result= new int[oldRefferences.length];
		for (int i = 0; i < oldRefferences.length; i++) {
			result[i]=refferenceTable[oldRefferences[i]];
		}
		return result;
	} 
	private void newDatalists() {
		DataListSet datas=source.getVertexAttributes();
		Set<Attribute> atts=(Set<Attribute>) datas.storedAttributes();
		// VertexDatalists
		for(Attribute at : atts){
			DataList dl=datas.getList(at);
			if (dl instanceof DoubleArrayArray) {DoubleArrayArray dd = (DoubleArrayArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
			if (dl instanceof DoubleArray) {DoubleArray dd = (DoubleArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
			if (dl instanceof IntArrayArray) {IntArrayArray dd = (IntArrayArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
			if (dl instanceof IntArray) {IntArray dd = (IntArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
			if (dl instanceof StringArrayArray) {
				StringArrayArray dd = (StringArrayArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
			if (dl instanceof StringArray) {StringArray dd = (StringArray) dl;
				geo.setVertexAttributes(at, RemoveDuplicateInfo.getSublist(dd, sublistTable));
			}
		}
		geo.setEdgeAttributes(source.getEdgeAttributes());
		geo.setFaceAttributes(source.getFaceAttributes());
		geo.setGeometryAttributes(source.getGeometryAttributes());
		geo.setGeometryAttributes("quadMesh",null);
	}
	// getter / setter ---------------------------------------- 
	/** get Tolerance for equality */
	public double getEps() {
		return eps;
	}
	/** set Tolerance for equality*/
	public void setEps(double eps) {
		this.eps = eps;
	}
	public int[] getRefferenceTable() {
		return refferenceTable;
	}
	/** removes vertices which are not used by faces.
	 * changes faceIndices.
	 * @param vertices
	 * @param faces
	 * @return vertices
	 */
	public static double[][] removeNoFaceVertices(double[][] vertices, int[][] faces){
		int numVOld=vertices.length;
		int numF=faces.length;
		boolean[] usedVertices= new boolean[numVOld];
		for (int i = 0; i < numVOld; i++) 
			usedVertices[i]=false;
		// remember all vertices used in faces
		for (int i = 0; i < numF; i++) 
			for (int j = 0; j < faces[i].length; j++) 
				usedVertices[faces[i][j]]=true;	
		int count=0; 
		int[] refferenceTabel= new int[numVOld];
		for (int i = 0; i < numVOld; i++) {
			if(usedVertices[i]){
				refferenceTabel[i]=count;
				vertices[count]=vertices[i];// vertices gleich richtig einschreiben
				count++;
			}
			else{
				refferenceTabel[i]=-1;
			}
		}
		// faces umindizieren
		for (int i = 0; i < numF; i++) 
			for (int j = 0; j < faces[i].length; j++) 
				faces[i][j]=refferenceTabel[faces[i][j]];
		// VertexListe erneuern
		double[][] newVertices= new double[count][];
		System.arraycopy(vertices, 0, newVertices, 0, count);
		return newVertices;
	}
	/** a face definition can repeat the first index at the end  
	 * excample: {1,2,3,4,1} or {1,2,3,4}
	 * in first case: the last index will be removed
	 */
	public static void removeCycleDefinition(int[][] faces){
		for (int i = 0; i < faces.length; i++) {
			int len=faces[i].length;
			if(len>1)
				if(faces[i][len-1]==faces[i][0]){
					int[] newIndis= new int[len-1];
					System.arraycopy(faces[i], 0, newIndis, 0, len-1);
					faces[i]=newIndis;
				}
		}
	}
	// ------------------ sublists -----------------------------
	public static DataList getSublist(DoubleArrayArray dd, int[] referenceTable){
		if(dd.getLength()==0)return dd;
		return getSublist(dd.toDoubleArrayArray(null), referenceTable);
	} 
	public static DataList getSublist(double[][] dd, int[] referenceTable){
		if (dd.length==0)return new DoubleArrayArray.Array(new double[][]{{}});
		int dim=dd[0].length;
		double[][] newList=new double[referenceTable.length][dim];
		for (int i = 0; i < newList.length; i++) 
			for (int j = 0; j < dim; j++) 
				newList[i][j]=dd[referenceTable[i]][j];
		return new DoubleArrayArray.Array(newList);
	} 
	public static DataList getSublist(DoubleArray d, int[] referenceTable){
		if(d.getLength()==0)return d;
		return getSublist(d.toDoubleArray(null), referenceTable);
	} 
	public static DataList getSublist(double[] d, int[] referenceTable){
		if (d.length==0)return new DoubleArray(new double[]{});
		double[] newList=new double[referenceTable.length];
		for (int i = 0; i < newList.length; i++) 
			newList[i]=d[referenceTable[i]];
		return new DoubleArray(newList);
	} 
	public static DataList getSublist(IntArrayArray dd, int[] referenceTable){
		if(dd.getLength()==0)return dd;
		return getSublist(dd.toIntArrayArray(null), referenceTable);
	} 
	public static DataList getSublist(int[][] dd, int[] referenceTable){
		if (dd.length==0)return new IntArrayArray.Array(new int[][]{{}});
		int dim=dd[0].length;
		int[][] newList=new int[referenceTable.length][dim];
		for (int i = 0; i < newList.length; i++) 
			for (int j = 0; j < dim; j++) 
				newList[i][j]=dd[referenceTable[i]][j];
		return new IntArrayArray.Array(newList);
	} 
	public static DataList getSublist(IntArray d, int[] referenceTable){
		if(d.getLength()==0)return d;
		return getSublist(d.toIntArray(null), referenceTable);
	} 
	public static DataList getSublist(int[] d, int[] referenceTable){
		if (d.length==0)return new IntArray(new int[]{});
		int[] newList=new int[referenceTable.length];
		for (int i = 0; i < newList.length; i++) 
			newList[i]=d[referenceTable[i]];
		return new IntArray(newList);
	} 
	public static DataList getSublist(StringArrayArray dd, int[] referenceTable){
		if(dd.getLength()==0)return dd;
		return getSublist(dd.toStringArrayArray(null), referenceTable);
	} 
	public static DataList getSublist(String[][] dd, int[] referenceTable){
		if (dd.length==0)return new StringArrayArray.Array(new String[][]{{}});
		int dim=dd[0].length;
		String[][] newList=new String[referenceTable.length][dim];
		for (int i = 0; i < newList.length; i++) 
			for (int j = 0; j < dim; j++) 
				newList[i][j]=dd[referenceTable[i]][j];
		return new StringArrayArray.Array(newList);
	} 
	public static DataList getSublist(StringArray d, int[] referenceTable){
		if(d.getLength()==0)return d;
		return getSublist(d.toStringArray(null), referenceTable);
	} 
	public static DataList getSublist(String[] d, int[] referenceTable){
		if (d.length==0)return new StringArray(new String[]{});
		String[] newList=new String[referenceTable.length];
		for (int i = 0; i < newList.length; i++) 
			newList[i]=d[referenceTable[i]];
		return new StringArray(newList);
	} 

	public static void main(String[] args) {
		IndexedFaceSetFactory bloed= new IndexedFaceSetFactory();
		bloed.setVertexCount(273);
		bloed.setLineCount(0);
		bloed.setFaceCount(0);
		bloed.setVertexCoordinates( new double[][]
		 {{0.643000 ,1.36645 ,0.816000 ,1.00000  },
				{-0.643000 ,1.36645 ,-0.816000 ,1.00000  },
				{0.214091 ,1.56603 ,0.283867 ,1.00000 },
				{-0.214091 ,1.56603 ,-0.283867 ,1.00000 },
				{1.71409 ,1.43380 ,-0.690836 ,1.00000 },
				{0.428909 ,1.53419 ,-0.423444 ,1.00000 },
				{1.07109 ,1.49008 ,-0.559198 ,1.00000 },
				{0.428909 ,0.876809 ,1.32825 ,1.00000 },
				{0.857000 ,1.58511 ,0.143019 ,1.00000 },
				{-1.71409 ,1.43380 ,0.690836 ,1.00000 },
				{-0.428909 ,1.53419 ,0.423444 ,1.00000 },
				{-1.07109 ,1.49008 ,0.559198 ,1.00000 },
				{-0.428909 ,0.876809 ,-1.32825 ,1.00000 },
				{-0.857000 ,1.58511 ,-0.143019 ,1.00000 },
				{1.07109 ,-0.0713700 ,-1.58995 ,1.00000 },
				{0.643000 ,1.19832 ,-1.04741 ,1.00000 },
				{0.857000 ,0.625845 ,-1.46333 ,1.00000 },
				{2.14300 ,0.625845 ,1.46333 ,1.00000 },
				{1.28591 ,1.43380 ,0.690836 ,1.00000 },
				{1.71409 ,1.10009 ,1.15014 ,1.00000 },
				{-0.214091 ,0.753903 ,1.40166 ,1.00000 },
				{1.07109 ,0.992288 ,1.24435 ,1.00000 },
				{-1.07109 ,-0.0713700 ,1.58995 ,1.00000 },
				{-0.643000 ,1.19832 ,1.04741 ,1.00000 },
				{-0.857000 ,0.625845 ,1.46333 ,1.00000 },
				{-2.14300 ,0.625845 ,-1.46333 ,1.00000 },
				{-1.28591 ,1.43380 ,-0.690836 ,1.00000 },
				{-1.71409 ,1.10009 ,-1.15014 ,1.00000 },
				{0.214091 ,0.753903 ,-1.40166 ,1.00000 },
				{-1.07109 ,0.992288 ,-1.24435 ,1.00000 },
				{2.14300 ,1.58511 ,-0.143019 ,1.00000 },
				{1.28591 ,1.10009 ,-1.15014 ,1.00000 },
				{1.07109 ,-0.0713700 ,-1.58995 ,1.00000 },
				{-0.643000 ,-0.353807 ,-1.55172 ,1.00000 },
				{-0.214091 ,0.213956 ,-1.57710 ,1.00000 },
				{3.21409 ,1.56603 ,0.283867 ,1.00000 },
				{1.92891 ,1.49008 ,0.559198 ,1.00000 },
				{2.57109 ,1.53419 ,0.423444 ,1.00000 },
				{0.428909 ,-0.992288 ,1.24435 ,1.00000 },
				{0.857000 ,0.353807 ,1.55172 ,1.00000 },
				{0.643000 ,-0.353807 ,1.55172 ,1.00000 },
				{0.214091 ,0.213956 ,1.57710 ,1.00000 },
				{-2.14300 ,1.58511 ,0.143019 ,1.00000 },
				{-1.28591 ,1.10009 ,1.15014 ,1.00000 },
				{-3.21409 ,1.56603 ,-0.283867 ,1.00000 },
				{-1.92891 ,1.49008 ,-0.559198 ,1.00000 },
				{-2.57109 ,1.53419 ,-0.423444 ,1.00000 },
				{-0.428909 ,-0.992288 ,-1.24435 ,1.00000 },
				{-0.857000 ,0.353807 ,-1.55172 ,1.00000 },
				{-0.643000 ,-0.353807 ,-1.55172 ,1.00000 },
				{-0.643000 ,-0.353807 ,-1.55172 ,1.00000 },
				{-0.214091 ,0.213956 ,-1.57710 ,1.00000 },
				{3.21409 ,0.753903 ,-1.40166 ,1.00000 },
				{1.92891 ,0.992288 ,-1.24435 ,1.00000 },
				{2.57109 ,0.876809 ,-1.32825 ,1.00000 },
				{2.35700 ,1.36645 ,-0.816000 ,1.00000 },
				{1.71409 ,-0.213956 ,-1.57710 ,1.00000 },
				{-0.214091 ,0.213956 ,-1.57710 ,1.00000 },
				{1.07109 ,-0.0713700 ,-1.58995 ,1.00000 },
				{0.428909 ,0.0713700 ,-1.58995 ,1.00000 },
				{-0.214091 ,0.213956 ,-1.57710 ,1.00000 },
				{1.71409 ,-0.213956 ,-1.57710 ,1.00000 },
				{0.428909 ,0.0713700 ,-1.58995 ,1.00000 },
				{1.07109 ,-0.0713700 ,-1.58995 ,1.00000 },
				{1.92891 ,-0.0713700 ,1.58995 ,1.00000 },
				{2.35700 ,1.19832 ,1.04741 ,1.00000 },
				{2.14300 ,-1.19832 ,1.04741 ,1.00000 },
				{1.28591 ,-0.213956 ,1.57710 ,1.00000 },
				{1.71409 ,-0.753903 ,1.40166 ,1.00000 },
				{-1.71409 ,-0.213956 ,1.57710 ,1.00000 },
				{-0.428909 ,0.0713700 ,1.58995 ,1.00000 },
				{-3.21409 ,0.753903 ,1.40166 ,1.00000 },
				{-1.92891 ,0.992288 ,1.24435 ,1.00000 },
				{-2.57109 ,0.876809 ,1.32825 ,1.00000 },
				{-2.35700 ,1.36645 ,0.816000 ,1.00000 },
				{-1.92891 ,-0.0713700 ,-1.58995 ,1.00000 },
				{-2.35700 ,1.19832 ,-1.04741 ,1.00000 },
				{-1.92891 ,-0.0713700 ,-1.58995 ,1.00000 },
				{-0.643000 ,-0.353807 ,-1.55172 ,1.00000 },
				{-0.857000 ,0.353807 ,-1.55172 ,1.00000 },
				{-0.857000 ,0.353807 ,-1.55172 ,1.00000 },
				{-2.14300 ,-1.19832 ,-1.04741 ,1.00000 },
				{-1.28591 ,-0.213956 ,-1.57710 ,1.00000 },
				{-1.71409 ,-0.753903 ,-1.40166 ,1.00000 },
				{2.57109 ,-0.992288 ,-1.24435 ,1.00000 },
				{2.14300 ,0.353807 ,-1.55172 ,1.00000 },
				{2.35700 ,-0.353807 ,-1.55172 ,1.00000 },
				{3.64300 ,1.36645 ,0.816000 ,1.00000 },
				{2.78591 ,1.56603 ,-0.283867 ,1.00000 },
				{0.428909 ,0.0713700 ,-1.58995 ,1.00000 },
				{1.07109 ,-1.53419 ,-0.423444 ,1.00000 },
				{0.643000 ,-0.625845 ,-1.46333 ,1.00000 },
				{0.857000 ,-1.19832 ,-1.04741 ,1.00000 },
				{2.14300 ,0.353807 ,-1.55172 ,1.00000 },
				{1.71409 ,-0.213956 ,-1.57710 ,1.00000 },
				{1.28591 ,-0.753903 ,-1.40166 ,1.00000 },
				{2.14300 ,0.353807 ,-1.55172 ,1.00000 },
				{1.71409 ,-0.213956 ,-1.57710 ,1.00000 },
				{3.64300 ,-0.353807 ,1.55172 ,1.00000 },
				{2.78591 ,0.753903 ,1.40166 ,1.00000 },
				{3.21409 ,0.213956 ,1.57710 ,1.00000 },
				{2.57109 ,0.0713700 ,1.58995 ,1.00000 },
				{-0.214091 ,-1.10009 ,1.15014 ,1.00000 },
				{1.07109 ,-0.876809 ,1.32825 ,1.00000 },
				{-1.07109 ,-1.53419 ,0.423444 ,1.00000 },
				{-0.643000 ,-0.625845 ,1.46333 ,1.00000 },
				{-0.857000 ,-1.19832 ,1.04741 ,1.00000 },
				{-2.57109 ,-0.992288 ,1.24435 ,1.00000 },
				{-2.14300 ,0.353807 ,1.55172 ,1.00000 },
				{-2.35700 ,-0.353807 ,1.55172 ,1.00000 },
				{-3.64300 ,1.36645 ,-0.816000 ,1.00000 },
				{-2.78591 ,1.56603 ,0.283867 ,1.00000 },
				{-1.28591 ,-0.753903 ,1.40166 ,1.00000 },
				{-3.64300 ,-0.353807 ,-1.55172 ,1.00000 },
				{-2.78591 ,0.753903 ,-1.40166 ,1.00000 },
				{-3.21409 ,0.213956 ,-1.57710 ,1.00000 },
				{-1.28591 ,-0.213956 ,-1.57710 ,1.00000 },
				{-3.21409 ,0.213956 ,-1.57710 ,1.00000 },
				{-1.92891 ,-0.0713700 ,-1.58995 ,1.00000 },
				{-2.57109 ,0.0713700 ,-1.58995 ,1.00000 },
				{-3.21409 ,0.213956 ,-1.57710 ,1.00000 },
				{-1.28591 ,-0.213956 ,-1.57710 ,1.00000 },
				{-2.57109 ,0.0713700 ,-1.58995 ,1.00000 },
				{-1.92891 ,-0.0713700 ,-1.58995 ,1.00000 },
				{-3.64300 ,1.36645 ,-0.816000 ,1.00000 },
				{0.214091 ,-1.10009 ,-1.15014 ,1.00000 },
				{-1.07109 ,-0.876809 ,-1.32825 ,1.00000 },
				{-0.857000 ,0.353807 ,-1.55172 ,1.00000 },
				{-1.28591 ,-0.213956 ,-1.57710 ,1.00000 },
				{3.64300 ,1.19832 ,-1.04741 ,1.00000 },
				{2.35700 ,-0.353807 ,-1.55172 ,1.00000 },
				{2.78591 ,0.213956 ,-1.57710 ,1.00000 },
				{2.35700 ,-0.353807 ,-1.55172 ,1.00000 },
				{2.78591 ,0.213956 ,-1.57710 ,1.00000 },
				{2.35700 ,-0.353807 ,-1.55172 ,1.00000 },
				{2.14300 ,0.353807 ,-1.55172 ,1.00000 },
				{1.07109 ,-1.53419 ,-0.423444 ,1.00000 },
				{0.428909 ,0.0713700 ,-1.58995 ,1.00000 },
				{-0.643000 ,-1.58511 ,-0.143019 ,1.00000 },
				{-0.214091 ,-1.43380 ,-0.690836 ,1.00000 },
				{3.21409 ,-1.10009 ,-1.15014 ,1.00000 },
				{1.92891 ,-0.876809 ,-1.32825 ,1.00000 },
				{1.92891 ,-1.53419 ,0.423444 ,1.00000 },
				{2.35700 ,-0.625845 ,1.46333 ,1.00000 },
				{0.428909 ,-1.49008 ,-0.559198 ,1.00000 },
				{0.857000 ,-1.36645 ,0.816000 ,1.00000 },
				{0.643000 ,-1.58511 ,0.143019 ,1.00000 },
				{0.643000 ,-1.58511 ,0.143019 ,1.00000 },
				{0.214091 ,-1.43380 ,0.690836 ,1.00000 },
				{0.643000 ,-1.58511 ,0.143019 ,1.00000 },
				{1.92891 ,-1.53419 ,0.423444 ,1.00000 },
				{-1.07109 ,-1.53419 ,0.423444 ,1.00000 },
				{-3.64300 ,1.19832 ,1.04741 ,1.00000 },
				{-2.78591 ,0.213956 ,1.57710 ,1.00000 },
				{-3.64300 ,1.19832 ,1.04741 ,1.00000 },
				{-3.21409 ,-1.10009 ,1.15014 ,1.00000 },
				{-1.92891 ,-0.876809 ,1.32825 ,1.00000 },
				{-3.64300 ,-0.353807 ,-1.55172 ,1.00000 },
				{-3.21409 ,0.213956 ,-1.57710 ,1.00000 },
				{-2.57109 ,0.0713700 ,-1.58995 ,1.00000 },
				{-1.92891 ,-1.53419 ,-0.423444 ,1.00000 },
				{-2.35700 ,-0.625845 ,-1.46333 ,1.00000 },
				{-0.428909 ,-1.49008 ,0.559198 ,1.00000 },
				{-0.857000 ,-1.36645 ,-0.816000 ,1.00000 },
				{-0.643000 ,-1.58511 ,-0.143019 ,1.00000 },
				{-0.643000 ,-1.58511 ,-0.143019 ,1.00000 },
				{-1.92891 ,-1.53419 ,-0.423444 ,1.00000 },
				{-2.57109 ,0.0713700 ,-1.58995 ,1.00000 },
				{1.71409 ,-1.56603 ,-0.283867 ,1.00000 },
				{1.07109 ,-1.53419 ,-0.423444 ,1.00000 },
				{0.428909 ,-1.49008 ,-0.559198 ,1.00000 },
				{1.71409 ,-1.56603 ,-0.283867 ,1.00000 },
				{0.428909 ,-1.49008 ,-0.559198 ,1.00000 },
				{1.07109 ,-1.53419 ,-0.423444 ,1.00000 },
				{2.57109 ,-1.49008 ,0.559198 ,1.00000 },
				{2.14300 ,-1.36645 ,-0.816000 ,1.00000 },
				{2.35700 ,-1.58511 ,-0.143019 ,1.00000 },
				{0.428909 ,-1.49008 ,-0.559198 ,1.00000 },
				{0.643000 ,-1.58511 ,0.143019 ,1.00000 },
				{1.28591 ,-1.56603 ,0.283867 ,1.00000 },
				{1.71409 ,-1.56603 ,-0.283867 ,1.00000 },
				{-1.71409 ,-1.56603 ,0.283867 ,1.00000 },
				{-0.428909 ,-1.49008 ,0.559198 ,1.00000 },
				{-1.07109 ,-1.53419 ,0.423444 ,1.00000 },
				{1.28591 ,-1.56603 ,0.283867 ,1.00000 },
				{3.21409 ,-1.43380 ,0.690836 ,1.00000 },
				{1.92891 ,-1.53419 ,0.423444 ,1.00000 },
				{2.57109 ,-1.49008 ,0.559198 ,1.00000 },
				{1.28591 ,-1.56603 ,0.283867 ,1.00000 },
				{2.57109 ,-1.49008 ,0.559198 ,1.00000 },
				{1.92891 ,-1.53419 ,0.423444 ,1.00000 },
				{-1.71409 ,-1.56603 ,0.283867 ,1.00000 },
				{-1.07109 ,-1.53419 ,0.423444 ,1.00000 },
				{-0.428909 ,-1.49008 ,0.559198 ,1.00000 },
				{-2.57109 ,-1.49008 ,-0.559198 ,1.00000 },
				{-2.14300 ,-1.36645 ,0.816000 ,1.00000 },
				{-2.35700 ,-1.58511 ,0.143019 ,1.00000 },
				{-0.428909 ,-1.49008 ,0.559198 ,1.00000 },
				{-0.643000 ,-1.58511 ,-0.143019 ,1.00000 },
				{-1.28591 ,-1.56603 ,-0.283867 ,1.00000 },
				{-1.71409 ,-1.56603 ,0.283867 ,1.00000 },
				{-1.28591 ,-1.56603 ,-0.283867 ,1.00000 },
				{-3.21409 ,-1.43380 ,-0.690836 ,1.00000 },
				{-1.92891 ,-1.53419 ,-0.423444 ,1.00000 },
				{-2.57109 ,-1.49008 ,-0.559198 ,1.00000 },
				{-1.28591 ,-1.56603 ,-0.283867 ,1.00000 },
				{-2.57109 ,-1.49008 ,-0.559198 ,1.00000 },
				{-1.92891 ,-1.53419 ,-0.423444 ,1.00000 },
				{3.64300 ,-0.625845 ,-1.46333 ,1.00000 },
				{2.35700 ,-1.58511 ,-0.143019 ,1.00000 },
				{2.78591 ,-1.43380 ,-0.690836 ,1.00000 },
				{2.35700 ,-1.58511 ,-0.143019 ,1.00000 },
				{0.428909 ,-1.49008 ,-0.559198 ,1.00000 },
				{0.643000 ,-1.58511 ,0.143019 ,1.00000 },
				{1.71409 ,-1.56603 ,-0.283867 ,1.00000 },
				{1.28591 ,-1.56603 ,0.283867 ,1.00000 },
				{1.28591 ,-1.56603 ,0.283867 ,1.00000 },
				{1.71409 ,-1.56603 ,-0.283867 ,1.00000 },
				{2.57109 ,-1.49008 ,0.559198 ,1.00000 },
				{2.35700 ,-1.58511 ,-0.143019 ,1.00000 },
				{1.71409 ,-1.56603 ,-0.283867 ,1.00000 },
				{0.428909 ,-1.49008 ,-0.559198 ,1.00000 },
				{1.07109 ,-1.53419 ,-0.423444 ,1.00000 },
				{1.71409 ,-1.56603 ,-0.283867 ,1.00000 },
				{1.07109 ,-1.53419 ,-0.423444 ,1.00000 },
				{0.428909 ,-1.49008 ,-0.559198 ,1.00000 },
				{1.71409 ,-1.56603 ,-0.283867 ,1.00000 },
				{1.28591 ,-1.56603 ,0.283867 ,1.00000 },
				{-0.428909 ,-1.49008 ,0.559198 ,1.00000 },
				{-0.643000 ,-1.58511 ,-0.143019 ,1.00000 },
				{2.57109 ,-1.49008 ,0.559198 ,1.00000 },
				{2.35700 ,-1.58511 ,-0.143019 ,1.00000 },
				{-1.71409 ,-1.56603 ,0.283867 ,1.00000 },
				{-1.28591 ,-1.56603 ,-0.283867 ,1.00000 },
				{-1.28591 ,-1.56603 ,-0.283867 ,1.00000 },
				{-1.71409 ,-1.56603 ,0.283867 ,1.00000 },
				{-3.64300 ,-0.625845 ,1.46333 ,1.00000 },
				{-2.35700 ,-1.58511 ,0.143019 ,1.00000 },
				{-2.78591 ,-1.43380 ,0.690836 ,1.00000 },
				{-2.35700 ,-1.58511 ,0.143019 ,1.00000 },
				{-3.64300 ,-0.625845 ,1.46333 ,1.00000 },
				{-2.57109 ,-1.49008 ,-0.559198 ,1.00000 },
				{-2.35700 ,-1.58511 ,0.143019 ,1.00000 },
				{-1.71409 ,-1.56603 ,0.283867 ,1.00000 },
				{-0.428909 ,-1.49008 ,0.559198 ,1.00000 },
				{-1.07109 ,-1.53419 ,0.423444 ,1.00000 },
				{-1.71409 ,-1.56603 ,0.283867 ,1.00000 },
				{-1.07109 ,-1.53419 ,0.423444 ,1.00000 },
				{-0.428909 ,-1.49008 ,0.559198 ,1.00000 },
				{-1.71409 ,-1.56603 ,0.283867 ,1.00000 },
				{-1.28591 ,-1.56603 ,-0.283867 ,1.00000 },
				{-2.57109 ,-1.49008 ,-0.559198 ,1.00000 },
				{-2.35700 ,-1.58511 ,0.143019 ,1.00000 },
				{0.428909 ,-1.49008 ,-0.559198 ,1.00000 },
				{0.643000 ,-1.58511 ,0.143019 ,1.00000 },
				{1.28591 ,-1.56603 ,0.283867 ,1.00000 },
				{1.92891 ,-1.53419 ,0.423444 ,1.00000 },
				{2.57109 ,-1.49008 ,0.559198 ,1.00000 },
				{1.28591 ,-1.56603 ,0.283867 ,1.00000 },
				{2.57109 ,-1.49008 ,0.559198 ,1.00000 },
				{1.92891 ,-1.53419 ,0.423444 ,1.00000 },
				{2.57109 ,-1.49008 ,0.559198 ,1.00000 },
				{2.35700 ,-1.58511 ,-0.143019 ,1.00000 },
				{-0.428909 ,-1.49008 ,0.559198 ,1.00000 },
				{-0.643000 ,-1.58511 ,-0.143019 ,1.00000 },
				{-1.28591 ,-1.56603 ,-0.283867 ,1.00000 },
				{-1.92891 ,-1.53419 ,-0.423444 ,1.00000 },
				{-2.57109 ,-1.49008 ,-0.559198 ,1.00000 },
				{-1.28591 ,-1.56603 ,-0.283867 ,1.00000 },
				{-2.57109 ,-1.49008 ,-0.559198 ,1.00000 },
				{-1.92891 ,-1.53419 ,-0.423444 ,1.00000 },
				{-2.57109 ,-1.49008 ,-0.559198 ,1.00000 },
				{-2.35700 ,-1.58511 ,0.143019 ,1.00000 }});
		bloed.update();
		SceneGraphComponent root = new SceneGraphComponent();
//		root.setGeometry(bloed.getIndexedFaceSet());
//		ViewerApp.display(root);
		
		// 136 Punkte sind das optimale minimum
		// (welches wir erreichen wollen)
		
		
		IndexedFaceSet i= RemoveDuplicateInfo.removeDuplicateVertices(bloed.getIndexedFaceSet() );
		root.setGeometry(i);
		System.out.println("RemoveDuplicateInfo.main(#Points) "+i.getNumPoints());
		ViewerApp.display(root);
		
		}
}
