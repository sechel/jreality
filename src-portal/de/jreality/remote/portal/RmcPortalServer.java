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

package de.jreality.remote.portal;

import java.awt.Color;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import szg.framework.event.HeadEvent;
import szg.framework.event.HeadMotionListener;
import szg.framework.event.WandEvent;
import szg.framework.event.WandListener;
import szg.framework.event.WandMotionListener;
import szg.framework.event.remote.RemoteEventQueueImpl;
import de.jreality.remote.ClientDisconnectedException;
import de.jreality.remote.RemoteServerClient;
import de.jreality.remote.RemoteServerImpl;
import de.jreality.remote.RemoteViewer;
import de.jreality.remote.RmcRemoteServer;
import de.jreality.remote.util.INetUtilities;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.Lock;
import de.jreality.util.P3;

/**
 * The differences between this class and its superclass are the following:<br>
 * <br>
 * * for the Portal every viewer has already one headtracked camera so we don't set the cameraPath<br>
 * * we need to send the HeadTransformation to the viewers.<br>
 * * needs jSyzygy in the classpath and the native library
 * 
 * @author weissman
 */

public class RmcPortalServer extends RmcRemoteServer implements WandListener, WandMotionListener, HeadMotionListener {
		
	private static final long serialVersionUID = -7211946652867582081L;
	boolean manualSwapBuffers;
	SceneGraphComponent navComp;

	private RemoteEventQueueImpl queue;
	
	private volatile boolean autoRender = false;
	private final Object autoRenderSynch = new Object();
	
	EventBoxVisitor boxVisitor;
	
