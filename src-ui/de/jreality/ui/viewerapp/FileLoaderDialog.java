package de.jreality.ui.viewerapp;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import de.jreality.reader.Readers;

public class FileLoaderDialog {
	
  static File lastDir;
  
	static JFileChooser createFileChooser() {
		FileSystemView view = FileSystemView.getFileSystemView();
		JFileChooser chooser = new JFileChooser(lastDir == null ? view.getHomeDirectory() : lastDir, view);
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
		chooser.addChoosableFileFilter(ff);
		chooser.setMultiSelectionEnabled(true);
		return chooser;
	}
	
  public static File[] loadFiles(Component parent) {
    JFileChooser chooser = createFileChooser();
    chooser.showOpenDialog(parent);
    File[] files = chooser.getSelectedFiles();
    lastDir = chooser.getCurrentDirectory();
    return files;
  }
}
