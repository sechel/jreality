package de.jreality.ui.viewerapp.actions.file;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;
import de.jreality.writer.WriterVRML;

public class ExportVRML extends AbstractJrAction {

	private Viewer viewer;

	private boolean writeTextureFiles = false;
	private JComponent options;

	public ExportVRML(String name, Viewer viewer, Component parentComp) {
		super(name, parentComp);

		if (viewer == null)
			throw new IllegalArgumentException("Viewer is null!");
		this.viewer = viewer;

		setShortDescription("Export the current scene as VRML file");
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (options == null) options = createAccessory();
		
		File file = FileLoaderDialog.selectTargetFile(parentComp, options, "wrl", "VRML Files");
		if (file == null) return;  //dialog cancelled

		if (options == null) options = createAccessory();

		try {
//			WriterVRML.write(viewer.getSceneRoot(), new FileOutputStream(file));
			WriterVRML writer = new WriterVRML(new FileOutputStream(file));
			writer.setWritePath(file.getParent()+"/");
			writer.setWriteTextureFiles(writeTextureFiles);
			writer.write(viewer.getSceneRoot());
		} catch (Exception exc) {
			exc.printStackTrace();
		}			
	}
	
	
	JCheckBox checkbox;
	private JComponent createAccessory() {
		
		Box accessory = Box.createVerticalBox();
		accessory.add(Box.createVerticalGlue());
		checkbox = new JCheckBox(new AbstractAction("write texture files") {
			public void actionPerformed(ActionEvent e) {
				writeTextureFiles = checkbox.isSelected();
			}
		});
		accessory.add(checkbox);
		
		return accessory;
	}
}
