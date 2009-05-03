package de.jreality.plugin;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import de.jreality.plugin.view.AlignedContent;
import de.jreality.plugin.view.Background;
import de.jreality.plugin.view.CameraStand;
import de.jreality.plugin.view.ContentAppearance;
import de.jreality.plugin.view.ContentGUI;
import de.jreality.plugin.view.ContentLoader;
import de.jreality.plugin.view.ContentTools;
import de.jreality.plugin.view.DisplayOptions;
import de.jreality.plugin.view.Export;
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
import de.jreality.scene.SceneGraphComponent;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class ContentViewer {
	private SimpleController controller;
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
	
	public ContentViewer() {
		controller = new SimpleController(new File("ContentViewer.jrw"));
		
		view = new View();
		controller.registerPlugin(view);
		
		cameraStand = new CameraStand();
		controller.registerPlugin(cameraStand);
		
		lights = new Lights();
		controller.registerPlugin(lights);
		
		background = new Background();
		controller.registerPlugin(background);
		
		viewMenuBar = new ViewMenuBar();
		viewMenuBar.addMenuSeparator(ContentViewer.class, 19.0, "File");
		viewMenuBar.addMenuItem(ContentViewer.class, 20.0, new ExitAction(), "File");
		controller.registerPlugin(viewMenuBar);
		
		alignedContent = new AlignedContent();
		controller.registerPlugin(alignedContent);
		
		viewPreferences =  new ViewPreferences();
		controller.registerPlugin(viewPreferences);
		
		inspector = new Inspector();
		controller.registerPlugin(inspector);
		
		shell = new Shell();
		controller.registerPlugin(shell);
		
		contentAppearance = new ContentAppearance();
		controller.registerPlugin(contentAppearance);
		
		contentTools = new ContentTools();
		controller.registerPlugin(contentTools);
		
		displayOptions = new DisplayOptions();
		controller.registerPlugin(displayOptions);
		
		controller.registerPlugin(new ViewToolBar());
		controller.registerPlugin(new ManagedContent());
	}

	public void registerPlugin(Plugin plugin) {
		controller.registerPlugin(plugin);
	}
	
	public void startup() {
		controller.startup();
	}
	
	public void setContent(SceneGraphComponent content) {
		alignedContent.setContent(content);
	}
	
	public void contentChanged() {
		alignedContent.contentChanged();
	}
	
	public ViewMenuBar getViewMenuBar() {
		return viewMenuBar;
	}

	public View getView() {
		return view;
	}

	public CameraStand getCameraStand() {
		return cameraStand;
	}

	public Lights getLights() {
		return lights;
	}

	public Background getBackground() {
		return background;
	}

	public AlignedContent getAlignedContent() {
		return alignedContent;
	}

	public ViewPreferences getViewPreferences() {
		return viewPreferences;
	}

	public Shell getShell() {
		return shell;
	}

	public ContentAppearance getContentAppearance() {
		return contentAppearance;
	}

	public ContentTools getContentTools() {
		return contentTools;
	}

	public DisplayOptions getDisplayPanel() {
		return displayOptions;
	}

	private static class ExitAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ExitAction() {
			putValue(AbstractAction.NAME, "Exit");
		}
		
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		ContentViewer contentViewer = new ContentViewer();
		contentViewer.registerPlugin(new Export());
		contentViewer.registerPlugin(new ContentLoader());
		contentViewer.registerPlugin(new ZoomTool());
		contentViewer.registerPlugin(new ViewerKeyListenerPlugin());
		contentViewer.registerPlugin(new InfoOverlayPlugin());
		contentViewer.registerPlugin(new ContentGUI());
		contentViewer.startup();
	}
}
