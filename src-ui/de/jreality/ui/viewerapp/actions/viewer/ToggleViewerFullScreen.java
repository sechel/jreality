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


package de.jreality.ui.viewerapp.actions.viewer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.actions.AbstractAction;


public class ToggleViewerFullScreen extends AbstractAction {

  boolean isFullscreen = false;
  private ViewerApp viewerApp;
  private JFrame fsf;  //full screen frame
  private JFrame frame;  //the viewerApp's frame
  
  private static HashMap <ViewerApp, ToggleViewerFullScreen> sharedInstances = new HashMap <ViewerApp, ToggleViewerFullScreen>();
  
  
  private ToggleViewerFullScreen(String name, ViewerApp viewerApp) {
    super(name);
    this.viewerApp = viewerApp;
    this.frame = viewerApp.getFrame();
    this.fsf = new JFrame("jReality Viewer");
    fsf.setUndecorated(true);
   
    putValue(SHORT_DESCRIPTION, "Toggle viewer full screen");
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, 0));
  }

  
  /**
   * Returns a shared instance of this action depending on the specified viewerApp
   * (i.e. there is a shared instance for each viewerApp). 
   * The action's name is overwritten by the specified name.
   * @param name name of the action
   * @param viewerApp the viewerApp displaying the viewer
   * @throws UnsupportedOperationException if viewerApp equals null
   * @return shared instance of ToggleViewerFullScreen with specified name
   */
  public static ToggleViewerFullScreen sharedInstance(String name, ViewerApp viewerApp) {
    if (viewerApp == null) 
      throw new UnsupportedOperationException("ViewerApp not allowed to be null!");
    
    ToggleViewerFullScreen sharedInstance = sharedInstances.get(viewerApp);
    if (sharedInstance == null) {
      sharedInstance = new ToggleViewerFullScreen(name, viewerApp);
      sharedInstances.put(viewerApp, sharedInstance);
      //add same action instance to full screen frame
      JMenuBar menu = new JMenuBar();
      JMenuItem item = new JMenuItem(sharedInstance(name, viewerApp));
      item.setVisible(false);
      menu.add(item);
      sharedInstance.fsf.setJMenuBar(menu);
    }
     
    sharedInstance.setName(name);
    return sharedInstance;
  }
  
  
  public void actionPerformed(ActionEvent e) {

    if (isFullscreen) {
      fsf.dispose();
      fsf.getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
      frame.getContentPane().removeAll();
      frame.getContentPane().add(viewerApp.getComponent());
      frame.validate();
      frame.setVisible(true);
      isFullscreen = false;
    } else {
      fsf.getContentPane().removeAll();
      fsf.getContentPane().add(viewerApp.getViewerSwitch().getViewingComponent());
      fsf.getGraphicsConfiguration().getDevice().setFullScreenWindow(fsf);
      fsf.validate();
      frame.setVisible(false);
      isFullscreen = true;
    }
    ((Component) viewerApp.getViewer().getViewingComponent()).requestFocusInWindow();
  }
  
}