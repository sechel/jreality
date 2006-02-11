package de.jreality.ui.viewerapp;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import de.jreality.reader.Readers;

public class FileLoaderDialog {
	
  static File lastDir=new File("/net/MathVis/data/testData3D");
  
  
  
  static JFileChooser createFileChooser() {
      FileFilter ff = new FileFilter(){
            public boolean accept(File arg0) {
                if (arg0.isDirectory()) return true;
                String filename = arg0.getName().toLowerCase();
                if (Readers.findFormat(filename) != null)
                    return true;
                return false;
            }

            public String getDescription() {
                return "jReality 3D data files";
            }
        };
      return createFileChooser(ff);
  }
  
  static JFileChooser createFileChooser(final String ext, final String description) {
      FileFilter ff = new FileFilter(){
          public boolean accept(File arg0) {
              if (arg0.isDirectory()) return true;
              if(arg0.getName().endsWith("."+ext))
                  return true;
              return false;
          }

          public String getDescription() {
              return description;
          }
      };
      return createFileChooser(ff);
  }
	static JFileChooser createFileChooser(FileFilter ff) {
		FileSystemView view = FileSystemView.getFileSystemView();
		JFileChooser chooser = new JFileChooser(!lastDir.exists() ? view.getHomeDirectory() : lastDir, view);
		chooser.addChoosableFileFilter(ff);
		return chooser;
	}
	
  public static File[] loadFiles(Component parent) {
    JFileChooser chooser = createFileChooser();
    chooser.setMultiSelectionEnabled(true);
    chooser.showOpenDialog(parent);
    File[] files = chooser.getSelectedFiles();
    lastDir = chooser.getCurrentDirectory();
    return files;
  }
  
  private static File selectTargetFile(Component parent,JFileChooser chooser) {
    chooser.setMultiSelectionEnabled(false);
    chooser.showSaveDialog(parent);
    lastDir = chooser.getCurrentDirectory();
    return chooser.getSelectedFile();
  }
  public static File selectTargetFile(Component parent) {
      JFileChooser chooser = createFileChooser();
      return selectTargetFile(parent,chooser);
  }
  
  public static File selectTargetFile(Component parent, String extension, String description) {
      JFileChooser chooser = createFileChooser(extension, description);
      return selectTargetFile(parent,chooser);
    }
  
  
}
