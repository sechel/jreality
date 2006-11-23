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

import java.awt.Component;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import de.jreality.reader.Readers;
import de.jreality.util.Secure;


public class FileLoaderDialog {
	
  static File lastDir = new File(Secure.getProperty("jreality.data", "/net/MathVis/data/testData3D"));
  
  
  public static JFileChooser createFileChooser() {
    FileFilter ff = new FileFilter("jReality 3D data files") {
      @Override
      public boolean accept(File f) {
        if (f.isDirectory()) return true;
        String filename = f.getName().toLowerCase();
        return (Readers.findFormat(filename) != null);
      }
    };
    ff.setShowExtensionList(false);
    return createFileChooser(true, ff);
  }
  
  
  public static JFileChooser createFileChooser(final String ext, final String description) {
      return createFileChooser(true, new FileFilter(description, ext));
  }

  
  public static JFileChooser createFileChooser(boolean useAcceptAllFileFilter, FileFilter... ff) {
    FileSystemView view = FileSystemView.getFileSystemView();
    JFileChooser chooser = new JFileChooser(!lastDir.exists() ? view.getHomeDirectory() : lastDir, view);
    
    chooser.setAcceptAllFileFilterUsed(useAcceptAllFileFilter);
    for (int i = 0; i < ff.length; i++)
      chooser.addChoosableFileFilter(ff[i]);
    if (ff.length != 0) chooser.setFileFilter(ff[0]);
    else if (useAcceptAllFileFilter)
      chooser.setFileFilter(chooser.getAcceptAllFileFilter());
    
    return chooser;
  }
  
  
  
  //-- OPEN FILE DIALOGS ----------------------------------
  
  private static File[] loadFiles(Component parent, JFileChooser chooser, JComponent accessory) {
    if (accessory != null) chooser.setAccessory(accessory);
    chooser.setMultiSelectionEnabled(true);
    chooser.showOpenDialog(parent);
    File[] files = chooser.getSelectedFiles();
    lastDir = chooser.getCurrentDirectory();
    return files;
  }
  
  private static File loadFile(Component parent, JFileChooser chooser, JComponent accessory) {
    if (accessory != null) chooser.setAccessory(accessory);
    chooser.setMultiSelectionEnabled(false);
    chooser.showOpenDialog(parent);
    File file = chooser.getSelectedFile();
    lastDir = chooser.getCurrentDirectory();
    return file;
  }
  
  
  public static File[] loadFiles(Component parent) {
    return loadFiles(parent, (JComponent)null);
  }
  
  public static File[] loadFiles(Component parent, JComponent accessory) {
    JFileChooser chooser = createFileChooser();  //adds default file filter for jReality 3D data files
    return loadFiles(parent, chooser, accessory); 
  }
  
  public static File loadFile(Component parent, String extension, String description) {
    JFileChooser chooser = createFileChooser(extension, description);
    return loadFile(parent, chooser, (JComponent)null);
  }
  
  
  
  //-- SAVE FILE DIALOGS ----------------------------------
  
  private static File selectTargetFile(Component parent, JFileChooser chooser, JComponent accessory) {
    if (accessory != null) chooser.setAccessory(accessory);
    chooser.setMultiSelectionEnabled(false);
    chooser.showSaveDialog(parent);
    lastDir = chooser.getCurrentDirectory();
    File file = chooser.getSelectedFile();
    
    //append preferred extension of used file filter if existing and user did not specify one
    try {
      FileFilter filter = (FileFilter) chooser.getFileFilter();
      if (!filter.accept(file)) {  //invalid extension
        String extension = filter.getPreferredExtension();
        if (extension != null) file = new File(file.getPath()+"."+extension);
      }
    } catch (Exception e) {}
   
    return file;
  }
  
  
  public static File selectTargetFile(Component parent) {
      return selectTargetFile(parent, (JComponent)null);
  }
  
  public static File selectTargetFile(Component parent, JComponent accessory) {
    JFileChooser chooser = createFileChooser();
    return selectTargetFile(parent, chooser, accessory);
  }

  public static File selectTargetFile(Component parent, String extension, String description) {
    return selectTargetFile(parent, (JComponent)null, extension, description);
  }
  
  public static File selectTargetFile(Component parent, JComponent accessory, String extension, String description) {
      JFileChooser chooser = createFileChooser(extension, description);
      return selectTargetFile(parent, chooser, accessory);
  }
  
  public static File selectTargetFile(Component parent, boolean useAcceptAllFileFilter, FileFilter... ff) {
    return selectTargetFile(parent, (JComponent)null, useAcceptAllFileFilter, ff);
  }
  
  public static File selectTargetFile(Component parent, JComponent accessory, boolean useAcceptAllFileFilter, FileFilter... ff) {
    JFileChooser chooser = createFileChooser(useAcceptAllFileFilter, ff);
    return selectTargetFile(parent, chooser, accessory);
  }
  
}