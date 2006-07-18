package de.jreality.tutorial;

import de.jreality.geometry.Primitives;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.ScaleTool;
import de.jreality.ui.viewerapp.ViewerApp;

public class ScaleToolExample {

	public static void main(String[] args) {
		SceneGraphComponent cmp = new SceneGraphComponent();
		ScaleTool tool=new ScaleTool();
		cmp.addTool(tool);		
		cmp.setGeometry(Primitives.icosahedron());
		cmp.setAppearance(new Appearance());
		cmp.getAppearance().setAttribute(CommonAttributes.SMOOTH_SHADING,false);
	    ViewerApp.display(cmp);
	}
}
