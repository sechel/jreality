/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.jreality.ui.viewerapp.actions.file;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.jreality.io.JrScene;
import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;
import de.jreality.writer.blender.BlenderConnection;
import de.jreality.writer.blender.WriterBlender;

/**
 * 
 * @author Stefan Sechelmann, Thilo Rörig
 * 
 */
public class ExportBlender extends AbstractJrAction {

	private static final long 
		serialVersionUID = 1L;
	private Viewer 
		viewer = null;
	private JFileChooser
		executableChooser = new JFileChooser();

	public ExportBlender(String name, Viewer viewer, Component parentComp) {
		super(name, parentComp);
		if (viewer == null) {
			throw new IllegalArgumentException("Viewer is null!");
		}
		this.viewer = viewer;
		setShortDescription("Save scene as a U3D file");
		
		executableChooser.setAcceptAllFileFilterUsed(true);
		executableChooser.setMultiSelectionEnabled(false);
		executableChooser.setDialogTitle("Select blender executable");
		executableChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		File file = FileLoaderDialog.selectTargetFile(parentComp, "blend", "Blender files");
		if (file == null) {
			return;
		}
		try {
			writeSceneToFile(file);
		} catch (IOException ioe) {
			Icon blenderIcon = new ImageIcon(getClass().getResource("blender.png"), "Blender Icon");
			String[] options = {"Choose Executable", "Cancel"};
			int result = JOptionPane.showOptionDialog(parentComp, "Blender executable not found.", "Export Error", YES_NO_CANCEL_OPTION, INFORMATION_MESSAGE, blenderIcon, options, null);
			if (result != 0) return;
			result = executableChooser.showOpenDialog(parentComp);
			if (result != JFileChooser.APPROVE_OPTION) return;
			File executable = executableChooser.getSelectedFile();
			if (executable.isDirectory()) { // mac application package
				executable = new File(executable + "/Contents/MacOS/blender");
			}
			BlenderConnection.setBlenderExecutable(executable);
			try {
				writeSceneToFile(file);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(parentComp, "Could not write blender file", "Error", ERROR_MESSAGE);
			}
		}
	}

	private void writeSceneToFile(File file) throws FileNotFoundException, IOException {
		WriterBlender writer = new WriterBlender();
		JrScene s = new JrScene(viewer.getSceneRoot());
		s.addPath("cameraPath", viewer.getCameraPath());
		FileOutputStream fos = new FileOutputStream(file);
		writer.writeScene(s, fos);
		fos.close();
	}

}