package de.jreality.toolsystem.raw;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;

public class DevicePortalTrackd extends DeviceTrackd {

	/**
	 * IN PROGRESS
	 */
	@Override
	protected void calibrate(double[] sensorMatrix, int index) {
		Matrix m = new Matrix(sensorMatrix);
		m.setEntry(0, 3, m.getEntry(0, 3)/100-1.24);
		m.setEntry(1, 3, m.getEntry(1, 3)/100);//-0.41);
		m.setEntry(2, 3, m.getEntry(2, 3)/100-1.24);
		//System.out.println(m);
		// rotate:
		MatrixBuilder mb = MatrixBuilder.euclidean(m);
		if (index == 1) mb.rotateZ(Math.PI); // wand
		if (index == 0) mb.rotateX(-25./360.*Math.PI).rotateY(-Math.PI/2).rotateX(Math.PI/2).rotateY(-Math.PI/2); //head
	}
	
	@Override
	public String getName() {
		return "PORTAL: "+super.getName();
	}
}
