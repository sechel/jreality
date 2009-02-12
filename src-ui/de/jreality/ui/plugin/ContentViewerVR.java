package de.jreality.ui.plugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.plugin.view.AlignedContent;
import de.jreality.ui.plugin.view.Background;
import de.jreality.ui.plugin.view.CameraStand;
import de.jreality.ui.plugin.view.ContentAppearance;
import de.jreality.ui.plugin.view.ContentLoader;
import de.jreality.ui.plugin.view.ContentTools;
import de.jreality.ui.plugin.view.Inspector;
import de.jreality.ui.plugin.view.Lights;
import de.jreality.ui.plugin.view.Shell;
import de.jreality.ui.plugin.view.View;
import de.jreality.ui.plugin.view.ViewMenuBar;
import de.jreality.ui.plugin.view.ViewPreferences;
import de.jreality.ui.plugin.vr.Avatar;
import de.jreality.ui.plugin.vr.Sky;
import de.jreality.ui.plugin.vr.Terrain;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class ContentViewerVR {
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
	
	private Avatar avatar;
	private Terrain terrain;
	
//	private HeadUpDisplay headUpDisplay;

//	private Audio audio;
//	private ContentSound contentSound;
	
	public ContentViewerVR(boolean withAudio) {
		controller = new SimpleController();
		
		viewMenuBar = new ViewMenuBar();
		viewMenuBar.addMenuSeparator(ContentViewerVR.class, 19.0, "File");
		viewMenuBar.addMenuItem(ContentViewerVR.class, 20.0, new ExitAction(), "File");
		
		view = new View();
		controller.registerPlugin(view);
		
		cameraStand = new CameraStand();
		controller.registerPlugin(cameraStand);
		
		lights = new Lights();
		controller.registerPlugin(lights);
		
		background = new Background();
		controller.registerPlugin(background);
		
		viewMenuBar = new ViewMenuBar();
		viewMenuBar.addMenuSeparator(ContentViewerVR.class, 19.0, "File");
		viewMenuBar.addMenuItem(ContentViewerVR.class, 20.0, new ExitAction(), "File");
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

			avatar = new Avatar();
			avatar.setShowPanel(false);
			controller.registerPlugin(avatar);
		
			terrain = new Terrain();
			controller.registerPlugin(terrain);
		
//		if (withAudio) {
//			audio = new Audio();
//			controller.registerPlugin(audio);
//			contentSound = new ContentSound();
//			controller.registerPlugin(contentSound);
//		}
		
		contentTools = new ContentTools();
		controller.registerPlugin(contentTools);
		
//		headUpDisplay = new HeadUpDisplay();
//		controller.registerPlugin(headUpDisplay);
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

	public Avatar getAvatar() {
		return avatar;
	}

	public Terrain getTerrain() {
		return terrain;
	}
//
//	public Audio getAudio() {
//		return audio;
//	}
	
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
		ContentViewerVR contentViewer = new ContentViewerVR(true);
		contentViewer.registerPlugin(new ContentLoader());
		contentViewer.startup();
	}

	public static View remoteMain(String[] args) {
		ContentViewerVR contentViewer = new ContentViewerVR(true);
		contentViewer.registerPlugin(new ContentLoader());
		contentViewer.startup();
		return contentViewer.view;
	}
	
	
//	public ContentSound getContentSound() {
//		return contentSound;
//	}

//	public HeadUpDisplay getHeadUpDisplay() {
//		return headUpDisplay;
//	}

}
