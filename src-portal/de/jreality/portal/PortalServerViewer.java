package de.jreality.portal;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;

import szg.framework.event.HeadEvent;
import szg.framework.event.HeadMotionListener;
import szg.framework.event.remote.RemoteEventQueueImpl;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Drawable;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickSystem;
import de.jreality.scene.proxy.scene.RemoteSceneGraphComponent;
import de.jreality.scene.proxy.smrj.SMRJMirrorScene;
import de.jreality.scene.tool.DraggingTool;
import de.jreality.scene.tool.EgoShooterTool;
import de.jreality.scene.tool.TestTool;
import de.jreality.scene.tool.ToolSystem;
import de.jreality.util.CmdLineParser;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.jreality.util.Lock;
import de.jreality.util.LoggingSystem;
import de.jreality.util.MatrixBuilder;
import de.jreality.util.P3;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;
import de.smrj.RemoteFactory;
import de.smrj.tcp.TCPBroadcasterIO;
import de.smrj.tcp.TCPBroadcasterNIO;
/*
 * Created on Apr 13, 2005
 *
 * This file is part of the  package.
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

/**
 * @author weissman
 *
 */
public class PortalServerViewer implements Viewer, HeadMotionListener {
	
	SceneGraphComponent root;
	SceneGraphPath camPath;
	RemoteFactory factory;
	private int signature;

	RemoteJoglViewer clients;
    SMRJMirrorScene proxyScene;

    private RemoteEventQueueImpl szgQueue;
    
    private ToolSystem toolSystem;
    
    private Color backgroundColor = new Color(122,122,122);

    private final Lock headMatrixLock = new Lock();
	private final double[] headMatrix = new double[16];
	private volatile boolean headChanged;
	private boolean manualSwapBuffers;
	private boolean rendering;
	private boolean reRender;

	public PortalServerViewer(RemoteFactory factory) throws IOException, MalformedURLException, RemoteException, NotBoundException {
		this.factory = factory;
        clients = (RemoteJoglViewer) factory.createRemoteViaStaticMethod(
        		PortalJoglClientViewer.class,
        		PortalJoglClientViewer.class, "getInstance");
        proxyScene = new SMRJMirrorScene(factory);
        szgQueue = new RemoteEventQueueImpl();
	}
	
	public void start() {
        szgQueue.addHeadMotionListener(this);
	}
	
	public void pause() {
		szgQueue.removeHeadMotionListener(this);
	}
	
	public void headMoved(HeadEvent event) {
        headMatrixLock.writeLock();
        System.arraycopy(Rn.transposeF2D(null, event.getMatrix()), 0,
                headMatrix, 0, 16);
        headMatrixLock.writeUnlock();
        headChanged = true;
        render();
	}

	public SceneGraphComponent getSceneRoot() {
		return root;
	}

	public void setSceneRoot(SceneGraphComponent r) {
		this.root = r;
        applyBackgroundColor();
		RemoteSceneGraphComponent rsgc = (RemoteSceneGraphComponent) proxyScene.createProxyScene(root);
		clients.setRemoteSceneRoot(rsgc);
	}

	public SceneGraphPath getCameraPath() {
		return camPath;
	}

	public void setCameraPath(SceneGraphPath p) {
		camPath = p;
		clients.setRemoteCameraPath(proxyScene.getProxies(p.toList()));
	}

    int statDelay = 10000; // milliseconds between statistic output
    int runs = 0;
    int renders = 0;
    long startTime = System.currentTimeMillis();
    long maxFrameTime = 0;

    public void render() {
    	if (root == null || camPath == null) {
    		LoggingSystem.getLogger(this).log(Level.FINER, "not rendering - roo or camera path is null");
    		return;
    	}
        if (rendering) {
            reRender = true;
            return;
        }
        rendering = reRender = true;
        while (reRender) {
            reRender = false;
            long start = System.currentTimeMillis();
            renders++;
            headMatrixLock.readLock();
            clients.render(headMatrix);
            headMatrixLock.readUnlock();
            clients.waitForRenderFinish();
            if (manualSwapBuffers) clients.swapBuffers();
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
    }

	/**
	 * TODO: open frame for keyboard/mouse input!?
	 */
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
	}

	public int getSignature() {
		return signature;
	}

	public void setSignature(int sig) {
		this.signature = sig;
        clients.setSignature(this.signature);
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
        PortalServerViewer rsi = new PortalServerViewer(
                io ? new TCPBroadcasterIO(8868).getRemoteFactory()
                    : new TCPBroadcasterNIO(8868).getRemoteFactory());
        Double scale = (Double) parser.getOptionValue(scaleOpt);
        if (scale != null) {
            System.out.println("setting scale to "+scale);
            rsi.setFileScale(scale.doubleValue());
        }
        boolean loadWorld = ((Boolean)parser.getOptionValue(worldOpt)).booleanValue();
        if (loadWorld) rsi.loadWorld(dataArgs[0]);
        else rsi.loadFile(dataArgs[0]);
        rsi.setBackgroundColor(new java.awt.Color(0f, 0.2f, 0.2f));
        rsi.start();
    }

