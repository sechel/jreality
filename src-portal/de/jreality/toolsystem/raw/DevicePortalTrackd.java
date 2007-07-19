package de.jreality.toolsystem.raw;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JRadioButton;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.portal.PortalCoordinateSystem;
import de.jreality.scene.data.DoubleArray;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.util.Secure;

public class DevicePortalTrackd extends DeviceTrackd {
	
//	public static double portalScale = 1.0;
	static final Matrix HEAD_CALIB = MatrixBuilder.euclidean()
										.rotateY(-Math.PI/2)
										.rotateX(Math.PI/2)
										.rotateY(-Math.PI/2)
										.rotateX(Math.PI/2)
										.rotateZ(rad(-7))
										.rotateY(rad(12))
										.rotateX(rad(-5))
										.translate(0.08, 0., 0.)
										.getMatrix();
	static final Matrix WAND_CALIB = MatrixBuilder.euclidean()
		.rotateZ(Math.PI)
		.rotateX(rad(-5))
		.getMatrix();

	static final double[] FIXED_HEAD, SCALE_MATRIX;
	static {
		System.err.println("DPR: Portal scale is "+PortalCoordinateSystem.getPortalScale());
		FIXED_HEAD = MatrixBuilder.euclidean().translate(0, PortalCoordinateSystem.getPortalScale() * 1.7, 0).getArray();
		SCALE_MATRIX = MatrixBuilder.euclidean().scale(PortalCoordinateSystem.getPortalScale()).getArray();
	}
	public DevicePortalTrackd() {
		JFrame f = new JFrame("Head tracking:");
		ButtonGroup bg = new ButtonGroup();
		JRadioButton b1 = new JRadioButton("enabled");
		b1.setSelected(true);
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				freeHead();
			}
		});
		JRadioButton b2 = new JRadioButton("disabled");
		b2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fixHead();
			}
		});
		bg.add(b1);
		bg.add(b2);
		f.getContentPane().setLayout(new GridLayout(1, 2));
		f.getContentPane().add(b1);
		f.getContentPane().add(b2);
		f.pack();
		f.show();
	}
	
	@SuppressWarnings("serial")
	protected void fixHead() {
		disableSensor(0);
		if (queue != null) {
			ToolEvent te = new ToolEvent(this, System.currentTimeMillis(), sensorSlot(0), null, new DoubleArray(FIXED_HEAD));
			queue.addEvent(te);
		}
	}

	protected void freeHead() {
		enableSensor(0);
	}

	private static double rad(double deg) {
		return deg*Math.PI/180.;
	}
	
	@Override
	protected void calibrate(double[] sensorMatrix, int index) {
		Matrix m = new Matrix(sensorMatrix);
		
		// convert to coordinate system where the origin is in the middle of the bottom of the floor
		m.setEntry(0, 3, m.getEntry(0, 3)/100-PortalCoordinateSystem.xDimPORTAL/2);
		m.setEntry(1, 3, m.getEntry(1, 3)/100);
		m.setEntry(2, 3, m.getEntry(2, 3)/100-PortalCoordinateSystem.xDimPORTAL/2);

		// rotate:
		if (index == 1) m.multiplyOnRight(WAND_CALIB); // wand
		if (index == 0) m.multiplyOnRight(HEAD_CALIB); // head

		// apply portal scale separately to the translation part of the matrices
		if (index == 0 || index == 1  && PortalCoordinateSystem.getPortalScale() != 1.0) {
			double[] tlate = m.getColumn(3);
			tlate = Rn.matrixTimesVector(null, SCALE_MATRIX, tlate);
			m.setColumn(3, tlate);
		}
}
	
	@Override
	public String getName() {
		return "PORTAL: "+super.getName();
	}
}
