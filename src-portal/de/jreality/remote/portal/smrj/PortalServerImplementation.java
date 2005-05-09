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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import szg.framework.event.*;
import szg.framework.event.remote.RemoteEventQueueImpl;
import de.jreality.portal.tools.EventBoxVisitor;
import de.jreality.portal.tools.WandTool;
import de.jreality.reader.Input;
import de.jreality.reader.Readers;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.EgoShooterTool;
import de.jreality.scene.tool.PickSystem;
import de.jreality.scene.tool.TestTool;
import de.jreality.scene.tool.ToolSystem;
import de.jreality.util.BoundingBoxTraversal;
import de.jreality.util.CmdLineParser;
import de.jreality.util.LoadableScene;
import de.jreality.util.Lock;
import de.jreality.util.P3;
import de.jreality.util.Rectangle3D;
import de.jreality.util.Rn;
import de.smrj.RemoteFactory;
import de.smrj.tcp.TCPBroadcasterIO;
import de.smrj.tcp.TCPBroadcasterNIO;
/**
 * The differences between this class and its superclass are the following: <br>
 * <br>* for the Portal every viewer has already one headtracked camera so we
 * don't set the cameraPath <br>* we need to send the HeadTransformation to the
 * viewers. <br>* needs jSyzygy in the classpath and the native library
 * 
 * @author weissman
 */

