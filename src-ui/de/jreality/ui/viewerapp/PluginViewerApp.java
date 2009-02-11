package de.jreality.ui.viewerapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.Viewer;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.ui.plugin.AlignedContent;
import de.jreality.ui.plugin.Background;
import de.jreality.ui.plugin.CameraStand;
import de.jreality.ui.plugin.ContentAppearance;
import de.jreality.ui.plugin.ContentTools;
import de.jreality.ui.plugin.ContentViewer;
import de.jreality.ui.plugin.DisplayOptions;
import de.jreality.ui.plugin.Inspector;
import de.jreality.ui.plugin.Lights;
import de.jreality.ui.plugin.Shell;
import de.jreality.ui.plugin.View;
import de.jreality.ui.plugin.ViewMenuBar;
import de.jreality.ui.plugin.ViewPreferences;
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
		}
		SceneGraphComponent root = null;
		if (contentNode instanceof SceneGraphComponent) {
			root = (SceneGraphComponent)contentNode;
		} else {
			root = new SceneGraphComponent();
			root.setGeometry((Geometry)contentNode);
		}
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
		
		setCreateMenu(true);
		setAttachBeanShell(true);
		setAttachNavigator(true);
	}

	public void addAccessory(final Component c) {
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
				return new PluginInfo(c.getName());
			}
		};
		controller.registerPlugin(p);
	}

	public void addAccessory(Component c, String title) {
		c.setName(title);
		addAccessory(c);
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
			viewMenuBar.addMenuSeparator(ContentViewer.class, 19.0, "File");
			viewMenuBar.addMenuItem(ContentViewer.class, 20.0, new ExitAction(), "File");
			controller.registerPlugin(viewMenuBar);
		}
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
	
	
}
