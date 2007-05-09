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


package de.jreality.geometry;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.data.StringArray;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rectangle3D;

/**
 * Static methods for editing and processing instances of {@link de.jreality.scene.IndexedFaceSet}.
 * 
 * @author Charles Gunn and others
 *
 */
public class IndexedFaceSetUtility {

	static private int[][] boxIndices = {
	{0,1,3,2},
	{1,0,4,5},
	{3,1,5,7},
	{5,4,6,7},
	{2,3,7,6},
	{0,2,6,4}};

	private static double EPS = 10E-8;
		
	private IndexedFaceSetUtility() {
	}
       
	/**
	 * Only applicable for <i>ifs</i> all of whose faces are triangles; then
	 * each triangle is broken into
	 * four smaller triangles by bisecting the edges.
	 * 
	 * @param ifs
	 * @return
	 */
	public static IndexedFaceSet binaryRefine(IndexedFaceSet ifs)	{
		int[][] indices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray().toIntArrayArray(null);
		for (int i=0; i<indices.length; ++i)	{
			if (indices[i].length != 3) 
				throw new IllegalArgumentException("Indexed face set must consist of triangles");
		}
	
		Hashtable edgeVals = new Hashtable();	
	
		// the new triangulation will have:
		// 		vertices:	V + E
		//		edges:		2E + 3F
		//		faces:		4F
		//	we also know 	 V - E + F = 2 so E = V + F - 2
		int numVerts = ifs.getNumPoints();
		int numFaces = ifs.getNumFaces();
		int numEdges = ifs.getNumEdges();
		int vLength = GeometryUtility.getVectorLength(ifs);
		int newSize = numVerts + numEdges; //numFaces - 2;
		double[][] nvd = new double[newSize][vLength];
		double[][] newpptr = nvd;
		double[][] oldpptr = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		System.arraycopy(oldpptr, 0, newpptr, 0, numVerts);		// copy over the existing vertices
	
		int[][] newIndices = new int[4*numFaces][3];
		int[] index = new int[3];
		int nV = numVerts;
		int nF = 0;
		for ( int i=0; i<numFaces; ++i)	{
			// create the new points
			for (int j=0; j<3; ++j)	{
				int v1 = indices[i][(j+1)%3];
				int v2 = indices[i][(j+2)%3];
				int kk = (v1>v2) ? (v1<<15) + v2 : (v2<<15) + v1;	// same for either order!
				Integer key = new Integer(kk);
				Integer value = (Integer) edgeVals.get(key);
				if (value != null )       {	// reuse old vertex
					index[j] = value.intValue();
				} else {		// new vertex creation
					index[j] = nV;	
					Rn.add(newpptr[nV],oldpptr[v1], oldpptr[v2]);
					Rn.times(newpptr[nV], .5,newpptr[nV]);
					edgeVals.put(key, new Integer(nV));
					nV++;
					if ( nV > newSize)	{
						//TODO indicate error!
						return null;
					}
				}
			}
			// the internal new face
			for (int k = 0 ; k<3; ++k)	newIndices[nF][k] = index[k];
			// the other three new faces
			nF++;
			newIndices[nF][0] = indices[i][0];
			newIndices[nF][1] = index[2];
			newIndices[nF][2] = index[1];
			nF++;
			newIndices[nF][0] = indices[i][1];
			newIndices[nF][1] = index[0];
			newIndices[nF][2] = index[2];
			nF++;
			newIndices[nF][0] = indices[i][2];
			newIndices[nF][1] = index[1];
			newIndices[nF][2] = index[0];
			nF++;
		}
	
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(newpptr.length);
		ifsf.setVertexCoordinates(newpptr);
		ifsf.setFaceCount(newIndices.length);
		ifsf.setFaceIndices(newIndices);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}

	public static void calculateAndSetEdgesFromFaces( IndexedFaceSet ifs ) {
		IntArrayArray faces=
	        ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray();
		 ifs.setEdgeCountAndAttributes(Attribute.INDICES, edgesFromFaces( faces ) );
	}


	/**
	 * Rough clipping: retains all faces one of whose vertices lies within the clipping box <i>box</i>.
	 * Currently not all attributes are retained.
	 * @param ifs
	 * @param box
	 * @return
	 */
	public static IndexedFaceSet clipToBox(IndexedFaceSet ifs, Rectangle3D box)	{
//		IndexedFaceSet clipped = new IndexedFaceSet();
		int n = ifs.getNumFaces();
//		clipped.setNumPoints(ifs.getNumPoints());
//		clipped.setNumEdges(ifs.getNumEdges());
//		clipped.setNumFaces(ifs.getNumFaces());
//		clipped.setVertexAttributes(ifs.getVertexAttributes());
//		clipped.setFaceAttributes(ifs.getFaceAttributes());
//		clipped.setEdgeAttributes(ifs.getEdgeAttributes());
		DataList verts = ifs.getVertexAttributes(Attribute.COORDINATES);
		ArrayList inBounds = new ArrayList();
		double[][] bnds = box.getBounds();
		for (int i = 0; i<n; ++i)	{
			int[] tf = ifs.getFaceAttributes(Attribute.INDICES).item(i).toIntArray(null);
			boolean outside = true;
			for (int j = 0; (j<tf.length) && outside; ++j)	{
				int k = tf[j];
				double[] vec = verts.item(k).toDoubleArray(null);
				if (vec.length == 4) Pn.dehomogenize(vec,vec);
				if (bnds[0][0] < vec[0] && bnds[1][0] > vec[0] 
				&& bnds[0][1] < vec[1] && bnds[1][1] > vec[1] 
				&& bnds[0][2] < vec[2] && bnds[1][2] > vec[2]	 ) 
						outside = false;
			}
			if (!outside)	{
				inBounds.add(tf);
			}
		}
		int m = inBounds.size();
		int[][] newIndices = new int[m][];
		for (int i =0; i<m; ++i)	{
			newIndices[i] = (int[] ) inBounds.get(i);
		}
		LoggingSystem.getLogger(GeometryUtility.class).log(Level.FINE,"In, out face count: "+n+"  "+m);
		//TODO rescue the other face attributes
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(ifs.getNumPoints());
		ifsf.setVertexAttributes(ifs.getVertexAttributes());
		ifsf.setFaceCount(m);
		ifsf.setFaceIndices(newIndices);		
		ifsf.setGenerateEdgesFromFaces(true);

		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}

	/**
	 * A simple constructor for an IndexedFaceSet with a single face.
	 * @param points
	 * @return
	 */
	public static IndexedFaceSet constructPolygon(double[][] points)	{
		return constructPolygon(null, points);
	}
	
	/**
	 *  A simple constructor for an IndexedFaceSet with a single face.
	 *  Currently the argument <i>exists</i> is not used.
	 * @param ifs
	 * @param points
	 * @return
	 */
	public static IndexedFaceSet constructPolygon(IndexedFaceSet ifs, double[][] points)	{
		return constructPolygon(ifs, points, Pn.EUCLIDEAN);
	}
	public static IndexedFaceSet constructPolygon(IndexedFaceSet ifs, double[][] points, int sig)	{
		int[][] ind = new int[1][points.length];
		for (int i = 0; i<points.length; ++i)	ind[0][i] = i;
		if (ifs == null) ifs = new IndexedFaceSet();
		// TODO replace this code when it's fixed to initialize the factory with the existing ifs.
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();// Pn.EUCLIDEAN, true, false, true);
		ifsf.setSignature(sig);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setVertexCount(points.length);
		ifsf.setFaceCount(1);
		ifsf.setVertexCoordinates(points);
		ifsf.setFaceIndices(ind);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}

	public static IntArrayArray edgesFromFaces( int [][] faces ) {
		return edgesFromFaces( new IntArrayArray.Array(faces));
	}

	// needed for the new implementation of buildEdgesFromFaces:
	private static final class Pair {
	  final int l, h;
	  Pair(int a, int b) {
	    if(a<=b) { l=a; h=b; }
	    else     { h=a; l=b; }
	  }
	  public boolean equals(Object obj) {
	    if(this==obj) return true;
	    try {
	      final Pair p=(Pair)obj;
	      return l == p.l && h == p.h;
	    } catch(ClassCastException ex) {
	      return false;
	    }
	  }
	  public int hashCode() {
	    return (l<<16)^h;
	  }
	}

