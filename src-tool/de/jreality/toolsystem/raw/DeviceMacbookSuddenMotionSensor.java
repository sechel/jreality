package de.jreality.toolsystem.raw;

import java.util.Arrays;
import java.util.Map;

import sms.Unimotion;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;

public class DeviceMacbookSuddenMotionSensor implements RawDevice, PollingDevice {
	
	InputSlot slot;
		
	Matrix mat = new Matrix();

	private ToolEventQueue queue;
	
	public void dispose() {
	}

	public String getName() {
		return "Mac SuddenMotionSensor";
	}

	public void initialize(Viewer viewer, Map<String, Object> config) {
		
	}
	
	public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
		slot = inputDevice;
		return new ToolEvent(this, System.currentTimeMillis(), slot, new DoubleArray(mat.getArray()));
	}

	public void setEventQueue(ToolEventQueue queue) {
		this.queue = queue;
	}

	static final double scale = 1.0/255.0;
	
	public void poll() {
		int[] v = Unimotion.getSMSArray();
		MatrixBuilder.euclidean(mat).reset().translate(scale*v[1], scale*v[2], -scale*v[0]);
		ToolEvent te = new ToolEvent(this, System.currentTimeMillis(), slot, new DoubleArray(mat.getArray()));
		queue.addEvent(te);
	}

	public static void main(String[] args) throws InterruptedException {
		while (true) {
			int[] v = Unimotion.getSMSArray();
			MatrixBuilder mm = MatrixBuilder.euclidean().translate(scale*v[0], scale*v[1], scale*v[2]);
			System.out.println(Arrays.toString(mm.getMatrix().getColumn(3)));
			Thread.sleep(250);
		}
	}
	
}
