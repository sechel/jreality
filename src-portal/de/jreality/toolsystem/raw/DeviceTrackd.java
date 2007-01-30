package de.jreality.toolsystem.raw;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.Timer;

import de.jreality.devicedriver.TrackdJNI;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;

/**
 * 
 * Sensors (matrices), buttons and valuators are available under their trackd name - 
 * i. e. button_0, sensor_2, valuator_7 etc.
 * 
 * @author Steffen Weissmann
 *
 */
public class DeviceTrackd implements RawDevice, PollingDevice {

	private static TrackdJNI trackd;
	int numSensors;
	private ToolEventQueue queue;

	private HashMap<Integer, double[]> matrix = new HashMap<Integer, double[]>(); 
	private HashMap<Integer, Integer> button = new HashMap<Integer, Integer>(); 
	private HashMap<Integer, Double> valuator = new HashMap<Integer, Double>(); 

	private HashMap<Integer, InputSlot> matrixSlot = new HashMap<Integer, InputSlot>(); 
	private HashMap<Integer, InputSlot> buttonSlot = new HashMap<Integer, InputSlot>(); 
	private HashMap<Integer, InputSlot> valuatorSlot = new HashMap<Integer, InputSlot>(); 
	
	private final float[] tmpMatrix = new float[16];
	
	
	public void dispose() {
	}

	public String getName() {
		return "Trackd driver";
	}

	public void initialize(Viewer viewer) {
        try {
            trackd = new TrackdJNI(4126, 4127);
            System.out.println("Trackd: sensors="+trackd.getNumSensors()+" buttons="+trackd.getNumButtons()+" valuators="+trackd.getNumValuators());
        } catch (IOException e) {
            e.printStackTrace();
            RuntimeException re = new RuntimeException();
            re.initCause(e);
            throw re;
        }
	}

	public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
		String[] split = rawDeviceName.split("_");
		int index = Integer.parseInt(split[1]);
		if ("sensor".equals(split[0])) {
			if (index >= trackd.getNumSensors()) throw new IllegalArgumentException("unknown sensor: "+index);
			double[] sensorMatrix = new double[16];
			// register slot:
			matrix.put(index, sensorMatrix);
			matrixSlot.put(index, inputDevice);
			// read initial value
			trackd.getMatrix(tmpMatrix, index);
			copy(tmpMatrix, sensorMatrix);
			calibrate(sensorMatrix, index);
			return new ToolEvent(this, inputDevice, null, new DoubleArray(sensorMatrix));
		} else if ("button".equals(split[0])) {
			if (index >= trackd.getNumButtons()) throw new IllegalArgumentException("unknown button: "+index);
			int buttonState = trackd.getButton(index);
			// register slot:
			button.put(index, buttonState);
			buttonSlot.put(index, inputDevice);

			return new ToolEvent(this, inputDevice, buttonState == 0 ? AxisState.ORIGIN : AxisState.PRESSED, null);
		} else if ("valuator".equals(split[0])) {
			if (index >= trackd.getNumValuators()) throw new IllegalArgumentException("unknown valuator: "+index);
			double value = trackd.getValuator(index);
			// register slot:
			valuator.put(index, value);
			valuatorSlot.put(index, inputDevice);

			return new ToolEvent(this, inputDevice, new AxisState(value), null);
		} else {
			throw new IllegalArgumentException("unknown trackd device: "+rawDeviceName);
		}
	}

	private void copy(float[] floats, double[] doubles) {
		for (int i = 0; i < 16; i++)
			doubles[i]=floats[i];
	}

	public void setEventQueue(ToolEventQueue queue) {
		this.queue=queue;
	}

	public void poll() {
		for (Entry<Integer, double[]> e : matrix.entrySet()) {
			int i = e.getKey();
			double[] val = e.getValue();
			InputSlot slot = matrixSlot.get(i);
			trackd.getMatrix(tmpMatrix, i);
			copy(tmpMatrix, val);
			calibrate(val, i);
			ToolEvent te = new ToolEvent(this, slot, null, new DoubleArray(val)) {
				@Override
				protected boolean compareTransformation(DoubleArray trafo1, DoubleArray trafo2) {
					return true;
				}
			};
			if (queue != null) queue.addEvent(te);
			else System.out.println(te);
		}
		for (Entry<Integer, Integer> e : button.entrySet()) {
			int i = e.getKey();
			int val = e.getValue();
			InputSlot slot = buttonSlot.get(i);
			int newVal = trackd.getButton(i);
			if (newVal != val) {
				button.put(i, newVal);
				ToolEvent te = new ToolEvent(this, slot, newVal == 0 ? AxisState.ORIGIN : AxisState.PRESSED, null);
				if (queue != null) queue.addEvent(te);
				else System.out.println(te);
			}
		}
		for (Entry<Integer, Double> e : valuator.entrySet()) {
			int i = e.getKey();
			double val = e.getValue();
			InputSlot slot = valuatorSlot.get(i);
			double newVal = trackd.getValuator(i);
			if (newVal != val) {
				valuator.put(i, newVal);
				ToolEvent te = new ToolEvent(this, slot, new AxisState(newVal), null);
				if (queue != null) queue.addEvent(te);
				else System.out.println(te);
			}
		}
	}

	protected void calibrate(double[] sensorMatrix, int index) {	
	}
	
}
