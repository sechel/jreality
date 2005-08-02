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

import net.java.games.jogl.GLCanvas;
import de.jreality.jogl.InteractiveViewer;
import de.jreality.scene.proxy.scene.RemoteSceneGraphComponent;
import de.smrj.ClientFactory;

/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class HeadtrackedRemoteJOGLViewerImp extends HeadtrackedRemoteViewerImp implements HeadtrackedRemoteJOGLViewer, ClientFactory.ResetCallback {

    private static final class Singleton {
        private static final HeadtrackedRemoteJOGLViewerImp instance = new HeadtrackedRemoteJOGLViewerImp();
    }
    
    public static HeadtrackedRemoteJOGLViewerImp getInstance() {
        Singleton.instance.initFrame();
        return Singleton.instance;
    }
    
    private HeadtrackedRemoteJOGLViewerImp() {
        super(new de.jreality.jogl.InteractiveViewer());
    }
    protected void init() {
        super.init();
        getViewer().setStereoType(de.jreality.jogl.Viewer.CROSS_EYED_STEREO);
        getViewer().getHelpOverlay().setVisible(config.getBool("viewer.showFPS"));
        getViewer().setAutoSwapMode(config.getBool("viewer.autoBufferSwap"));
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

    /**
     * TODO !!
     */
    public void setUseDisplayLists(boolean b) {
//        getViewer().getRenderer().setUseDisplayLists(b);
    }
    public void waitForRenderFinish() {
        getViewer().waitForRenderFinish();
    }
    public void setBackgroundColor(java.awt.Color color) {
        getViewer().setBackgroundColor(color);
    }
	public void setRemoteSceneRoot(RemoteSceneGraphComponent r) {
		GLCanvas g = (GLCanvas) getViewer().getViewingComponent();
		System.out.println(g);
		super.setRemoteSceneRoot(r);
		g = (GLCanvas) getViewer().getViewingComponent();
		System.out.println(g);
	}

    public void resetCalled() {
        System.out.println("disposing prev viewer instance");
        getInstance().setRemoteSceneRoot(null);
        getInstance().f.hide();
        //getInstance().f.dispose();
    }
}
