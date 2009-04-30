package de.jreality.plugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.jreality.plugin.view.AlignedContent;
import de.jreality.plugin.view.Background;
import de.jreality.plugin.view.CameraStand;
import de.jreality.plugin.view.ContentAppearance;
import de.jreality.plugin.view.ContentLoader;
import de.jreality.plugin.view.ContentTools;
import de.jreality.plugin.view.DisplayOptions;
import de.jreality.plugin.view.InfoOverlayPlugin;
import de.jreality.plugin.view.Inspector;
import de.jreality.plugin.view.Lights;
import de.jreality.plugin.view.ManagedContent;
import de.jreality.plugin.view.Shell;
import de.jreality.plugin.view.View;
import de.jreality.plugin.view.ViewMenuBar;
import de.jreality.plugin.view.ViewPreferences;
import de.jreality.plugin.view.ViewToolBar;
import de.jreality.plugin.view.ViewerKeyListenerPlugin;
import de.jreality.plugin.view.ZoomTool;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.Viewer;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class PluginViewerApp {

	private SimpleController
		controller = new SimpleController();
	private ViewMenuBar viewMenuBar;
	private View view;
	private CameraStand cameraStand;
	private Lights lights;
	private Background background;
	private AlignedContent alignedContent;
	private ViewPreferences viewPreferences;
	private Inspector inspector;
	private Shell shell;
	private ContentAppearance contentAppearance;
	private ContentTools contentTools;
	private DisplayOptions displayOptions;
	private ContentLoader contentLoader;
	private ZoomTool zoomTool;
	private InfoOverlayPlugin infoOverlay;
	private ViewerKeyListenerPlugin viewerKeyListener;
	private SceneGraphComponent root;

	private static class ExitAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ExitAction() {
			putValue(AbstractAction.NAME, "Exit");
		}
		
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	
	public PluginViewerApp(SceneGraphNode contentNode) {
		if (contentNode != null) {
			if (!(contentNode instanceof Geometry)
					&& !(contentNode instanceof SceneGraphComponent)) {
				throw new IllegalArgumentException(
						"Only Geometry or SceneGraphComponent allowed!");
			}
			root = null;
			if (contentNode instanceof SceneGraphComponent) {
				root = (SceneGraphComponent)contentNode;
			} else {
				root = new SceneGraphComponent();
				root.setGeometry((Geometry)contentNode);
			}
		} else root = new SceneGraphComponent("empty root");
		
		view = new View();
		controller.registerPlugin(view);
		
		cameraStand = new CameraStand();
		controller.registerPlugin(cameraStand);
		
		lights = new Lights();
		controller.registerPlugin(lights);
		
		background = new Background();
		controller.registerPlugin(background);
		
		alignedContent = new AlignedContent();
		alignedContent.setContent(root);
		controller.registerPlugin(alignedContent);
		
		viewPreferences =  new ViewPreferences();
		controller.registerPlugin(viewPreferences);
		
		contentAppearance = new ContentAppearance();
		controller.registerPlugin(contentAppearance);
		
		contentTools = new ContentTools();
		controller.registerPlugin(contentTools);
		
		contentLoader = new ContentLoader();
		controller.registerPlugin(contentLoader);
		
		zoomTool = new ZoomTool();
		controller.registerPlugin(zoomTool);
		
		infoOverlay = new InfoOverlayPlugin();
		controller.registerPlugin(infoOverlay);
		
		viewerKeyListener = new ViewerKeyListenerPlugin();
		controller.registerPlugin(viewerKeyListener);
		
		controller.registerPlugin(new ViewToolBar());
		controller.registerPlugin(new ManagedContent());
		
		// set defaults
		setCreateMenu(true);
		setAttachBeanShell(true);
		setAttachNavigator(true);
	}

	public SimpleController getController() {
		return controller;
	}

	public void addAccessory(final Component c, final String title) {
		ShrinkPanelPlugin p = new ShrinkPanelPlugin() {
			
			{
				shrinkPanel.setLayout(new GridLayout());
				shrinkPanel.add(c);
			}
			
			@Override
			public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
				return View.class;
			}

			@Override
			public PluginInfo getPluginInfo() {
				if (title == null) {
					return new PluginInfo("No Title");
				} else {
					return new PluginInfo(title);
				}
				
			}
		};
		controller.registerPlugin(p);
	}

	public void addAccessory(Component c, String title, boolean scrolling) {
		addAccessory(c, title);
	}

	public void display() {
		controller.startup();
	}


	public Shell getBeanShell() {
		return shell;
	}

	public AlignedContent getContent() {
		return alignedContent;
	}

	public Viewer getCurrentViewer() {
		return view.getViewer().getCurrentViewer();
	}


	public ViewMenuBar getMenu() {
		return viewMenuBar;
	}

	public Inspector getNavigator() {
		return inspector;
	}


	public SceneGraphComponent getSceneRoot() {
		return view.getSceneRoot();
	}


	public ToolSystem getToolSystem() {
		return view.getToolSystem();
	}

	public Viewer getViewer() {
		return view.getViewer().getCurrentViewer();
	}

	public ViewerSwitch getViewerSwitch() {
		return view.getViewer();
	}

	public Component getViewingComponent() {
		return view.getViewer().getViewingComponent();
	}

	public Lights getLights() {
		return lights;
	}

	public AlignedContent getAlignedContent() {
		return alignedContent;
	}

	public ViewerKeyListenerPlugin getViewerKeyListener() {
		return viewerKeyListener;
	}

	public boolean isAttachBeanShell() {
		return controller.isActive(shell);
	}

	public boolean isAttachNavigator() {
		return controller.isActive(inspector);
	}

	public boolean isCreateMenu() {
		return controller.isActive(viewMenuBar);
	}

	public boolean isExternalBeanShell() {
		return false;
	}


	public boolean isIncludeMenu() {
		return isCreateMenu();
	}

	public boolean isShowMenu() {
		return isCreateMenu();
	}

	public void removeAccessory(Component c) {
		
	}

	public void setAttachBeanShell(boolean b) {
		if (b)	{
			shell = new Shell();
			controller.registerPlugin(shell);					
		}
	}

	public void setAttachNavigator(boolean b) {
		if (b)	{
			inspector = new Inspector();
			controller.registerPlugin(inspector);				
		}
	}

	public void setBackgroundColor(Color... colors) {

	}

	public void setDisplayOptions(boolean b)	{
		if (b) {
			displayOptions = new DisplayOptions();
			controller.registerPlugin(displayOptions);
						
		}
	}
	public void setCreateMenu(boolean b) {
		if (b) {
			viewMenuBar = new ViewMenuBar();
			viewMenuBar.addMenuSeparator(PluginViewerApp.class, 19.0, "File");
			viewMenuBar.addMenuItem(PluginViewerApp.class, 20.0, new ExitAction(), "File");
			controller.registerPlugin(viewMenuBar);
		}
	}

	public void setInfoOverlay(boolean b)	{
		infoOverlay.getInfoOverlay().setVisible(b);
	}

	@Deprecated
	public void setFirstAccessory(Component c) {
		
	}


	@Deprecated
	public void update() {
		
	}

	public static void display(SceneGraphNode sceneGraphNode) {
		if (sceneGraphNode == null) sceneGraphNode = new SceneGraphComponent("null");
		new PluginViewerApp(sceneGraphNode).display();
	}

	/**
	 * @deprecated
	 * @param b
	 */
	public void setExternalBeanShell(boolean b) {
	}

	/**
	 * @deprecated
	 * @param b
	 */
	public void setExternalNavigator(boolean b) {
	}
	
	
}
