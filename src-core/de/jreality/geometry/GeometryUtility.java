/*
 * Created on May 7, 2004
 *
 */
package de.jreality.geometry;

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
import de.jreality.scene.proxy.CopyFactory;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rectangle3D;

/*
  * 
  * @author Charles Gunn
 */
public class GeometryUtility {
 
	// TODO: Somewhere it would be good to register the attributes used in the jreality release.
	 public static Attribute SIGNATURE;		// value:	Integer
	 public static Attribute BOUNDING_BOX;		// value:	de.jreality.util.Rectangle3D
	 public static Attribute QUAD_MESH_SHAPE;	// value:	java.awt.Dimension
	 public static Attribute REGULAR_DOMAIN_QUAD_MESH_SHAPE;	// value:	java.awt.Rectangle2D

	 static {
	  	BOUNDING_BOX = Attribute.attributeForName("boundingBox");
	  	SIGNATURE = Attribute.attributeForName("signature");
	  	QUAD_MESH_SHAPE = Attribute.attributeForName( "quadMeshShape"); // vlue is java.awt.Dimension
	  	REGULAR_DOMAIN_QUAD_MESH_SHAPE = Attribute.attributeForName("regularDomainQuadMeshShape");
	  }

	/**
	 * 
	 */
	private GeometryUtility() {
		super();
	}

	   /**
     * inlines the given 2-dim array. Assumes equal length for sub-arrays
     * 
     * @return the target array (a new one if target == null)
     */
    public static double[] inline(double[] target, double[][] src) {
    		// TODO decide if this belongs here -- doesn't have anything to do with Geometry per se.
        final int slotLength = src[0].length;
        if(target==null) target=new double[src.length*slotLength];
        for (int i=0; i<src.length; i++)
            for (int j=0; j<slotLength; j++) target[i*slotLength+j]=src[i][j];
        return target;
    }

	public static int getVectorLength(PointSet ps)		{
		DataList vv = ps.getVertexAttributes(Attribute.COORDINATES);
		return getVectorLength(vv);
	}
	
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
	
	public static void calculateAndSetNormals(IndexedFaceSet ifs)	{
		calculateAndSetFaceNormals(ifs);
		calculateAndSetVertexNormals(ifs);
	}
	
