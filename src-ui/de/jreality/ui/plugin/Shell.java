package de.jreality.ui.plugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.Expression;
import java.beans.Statement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import de.jreality.scene.Viewer;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.ui.plugin.image.ImageHook;
import de.jreality.ui.viewerapp.BeanShell;
import de.jreality.ui.viewerapp.SelectionManager;
import de.jreality.ui.viewerapp.SelectionManagerInterface;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class Shell extends ShrinkPanelPlugin {

	public Shell() {
		setInitialPosition(SHRINKER_BOTTOM);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	public static class H {
	
		public static String help(Object o) {
			class MethodComparator implements Comparator<Method> {
				public int compare(Method o1, Method o2) {
					return o1.getName().compareTo(o2.getName());
				}
			};
			Method[] methods = o.getClass().getDeclaredMethods();
			Arrays.sort(methods, new MethodComparator());
			StringBuffer r = new StringBuffer();
			for (Method m : methods) {
				if (!Modifier.isPublic(m.getModifiers())) {
					continue;
				}
				r.append(m.getName() + "(");
				int num = 0;
				for (Class<?> param : m.getParameterTypes()) {
					r.append(param.getSimpleName());
					if (++num != m.getParameterTypes().length) {
						 r.append(", ");
					}
				}
				r.append(")\n");
			}
			return r.toString();
		}
		
	}
		
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		View sceneViewPlugin = c.getPlugin(View.class);
		View sceneView = sceneViewPlugin;
		Viewer viewer = sceneView.getViewer();
		SelectionManagerInterface selectionManager =
			SelectionManager.selectionManagerForViewer(viewer);
		ToolSystem toolSystem = ToolSystem.getToolSystemForViewer(viewer);

		BeanShell beanShell = new BeanShell(selectionManager);

		beanShell.eval("import de.jreality.geometry.*;");
		beanShell.eval("import de.jreality.math.*;");    
		beanShell.eval("import de.jreality.scene.*;");
		beanShell.eval("import de.jreality.scene.data.*;");
		beanShell.eval("import de.jreality.scene.tool.*;");
		beanShell.eval("import de.jreality.shader.*;");
		beanShell.eval("import de.jreality.tools.*;");
		beanShell.eval("import de.jreality.util.*;");
		beanShell.eval("import de.jreality.ui.plugin.Shell.H;");
		beanShell.eval("import de.jreality.ui.plugin.*");
		beanShell.eval("import de.jreality.vr.plugin.*");
		beanShell.eval("import de.jreality.audio.plugin.*");

		//set some objects to be accessible from within the beanShell
		try {
			Object bshEval = new Expression(beanShell, "getBshEval", null).getValue();
			Object interpreter = new Expression(bshEval, "getInterpreter", null).getValue();
			new Statement(interpreter, "set", new Object[]{"_viewer", viewer}).execute();
			new Statement(interpreter, "set", new Object[]{"_toolSystem", toolSystem}).execute();
			new Statement(interpreter, "set", new Object[]{"_c", c}).execute();
		} 
		catch (Exception e) { e.printStackTrace(); }

		JComponent shell = (JComponent) beanShell.getComponent();
		shell.setPreferredSize(new Dimension(0, 100));
		shell.setMinimumSize(new Dimension(10, 100));

		shrinkPanel.getContentPanel().setBorder(BorderFactory.createEtchedBorder());
		shrinkPanel.setLayout(new GridLayout());
		shrinkPanel.add(shell);
		shrinkPanel.setHeaderColor(new Color(0.5f, 0.3f, 0.4f));
	}


	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		shrinkPanel.removeAll();
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "BeanShell";
		info.vendorName = "Stefan Sechelmann";
		info.icon = ImageHook.getIcon("zahnradgrau.png");
		return info;
	}

}

