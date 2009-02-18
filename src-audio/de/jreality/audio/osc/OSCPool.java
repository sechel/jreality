package de.jreality.audio.osc;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import de.sciss.net.OSCChannel;
import de.sciss.net.OSCClient;
import de.sciss.net.OSCServer;

/**
 * 
 * Simple class for managing a pool of OSC clients and servers, so  that many OSC tools may share the same OSC connection.
 * Pretty straightforward, except that users of this class should not call stop() on any of the clients/servers received from
 * this class; just rely on the garbage collector to clean things up if necessary.
 * 
 * Depends on the NetUtil OSC library {@link http://www.sciss.de/netutil/}.
 * 
 * @author brinkman
 *
 */
public class OSCPool {

	private static final Map<InetSocketAddress, WeakReference<OSCClient>> udpClients = new HashMap<InetSocketAddress, WeakReference<OSCClient>>();
	private static final Map<InetSocketAddress, WeakReference<OSCClient>> tcpClients = new HashMap<InetSocketAddress, WeakReference<OSCClient>>();

	private static final Map<Integer, WeakReference<OSCServer>> udpServers = new HashMap<Integer, WeakReference<OSCServer>>();
	private static final Map<Integer, WeakReference<OSCServer>> tcpServers = new HashMap<Integer, WeakReference<OSCServer>>();


	public static OSCClient getUDPClient(InetSocketAddress addr) throws IOException {
		return getClient(addr, udpClients, OSCChannel.UDP);
	}

	public static OSCClient getTCPClient(InetSocketAddress addr) throws IOException {
		return getClient(addr, tcpClients, OSCChannel.TCP);
	}
	
	public static OSCServer getUDPServer(int port) throws IOException {
		return getServer(port, udpServers, OSCChannel.UDP);
	}
	
	public static OSCServer getTCPServer(int port) throws IOException {
		return getServer(port, tcpServers, OSCChannel.TCP);
	}

	
	private static OSCClient getClient(InetSocketAddress addr, Map<InetSocketAddress, WeakReference<OSCClient>> clients, String protocol) throws IOException {
		synchronized (clients) {
			WeakReference<OSCClient> ref = clients.get(addr);
			OSCClient client = (ref != null) ? ref.get() : null;
			if (client == null) {
				client = OSCClient.newUsing(protocol);
				client.setTarget(addr);
				client.start();

				clients.put(addr, new WeakReference<OSCClient>(client));
			}
			return client;
		}
	}
	
	private static OSCServer getServer(int port, Map<Integer, WeakReference<OSCServer>> servers, String protocol) throws IOException {
		synchronized (servers) {
			WeakReference<OSCServer> ref = servers.get(port);
			OSCServer server = (ref != null) ? ref.get() : null;
			if (server == null) {
				server = OSCServer.newUsing(protocol, port);
				server.start();

				servers.put(port, new WeakReference<OSCServer>(server));
			}
			return server;
		}
	}
}
