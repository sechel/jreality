package de.jreality.util;

import java.util.WeakHashMap;

import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;

/**
 * Utility to store default matrices for {@link Transformation}s.
 * For convienience, there is one shared instance.
 * 
 * @author weissman
 *
 */
public class DefaultMatrixSupport {

	private static final double[] IDENTITY = Rn.identityMatrix(4);
	private WeakHashMap<Transformation, double[]> store = new WeakHashMap<Transformation, double[]>();

	private static final DefaultMatrixSupport instance = new DefaultMatrixSupport();
	
	public static DefaultMatrixSupport getSharedInstance() {
		return instance;
	}
	
	/**
	 * Stores the given array as default matrix for trafo.
	 * @param trafo the trafo for which to set the default matrix
	 * @param defMatrix the default matrix for trafo
	 */
	public void storeDefault(Transformation trafo, double[] defMatrix) {
		store.put(trafo, defMatrix);
	}
	
	/**
	 * Stores the current matrix of trafo as its default matrix.
	 * @param trafo the trafo
	 */
	public void storeAsDefault(Transformation trafo) {
		storeDefault(trafo, trafo.getMatrix());
	}
	
	/**
	 * Restores the default matrix if there is any, otherwise
	 * assigns the identity matrix.
	 * @param trafo the trafo to restore
	 */
	public void restoreDefault(Transformation trafo) {
		double[] defMatrix = store.get(trafo);
		trafo.setMatrix(defMatrix != null ? defMatrix : IDENTITY);
	}
	
  	public void restoreDefaultTransformations(SceneGraphComponent r) {
	  	r.accept(new SceneGraphVisitor() {
	  		@Override
	  		public void visit(SceneGraphComponent c)	{
			  	c.childrenWriteAccept(this, true, false, false, false, false, false);
		  	}
		  	@Override
		  	public void visit(Transformation t) {
		  		restoreDefault(t);
		  	}
	  	});
  	}
  
 	public void setDefaultMatrix(SceneGraphComponent r)	{
	  	r.accept(new SceneGraphVisitor() {
	  		@Override
	  		public void visit(SceneGraphComponent c)	{
			  	c.childrenAccept(this);
		  	}
		  	@Override
		  	public void visit(Transformation t) {
		  		storeAsDefault(t);
		  	}
	  	});
  	}

}
