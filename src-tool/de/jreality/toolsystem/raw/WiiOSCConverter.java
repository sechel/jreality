package de.jreality.toolsystem.raw;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import de.jreality.toolsystem.util.OSCPool;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

/**
 * 
 * TODO: Implement a proper WiiMote device in Java and get rid of this Rube Goldberg contraption!!!
 * 
 * Converter from wiiosc to WiiMoteOSC, currently only useful under Linux; this class
 * and wiiosc (see attached tarball) replace DarwiinRemoteOSC.
 * 
 * Start wiiosc like this: wiiosc 57120 57150 127.0.0.1 auto
 * 
 * 
 * @author brinkman
 *
 */
public class WiiOSCConverter {

	final private InetSocketAddress target;
	final private OSCServer osc;

	public WiiOSCConverter() throws IOException {
		this(57120, 5600);
	}
	
	public WiiOSCConverter(int sourcePort) throws IOException {
		this(sourcePort, 5600);
	}
	
	public WiiOSCConverter(int sourcePort, int targetPort) throws IOException {
		target = new InetSocketAddress("127.0.0.1", targetPort);
		osc = OSCPool.getUDPServer(sourcePort);
		
		osc.addOSCListener(new OSCListener() {
			private float x, y, z;
			private float roll, pitch;
			private static final float RAD2DEG = 180/(float) Math.PI;
			private Map<String, Integer> buttonStates = new HashMap<String, Integer>();

			public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
				int id = (Integer) msg.getArg(0);
				if (id!=0) return;
				String name = msg.getName();
				//System.out.println(".messageReceived(): "+name);
				if (name.startsWith("/wii/keys/")) {
					String key = name.substring(10);
					Integer state = (Integer) msg.getArg(1);
					if (!state.equals(buttonStates.get(key))) {
						buttonStates.put(key, state);
						try {
							osc.send(new OSCMessage("/wii/button/"+key, new Object[] {state}), target);
						} catch (IOException e) {
							// do nothing
						}
					}
				} else if (name.equals("/wii/acc/x")) {
					x = 10*((Float) msg.getArg(1))-5;
				} else if (name.equals("/wii/acc/y")) {
					y = 10*((Float) msg.getArg(1))-5;
				} else if (name.equals("/wii/acc/z")) {
					z = 10*((Float) msg.getArg(1))-5;
					pitch = (float) Math.atan2(y, z)*RAD2DEG;
					roll = (float) Math.atan2(x, z)*RAD2DEG;
					try {
						osc.send(new OSCMessage("/wii/acc", new Object[] {new Float(x), new Float(y), new Float(z)}), target);
						osc.send(new OSCMessage("/wii/orientation", new Object[] {new Float(roll), new Float(pitch)}), target);
					} catch (IOException e) {
						// do nothing
					}
				} else if ((name.equals("/wii/battery"))) {
					try {
						osc.send(new OSCMessage("/wii/batterylevel", new Object[] {msg.getArg(1)}), target);
					} catch (IOException e) {
						// do nothing
					}
				}
			}
		});
	}


	public static void main(String[] args) throws InterruptedException, IOException {
		new WiiOSCConverter();
		while (true) {
			Thread.sleep(1000);
		}
	}
}

/*
/wii/connected , i
/wii/mousemode , i
/wii/button/a , i
/wii/button/b , i
/wii/button/up , i
/wii/button/down , i
/wii/button/left , i
/wii/button/right , i
/wii/button/minus , i
/wii/button/plus , i
/wii/button/home , i
/wii/button/one , i
/wii/button/two , i
/wii/acc , fff
/wii/orientation , ff
/wii/irdata , ffffffffffff
/wii/batterylevel , f
/nunchuk/joystick , ff
/nunchuk/button/z , i
/nunchuk/button/c , i
/nunchuk/acc , fff
/nunchuk/orientation , ff
 */
