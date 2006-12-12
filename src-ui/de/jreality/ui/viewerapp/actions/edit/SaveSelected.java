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


package de.jreality.ui.viewerapp.actions.edit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.SelectionEvent;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.actions.AbstractSelectionListenerAction;
import de.jreality.writer.WriterJRS;


/**
 * Saves the selected SceneGraphComponent into a file 
 * (if no SceneGraphComponent is selected, this action is disabled).
 * 
 * @author msommer
 */
public class SaveSelected extends AbstractSelectionListenerAction {

  public SaveSelected(String name, SelectionManager sm, Component frame) {
    super(name, sm, frame);
    setShortDescription("Save selected SceneGraphComponent as a file");
    setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
  }

  public SaveSelected(String name, ViewerApp v) {
    this(name, v.getSelectionManager(), v.getFrame());
  }
  
  
  @Override
  public void actionPerformed(ActionEvent e) {
    File file = FileLoaderDialog.selectTargetFile(frame);
    if (file == null) return;
    if (!file.getName().endsWith(".jrs")) {
    	JOptionPane.showMessageDialog(frame, "can only safe .jrs files", "unsupported format", JOptionPane.ERROR_MESSAGE);
    	return;
    }
    try {
      FileWriter fw = new FileWriter(file);
      WriterJRS writer = new WriterJRS();
      writer.write(selection.getLastComponent(), fw);
      fw.close();
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(frame, "Save failed: "+ioe.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  
  @Override
  public boolean isEnabled(SelectionEvent e) {
    return e.componentSelected();
  }
  
}