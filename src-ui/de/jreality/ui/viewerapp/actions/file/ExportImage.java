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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.opengl.util.FileUtil;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.ui.viewerapp.actions.AbstractAction;
import de.jtem.beans.DimensionDialog;


public class ExportImage extends AbstractAction {

	private static final long serialVersionUID = 5793099900216754633L;
	private Viewer viewer;


	public ExportImage(String name, Viewer viewer, Frame frame) {
		super(name);
		this.frame = frame;
		putValue(SHORT_DESCRIPTION, "Export image file");

		if (viewer == null) 
			throw new IllegalArgumentException("Viewer is null!");
		this.viewer = viewer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		File file = FileLoaderDialog.selectTargetFile(frame, null, "image files");
		if (file == null) return;
		// Hack
		Viewer realViewer = ((ViewerSwitch)viewer).getCurrentViewer();
		de.jreality.jogl.Viewer joglViewer = (de.jreality.jogl.Viewer) realViewer;
		
		Dimension d = joglViewer.getViewingComponentSize();

		Dimension dim = DimensionDialog.selectDimension(d,frame);
		if (dim == null) return;
		joglViewer.renderOffscreen(dim.width, dim.height,file);
	}
	
	@Override
	public boolean isEnabled() {
		Viewer realViewer = ((ViewerSwitch)viewer).getCurrentViewer();
		return realViewer instanceof de.jreality.jogl.Viewer;
	}
}