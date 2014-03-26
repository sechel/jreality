package de.jreality.portal.vrpn;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;

import vrpn.AnalogOutputRemote;
import vrpn.AnalogRemote;
import vrpn.AnalogRemote.AnalogChangeListener;
import vrpn.AnalogRemote.AnalogUpdate;
import vrpn.ButtonRemote;
import vrpn.ButtonRemote.ButtonChangeListener;
import vrpn.ButtonRemote.ButtonUpdate;
import vrpn.TrackerRemote;
import vrpn.TrackerRemote.PositionChangeListener;
import vrpn.TrackerRemote.TrackerUpdate;
import de.jreality.math.Quaternion;

/**
 * Set Native-lib-path!
 * @author heydt, pastorelli, herrmann 
 *
 */

public class TestVRPNARTrack implements  
			ButtonChangeListener, 
			PositionChangeListener, 
			AnalogChangeListener, 
			ActionListener {

	
	private ButtonRemote button = null;
	private TrackerRemote tracker = null;
	private AnalogRemote analog = null;
	private AnalogOutputRemote ao = null;
	
	int numSensors;
	
	int sensor;

	private JRadioButton[] rbSensors = null;

	private JTable jtSensors = null;

	
	private int numButtons;
	private JRadioButton[] rbButtons = null;
	
	private int numValuators;
	private JTable jtValuators = null;

	public TestVRPNARTrack(String device) {
		init(device);
	}

	private void init(String device) {
		
		numButtons = 6;
		numSensors = 2;
		numValuators = 2;
		
		try {
			button = new ButtonRemote(device, null, null, null, null);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		button.addButtonChangeListener( this );
			
		try {
			tracker = new TrackerRemote(device, null, null, null, null);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tracker.addPositionChangeListener( this );
		
		try {
			analog = new AnalogRemote(device, null, null, null, null);
			ao = new AnalogOutputRemote(device, null, null, null, null);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		analog.addAnalogChangeListener( this );
		ao.requestValueChange( 2, 5 );
//		try {
//			trackdJNI = new TrackdJNI(4126, 4127);
//		} catch (IOException e) {
//			System.out.println(e);
//			System.exit(1);
//		}
//		numSensors = trackdJNI.getNumSensors();

		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("VRPN");
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

		jtSensors = new JTable(4, 4);
		jtSensors.setEnabled(false);
		pMatrix.add(jtSensors);

		sensor = 0;
		rbSensors[sensor].setSelected(true);
		
		rbButtons = new JRadioButton[numButtons];
		JPanel pButtons = new JPanel(new GridLayout(1, 0));
		pButtons.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Buttons"), BorderFactory.createEmptyBorder(
				5, 5, 5, 5)));
		for (int i = 0; i < numButtons; i++) {
			rbButtons[i] = new JRadioButton();
			rbButtons[i].setEnabled(false);
			pButtons.add(rbButtons[i]);
		}
		
		panel.add(pButtons);

		JPanel pValuators = new JPanel();
		pValuators.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Valuators"), BorderFactory.createEmptyBorder(0,
				0, 0, 0)));
		panel.add(pValuators);

		jtValuators = new JTable(1, numValuators);
		jtValuators.setEnabled(false);
		pValuators.add(jtValuators);
		
		frame.pack();
		frame.setVisible(true);
		
		System.out.println("numSensors="+numSensors);
		System.out.println("numButtons="+numButtons);
		System.out.println("numValuators="+numValuators);
		
	}

	public void actionPerformed(ActionEvent event) {
		for (int i = 0; i < numSensors; i++)
			if (rbSensors[i].isSelected()) {
				sensor = i;
				break;
			}
	}

	@Override
	public void buttonUpdate(ButtonUpdate u, ButtonRemote button0) {
		System.out.println( "Button message from vrpn: \n" +
				"\ttime:  " + u.msg_time.getTime( ) + "  button:  " + u.button + "\n" +
				"\tstate:  " + u.state );
		if(u.state == 1)
			rbButtons[u.button].setSelected(true);
		else rbButtons[u.button].setSelected(false);
	}
 
	int step =0;
	@Override
	public void trackerPositionUpdate(TrackerUpdate u, TrackerRemote tracker0) {
		
		if (step ==100){
		
			System.out.println( "Tracker position message from vrpn: \n" +
									"\ttime:  " + u.msg_time.getTime( ) + "  sensor:  " + u.sensor + "\n" 
//									+ "\tposition:  " + u.pos[0] + " " + u.pos[1] + " " + u.pos[2] + "\n" 
//									+ "\torientation:  " + u.quat[0] + " " + u.quat[1] + " "
//									+ u.quat[2] + " " + u.quat[3] 
											);
			Quaternion q = new Quaternion(u.quat[3],u.quat[0],u.quat[1],u.quat[2]);
			double[] quatMatrix = Quaternion.quaternionToRotationMatrix(null, q);
			quatMatrix[3]=u.pos[0];
			quatMatrix[7]=u.pos[1];
			quatMatrix[11]=u.pos[2];
			convertToInches(quatMatrix);
			step =0;
			for (int i = 0; i < 4; i++)
				for (int j = 0; j < 4; j++)
					jtSensors.setValueAt(new Float(quatMatrix[4 * i + j]), i, j);
		} else {
			step++;
		}
		
	}
		
	private void convertToInches(double[] quat) {
		quat[3] = 3.28084* quat[3];
		quat[7] = 3.28084* quat[7];
		quat[11] = 3.28084* quat[11];
		
		System.out.println("feet " + Arrays.toString(quat));
	}

	@Override
	public void analogUpdate(AnalogUpdate u, AnalogRemote analog0) {
		System.out.println( "Analog message from vrpn: \n" +
				"\ttime:  " + u.msg_time.getTime( ) + "\n" +
				"\tchannel:  " +  Arrays.toString(u.channel)  );
		
		for (int i=0; i<numValuators; i++) {
			jtValuators.setValueAt(u.channel[i], 0, i);
		}
	}

	public static void main(String[] args) {
		TestVRPNARTrack me = new TestVRPNARTrack("DTrack@n01");
	}

}

