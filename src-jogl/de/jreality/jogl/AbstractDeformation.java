/*
 * Author	gunn
 * Created on May 2, 2005
 *
 */
package de.jreality.jogl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rn;

/**
 * @author gunn
 *
 */
public abstract class AbstractDeformation {

	/**
	 * 
	 */
	public AbstractDeformation() {
		super();
	}
	
	public abstract double[] valueAt(double[] input, double[] output);

	public static SceneGraphComponent deform(final SceneGraphComponent sgc, final AbstractDeformation deform)	{
	    final HashMap map =new HashMap();
		
	    SceneGraphVisitor v =new SceneGraphVisitor() {
	        public void visit(SceneGraphComponent c) {
	        	    //System.out.println("AbstractDeformation.deform()"+c.getName());
	            c.childrenAccept(this);
	        }
	        public void visit(PointSet ps) {
	        	   double[][] v = ps.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
	        	   double[][] nv = new double[v.length][v[0].length];
	        	   for (int i = 0; i<v.length; ++i)	{
	        	   		deform.valueAt(v[i], nv[i]);
	        	   }
	           map.put(ps, nv);
	        	   //System.out.println("AbstractDeformation.ps()");
	        	   // TODO transform normals also by the inverse matrix
	        }
	        public void visit(Sphere s)	{
	        	    LoggingSystem.getLogger(GeometryUtility.class).log(Level.WARNING, "Can't flatten a sphere");
	        }
	    };
		v.visit(sgc);
        Set keys = map.keySet();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            PointSet i = (PointSet) iter.next();
            double[][] nv = (double[][]) map.get(i);
     	   i.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(nv[0].length).createWritableDataList(nv));
       }
		GeometryUtility.calculateFaceNormals(sgc);
		GeometryUtility.calculateVertexNormals(sgc);
		//System.out.println("Bounding box is "+GeometryUtility.calculateBoundingBox(sgc).toString());
		return sgc;
	}
}
