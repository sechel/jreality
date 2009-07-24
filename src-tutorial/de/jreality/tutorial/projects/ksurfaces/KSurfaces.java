package de.jreality.tutorial.projects.ksurfaces;


/**  The formulas to generate K-surfaces. 
 * 
 * @author Ulrich Pinkall, adapted for the tutorial by G. Paul Peters, 24.07.2009
 * @see Ulrich Pinkall: "Designing Cylinders with Constant Negative Curvature", in 
 * Discrete Differential Geometry, pages 57-66. Springer 2008.
 *
 */
public class KSurfaces {
	/** Computes the Gauss map of a K-surface from initial Cauchy data, i.e., two closed curves
	 * 
	 * @param initialAnnulus the initial data, i.e., a double array double[2][n][3] containing 2 polygons with n vertices,
	 *  which are both interpreted as closed curves connecting that last and the first one. 
	 * @param target a double array that may be filled with the result, i.e., an array double[m][n][3], where m&gt;1 is the number of
	 * 	time steps to be calculated.
	 */
	public static void gaussMapFromInitialAnnulus(double[][][] initialAnnulus, double[][][] target) {
		final int m = target.length;
		final int n = target[0].length;
		final double[] a = new double[3];
		
		// copy first two rows
		for (int i=0; i<2; i++) {
			for (int j=0; j<n; j++) {
				R3.copy(initialAnnulus[i][j], target[i][j]);
			}
		}
		
		// compute the other rows
		for (int i=2; i<m; i++) {
			for (int j=0; j<n; j++) {
				int k = j==0 ? n-1 : j-1;
				R3.plus(target[i-1][k], target[i-1][j], a);
				double[] w = target[i-2][k];
				double s = 2 * R3.dot(a,w) / R3.dot(a,a);
				R3.times(s, a, a);
				R3.minus(a, w, target[i][j]);
			}
		}
	}
	

}
