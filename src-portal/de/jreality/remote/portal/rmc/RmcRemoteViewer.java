/*
 * Created on Jul 14, 2004
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

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import javax.swing.JFrame;

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;

import de.jreality.remote.util.INetUtilities;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.proxy.rmc.RmcMirrorFactoryClient;
import de.jreality.soft.DefaultViewer;
import de.jreality.util.ConfigurationAttributes;

/**
 * This is a simple remote viewer that encapsulates the Viewer given to the Constructor.
 * It reads window properties such as size and title from the Properies file set as "jreality.config"
 * default value for this file is "jreality.props" (in the current folder).
 * 
 * The attributes used are:<br>
 * <ul>
 * <li> frame.title
 * <li> frame.width
 * <li> frame.height
 * <li> camera.name (The default camera name is "defaultCamera"
 * so make sure to have such a camera path in the remote SceneGraph)
 * </ul>
 *
 * @author weissman
 *  
 */
public class RmcRemoteViewer {

	Viewer viewer;
	ConfigurationAttributes config;
	RmcMirrorFactoryClient factory;
	JFrame f;

    final String channel_name="jRealityRmcViewer";
    RpcDispatcher disp;
    Channel channel;		
    String props="UDP(mcast_addr=228.10.9.8;mcast_port=5679):" +
//  "PING(num_initial_members=2;timeout=3000):" +
//  "FD:" +
//  "pbcast.PBCAST(gossip_interval=5000;gc_lag=50):" +
//  "UNICAST:" +
//  "FRAG:" +
//  "pbcast.GMS:" +
//  "pbcast.STATE_TRANSFER";
//  String props="UDP(mcast_addr=228.10.9.8;mcast_port=45566;ip_ttl=3;" +
//  "mcast_send_buf_size=20000;mcast_recv_buf_size=80000000):" +
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


	public RmcRemoteViewer(Viewer viewer) {
		factory = new RmcMirrorFactoryClient();
		this.viewer = viewer;
		if (!viewer.hasViewingComponent()) throw new RuntimeException("expecting viewer with component!");
		Thread.currentThread().setName("RemoteViewerImpl");
		config = ConfigurationAttributes.getSharedConfiguration();
//		new Thread(new Runnable() {
//		   public void run() {
			try {
	            channel=new JChannel(props);
	            //channel.setOpt(Channel.GET_STATE_EVENTS, Boolean.TRUE); // ??
	            disp=new RpcDispatcher(channel, null, null, this);
	            channel.connect(channel_name);
	            System.out.println("\nRmcRemoteViewer started at " + new Date());
	            System.out.println("Joined channel '" + channel_name + "' (" + channel.getView().size() + " members)");
	            System.out.println("Ready to serve requests");
	        }
	        catch(Exception e) {
	            System.err.println("RmcRemoteViewer() : " + e);
	            System.exit(-1);
	        }
//		   }
//		}).run();
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// frame settings
		f = new JFrame(config.getProperty("frame.title", "no title"));
		if (config.getBool("frame.fullscreen")) {
			f.dispose();
			f.setUndecorated(true);
			f.getGraphicsConfiguration().getDevice().setFullScreenWindow(f);
		} else {
			f.setSize(config.getInt("frame.width"), config
					.getInt("frame.height"));
			f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					quit();
				}
			});
		}
		f.getContentPane().add(viewer.getViewingComponent());
	}
	
	public void setRemoteSceneRoot(int id) {
		SceneGraphComponent r = (SceneGraphComponent) RmcMirrorFactoryClient.getLocal(id);
		System.out.println("Setting scene root to ["+r.getName()+"] ");
		viewer.setSceneRoot(r);
	}

	public void setRemoteCameraPath(int[] list) {
		SceneGraphPath sgp = SceneGraphPath.fromList(RmcMirrorFactoryClient.convertToLocal(list));
		System.out.println("[RemoteViewer->setCameraPath()] CameraPath: "+sgp.toString());
		viewer.setCameraPath(sgp);
		f.setVisible(list != null);
		try {
			Thread.sleep(10);
		} catch (Exception e) {}
		render();
	}

	public void render() {
		if (f.isVisible() && viewer.getSceneRoot() != null && viewer.getCameraPath() != null) viewer.render();
		System.out.println("RmcRemoteViewer render!");
	}

	public int getSignature() {
		return viewer.getSignature();
	}

	public void setSignature(int sig) {
		viewer.setSignature(sig);
	}
	
	public void quit() {
	  java.awt.event.ActionListener taskPerformer = new java.awt.event.ActionListener() {
	      public void actionPerformed(java.awt.event.ActionEvent evt) {
	          System.exit(0);
	      }
	  };
	  new javax.swing.Timer(50, taskPerformer).start();
	}
		
	public static void main(String args[]) {
		String hostname = INetUtilities.getHostname();
		ConfigurationAttributes config = ConfigurationAttributes.getDefaultConfiguration();
		DefaultViewer viewer = new DefaultViewer(false); // new de.jreality.jogl.InteractiveViewer();
		viewer.setBackground(new Color(.4f,.5f,.8f));
		RmcRemoteViewer obj = new RmcRemoteViewer(viewer);		
	}
		
}