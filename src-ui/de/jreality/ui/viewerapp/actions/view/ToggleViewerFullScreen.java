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


package de.jreality.ui.viewerapp.actions.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;


/**
 * Toggles full screen of the ViewerApp's viewer component (where the scene is displayed).<br>
 * There is only one instance of this action.
 * 
 * @author msommer
 */
public class ToggleViewerFullScreen extends AbstractJrAction {

  private boolean isFullscreen = false;
  private boolean showMenu = false;
  
  private ViewerApp viewerApp;
  private JFrame fsf;  //full screen frame
  private JFrame frame;  //the viewerApp's frame
  private JMenuBar menuBar;
  
  private static HashMap <ViewerApp, ToggleViewerFullScreen> sharedInstances = new HashMap <ViewerApp, ToggleViewerFullScreen>();
  
  
  private ToggleViewerFullScreen(String name, ViewerApp viewerApp) {
    super(name);
    this.viewerApp = viewerApp;
    this.frame = viewerApp.getFrame();
    this.fsf = new JFrame("jReality Viewer");
    fsf.setUndecorated(true);
   
    setShortDescription("Toggle viewer full screen");
    setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
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
    }
     
    sharedInstance.setName(name);
    return sharedInstance;
  }
  
  
  @Override
  public void actionPerformed(ActionEvent e) {

    menuBar = frame.getJMenuBar();
    
    if (isFullscreen) {  //exit full screen
      frame.getContentPane().removeAll();
      frame.getContentPane().add(viewerApp.getComponent());
      if (showMenu) showMenuBar(true);  //restore menu state
      frame.setJMenuBar(menuBar);
      frame.validate();
      frame.setVisible(true);
      fsf.dispose();
      fsf.getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
      isFullscreen = false;
    } else {  //switch to full screen
      fsf.getContentPane().removeAll();
      fsf.getContentPane().add(viewerApp.getViewerSwitch().getViewingComponent());
      showMenu = menuBar.getMenu(0).isVisible();  //remember menu state
      if (showMenu) showMenuBar(false);  //hide menu bar
      fsf.setJMenuBar(menuBar);
      fsf.validate();
      fsf.getGraphicsConfiguration().getDevice().setFullScreenWindow(fsf);
      frame.setVisible(false);
      isFullscreen = true;
    }
    ((Component) viewerApp.getViewer().getViewingComponent()).requestFocusInWindow();
  }
  
  
  private void showMenuBar(boolean b) {
    JMenu menu;
    for (int i = 0; i < menuBar.getComponentCount(); i++) {
      menu = menuBar.getMenu(i);
      menu.setVisible(b);
    }
  }
}