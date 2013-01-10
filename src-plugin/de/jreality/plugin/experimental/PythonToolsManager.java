package de.jreality.plugin.experimental;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.python.core.PyCode;
import org.python.util.PythonInterpreter;

import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.icon.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.flavor.PreferencesFlavor;

public class PythonToolsManager extends Plugin implements PreferencesFlavor, ListSelectionListener, ActionListener {

	private JPanel
		panel = new JPanel(),
		topPanel = new JPanel();
	private Controller
		controller = null;
	private PythonConsole
		console = null;
	private Icon
		icon = ImageHook.getIcon("python.png");
	private List<PythonScriptTool>
		tools = new LinkedList<PythonToolsManager.PythonScriptTool>();
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
		nameField = new JTextField(),
		menuPathField = new JTextField();
	private JButton
		saveButton = new JButton("Save and Update");
		
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
		c.fill = GridBagConstraints.BOTH;
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
		toolsScroller.setPreferredSize(new Dimension(100, 100));
		panel.add(toolsScroller, c);
		panel.add(new JSeparator(JSeparator.VERTICAL), c);
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(new JLabel("Name"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(nameField, c);
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(new JLabel("Menu Path"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(menuPathField, c);
		
		c.weighty = 1.0; 
		panel.add(sourceScroller, c);
		
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
	}
	
	public class PythonScriptTool extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		public String
			name = "Python Scripting Tool",
			menuPath = "Python Scripts",
			sourceCode = "";
		public Icon 
			icon = ImageHook.getIcon("python.png");
		public PyCode
			code = null;

		public PythonScriptTool() {
			setName(name);
			setIcon(icon);
			setSourceCode(sourceCode);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			PythonInterpreter pi = console.getInterpreter();
			pi.exec(getCode());
		}
		
		private PyCode getCode() {
			if (code != null) {
				return code;
			} else {
				PythonInterpreter pi = console.getInterpreter();
				return code = pi.compile(sourceCode); 
			}
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(LONG_DESCRIPTION, name);
			this.name = name;
		}
		public String getSourceCode() {
			return sourceCode;
		}
		public void setSourceCode(String sourceCode) {
			this.sourceCode = sourceCode;
			this.code = null;
		}
		public Icon getIcon() {
			return icon;
		}
		public void setIcon(Icon icon) {
			putValue(SMALL_ICON, icon);
			putValue(LARGE_ICON_KEY, icon);
			this.icon = icon;
		}
		public String getMenuPath() {
			return menuPath;
		}
		public void setMenuPath(String menuPath) {
			this.menuPath = menuPath;
		}
		
		@Override
		public String toString() {
			return getName();
		}
		
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
		PythonScriptTool tool = getSelectedTool();
		nameField.setText(tool.getName());
		menuPathField.setText(tool.getMenuPath());
		int len = sourceDocument.getLength();
		try {
			sourceDocument.remove(0, len);
			sourceDocument.insertString(0, tool.getSourceCode(), null);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (addToolButton == e.getSource()) {
			PythonScriptTool newTool = new PythonScriptTool();
			tools.add(newTool);
			installTool(newTool);
		}
		if (removeToolButton == e.getSource()) {
			PythonScriptTool tool = getSelectedTool();
			if (tool == null) {
				return;
			}
			tools.remove(tool);
			uninstallTool(tool);
		}
		if (saveButton == e.getSource()) {
			PythonScriptTool tool = getSelectedTool();
			if (tool == null) {
				return;
			}
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
		toolsTable.revalidate();
	}
	
	
	public PythonScriptTool getSelectedTool() {
		int index = toolsTable.getSelectionModel().getMinSelectionIndex();
		if (index < 0) return null;
		return tools.get(index);
	}
	
	
	public void installTool(PythonScriptTool tool) {
		ViewMenuBar menu = controller.getPlugin(ViewMenuBar.class);
		String[] menuPath = tool.menuPath.split(",");
		ViewToolBar toolBar = controller.getPlugin(ViewToolBar.class);
		menu.addMenuItem(PythonToolsManager.class, 1.0, tool, menuPath);
		toolBar.addAction(PythonToolsManager.class, 10000.0, tool);
	}
	
	public void uninstallTool(PythonScriptTool tool) {
		ViewMenuBar menu = controller.getPlugin(ViewMenuBar.class);
		ViewToolBar toolBar = controller.getPlugin(ViewToolBar.class);
		menu.removeMenuItem(PythonToolsManager.class, tool);
		toolBar.removeAction(PythonToolsManager.class, tool);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		this.controller = c;
		this.console = c.getPlugin(PythonConsole.class);
		ViewToolBar toolBar = controller.getPlugin(ViewToolBar.class);
		toolBar.addSeparator(PythonToolsManager.class, 9999.0);
	}
	
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

}
