package de.jreality.ui.viewerapp.actions.file;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JComponent;

import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;
import de.jreality.writer.WriterSTL;

public class ExportSTL extends AbstractJrAction {

	private Viewer viewer;

	private JComponent options;

	public ExportSTL(String name, Viewer viewer, Component parentComp) {
		super(name, parentComp);

		if (viewer == null)
			throw new IllegalArgumentException("Viewer is null!");
		this.viewer = viewer;

		setShortDescription("Export the current scene as STL file");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		File file = FileLoaderDialog.selectTargetFile(parentComp, options, "stl", "STL Files");
		if (file == null) return;  //dialog cancelled

		try {
//			WriterVRML.write(viewer.getSceneRoot(), new FileOutputStream(file));
			WriterSTL.write(viewer.getSceneRoot(), new FileOutputStream(file));
		} catch (Exception exc) {
			exc.printStackTrace();
		}			
	}
}
