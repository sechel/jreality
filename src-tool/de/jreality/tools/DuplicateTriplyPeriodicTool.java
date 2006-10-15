package de.jreality.tools;

import java.util.Iterator;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.util.Rectangle3D;

public class DuplicateTriplyPeriodicTool extends AbstractTool {

	private double[] latticeSpacing;

	public DuplicateTriplyPeriodicTool(double latticeX, double latticeY, double latticeZ) {
		super(InputSlot.getDevice("Duplication"));
		latticeSpacing = new double[]{latticeX, latticeY, latticeZ};
	}

	public void activate(ToolContext tc) {
		PickResult pick = tc.getCurrentPick();
		double[] coords = new double[3];
		System.arraycopy(pick.getObjectCoordinates(), 0, coords, 0, 3);
		System.out.println(pick.getObjectCoordinates()[3]);
		SceneGraphComponent domain = tc.getRootToToolComponent().getLastComponent();
		SceneGraphComponent copy = tc.getRootToLocal().getLastComponent();
		Rectangle3D bb = GeometryUtility.calculateBoundingBox(copy);
		double[] center = bb.getCenter();
		Rn.subtract(coords, coords, center);
		int dir=0;
		if (Math.abs(coords[1])>Math.abs(coords[0])) dir=1;
		if (Math.abs(coords[2])>Math.abs(coords[dir])) dir=2;
		double[] trans=new double[3];
		trans[dir]=Math.signum(coords[dir]) * latticeSpacing[dir];
		SceneGraphComponent newCopy = new SceneGraphComponent();
		MatrixBuilder.euclidean().translate(trans).assignTo(newCopy);
		newCopy.setGeometry(domain.getGeometry());
		copy.addChild(newCopy);
	}
}
