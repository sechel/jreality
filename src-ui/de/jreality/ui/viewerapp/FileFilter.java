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

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;


/**
 * @author msommer
 */
public class FileFilter extends javax.swing.filechooser.FileFilter {
  
  private HashSet<String> extensions;
  private String preferred;
  private String description;
  private boolean showExtensionList = true;
  

  public FileFilter() {
    this(null);
  }

  
  public FileFilter(String description, String... extensions) {
    setDescription(description);
    this.extensions = new LinkedHashSet<String>();
    for (int i = 0; i < extensions.length; i++)
      addExtension(extensions[i]);
  }
  

  @Override
  public boolean accept(File f) {
    if (f == null) return false;
    if(f.isDirectory()) return true;
      
    String extension = getFileExtension(f);
    return (extension != null && 
        (extensions.contains(extension) ||
            extensions.contains(extension.toLowerCase()) ||
            extensions.contains(extension.toUpperCase())) );
  }


  @Override
  public String getDescription() {
    
    if (!showExtensionList || extensions.isEmpty()) 
      return description;
    
    String extensionList = null;
    for (String extension : extensions) {
      if (extensionList == null) extensionList = " (*."+extension;
      else extensionList += ", *."+extension;
    }
    extensionList += ")";
    
    return description + extensionList;
  }
  
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  
  public void addExtension(String extension) {
    extensions.add(extension);
  }

  
  public void setPreferredExtension(String preferred) {
    addExtension(preferred);
    this.preferred = preferred;
  }
  
  
  public String getPreferredExtension() {
    if (preferred == null && !extensions.isEmpty())
      return extensions.iterator().next();  //return first in set

    return preferred;
  }
  
  
  public static String getFileExtension(File f) {
    if (f != null) {
      String filename = f.getName();
      int i = filename.lastIndexOf('.');
      if(i>0 && i<filename.length()-1)
        return filename.substring(i+1).toLowerCase();
    }
    return null;
  }
  

  public boolean isShowExtensionList() {
    return showExtensionList;
  }


  public void setShowExtensionList(boolean showExtensionList) {
    this.showExtensionList = showExtensionList;
  }
  
}