	/**
	 * TODO optimize in case its a QuadMesh (u+v curves instead of u*v segments)
	 * @param faces
	 * @return
	 */
	public static IntArrayArray edgesFromFaces( IntArrayArray faces )
	{
	    HashSet set =new HashSet();
	   
	    for (int i= 0; i < faces.getLength(); i++)
	    {
	        IntArray f= faces.getValueAt(i);
	        for (int j= 0; j < f.getLength() - 1; j++)
	        {
	            set.add(new Pair(f.getValueAt(j), f.getValueAt(j + 1)));
	            
	        }
	        set.add(new Pair(f.getValueAt(f.getLength()-1), f.getValueAt(0)));
	    }
	    final int num= set.size();
	    int[] edge=new int[num*2];
	    int count=0;
	    for (Iterator iter = set.iterator(); iter.hasNext(); count+=2) {
	      Pair p = (Pair) iter.next();
	      edge[count]=p.l;
	      edge[count+1]=p.h;
	    }
	    return new IntArrayArray.Inlined(edge, 2);
	}

	/**
	 * Extract the <i>which</i> edge from the edge list of <i>ifs</i>. In case the latter
	 * is a generic IndexedFaceSet, then this will consist of two points.  But if it is
	 * marked as a quad mesh (see {@link GeometryUtility#QUAD_MESH_SHAPE}), then this will 
	 * correspond to a u- or v- parameter curve.
	 * 
	 * @param curve
	 * @param ifs
	 * @param which
	 * @return
	 */
	public static double[][] extractEdge(double[][] curve, IndexedFaceSet ifs, int which)	{
		DataList verts = ifs.getVertexAttributes(Attribute.COORDINATES);
		int[] indices = ifs.getEdgeAttributes(Attribute.INDICES).item(which).toIntArray(null);
		int n = indices.length;
		int m = GeometryUtility.getVectorLength(verts);
		if (curve == null || curve.length != n || curve[0].length != m) curve = new double[n][m];
		for (int i = 0; i<n; ++i)	{
			DoubleArray da = verts.item(i).toDoubleArray();
			for (int j = 0; j < n; ++j)	{
				curve[i][j] = da.getValueAt(j);				
			}
		}
		return curve;
	}

	private static double[][] getMinMax(int[] indices, double[][] array2d)	{
		int f = array2d[0].length;
		double[][] minmax = new double[2][f];
		System.arraycopy(array2d[indices[0]], 0, minmax[0], 0, f);
		System.arraycopy(array2d[indices[0]], 0, minmax[1], 0, f);
		for (int i = 1; i<indices.length; ++i)	{
			Rn.min(minmax[0], minmax[0], array2d[indices[i]]);
			Rn.max(minmax[1], minmax[1], array2d[indices[i]]);
		}
		return minmax;
	}

	
	/**
	 * Returns the total nubmer of faces in array of indexed face sets.
	 * @param ifs array of indexed faces sets
	 * @return total number of faces in array of indexed face sets.
	 */
	public static int getTotalNumFaces( IndexedFaceSet [] ifs ) {
		int N =0;
		for( int i=0; i<ifs.length; i++ ) {
			N += ifs[i].getNumFaces();
		}
		return N;
	}
	/**
	 * Returns the total nubmer of lines in array of indexed line sets.
	 * @param ifs array of indexed line sets
	 * @return total number of lines in array of indexed line sets.
	 */
	public static int getTotalNumLines( IndexedLineSet [] ils ) {
		int N =0;
		for( int i=0; i<ils.length; i++ ) {
			N += ils[i].getNumEdges();
		}
		return N;
	}
	
	/**
	 * Returns the total number of points in array of point sets.
	 * @param ps array of point sets
	 * @return total number of points in array of point set.
	 */
	public static int getTotalNumPoints( PointSet [] ps ) {
		int N =0;
		for( int i=0; i<ps.length; i++ ) {
			N += ps[i].getNumPoints();
		}
		return N;
	}
	
