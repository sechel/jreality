/*
 * Created on Jun 17, 2004
 *
 */
package de.jreality.jogl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
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
import de.jreality.soft.DefaultViewer;
import de.jreality.soft.PSViewer;
import de.jreality.ui.SceneTreeViewer;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.jreality.util.P3;
import de.jreality.util.Pn;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;
//import de.jreality.soft.DefaultViewer;

/**
 * @author Charles Gunn
 *
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
	boolean showSoft = false, isEncompass = false, addBackPlane = false;
	int mode;
	Box hack;
	boolean fullScreen = false;
	boolean loadedScene = false;
	int signature = Pn.EUCLIDEAN;
	boolean showCameraPathInspector = true;
	
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
		JPopupMenu.setDefaultLightWeightPopupEnabled( false ) ;
		mode = m;
		fullScreen = f;
		if (fullScreen)	{
			dispose();
			setUndecorated(true);
			getGraphicsConfiguration().getDevice().setFullScreenWindow(this);		
			mode = STANDARD;
		}
	
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
			final DefaultViewer v2 = new DefaultViewer();
			v2.initializeFrom(viewer);
			JFrame softframe = new JFrame();
			softframe.getContentPane().add(v2.getViewingComponent());
			softframe.show();
			//getContentPane().add(viewer.getViewingComponent(), BorderLayout.CENTER);
			//getContentPane().add(v2.getViewingComponent(), BorderLayout.EAST);
			if (!fullScreen) setSize(800, 600);
//			javax.swing.JPanel moo = new javax.swing.JPanel();
//			moo.setMaximumSize(new java.awt.Dimension(32768,32768));
//			moo.setMinimumSize(new java.awt.Dimension(10,10));
//			moo.setSize(400, 400);
//			moo.add(viewer.getViewingComponent());
//			JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,viewer.getViewingComponent(),v2.getViewingComponent());
//			splitPanel.setDividerLocation(0.5d);
//			splitPanel.setSize(800, 400);	
			javax.swing.Timer timer = new javax.swing.Timer(200, new ActionListener()	{
				public void actionPerformed(ActionEvent e) {
					v2.render();
				}
			});	
				
			timer.start();
//			getContentPane().add(splitPanel, BorderLayout.CENTER);	
//			setSize(800,400);
			getContentPane().add(viewer.getViewingComponent(), BorderLayout.CENTER);
		} else if (mode == TABBED_PANE){
			tabbedPane = new JTabbedPane();
			addViewer(viewer, "Standard");
			getContentPane().add(tabbedPane, BorderLayout.CENTER);
			setSize(800,600);
			ChangeListener vl = new ChangeListener()	{
				
				public void stateChanged(ChangeEvent e) {
					JOGLConfiguration.theLog.log(Level.INFO,"stateChanged: now there are: "+tabbedPane.getTabCount());
					Component c = tabbedPane.getSelectedComponent();
					if (c == null) return;
					Iterator iter = viewerList.iterator();
					boolean found = false;
					while (iter.hasNext())	{
						InteractiveViewer v = (InteractiveViewer) iter.next();
						if (v.getViewingComponent() == c)  {
							viewer = v;
							JOGLConfiguration.theLog.log(Level.INFO,"Current viewer is "+viewer);
							found = true;
							break;
						}
					}
					if (!found)	{
						JOGLConfiguration.theLog.log(Level.INFO,"No such viewing component: "+c);
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
	LoadableScene currentLoadedScene = null;
	JOGLLoadableScene currentJOGLLoadedScene = null;
	Component inspectorPanel = null;
	public void initializeScene()	{
		root = viewer.getSceneRoot();
		SceneGraphComponent lights = makeLights();		
		if (lights != null)	CameraUtility.getCameraNode(viewer).addChild(lights);		
	}
	
	public void unloadScene()	{
		if (currentJOGLLoadedScene != null) currentJOGLLoadedScene.dispose();
		if (treeViewer != null) {
			getContentPane().remove(treeViewer.getViewingComponent());
			treeViewer.removeTreeSelectionListener(treeListener);
			treeViewer = null;
		}
		if (inspectorPanel != null) {
			getContentPane().remove(inspectorPanel);
			inspectorPanel = null;
		}
		setSize(800, 600);
		if (world != null && root.isDirectAncestor(world))	root.removeChild(world);
		CameraUtility.getCameraNode(viewer).getTransformation().setMatrix(Rn.identityMatrix(4));
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
		root.getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(100, 120, 80));
		
		if (loadableScene == null)  world = makeWorld();		// subclasses
		else		{
		       try {
	            currentLoadedScene = (LoadableScene) Class.forName(loadableScene).newInstance();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        currentJOGLLoadedScene = null;
	        if (currentLoadedScene instanceof JOGLLoadableScene) currentJOGLLoadedScene = (JOGLLoadableScene) currentLoadedScene;
	        signature = currentLoadedScene.getSignature();
	        if (currentJOGLLoadedScene != null)	{
		        currentJOGLLoadedScene.setConfiguration(ConfigurationAttributes.getDefaultConfiguration());
		        isEncompass = currentJOGLLoadedScene.isEncompass();
		        addBackPlane = currentJOGLLoadedScene.addBackPlane();        	
	        }
	        // scene settings
	        world = currentLoadedScene.makeWorld();
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
		 if (currentJOGLLoadedScene != null) currentJOGLLoadedScene.customize(theMenuBar, viewer);
		 hack.add(theMenuBar, 0);
		hack.add(Box.createHorizontalGlue(), 1);
		if (currentJOGLLoadedScene != null) {
			inspectorPanel = currentJOGLLoadedScene.getInspector();
		}
		if (inspectorPanel != null) {
			getContentPane().add(inspectorPanel, BorderLayout.SOUTH);
		}
		setVisible(true);
		repaint();
		viewer.render();
		viewer.getViewingComponent().requestFocus();
		updateTreeInspector();
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
	jcc = new JMenuItem("Save PS...");
	fileM.add(jcc);
	jcc.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e)	{
			savePSToFile();
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
		jcc = new JMenuItem("New Viewer");
		testM.add(jcc);
		jcc.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				InteractiveViewer iv = new InteractiveViewer();
				iv.initializeFrom(viewer);
				addViewer(iv, "testing");
			}
		});
		jcc = new JRadioButtonMenuItem("Tree Inspector");
		jcc.setSelected(showTreeInspector);
		testM.add(jcc);
		jcc.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				toggleTreeInspector();
			}
		});
		theMenuBar.add(testM);
		return theMenuBar;
	}
	
	boolean showTreeInspector;
	SceneTreeViewer treeViewer = null;
	HandleTreeSelection treeListener = null;
	public void toggleTreeInspector()	{
		showTreeInspector = !showTreeInspector;
		updateTreeInspector();
	}
	public void updateTreeInspector()	{
		if (showTreeInspector)	{
			if (treeViewer == null)	{
				treeViewer =  SceneTreeViewer.sceneTreeViewerFactory(viewer);
				treeListener = new de.jreality.jogl.HandleTreeSelection(viewer);
				treeViewer.addTreeSelectionListener(treeListener);
				   //treeViewer.initializeFrom(viewer);
				JScrollPane comp = (JScrollPane) treeViewer.getViewingComponent();
				comp.setPreferredSize(new Dimension(300, viewer.getDrawable().getHeight()));
				getContentPane().add(comp, BorderLayout.EAST);		
				pack();
			}
		} else {
			if (treeViewer == null) return;
			getContentPane().remove(treeViewer.getViewingComponent());
			pack();
		}
		treeViewer.setVisible(showTreeInspector);
		repaint();
		viewer.render();
	}
 	
	protected void loadFile() {
		SceneGraphComponent parent= null;
		SceneGraphPath sgp = viewer.getSelectionManager().getSelection();
    boolean hasSelection = false;
    if (sgp != null) {
        hasSelection = true;
    		if (! (sgp.getLastElement() instanceof SceneGraphComponent)) {
    			JOGLConfiguration.theLog.log(Level.WARNING,"Unable to load file; invalid selection");
    			return;
    		}
    		parent = (SceneGraphComponent) sgp.getLastElement();
    } else {
        JOGLConfiguration.theLog.log(Level.INFO,"SelectionPath == null!");
        parent = root;
        sgp = new SceneGraphPath();
        sgp.push(parent);
    }
		JFileChooser fc = new JFileChooser(JOGLConfiguration.resourceDir);
		//JOGLConfiguration.theLog.log(Level.INFO,"FCI resource dir is: "+resourceDir);
		int result = fc.showOpenDialog(this);
		SceneGraphComponent sgc = null;
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			sgc = Readers.readFile(file);
			parent.addChild(sgc);
			sgp.push(sgc);
			JOGLConfiguration.resourceDir = file.getAbsolutePath();
		} else {
			JOGLConfiguration.theLog.log(Level.WARNING,"Unable to open file");
			return;
		}
		viewer.getSelectionManager().setSelection(sgp);
		viewer.render();
	}
	protected void saveToFile() {
		SceneGraphComponent parent= null;
		JFileChooser fc = new JFileChooser(JOGLConfiguration.saveResourceDir);
		//JOGLConfiguration.theLog.log(Level.INFO,"FCI resource dir is: "+resourceDir);
		int result = fc.showSaveDialog(this);
		SceneGraphComponent sgc = null;
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			viewer.getRenderer().saveScreenShot(file);
			JOGLConfiguration.saveResourceDir = file.getAbsolutePath();
		} else {
			JOGLConfiguration.theLog.log(Level.WARNING,"Unable to open file");
			return;
		}
		viewer.render();
	}

	protected void saveRIBToFile() {
		SceneGraphComponent parent= null;
		JFileChooser fc = new JFileChooser(JOGLConfiguration.saveResourceDir);
		//JOGLConfiguration.theLog.log(Level.INFO,"FCI resource dir is: "+resourceDir);
		int result = fc.showSaveDialog(this);
		SceneGraphComponent sgc = null;
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			String name = file.getAbsolutePath();
			JOGLConfiguration.saveResourceDir = file.getAbsolutePath();
			file.delete();
			RIBViewer ribv = new RIBViewer();
			ribv.initializeFrom(viewer);
			ribv.setFileName(name);
			ribv.render();
		} else {
			JOGLConfiguration.theLog.log(Level.WARNING,"Unable to open file");
			return;
		}
		viewer.render();
	}

	protected void savePSToFile() {
		SceneGraphComponent parent= null;
		JFileChooser fc = new JFileChooser(JOGLConfiguration.saveResourceDir);
		int result = fc.showSaveDialog(this);
		SceneGraphComponent sgc = null;
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			String name = file.getAbsolutePath();
			JOGLConfiguration.saveResourceDir = file.getAbsolutePath();
			file.delete();
			PSViewer psv = new PSViewer(name);
			psv.initializeFrom(viewer);
			psv.render(viewer.canvas.getWidth(), viewer.canvas.getHeight());
		} else {
			JOGLConfiguration.theLog.log(Level.WARNING,"Unable to open file");
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
			JOGLConfiguration.theLog.log(Level.INFO,"Beginning inspector");
		}
		else {
			fci.endInspection();
			repaint();
			viewer.setCameraPath(camPath);
			viewer.render();
			JOGLConfiguration.theLog.log(Level.INFO,"Ending inspector");
		}
	}
	
	// allow subclasses to customize the camera path inspector (hack!)
	public  FramedCurveInspector getCameraPathInspector()	{
		return new FramedCurveInspector(viewer, world);
	}
	
	public void update()	{
	}
	
	
	public void addViewer(Viewer v, String name)	{
		viewerList.add(v);
		if (mode == TABBED_PANE)	{
			tabbedPane.addTab(name, v.getViewingComponent());
			JOGLConfiguration.theLog.log(Level.INFO,"Adding viewer, now there are: "+tabbedPane.getTabCount());			
		}
	}
	
	public void removeViewer(Viewer v)	{
		if (mode == TABBED_PANE)	{
			tabbedPane.remove(viewer.getViewingComponent());
			JOGLConfiguration.theLog.log(Level.INFO,"Removing viewer, now there are: "+tabbedPane.getTabCount());		
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
    		String loadableScene = "de.jreality.worlds.TestSphereDrawing";
    		if (args != null && args.length > 0) {
    			//iv.loadWorld(args[0]);
    			loadableScene = args[0];
    		}
    		iv.initializeScene();
    		iv.loadScene(loadableScene);
    }
}
