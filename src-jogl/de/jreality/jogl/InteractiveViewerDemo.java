/*
 * Created on Jun 17, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.GeometryUtility;
import de.jreality.reader.Readers;
import de.jreality.renderman.RIBViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.jreality.util.P3;
import de.jreality.util.Pn;
import de.jreality.util.SceneGraphUtilities;
//import de.jreality.soft.DefaultViewer;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class InteractiveViewerDemo extends JFrame{
	public final static int STANDARD = 1;
	public final static int TABBED_PANE = 2;
	public final static int SPLIT_PANE = 3;
//	JFrame frame;
	JSplitPane splitPanel;
	protected JMenuBar theMenuBar;
	protected Vector viewerList;	
	//TODO rename currentViewer
	protected InteractiveViewer viewer;
	JTabbedPane tabbedPane;
	//DefaultViewer softViewer;
	boolean showSoft = true, isEncompass = false, addBackPlane = false;
	int mode;
	Box hack;
	boolean fullScreen = false;
	boolean loadedScene = false;
	int signature = Pn.EUCLIDEAN;
	boolean showCameraPathInspector = false;
	
	protected static String resourceDir = System.getProperty("home.dir"), saveResourceDir = System.getProperty("home.dir");
	static {
		String foo = System.getProperty("jreality.jogl.resourceDir");
		if (foo != null) saveResourceDir = resourceDir = foo; 
	}
	/**
	 * 
	 */
	public InteractiveViewerDemo() {
		this(STANDARD, false);
	}
	
	public InteractiveViewerDemo(boolean f) {
		this(STANDARD, f);
	}
	
	public InteractiveViewerDemo(int m, boolean f)	{
		super();
		mode = m;
		fullScreen = f;
		if (fullScreen)	{
			dispose();
			setUndecorated(true);
			getGraphicsConfiguration().getDevice().setFullScreenWindow(this);		
			mode = STANDARD;
		}
	
		String foo = System.getProperty("jreality.jogl.resourceDir");
		if (foo != null) resourceDir = foo;
		
		viewer = new InteractiveViewer(null, null);
		viewerList = new Vector();
		viewerList.add(viewer);
		Logger.getLogger("de.jreality").log(Level.INFO,
		  "Created viewer {0}", viewer);

		getContentPane().setLayout(new BorderLayout());
		// WARNING:
		// The frame has to be visible (on some versions of linux!) before we can safely add the GLCanvas!!
		setVisible(true);
		if (mode == SPLIT_PANE)	{
			Viewer v2 = new Viewer(null, null);
			v2.initializeFrom(viewer);
			splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, viewer.getViewingComponent(),v2.getViewingComponent());
			splitPanel.setDividerLocation(0.5d);
			//setSize(800, 400);	
			getContentPane().add(splitPanel, BorderLayout.CENTER);			
		} else if (mode == TABBED_PANE){
			tabbedPane = new JTabbedPane();
			addViewer(viewer, "Standard");
			getContentPane().add(tabbedPane, BorderLayout.CENTER);
			setSize(800,600);
			ChangeListener vl = new ChangeListener()	{
				
				public void stateChanged(ChangeEvent e) {
					System.out.println("stateChanged: now there are: "+tabbedPane.getTabCount());
					Component c = tabbedPane.getSelectedComponent();
					if (c == null) return;
					Iterator iter = viewerList.iterator();
					boolean found = false;
					while (iter.hasNext())	{
						InteractiveViewer v = (InteractiveViewer) iter.next();
						if (v.getViewingComponent() == c)  {
							viewer = v;
							System.out.println("Current viewer is "+viewer);
							found = true;
							break;
						}
					}
					if (!found)	{
						System.out.println("No such viewing component: "+c);
					}
					else viewer.render();
				}
			};
			
			tabbedPane.addChangeListener(vl);
		} else {
			getContentPane().add(viewer.getViewingComponent(), BorderLayout.CENTER);
			if (!fullScreen) setSize(800, 600);
		}

		JToolBar tb = viewer.getToolManager().getToolbar();
		ToolTipManager.sharedInstance().setEnabled(true);
		hack = Box.createHorizontalBox();
		hack.add(tb);
		hack.add(Box.createHorizontalGlue());
		if (!fullScreen) getContentPane().add(hack, BorderLayout.NORTH);
		//This fixes a bug in the Linux version of GLCanvas which prevented menus from showing up
		 JPopupMenu.setDefaultLightWeightPopupEnabled( false ) ;
				
		//TODO this should go into the main() method-- but that belongs to the subclass... hmmmm
		addWindowListener(new java.awt.event.WindowAdapter() {
			 public void windowClosing(java.awt.event.WindowEvent evt) {
				 System.exit(0);
			 }
		 });

	}
    SceneGraphComponent world = null;
    public void begin()	{
    		initializeScene();
    		loadScene(null);
    }
    
	SceneGraphComponent root;
	public void initializeScene()	{
		root = viewer.getSceneRoot();
		SceneGraphComponent lights = makeLights();		
		if (lights != null)	CameraUtility.getCameraNode(viewer).addChild(lights);		
	}
	
	public void unloadScene()	{
		if (world != null && root.isDirectAncestor(world))	root.removeChild(world);
		viewer.getSceneRoot().setAppearance(null);
		viewer.getSelectionManager().setDefaultSelection(null);
		viewer.getSelectionManager().setSelection(null);
	}
	
	public void loadScene(String loadableScene)	{
		root = viewer.getSceneRoot();
		if (root.getAppearance() == null) root.setAppearance(new Appearance());
		CommonAttributes.setDefaultValues(root.getAppearance());
		root.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		root.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
		root.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
		LoadableScene wm = null;
		
		if (loadableScene == null)  world = makeWorld();
		else		{
		       try {
	            wm = (LoadableScene) Class.forName(loadableScene).newInstance();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        // scene settings
	        wm.setConfiguration(ConfigurationAttributes.getDefaultConfiguration());
	        world = wm.makeWorld();
	        signature = wm.getSignature();
	        isEncompass = wm.isEncompass();
	        addBackPlane = wm.addBackPlane();
	        loadedScene = true;
		}
		if (world != null && !root.isDirectAncestor(world)) {		// sometimes the subclass has already added the world
			root.addChild(world);
			if (world.getTransformation() == null) 		world.setTransformation(new Transformation());
		}

		CameraUtility.getCamera(viewer).setSignature(getSignature());
		CameraUtility.getCamera(viewer).reset();		
		if (isEncompass())	{
			// I have to do this ... for reasons unknown ... or else the encompass sometimes fails.
			CameraUtility.getCameraNode(viewer).getTransformation().setTranslation(0d, 0d, 2d);
			CameraUtility.encompass(viewer);
		}
	
		if (addBackPlane()) viewer.addBackPlane();
		else viewer.removeBackPlane();

		SceneGraphUtilities.setDefaultMatrix(root);
		SceneGraphUtilities.setSignature(root, getSignature());

		SceneGraphPath ds = viewer.getSelectionManager().getDefaultSelection();
		if (ds == null && world != null)	{
			ds = SceneGraphUtilities.findFirstPathBetween(root, world);
			viewer.getSelectionManager().setDefaultSelection(ds);
			viewer.getSelectionManager().setSelection(ds);	
		} 
		if (theMenuBar != null)	hack.remove(theMenuBar);
		 theMenuBar = createMenuBar();
		 if (loadedScene) wm.customize(theMenuBar, viewer);
		 hack.add(theMenuBar, 0);
		hack.add(Box.createHorizontalGlue(), 1);
		setVisible(true);
		repaint();
		viewer.render();
		viewer.getViewingComponent().requestFocus();
		javax.swing.Timer foo = new javax.swing.Timer(33, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {update(); } } );
		foo.start();
	}

	SceneGraphPath camPath = null;
	FramedCurveInspector fci;
	boolean showInspector = false;
	protected JMenu fileM;
	// TODO clean this up to make adding menu items easy (via Actions?)
	public JMenuBar createMenuBar()	{
		theMenuBar = new JMenuBar();
		fileM = new JMenu("File");
    JMenuItem jcc = new JMenuItem("Open...");
    fileM.add(jcc);
    jcc.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e)  {
            loadFile();
            viewer.render();
        }
    });
	jcc = new JMenuItem("Save Screen...");
	fileM.add(jcc);
	jcc.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e)	{
			saveToFile();
		}
	});
	jcc = new JMenuItem("Save RIB...");
	fileM.add(jcc);
	jcc.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e)	{
			saveRIBToFile();
		}
	});
		theMenuBar.add(fileM);
		JMenu testM = new JMenu("Windows");
		if (showCameraPathInspector)	{
			jcc = new JCheckBoxMenuItem("Camera Path Inspector...");
			jcc.setSelected(showInspector);
			testM.add(jcc);
			jcc.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					showInspector = !showInspector;
					toggleInspection();
				}
			});			
		}
		jcc = new JCheckBoxMenuItem("New Viewer");
		jcc.setSelected(showInspector);
		testM.add(jcc);
		jcc.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				InteractiveViewer iv = new InteractiveViewer();
				iv.initializeFrom(viewer);
				addViewer(iv, "testing");
			}
		});
		theMenuBar.add(testM);
		return theMenuBar;
	}
	
	protected void loadFile() {
		SceneGraphComponent parent= null;
		SceneGraphPath sgp = viewer.getSelectionManager().getSelection();
    boolean hasSelection = false;
    if (sgp != null) {
        hasSelection = true;
    		if (! (sgp.getLastElement() instanceof SceneGraphComponent)) {
    			System.out.println("Unable to load file; invalid selection");
    			return;
    		}
    		parent = (SceneGraphComponent) sgp.getLastElement();
    } else {
        System.out.println("SelectrionPath == null!");
        parent = root;
        sgp = new SceneGraphPath();
        sgp.push(parent);
    }
		JFileChooser fc = new JFileChooser(resourceDir);
		//System.out.println("FCI resource dir is: "+resourceDir);
		int result = fc.showOpenDialog(this);
		SceneGraphComponent sgc = null;
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			sgc = Readers.readFile(file);
			parent.addChild(sgc);
      sgp.push(sgc);
			resourceDir = file.getAbsolutePath();
		} else {
			System.out.println("Unable to open file");
			return;
		}
		viewer.getSelectionManager().setSelection(sgp);
		viewer.render();
	}
	protected void saveToFile() {
		SceneGraphComponent parent= null;
		JFileChooser fc = new JFileChooser(saveResourceDir);
		//System.out.println("FCI resource dir is: "+resourceDir);
		int result = fc.showSaveDialog(this);
		SceneGraphComponent sgc = null;
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			viewer.getRenderer().saveScreenShot(file);
			saveResourceDir = file.getAbsolutePath();
		} else {
			System.out.println("Unable to open file");
			return;
		}
		viewer.render();
	}

	protected void saveRIBToFile() {
		SceneGraphComponent parent= null;
		JFileChooser fc = new JFileChooser(saveResourceDir);
		//System.out.println("FCI resource dir is: "+resourceDir);
		int result = fc.showSaveDialog(this);
		SceneGraphComponent sgc = null;
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			String name = file.getAbsolutePath();
			saveResourceDir = file.getAbsolutePath();
			file.delete();
			RIBViewer ribv = new RIBViewer();
			ribv.setCameraPath(viewer.getCameraPath());
			ribv.setSceneRoot(viewer.getSceneRoot());
			ribv.setHeight(viewer.getViewingComponent().getHeight());
			ribv.setWidth(viewer.getViewingComponent().getWidth());
			ribv.setFileName(name);
			ribv.render();
		} else {
			System.out.println("Unable to open file");
			return;
		}
		viewer.render();
	}

	protected void toggleInspection() {
		if (showInspector) {
			if (fci == null) {
				fci = getCameraPathInspector();
			}
			camPath = viewer.getCameraPath();

			fci.beginInspection();
			repaint();
			System.out.println("Beginning inspector");
		}
		else {
			fci.endInspection();
			repaint();
			viewer.setCameraPath(camPath);
			viewer.render();
			System.out.println("Ending inspector");
		}
	}
	
	// allow subclasses to customize the camera path inspector (hack!)
	public  FramedCurveInspector getCameraPathInspector()	{
		return new FramedCurveInspector(viewer);
	}
	
	public void update()	{
	}
	
	
	public void addViewer(Viewer v, String name)	{
		viewerList.add(v);
		if (mode == TABBED_PANE)	{
			tabbedPane.addTab(name, v.getViewingComponent());
			System.out.println("Adding viewer, now there are: "+tabbedPane.getTabCount());			
		}
	}
	
	public void removeViewer(Viewer v)	{
		if (mode == TABBED_PANE)	{
			tabbedPane.remove(viewer.getViewingComponent());
			System.out.println("Removing viewer, now there are: "+tabbedPane.getTabCount());		
		}
		viewerList.remove(v);
	}
	
	public boolean showSoft()	{
		return false;
	}
	
	public boolean isEncompass()	{
		return isEncompass;
	}
	
	public boolean addBackPlane()	{
		return addBackPlane;
	}
	
	public SceneGraphComponent makeWorld()	{
		return null;
	}
	
	public int getSignature()	{
		return signature;
	}
	
	public SceneGraphComponent makeLights()	{
		SceneGraphComponent lights = new SceneGraphComponent();
		lights.setName("lights");
		//SpotLight pl = new SpotLight();
		PointLight pl = new PointLight();
		//DirectionalLight pl = new DirectionalLight();
		pl.setFalloff(1.0, 0.0, 0.0);
		pl.setColor(new Color(170, 170, 120));
		//pl.setConeAngle(Math.PI);

		pl.setIntensity(0.6);
		SceneGraphComponent l0 = SceneGraphUtilities.createFullSceneGraphComponent("light0");
		l0.setLight(pl);
		lights.addChild(l0);
		DirectionalLight dl = new DirectionalLight();
		dl.setColor(new Color(200, 150, 200));
		dl.setIntensity(0.6);
		l0 = SceneGraphUtilities.createFullSceneGraphComponent("light1");
		double[] zaxis = {0,0,1};
		double[] other = {1,1,1};
		l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other));
		l0.setLight(dl);
		lights.addChild(l0);
		
		return lights;
	}
    public static void main(String[] args) throws Exception {
    		InteractiveViewerDemo iv = new InteractiveViewerDemo();
    		String loadableScene = null;
    		if (args != null && args.length > 0) {
    			//iv.loadWorld(args[0]);
    			loadableScene = args[0];
    		}
    		iv.initializeScene();
    		iv.loadScene(loadableScene);
    }
}
