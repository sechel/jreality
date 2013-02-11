package de.jreality.plugin.scripting;

import static javax.swing.JOptionPane.WARNING_MESSAGE;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.icon.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.flavor.FrontendFlavor;
import de.jtem.jrworkspace.plugin.flavor.PreferencesFlavor;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public class PythonToolsManager extends Plugin implements PreferencesFlavor, ListSelectionListener, ActionListener, FrontendFlavor {

	protected static final String
		DEFAULT_SOURCE = "print('Hallo Welt')\nprint(C)";
	protected static final Icon
		DEFAULT_ICON = ImageHook.getIcon("python.png");
	private JPanel
		panel = new JPanel(),
		topPanel = new JPanel();
	private ShrinkPanel
		fileLinkPanel = new ShrinkPanel("File Link");
	private Controller
		controller = null;
	private PythonConsole
		console = null;
	private Icon
		icon = ImageHook.getIcon("python.png");
	private List<PythonScriptTool>
		tools = new LinkedList<PythonScriptTool>();
	private ToolTableModel
		toolTableModel = new ToolTableModel();
	private JTable
		toolsTable = new JTable(toolTableModel);
	private JScrollPane
		toolsScroller = new JScrollPane(toolsTable);
	private JButton
		addToolButton = new JButton(ImageHook.getIcon("add.png")),
		removeToolButton = new JButton(ImageHook.getIcon("delete.png"));
	private Document
		sourceDocument = new PlainDocument();
	private JTextArea
		sourceArea = new JTextArea(sourceDocument);
	private JScrollPane
		sourceScroller = new JScrollPane(sourceArea);
	private JTextField
		nameField = new JTextField(15),
		menuPathField = new JTextField(15);
	private JButton
		saveButton = new JButton("Save and Update"),
		browseFileLinkButton = new JButton("..."),
		browseIconButton = new JButton("...");
	private JCheckBox
		useMenuEntryChecker = new JCheckBox("Menu Path"),
		useToolItemChecker = new JCheckBox("Tool Bar Item"),
		useFileLinkChecker = new JCheckBox("Load Source From File");
	private JTextField
		fileLinkField = new JTextField();
	private JFileChooser
		fileLinkChooser = new JFileChooser(),
		iconChooser = new JFileChooser();
	private JLabel
		iconLabel = new JLabel("Tool Icon");
	private FrontendListener
		frontendListener = null;
	
	private boolean
		listenersEnabled = true;
		
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setSize(800, 800);
		PythonToolsManager m = new PythonToolsManager();
		f.setLayout(new GridLayout());
		f.add(m.panel);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public PythonToolsManager() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;

		topPanel.setLayout(new GridBagLayout());
		c.weightx = 0.0;
		c.gridwidth = 1;
		topPanel.add(addToolButton, c);
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		topPanel.add(removeToolButton, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		JPanel spacer = new JPanel();
		topPanel.add(spacer, c);
		panel.add(topPanel, c);
		
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.BOTH;
		toolsScroller.setPreferredSize(new Dimension(100, 100));
		panel.add(toolsScroller, c);
		panel.add(new JSeparator(JSeparator.VERTICAL), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(new JLabel("Name"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(nameField, c);
		
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(iconLabel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(browseIconButton, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(useMenuEntryChecker, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(menuPathField, c);
		
		panel.add(useToolItemChecker, c);
		
		c.weighty = 1.0; 
		c.fill = GridBagConstraints.BOTH;
		panel.add(sourceScroller, c);
		
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		fileLinkPanel.setLayout(new GridBagLayout());
		fileLinkPanel.add(useFileLinkChecker, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		fileLinkPanel.add(fileLinkField, c);
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		fileLinkPanel.add(browseFileLinkButton, c);
		c.fill = GridBagConstraints.BOTH;
		panel.add(fileLinkPanel, c);
		
		c.weighty = 0.0;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.EAST;
		panel.add(saveButton, c);
		
		toolsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		toolsTable.getSelectionModel().addListSelectionListener(this);
		
		addToolButton.addActionListener(this);
		removeToolButton.addActionListener(this);
		saveButton.addActionListener(this);
		useFileLinkChecker.addActionListener(this);
		browseFileLinkButton.addActionListener(this);
		browseIconButton.addActionListener(this);
		useMenuEntryChecker.addActionListener(this);
		useToolItemChecker.addActionListener(this);
		nameField.addActionListener(this);
		menuPathField.addActionListener(this);
		
		
		toolsTable.getColumnModel().getColumn(0).setMaxWidth(30);
		toolsTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		toolsTable.setRowHeight(22);
		
		fileLinkField.setEditable(false);
		fileLinkPanel.setShrinked(true);
		
		fileLinkChooser.setMultiSelectionEnabled(false);
		fileLinkChooser.setDialogTitle("Open Python Script");
		fileLinkChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		fileLinkChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "Python Script (*.py)";
			}
			
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".py");
			}
			
		});
		iconChooser.setMultiSelectionEnabled(false);
		iconChooser.setDialogTitle("Open Icon Image");
		iconChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		iconChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "PNG Image File (*.png)";
			}
			
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
			}
			
		});
	}
	
	private class ToolTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return tools.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Icon.class;
			case 1:
				return PythonScriptTool.class;
			default:
				return String.class;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return tools.get(rowIndex).getIcon();
			case 1:
				return tools.get(rowIndex);
			default:
				return "-";
			}
		}
		
	}
	
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		updateTooleditor();
	}
	
	public void updateTooleditor() {
		listenersEnabled = false;
		PythonScriptTool tool = getSelectedTool();
		if (tool == null) {
			nameField.setText("");
			menuPathField.setText("");
			useFileLinkChecker.setSelected(false);
			fileLinkField.setText("");
			fileLinkField.setEnabled(false);
			browseFileLinkButton.setEnabled(false);
			sourceArea.setEditable(true);
			iconLabel.setIcon(null);
			try {
				int len = sourceDocument.getLength();
				sourceDocument.remove(0, len);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
			listenersEnabled = true;
			return;
		}
		nameField.setText(tool.getName());
		menuPathField.setText(tool.getMenuPath());
		useFileLinkChecker.setSelected(tool.isUseFileLink());
		fileLinkField.setEnabled(tool.isUseFileLink());
		browseFileLinkButton.setEnabled(tool.isUseFileLink());
		sourceArea.setEditable(!tool.isUseFileLink());
		iconLabel.setIcon(tool.getIcon());
		useMenuEntryChecker.setSelected(tool.isUseMenuItem());
		useToolItemChecker.setSelected(tool.isUseToolItem());
		menuPathField.setEnabled(useMenuEntryChecker.isSelected());
		if (tool.getFileLink() != null) {
			fileLinkField.setText(tool.getFileLink().getAbsolutePath());
		} else {
			fileLinkField.setText("");
		}
		int len = sourceDocument.getLength();
		try {
			sourceDocument.remove(0, len);
			sourceDocument.insertString(0, tool.getSourceCode(), null);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		listenersEnabled = true;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!listenersEnabled) return;
		PythonScriptTool tool = getSelectedTool();
		if (addToolButton == e.getSource()) {
			PythonScriptTool newTool = new PythonScriptTool(console, controller);
			tools.add(newTool);
			installTool(newTool);
		}
		if (removeToolButton == e.getSource()) {
			if (tool == null) return;
			tools.remove(tool);
			uninstallTool(tool);
		}
		if (saveButton == e.getSource()) {
			if (tool == null) return;
			uninstallTool(tool);
			tool.setName(nameField.getText());
			tool.setMenuPath(menuPathField.getText());
			int len = sourceDocument.getLength();
			try {
				String source = sourceDocument.getText(0, len);
				tool.setSourceCode(source);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
			installTool(tool);
		}
		if (useFileLinkChecker == e.getSource()) {
			if (tool == null) return;
			tool.setUseFileLink(useFileLinkChecker.isSelected());
		}
		if (browseFileLinkButton == e.getSource()) {
			if (tool == null) return;
			Window w = SwingUtilities.getWindowAncestor(panel);
			if (tool.getFileLink() != null) {
				fileLinkChooser.setSelectedFile(tool.getFileLink());
			}
			int r = fileLinkChooser.showOpenDialog(w);
			if (r != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File fileLink = fileLinkChooser.getSelectedFile();
			tool.setFileLink(fileLink);
		}
		if (browseIconButton == e.getSource()) {
			if (tool == null) return;
			Window w = SwingUtilities.getWindowAncestor(panel);
			int r = iconChooser.showOpenDialog(w);
			if (r != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File iconFile = iconChooser.getSelectedFile();
			try {
				BufferedImage image = ImageIO.read(iconFile);
				if (image.getWidth() > 22 || image.getHeight() > 22) {
					JOptionPane.showMessageDialog(w, "Please select a smaller image.", "Image too large", WARNING_MESSAGE);
					return;
				}
				ImageIcon icon = new ImageIcon(image);
				tool.setIcon(icon);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if (useToolItemChecker == e.getSource() || useMenuEntryChecker == e.getSource()) {
			if (tool == null) return;
			tool.setUseMenuItem(useMenuEntryChecker.isSelected());
			tool.setUseToolItem(useToolItemChecker.isSelected());
			updateTool(tool);
		}
		if (menuPathField == e.getSource() || nameField == e.getSource()) {
			if (tool == null) return;
			updateTool(tool);
		}
		updateTooleditor();
		toolsTable.revalidate();
	}
	
	
	public PythonScriptTool getSelectedTool() {
		int index = toolsTable.getSelectionModel().getMinSelectionIndex();
		if (index < 0) return null;
		if (index >= tools.size()) return null;
		return tools.get(index);
	}
	
	
	public void updateTool(PythonScriptTool tool) {
		uninstallTool(tool);
		installTool(tool);
	}
	
	public void installTool(PythonScriptTool tool) {
		ViewMenuBar menu = controller.getPlugin(ViewMenuBar.class);
		String[] menuPath = tool.getMenuPath().split(",");
		ViewToolBar toolBar = controller.getPlugin(ViewToolBar.class);
		if (tool.isUseMenuItem()) {
			menu.addMenuItem(PythonToolsManager.class, 1.0, tool, menuPath);
		}
		if (tool.isUseToolItem()) {
			toolBar.addAction(PythonToolsManager.class, 10000.0, tool);
		}
		frontendListener.updateFrontendUI();
	}
	
	public void uninstallTool(PythonScriptTool tool) {
		ViewMenuBar menu = controller.getPlugin(ViewMenuBar.class);
		ViewToolBar toolBar = controller.getPlugin(ViewToolBar.class);
		menu.removeMenuItem(PythonToolsManager.class, tool);
		toolBar.removeAction(PythonToolsManager.class, tool);
		frontendListener.updateFrontendUI();
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		this.controller = c;
		this.console = c.getPlugin(PythonConsole.class);
		ViewToolBar toolBar = controller.getPlugin(ViewToolBar.class);
		toolBar.addSeparator(PythonToolsManager.class, 9999.0);
		for (PythonScriptTool tool : tools) {
			installTool(tool);
		}
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "chooserDir", fileLinkChooser.getCurrentDirectory().getAbsolutePath());
		c.storeProperty(getClass(), "iconChooserDir", iconChooser.getCurrentDirectory().getAbsolutePath());
		c.storeProperty(getClass(), "numTools", tools.size());
		for (PythonScriptTool tool : tools) {
			int index = tools.indexOf(tool);
			c.storeProperty(getClass(), "name" + index, tool.getName());
			c.storeProperty(getClass(), "sourceCode" + index, tool.getSourceCode());
			c.storeProperty(getClass(), "useFileLink" + index, tool.isUseFileLink());
			c.storeProperty(getClass(), "menuPath" + index, tool.getMenuPath());
			c.storeProperty(getClass(), "icon" + index, tool.getIcon());
			c.storeProperty(getClass(), "useMenuItem" + index, tool.isUseMenuItem());
			c.storeProperty(getClass(), "useToolItem" + index, tool.isUseToolItem());
			if (tool.getFileLink() != null) {
				c.storeProperty(getClass(), "fileLink" + index, tool.getFileLink().getAbsolutePath());
			} else {
				c.storeProperty(getClass(), "fileLink" + index, null);
			}
		}
	}
	
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		PythonConsole console = c.getPlugin(PythonConsole.class);
		String chooserDir = c.getProperty(getClass(), "chooserDir", ".");
		fileLinkChooser.setCurrentDirectory(new File(chooserDir));
		String iconDir = c.getProperty(getClass(), "iconChooserDir", ".");
		iconChooser.setCurrentDirectory(new File(iconDir));
		int numTools = c.getProperty(getClass(), "numTools", 0);
		for (int i = 0; i < numTools; i++) {
			PythonScriptTool tool = new PythonScriptTool(console, c);
			String name = c.getProperty(getClass(), "name" + i, "Unknown Name");
			String sourceCode = c.getProperty(getClass(), "sourceCode" + i, DEFAULT_SOURCE);
			boolean useFileLink = c.getProperty(getClass(), "useFileLink" + i, false);
			String menuPath = c.getProperty(getClass(), "menuPath" + i, "Python Tools");
			Icon icon = c.getProperty(getClass(), "icon" + i, DEFAULT_ICON);
			String fileLinkPath = c.getProperty(getClass(), "fileLink" + i, null);
			boolean useMenuItem = c.getProperty(getClass(), "useMenuItem" + i, true);
			boolean useToolItem = c.getProperty(getClass(), "useToolItem" + i, true);
			tool.setName(name);
			tool.setSourceCode(sourceCode);
			tool.setUseFileLink(useFileLink);
			tool.setMenuPath(menuPath);
			tool.setIcon(icon);
			tool.setUseMenuItem(useMenuItem);
			tool.setUseToolItem(useToolItem);
			if (fileLinkPath != null) {
				File fileLink = new File(fileLinkPath);
				tool.setFileLink(fileLink);
			}
			tools.add(tool);
		}
	};
	
	
	@Override
	public String getMainName() {
		return "Python Script Tools";
	}

	@Override
	public JPanel getMainPage() {
		return panel;
	}

	@Override
	public Icon getMainIcon() {
		return icon;
	}

	@Override
	public int getNumSubPages() {
		return 0;
	}
	@Override
	public String getSubPageName(int i) {
		return null;
	}
	@Override
	public JPanel getSubPage(int i) {
		return null;
	}
	@Override
	public Icon getSubPageIcon(int i) {
		return null;
	}

	@Override
	public void setFrontendListener(FrontendListener l) {
		this.frontendListener = l;
	}

}
