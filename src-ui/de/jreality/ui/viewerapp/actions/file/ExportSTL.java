package de.jreality.ui.viewerapp.actions.file;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import de.jreality.scene.Viewer;
import de.jreality.softviewer.PSRenderer;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;
import de.jreality.writer.WriterSTL;
import de.jreality.writer.WriterVRML;
import de.jtem.beans.DimensionPanel;

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

//	public ExportPS(String name, ViewerApp v) {
//	this(name, v.getViewerSwitch(), v.getFrame());
//	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		File file = FileLoaderDialog.selectTargetFile(parentComp, options, "stl", "STL Files");
		if (file == null) return;  //dialog cancelled

		try {
//			WriterVRML.write(viewer.getSceneRoot(), new FileOutputStream(file));
			WriterSTL.write(viewer.getSceneRoot(), new FileOutputStream(file));
		} catch (FileNotFoundException exc) {
			exc.printStackTrace();
		} catch (IOException ev) {
			// TODO Auto-generated catch block
			ev.printStackTrace();
		}			
	}
}