	/**
	 * For each face of <i>ifs</i>, replace it with a face gotten by:
	 * <ul>
	 * <li>if factor > 0: a shrunken version of the face (factor == 1 gives original face), or </li>
	 * <li>if factor < 0: a hole is cut out of the face, corresponding to the shrunken version with the
	 * same absolute value.</li>
	 * </ul>
	 * @param ifs
	 * @param factor
	 * @return
	 */public static IndexedFaceSet implode(IndexedFaceSet ifs, double factor)	{
		 
		 	// steffen: changed so that edges are only created when the
			// given isf had edges
			boolean makeEdges = ifs.getEdgeAttributes().containsAttribute(Attribute.INDICES);
			
		int vertcount = 0;
		int[][] ind = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		//int[][] ind = ifs.getIndices();
		for (int i = 0; i<ind.length; ++i)	{
			vertcount += ind[i].length;
		}
		
		double[][] oldverts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		//double[][] oldverts = ifs.getVertices().getData();
		int vectorLength = oldverts[0].length; //ifs.getVertices().getVectorLength();
		if (vectorLength != 3)	{
			double[][] oldverts2 = new double[oldverts.length][3];
			Pn.dehomogenize(oldverts2, oldverts);
			oldverts = oldverts2;
			vectorLength = 3;
			//return null;
		}
		
		int[][] newind;
		double[][] newverts;
		IndexedFaceSet imploded;
		double implode = -factor; //(factor > 0) ? (-(1.0-factor)) : ((1.0+factor));
		if (implode > 0.0)	{
			newind = new int[ind.length][];
			newverts = new double[vertcount][vectorLength];
			for (int i = 0, count = 0; i<ind.length; ++i)	{
				int[] thisf = ind[i];
				newind[i] = new int[thisf.length];
				double[] center = new double[3];
				for (int j = 0; j<thisf.length; ++j)	{
					Rn.add(center, oldverts[ind[i][j]], center);
					newind[i][j] = count+j;
				} 
				Rn.times(center, 1.0/thisf.length, center);
				double[] diff = new double[vectorLength];
				for (int j = 0; j<thisf.length; ++j)	{
					Rn.subtract(diff, oldverts[ind[i][j]], center);
					Rn.times(diff, implode, diff);
					Rn.add(newverts[count+j], center, diff);
				}
				count += thisf.length;
			}
			double[][] fn = null, fc = null;
			//imploded = new IndexedFaceSet(newind, new DataGrid(newverts, false), null, null, ifs.getFaceNormals(), ifs.getFaceColors());
			if (ifs.getFaceAttributes(Attribute.NORMALS) != null) fn = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
			else fn = GeometryUtility.calculateFaceNormals(ifs);
			if (ifs.getFaceAttributes(Attribute.COLORS) != null) fc = ifs.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null);

			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(vertcount);
			ifsf.setFaceCount(ind.length);
			ifsf.setVertexCoordinates(newverts);
			ifsf.setFaceIndices(newind);
			if (fn != null)   ifsf.setFaceNormals(fn);
			else ifsf.setGenerateFaceNormals(true);
			if (fc != null)   ifsf.setFaceColors(fc);
			
			ifsf.setGenerateEdgesFromFaces(makeEdges);
			ifsf.update();
			imploded = ifsf.getIndexedFaceSet();

//			imploded = createIndexedFaceSetFrom(newind,newverts, null, null, fn, fc);
//			imploded.buildEdgesFromFaces();
		} else {
			int oldcount = oldverts.length;
			newind = new int[vertcount][4];
			newverts = new double[vertcount + oldcount][vectorLength];
			for (int i = 0; i<oldcount; ++i)	Rn.copy(newverts[i], oldverts[i]);
			for (int i = 0, count = 0; i<ind.length; ++i)	{
				int[] thisf = ind[i];
				double[] center = new double[3];
				for (int j = 0; j<thisf.length; ++j)	{
					Rn.add(center, oldverts[ind[i][j]], center);
					newind[count+j][0] = ind[i][j];
					newind[count+j][1] = ind[i][(j+1)%thisf.length];
					newind[count+j][2] = oldcount+count+((j+1)%thisf.length);
					newind[count+j][3] = oldcount+count+j;
				} 
				Rn.times(center, 1.0/thisf.length, center);
				double[] diff = new double[vectorLength];
				for (int j = 0; j<thisf.length; ++j)	{
					Rn.subtract(diff, center, oldverts[ind[i][j]]);
					Rn.times(diff, -implode, diff);
					Rn.add(newverts[oldcount + count+j], oldverts[ind[i][j]], diff);
				}
				count += thisf.length;
			}
//			//imploded = new IndexedFaceSet(newind, new DataGrid(newverts, false), null, null, null, null);
//			imploded = createIndexedFaceSetFrom(newind,newverts, null, null, null, null);
//			GeometryUtility.calculateAndSetFaceNormals(imploded);
//			//imploded.setEdgeAttributes(Attribute.INDICES, ifs.getEdgeAttributes(Attribute.INDICES));
//			imploded.setEdgeCountAndAttributes(Attribute.INDICES, ifs.getEdgeAttributes(Attribute.INDICES));
			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(vertcount+oldcount);
			ifsf.setFaceCount(vertcount);
			ifsf.setVertexCoordinates(newverts);
			ifsf.setFaceIndices(newind);
			ifsf.setGenerateFaceNormals(true);
			ifsf.setGenerateEdgesFromFaces(makeEdges);
			ifsf.update();
			imploded = ifsf.getIndexedFaceSet();

		}
		return imploded;
	}
	
	 /** @deprecated use <code>GeometryMergeFactory</code>*/
		public static double [][] mergeDoubleArrayArrayFaceAttribute( IndexedFaceSet [] ifs , Attribute attr) {
			
			double [][] result = new double[getTotalNumFaces(ifs)][];
			
			for( int i=0, n=0; i<ifs.length; n += ifs[i].getNumFaces(), i++ ) {
				double[][] values = ifs[i].getFaceAttributes( attr ).toDoubleArrayArray(null);
				System.arraycopy(values, 0, result, n, values.length );
			}
			
			return result;  
		}
		/** @deprecated use <code>GeometryMergeFactory</code>*/
		public static double [][] mergeDoubleArrayArrayEdgeAttribute( IndexedLineSet [] ils , Attribute attr) {
			
			double [][] result = new double[getTotalNumLines(ils)][];
			
			for( int i=0, n=0; i<ils.length; n += ils[i].getNumEdges(), i++ ) {
				double[][] values = ils[i].getEdgeAttributes( attr ).toDoubleArrayArray(null);
				System.arraycopy(values, 0, result, n, values.length );
			}
			
			return result;  
		}
		/** @deprecated use <code>GeometryMergeFactory</code>*/
	private static void mergeDoubleArrayArrayFaceAttributes(IndexedFaceSet[] ifs, IndexedFaceSet result, boolean firstTime ) {
		Object [] faceAttr = ifs[0].getFaceAttributes().storedAttributes().toArray();
		for( int i=0; i<faceAttr.length; i++ ) {
			Attribute attr = (Attribute)faceAttr[i];
			if( ifs[0].getFaceAttributes(attr) instanceof DoubleArrayArray  ) {
				try {
					DataList dataList = new DoubleArrayArray.Array(mergeDoubleArrayArrayFaceAttribute(ifs, attr ));
					if( firstTime ) {
						result.setFaceCountAndAttributes( attr, dataList );
						firstTime = false;
					} else {
						result.setFaceAttributes( attr, dataList );
					}
			} catch (Exception e) {}
				
			}
		}
	}
	/** @deprecated use <code>GeometryMergeFactory</code>*/
	private static void mergeDoubleArrayArrayEdgeAttributes(IndexedLineSet[] ils, IndexedLineSet result, boolean firstTime ) {
		Object [] edgeAttr = ils[0].getEdgeAttributes().storedAttributes().toArray();
		for( int i=0; i<edgeAttr.length; i++ ) {
			Attribute attr = (Attribute)edgeAttr[i];
			if( ils[0].getEdgeAttributes(attr) instanceof DoubleArrayArray  ) {
				try {
					DataList dataList = new DoubleArrayArray.Array(mergeDoubleArrayArrayEdgeAttribute(ils, attr ));
					if( firstTime ) {
						result.setEdgeCountAndAttributes( attr, dataList );
						firstTime = false;
					} else {
						result.setEdgeAttributes( attr, dataList );
					}
			} catch (Exception e) {}
				
			}
		}
	}// ende Bernd
	
	/** @deprecated use <code>GeometryMergeFactory</code>*/
	public static double [][] mergeDoubleArrayArrayVertexAttribute( PointSet [] ps , Attribute attr) {
		
		double [][] result = new double[getTotalNumPoints(ps)][];
		
		for( int i=0, n=0; i<ps.length; n += ps[i].getNumPoints(), i++ ) {
			double[][] values = ps[i].getVertexAttributes( attr ).toDoubleArrayArray(null);
			System.arraycopy(values, 0, result, n, values.length );
		}
		
		return result;  
	}
	/** @deprecated use <code>GeometryMergeFactory</code>*/
	private static void mergeDoubleArrayArrayVertexAttributes( PointSet[] ps, PointSet result, boolean firstTime ) {
		Object [] vertexAttr = ps[0].getVertexAttributes().storedAttributes().toArray();
		
		for( int i=0; i<vertexAttr.length; i++ ) {
			Attribute attr = (Attribute)vertexAttr[i];
			if( ps[0].getVertexAttributes(attr) instanceof DoubleArrayArray ) {
				try {
					DataList dataList = new DoubleArrayArray.Array(mergeDoubleArrayArrayVertexAttribute(ps, attr ));
					if( firstTime ) {
						result.setVertexCountAndAttributes( attr, dataList );
						firstTime = false;
					} else {
						result.setVertexAttributes( attr, dataList );
					}
					
				} catch (Exception e) {}
				
			}
		}
	}
	
	/** @deprecated use <code>GeometryMergeFactory</code>*/
	public static IndexedFaceSet mergeIndexedFaceSets( IndexedFaceSet [] ifs ) {
				
		IndexedFaceSet result = new IndexedFaceSet();
		
		final int [][] faceIndices = mergeIntArrayArrayFaceAttribute( ifs, Attribute.INDICES );
		
		for( int i=1, n=ifs[0].getNumPoints(), k=ifs[0].getNumFaces(); i<ifs.length; n += ifs[i].getNumPoints(), i++ ) {
			final int nof = ifs[i].getNumFaces();
			for( int f=0; f<nof; f++, k++ ) {
				final int [] face = faceIndices[k];
				for( int j=0; j<face.length; j++) {
					face[j] += n;
				}
			}	
		}
			
		result.setFaceCountAndAttributes(
				Attribute.INDICES,
				new IntArrayArray.Array( faceIndices )
		);
		
		mergeDoubleArrayArrayVertexAttributes(ifs, result, true );
		mergeDoubleArrayArrayFaceAttributes(ifs, result, false );
		
		return result;
	}
	
	/** @deprecated use <code>GeometryMergeFactory</code>*/
	public static IndexedLineSet mergeIndexedLineSets( IndexedLineSet [] ils ) {
		
		IndexedLineSet result = new IndexedLineSet();
		
		final int [][] lineIndices = mergeIntArrayArrayEdgeAttribute( ils, Attribute.INDICES );
		
		for( int i=1, n=ils[0].getNumPoints(), k=ils[0].getNumEdges(); i<ils.length; n += ils[i].getNumPoints(), i++ ) {
			final int nof = ils[i].getNumEdges();
			for( int f=0; f<nof; f++, k++ ) {
				final int [] line = lineIndices[k];
				for( int j=0; j<line.length; j++) {
					line[j] += n;
				}
			}	
		}
			
		result.setEdgeCountAndAttributes(
				Attribute.INDICES,
				new IntArrayArray.Array( lineIndices )
		);
		
		mergeDoubleArrayArrayVertexAttributes(ils, result, true );
		mergeDoubleArrayArrayEdgeAttributes(ils, result, false );
		
		return result;
	}// ende Bernd
	
	/** @deprecated use <code>GeometryMergeFactory</code>*/
	public static SceneGraphComponent mergeIndexedFaceSets(SceneGraphComponent sgc)	{
		
		// collects sgc's which themselves have children
		class CollectComponents extends SceneGraphVisitor {
			SceneGraphComponent root;
			Vector sgclist;
			CollectComponents(SceneGraphComponent r)	{
				root = r;
			}
			Object visit()	{
				sgclist = new Vector();
				visit(root);
				return sgclist;
			}
			
			public void visit(SceneGraphComponent c) {
				if (c.getChildComponentCount() > 0) sgclist.add(c);
				c.childrenAccept(this);
			}
	    }
		
		CollectComponents cc = new CollectComponents(sgc);
		Vector sgclist = (Vector) cc.visit();
		Iterator iter = sgclist.iterator();
		// HACK:
		SceneGraphComponent result=null;
		while (iter.hasNext())	{
			SceneGraphComponent next = (SceneGraphComponent) iter.next();
			// XXX this makes no sense to me -gunn
			result = _mergeIndexedFaceSets(next);
		}
		
		// HACK continued
		SceneGraphComponent mergedIFS = new SceneGraphComponent();
		mergedIFS.setGeometry(result.getGeometry());
		result.setGeometry(null);
		result.addChild(mergedIFS);
		return sgc;
	}
	/** @deprecated use <code>GeometryMergeFactory</code>*/
	public static SceneGraphComponent _mergeIndexedFaceSets(SceneGraphComponent sgc)	{
    	Vector<IndexedFaceSet> ifslist = new Vector<IndexedFaceSet>();
    	Vector colorList = new Vector();
    	Vector lengths = new Vector();
    	Vector toRemove = new Vector();
    	int n = sgc.getChildComponentCount();
    	Appearance ap = sgc.getAppearance();
    	EffectiveAppearance eap = EffectiveAppearance.create();
    	if (ap != null) eap = eap.create(ap);
    	IndexedFaceSet ifs;
    	int vcount = 0;
    	if (sgc.getGeometry()!= null && sgc.getGeometry() instanceof IndexedFaceSet) {
    		ifs = (IndexedFaceSet) sgc.getGeometry(); 
    		ifslist.add(ifs);
    		lengths.add(new Integer(ifs.getNumPoints()));
    		vcount += ifs.getNumPoints();
        	Object dc =  eap.getAttribute("polygonShader.diffuseColor",Color.WHITE, Color.class);
        	if (dc instanceof Color)		{
        		colorList.add(dc);
        	}  else 
        		colorList.add(Color.WHITE);
        	 
    	}
    	for (int i = 0; i<n; ++i)	{
    		SceneGraphComponent child = sgc.getChildComponent(i);
    		if (child.getTransformation() != null) continue;
    		if (child.getChildComponentCount() != 0) continue;
    		if (child.getGeometry() == null) continue;
    		Geometry geom = child.getGeometry();
     		if (geom instanceof IndexedFaceSet)	{
     			System.err.println("merging indexfaceset "+geom.getName());
     			ifslist.add((IndexedFaceSet) geom);
     			toRemove.add(child);
     			ifs = (IndexedFaceSet) geom;
           		lengths.add(new Integer(ifs.getNumPoints()));
           		vcount += ifs.getNumPoints();
          		ap = child.getAppearance();
          		if (ap != null)	{
          			EffectiveAppearance ceap = eap.create(ap);
          			Object dc =  ceap.getAttribute("polygonShader.diffuseColor",Color.WHITE, Color.class);
          			if (dc instanceof Color)		{
          				colorList.add(dc);
          			}  else 
          				colorList.add(Color.WHITE);	
        		}
    		}
    	}
    	Iterator iter = toRemove.iterator();
    	while (iter.hasNext())	{ sgc.removeChild( (SceneGraphComponent) iter.next()); }
    	
    	n = ifslist.size();
    	if (n == 0) return null; //ifslist.get(0);
    	IndexedFaceSet[] list = new IndexedFaceSet[ifslist.size()];
    	list = (IndexedFaceSet[]) ifslist.toArray(list);
    	ifs = mergeIndexedFaceSets(list);
    	// construct vertex color list
    	double[] carray = new double[vcount*4];
    	iter = colorList.iterator();
    	int i = 0;
    	int cptr = 0;
    	while (iter.hasNext())	{
    		Color c = (Color) iter.next();
    		int howMany = ((Integer) lengths.get(i++)).intValue();
    		float[] rgba = c.getRGBComponents(null);
    		for (int j= 0; j<howMany; ++j)	{
    			for (int k=0; k<4; ++k)		carray[cptr++] = rgba[k];
    		}
    	}
    	ifs.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.inlined(4).createReadOnly(carray));
    	sgc.setGeometry(ifs);
    	return sgc;
    }
	
	/** @deprecated use <code>GeometryMergeFactory</code>*/ 
	public static SceneGraphComponent mergeIndexedLineSets(SceneGraphComponent sgc)	{	
		// collects sgc's which themselves have children
		class CollectComponents extends SceneGraphVisitor {
			SceneGraphComponent root;
			Vector sgclist;
			CollectComponents(SceneGraphComponent r)	{
				root = r;
			}
			Object visit()	{
				sgclist = new Vector();
				visit(root);
				return sgclist;
			}
			
			public void visit(SceneGraphComponent c) {
				if (c.getChildComponentCount() > 0) sgclist.add(c);
				c.childrenAccept(this);
			}
	    }
		
		CollectComponents cc = new CollectComponents(sgc);
		Vector sgclist = (Vector) cc.visit();
		Iterator iter = sgclist.iterator();
		// HACK:
		SceneGraphComponent result=null;
		while (iter.hasNext())	{
			SceneGraphComponent next = (SceneGraphComponent) iter.next();
			result=_mergeIndexedLineSets(next);
		}
		// HACK continued
		SceneGraphComponent mergedIFS = new SceneGraphComponent();
		mergedIFS.setGeometry(result.getGeometry());
		result.setGeometry(null);
		result.addChild(mergedIFS);
		return sgc;
	} // ende Bernd
	// XXX: reads specular (!) color as vertex colors for IndexedLineSets - bug in VRML reader? 
	// Anfang Bernd 
	private static SceneGraphComponent _mergeIndexedLineSets(SceneGraphComponent sgc)	{
    	Vector ilslist = new Vector();
    	Vector colorList = new Vector();
    	Vector lengths = new Vector();
    	Vector toRemove = new Vector(); // ?
    	int n = sgc.getChildComponentCount();
    	Appearance ap = sgc.getAppearance();
    	EffectiveAppearance eap = EffectiveAppearance.create();
    	if (ap != null) eap = eap.create(ap);

    	IndexedLineSet ils;
    	int vcount = 0;

    	if ((sgc.getGeometry()!= null) && (sgc.getGeometry() instanceof IndexedLineSet)&&(!(sgc.getGeometry() instanceof IndexedFaceSet))) {
    		ils = (IndexedLineSet) sgc.getGeometry(); 
    		ilslist.add(ils);
    		lengths.add(new Integer(ils.getNumPoints()));
    		vcount += ils.getNumPoints();
        	Object dc =  eap.getAttribute("specularColor",Color.WHITE, Color.class);
        	if (dc instanceof Color)		{
        		colorList.add(dc);
        	}  else 
        		colorList.add(Color.WHITE); 
    	}
    	for (int i = 0; i<n; ++i)	{
    		SceneGraphComponent child = sgc.getChildComponent(i);
    		if (child.getTransformation() != null) continue;
    		if (child.getChildComponentCount() != 0) continue;
    		if (child.getGeometry() == null) continue;
    		Geometry geom = child.getGeometry();
     		if ((geom instanceof IndexedLineSet)&&(!(geom instanceof IndexedFaceSet)))	{
     			ilslist.add(geom);
     			toRemove.add(child);
     			ils = (IndexedLineSet) geom;
           		lengths.add(new Integer(ils.getNumPoints()));
           		vcount += ils.getNumPoints();
          		ap = child.getAppearance();
          		if (ap != null)	{
          			EffectiveAppearance ceap = eap.create(ap);
          			Object dc =  ceap.getAttribute("specularColor",Color.WHITE, Color.class);
          			if (dc instanceof Color)		{
          				colorList.add(dc);
          			}  else 
          				colorList.add(Color.WHITE);	
        		}
    		}
    	}
    	Iterator iter = toRemove.iterator();
    	while (iter.hasNext())	{ sgc.removeChild( (SceneGraphComponent) iter.next()); }
    	
    	n = ilslist.size();
    	if (n <= 1) return sgc;
    	IndexedLineSet[] list = new IndexedLineSet[ilslist.size()];
    	list = (IndexedLineSet[]) ilslist.toArray(list);
    	ils = mergeIndexedLineSets(list);
    	// construct vertex color list
    	double[] carray = new double[vcount*4];
    	iter = colorList.iterator();
    	int i = 0;
    	int cptr = 0;
    	while (iter.hasNext())	{
    		Color c = (Color) iter.next();
    		int howMany = ((Integer) lengths.get(i++)).intValue();
    		float[] rgba = c.getRGBComponents(null);
    		for (int j= 0; j<howMany; ++j)	{
    			for (int k=0; k<4; ++k)		carray[cptr++] = rgba[k];
    		}
    	}
    	ils.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.inlined(4).createReadOnly(carray));
    	sgc.setGeometry(ils);
    	return sgc;
    }// Ende Bernd
	/** @deprecated use <code>GeometryMergeFactory</code>*/
	public static int [][] mergeIntArrayArrayFaceAttribute( IndexedFaceSet [] ifs , Attribute attr) {
		
		int [][] result = new int[getTotalNumFaces(ifs)][];
		
		for( int i=0, n=0; i<ifs.length; n += ifs[i].getNumFaces(), i++ ) {
			int[][] values = ifs[i].getFaceAttributes( attr ).toIntArrayArray(null);
			System.arraycopy(values, 0, result, n, values.length );
		}
		
		return result;  
	}
	/** @deprecated use <code>GeometryMergeFactory</code>*/
	public static int [][] mergeIntArrayArrayEdgeAttribute( IndexedLineSet [] ils , Attribute attr) {
		
		int [][] result = new int[getTotalNumLines(ils)][];
		
		for( int i=0, n=0; i<ils.length; n += ils[i].getNumEdges(), i++ ) {
			int[][] values = ils[i].getEdgeAttributes( attr ).toIntArrayArray(null);
			System.arraycopy(values, 0, result, n, values.length );
		}
		
		return result;  
	} 

	/**
	 * A special purpose code for the Buddy-Baer project which might be useful for other situations
	 * where texture coordinates wrap around.
	 * @param src
	 * @param jumpSize
	 * @return
	 */
	public static IndexedFaceSet removeTextureCoordinateJumps(IndexedFaceSet src, double jumpSize)	{
		int np = src.getNumPoints();
		int nf = src.getNumFaces();
		double[][] textureCoords = src.getVertexAttributes(Attribute.TEXTURE_COORDINATES).toDoubleArrayArray(null);
		int[][] indices = src.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		int newVerts = 0;
		double[][][] minmax = new double[nf][][];
		boolean[][] textureJumps = new boolean[nf][2];
		for (int i = 0; i<nf; ++i)		{
			minmax[i] = getMinMax(indices[i], textureCoords );
			if ( minmax[i][1][0] - minmax[i][0][0] > jumpSize) textureJumps[i][0] = true;
			if ( minmax[i][1][1] - minmax[i][0][1] > jumpSize) textureJumps[i][1] = true;
			if (textureJumps[i][0] || textureJumps[i][1]) newVerts += indices[i].length;
		}
		LoggingSystem.getLogger(GeometryUtility.class).log(Level.FINE, "Adding "+newVerts+" vertices");
		IndexedFaceSet dst = new IndexedFaceSet(newVerts+np, nf);
		double[][] oldVerts = src.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[][] on =null, nn = null;
		int nl = 0;
		if (src.getVertexAttributes(Attribute.NORMALS) != null)		{
			on = src.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
			nl = on[0].length;
			nn = new double[newVerts+np][nl];
		}
		double[][] nverts = new double[newVerts+np][oldVerts[0].length];
		double[][] ntex = new double[newVerts+np][textureCoords[0].length];
		int l = oldVerts[0].length;
		for (int i = 0; i<np; ++i)	{
			System.arraycopy(oldVerts[i], 0, nverts[i],0,l );
			System.arraycopy(textureCoords[i], 0, ntex[i], 0, 2);
			if (on != null) System.arraycopy(on[i], 0, nn[i], 0 ,nl);
		}
		int outcount = np;
		for (int i = 0; i<nf; ++i)	{
			if ( textureJumps[i][0] || textureJumps[i][1])		{
				LoggingSystem.getLogger(GeometryUtility.class).log(Level.INFO,"Face "+i);
				for (int j = 0; j<indices[i].length; ++j)	{
					int which = indices[i][j];
					System.arraycopy(oldVerts[which],0,nverts[outcount], 0, l);
					System.arraycopy(textureCoords[which], 0, ntex[outcount], 0 ,2);
					if (on != null) System.arraycopy(on[which], 0, nn[outcount], 0 ,nl);
					for (int k=0;k<2;++k)		{
						if (textureJumps[i][k])	{
							if (ntex[outcount][k] < .5) ntex[outcount][k] += 1.0;//Math.ceil(ntex[outcount][k] - minmax[i][0][k]); 
							LoggingSystem.getLogger(GeometryUtility.class).log(Level.INFO, "Setting texture coordinate to "+ntex[outcount][k]+"from "+textureCoords[which][k]);
												}
					}
					indices[i][j] = outcount;
					outcount++;
				}
			}
			
		}
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(nverts.length);
		ifsf.setVertexCoordinates(nverts);
		ifsf.setFaceCount(indices.length);
		ifsf.setVertexNormals(nn);
		ifsf.setVertexTextureCoordinates(ntex);
		if( src.getFaceAttributes(Attribute.NORMALS) != null)
			ifsf.setFaceNormals( src.getFaceAttributes(Attribute.NORMALS));
		if( src.getFaceAttributes(Attribute.COLORS) != null) 
			ifsf.setFaceColors( src.getFaceAttributes(Attribute.COLORS));
		return dst;
	}

        
    /**
     * Update the vertices of an existing IFS to match the state of <i>box</i>.
     * @param exists
     * @param box
     * @return
     */public static IndexedFaceSet representAsSceneGraph(final IndexedFaceSet exists, Rectangle3D box)	{
		if (exists == null) return representAsSceneGraph(box);
		final double[][] verts = new double[8][3];
		double[][] bnds = box.getBounds();
		for (int i = 0; i<2; ++i)	
			for (int j = 0; j<2; ++j)	
				for (int k = 0; k<2; ++k)	{
					verts[4*i + 2 * j + k][0] = bnds[i][0];
					verts[4*i + 2 * j + k][1] = bnds[j][1];
					verts[4*i + 2 * j + k][2] = bnds[k][2];
				}
		Scene.executeWriter(exists, new Runnable() {

			public void run() {
				exists.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(verts));	
			}
			
		});
		return exists;
	}
   
    /**
     * Represent an instance of {@link Rectangle3D} as an instance of {@link IndexedFaceSet}.
     * 
     * @param box
     * @return
     */
     public static IndexedFaceSet representAsSceneGraph(Rectangle3D box)	{
		double[][] verts = new double[8][3];
		double[][] bnds = box.getBounds();
		for (int i = 0; i<2; ++i)	
			for (int j = 0; j<2; ++j)	
				for (int k = 0; k<2; ++k)	{
					verts[4*i + 2 * j + k][0] = bnds[i][0];
					verts[4*i + 2 * j + k][1] = bnds[j][1];
					verts[4*i + 2 * j + k][2] = bnds[k][2];
				}
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(8);
		ifsf.setFaceCount(6);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceIndices(boxIndices);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}
    
    /**
     * @param normal
     * @param pts
     * @return
     */
    private static int rotationIndex(double[] normal, DoubleArray[] pts) {
        final int numPts = pts.length;
        double[] pt0 = pts[0].toDoubleArray(null); 
        double[] pt1 = pts[1].toDoubleArray(null);
        double dir1[] = Rn.subtract(null,pt1,pt0);
        Rn.normalize(dir1,dir1);
        double dir0[] = new double[dir1.length];
        double angle = 0;
        for(int i = 0;i<numPts;i++) {
            double[] tmp = dir0;
            dir0 = dir1;
            pt0 = pts[(i+1)%numPts].toDoubleArray(null); 
            pt1 = pts[(i+2)%numPts].toDoubleArray(null);
            dir1 = Rn.subtract(tmp,pt1,pt0);
            Rn.normalize(dir1,dir1);
            angle += Math.asin(Rn.innerProduct(normal,Rn.crossProduct(null,dir1,dir0)));
        }
        angle /= 2*Math.PI;
        int i = (int) Math.round(angle);
        return i;
    }
    
    /**
     * 
     * @param ifs
     * @return
     * @author gonska
     */
    public static IndexedFaceSet [] splitIfsToPrimitiveFaces(IndexedFaceSet ifs){
	   	// TODO:
    	// This code doesn't really handle the vertex attributes correctly
    	// it uses the ith vertex attribute as the face attribute for the resulting object
    	// To be correct it should read out the vertex attributes for the ith face and
    	// set the vertex attributes of the new ifs accordingly
    	// But this has low priority since the whole misthafen is a result of a RenderMan
    	// bug which has been fixed in the current release (which we don't have) --gunn
	   	int num=ifs.getNumFaces();
	   	IndexedFaceSet [] parts= new IndexedFaceSet[num];
	   	for (int i=0;i<num;i++){
	   		parts[i]= new IndexedFaceSet();
	   		parts[i].setNumFaces(1);
	   		parts[i].setNumPoints(ifs.getNumPoints());
	   		parts[i].setVertexAttributes(ifs.getVertexAttributes());
	   		
	   		int[][]    oldIndizeesArray=null;
	   		String[]   oldLabelsArray=null;
	   		double[][] oldNormalsArray=null;
	   		double[][] oldTextureCoordsArray=null;
	   		int[][]    newIndizeesArray= new int[1][];
	   		String[]   newLabelsArray=new String[1];
	   		double[][] newNormalsArray=new double[1][];
	   		double[][] newTextureCoordsArray=new double[1][];
	   		
	   		DataList temp=ifs.getFaceAttributes( Attribute.INDICES );
	   		if (temp !=null){
	   			oldIndizeesArray	= temp.toIntArrayArray(null);
	   			newIndizeesArray[0] = oldIndizeesArray[i]; 
	   			parts[i].setFaceAttributes(Attribute.INDICES,
	   					new IntArrayArray.Array(newIndizeesArray));
	   		}
	   		
	   		temp= ifs.getVertexAttributes( Attribute.LABELS );
	   		if (temp!=null){
	   			oldLabelsArray 		= temp.toStringArray(null);
	   			newLabelsArray[0]	= oldLabelsArray[i];
	   			parts[i].setFaceAttributes(Attribute.LABELS,
	   					new StringArray(newLabelsArray));
	   		}
	   		temp= ifs.getVertexAttributes( Attribute.NORMALS );
	   		if (temp!=null){
	   			oldNormalsArray 	= temp.toDoubleArrayArray(null);
	   			newNormalsArray[0] = oldNormalsArray[i]; 
	   			parts[i].setFaceAttributes(Attribute.NORMALS,
	   					new DoubleArrayArray.Array(newNormalsArray));
	   		}
	   		temp= ifs.getFaceAttributes( Attribute.NORMALS );
	   		if (temp!=null){
	   			oldNormalsArray 	= temp.toDoubleArrayArray(null);
	   			newNormalsArray[0] = oldNormalsArray[i]; 
	   			parts[i].setFaceAttributes(Attribute.NORMALS,
	   					new DoubleArrayArray.Array(newNormalsArray));
	   		}
	   		temp= ifs.getVertexAttributes( Attribute.TEXTURE_COORDINATES );
	   		if (temp!=null){
	   			oldTextureCoordsArray = temp.toDoubleArrayArray(null);
	   			newTextureCoordsArray[0] = oldTextureCoordsArray[i]; 
	   			parts[i].setFaceAttributes(Attribute.TEXTURE_COORDINATES,
	   					new DoubleArrayArray.Array(newTextureCoordsArray));
	   		}
	   	}
	   	return parts;
	   }
    
    
   /**
     * This mehtod assumes that the faces in the given IndexedFaceSet are
     * planar embedded polygons. They need not be convex. 
     * Moreover it assumes that the facenormals are set.
     * This method is not R4 safe at the moment. 
     * @param fs
     * @return
     */
    public static IndexedFaceSet triangulate(IndexedFaceSet fs) {
     IndexedFaceSet ts = new IndexedFaceSet();
     DataListSet vertexData = fs.getVertexAttributes();
     ts.setVertexCountAndAttributes(vertexData);
     DataListSet edgeData = fs.getEdgeAttributes();
     ts.setEdgeCountAndAttributes(edgeData);
     
     final int n = fs.getNumFaces(); 
     DataList faceDL = fs.getFaceAttributes(Attribute.INDICES);
     DataList pointDL = fs.getVertexAttributes(Attribute.COORDINATES);
     DataList fNormalDL = fs.getFaceAttributes(Attribute.NORMALS);
     if (fNormalDL == null) {
       double[][] fn = GeometryUtility.calculateFaceNormals(fs);
       fNormalDL = StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(fn);
     }
     
//     System.out.println(" points are "+pointDL);
     
     // here we place the new triangles:
     ArrayList triangles = new ArrayList();
     
     // iterate over all polygons:
     for(int i = 0;i<n;i++) {
         int[] faceIndices = faceDL.item(i).toIntArray(null);
//         System.out.println(" face "+i+" is "+faceDL.item(i));
         // pack the points for this polygon into a list:
         DoubleArray[] pts = new DoubleArray[faceIndices.length];
         for(int j = 0;j<faceIndices.length;j++)
             pts[j] = (pointDL.item(faceIndices[j]).toDoubleArray());
         
         double[] normal = fNormalDL.item(i).toDoubleArray(null);
         normal = Rn.normalize(null,normal);
         int rotationIndex = rotationIndex(normal,pts);
         if(rotationIndex<0) Rn.times(normal,-1,normal);
         // iterate over triplets of succesive points
         final int numPts = faceIndices.length;
         int remainingPts = numPts;
         int first =0, second = 0, third = 0;
         while(remainingPts>3) {
             first = first%numPts;
             //find three successive points:
             while(pts[first]== null) first= (first+1)%numPts;
             second = (first+1)%numPts;
             while(pts[second]== null) second = (second+1)%numPts;
             third = (second+1)%numPts;
             while(pts[third]== null) third = (third+1)%numPts;
              // check triangle for degeneracy and test
             // wether any other point is inside the triangle:
             double[] p1 = pts[first].toDoubleArray(null);
             double[] p2 = pts[second].toDoubleArray(null);
             double[] p3 = pts[third].toDoubleArray(null);
             double[] e1 = Rn.subtract(null,p2,p1);
             double[] e2 = Rn.subtract(null,p3,p2);
             double[] e3 = Rn.subtract(null,p1,p3);

             double[] cnormal = Rn.crossProduct(null,e2,e1);
             double d = Rn.innerProduct(normal, cnormal);
             if(Math.abs(d) < EPS) {
                 System.out.println("Warning degenerate triangle in triangulate... dropping "+second);
                 System.out.println(" ->"+first+" "+second+" "+third);
                 pts[second] = null;
                 remainingPts--;
                 first = second;
                 continue;
             }
             if(d < 0) {
               first++;
               continue;
             }
             boolean allOutside = true;
             for(int k = 0;k<numPts;k++) {
                 if(pts[k] ==null||k == first|| k == second|| k == third) continue;
                 double[] p4 = pts[k].toDoubleArray(null);
                 double[] dir = Rn.subtract(null,p4,p1);
                 double s1 = Rn.innerProduct(normal,Rn.crossProduct(null,e1,dir));
                 dir = Rn.subtract(dir,p4,p2);
                 double s2 = Rn.innerProduct(normal,Rn.crossProduct(null,e2,dir));
                 dir = Rn.subtract(dir,p4,p3);
                 double s3 = Rn.innerProduct(normal,Rn.crossProduct(null,e3,dir));
                 //if(!(s1 <=0 || s2 <=0 || s3<=0)) {
                 if((s1 <0 && s2 <0 && s3<0)) {
                     allOutside = false;
//                     System.out.println("pnt no "+k+" is inside "+s1+"  "+s2+"  "+s3);
//                     System.out.println("  ->"+pts[k]+" is inside");
//                     System.out.println("  ->"+pts[first]);
//                     System.out.println("  ->"+pts[second]);
//                     System.out.println("  ->"+pts[third]);
                     break;
                 } 
             }  
             if(!allOutside) {
                 //if yes continue:
                 first++;
                 continue;
             }
			// if not add the triangle to the list of new polygons and
			 // remove the middle point from pts list:
				 triangles.add(new int[] {faceIndices[first],faceIndices[second],faceIndices[third]});
				 pts[second] = null;
				 remainingPts--;
   //                 System.out.println("adding "+ first+" "+second+" "+third+" ... "+remainingPts+" to go");
			 first++;
         }
         while(pts[first]== null) first= (first+1)%numPts;
         second = (first+1)%numPts;
         while(pts[second]== null) second = (second+1)%numPts;
         third = (second+1)%numPts;
         while(pts[third]== null) third = (third+1)%numPts;
         triangles.add(new int[] {faceIndices[first],faceIndices[second],faceIndices[third]});
//         System.out.println("finally adding "+ first+" "+second+" "+third);
     }
     
     int[][] faces = new int[triangles.size()][];
     faces = (int[][]) triangles.toArray(faces);
     
     ts.setFaceCountAndAttributes(Attribute.INDICES,StorageModel.INT_ARRAY_ARRAY.createReadOnly(faces));
     GeometryUtility.calculateAndSetNormals(ts);
     return ts;
    }
       
    /**
	 * Truncate the corners of each face of <i>ifs</i>.
	 * @param ifs
	 * @return
	 */
    public static IndexedFaceSet truncate(IndexedFaceSet ifs)	{
		int vertcount = 0;
		int[][] ind = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray().toIntArrayArray(null);
		for (int i = 0; i<ind.length; ++i)	{
			vertcount += ind[i].length;
		}
		
		double[][] oldverts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		//double[][] oldverts = ifs.getVertices().getData();
		int vectorLength = oldverts[0].length; //ifs.getVertices().getVectorLength();
		if (vectorLength != 3 && vectorLength != 4)	{
			throw new IllegalArgumentException("Vector length must be 3 or 4");
		}
		double[][] oldverts3;
		if (vectorLength == 4)	{
			oldverts3 = Pn.dehomogenize(null, oldverts);
		} else oldverts3 = oldverts;
		vectorLength = 3;
		int[][] newind;
		double[][] newverts;
//		IndexedFaceSet truncated;
		newind = new int[ind.length][];
		newverts = new double[vertcount][vectorLength];
		for (int i = 0, count = 0; i<ind.length; ++i)	{
			int[] thisf = ind[i];
			newind[i] = new int[thisf.length];
			for (int j = 0; j<thisf.length; ++j)	{
				int k  = (j+1)%thisf.length;
				Rn.add(newverts[count+j], oldverts3[ind[i][j]], oldverts3[ind[i][k]]);
				Rn.times(newverts[count+j], .5, newverts[count+j]);
				newind[i][j] = count+j;
			} 
			count += thisf.length;
		}
		double[][] fn = null, fc = null;
		//imploded = new IndexedFaceSet(newind, new DataGrid(newverts, false), null, null, ifs.getFaceNormals(), ifs.getFaceColors());
		if (ifs.getFaceAttributes(Attribute.NORMALS) != null) fn = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
		if (ifs.getFaceAttributes(Attribute.COLORS) != null) fc = ifs.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null);
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(vertcount);
		ifsf.setFaceCount(ind.length);
		ifsf.setVertexCoordinates(newverts);
		ifsf.setFaceIndices(newind);
		if (fn != null)   ifsf.setFaceNormals(fn);
		else ifsf.setGenerateFaceNormals(true);
		if (fc != null)   ifsf.setFaceColors(fc);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();

