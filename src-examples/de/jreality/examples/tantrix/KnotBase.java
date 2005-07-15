/*
 * Created on Jul 14, 2005
 */
package de.jreality.examples.tantrix;

import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;

public abstract class KnotBase extends IndexedLineSet {
	
	protected double coords[][];
	protected int idx[][];
	
	public int size() {
		return coords.length;
	}
	public double[] get(int i) {
		return coords[(i+size())%size()];
	}
	protected void init() {
        setVertexCountAndAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(coords));
        setEdgeCountAndAttributes(Attribute.INDICES,
        		new IntArrayArray.Array(idx));
	}
}
