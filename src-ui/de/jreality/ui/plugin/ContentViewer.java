package de.jreality.ui.plugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.vr.plugin.Sky;
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
	private Sky sky;
	private ContentTools contentTools;
	
	public ContentViewer() {
		controller = new SimpleController();
		
		viewMenuBar = new ViewMenuBar();
		viewMenuBar.addMenuSeparator(ContentViewer.class, 19.0, "File");
		viewMenuBar.addMenuItem(ContentViewer.class, 20.0, new ExitAction(), "File");
		
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
		
		sky = new Sky();
		controller.registerPlugin(sky);
		
		contentTools = new ContentTools();
		controller.registerPlugin(contentTools);
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

	public Sky getSky() {
		return sky;
	}

	public ContentTools getContentTools() {
		return contentTools;
	}
	
	private static class ExitAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ExitAction() {
			putValue(AbstractAction.NAME, "Exit");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		ContentViewer contentViewer = new ContentViewer();
		contentViewer.registerPlugin(new ContentLoader());
		contentViewer.startup();
	}

}
