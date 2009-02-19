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
	public boolean clipToCamera = true;
	public boolean componentDisplayLists = true;
	public int count;
	public int delay = 500;
}
