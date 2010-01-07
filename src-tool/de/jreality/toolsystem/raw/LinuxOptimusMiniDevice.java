package de.jreality.toolsystem.raw;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Map;

import de.jreality.scene.Viewer;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;
import de.jreality.toolsystem.util.OSCPool;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class LinuxOptimusMiniDevice implements RawDevice, OSCListener {

	private OSCServer osc;
	
	private ToolEventQueue queue;

	private InputSlot[] keySlots=new InputSlot[3];
	
	public void dispose() {
		osc.removeOSCListener(this);
	}

	public String getName() {
		return "Optimus Mini";
	}

	public void initialize(Viewer viewer, Map<String, Object> config) {
		try {
			int port = 11223; // our optimus driver sends to 11223
			if (config.containsKey("port"))  {
				Object co = config.get("port");
				if (co instanceof Integer) {
					port = (Integer) co;
				}
			}
			osc = OSCPool.getUDPServer(port);
			osc.addOSCListener(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
		int id = -1;
		
		if (rawDeviceName.equals("Key1")) id = 0;
		if (rawDeviceName.equals("Key2")) id = 1;
		if (rawDeviceName.equals("Key3")) id = 2;
		
		if (id == -1) throw new IllegalArgumentException("No such key: "+rawDeviceName);
		
		keySlots[id] = inputDevice;
		
		return new ToolEvent(this, System.currentTimeMillis(), inputDevice, AxisState.ORIGIN);
	}

	public void setEventQueue(ToolEventQueue queue) {
		this.queue = queue;
	}

	public void buttonPressed(int buttonID) {
		if (buttonID < 1 || buttonID > 3) {
			System.out.println("ILLEGAL BUTTON ID: "+buttonID);
			return;
		}		
		InputSlot inputDevice = keySlots[buttonID-1];
		long ct = System.currentTimeMillis();
		ToolEvent press = new ToolEvent(this, ct, inputDevice, AxisState.PRESSED);
		ToolEvent release = new ToolEvent(this, ct+1, inputDevice, AxisState.ORIGIN);
		
		queue.addEvent(press);
		queue.addEvent(release);
	}

	public void messageReceived(OSCMessage msg, SocketAddress addr, long time) {
		buttonPressed((Integer) msg.getArg(0));
	}

}
