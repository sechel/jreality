package de.jreality.ui.plugin;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.plugin.image.ImageHook;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class Background extends Plugin {

	public static Color[] defaultBackgroundColor = new Color[]{
		new Color(225, 225, 225), new Color(225, 225, 225),
		new Color(255, 225, 180), new Color(255, 225, 180), };

	private View view;
	private JMenu menu;
	private ButtonGroup buttonGroup;
	private HashMap<String, ButtonModel> nameToButton = new  HashMap<String, ButtonModel>();
	private HashMap<String, Color[]> nameToColors = new  HashMap<String, Color[]>();
	
	private ViewMenuBar viewerMenuAggregator;

	public Background() {

		menu = new JMenu("Set background color");
		buttonGroup = new ButtonGroup();

		addChoice(
				"default",
				new Color[] {
						new Color(225, 225, 225), new Color(225, 225, 225),
						new Color(255, 225, 180), new Color(255, 225, 180)
				}
		);
		addChoice("white", Color.white);
		addChoice("gray", new Color(225, 225, 225));
		addChoice("black", Color.black);

		setColor("default");
	}

	public String getColor() {
		return buttonGroup.getSelection().getActionCommand();
	}
	
	/**
	 * Sets the scene root's background color.
	 * @param colors list of colors with length = 1 or 4
	 */
	public void setColor(String name) {
		nameToButton.get(name).setSelected(true);
		if (view != null) {
			Color[] colors = nameToColors.get(name);
			if (colors == null || (colors.length!=1 && colors.length!=4)) {
				throw new IllegalArgumentException("illegal length of colors[]");
			}
			SceneGraphComponent root = view.getSceneRoot();
			Appearance app = root.getAppearance();
			if (app == null) {
				app = new Appearance("root appearance");
				ShaderUtility.createRootAppearance(app);
				root.setAppearance(app);
			}

			//trim colors[] if it contains the same 4 colors
			if (colors.length == 4) {
				boolean equal = true;
				for (int i = 1; i < colors.length; i++)
					if (colors[i] != colors[0]) equal = false;
				if (equal) colors = new Color[]{ colors[0] };
			}

			app.setAttribute("backgroundColor", (colors.length==1)? colors[0] : Appearance.INHERITED);
			app.setAttribute("backgroundColors", (colors.length==4)? colors : Appearance.INHERITED); 
		}
	}

	public JMenu getMenu() {
		return menu;
	}

	@SuppressWarnings("serial")
	private void addChoice(final String name, final Color ... colors) {
		nameToColors.put(name, colors);
		Action action = new AbstractAction(name) {

			public void actionPerformed(ActionEvent e) {
				setColor(name);

			}

		};
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(action);
		item.getModel().setActionCommand(name);
		nameToButton.put(name, item.getModel());
		buttonGroup.add(item);
		menu.add(item);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Viewer Background";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("arrow.png");
		return info;
	}

	public void install(View view) {
		this.view = view;
		setColor(getColor());
	}

	@Override
	public void install(Controller c) throws Exception {
		install(view);
		viewerMenuAggregator = c.getPlugin(ViewMenuBar.class);
		viewerMenuAggregator.addMenuItem(
				getClass(),
				10.0,
				getMenu(),
				"Viewer"
		);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		viewerMenuAggregator.removeMenuAll(getClass());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		setColor(c.getProperty(getClass(), "color", getColor()));
		super.restoreStates(c);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "color", getColor());
		super.storeStates(c);
	}
}
