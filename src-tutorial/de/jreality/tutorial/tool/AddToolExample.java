package de.jreality.tutorial.tool;

import java.awt.Color;

import de.jreality.geometry.Primitives;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.ScaleTool;
import de.jreality.ui.viewerapp.ViewerApp;

public class AddToolExample {

	public static void main(String[] args) {
		SceneGraphComponent cmp = new SceneGraphComponent();
		Appearance ap = new Appearance();
		cmp.setAppearance(ap);
		setupAppearance(ap);
		ScaleTool tool= new ScaleTool();
		cmp.addTool(tool);		
		cmp.setGeometry(Primitives.icosahedron());
	    ViewerApp.display(cmp);
	}
	private static void setupAppearance(Appearance ap) {
		DefaultGeometryShader dgs;
		DefaultLineShader dls;
		DefaultPointShader dpts;
		dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		dls = (DefaultLineShader) dgs.createLineShader("default");
		dls.setDiffuseColor(Color.yellow);
		dls.setTubeRadius(.03);
		dpts = (DefaultPointShader) dgs.createPointShader("default");
		dpts.setDiffuseColor(Color.red);
		dpts.setPointRadius(.05);
	}
}