    public static void calculateAndSetFaceNormals(IndexedFaceSet ifs)   {
        double[][] fn = calculateFaceNormals(ifs);
        ifs.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(fn[0].length).createReadOnly(fn));
    }
    
    public static void calculateAndSetVertexNormals(IndexedFaceSet ifs) {
        double[][] vn = calculateVertexNormals(ifs);
        ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(vn[0].length).createReadOnly(vn));
    }

    /**
     * @deprecated Until the use of DOUBLE3_INLINED is removed, non-euclidean geometries have 4D normals
     * @param ifs
     */public static void calculateAndSetFaceNormalsInlined(IndexedFaceSet ifs)   {
        double[] fn = inline(null, calculateFaceNormals(ifs));
        ifs.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE3_INLINED.createReadOnly(fn));
    }
    
     /**
      * @deprecated Until the use of DOUBLE3_INLINED is removed, non-euclidean geometries have 4D normals
      * @param ifs
      */public static void calculateAndSetVertexNormalsInlined(IndexedFaceSet ifs) {
        double[] vn = inline(null, calculateVertexNormals(ifs));
        ifs.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE3_INLINED.createReadOnly(vn));
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
	public static double[][] calculateFaceNormals(int[][] indices, double[][] verts, int signature)	{
		if (indices == null) return null;
		int normalLength = 4;
		// TODO fix this when non-euclidean shading is working!
		signature = Pn.EUCLIDEAN;
		if (signature == Pn.EUCLIDEAN)	normalLength = 3;
		double[][] fn = new double[indices.length][normalLength];
		if (signature == Pn.EUCLIDEAN && verts[0].length == 4) Pn.dehomogenize(verts,verts);
		for (int i=0; i<indices.length; ++i)	{
			if (indices[i].length < 3) continue;
			if (signature == Pn.EUCLIDEAN)	{		
				// not necessary but probably a bit faster
				double[] v1 = Rn.subtract(null, verts[indices[i][1]], verts[indices[i][0]]);
				double[] v2 = Rn.subtract(null, verts[indices[i][2]], verts[indices[i][0]]);
				Rn.crossProduct(fn[i], v1,v2);
				Rn.normalize(fn[i], fn[i]);
			} else {
				double[] osculatingPlane = P3.planeFromPoints(null, verts[indices[i][0]], verts[indices[i][1]], verts[indices[i][2]]);
				double[] normal = Pn.polarizePlane(null, osculatingPlane,signature);					
				Pn.setToLength(normal, normal, 1.0, signature);
				normal[0] *= -1; normal[1] *= -1; normal[2] *= -1;
				System.arraycopy(normal, 0, fn[i], 0, normalLength);				
			}
		}
		return fn;
	}
    
	
	public static double[][] calculateVertexNormals(IndexedFaceSet ifs)	{
		Object sigO = ifs.getGeometryAttributes(SIGNATURE);
		int sig = Pn.EUCLIDEAN;
		if (sigO != null && sigO instanceof Integer)	{
			sig = ((Integer) sigO).intValue();
		}
		return calculateVertexNormals(ifs, sig);
	}
   
  public static double[][] calculateVertexNormals(IndexedFaceSet ifs, int signature)  {
    int[][] indices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
    if (indices == null) return null;
    double[][] fn = null;
    if (ifs.getFaceAttributes(Attribute.NORMALS) == null) {
      fn = calculateFaceNormals(ifs, signature);      
    } else  
      fn = ifs.getFaceAttributes(Attribute.NORMALS).toDoubleArrayArray(null);
    double[][] vertsAs2D = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
    return calculateVertexNormals(indices, vertsAs2D, fn, signature);
  }

  public static double[][] calculateVertexNormals(int[][] indices, double[][] vertsAs2D, double[][] fn, int signature)	{
		double[][] nvn = new double[vertsAs2D.length][3];
		for (int j = 0; j<indices.length; ++j)	{
			for (int k = 0; k<indices[j].length; ++k)	{
				int m = indices[j][k];
				Rn.add(nvn[m], fn[j], nvn[m]);
			}
		}
		if (signature == Pn.EUCLIDEAN) Rn.normalize(nvn, nvn);
		else Pn.normalize(nvn, nvn, signature);
		return nvn;
	}
	
    public static void calculateVertexNormals(SceneGraphComponent c) {
        // We have to use the map at the moment, since the visit sets
        // a read lock, that prevents us from modifying the indexed face set
        // while visiting it.
        final HashMap map =new HashMap();
        SceneGraphVisitor v =new SceneGraphVisitor() {
            public void visit(SceneGraphComponent c) {
                c.childrenAccept(this);
            }
            public void visit(IndexedFaceSet i) {
                if(i.getVertexAttributes(Attribute.NORMALS)== null) {
                    double[][] n = calculateVertexNormals(i);
                    map.put(i,n);            
                }

                super.visit(i);
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
	
    public static void calculateFaceNormals(SceneGraphComponent c) {
        // We have to use the map at the moment, since the visit sets
        // a read lock, that prevents us from modifying the indexed face set
        // while visiting it.
        final HashMap map =new HashMap();
        SceneGraphVisitor v =new SceneGraphVisitor() {
            public void visit(SceneGraphComponent c) {
                c.childrenAccept(this);
            }
            public void visit(IndexedFaceSet i) {
                if(i.getFaceAttributes(Attribute.NORMALS)== null) {
                    double[][] n = calculateFaceNormals(i);
                    map.put(i,n);
                    
                }

                super.visit(i);
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

	public static Rectangle3D calculateChildrenBoundingBox(SceneGraphComponent sgc)	{
		SceneGraphComponent tmp = new SceneGraphComponent();
		for (int i =0; i<sgc.getChildComponentCount(); ++i)	
			tmp.addChild(sgc.getChildComponent(i));
		
		tmp.setGeometry(sgc.getGeometry());
		return calculateBoundingBox(null, tmp);
	}

	public static Rectangle3D calculateBoundingBox(Sphere sph)	{
		return SphereUtility.getSphereBoundingBox();
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
	
	public static void setSignature(Geometry g, int s)	{
		Object o = g.getGeometryAttributes(SIGNATURE);
		if (o != null && o instanceof Integer)		{
			if (((Integer) o).intValue() == s) return;			//unchanged
		}
		g.setGeometryAttributes(SIGNATURE, new Integer(s));
	}
	
	/**
	 * Goes through a SGC and finds all PointSet instances.  It transforms them into world coordinates and puts it into a 
	 * new SceneGraphComponent instance.  All these instances are put into one parent, and this parent is returned. 
	 * Geometry that is not PointSet is simply ignored.
	 * @param sgc
	 * @return
	 */public static SceneGraphComponent flatten(SceneGraphComponent sgc)		{
		
	    final double[] flipit = P3.makeStretchMatrix(null, new double[] {-1,0, -1,0, -1.0});
		final ArrayList geoms = new ArrayList();
		//TODO evaluate the appearance also and stick it in the flattened node with the geometry.
	    SceneGraphVisitor v =new SceneGraphVisitor() {
	    	    SceneGraphPath thePath = new SceneGraphPath();
	    	    
            public void visit(SceneGraphComponent c) {
            	   thePath.push(c);
                c.childrenAccept(this);
               //if (c.getTransformation() != null) c.getTransformation().setMatrix(Rn.identityMatrix(4));
               //c.setName(c.getName() + "_flat");
                thePath.pop();
            }
            public void visit(PointSet oldi) {
            	// have to copy the geometry in case it is reused!
            	   CopyFactory cf = new CopyFactory();
            	   oldi.accept(cf);
            	   PointSet i = (PointSet) cf.getProxy();
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

    /**
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
}

