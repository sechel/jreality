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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.reader.Readers;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.toolsystem.ToolSystemViewer;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;
import de.jreality.util.CameraUtility;
import de.jreality.util.PickUtility;


/**
 * Loads one or several files into the scene and optionally merges indexed face & line sets
 * (adds the files as children to the selection managers default selection, 
 * which is usually the scene node).
 * 
 * @author msommer
 */
public class LoadFile extends AbstractJrAction {


  private SceneGraphComponent parentNode;
  private ToolSystemViewer viewer;
  
  private JComponent options; 
  private JCheckBox mergeLineSets;
  private JCheckBox mergeFaceSets;
  

  public LoadFile(String name, SceneGraphComponent parentNode, ToolSystemViewer viewer, Component parentComp) {
    super(name, parentComp);
    this.parentNode = parentNode;
    this.viewer = viewer;

    setShortDescription("Load one or more files");
    setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
  }

  public LoadFile(String name, SceneGraphComponent parentNode, ToolSystemViewer viewer) {
	  this(name, parentNode, viewer, null);
  }
  
  public LoadFile(String name, SceneGraphComponent parentNode, Component parentComp) {
	  this(name, parentNode, null, parentComp);
  }
  
  public LoadFile(String name, SceneGraphComponent parentNode) {
	  this(name, parentNode, null, null);
  }
  
//  public LoadFile(String name, ViewerApp v) {
//	  this(name, 
//			  v.getSelectionManager().getDefaultSelection().getLastComponent(), 
//			  v.getViewer(), 
//			  v.getFrame());
//  }
  
  
  @Override
  public void actionPerformed(ActionEvent e) {

    if (options == null) options = createAccessory();
    mergeLineSets.setSelected(false);
    mergeFaceSets.setSelected(false);
    
    File[] files = FileLoaderDialog.loadFiles(parentComp, options);
    if (files == null) return;  //dialog cancelled
    
    for (int i = 0; i < files.length; i++) {
      try {
        SceneGraphComponent sgc = Readers.read(files[i]);
        if (mergeFaceSets.isSelected()) 
          sgc = IndexedFaceSetUtility.mergeIndexedFaceSets(sgc);
        if (mergeLineSets.isSelected()) 
          sgc = IndexedFaceSetUtility.mergeIndexedLineSets(sgc);
        sgc.setName(files[i].getName());
        System.out.println("READ finished.");
        parentNode.addChild(sgc);
        
        PickUtility.assignFaceAABBTrees(sgc);
        
        if (viewer != null) {
        	CameraUtility.encompass(viewer.getAvatarPath(),
        			viewer.getEmptyPickPath(),
        			viewer.getCameraPath(),
        			1.75, viewer.getSignature());
        }
      } 
      catch (IOException ioe) {
        JOptionPane.showMessageDialog(parentComp, "Failed to load file: "+ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  
  private JComponent createAccessory() {
    Box box = Box.createVerticalBox();
    TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Options");
    box.setBorder(title);

    mergeLineSets = new JCheckBox("merge IndexedLineSets");
    mergeFaceSets = new JCheckBox("merge IndexedFaceSets");
    box.add(mergeLineSets);
    box.add(mergeFaceSets);
    
    return box;
  }

}