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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

import com.sun.opengl.util.FileUtil;

import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.FileLoaderDialog;
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
    
    // Hack
    Viewer realViewer = ((ViewerSwitch)viewer).getCurrentViewer();
    de.jreality.jogl.Viewer joglViewer = (de.jreality.jogl.Viewer) realViewer;
    Dimension d = joglViewer.getViewingComponentSize();
    Dimension dim = DimensionDialog.selectDimension(d,frame);
    if (dim == null) return;
    
    File file = FileLoaderDialog.selectTargetFile(frame, createFileFilters());
    if (file == null) return;  //dialog cancelled 
    if (FileUtil.getFileSuffix(file) == null) {  //no extension specified
      System.err.println("Please specify a valid file extension.\n" +
      "Export aborted.");
      return;
    }
    
    BufferedImage img = joglViewer.renderOffscreen(4*dim.width, 4*dim.height);
    BufferedImage img2 = new BufferedImage(dim.width, dim.height,BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = (Graphics2D) img2.getGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    img2.getGraphics().drawImage(
        img.getScaledInstance(
            dim.width,
            dim.height,
            BufferedImage.SCALE_SMOOTH
        ),
        0,
        0,
        null
    );
    de.jreality.jogl.JOGLRenderer.writeBufferedImage(file,img2);
  }
  
  
  @Override
  public boolean isEnabled() {
    Viewer realViewer = ((ViewerSwitch)viewer).getCurrentViewer();
    return realViewer instanceof de.jreality.jogl.Viewer;
  }
  
  
  private FileFilter[] createFileFilters() {
    
    String writerFormats[] = ImageIO.getWriterFormatNames();
    //remove duplicate entries (ignore case)
    Set<String> s = new TreeSet<String>();  //ordered set
    for (int i = 0; i < writerFormats.length; i++)
      s.add(writerFormats[i].toLowerCase());
    s.add("tif");
    s.add("tiff");
    final String[] formats = new String[s.size()];
    s.toArray(formats);
    
    //TODO: tif=tiff, jpg=jpeg
    
    FileFilter[] ff = new FileFilter[formats.length];
    for (int i = 0; i < ff.length; i++) {
      final int ind = i;
      ff[i] = new FileFilter(){
        public boolean accept(File f) {
          return (f.isDirectory() || 
              f.getName().endsWith("."+formats[ind]) || 
              f.getName().endsWith("."+formats[ind].toUpperCase()));
        }
        public String getDescription() {
          return formats[ind].toUpperCase()+" Image";
        }
      };
    }
    
    return ff;
  }
  
}