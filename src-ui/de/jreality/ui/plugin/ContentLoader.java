package de.jreality.ui.plugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.reader.Readers;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.data.Attribute;
import de.jreality.ui.plugin.image.ImageHook;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.util.Input;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.flavor.UIFlavor;

public class ContentLoader extends Plugin implements UIFlavor {

	private AlignedContent alignedContent;

	private final JCheckBox smoothNormalsCheckBox = new JCheckBox("smooth normals");
	private final JCheckBox removeAppsCheckBox = new JCheckBox("ignore appearances");
	private JMenuItem menuItem;
	private JFileChooser chooser;
	private Component parent;

	@SuppressWarnings("serial")

	public ContentLoader() {
		Box checkBoxPanel = new Box(BoxLayout.Y_AXIS);
		JCheckBox smoothNormalsCheckBox = new JCheckBox("smooth normals");
		JCheckBox removeAppsCheckBox = new JCheckBox("ignore appearances");

		checkBoxPanel.add(smoothNormalsCheckBox);
		checkBoxPanel.add(removeAppsCheckBox);

		chooser = FileLoaderDialog.createFileChooser();
		chooser.setAccessory(checkBoxPanel);
		chooser.setMultiSelectionEnabled(false);

		Action action = new AbstractAction("Load Content") {

			public void actionPerformed(ActionEvent e) {
				loadFile();
			}
		};
		menuItem = new JMenuItem(action);
	}

	public void install(View sceneView, AlignedContent alignedContent) {
		parent = sceneView.getViewer().getViewingComponent();
		this.alignedContent = alignedContent;
	}

	public JMenuItem getMenuItem() {
		return menuItem;
	}

	private void loadFile() {
		File file = null;
		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
		}
		if (file != null) {
			try {
				SceneGraphComponent read = Readers.read(Input.getInput(file));
				SceneGraphComponent tempRoot = new SceneGraphComponent();
				tempRoot.addChild(read);
				tempRoot.accept(new SceneGraphVisitor() {
					@Override
					public void visit(SceneGraphComponent c) {
						if (removeAppsCheckBox.isSelected() && c.getAppearance() != null) c.setAppearance(null); 
						c.childrenWriteAccept(this, false, false, false, false, true,
								true);
					}
					@Override
					public void visit(IndexedFaceSet i) {
						if (i.getFaceAttributes(Attribute.NORMALS) == null) IndexedFaceSetUtility.calculateAndSetFaceNormals(i);
						if (i.getVertexAttributes(Attribute.NORMALS) == null) IndexedFaceSetUtility.calculateAndSetVertexNormals(i);
						if (smoothNormalsCheckBox.isSelected()) IndexedFaceSetUtility.assignSmoothVertexNormals(i, -1);
					}
				});
				tempRoot.removeChild(read);
				alignedContent.setContent(read);
			} catch (IOException e) {
				e.printStackTrace();
			}
			smoothNormalsCheckBox.setSelected(false);
			removeAppsCheckBox.setSelected(false);
		}
	}

	private ViewMenuBar viewerMenuAggregator;

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Content Loader";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("diskette.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {

		View viewPlugin = c.getPlugin(View.class);
		AlignedContent contentPlugin = c.getPlugin(AlignedContent.class);

		install(
				viewPlugin,
				contentPlugin
		);

		viewerMenuAggregator = c.getPlugin(ViewMenuBar.class);
		viewerMenuAggregator.addMenuItem(getClass(), 0.0, menuItem, "File");
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		viewerMenuAggregator.removeMenuAll(getClass()); 
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		setCurrentDirectory(c.getProperty(getClass(), "currentDirectory", getCurrentDirectory()));
		super.restoreStates(c);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "currentDirectory", getCurrentDirectory());
		super.storeStates(c);
	}

	public String getCurrentDirectory() {
		return chooser.getCurrentDirectory().getAbsolutePath();
	}
	
 	public void setCurrentDirectory(String directory) {
		File dir = new File(directory);
		if (dir.exists() && dir.isDirectory()) {
			chooser.setCurrentDirectory(dir);
		} else {
			System.out.println(
					"failed to restore ContentLoader directory "+directory
			);
		}
	}
 	
 	public void mainUIChanged(String uiClass) {
 		SwingUtilities.updateComponentTreeUI(chooser);
 	}
 	
}

