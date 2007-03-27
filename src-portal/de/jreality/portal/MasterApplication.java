package de.jreality.portal;

import java.io.IOException;
import java.net.ServerSocket;

import de.jreality.scene.proxy.smrj.ClientFactory;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.DeviceManager;
import de.jreality.toolsystem.PortalToolSystem;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;
import de.jreality.toolsystem.ToolEventReceiver;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.vr.ViewerVR;
import de.smrj.Broadcaster;
import de.smrj.tcp.TCPBroadcasterNIO;
import de.smrj.tcp.management.JarServer;
import de.smrj.tcp.management.Local;

/**
 * Starts a ToolSystem with Trackd device and a remote application on the different walls of the portal.
 * Default is to start ViewerVR, but you can give another classname. The given class needs at least one
 * static method called remoteMain:
 * 
 * public static ViewerApp remoteMain(String[] args) { ... }
 * 
 * 
 * 
 * @author weissman
 *
 */
public class MasterApplication {
	
	protected static final InputSlot SYSTEM_TIME = InputSlot.getDevice("SystemTime");
	DeviceManager deviceManager;
	ToolEventQueue eventQueue;
	
	public MasterApplication(final PortalToolSystem receiver) throws IOException {
		ToolSystemConfiguration config = ToolSystemConfiguration.loadRemotePortalMasterConfiguration();
		eventQueue = new ToolEventQueue(new ToolEventReceiver() {
			long st;
			public void processToolEvent(ToolEvent event) {
				//st = -System.currentTimeMillis();
				receiver.processToolEvent(event);
				//st+=System.currentTimeMillis();
				//System.out.println("send took "+st+" ms: "+event.getInputSlot());

				if (event.getInputSlot() == SYSTEM_TIME) {
					
					
					st = -System.currentTimeMillis();
					receiver.render();
					st+=System.currentTimeMillis();
					System.out.println("render took "+st+" ms");
					
//					st = -System.currentTimeMillis();
//					receiver.swapBuffers();
//					st+=System.currentTimeMillis();
//					System.out.println("swapBuffers took "+st+" ms");

				}
			}
		});
		deviceManager = new DeviceManager(config, eventQueue, null);
		eventQueue.start();
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		int port = 8844;
		int cpPort = 8845;
		JarServer js = new JarServer(new ServerSocket(cpPort));
		Broadcaster bc = new TCPBroadcasterNIO(port, Broadcaster.RESPONSE_TYPE_EXCEPTION);
		Local.sendStart(port, cpPort, Broadcaster.RESPONSE_TYPE_EXCEPTION, ClientFactory.class);
		js.waitForDownloads();
		Class appClass;
		if (args.length == 0) appClass=ViewerVR.class;
		else appClass = Class.forName(args[0]);
		PortalToolSystem tr = bc.getRemoteFactory().createRemoteViaStaticMethod(
				PortalToolSystem.class, RemoteExecutor.class,
				"startRemote", new Class[]{Class.class, String[].class}, new Object[]{appClass, null});
		new MasterApplication(tr);
	}
}
