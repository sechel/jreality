package de.jreality.toolsystem.raw;

import vrpn.TrackerRemote;
import vrpn.TrackerRemote.PositionChangeListener;
import vrpn.TrackerRemote.TrackerUpdate;
import de.jreality.math.Quaternion;
import de.jreality.scene.tool.InputSlot;

public class DeviceVRPNTracker implements PositionChangeListener {
	
	private TrackerRemote tracker;
	private int numTrackers;
	private double[][] trackerMatrix; 
	private InputSlot[] trackerSlot;

	
	public DeviceVRPNTracker(String device, int numTrackers0){
		try {
			tracker = new TrackerRemote(device, null, null, null, null);
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		numTrackers = numTrackers0;
		init();
	}
	
	public void init(){
		//initialize before Changelistener
		trackerMatrix = new double[numTrackers][16]; 
		trackerSlot = new InputSlot[numTrackers];
		
		//meter
		double headHeight = 1.7;
		double controllerHeight = 1.3;
		
		trackerMatrix[0] = new double[]{
				1.,0.,0.,0.,
				0.,1.,0.,headHeight,
				0.,0.,1.,0.,
				0.,0,0.,1.};
		trackerMatrix[1] = new double[]{
				1.,0.,0.,0.,
				0.,1.,0.,controllerHeight,
				0.,0.,1.,0.,
				0.,0.,0.,1.};

		tracker.addPositionChangeListener( this );
	}
	
	public void setSlot(int idx, InputSlot slot){
		trackerSlot[idx]=(slot);
	}
	
	public double[] getMatrix(int button0){
		return trackerMatrix[button0];	
	}
	public InputSlot getSlot(int button0){
		return trackerSlot[button0];	
	}

	@Override
	public void trackerPositionUpdate(TrackerUpdate u, TrackerRemote tr) {
		Quaternion q = new Quaternion(u.quat[3],u.quat[0],u.quat[1],u.quat[2]);
		double[] quatMatrix = Quaternion.quaternionToRotationMatrix(null, q);
		quatMatrix[3]=u.pos[0];
		quatMatrix[7]=u.pos[1];
		quatMatrix[11]=u.pos[2];
		trackerMatrix[u.sensor]=quatMatrix;
	}
}

