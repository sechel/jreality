/*
 * Created on Jul 5, 2004
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */

package de.jreality.remote.portal.rmc;

import java.awt.Component;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RpcDispatcher;

import de.jreality.remote.util.INetUtilities;
import de.jreality.scene.Camera;
import de.jreality.scene.Drawable;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.proxy.rmc.RmcMirrorFactory;
import de.jreality.scene.proxy.rmc.RmcMirrorScene;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.jreality.util.Lock;



/**
 * This class behaves like a usual viewer but distributes it for RemoteViewers.
 * RemoteViewers should know the name of the Camera / CameraPath they want to display.
 * 
 * TODO: Manage camera and cameraPath for LoadableScene
 *  
 * @author weissman
 *
 */
public class RmcRemoteServer implements Viewer {


	private SceneGraphComponent root;
	private int signature;
	private ConfigurationAttributes config;
	private boolean swapBuffers = false;

	private static final long TIMEOUT = 0;
	private static final int RESPONSETYPE = GroupRequest.GET_FIRST;

    final String channel_name="jRealityRmcViewer";
    RpcDispatcher disp;
    Channel channel;		
    String props="UDP(mcast_addr=228.10.9.8;mcast_port=5679):" +
//    "PING(num_initial_members=2;timeout=3000):" +
//    "FD:" +
//    "pbcast.PBCAST(gossip_interval=5000;gc_lag=50):" +
//    "UNICAST:" +
//    "FRAG:" +
//    "pbcast.GMS:" +
//    "pbcast.STATE_TRANSFER";
//    String props="UDP(mcast_addr=228.10.9.8;mcast_port=45566;ip_ttl=3;" +
//    "mcast_send_buf_size=20000;mcast_recv_buf_size=80000000):" +
    "PING(timeout=2000;num_initial_members=3):" +
    "MERGE2(min_interval=5000;max_interval=10000):" +
    "FD_SOCK:" +
    "VERIFY_SUSPECT(timeout=1500):" +
    "pbcast.NAKACK(max_xmit_size=8096;gc_lag=50;retransmit_timeout=300,600,1200,2400,4800):" +
    "UNICAST(timeout=5000):" +
    "pbcast.STABLE(desired_avg_gossip=20000):" +
    "FRAG(frag_size=8096;down_thread=false;up_thread=false):" +
    "pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;" +
    "shun=false;print_local_addr=true):" +
    "pbcast.STATE_TRANSFER";
    
    RmcMirrorScene scene;
    
    protected Thread env;

	public RmcRemoteServer() throws RemoteException {
		Thread.currentThread().setName("RmcRemoteServerImpl");
		env = Thread.currentThread();
		scene = new RmcMirrorScene();
		config = ConfigurationAttributes.getDefaultConfiguration();
		swapBuffers = config.getBool("viewer.autoswap");	
//		new Thread(new Runnable() {
//			   public void run() {
        try {
            channel=new JChannel(props);
            channel.setOpt(Channel.LOCAL, Boolean.FALSE);
            disp=new RpcDispatcher(channel, null, null, this);
            channel.connect(channel_name);
            System.out.println("\nRmcRemoteServer started at " + new Date());
            System.out.println("Joined channel '" + channel_name + "' (" + channel.getView().size() + " members)");
            System.out.println("Ready to send requests");
        }
        catch(Exception e) {
            System.err.println("RmcRemoteServer() : " + e);
            System.exit(-1);
        }
//			   }
//		}).start();
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	/**
	 * @return Returns the sWAP_BUFFERS.
	 */
	public boolean isSwapBuffers() {
		return swapBuffers;
	}
	/**
	 * @param swap_buffers The sWAP_BUFFERS to set.
	 */
	public void setSwapBuffers(boolean swap_buffers) {
		swapBuffers = swap_buffers;
	}

	/**
	 * @return Returns the config.
	 */
	public ConfigurationAttributes getConfig() {
		return config;
	}
	
    MethodCall mc = new MethodCall();
    void execute(String methodName, Object local, Object[] params) {
    	Object[] compParams = new Object[params.length+1];
    	compParams[0] = new Integer(System.identityHashCode(local));
    	System.arraycopy(params, 0, compParams, 1, params.length);
    	execute(methodName, compParams);
    }
    protected void execute(String methodName, Object[] params) {
		//System.out.println("["+Thread.currentThread().getName()+"] executing: "+methodName);
    	mc.setName(methodName);
    	mc.setArgs(params);
    	disp.callRemoteMethods(null, mc, RESPONSETYPE, TIMEOUT);
    }

		    
    /******* Viewer INTERFACE Methods ***********/
    
	protected volatile boolean rendering = false;
	protected volatile boolean reRender = false;
	private SceneGraphPath camPath;
	public void render() {
		if (rendering) { reRender = true; return; }
		rendering = reRender = true;
		while (reRender) {
			reRender = false;
			execute("render", null);
		}
		rendering = false;
	}

	public SceneGraphComponent getSceneRoot() {
		return root;
	}

	public void setSceneRoot(SceneGraphComponent r) {
		root = r;
		Object proxy = scene.createProxyScene(r);
	System.out.println("setting proxy "+proxy+" as scene root.");
		execute("setRemoteSceneRoot", new Object[]{proxy});
	}

	public SceneGraphPath getCameraPath() {
		return camPath;
	}

	public void setCameraPath(SceneGraphPath p) {
		camPath = p;
	}
	
	public int getSignature() {
		return signature;
	}

	public void setSignature(int sig) {
		signature = sig;
		execute("setSignature", new Object[]{new Integer(sig)});
	}
	
	/********** NOT USED *********/
	
	public boolean hasViewingComponent() {
		return false;
	}

	public Component getViewingComponent() {
		return null;
	}

	public boolean hasDrawable() {
		return false;
	}

	public Drawable getDrawable() {
		return null;
	}

	public void initializeFrom(Viewer v) {
		setSceneRoot(v.getSceneRoot());
		setCameraPath(v.getCameraPath());
		render();
	}
	
	public void loadWorld(String classname) {
		LoadableScene wm = null;
		try {
			wm = (LoadableScene) Class.forName(classname).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// scene settings
		wm.setConfiguration(config);
		de.jreality.scene.SceneGraphComponent world = wm.makeWorld();
		if (world != null)
			setSceneRoot(world);
		setSignature(wm.getSignature());
		System.out.println("loaded world "+classname+" successful.");
	}
	
	public static void main(String[] args) throws RemoteException {
		RmcRemoteServer rsi = new RmcRemoteServer();
		rsi.loadWorld(args[0]);
	}

}