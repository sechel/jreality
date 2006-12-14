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


package de.jreality.ui.viewerapp;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.Statement;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import de.jreality.scene.Viewer;
import de.jtem.beans.DimensionPanel;
import de.jtem.beans.InspectorPanel;

@SuppressWarnings("serial")
public class SunflowMenu extends JMenu {
  
  private static FileFilter[] fileFilters;
  
  static {
    fileFilters = new FileFilter[3];
    fileFilters[0] = new de.jreality.ui.viewerapp.FileFilter("PNG Image", "png");
    fileFilters[1] = new de.jreality.ui.viewerapp.FileFilter("TGA Image", "tga");
    fileFilters[2] = new de.jreality.ui.viewerapp.FileFilter("HDR Image", "hdr");
  }
  
  private JFrame settingsFrame;
  private Object renderOptions;
  private Object previewOptions;
  private DimensionPanel dimPanel;
  
  private ViewerApp va;
  
  public SunflowMenu(ViewerApp vapp) {
    super("Sunflow");
    va = vapp;
    settingsFrame = new JFrame("Sunflow Settings");
    JTabbedPane tabs = new JTabbedPane();
    
    EmptyBorder border = new EmptyBorder(10, 10, 10, 10);
    
    previewOptions = createRenderOptions(-2, 0);
    InspectorPanel renderSameSettings = new InspectorPanel(false);
    renderSameSettings.setBorder(border);
    renderSameSettings.setObject(previewOptions, Collections
        .singleton("nothing"));
    tabs.add("preview", renderSameSettings);
    
    renderOptions = createRenderOptions(0, 2);
    
    InspectorPanel renderSettings = new InspectorPanel(false);
    renderSettings.setBorder(border);
    renderSettings.setObject(renderOptions, Collections
        .singleton("nothing"));
    tabs.add("render", renderSettings);
    
    settingsFrame.add(tabs);
    settingsFrame.pack();
    
    Action previewAction = new AbstractAction("preview") {
      public void actionPerformed(ActionEvent arg0) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
          public Object run() {
            render(va.getViewer(), va.getViewer().getViewingComponentSize(), getPreviewOptions());
            return null;
          }
        });
      }
    };
    previewAction.putValue(
        Action.ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK)
    );
    add(previewAction);
    
    add(new AbstractAction("render") {
      public void actionPerformed(ActionEvent arg0) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
          public Object run() {
            renderAndSave(va.getViewer(), getRenderOptions());
            return null;
          }
        });
      }
    });
    
    add(new AbstractAction("settings") {
      public void actionPerformed(ActionEvent arg0) {
        showSettingsInspector();
      }
    });
  }
  
  private Object createRenderOptions(int aaMin, int aaMax) {
    try {
      Object renderOptions = Class.forName("de.jreality.sunflow.RenderOptions").newInstance();
      new Statement(renderOptions, "setAaMin", new Object[]{aaMin}).execute();
      new Statement(renderOptions, "setAaMax", new Object[]{aaMax}).execute();
      return renderOptions;
    } catch (Throwable e) {
      throw new RuntimeException("sunflow missing", e);
    }
  }
  
  protected void renderAndSave(Viewer viewer, Object opts) {
    if (dimPanel == null) {
      dimPanel = new DimensionPanel();
      dimPanel.setDimension(new Dimension(800,600));
      TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dimension");
      dimPanel.setBorder(title);
    }
    File file = FileLoaderDialog.selectTargetFile(null, dimPanel, false, fileFilters);
    final Dimension dim = dimPanel.getDimension();
    try {
      new Statement(Class.forName("de.jreality.sunflow.Sunflow"), "renderAndSave", new Object[]{viewer, opts, dim, file}).execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  protected void render(Viewer viewer, Dimension dim, Object opts) {
    try {
      new Statement(Class.forName("de.jreality.sunflow.Sunflow"), "render", new Object[]{viewer, dim, opts}).execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void showSettingsInspector() {
    settingsFrame.setVisible(true);
    settingsFrame.toFront();
  }
  
  public Object getRenderOptions() {
    return renderOptions;
  }
  
  public Object getPreviewOptions() {
    return previewOptions;
  }
  
}