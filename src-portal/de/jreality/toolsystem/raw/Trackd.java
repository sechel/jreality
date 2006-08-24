package de.jreality.toolsystem.raw;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

import de.jreality.devicedriver.TrackdJNI;

public class Trackd implements ActionListener, Runnable {

	private final static long TIMESTEP = 100;

	private Thread anim;

	private static TrackdJNI trackdJNI = null;

	int numSensors;

	int sensor;

	private JRadioButton[] rbSensors = null;

	private JTable jtData = null;

	private void init() {

		try {
			trackdJNI = new TrackdJNI(4126, 4127);
		} catch (IOException e) {
			System.out.println(e);
			System.exit(1);
		}
		numSensors = trackdJNI.getNumSensors();

		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("Trackd");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		frame.getContentPane().add(panel);

		JPanel pSensors = new JPanel(new GridLayout(1, 0));
		pSensors.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Sensor"), BorderFactory.createEmptyBorder(
				5, 5, 5, 5)));
		rbSensors = new JRadioButton[numSensors];
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < numSensors; i++) {
			rbSensors[i] = new JRadioButton();
			group.add(rbSensors[i]);
			rbSensors[i].addActionListener(this);
			pSensors.add(rbSensors[i]);
		}
		panel.add(pSensors);

		JPanel pMatrix = new JPanel();
		pMatrix.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Data"), BorderFactory.createEmptyBorder(0,
				0, 0, 0)));
		panel.add(pMatrix);

		jtData = new JTable(4, 4);
		pMatrix.add(jtData);

		sensor = 0;
		rbSensors[sensor].setSelected(true);
		update();
		frame.pack();
		frame.setVisible(true);
	}

	public void run() {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		while (Thread.currentThread() == anim) {
			try {
				update();
				Thread.sleep(TIMESTEP);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public void actionPerformed(ActionEvent event) {
		for (int i = 0; i < numSensors; i++)
			if (rbSensors[i].isSelected()) {
				sensor = i;
				break;
			}
	}

	public void update() {
		float[] matrix = new float[16];
		trackdJNI.getMatrix(matrix, sensor);
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				jtData.setValueAt(new Float(matrix[4 * i + j]), i, j);
	}

	public void start() {
		if (anim == null)
			anim = new Thread(this);
		anim.start();
	}

	public void stop() {
		anim = null;
	}

	public static void main(String[] args) {
		Trackd me = new Trackd();
		me.init();
		me.start();
	}

}
