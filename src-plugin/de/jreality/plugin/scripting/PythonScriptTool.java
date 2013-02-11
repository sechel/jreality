package de.jreality.plugin.scripting;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.python.core.PyCode;
import org.python.util.PythonInterpreter;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.Controller;

public class PythonScriptTool extends AbstractAction {
	
	private static final long serialVersionUID = 1L;
	private static Random
		random = new Random();
	private String
		name = "New Python Tool",
		menuPath = "Python Tools",
		sourceCode = PythonToolsManager.DEFAULT_SOURCE;
	private boolean
		useGUI = false,
		useMenuItem = true,
		useToolItem = true,
		useFileLink = false;
	private File
		fileLink = null;
	private long
		fileLastModified = -1;
	private Icon
		icon = PythonToolsManager.DEFAULT_ICON;
	private PyCode
		code = null;
	private PythonConsole
		console = null;
	private Controller
		controller = null;
	private List<PythonGUI<?>>
		guiList = new LinkedList<PythonGUI<?>>();
	private long
		toolId = random.nextLong();
	private PythonGUIShrinker
		guiShrinker = new PythonGUIShrinker(this);

	public PythonScriptTool(PythonConsole console, Controller controller) {
		this(console, controller, -1);
	}
	
	public PythonScriptTool(PythonConsole console, Controller controller, long id) {
		this.console = console;
		this.controller = controller;
		this.toolId = id;
		setName(name);
		setIcon(icon);
		setSourceCode(sourceCode);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		execute();
	}

	public void execute() {
		PythonInterpreter pi = console.getInterpreter();
		pi.cleanup();
		pi.set("C", controller);
		if (useGUI) {
			for (PythonGUI<?> gui : guiList) {
				pi.set(gui.getVariableName(), gui.getVariableValue());
			}
		}
		PyCode code = getCode();
		pi.exec(code);
	}
	
	private PyCode getCode() {
		if (isSourceDirty()) {
			code = null;
		}
		if (code != null) {
			return code;
		} else {
			PythonInterpreter pi = console.getInterpreter();
			return code = pi.compile(getSourceCode()); 
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
		if (isSourceDirty()) {
			FileReader fr = null;
			try {
				fr = new FileReader(fileLink);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				return sourceCode;
			}
			LineNumberReader lr = new LineNumberReader(fr);
			try {
				sourceCode = "";
				String line = lr.readLine();
				while (line != null) {
					sourceCode += line + "\n";
					line = lr.readLine();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					lr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			code = null;
			fileLastModified = fileLink.lastModified();
		}
		return sourceCode;
	}
	private boolean isSourceDirty() {
		return useFileLink && fileLink != null && fileLink.lastModified() != fileLastModified;
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
		guiShrinker.setIcon(icon);
	}
	public String getMenuPath() {
		return menuPath;
	}
	public void setMenuPath(String menuPath) {
		this.menuPath = menuPath;
	}
	
	public File getFileLink() {
		return fileLink;
	}
	public void setFileLink(File fileLink) {
		fileLastModified = -1;
		this.fileLink = fileLink;
	}
	public boolean isUseFileLink() {
		return useFileLink;
	}
	public void setUseFileLink(boolean useFileLink) {
		fileLastModified = -1;
		this.useFileLink = useFileLink;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public boolean isUseMenuItem() {
		return useMenuItem;
	}
	public void setUseMenuItem(boolean useMenuItem) {
		this.useMenuItem = useMenuItem;
	}
	public boolean isUseToolItem() {
		return useToolItem;
	}
	public void setUseToolItem(boolean useToolItem) {
		this.useToolItem = useToolItem;
	}
	public boolean isUseGUI() {
		return useGUI;
	}
	public void setUseGUI(boolean useGUI) {
		this.useGUI = useGUI;
	}
	public List<PythonGUI<?>> getGuiList() {
		return guiList;
	}
	public void updateGUI() {
		guiShrinker.updateGUI();
		View view = controller.getPlugin(View.class);
		view.getLeftSlot().removeShrinkPanel(guiShrinker);
		if (useGUI) {
			view.getLeftSlot().addShrinkPanel(guiShrinker);
		}
	}
	
	public long getToolId() {
		return toolId;
	}
	
}