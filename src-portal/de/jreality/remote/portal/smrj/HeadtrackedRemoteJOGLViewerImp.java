/*
 * Created on 10-Jan-2005
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

import java.awt.Color;
import java.rmi.RemoteException;

import de.jreality.jogl.InteractiveViewer;
import de.jreality.scene.Transformation;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.jreality.worlds.DebugLattice;
import de.smrj.tcp.TCPBroadcasterIO;
import de.smrj.tcp.TCPBroadcasterNIO;

/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class HeadtrackedRemoteJOGLViewerImp extends HeadtrackedRemoteViewerImp implements HeadtrackedRemoteJOGLViewer {

    private static final class Singleton {
        private static final HeadtrackedRemoteJOGLViewerImp instance = new HeadtrackedRemoteJOGLViewerImp();
    }
    public static HeadtrackedRemoteJOGLViewerImp getInstance() {
        return Singleton.instance;
    }
    
    private HeadtrackedRemoteJOGLViewerImp() {
        super(new de.jreality.jogl.InteractiveViewer());
        getViewer().setStereoType(de.jreality.jogl.Viewer.CROSS_EYED_STEREO);
        getViewer().getHelpOverlay().setVisible(config.getBool("viewer.showFPS"));
        getViewer().setAutoSwapMode(true); //config.getBool("viewer.autoBufferSwap"));
    }
    private InteractiveViewer getViewer() {
        return (InteractiveViewer) viewer;
    }
    public void setManualSwapBuffers(boolean b) {
        getViewer().setAutoSwapMode(!b);
    }
    public void swapBuffers() {
        getViewer().swapBuffers();
    }
    public void setUseDisplayLists(boolean b) {
        getViewer().getRenderer().setUseDisplayLists(b);
    }
    public void waitForRenderFinish() {
        getViewer().waitForRenderFinish();
    }
    public void setBackgroundColor(java.awt.Color color) {
        getViewer().setBackgroundColor(color);
    }
    
    
    // just to test if the viewer draws something or not
    
    public void loadWorld(String classname) {
        long t = System.currentTimeMillis();
        LoadableScene wm = null;
        try {
            wm = (LoadableScene) Class.forName(classname).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // scene settings
        wm.setConfiguration(ConfigurationAttributes.getSharedConfiguration());
        de.jreality.scene.SceneGraphComponent world = wm.makeWorld();
        long s = System.currentTimeMillis() - t;
        System.out.println("make world " + classname + " successful. ["+s+"ms]");
        setSignature(wm.getSignature());
        t = System.currentTimeMillis();
        if (world != null) viewer.getSceneRoot().addChild(world);
        s = System.currentTimeMillis() - t;
    }

    public static void main(String[] args) throws Exception {
        HeadtrackedRemoteJOGLViewerImp rsi = getInstance();
        rsi.loadWorld(args[0]);
    }
   
}
