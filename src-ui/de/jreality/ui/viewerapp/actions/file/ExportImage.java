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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import de.jreality.scene.Viewer;
import de.jreality.softviewer.SoftViewer;
import de.jreality.ui.viewerapp.FileFilter;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.ui.viewerapp.actions.AbstractJrAction;
import de.jtem.beans.DimensionPanel;


/**
 * Exports the scene displayed in a viewer as an image.
 * 
 * @author pinkall
 */
public class ExportImage extends AbstractJrAction {
  
  private Viewer viewer;
  private DimensionPanel dimPanel;
  
  
  public ExportImage(String name, Viewer viewer, Frame frame) {
    super(name);
    this.frame = frame;
    setShortDescription("Export image file");
    
    if (viewer == null) 
      throw new IllegalArgumentException("Viewer is null!");
    this.viewer = viewer;
  }
  
  public ExportImage(String name, ViewerApp v) {
    this(name, v.getViewerSwitch(), v.getFrame());
  }
  
  
  @Override
  public void actionPerformed(ActionEvent e) {
    
    if (dimPanel == null) {
      dimPanel = new DimensionPanel();
      TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dimension");
      dimPanel.setBorder(title);
    }
    
    // Hack
    Viewer realViewer = ((ViewerSwitch)viewer).getCurrentViewer();
//    de.jreality.jogl.Viewer joglViewer = (de.jreality.jogl.Viewer) realViewer;
    Dimension d = realViewer.getViewingComponentSize();
    dimPanel.setDimension(d);
    
    File file = FileLoaderDialog.selectTargetFile(frame, dimPanel, false, createFileFilters());
    Dimension dim = dimPanel.getDimension();
//    Dimension dim = DimensionDialog.selectDimension(d,frame);
    if (file == null || dim == null) return;

    if (FileFilter.getFileExtension(file) == null) {  //no extension specified
      System.err.println("Please specify a valid file extension.\n" +
      "Export aborted.");
      return;
    }
    //render offscreen
    BufferedImage img = null;;
    if(realViewer instanceof de.jreality.jogl.Viewer)   
        img = ((de.jreality.jogl.Viewer)realViewer).renderOffscreen(4*dim.width, 4*dim.height);
    if(realViewer instanceof SoftViewer)
        img = ((SoftViewer)realViewer).renderOffscreen(4*dim.width, 4*dim.height);
    BufferedImage img2 = new BufferedImage(dim.width, dim.height,BufferedImage.TYPE_INT_RGB);
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
//    System.out.println("\nWriting to file "+file.getPath());
    de.jreality.jogl.JOGLRenderer.writeBufferedImage(file,img2);
  }
  
  
  @Override
  public boolean isEnabled() {
    Viewer realViewer = ((ViewerSwitch)viewer).getCurrentViewer();
    return realViewer instanceof de.jreality.jogl.Viewer || realViewer instanceof SoftViewer;
  }
  
  
  private FileFilter[] createFileFilters() {
    
    //get existing writer formats
    String writerFormats[] = ImageIO.getWriterFormatNames();
    //usually [bmp, jpeg, jpg, png, wbmp]
    String[] known = new String[]{"bmp","jpeg","jpg","png", "wbmp"};
    //get remaining formats ignoring case
    Set<String> special = new HashSet<String>();
    outer: for (int i = 0; i < writerFormats.length; i++) {
      final String ext = writerFormats[i].toLowerCase();
      for (int j = 0; j < known.length; j++) {
        if (known[j].equals(ext)) continue outer;
      }
      special.add(ext);
    }
    
    //ordered set of file filters (lexicographically order of preferred extension)
    Set<FileFilter> filters = new TreeSet<FileFilter>( new Comparator<FileFilter>(){
      public int compare(FileFilter f1, FileFilter f2) {
        return f1.getPreferredExtension().compareTo(f2.getPreferredExtension());
      }});
    
    //add known filter
    filters.add(new FileFilter("BMP Image", "bmp"));
    filters.add(new FileFilter("Wireless BMP Image", "wbmp"));
    filters.add(new FileFilter("JPEG Image", "jpg", "jpeg"));
    filters.add(new FileFilter("PNG Image", "png"));
    //add tiff filter if writer exists
    try { Class.forName("javax.media.jai.JAI");
    filters.add(new FileFilter("TIFF Image", "tiff", "tif"));
    } catch (ClassNotFoundException e) {}
    //add filters for special writer formats
    for (String s : special)
      filters.add(new FileFilter(s.toUpperCase()+" Image", s));
    
    //convert to (ordered) array
    FileFilter[] ff = new FileFilter[filters.size()];
    return filters.toArray(ff);
  }
  
}