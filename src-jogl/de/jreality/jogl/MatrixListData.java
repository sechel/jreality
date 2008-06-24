package de.jreality.jogl;

import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;

public class MatrixListData extends Geometry {
	public MatrixListData(String name) {
		super(name);
	}
	public double[][] matrixList = {Rn.identityMatrix(4)};
	public boolean[] visibleList;
	public boolean newVisibleList;
	public boolean rendering = false;
	public boolean copycat = true;
//	public double minDistance = -1, maxDistance = -1;
	public double ndcFudgeFactor = 1.0;
	public double ztlate = .5;
	public boolean clipToCamera = true;
//	public boolean followCamera = false;
	public boolean componentDisplayLists = false;
//	public Object dgcf;
//	public int signature;
	public int count;
	public int delay = 150;
	public SceneGraphComponent child;
}
