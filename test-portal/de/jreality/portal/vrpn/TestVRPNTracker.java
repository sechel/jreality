package de.jreality.portal.vrpn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import vrpn.TrackerRemote;
import vrpn.TrackerRemote.AccelerationChangeListener;
import vrpn.TrackerRemote.AccelerationUpdate;
import vrpn.TrackerRemote.PositionChangeListener;
import vrpn.TrackerRemote.TrackerUpdate;
import vrpn.TrackerRemote.VelocityChangeListener;
import vrpn.TrackerRemote.VelocityUpdate;
import de.jreality.math.Quaternion;


public class TestVRPNTracker 
		implements PositionChangeListener,
		VelocityChangeListener,
		AccelerationChangeListener {
	
	int step =0;

	public void trackerPositionUpdate( TrackerUpdate u,
									   TrackerRemote tracker )
	{
		
		if (step ==100){
		
			System.out.println( "Tracker position message from vrpn: \n" +
									"\ttime:  " + u.msg_time.getTime( ) + "  sensor:  " + u.sensor + "\n" +
									"\tposition:  " + u.pos[0] + " " + u.pos[1] + " " + u.pos[2] + "\n" +
									"\torientation:  " + u.quat[0] + " " + u.quat[1] + " " +
									u.quat[2] + " " + u.quat[3]  );
			Quaternion q = new Quaternion(u.quat[3],u.quat[0],u.quat[1],u.quat[2]);
			double[] quat = Quaternion.quaternionToRotationMatrix(null, q);
			System.out.println(Arrays.toString(quat));
			quat[3]=u.pos[0];
			quat[7]=u.pos[1];
			quat[11]=u.pos[2];
			System.out.println("meters " + Arrays.toString(quat));
			convertToInches(quat);
			step =0;
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

	public void trackerVelocityUpdate( VelocityUpdate v,
									   TrackerRemote tracker )
	{
		System.out.println( "Tracker velocity message from vrpn: \n" /* +
								"\ttime:  " + v.msg_time.getTime( ) + "  sensor:  " + v.sensor + "\n" +
								"\tvelocity:  " + v.vel[0] + " " + v.vel[1] + " " + v.vel[2] + "\n" +
								"\torientation:  " + v.vel_quat[0] + " " + v.vel_quat[1] + " " +
								v.vel_quat[2] + " " + v.vel_quat[3] + "\n" +
								"\t quat dt:  " + v.vel_quat_dt */ );
	}
		
	public void trackerAccelerationUpdate( AccelerationUpdate a,
										   TrackerRemote tracker )
	{
		System.out.println( "Tracker acceleration message from vrpn: \n" /* +
								"\ttime:  " + a.msg_time.getTime( ) + "  sensor:  " + a.sensor + "\n" +
								"\tposition:  " + a.acc[0] + " " + a.acc[1] + " " + a.acc[2] + "\n" +
								"\torientation:  " + a.acc_quat[0] + " " + a.acc_quat[1] + " " +
								a.acc_quat[2] + " " + a.acc_quat[3] + "\n" +
								"\t quat dt:  " + a.acc_quat_dt */ );
	}
		
	public static void main(String[] args) {
		String trackerName = "DTrack@n01";
		TrackerRemote tracker = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			tracker = new TrackerRemote(trackerName, null, null, null, null);
		} catch (InstantiationException e) {
			// do something b/c you couldn't create the tracker
			System.out.println("We couldn't connect to tracker " + trackerName
					+ ".");
			System.out.println(e.getMessage());
			try {
				System.out.flush();
				System.err.flush();
				System.out.println("hit enter to end");
				in.readLine();
			} catch (IOException ioe) {
			}
			return;
		}

		TestVRPNTracker test = new TestVRPNTracker();
		tracker.addPositionChangeListener(test);
		tracker.addVelocityChangeListener(test);
		tracker.addAccelerationChangeListener(test);

	}

}


