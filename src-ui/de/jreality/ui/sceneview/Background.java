package de.jreality.ui.sceneview;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ShaderUtility;

public class Background {

	public static Color[] defaultBackgroundColor = new Color[]{
		new Color(225, 225, 225), new Color(225, 225, 225),
		new Color(255, 225, 180), new Color(255, 225, 180), };

	private SceneView view;
	private JMenu backgroundColorMenu;
	private ButtonGroup buttonGroup;
	private ButtonModel defaultModel;

	public Background(SceneView view) {
		this.view = view;
		
		backgroundColorMenu = new JMenu("Set background color");
		buttonGroup = new ButtonGroup();
		LinkedList<JRadioButtonMenuItem> items = new LinkedList<JRadioButtonMenuItem>();
		JRadioButtonMenuItem defaultItem = new JRadioButtonMenuItem(
				createAction("default", defaultBackgroundColor)
		);
		defaultModel = defaultItem.getModel();
		items.add(defaultItem);
		items.add( new JRadioButtonMenuItem(createAction("white", Color.WHITE)) );
		items.add( new JRadioButtonMenuItem(createAction("gray", new Color(225, 225, 225))) );
		items.add( new JRadioButtonMenuItem(createAction("black", Color.BLACK)) );
		for (JRadioButtonMenuItem item : items) {
			buttonGroup.add(item);
			backgroundColorMenu.add(item);
		}

		//set viewer background color if not specified already
		Appearance app = view.getSceneRoot().getAppearance();
		if (
				app == null ||
				app.getAttribute(CommonAttributes.BACKGROUND_COLORS) == Appearance.INHERITED&&
				app.getAttribute(CommonAttributes.BACKGROUND_COLOR) == Appearance.INHERITED
		) {
			buttonGroup.setSelected(defaultModel, true);
			setBackgroundColor(defaultBackgroundColor);
		}
	}

	/**
	 * Sets the scene root's background color.
	 * @param colors list of colors with length = 1 or 4
	 */
	private void setBackgroundColor(Color... colors) {
		if (colors == null || (colors.length!=1 && colors.length!=4)) 
			throw new IllegalArgumentException("illegal length of colors[]");
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

	public JMenu getMenu() {
		return backgroundColorMenu;
	}

	@SuppressWarnings("serial")
	private Action createAction(String name, final Color ... colors) {
		return new AbstractAction(name) {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setBackgroundColor(colors);
			}
		};
	}
}
