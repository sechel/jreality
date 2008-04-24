package de.jreality.jogl;

import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;

public class MatrixListData extends Geometry {
	public MatrixListData(String name) {
		super(name);
	}
	public double[][] matrixList = {Rn.identityMatrix(4)};
	public double minDistance = -1, maxDistance = -1;
	public double ndcFudgeFactor = 1.3;
	public boolean clipToCamera = false;
	public boolean followCamera = false;
	public boolean componentDisplayLists = false;
	public Object dgcf;
	public int signature;
	public int count;
	public int delay = 150;
	public SceneGraphComponent child;
}