	private Thread renderer = new Thread() {
		public void run() {
			while (true) {
				synchronized(autoRenderSynch) {
					if (!autoRender)
						try {
							autoRenderSynch.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
				while (autoRender) {
					render();
//					try {
//						Thread.sleep(1);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
			}
		}
	};
//	private ControlPanel controlPanel;
	
	private boolean navigationEnabled;
	private boolean useDisplayLists = true;
	private Transformation wandOffset = new Transformation();
	
	public RmcPortalServer() throws RemoteException {
		super();
		manualSwapBuffers = !getConfig().getBool("viewer.autoBufferSwap");
		System.out.println("manualBufferSwap:"+manualSwapBuffers);
		SceneGraphComponent root = new SceneGraphComponent();
		navComp = new SceneGraphComponent();
		root.addChild(navComp);
		wandComp = new SceneGraphComponent();
		root.addChild(wandComp);
		super.setSceneRoot(root);
		boxVisitor = new EventBoxVisitor(root, wandOffset);
		try {
			queue = new RemoteEventQueueImpl();
			wandTool = new WandTool(navComp, wandComp);
			queue.addWandListener(wandTool);
			queue.addWandMotionListener(wandTool);
			queue.addWandListener(this);
			queue.addWandMotionListener(this);
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		queue.addHeadMotionListener(this);
		renderer.setPriority(Thread.MIN_PRIORITY);
		renderer.start();
//		controlPanel = new ControlPanel();
	}
	
	WandTool wandTool;
	SceneGraphComponent sceneRoot;
	SceneGraphComponent wandComp;
	public void setSceneRoot(SceneGraphComponent root) {
		throw new UnsupportedOperationException("can't set Root in portal viewer!");
	}
	
	/**
	 * method to set geometry for the wand. 
	 * the returned Component has exactly the wand coordinates (world coordinates)
	 * @return
	 */
	public SceneGraphComponent getWandComponent() {
		return wandComp;
	}
	
	/**
	 * method to add not movable geometries. 
	 * the returned Component has world coordinates
	 * @return
	 */
	public SceneGraphComponent getSceneRoot() {
		return super.getSceneRoot();
	}
	
	/**
	 * method to add movable geometries. 
	 * the returned Component has coordinates that can be changed with the wand (if isNavigationEnabled() == true)
	 * @return
	 */
	public SceneGraphComponent getNavigationComponent() {
		return navComp;
	}
	
	java.awt.Color bgColor;
	public void setBackgroundColor(java.awt.Color color) {
		bgColor = color;
		execute("setBackgroundColor", new Object[]{color});
	}
	
	Lock headMatrixLock = new Lock();
	double[] headMatrix=new double[16];
	boolean headChanged = false;
	final Object renderSynch = new Object();
	int statDelay = 10000; // milliseconds between statistic output
	int runs = 0;
	int renders = 0;
	long startTime = System.currentTimeMillis();
	long maxFrameTime = 0;
	private boolean headTracked = true;
	
	public void render() {
		if (rendering) { reRender = true; return; }
		if (Thread.currentThread() == env) {
			new Thread(new Runnable() {
				public void run() {
					RmcPortalServer.this.render();
				}
			}).start();
			return;
		}
		rendering = reRender = true;
		while (reRender) {
			reRender = false;
			if (headTracked && headChanged) {
				headMatrixLock.readLock();
				execute("sendHeadTransformation", new Object[]{headMatrix});
				headMatrixLock.readUnlock();
			}
			long start = System.currentTimeMillis();
			renders++;
			execute("render", new Object[]{});
			execute("waitForRenderFinish", null);
			if (manualSwapBuffers) execute("swapBuffers", null);
			long delay = System.currentTimeMillis() - start;
			if (maxFrameTime < delay) maxFrameTime = delay;
			long locTime = System.currentTimeMillis() - startTime;
			if (locTime >= statDelay) {// statistics
				System.out.println("************* stats  *****************");
				System.out.println("elapsed time: "+ locTime *0.001+ " sec");
				System.out.println("fps: " + ((double)renders) / ((double)locTime * 0.001)  );
				System.out.println("wait(): " +runs);
				System.out.println("Max. frametime: " +maxFrameTime+ "[fps: "+(1./(maxFrameTime*0.001))+"]");
				System.out.println("******************************");
				runs = renders = 0;
				maxFrameTime = 0;
				startTime = System.currentTimeMillis();
			}
			runs++;
			rendering = false;
		}
	}

	/*************** portal event listener implementation ***************/
	
	private static final boolean DEBUG = true;
	private volatile boolean renderOnHeadMove = true;
	
	public void headMoved(HeadEvent event) {
		headMatrixLock.writeLock();
		System.arraycopy(P3.transposeF2D(null, event.getMatrix()), 0, headMatrix, 0, 16);
		headMatrixLock.writeUnlock();
		headChanged = true;
		if (renderOnHeadMove && !autoRender) render();
	}

	Transformation tmpTrans = new Transformation();
	

	public static void main(String[] args) throws RemoteException {
		String hostname = INetUtilities.getHostname();
		RmcPortalServer rsi = new RmcPortalServer();
		rsi.setBackgroundColor(new Color(120, 10, 44, 20));
		rsi.loadWorld(args[0]);
	}

	public boolean isAutoRender() {
		return autoRender;
	}
	public void setAutoRender(boolean autoRender) {
		this.autoRender = autoRender;
		synchronized (autoRenderSynch) {
			autoRenderSynch.notify();
		}
	}
	
	
	public void dispose() {
		setAutoRender(false);
		setNavigationEnabled(false);
		queue.removeHeadMotionListener(this);
		queue.removeWandListener(wandTool);
		queue.removeWandMotionListener(wandTool);
		queue.dispose();
	}
	public boolean isRenderOnHeadMove() {
		return renderOnHeadMove;
	}
	public void setRenderOnHeadMove(boolean renderOnHeadMove) {
		this.renderOnHeadMove = renderOnHeadMove;
	}

	public boolean isManualSwapBuffers() {
		return manualSwapBuffers;
	}

	public void setManualSwapBuffers(boolean manualSwapBuffers) {
		this.manualSwapBuffers = manualSwapBuffers;
		execute("setManualSwapBuffers", new Object[]{new Boolean(manualSwapBuffers)});
	}

	public boolean isNavigationEnabled() {
		return navigationEnabled;
	}
	public void setNavigationEnabled(boolean navigationEnabled) {
		this.navigationEnabled = navigationEnabled;
		wandTool.setNavigationEnabled(navigationEnabled);
	}

	public void buttonPressed(WandEvent event) {
		boxVisitor.process(event);
	}

	public void buttonReleased(WandEvent event) {
		boxVisitor.process(event);
	}

	public void buttonTipped(WandEvent event) {
	}

	public void axisMoved(WandEvent event) {
	}

	public void wandDragged(WandEvent event) {
		boxVisitor.process(event);
	}

	public void wandMoved(WandEvent event) {
		boxVisitor.process(event);
	}

	public Transformation getWandOffset() {
		return wandOffset;
	}
	public void setWandOffset(Transformation wandOffset) {
		this.wandOffset = wandOffset;
		boxVisitor.setWandOffset(wandOffset);
	}
	public boolean isHeadTracked() {
		return headTracked;
	}
	public void setHeadTracked(boolean headTracked) {
		this.headTracked = headTracked;
	}
	public boolean isUseDisplayLists() {
		return useDisplayLists;
	}
	public void setUseDisplayLists(boolean useDisplayLists) {
		this.useDisplayLists = useDisplayLists;
		execute("setUseDisplayLists", new Object[]{new Boolean(useDisplayLists)});
	}
}