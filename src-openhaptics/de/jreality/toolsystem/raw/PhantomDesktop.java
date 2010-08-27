package de.jreality.toolsystem.raw;

import java.util.Map;

import de.jreality.openhaptics.OHRawDevice;
import de.jreality.openhaptics.OHViewer;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.varylab.openhaptics.HL;

public class PhantomDesktop implements RawDevice, OHRawDevice {
	boolean running=true;
	
	InputSlot[] slots = new InputSlot[2];
	
	private ToolEventQueue queue;
	
	private OHViewer ohviewer;

	private boolean lastButtonState = false;


	synchronized boolean isRunning() {
		return running;
	}
	
	synchronized void stop() {
		running = false;
	}
	
	public void dispose() {
		stop();
	}

	public String getName() {
		return "PhantomDesktop";
	}

	
	public void initialize(Viewer viewer, Map<String, Object> config) {
//		sensitivity = 1.0;
//		if (config.containsKey("sensitivity")) {
//			sensitivity = (Double) config.get("sensitivity");
//		}
		
		if(viewer instanceof ViewerSwitch){
			for(Viewer v : ((ViewerSwitch) viewer).getViewers()){
				if(v instanceof OHViewer){
					viewer = v;
					break;
				}
			}
		}
		
		if(viewer instanceof OHViewer ){
			ohviewer =((OHViewer)viewer);
			ohviewer.getRawDevices().add(this);
			ohviewer.setBox((double[]) config.get("p0"),(double[]) config.get("p1"));
		} else throw new IllegalArgumentException("The phantomdesktop device requires the open haptics viewer (OHViewer).");
	}

	public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
		if (rawDeviceName.equals("trafo"))
			slots[0]=inputDevice;
		else if (rawDeviceName.equals("button_1"))
			slots[1]=inputDevice;
		else 
			throw new IllegalArgumentException("no such device: " + rawDeviceName);
		System.out.println("registered "+rawDeviceName+"->"+inputDevice);
		return new ToolEvent(this, System.currentTimeMillis(), inputDevice, AxisState.ORIGIN);
	}

	public void setEventQueue(ToolEventQueue queue) {
		this.queue = queue;
	}
	
	public void checkDevice() {
		double trafo[] = new double[16];
		HL.hlGetDoublev(HL.HL_PROXY_TRANSFORM, trafo, 0);
		queue.addEvent(new ToolEvent(this, System.currentTimeMillis(), slots[0], new DoubleArray(trafo)) {
			private static final long serialVersionUID = 5542511510287252014L;
			@Override
			protected boolean compareTransformation(DoubleArray trafo1, DoubleArray trafo2) {
				return true;
			}
		});
		
		byte buttonState[] = new byte [1];
		HL.hlGetBooleanv(HL.HL_BUTTON1_STATE, buttonState, 0);
		
		boolean newButtonState = buttonState[0]==1;
		if(newButtonState != lastButtonState){
			lastButtonState = newButtonState;
			queue.addEvent(new ToolEvent(this, System.currentTimeMillis(), slots[1], new AxisState(buttonState[0]==0x1 ? 1: 0)));
		}
	}

	public void start() {
		// check if haptic device was initialized:
		running = ohviewer != null && ohviewer.getRenderer().isDevicePresent();
	}
}
