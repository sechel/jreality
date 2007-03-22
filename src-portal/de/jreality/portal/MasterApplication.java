package de.jreality.portal;

import java.io.IOException;
import java.net.ServerSocket;

import de.jreality.scene.proxy.smrj.ClientFactory;
import de.jreality.toolsystem.DeviceManager;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;
import de.jreality.toolsystem.ToolEventReceiver;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.vr.ViewerVR;
import de.smrj.Broadcaster;
import de.smrj.tcp.TCPBroadcasterNIO;
import de.smrj.tcp.management.JarServer;
import de.smrj.tcp.management.Local;

public class MasterApplication {
	
	DeviceManager deviceManager;
	ToolEventQueue eventQueue;
	
	public MasterApplication(final ToolEventReceiver receiver) throws IOException {
		
		
		
		ToolSystemConfiguration config = ToolSystemConfiguration.loadRemotePortalMasterConfiguration();
		eventQueue = new ToolEventQueue(new ToolEventReceiver() {
			public void processToolEvent(ToolEvent event) {
				//System.out.println("Sending: "+event);
				receiver.processToolEvent(event);
			}
		});
		//eventQueue.getThread().setDaemon(true);
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
		ToolEventReceiver tr = bc.getRemoteFactory().createRemoteViaStaticMethod(
				ToolEventReceiver.class, RemoteExecutor.class,
				"startRemote", new Class[]{Class.class, String[].class}, new Object[]{appClass, null});
		new MasterApplication(tr);
	}
}
