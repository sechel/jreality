package de.jreality.tutorial.tool;

import java.awt.Color;

import de.jreality.geometry.Primitives;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.ScaleTool;
import de.jreality.ui.viewerapp.ViewerApp;
import static de.jreality.shader.CommonAttributes.*;

public class ScaleToolExample {

	public static void main(String[] args) {
		SceneGraphComponent cmp = new SceneGraphComponent();
		ScaleTool tool=new ScaleTool();
		cmp.addTool(tool);		
		cmp.setGeometry(Primitives.icosahedron());
		cmp.setAppearance(new Appearance());
		cmp.getAppearance().setAttribute(LINE_SHADER+"."+DIFFUSE_COLOR,new Color(250, 250, 0));
		cmp.getAppearance().setAttribute(POINT_SHADER+"."+DIFFUSE_COLOR,new Color(250, 0, 0));
	    ViewerApp.display(cmp);
	}
}
