package de.jreality.ui.sceneview;

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

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.reader.Readers;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.data.Attribute;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.util.Input;

public class SceneContentLoader {

	private SceneContentManager sceneContentManager;
	private final JCheckBox smoothNormalsCheckBox = new JCheckBox("smooth normals");
	private final JCheckBox removeAppsCheckBox = new JCheckBox("ignore appearances");
	private JMenuItem menuItem;
	private JFileChooser chooser;
	private Component parent;

	@SuppressWarnings("serial")
	
	public SceneContentLoader(Component parent) {
		this.parent = parent;
		Box checkBoxPanel = new Box(BoxLayout.Y_AXIS);
		JCheckBox smoothNormalsCheckBox = new JCheckBox("smooth normals");
		JCheckBox removeAppsCheckBox = new JCheckBox("ignore appearances");
		
		checkBoxPanel.add(smoothNormalsCheckBox);
		checkBoxPanel.add(removeAppsCheckBox);
		
		chooser = FileLoaderDialog.createFileChooser();
		chooser.setAccessory(checkBoxPanel);
	    chooser.setMultiSelectionEnabled(false);
		
		Action action = new AbstractAction("Load Content") {

			@Override
			public void actionPerformed(ActionEvent e) {
				loadFile();
			}
		};
		menuItem = new JMenuItem(action);
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
					public void visit(SceneGraphComponent c) {
						if (removeAppsCheckBox.isSelected() && c.getAppearance() != null) c.setAppearance(null); 
						c.childrenWriteAccept(this, false, false, false, false, true,
								true);
					}
					public void visit(IndexedFaceSet i) {
						if (i.getFaceAttributes(Attribute.NORMALS) == null) GeometryUtility.calculateAndSetFaceNormals(i);
						if (i.getVertexAttributes(Attribute.NORMALS) == null) GeometryUtility.calculateAndSetVertexNormals(i);
						if (smoothNormalsCheckBox.isSelected()) IndexedFaceSetUtility.assignSmoothVertexNormals(i, -1);
					}
				});
				tempRoot.removeChild(read);
				getSceneContentManager().setContent(read);
			} catch (IOException e) {
				e.printStackTrace();
			}
			smoothNormalsCheckBox.setSelected(false);
			removeAppsCheckBox.setSelected(false);
		}
	}

	public void install(SceneContentManager sceneContentManager) {
		this.sceneContentManager = sceneContentManager;
	}

	private SceneContentManager getSceneContentManager() {
		return sceneContentManager;
	}
}
