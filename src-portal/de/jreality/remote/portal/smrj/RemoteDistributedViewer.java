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

package de.jreality.remote.portal.smrj;

import java.awt.Component;
import java.io.IOException;

import de.jreality.scene.Drawable;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.proxy.scene.RemoteSceneGraphComponent;
import de.jreality.scene.proxy.smrj.SMRJMirrorScene;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.smrj.RemoteFactory;
import de.smrj.tcp.TCPBroadcasterNIO;

/**
 * This class behaves like a usual viewer but distributes it for RemoteViewers.
 * RemoteViewers should know the name of the Camera / CameraPath they want to
 * display.
 * 
 * TODO: Manage camera and cameraPath for LoadableScene
 * 
 * @author weissman
 *  
 */
public class RemoteDistributedViewer implements Viewer {

    private SceneGraphComponent root;
    private int signature;
    private ConfigurationAttributes config;
    private boolean swapBuffers = false;
    private SceneGraphPath cameraPath;

    RemoteViewer clients;

    RemoteFactory factory;
    SMRJMirrorScene proxyScene;

    public boolean isSwapBuffers() {
        return swapBuffers;
    }
    public void setSwapBuffers(boolean swap_buffers) {
        swapBuffers = swap_buffers;
    }
    public ConfigurationAttributes getConfig() {
        return config;
    }

    public RemoteDistributedViewer(RemoteFactory factory) throws IOException {
        Thread.currentThread().setName("RemoteServerImpl");
        config = ConfigurationAttributes.getDefaultConfiguration();
        swapBuffers = config.getBool("viewer.autoswap");
        this.factory = factory;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        clients = initClients();
        clients.reset();
        proxyScene = new SMRJMirrorScene(factory);
    }

    protected RemoteViewer initClients() throws IOException {
    	// doesnt work!!!
        return (RemoteViewer) factory.createRemote(RemoteViewerImp.class);
    }
    
    /** ***** Viewer INTERFACE Methods ********** */

    protected volatile boolean rendering = false;
    protected volatile boolean reRender = false;

    public void render() {
        if (rendering) {
            reRender = true;
            return;
        }
        rendering = reRender = true;
        while (reRender) {
            reRender = false;
            renderImpl();
        }
        rendering = false;
    }

    private void renderImpl() {
        clients.render();
    }

    public SceneGraphComponent getSceneRoot() {
        return root;
    }

    public void setSceneRoot(SceneGraphComponent r) {
        root = r;
        RemoteSceneGraphComponent remoteRoot = (RemoteSceneGraphComponent) proxyScene
        .createProxyScene(r); 
        clients.setRemoteSceneRoot(remoteRoot);
    }

    public SceneGraphPath getCameraPath() {
        return cameraPath;
    }

    public void setCameraPath(SceneGraphPath p) {
        cameraPath = p;
    }

    public int getSignature() {
        return signature;
    }

    public void setSignature(int sig) {
        signature = sig;
        clients.setSignature(sig);
    }

    /** ******** NOT USED ******** */

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
        if (world != null) setSceneRoot(world);
        setSignature(wm.getSignature());
        System.out.println("loaded world " + classname + " successful.");
    }

    public void dispose() {
        factory.dispose();
    }
	public SceneGraphComponent getAuxiliaryRoot() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setAuxiliaryRoot(SceneGraphComponent ar) {
		// TODO Auto-generated method stub
		
	}

}