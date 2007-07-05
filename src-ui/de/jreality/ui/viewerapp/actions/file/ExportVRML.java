package de.jreality.ui.viewerapp.actions.file;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.jreality.scene.Viewer;
import de.jreality.softviewer.PSRenderer;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;
import de.jreality.writer.WriterVRML;

public class ExportVRML extends AbstractJrAction {

	private Viewer viewer;


	public ExportVRML(String name, Viewer viewer, Component parentComp) {
		super(name, parentComp);

		if (viewer == null)
			throw new IllegalArgumentException("Viewer is null!");
		this.viewer = viewer;

		setShortDescription("Export the current scene as VRML file");
	}

//	public ExportPS(String name, ViewerApp v) {
//	this(name, v.getViewerSwitch(), v.getFrame());
//	}


	@Override
	public void actionPerformed(ActionEvent e) {
		File file = FileLoaderDialog.selectTargetFile(parentComp, "wrl", "VRML Files");
		if (file == null) return;  //dialog cancelled

		// try {
		Dimension d = viewer.getViewingComponentSize();
		PSRenderer rv;
		try {
			WriterVRML.write(viewer.getSceneRoot(), new FileOutputStream(file));
		} catch (FileNotFoundException exc) {
			exc.printStackTrace();
		} catch (IOException ev) {
			// TODO Auto-generated catch block
			ev.printStackTrace();
		}			
	}


}
