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

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

/**
 * Static methods for various geometric operations.
 * <p>
 * There are a few basic categories:
 * <ul>
 * <li>static fields for specific geometry attributes, </li>
 * <li>methods for calculating normal vectors for {@link IndexedFaceSet} instances,</li>
 * <li>methods for calculating bounding boxes for {@link PointSet} instances, </li>
 * <li>analogous methods which traverse scene graphs, and </li>
 * <li>methods for transforming the geometry in a scene graph in some way.</li>
 * </ul>
 * @author Charles Gunn and others
 *
 */
public class GeometryUtility {
 
	/**
	 * For setting the bounding box of the geometry;
	 * Value: {@link Rectangle3D}.
	 * @see Geometry#setGeometryAttributes(Attribute, Object).
	 */
	 public static String BOUNDING_BOX = "boundingBox";		// value:	de.jreality.util.Rectangle3D
	/**
	 * For identifying this IndexedFaceSet as a QuadMesh;
	 * Value: {@link java.awt.Dimension}.
	 * @see Geometry#setGeometryAttributes(Attribute, Object).
	 * @see QuadMeshFactory
	 */
	 public static String QUAD_MESH_SHAPE = "quadMesh";	// value:	java.awt.Dimension
	/**
	 * For identifying this IndexedFaceSet as a QuadMesh with a single
	 * value at each point (z-value on a regular x-y 2D domain);
	 * Value: {@link Rectangle2D} identifies the 2D domain.
	 * @see Geometry#setGeometryAttributes(Attribute, Object).
	 * @see HeightFieldFactory
	 */
	 public static String HEIGHT_FIELD_SHAPE = "heightField";	// value:	java.awt.Rectangle2D
	/**
	 * For setting the signature ({@link Pn}) of the geometry; 
	 * Value: {@link Integer}
	 * @see Geometry#setGeometryAttributes(Attribute, Object).
	 */
	 public static String SIGNATURE = "signature";		// value:	Integer

//	 static {
//	  	BOUNDING_BOX = Attribute.attributeForName("boundingBox");
//	  	SIGNATURE = Attribute.attributeForName("signature");
//	  	QUAD_MESH_SHAPE = Attribute.attributeForName( "quadMeshShape"); // vlue is java.awt.Dimension
//	  	REGULAR_DOMAIN_QUAD_MESH_SHAPE = Attribute.attributeForName("regularDomainQuadMeshShape");
//	  }
//
	private GeometryUtility() {}
	
