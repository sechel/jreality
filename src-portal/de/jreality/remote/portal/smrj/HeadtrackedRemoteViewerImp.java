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

import de.jreality.scene.proxy.rmi.RemoteSceneGraphComponent;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.jreality.util.Lock;

/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class HeadtrackedRemoteViewerImp extends
de.jreality.remote.portal.HeadtrackedRemoteViewerImpl implements HeadtrackedRemoteViewer {

    public HeadtrackedRemoteViewerImp() throws RemoteException {
        super(new de.jreality.jogl.InteractiveViewer());
    }
    
    protected SceneGraphComponent getLocal(RemoteSceneGraphComponent r) {
        return (SceneGraphComponent) r;
    }
    
    public void loadWorld(String classname) {
        long t = System.currentTimeMillis();
        LoadableScene wm = null;
        try {
            wm = (LoadableScene) Class.forName(classname).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // scene settings
        wm.setConfiguration(ConfigurationAttributes.getDefaultConfiguration());
        de.jreality.scene.SceneGraphComponent world = wm.makeWorld();
        if (world != null) getViewer().getSceneRoot().addChild(world);
        setSignature(wm.getSignature());
        long s = System.currentTimeMillis() - t;
        System.out.println("loaded world " + classname + " successful. ["+s+"ms]");
    }
    
    Lock headMatrixLock = new Lock();
    double[] headMatrix = new double[16];
    boolean headChanged = false;
    final Object renderSynch = new Object();
    int statDelay = 10000; // milliseconds between statistic output
    int runs = 0;
    int renders = 0;
    long startTime = System.currentTimeMillis();
    long maxFrameTime = 0;
    private boolean headTracked = true;
    
    private boolean rendering, reRender;
    
    boolean manualSwapBuffers=true;
    private boolean measure = false;
    
    public void renderTest() {
            long s;
            long t;
            long start = System.currentTimeMillis();
            renders++;
            s  = System.currentTimeMillis();
            getViewer().render();
            t = System.currentTimeMillis() - s;
            if (measure)            System.out.println("render: "+t);
            s  = System.currentTimeMillis();
            getViewer().waitForRenderFinish();
            t = System.currentTimeMillis() - s;
            if (measure)            System.out.println("renderFinish: "+t);
            if (manualSwapBuffers) { 
                s  = System.currentTimeMillis();
                getViewer().swapBuffers();
                t = System.currentTimeMillis() - s;
                if (measure)                System.out.println("swapBuffers: "+t);
            }
            long delay = System.currentTimeMillis() - start;
            if (maxFrameTime < delay) maxFrameTime = delay;

            long locTime = System.currentTimeMillis() - startTime;
            if (locTime >= statDelay) {// statistics
                System.out.println("************* stats  *****************");
                System.out.println("elapsed time: " + locTime * 0.001 + " sec");
                System.out.println("fps: " + ((double) renders)
                        / ((double) locTime * 0.001));
                System.out.println("wait(): " + runs);
                System.out.println("Max. frametime: " + maxFrameTime + "[fps: "
                        + (1. / (maxFrameTime * 0.001)) + "]");
                System.out.println("******************************");
                runs = renders = 0;
                maxFrameTime = 0;
                startTime = System.currentTimeMillis();
            }
            runs++;
            rendering = false;
    }

	public void render(double[] headMatrix) {
		sendHeadTransformation(headMatrix);
		render();
	}
    
    public static void main(String[] argv) throws Exception {
    	HeadtrackedRemoteViewerImp rsi = new HeadtrackedRemoteViewerImp();
    	try {
    	    rsi.manualSwapBuffers=(argv[1].indexOf('s') != -1);
    	    rsi.measure = (argv[1].indexOf('m') != -1);
    	} catch (Exception e) {}
        rsi.setBackgroundColor(new Color(120, 10, 44, 20));
        rsi.loadWorld(argv[0]);
        //rsi.setUseDisplayLists(true);
        Thread.sleep(100);
        for (;;) {
        	rsi.renderTest();
        	Thread.yield();
        }
    }
}