//		truncated = createIndexedFaceSetFrom(newind, newverts, null, null, fn, fc);
//		truncated.buildEdgesFromFaces();
//		return truncated;
	}
    
  	public static void assignVertexTangents(IndexedFaceSet ifs) {
  		double[][] tangents = calculateVertexTangents(
  				ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES).toDoubleArrayArray(),
  				ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(),
  				ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(),
  				ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray());
  		ifs.setVertexAttributes(Attribute.attributeForName("TANGENTS"), new DoubleArrayArray.Array(tangents, 4));
  	}
  	
  	/**
  	 * calculates face tangents, converted from
  	 * http://www.terathon.com/code/tangent.php
  	 * 
  	 * @param texCoords
  	 * @param vertexCoordinates
  	 * @param vertexNormals
  	 * @param faceIndices
  	 * @return a double[verts.length][4] array containing the face tangents and the orienation (sign of 4th coordinate).
  	 */
  	public static double[][] calculateVertexTangents(DoubleArrayArray texCoords, DoubleArrayArray vertexCoordinates, DoubleArrayArray vertexNormals, IntArrayArray faceIndices) {
  		double[][] ret=new double[texCoords.getLength()][4];
  		double[][] tan1 = new double[texCoords.getLength()][3];
  		double[][] tan2 = new double[texCoords.getLength()][3];
  		for (int i=0; i < faceIndices.getLength(); i++) {
  			IntArray face = faceIndices.getValueAt(i);
  			for (int j = 0, n = face.getLength()-2; j < n; j++) {
  				int i1 = face.getValueAt(0);
  				int i2 = face.getValueAt(j+1);
  				int i3 = face.getValueAt(j+2);
  				double[] v1 = vertexCoordinates.getValueAt(i1).toDoubleArray(null);
  				double[] v2 = vertexCoordinates.getValueAt(i2).toDoubleArray(null);
  				double[] v3 = vertexCoordinates.getValueAt(i3).toDoubleArray(null);
  				double[] w1 = texCoords.getValueAt(i1).toDoubleArray(null);
  				double[] w2 = texCoords.getValueAt(i2).toDoubleArray(null);
  				double[] w3 = texCoords.getValueAt(i3).toDoubleArray(null);
  				
          double x1 = v2[0] - v1[0];
          double x2 = v3[0] - v1[0];
          double y1 = v2[1] - v1[1];
          double y2 = v3[1] - v1[1];
          double z1 = v2[2] - v1[2];
          double z2 = v3[2] - v1[2];
          
          double s1 = w2[0] - w1[0];
          double s2 = w3[0] - w1[0];
          double t1 = w2[1] - w1[1];
          double t2 = w3[1] - w1[1];
          
          double r = 1. / (s1 * t2 - s2 * t1);
          double[] sdir = new double[]{(t2 * x1 - t1 * x2) * r, (t2 * y1 - t1 * y2) * r,
                  (t2 * z1 - t1 * z2) * r};
          double[] tdir = new double[]{(s1 * x2 - s2 * x1) * r, (s1 * y2 - s2 * y1) * r,
                  (s1 * z2 - s2 * z1) * r};
          
          Rn.add(tan1[i1], tan1[i1], sdir);
          Rn.add(tan1[i2], tan1[i2], sdir);
          Rn.add(tan1[i3], tan1[i3], sdir);
          
          Rn.add(tan2[i1], tan2[i1], tdir);
          Rn.add(tan2[i2], tan2[i2], tdir);
          Rn.add(tan2[i3], tan2[i3], tdir);
  			}
  		}
  		
  		for (int a = 0; a < texCoords.getLength(); a++) {
        double[] n = vertexNormals.getValueAt(a).toDoubleArray(null);
        double[] t = tan1[a];

        // Gram-Schmidt orthogonalize
        double l=Rn.innerProduct(n, t);
        ret[a][0]=l*(t[0]-n[0]);
        ret[a][1]=l*(t[1]-n[1]);
        ret[a][2]=l*(t[2]-n[2]);
        
        Rn.normalize(ret[a], ret[a]);
          
          // Calculate handedness
        ret[a][3] = (Rn.innerProduct(Rn.crossProduct(null, n, t), tan2[a]) < 0) ? -1 : 1;
      }
  		return ret;
  	}
  	
  	private static class Point {
  		double x,y,z,w;
  		Point(DoubleArray da, int digits) {
	  			double r=Math.pow(10, digits);
	  			x=Math.round(r*da.getValueAt(0))/r;
	  			y=Math.round(r*da.getValueAt(1))/r;
	  			z=Math.round(r*da.getValueAt(2))/r;
	  			if (da.getLength() > 3) w=Math.round(r*da.getValueAt(3))/r;
  			}
  		Point(DoubleArray da) {
  			x=da.getValueAt(0);
  			y=da.getValueAt(1);
  			z=da.getValueAt(2);
  			if (da.getLength() > 3) w=da.getValueAt(3);  				
  		}
		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(w);
			result = PRIME * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(x);
			result = PRIME * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(y);
			result = PRIME * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(z);
			result = PRIME * result + (int) (temp ^ (temp >>> 32));
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Point other = (Point) obj;
			if (Double.doubleToLongBits(w) != Double.doubleToLongBits(other.w))
				return false;
			if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
				return false;
			if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
				return false;
			if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
				return false;
			return true;
		}
  	}
  	
  	/**
  	 * Averages the vertex normals for duplicate coordinates. If one normal 
  	 * is antipodal to another, the average is calculated with the antipodal
  	 * one flipped, and the result is then flipped back again.
  	 * 
  	 * @param ifs the IndexedFaceSet to change
  	 * @param maxAngle the maximum angle in degrees between two normals for being flipped
     * @param digits the number of digits to respect for comparing coordinates
  	 */
  	public static void assignSmoothVertexNormals(IndexedFaceSet ifs, double maxAngle, int digits) {
  		assignSmoothVertexNormals(ifs, true, maxAngle, digits);
  	}
  	/**
  	 * Averages the vertex normals for duplicate coordinates.
  	 * @param ifs
  	 * @param digits
  	 */
  	public static void assignSmoothVertexNormals(IndexedFaceSet ifs, int digits) {
  		assignSmoothVertexNormals(ifs, false, -1, digits);
  	}
  	private static void assignSmoothVertexNormals(IndexedFaceSet ifs, boolean flipNormals, double maxAngle, int digits) {
  		double cos = Math.cos(maxAngle*Math.PI/180);
  		HashSet<Integer> written = new HashSet<Integer>();
  		HashMap<Point, LinkedList<Integer>> table = new HashMap<Point, LinkedList<Integer>>() {
  			@Override
  			public LinkedList<Integer> get(Object key) {
  				LinkedList<Integer> ll = super.get(key);
  				if (ll == null) {
  					ll = new LinkedList<Integer>();
  					super.put((Point) key, ll);
  				}
  				return ll;
  			}
  		};
  		DoubleArrayArray points = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
  		for (int i=0, n=points.getLength(); i<n; i++) {
  			table.get(digits > 0 ? new Point(points.getValueAt(i), digits) : new Point(points.getValueAt(i))).add(i);
  		}
  		
  		if (ifs.getVertexAttributes(Attribute.NORMALS) == null) GeometryUtility.calculateAndSetVertexNormals(ifs);
  		
  		DoubleArrayArray normals = ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray();
  		double[][] na=normals.toDoubleArrayArray(null);
  		int total=0;
  		for (LinkedList<Integer> inds : table.values()) {
  			if (inds.size()==1) continue;
  			if (inds.size() > 2) System.out.println(inds.size()+"-fold point");
  			LinkedList<Integer> indices = inds;
  			
  			while (indices.size() > 1) {
  				int cnt=1;
  				LinkedList<Integer> remaining = new LinkedList<Integer>();
	  			double[] n = normals.getValueAt(indices.get(0)).toDoubleArray(null);
	  			Rn.normalize(n, n);
				double[] target = n.clone();
				LinkedList<Integer> flips = new LinkedList<Integer>();
	  			for (int j=1,m=indices.size(); j<m; j++) {
	  				double[] n2 = normals.getValueAt(indices.get(j)).toDoubleArray(null);
	  				Rn.normalize(n2, n2);
	  				if (flipNormals && Rn.innerProduct(n, n2) < 0) {
	  					Rn.times(n2, -1, n2);
	  					flips.add(indices.get(j));
	  				}
	  				if (flipNormals && Rn.innerProduct(n2, n)<cos) {
	  					remaining.add(indices.get(j));
	  					//flips.remove(new Integer(j));
	  					continue;
	  				}
	  				Rn.add(target, target, n2);
	  				cnt++; total++;
	  			}
	  			Rn.normalize(target, target);
	  			for (int i : indices) {
	  				if (remaining.contains(i)) continue;
	  				if (flips.contains(i)) {
	  					//if (Rn.innerProduct(na[i], target) > 0) throw new RuntimeException();
	  					Rn.times(na[i], -1, target);
	  				} else {
	  					//if (Rn.innerProduct(na[i], target) < 0) throw new RuntimeException();
	  					Rn.copy(na[i], target);
	  				}
	  				if (!written.add(i)) throw new RuntimeException();
	  			}
	  			if (Rn.innerProduct(n, target) < 0) throw new RuntimeException();
	  			if (cnt>2) System.out.println("merged "+cnt);
	  			indices=remaining;
  			}
  		}
  		//IndexedFaceSetUtility.assignSmoothVertexNormals(self, 5)
  		System.out.println("merged "+total+" points");
  		ifs.setVertexAttributes(Attribute.NORMALS, new DoubleArrayArray.Array(na, na[0].length));
  	}
  	
  	public static void triangulateBarycentric(IndexedFaceSet ifs) {
  		int[][] f = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
  		DoubleArrayArray points = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
  		LinkedList<double[]> barycenters = new LinkedList<double[]>();
  		int pc = points.size();
  		LinkedList<int[]> faces = new LinkedList<int[]>();
  		LinkedList<int[]> tris = new LinkedList<int[]>();
  		faces.addAll(Arrays.asList(f));
  		int fiberLength=points.getLengthAt(0);
  		double[] tmp = new double[fiberLength];
  		for (Iterator<int[]> it = faces.iterator(); it.hasNext(); ) {
  			int[] face = it.next();
  			if (face.length == 3) tris.add(face);
  			else {
  				double[] barycenter = new double[fiberLength];
  				for (int i : face) {
  					points.getValueAt(i).toDoubleArray(tmp);
  					Rn.add(barycenter, barycenter, tmp);
  				}
  				Rn.times(barycenter, 1./face.length, barycenter);
  				for (int i=0, n=face.length; i<n; i++) {
  					int[] tri = new int[3];
  					tri[0]=pc+barycenters.size();
  					tri[1]=face[i];
  					tri[2]=face[(i+1)%n];
  					tris.add(tri);
  				}
  				barycenters.add(barycenter);
  			}
  		}
  		if (!barycenters.isEmpty()) {
  			double[][] newPoints = points.toDoubleArrayArray(new double[pc+barycenters.size()][]);
  			for (int i = pc; i<newPoints.length; i++) {
  				newPoints[i]=barycenters.removeFirst();
  			}
  			ifs.setVertexCountAndAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(newPoints));
  		}
  		if (tris.size() > faces.size()) {
  			int[][] newFaces = tris.toArray(new int[0][]);
  			ifs.setFaceCountAndAttributes(Attribute.INDICES, new IntArrayArray.Array(newFaces));
  		}
  	}
  	
  	/**
     * Generates a triangulated sphere from a given set of equally spaced longitude (theta) circles.
     * The data for each circle (levels[i]) starts at phi=0 and ends at phi=2PI and is also equally
     * spaced. Computes texture coordinates and smoothes vertex normals along the "cut" phi=0/2*PI.
     * 
     * It is assumed that levels contains the following data:
     * 
     *   levels[i][j] = f(i*PI/(levels.length-1), j*2*PI/(levels[i].length-1),
     * 
     * where f is a function of longitude and lattitude.
     * 
     * NOTE: currently for a pole you need to give 2 points, for phi=0 and phi=2PI.
     */
  	public static IndexedFaceSet triangulateRectangularPatch(double[][][] levels) {
  		double dTheta = 1./(levels.length-1);
  		List<double[]> points = new LinkedList<double[]>();
  		List<double[]> texCoords = new LinkedList<double[]>();
		List<int[]> tris = new LinkedList<int[]>();
		LinkedList<Integer> associatedPoints = new LinkedList<Integer>();
		int lastCnt=0, lastIndex=0;
		int i=0;
		for (double[][] level : levels) {
			int cnt = level.length-1;
			
			// tex coords:
			double theta = dTheta*i;
			double dPhi=1./cnt;
			for (int k=0; k<=cnt; k++) {
				texCoords.add(new double[]{theta, k*dPhi});
			}
			associatedPoints.add(points.size());
			for (double[] p : level) points.add(p);
			associatedPoints.add(points.size()-1);
			if (i>0) {
				tris.addAll(calculateTris(lastCnt, cnt, lastIndex, points.size()-cnt-1));
			}
			i++;
			lastCnt=cnt;
			lastIndex=points.size()-cnt-1;
		}
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(points.size());
		ifsf.setFaceCount(tris.size());
		ifsf.setGenerateVertexNormals(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setVertexCoordinates(points.toArray(new double[0][]));
		ifsf.setVertexTextureCoordinates(texCoords.toArray(new double[0][]));
		ifsf.setFaceIndices(tris.toArray(new int[0][]));
		ifsf.update();
		
		// average normals:
		double[][] normals = ifsf.getIndexedFaceSet().getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
		while (!associatedPoints.isEmpty()) {
			int p1=associatedPoints.removeFirst();
			int p2=associatedPoints.removeFirst();
			normals[p1]=normals[p2]=Rn.times(normals[p1], 0.5, Rn.add(normals[p1], normals[p1], normals[p2]));
		}
		ifsf.getIndexedFaceSet().setVertexAttributes(Attribute.NORMALS, new DoubleArrayArray.Array(normals));
		return ifsf.getIndexedFaceSet();
  	}
  	
  	/**
     * auxilary method for generateSphere
     */
	private static List<int[]> calculateTris(int cntInner, int cntOuter, int iInner, int iOuter) {
		int i=0, j=0;
		double phi1=0, phi2=0, dp1=2*Math.PI/cntInner, dp2=2*Math.PI/cntOuter;
		LinkedList<int[]> tris = new LinkedList<int[]>();
		while (true) {
			if (Math.abs(((phi1+dp1)-phi2)) > Math.abs(((phi2+dp2)-phi1))) {
				// increase outer circle
				tris.add(new int[]{iInner+i, iOuter+j, iOuter+j+1});
				j++;
				phi2+=dp2;
			} else {
				// increase inner circle
				tris.add(new int[]{iInner+i, iOuter+j, iInner+i+1});
				i++;
				phi1+=dp1;
			}
			if (i==cntInner && j==(cntOuter-1)) {
				tris.add(new int[]{iInner+i, iOuter+j, iOuter+j+1});
				break;
			}
			if (j==cntOuter && i==(cntInner-1)){
				tris.add(new int[]{iInner+i, iOuter+j, iInner+i+1});
				break;
			}
		}
		return tris;
	}
	public static PointSet indexedFaceSetToPointSet(IndexedFaceSet f){
		PointSet p= new PointSet(f.getNumPoints());
		p.setGeometryAttributes(f.getGeometryAttributes());
		p.setVertexAttributes(f.getVertexAttributes());
		return p;
	}
	public static IndexedLineSet indexedFaceSetToIndexedLineSet(IndexedFaceSet f){
		IndexedLineSet l= new IndexedLineSet(f.getNumPoints(),f.getNumEdges());
		l.setGeometryAttributes(f.getGeometryAttributes());
		l.setVertexAttributes(f.getVertexAttributes());
		l.setVertexAttributes(f.getEdgeAttributes());
		return l;
	}
	public static IndexedFaceSet indexedLineSetToIndexedFaceSet(IndexedLineSet l){
		if (l instanceof IndexedFaceSet)
			return (IndexedFaceSet) l;
		IndexedFaceSet f= new IndexedFaceSet(l.getNumPoints(),0);
		f.setGeometryAttributes(l.getGeometryAttributes());
		f.setVertexAttributes(l.getVertexAttributes());
		f.setEdgeAttributes(l.getEdgeAttributes());
		return f;
	}
	public static IndexedFaceSet pointSetToIndexedFaceSet(PointSet p){
		if (p instanceof IndexedFaceSet)
			return (IndexedFaceSet) p;
		if (p instanceof IndexedLineSet)
			return indexedLineSetToIndexedFaceSet((IndexedLineSet)p);
		IndexedFaceSet f= new IndexedFaceSet(p.getNumPoints(),0);
		f.setGeometryAttributes(p.getGeometryAttributes());
		f.setVertexAttributes(p.getVertexAttributes());
		return f;
	}
	
  	
}
