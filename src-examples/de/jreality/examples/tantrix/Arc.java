/*
 * Created on Jul 14, 2005
 */
package de.jreality.examples.tantrix;

public abstract class Arc {
	protected double[][] coords;
	
	public int size() {
		return coords.length;
	}
	public double[] get(int i) {
		return coords[i];
	}
}