public class PortalServerImplementation extends RemoteDistributedViewer implements
        WandListener, WandMotionListener, HeadMotionListener {

    boolean manualSwapBuffers;

    private RemoteEventQueueImpl szgQueue;

    private volatile boolean autoRender = false;
    private final Object autoRenderSynch = new Object();

//    EventBoxVisitor boxVisitor;

    private Thread renderer = new Thread() {

        public void run() {
            while (true) {
                synchronized (autoRenderSynch) {
                    if (!autoRender) try {
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

    private boolean navigationEnabled;
    private boolean useDisplayLists = true;
    private Transformation wandOffset = new Transformation();
    
    private static final Lock renderLock = new Lock();

    private SceneGraphComponent navComp;
    
    public static void writing() {
    	renderLock.writeLock();
    }
    
    public static void writingFinished() {
    	renderLock.writeUnlock();
    }

    public PortalServerImplementation(RemoteFactory factory) throws IOException {
        super(factory);
        manualSwapBuffers = !getConfig().getBool("viewer.autoBufferSwap");
        System.out.println("manualBufferSwap:" + manualSwapBuffers);
        measure = getConfig().getBool("viewer.printRenderTime");
        SceneGraphComponent root = new SceneGraphComponent();
        SceneGraphComponent scaleComp = new SceneGraphComponent();
        SceneGraphComponent realNavComp = new SceneGraphComponent();
        scaleComp.addChild(navComp = new SceneGraphComponent());
        navComp.addTool(new EgoShooterTool());
        navComp.setTransformation(new Transformation());
        realNavComp.addChild(scaleComp);
        root.addChild(realNavComp);
        scaleComp.setTransformation(scaleTrafo = new Transformation());
        wandComp = new SceneGraphComponent();
        root.addChild(wandComp);
        root.addTool(new TestTool());
        super.setSceneRoot(root);

//        boxVisitor = new EventBoxVisitor(root, wandOffset);
        try {
            szgQueue = new RemoteEventQueueImpl();
//            wandTool = new WandTool(realNavComp, wandComp);
            startQueue();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        ToolSystem ts = new ToolSystem(this);
        try {
			PickSystem ps = (PickSystem) Class.forName("de.jreality.jme.intersection.proxy.JmePickSystem").newInstance();
			ps.setSceneRoot(root);
			ts.setPickSystem(ps);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        renderer.setPriority(Thread.MIN_PRIORITY);
        renderer.start();
        setBackgroundColor(new java.awt.Color(132, 132, 218));
    }

    private void startQueue() {
//        szgQueue.addWandListener(wandTool);
//        szgQueue.addWandMotionListener(wandTool);
//        szgQueue.addWandListener(this);
//        szgQueue.addWandMotionListener(this);
        szgQueue.addHeadMotionListener(this);
    }

    private void pauseQueue() {
//        szgQueue.removeWandListener(wandTool);
//        szgQueue.removeWandMotionListener(wandTool);
//        szgQueue.removeWandListener(this);
//        szgQueue.removeWandMotionListener(this);
        szgQueue.removeHeadMotionListener(this);
    }

    //WandTool wandTool;
    SceneGraphComponent sceneRoot;
    SceneGraphComponent wandComp;

    protected RemoteViewer initClients() throws IOException {
        RemoteViewer clients = (RemoteViewer) factory.createRemoteViaStaticMethod(HeadtrackedRemoteJOGLViewerImp.class, HeadtrackedRemoteJOGLViewerImp.class, "getInstance");
        clients.setRemoteSceneRoot(null);
        clients.reset();
        return clients;
    }

    public void setSceneRoot(SceneGraphComponent root) {
        throw new UnsupportedOperationException(
                "can't set Root in portal viewer!");
    }

    /**
     * method to set geometry for the wand. the returned Component has exactly
     * the wand coordinates (world coordinates)
     * 
     * @return
     */
    public SceneGraphComponent getWandComponent() {
        return wandComp;
    }

    /**
     * method to add not movable geometries. the returned Component has world
     * coordinates
     * 
     * @return
     */
    public SceneGraphComponent getSceneRoot() {
        return super.getSceneRoot();
    }

    /**
     * method to add movable geometries. the returned Component has coordinates
     * that can be changed with the wand (if isNavigationEnabled() == true)
     * 
     */
    public SceneGraphComponent getNavigationComponent() {
        return navComp;
    }

    java.awt.Color bgColor;

    public void setBackgroundColor(java.awt.Color color) {
        bgColor = color;
        getClients().setBackgroundColor(bgColor);
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

    private boolean measure;
    public void render() {
        if (rendering) {
            reRender = true;
            return;
        }
        rendering = reRender = true;
        while (reRender) {
            reRender = false;
            renderLock.readLock();
            long s;
            long t;
//            if (headTracked && headChanged) {
//                headMatrixLock.readLock();
//                s  = System.currentTimeMillis();
//                getClients().sendHeadTransformation(headMatrix);
//                t = System.currentTimeMillis() - s;
//if (measure)                System.out.println("sendHead: "+t);
//                headMatrixLock.readUnlock();
//            }
            long start = System.currentTimeMillis();
            renders++;
            s  = System.currentTimeMillis();
            headMatrixLock.readLock();
            getClients().render(headMatrix);
            headMatrixLock.readUnlock();
            t = System.currentTimeMillis() - s;
            if (measure)            System.out.println("render: "+t);
            s  = System.currentTimeMillis();
            getClients().waitForRenderFinish();
            t = System.currentTimeMillis() - s;
            if (measure)            System.out.println("renderFinish: "+t);
            if (manualSwapBuffers) { 
                s  = System.currentTimeMillis();
                getClients().swapBuffers();
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
            renderLock.readUnlock();
        }
    }

    private HeadtrackedRemoteJOGLViewer getClients() {
        return (HeadtrackedRemoteJOGLViewer) clients;
    }

    /** ************* portal event listener implementation ************** */

    private static final boolean DEBUG = true;
    private volatile boolean renderOnHeadMove = true;
    private Transformation scaleTrafo;

    public void headMoved(HeadEvent event) {
        headMatrixLock.writeLock();
        System.arraycopy(Rn.transposeF2D(null, event.getMatrix()), 0,
                headMatrix, 0, 16);
        headMatrixLock.writeUnlock();
        headChanged = true;
        if (renderOnHeadMove && !autoRender) render();
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
        szgQueue.removeHeadMotionListener(this);
//        szgQueue.removeWandListener(wandTool);
//        szgQueue.removeWandMotionListener(wandTool);
        szgQueue.dispose();
        super.dispose();
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
        getClients().setManualSwapBuffers(manualSwapBuffers);
    }

    public boolean isNavigationEnabled() {
        return navigationEnabled;
    }

    public void setNavigationEnabled(boolean navigationEnabled) {
        this.navigationEnabled = navigationEnabled;
//        wandTool.setNavigationEnabled(navigationEnabled);
    }

    public void buttonPressed(WandEvent event) {
//        boxVisitor.process(event);
    }

    public void buttonReleased(WandEvent event) {
//        boxVisitor.process(event);
    }

    public void buttonTipped(WandEvent event) {
    }

    public void axisMoved(WandEvent event) {
    }

    public void wandDragged(WandEvent event) {
//        boxVisitor.process(event);
    }

    public void wandMoved(WandEvent event) {
//        boxVisitor.process(event);
    }

    public Transformation getWandOffset() {
        return wandOffset;
    }

    public void setWandOffset(Transformation wandOffset) {
        this.wandOffset = wandOffset;
//        boxVisitor.setWandOffset(wandOffset);
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
        getClients().setUseDisplayLists(useDisplayLists);
    }
    
    public void loadWorld(String classname) {
        pauseQueue();
        long t = System.currentTimeMillis();
        LoadableScene wm = null;
        try {
            wm = (LoadableScene) Class.forName(classname).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // scene settings
        wm.setConfiguration(getConfig());
        de.jreality.scene.SceneGraphComponent world = wm.makeWorld();
        long s = System.currentTimeMillis() - t;
        System.out.println("make world " + classname + " successful. ["+s+"ms]");
        setSignature(wm.getSignature());
        t = System.currentTimeMillis();
        if (world != null) getNavigationComponent().addChild(world);
//        wandTool.center();
        s = System.currentTimeMillis() - t;
        System.out.println("distributed world " + classname +"["+s+"ms]");
        startQueue();
    }

    private void loadFile(String name) {
        long t = System.currentTimeMillis();
        pauseQueue();
        de.jreality.scene.SceneGraphComponent world = Readers.readFile(new File(name));
        if (world != null) getNavigationComponent().addChild(world);
        center();
//        wandTool.center();
        startQueue();
        long s = System.currentTimeMillis() - t;
        System.out.println("loaded file " + name + " successful. ["+s+"ms]");
    }
    
    /**
     * this method simply trnaslates the center of the boundingbox to (0,2,-2);
     * 
     * @param root
     * @return
     */
    Transformation worldTransform = new Transformation();
      public void center() {
          BoundingBoxTraversal bbv = new BoundingBoxTraversal();
          bbv.traverse(navComp);
          Rectangle3D worldBox = bbv.getBoundingBox();
          Transformation t = new Transformation();
          double[] transl = worldBox.getCenter();
          transl[1] -= 2; transl[2] += 2;
          t.setTranslation(transl);
          worldTransform.multiplyOnRight(t.getInverse());
          navComp.getTransformation().setMatrix(worldTransform.getMatrix());
      }


    private static String usage(CmdLineParser parser) {
        String ret = "usage: java PortalServerImplementation "+parser.usageString()+" <filename | classname>\n";
        ret +=parser.descriptionString();
        ret +="\tfilename\t3D data file\n";
        ret +="\tclassname\tfull classname of class that implements de.jreality.scene.LoadableScene\n";
        return ret;
    }
    
    public static void main(String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option ioOption = parser.addBooleanOption("io");
        parser.addDescription("io", "use blocking io");
        CmdLineParser.Option worldOpt = parser.addBooleanOption('w', "world");
        parser.addDescription("world", "given argument is classname of a LoadableScene (not a 3d data file)");
        CmdLineParser.Option propOpt = parser.addStringOption('p', "properties");
        parser.addDescription("properties", "the jreality property file to use");
        CmdLineParser.Option scaleOpt = parser.addDoubleOption('s', "scale");
        parser.addDescription("scale", "the global scale for displaying the given scene");
        CmdLineParser.Option helpOpt = parser.addBooleanOption('h', "help");
        parser.addDescription("help", "print this usage message");
        parser.parse(args);
        if (((Boolean)parser.getOptionValue(helpOpt)).booleanValue()) {
            System.out.println(usage(parser));
            System.exit(0);
        }
        String propFile = (String) parser.getOptionValue(propOpt);
        if (propFile != null) {
            System.setProperty("jreality.config", new File(propFile).getAbsolutePath());
        }
        System.out.println("jreality.config: "+System.getProperty("jreality.config"));
        String[] dataArgs = parser.getRemainingArgs();
        if (dataArgs == null || dataArgs.length != 1) {
            System.err.println(usage(parser));
            System.exit(2);
        }
        Boolean b = ((Boolean) parser.getOptionValue(ioOption));
        boolean io = b.booleanValue();
        if (io) System.err.println("Warning: using blocking IO");
        PortalServerImplementation rsi = new PortalServerImplementation(
                io ? new TCPBroadcasterIO(8868).getRemoteFactory()
                    : new TCPBroadcasterNIO(8868).getRemoteFactory());
        Double scale = (Double) parser.getOptionValue(scaleOpt);
        if (scale != null) {
            System.out.println("setting scale to "+scale);
            rsi.setGlobalScale(scale.doubleValue());
        }
        boolean loadWorld = ((Boolean)parser.getOptionValue(worldOpt)).booleanValue();
        if (loadWorld) rsi.loadWorld(dataArgs[0]);
        else rsi.loadFile(dataArgs[0]);
	rsi.setNavigationEnabled(true);
    }

    private void setGlobalScale(double d) {
        scaleTrafo.setStretch(d);
    }

    public boolean isMeasure() {
		return measure;
	}
	public void setMeasure(boolean measure) {
		this.measure = measure;
	}
	public RemoteEventQueueImpl getSzgQueue() {
		return szgQueue;
	}
}
