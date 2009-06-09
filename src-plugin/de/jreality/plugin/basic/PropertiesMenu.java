package de.jreality.plugin.basic;

import static javax.swing.JFileChooser.FILES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import de.jreality.plugin.icon.ImageHook;
import de.jreality.util.Secure;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.flavor.PropertiesFlavor;

public class PropertiesMenu extends Plugin implements PropertiesFlavor {

	
	protected class WritePropertiesAction extends AbstractAction {

		private static final long 
			serialVersionUID = 1L;

		public WritePropertiesAction() {
			putValue(Action.NAME, "Save Properties");
			putValue(Action.SMALL_ICON, ImageHook.getIcon("diskette.png"));
		}
		
		
		public void actionPerformed(ActionEvent e) {
			Component parent = SwingUtilities.getWindowAncestor(view.getCenterComponent());
			int result = fileChooser.showSaveDialog(parent);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File f = fileChooser.getSelectedFile();
			if (f.exists()) {
				int owr = JOptionPane.showConfirmDialog(parent, "The file " + f.getName() + " exists. Overwrite?", "File Exists", YES_NO_OPTION);
				if (owr != JOptionPane.YES_OPTION) {
					return;
				}
			}
			try {
				FileWriter w = new FileWriter(f);
				propertiesListener.writeProperties(w);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(parent, "File could not be written: " + ex.getLocalizedMessage(), "Write error", ERROR_MESSAGE);
			}
		}
		
	}
	
	protected class LoadPropertiesAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;

		public LoadPropertiesAction() {
			putValue(Action.NAME, "Load Properties");
			putValue(Action.SMALL_ICON, ImageHook.getIcon("folder.png"));
		}
		
		
		public void actionPerformed(ActionEvent e) {
			Component parent = SwingUtilities.getWindowAncestor(view.getCenterComponent());
			int result = fileChooser.showOpenDialog(parent);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File f = fileChooser.getSelectedFile();
			try {
				FileReader r = new FileReader(f);
				propertiesListener.readProperties(r);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(parent, "File could not be read: " + ex.getLocalizedMessage(), "Read error", ERROR_MESSAGE);
			}
		}
		
	}
	
	
	protected class LoadDefaultPropertiesAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;
		
		public LoadDefaultPropertiesAction() {
			putValue(Action.NAME, "Load Default Properties");
			putValue(Action.SMALL_ICON, ImageHook.getIcon("arrow_rotate_anticlockwise.png"));
		}
		
		
		public void actionPerformed(ActionEvent e) {
			Component parent = SwingUtilities.getWindowAncestor(view.getCenterComponent());
			int result = JOptionPane.showConfirmDialog(parent, "Do you really want to load the default properties?", "Confirm", YES_NO_OPTION);
			if (result != JOptionPane.YES_OPTION) {
				return;
			}
			propertiesListener.loadDefaultProperties();
		}
		
	}
	
	
	private class PropertiesFileFilter extends FileFilter {

		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			String name = f.getName().toLowerCase();
			return name.endsWith(".xml") || name.endsWith(".jrs");
		}

		@Override
		public String getDescription() {
			return "Property Files (*.xml, *.jrw)";
		}
		
	}
	
	
	private ViewMenuBar
		viewMenuBar = null;
	private View
		view = null;
	private PropertiesListener
		propertiesListener = null;
	private Action
		writeAction = new WritePropertiesAction(),
		loadAction = new LoadPropertiesAction(),
		loadDefaultAction = new LoadDefaultPropertiesAction();
	private JFileChooser
		fileChooser = new JFileChooser();
	
	
	public PropertiesMenu() {
		String dir = Secure.getProperty("user.dir", "");
		fileChooser.setCurrentDirectory(new File(dir));
		fileChooser.addChoosableFileFilter(new PropertiesFileFilter());
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileSelectionMode(FILES_ONLY);
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		viewMenuBar = c.getPlugin(ViewMenuBar.class);
		viewMenuBar.addMenuItem(getClass(), 1, writeAction, "Properties");
		viewMenuBar.addMenuItem(getClass(), 2, loadAction, "Properties");
		viewMenuBar.addMenuSeparator(getClass(), 2.5, "Properties");
		viewMenuBar.addMenuItem(getClass(), 3, loadDefaultAction, "Properties");
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		viewMenuBar.removeAll(getClass());
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Property Menu Items", "jReality Group");
	}

	public void setPropertiesListener(PropertiesListener l) {
		propertiesListener = l;
	}

}