	public static void calculateAndSetFaceNormals(IndexedFaceSet ifs)   {
		if (ifs.getNumFaces() == 0) return;
        double[][] fn = calculateFaceNormals(ifs);
        ifs.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(fn[0].length).createReadOnly(fn));
    }

	public static void calculateAndSetNormals(IndexedFaceSet ifs)	{
		calculateAndSetFaceNormals(ifs);
		calculateAndSetVertexNormals(ifs);
	}

	public static void calculateAndSetVertexNormals(IndexedFaceSet ifs) {
		if (ifs.getNumFaces() == 0) return;
        double[][] vn = calculateVertexNormals(ifs);
        ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(vn[0].length).createReadOnly(vn));
    }
	
	/**
     * Calculate the bounding box assuming that the scene graph is first transformed by
     * the matrix <i>initialMatrix</i>
     * @param tmp
     * @param sgc
     * @return
     */
    public static Rectangle3D calculateBoundingBox(double[] initialMatrix, SceneGraphComponent sgc) {
		BoundingBoxTraversal bbt = new BoundingBoxTraversal();
		if (initialMatrix!=null) bbt.setInitialMatrix(initialMatrix);
		bbt.traverse(sgc);
		return bbt.getBoundingBox();
    }
	
	/**
	 * Calculate the bounding box of the vertices <i>verts</i>. These may be
	 * 3- or 4-d points. 
	 * @param verts
	 * @return
	 * {@see Pn} for details.
	 */
     public static Rectangle3D calculateBoundingBox(double[][] verts)	{
		double[][] bnds = new double[2][3];
		if (verts[0].length == 4)	{
			Pn.calculateBounds(bnds, verts);
		} else {
			Rn.calculateBounds(bnds, verts);
		}
		Rectangle3D r3d = new Rectangle3D();
		r3d.setBounds(bnds);
		return r3d;
	}
	
    public static Rectangle3D calculateBoundingBox(PointSet ps)	{
		double[][] verts = ps.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		return calculateBoundingBox(verts);
	}
    
    public static Rectangle3D calculateBoundingBox(SceneGraphComponent sgc)	{
		return calculateBoundingBox(null, sgc);
	}

  	public static Rectangle3D calculateBoundingBox(Sphere sph)	{
		return SphereUtility.getSphereBoundingBox();
	}
   
	/**
	 * Calculate the bounding box for the scene graph tooted at <i>sgc</i> but
	 * do not apply the transformation, if any, attached to <i>sgc</i>.
	 * @param sgc
	 * @return
	 */
  	public static Rectangle3D calculateChildrenBoundingBox(SceneGraphComponent sgc)	{
		SceneGraphComponent tmp = new SceneGraphComponent();
		for (int i =0; i<sgc.getChildComponentCount(); ++i)	
			tmp.addChild(sgc.getChildComponent(i));
		
		tmp.setGeometry(sgc.getGeometry());
		return calculateBoundingBox(null, tmp);
	}
	
	public static double[][] calculateFaceNormals(IndexedFaceSet ifs)	{
		Object sigO = ifs.getGeometryAttributes(SIGNATURE);
		int sig = Pn.EUCLIDEAN;
		if (sigO != null && sigO instanceof Integer)	{
			sig = ((Integer) sigO).intValue();
			LoggingSystem.getLogger(GeometryUtility.class).log(Level.FINER,"Calculating normals with signature "+sig);
		}
		return calculateFaceNormals(ifs,sig);
	}
    
	
	public static double[][] calculateFaceNormals(IndexedFaceSet ifs, int signature) {
	   int[][] indices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
	   double[][] verts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
	   return calculateFaceNormals(indices, verts, signature);
	}
   
	/**
	 * Calculate face normals for the faces defined by the index list <i>indices</i> and 
	 * the vertex list <i>verts</i>, with respect to the given </i>signature</i>. The method attempts
	 * to skip over degenerate vertices (in the euclidean case only currently!), 
	 * but otherwise assumes the faces are planar.
	 * @param indices
	 * @param verts
	 * @param signature
	 * @return
	 */
	public static double[][] calculateFaceNormals(int[][] indices, double[][] verts, int signature)	{
		if (indices == null) return null;
		int normalLength = 4;
		//System.err.println("Sig is "+signature);
		if (signature == Pn.EUCLIDEAN)	normalLength = 3;
		double[][] fn = new double[indices.length][normalLength];
		if (signature == Pn.EUCLIDEAN && verts[0].length == 4) Pn.dehomogenize(verts,verts);
		for (int i=0; i<indices.length; ++i)	{
			int n = indices[i].length;
			if (n < 3) continue;
			if (signature == Pn.EUCLIDEAN)	{		
				// not necessary but probably a bit faster
				// have to find a non-degenerate set of 3 vertices
				int count = 1;
				double[] v1 = null;
				do {
					v1 = Rn.subtract(null, verts[indices[i][count++]], verts[indices[i][0]]);
				} while (Rn.euclideanNorm(v1) < 10E-16 && count < (n-1));
				double[] v2 = null;
				do {
					v2 = Rn.subtract(null, verts[indices[i][count++]], verts[indices[i][0]]);
				} while (Rn.euclideanNorm(v2) < 10E-16 && count < (n));
				if (count > n) continue;
				Rn.crossProduct(fn[i], v1,v2);
				Rn.normalize(fn[i], fn[i]);
			} else {
				// TODO find non-degenerate set of 3 vertices here also
				double[] osculatingPlane = P3.planeFromPoints(null, verts[indices[i][0]], verts[indices[i][1]], verts[indices[i][2]]);
				double[] normal = Pn.polarizePlane(null, osculatingPlane,signature);	
				Pn.setToLength(normal, normal, 1.0, signature);
				if (normal[3] < 0) Rn.times(normal, -1, normal);
	//				double[] np = new double[3];
	//				for (int k = 0; k<3; ++k)	{
	//					np[k] = Pn.innerProduct(normal, verts[indices[i][k]], signature);
	//				}
//				double[] v4 = Pn.homogenize(null, verts[indices[i][0]]);
//				System.err.println("N.P "+Pn.innerProduct(normal,v4, signature));
				System.arraycopy(normal, 0, fn[i], 0, normalLength);				
			}
		}
		return fn;
	}
	
	  /**
	  * Traverse a scene graph, calculating (and setting) face normals for 
	  * all instances of {@link IndexedFaceSet}.  If face normals are already
	  * present, they are not calculated again. The setting has to take place
	  * after the traversal due to locking considerations.  
	  * @param c
	  */
	 public static void calculateFaceNormals(SceneGraphComponent c) {
	    // We have to use the map at the moment, since the visit sets
	    // a read lock, that prevents us from modifying the indexed face set
	    // while visiting it.
	    final HashMap map =new HashMap();
	    SceneGraphVisitor v =new SceneGraphVisitor() {
	        public void visit(IndexedFaceSet i) {
	            if(i.getFaceAttributes(Attribute.NORMALS)== null) {
	                double[][] n = calculateFaceNormals(i);
	                map.put(i,n);
	                
	            }
	
	            super.visit(i);
	        }
	        public void visit(SceneGraphComponent c) {
	            c.childrenAccept(this);
	        }
	    };
	    v.visit(c);
	    Set keys = map.keySet();
	    for (Iterator iter = keys.iterator(); iter.hasNext();) {
	        IndexedFaceSet i = (IndexedFaceSet) iter.next();
	        double[][] n = (double[][]) map.get(i);
	        int nLength = n[0].length;
	        i.setFaceAttributes(Attribute.NORMALS,
	                StorageModel.DOUBLE_ARRAY.array(nLength).createWritableDataList(n));
	    }
	}
	
    public static double[][] calculateVertexNormals(IndexedFaceSet ifs)	{
		Object sigO = ifs.getGeometryAttributes(SIGNATURE);
		int sig = Pn.EUCLIDEAN;
		if (sigO != null && sigO instanceof Integer)	{
			sig = ((Integer) sigO).intValue();
		}
		return calculateVertexNormals(ifs, sig);
	}
	
     public static double[][] calculateVertexNormals(IndexedFaceSet ifs,
				int signature) {
			int[][] indices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
			if (indices == null)return null;
			double[][] fn = null;
			if (ifs.getFaceAttributes(Attribute.NORMALS) == null) {
				fn = calculateFaceNormals(ifs, signature);
			} else
				fn = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
			double[][] vertsAs2D = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			return calculateVertexNormals(indices, vertsAs2D, fn, signature);
		}


	/**
	   * Calculate the vertex normals of the vertices by averaging the face normals 
	   * of all faces to which the vertex belongs.  
	   * <p>
	   * <b>Note:</b> This method currently does not
	   * correctly average vertices lying on the boundary of a closed {@link #QUAD_MESH_SHAPE quad mesh}.
	   * @param indices
	   * @param vertsAs2D
	   * @param fn
	   * @param signature
	   * @return
	   */
     public static double[][] calculateVertexNormals(int[][] indices,
				double[][] vertsAs2D, double[][] fn, int signature) {
    	 	int n = fn[0].length;
			double[][] nvn = new double[vertsAs2D.length][n];
			// TODO average only after normalizing wrt the signature
			for (int j = 0; j < indices.length; ++j) {
				for (int k = 0; k < indices[j].length; ++k) {
					int m = indices[j][k];
					Rn.add(nvn[m], fn[j], nvn[m]);
				}
			}
			if (signature == Pn.EUCLIDEAN)
				Rn.normalize(nvn, nvn);
			else
				Pn.normalize(nvn, nvn, signature);
			return nvn;
		}

	/**
     * Traverse a scene graph, calculating (and setting) vertex normals for 
     * all instances of {@link IndexedFaceSet}.  If vertex normals are already
     * present, they are not calculated again. The setting has to take place
     * after the traversal due to locking considerations.  
     * @param c
     */
     public static void calculateVertexNormals(SceneGraphComponent c) {
        // We have to use the map at the moment, since the visit sets
        // a read lock, that prevents us from modifying the indexed face set
        // while visiting it.
        final HashMap map =new HashMap();
        	SceneGraphVisitor v =new SceneGraphVisitor() {
            public void visit(IndexedFaceSet i) {
                if(i.getVertexAttributes(Attribute.NORMALS)== null) {
                    double[][] n = calculateVertexNormals(i);
                    map.put(i,n);            
                }

                super.visit(i);
            }
            public void visit(SceneGraphComponent c) {
                c.childrenAccept(this);
            }
        };
        v.visit(c);
        Set keys = map.keySet();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            IndexedFaceSet i = (IndexedFaceSet) iter.next();
            double[][] n = (double[][]) map.get(i);
            int nLength = n[0].length;
             i.setVertexAttributes(Attribute.NORMALS,
                    StorageModel.DOUBLE_ARRAY.array(nLength).createWritableDataList(n));
        }
    }
	
	/**
	 * Apply transformations recursively to all instances of {@link PointSet} and
	 * produce a flat scene graph with no transformations.  
	 * It collects these instances, and transforms them into world coordinates. 
	 * All these instances are put into one parent, and this parent is returned. 
	 * Geometry that is not PointSet is simply ignored. Attributes are copied as much
	 * as possible, normals are also transformed.  The code is not robust.
	 * @param sgc
	 * @return
	 */
	 public static SceneGraphComponent flatten(SceneGraphComponent sgc)		{
		
	    final double[] flipit = P3.makeStretchMatrix(null, new double[] {-1,0, -1,0, -1.0});
		final ArrayList geoms = new ArrayList();
		//TODO evaluate the appearance also and stick it in the flattened node with the geometry.
	    SceneGraphVisitor v =new SceneGraphVisitor() {
	    	    SceneGraphPath thePath = new SceneGraphPath();
	    	    
            public void visit(PointSet oldi) {
            	// have to copy the geometry in case it is reused!
            	PointSet i = (PointSet) SceneGraphUtility.copy(oldi);
            	//System.err.println("point set is "+i);
            	if (i.getVertexAttributes(Attribute.COORDINATES) == null) return;
           	    double[][] v = i.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
            	double[] currentMatrix = thePath.getMatrix(null);
            	double[][] nv = Rn.matrixTimesVector(null, currentMatrix, v);
            	i.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
                double[] cmp = null;
         	    if (i instanceof IndexedFaceSet)	{
            	    IndexedFaceSet ifs = (IndexedFaceSet) i;
                    double[] mat = Rn.transpose(null, currentMatrix);          	
                    mat[12] = mat[13] = mat[14] = 0.0;
                    Rn.inverse(mat, mat);
//             	   if (Rn.determinant(currentMatrix) < 0.0)	cmp = Rn.times(null, flipit, mat);
//             	   else 
             	   cmp = mat;
            	   if (ifs.getFaceAttributes(Attribute.NORMALS) != null)	{
               	   //System.out.println("Setting face normals");
            	v = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
                    nv = Rn.matrixTimesVector(null, cmp, v);
                    ifs.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
            	       } else calculateAndSetFaceNormals(ifs);
               	   if (ifs.getVertexAttributes(Attribute.NORMALS) != null)	{
           	   		//System.out.println("Setting vertex normals");
                      v = ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
                        nv = Rn.matrixTimesVector(null, cmp, v);
                        ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
            	       } else calculateAndSetVertexNormals(ifs);
              	   if (Rn.determinant(currentMatrix) < 0.0)	{           	
               	   		//System.out.println("Flipping normals");
               	   		v = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
               	   		nv = Rn.matrixTimesVector(null, flipit, v);
               	   		ifs.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
               	   		v = ifs.getVertexAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
               	   		nv = Rn.matrixTimesVector(null, flipit, v);
               	   		ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
             	   }
           	   }
         	   //System.out.println("det is "+Rn.determinant(currentMatrix));
//	          if (Rn.determinant(currentMatrix) < 0.0)	{
	                SceneGraphComponent foo = new SceneGraphComponent();
	                foo.setGeometry(i);
	                if (thePath.getLastComponent().getAppearance() != null)	{
	                	foo.setAppearance(thePath.getLastComponent().getAppearance());
	                }
	                geoms.add(foo);
	         	   	
//          	   }
             }
            public void visit(SceneGraphComponent c) {
            	   thePath.push(c);
                c.childrenAccept(this);
               //if (c.getTransformation() != null) c.getTransformation().setMatrix(Rn.identityMatrix(4));
               //c.setName(c.getName() + "_flat");
                thePath.pop();
            }
            public void visit(Sphere s)	{
            	    LoggingSystem.getLogger(GeometryUtility.class).log(Level.WARNING, "Can't flatten a sphere yet");
            }
        };
        v.visit(sgc);
        SceneGraphComponent flat = new SceneGraphComponent();
        if (sgc.getAppearance() != null) flat.setAppearance(sgc.getAppearance());
        for (Iterator iter = geoms.iterator(); iter.hasNext();) {
             SceneGraphComponent foo = (SceneGraphComponent)iter.next(); ;
             flat.addChild(foo);
       }
        // TODO do this correclty: transform existing normals if there are any.
        //GeometryUtility.calculateFaceNormals(flat);
        //GeometryUtility.calculateVertexNormals(flat);
       return flat;
	}

	public static int getSignature(Geometry g ) {
		Object sigO = g.getGeometryAttributes(SIGNATURE);
		int sig = Pn.EUCLIDEAN;
		if (sigO != null && sigO instanceof Integer)	{
			sig = ((Integer) sigO).intValue();
			LoggingSystem.getLogger(GeometryUtility.class).log(Level.FINER,"Calculating normals with signature "+sig);
		}
		return sig;
	}

	/**
	 * Find out the length of the first element of this {@link DataList}.
	 * 
	 * @param ps
	 * @return
	 */
	 public static int getVectorLength(DataList ps)		{
		int[] dims = ps.getStorageModel().getDimensions(ps);
		int vl = dims[dims.length-1];
		if (vl == -1)		// not set; assume uniform
		{
		   ps=(DataList) ps.item(0);
		   vl = ps.size();
		}
		return vl;
	}
	
	/**
	 * Find out the length of the coordinates for a single vertex of this {@link PointSet}.
	 * @param ps
	 * @return
	 */public static int getVectorLength(PointSet ps)		{
		DataList vv = ps.getVertexAttributes(Attribute.COORDINATES);
		return getVectorLength(vv);
	}
	
	/**
     * inlines the given 2-dim array. Assumes equal length for sub-arrays
     * 
     * @return the target array (a new one if target == null)
     */
    static double[] inline(double[] target, double[][] src) {
    		// TODO decide if this belongs here -- doesn't have anything to do with Geometry per se.
        final int slotLength = src[0].length;
        if(target==null) target=new double[src.length*slotLength];
        for (int i=0; i<src.length; i++)
            for (int j=0; j<slotLength; j++) target[i*slotLength+j]=src[i][j];
        return target;
    }
	
	/**
	 * Set the signature ({@link Pn}) associated to this geometry.
	 * @param g
	 * @param s
	 */
    public static void setSignature(Geometry g, int s)	{
		Object o = g.getGeometryAttributes(SIGNATURE);
		if (o != null && o instanceof Integer)		{
			if (((Integer) o).intValue() == s) return;			//unchanged
		}
		g.setGeometryAttributes(SIGNATURE, new Integer(s));
	}

}