	public void setBackgroundColor(Color color) {
		this.backgroundColor = color;
        applyBackgroundColor();
	}

    private void applyBackgroundColor() {
        if (getSceneRoot() != null) {
            Appearance ap = getSceneRoot().getAppearance();
            if (ap == null) {
                ap = new Appearance();
                getSceneRoot().setAppearance(ap);
            }
            ap.setAttribute(CommonAttributes.BACKGROUND_COLOR, backgroundColor);
        }
    }

	private SceneGraphComponent defaultRoot;
    private SceneGraphPath defaultCamPath;
    
    private void initDefaultScene() {
    	defaultRoot = new SceneGraphComponent();
    	defaultRoot.setTransformation(new Transformation());
    	defaultRoot.setName("root node");
    	SceneGraphComponent portal = new SceneGraphComponent();
    	portal.setTransformation(new Transformation());
    	portal.setName("portal node");
    	defaultRoot.addChild(portal);
    	Camera camera = new Camera();
    	portal.setCamera(camera);
    	defaultCamPath = new SceneGraphPath();
    	defaultCamPath.push(defaultRoot);
    	defaultCamPath.push(portal);
    	defaultCamPath.push(camera);
    	defaultRoot.addChild(makeLights());
    	portal.addTool(new EgoShooterTool());
    }
    
    private double fileScale=1.;
	private void setFileScale(double d) {
		fileScale = d;
	}

	private void loadFile(String name) {
        long t = System.currentTimeMillis();
        try {
          de.jreality.scene.SceneGraphComponent world = Readers.read(new File(name));
          setUpWorld(world);
          long s = System.currentTimeMillis() - t;
          System.out.println("loaded file " + name + " successful. ["+s+"ms]");
        } catch (IOException e) {
          e.printStackTrace();
          System.out.println("loading file " + name + " failed!");
        }
	}

	/**
     * @param world
     */
    private void setUpWorld(de.jreality.scene.SceneGraphComponent world) {
        if (world != null) {
        	if (defaultRoot == null || !defaultCamPath.isValid()) {
        		initDefaultScene();
        	}
        	defaultRoot.addChild(world);
        	world.addTool(new DraggingTool());
        	world.addTool(new TestTool());
        	world.setTransformation(new Transformation());
        	world.getTransformation().setMatrix(MatrixBuilder.euclidian().scale(fileScale).getMatrix().getArray());
        	setSceneRoot(defaultRoot);
        	setCameraPath(defaultCamPath);
        	ToolSystem ts = new ToolSystem(this);
        	try {
        		PickSystem ps = (PickSystem) Class.forName("de.jreality.jme.intersection.proxy.JmePickSystem").newInstance();
        		ts.setPickSystem(ps);
        	} catch (Exception e) {
        		LoggingSystem.getLogger(this).log(Level.WARNING, "PickSystem instanciation failed", e);
        	}
        }
    }

    private void loadWorld(String classname) {
        long t = System.currentTimeMillis();
        LoadableScene wm = null;
        try {
            wm = (LoadableScene) Class.forName(classname).newInstance();
        } catch (Exception e) {
            LoggingSystem.getLogger(this).log(Level.WARNING, "problem loading world", e);
            return;
        }
        // scene settings
        wm.setConfiguration(ConfigurationAttributes.getDefaultConfiguration());
        SceneGraphComponent world = wm.makeWorld();
        long s = System.currentTimeMillis() - t;
        System.out.println("make world " + classname + " successful. ["+s+"ms]");
        setSignature(wm.getSignature());
        t = System.currentTimeMillis();
        if (world != null) setUpWorld(world);
        s = System.currentTimeMillis() - t;
        System.out.println("distributed world " + classname +"["+s+"ms]");
	}

    static SceneGraphComponent makeLights()    {
        SceneGraphComponent lights = new SceneGraphComponent();
        lights.setName("lights");
        SpotLight pl = new SpotLight();
        pl.setFalloff(1.0, 0.0, 0.0);
        pl.setColor(new Color(120, 250, 180));
        pl.setConeAngle(Math.PI);
        pl.setIntensity(0.6);
        SceneGraphComponent l0 = SceneGraphUtilities.createFullSceneGraphComponent("light0");
        l0.setLight(pl);
        lights.addChild(l0);
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(new Color(250, 100, 255));
        dl.setIntensity(0.6);
        l0 = SceneGraphUtilities.createFullSceneGraphComponent("light1");
        double[] zaxis = {0,0,1};
        double[] other = {1,1,1};
        l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other));
        l0.setLight(dl);
        lights.addChild(l0);
        return lights;
    }

	/**
	 * @return
	 */
	public RemoteEventQueueImpl getSzgQueue() {
		return szgQueue;
	}

}
