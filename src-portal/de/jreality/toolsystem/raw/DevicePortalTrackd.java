package de.jreality.toolsystem.raw;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JRadioButton;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.data.DoubleArray;
import de.jreality.toolsystem.ToolEvent;

public class DevicePortalTrackd extends DeviceTrackd {
	
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
	
	static final double[] FIXED_HEAD = MatrixBuilder.euclidean().translate(0, 1.7, 0).getArray();
	
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
			ToolEvent te = new ToolEvent(this, sensorSlot(0), null, new DoubleArray(FIXED_HEAD));
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
		
		m.setEntry(0, 3, m.getEntry(0, 3)/100-1.24);
		m.setEntry(1, 3, m.getEntry(1, 3)/100);
		m.setEntry(2, 3, m.getEntry(2, 3)/100-1.24);

		// rotate:
		if (index == 1) m.multiplyOnRight(WAND_CALIB); // wand
		if (index == 0) m.multiplyOnRight(HEAD_CALIB); // head
	}
	
	@Override
	public String getName() {
		return "PORTAL: "+super.getName();
	}
}
