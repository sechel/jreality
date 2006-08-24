package de.jreality.toolsystem.raw;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;

public class DevicePortalTrackd extends DeviceTrackd {

	static final Matrix HEAD_CALIB = MatrixBuilder.euclidean()
										.rotateY(-Math.PI/2)
										.rotateX(Math.PI/2)
										.rotateY(-Math.PI/2)
										.rotateX(Math.PI/2)
										.rotateZ(rad(-7))
										.rotateY(rad(12))
										.rotateX(rad(-5))
										.getMatrix();
	static final Matrix WAND_CALIB = MatrixBuilder.euclidean()
										.rotateZ(Math.PI)
										//.rotateZ(rad(-2))										
										//.rotateX(rad(-9))
										.getMatrix();
	
	private static double rad(double deg) {
		return deg*Math.PI/180.;
	}
	
	/**
	 * IN PROGRESS
	 */
	@Override
	protected void calibrate(double[] sensorMatrix, int index) {
		Matrix m = new Matrix(sensorMatrix);
		m.setEntry(0, 3, m.getEntry(0, 3)/100-1.24);
		m.setEntry(1, 3, m.getEntry(1, 3)/100);
		m.setEntry(2, 3, m.getEntry(2, 3)/100-1.24);
		//System.out.println(m);
		// rotate:
		if (index == 1) m.multiplyOnRight(WAND_CALIB); // wand
		if (index == 0) m.multiplyOnRight(HEAD_CALIB); // head
	}
	
	@Override
	public String getName() {
		return "PORTAL: "+super.getName();
	}
}
