package de.jreality.jogl;

import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;

public class DiscreteGroupData extends Geometry {
	public DiscreteGroupData(String name) {
		super(name);
	}
	public double[][] matrixList = {Rn.identityMatrix(4)};
	public double minDistance = -1, maxDistance = -1;
	public boolean clipToCamera = false;
	public int signature;
	public int count;
	public int delay = 50;
	public SceneGraphComponent child;
}
