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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.jreality.io.JrScene;
import de.jreality.reader.ReaderJRS;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.actions.AbstractAction;
import de.jreality.util.Input;


public class LoadScene extends AbstractAction {

  private ViewerApp viewerApp;
  

  public LoadScene(String name, ViewerApp viewerApp, Frame frame) {
    super(name);
    this.frame = frame;
    putValue(SHORT_DESCRIPTION, "Open saved scene");
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
    
    if (viewerApp == null) 
      throw new IllegalArgumentException("ViewerApp is null!");
    
    this.viewerApp = viewerApp;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    File[] fs = FileLoaderDialog.loadFiles(frame);
    if (fs == null || fs.length == 0) return;
    File f = fs[0];
    JrScene scene = null;
    try {
      ReaderJRS r = new ReaderJRS();
      r.setInput(new Input(f));
      scene = r.getScene();
      ViewerApp v = new ViewerApp(scene);
      v.setAttachNavigator(viewerApp.isAttachNavigator());
      v.setAttachBeanShell(viewerApp.isAttachBeanShell());
      v.update();
      viewerApp.dispose();
      v.display();
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(frame, "Load failed: "+ioe.getMessage());
    }
  }

}