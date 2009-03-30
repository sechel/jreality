package de.jreality.audio.osc;

import java.io.IOException;
import java.net.SocketAddress;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class WiiTest {
	public static void main(String[] args) throws IOException, InterruptedException {		
		OSCServer osc = OSCPool.getUDPServer(5600);
		
		osc.addOSCListener(new OSCListener() {
			public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
				System.err.println(msg.getName());
				for(int i=0; i<msg.getArgCount(); i++) {
					System.err.println(" * "+msg.getArg(i));
				}
			}
		});
		
		while(true) {
			Thread.sleep(5000);
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
