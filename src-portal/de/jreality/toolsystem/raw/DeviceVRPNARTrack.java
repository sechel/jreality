package de.jreality.toolsystem.raw;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;
import de.jreality.util.LoggingSystem;

/**
 * 
 * Sensors (matrices), buttons and valuators are available under their trackd name - 
 * i. e. button_0, sensor_2, valuator_7 etc.
 * 
 * @author Steffen Weissmann
 *
 */
public class DeviceVRPNARTrack implements RawDevice, PollingDevice {
	int numTrackers, numButtons, numAnalogs;
	String device;
	protected ToolEventQueue queue;
	private double EPS = 10E-6;
	
	private DeviceVRPNButton VRPNButtonDevice;
	private DeviceVRPNAnalog VRPNAnalogDevice;
	private DeviceVRPNTracker VRPNTrackerDevice;

	private double[][] trackerMatrix; 
	private int[] buttonStates; 
	private double[] analogValues;

	private HashSet<Integer> disabledSensors = new HashSet<Integer>();
	
	public String getName() {
		return "VRPN ARTrack driver";
	}

	public void initialize(Viewer viewer, Map<String, Object> config) {
		System.out.println("init");
		if(config.containsKey("num_trackers"))numTrackers = (Integer)config.get("num_trackers");
		else{ System.out.println("trackers"); LoggingSystem.getLogger(this).warning("not using default trackers");}
		if(config.containsKey("num_buttons")) numButtons = (Integer)config.get("num_buttons");
		else{ System.out.println("buttons"); LoggingSystem.getLogger(this).warning("not using default buttons");}
		if(config.containsKey("num_analogs")) numAnalogs = (Integer)config.get("num_analogs");
		else{ System.out.println("analogs"); LoggingSystem.getLogger(this).warning("not using default analogs");}
		
		buttonStates = new int[numButtons];
		analogValues = new double[numAnalogs];
		trackerMatrix = new double[numTrackers][16];
		
		System.out.println("VRPN ARTrack: trackers="+numTrackers+" buttons="+numButtons+" analogs="+numAnalogs);

		if(config.containsKey("id_string")) {
			device = (String) config.get("id_string");
			System.out.println("Device "+ device );
			VRPNButtonDevice = new DeviceVRPNButton(device, numButtons);
			VRPNAnalogDevice = new DeviceVRPNAnalog(device, numAnalogs);
			VRPNTrackerDevice = new DeviceVRPNTracker(device, numTrackers);
		}
		
	}

	public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
		String[] split = rawDeviceName.split("_");
		int index = Integer.parseInt(split[1]);
		
		if ("tracker".equals(split[0])) {
			if (index >= numTrackers) throw new IllegalArgumentException("unknown tracker: "+index);
			double[] sensorMatrix = VRPNTrackerDevice.getMatrix(index);
			// register slot:
			trackerMatrix[index]= sensorMatrix;
			VRPNTrackerDevice.setSlot(index, inputDevice);
			// read initial value
			calibrate(sensorMatrix, index);
			return new ToolEvent(this, System.currentTimeMillis(), inputDevice, null, new DoubleArray(sensorMatrix));
		} else if ("button".equals(split[0])) {
			if (index >= numButtons) throw new IllegalArgumentException("Unknown button: "+index);
			 //register slot:
			int buttonState = VRPNButtonDevice.getState(index);
			buttonStates[index]=buttonState;
			VRPNButtonDevice.setSlot(index, inputDevice);
			return new ToolEvent(this,  System.currentTimeMillis(), inputDevice, buttonState == 0 ? AxisState.ORIGIN : AxisState.PRESSED, null);
		} else if ("analog".equals(split[0])) {
			if (index >= numAnalogs) throw new IllegalArgumentException("unknown analog: "+index);
			double value = VRPNAnalogDevice.getValue(index);
			VRPNAnalogDevice.setSlot(index, inputDevice);
			return new ToolEvent(this, System.currentTimeMillis(), inputDevice, new AxisState(value), null);
		} else {
			throw new IllegalArgumentException("unknown trackd device: "+rawDeviceName);
		}
	}

	public void setEventQueue(ToolEventQueue queue) {
		this.queue=queue;
	}

	public synchronized void poll(long when) {
//		System.out.println("poll");
		for(int t=0;t<trackerMatrix.length;t++){
			InputSlot slot = VRPNTrackerDevice.getSlot(t);
			double[]matrix = VRPNTrackerDevice.getMatrix(t);
//			System.out.println(Arrays.toString(matrix));
//			calibrate(matrix, t);
			ToolEvent te = new MyToolEvent(this, when, slot, null, new DoubleArray(matrix));
			if (queue != null) queue.addEvent(te);
			else System.out.println(te);
		}
		for(int b=0;b<buttonStates.length;b++){
			int bs = buttonStates[b];
			int state = VRPNButtonDevice.getState(b);
			if(bs != state){
				buttonStates[b] =  state;
				InputSlot slot = VRPNButtonDevice.getSlot(b);
				ToolEvent te = new ToolEvent(this, when, slot, state == 0 ? AxisState.ORIGIN : AxisState.PRESSED, null);
				if (queue != null) queue.addEvent(te);
				else System.out.println(te);
			}
		}
		for(int i=0;i<analogValues.length;i++){
			double av = analogValues[i];
			double newVal = VRPNAnalogDevice.getValue(i);
			if(StrictMath.abs(av - newVal) > EPS){
				analogValues[i] = newVal;
				InputSlot slot = VRPNAnalogDevice.getSlot(i);
				ToolEvent te = new ToolEvent(this, when, slot, new AxisState(newVal), null);
				if (queue != null) queue.addEvent(te);
				else System.out.println(te);
			}
		}
	}

	protected void calibrate(double[] sensorMatrix, int index) {	
	}
	
	protected synchronized void disableSensor(int sensorID) {
		disabledSensors.add(sensorID);
	}

	protected synchronized void enableSensor(int sensorID) {
		disabledSensors.remove(sensorID);
	}
	
	static class MyToolEvent extends ToolEvent {
		
		private static final long serialVersionUID = -8503410127439268525L;

		public MyToolEvent(Object source, long when, InputSlot device, AxisState axis, DoubleArray trafo) {
			super(source, when, device, axis, trafo);
		}

		protected boolean compareTransformation(DoubleArray trafo1, DoubleArray trafo2) {
			return true;
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}

