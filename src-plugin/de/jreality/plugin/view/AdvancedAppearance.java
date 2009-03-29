package de.jreality.plugin.view;

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;

import javax.swing.JButton;

import de.jreality.scene.Appearance;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class AdvancedAppearance extends ShrinkPanelPlugin implements ActionListener {

	private AlignedContent
		content = null;
	private JButton
		activateButton = new JButton("Activate Shader");
	
	public AdvancedAppearance() {
		setInitialPosition(SHRINKER_RIGHT);
		shrinkPanel.add(activateButton);
		activateButton.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Appearance app = content.getAppearanceComponent().getAppearance();
		try {
			makeNormalMap(app);
		} catch (Exception e1) {
			System.out.println("Could not create normal map: " + e1.getMessage());
		}
		DefaultGeometryShader dgs = (DefaultGeometryShader)ShaderUtility.createDefaultGeometryShader(app, true);
		dgs.createPolygonShader("glsl");
		try {
			
			InputStream vIn = getClass().getResourceAsStream("data/vertexProgramTest.glsl");
			InputStream fIn = getClass().getResourceAsStream("data/fragmentProgramTest.glsl");
			Input vertexIn = Input.getInput("vertex shader", vIn);
			Input fragmentIn = Input.getInput("fragment shader", fIn);
			GlslProgram p = new GlslProgram(app, POLYGON_SHADER, vertexIn, fragmentIn);
			p.setUniform("diffuseMap", 0);
			p.setUniform("normalMap", 1);
		} catch (Exception ex) {
			System.out.println("Cannot load shader: " + ex.getMessage());
		}
	}
	
	private void makeNormalMap(Appearance app) throws Exception {
		InputStream nIn = getClass().getResourceAsStream("data/normalMapTest.jpg");
		Input normalIn = Input.getInput("normal map", nIn);
		ImageData data = ImageData.load(normalIn);
		TextureUtility.createTexture(app, POLYGON_SHADER, 1, data);
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		content = c.getPlugin(AlignedContent.class);
	}
	
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Advanced Appearance", "Stefan Sechelmann");
	}

}
