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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.reader.Readers;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.actions.AbstractAction;
import de.jreality.util.CameraUtility;
import de.jreality.util.PickUtility;


public class LoadFileMerged extends AbstractAction {


  private ViewerApp viewerApp;


public LoadFileMerged(String name, SelectionManager sm, ViewerApp viewerApp, Component frame) {
    super(name, sm, frame);
    this.viewerApp = viewerApp;
    putValue(SHORT_DESCRIPTION, "Load one or more files and merge IndexedFaceSets");
  }

  
  public void actionPerformed(ActionEvent e) {
  
    File[] files = FileLoaderDialog.loadFiles(frame);
    for (int i = 0; i < files.length; i++) {
      try {
        SceneGraphComponent sgc = Readers.read(files[i]);
        sgc = IndexedFaceSetUtility.mergeIndexedFaceSets(sgc);
        sgc = IndexedFaceSetUtility.mergeIndexedLineSets(sgc);
        sgc.setName(files[i].getName());
        System.out.println("READ finished.");
        selection.getLastComponent().addChild(sgc);
        
        PickUtility.assignFaceAABBTrees(sgc);

        CameraUtility.encompass(viewerApp.getViewer().getAvatarPath(),
				viewerApp.getViewer().getEmptyPickPath(),
				viewerApp.getViewer().getCameraPath(),
				1.75, viewerApp.getViewer().getSignature());

      } 
      catch (IOException ioe) {
        JOptionPane.showMessageDialog(frame, "Failed to load file: "+ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

}