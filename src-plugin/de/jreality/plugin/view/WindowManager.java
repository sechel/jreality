package de.jreality.plugin.view;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.swing.JFakeFrameWithGeometry;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class WindowManager extends Plugin {

	SceneGraphComponent windowRoot=new SceneGraphComponent("window root");
	private int
		resX=1024,
		resY=768;
	
	private double
		distance=2;
	private double screenWidth=2;
	
	private boolean showDesktopBorder=false;
	SceneGraphComponent desktopBorder=new SceneGraphComponent("desktop bounds");
	
	public WindowManager() {
		desktopBorder.setAppearance(new Appearance());
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(desktopBorder.getAppearance(), false);
		DefaultLineShader dls = (DefaultLineShader) dgs.getLineShader();
		dls.setTubeRadius(1.0);
		desktopBorder.setGeometry(IndexedLineSetUtility.createCurveFromPoints(new double[][]{{0,0,0},{resX,0,0},{resX,resY,0},{0,resY,0}}, true));
	}
	
	private void updateWindowRootTransformation() {
		MatrixBuilder.euclidean().translate(0,0,-distance).scale(screenWidth/resX).rotateX(Math.PI).translate(-resX/2, -resY/2, 0).assignTo(windowRoot);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("window manager");
	}
	
	@Override
	public void install(Controller c) throws Exception {
		updateWindowRootTransformation();
		c.getPlugin(CameraStand.class);
		c.getPlugin(View.class).getCameraPath().getLastComponent().addChild(windowRoot);
	}

	public JFakeFrameWithGeometry createFrame(String title) {
		JFakeFrameWithGeometry ff = new JFakeFrameWithGeometry(title);
		ff.setDesktopComponent(windowRoot);
		ff.setDesktopWidth(resX);
		ff.setDesktopHeight(resY);
		return ff;
	}
	
	public void setShowDesktopBorder(boolean showDesktopBorder) {
		this.showDesktopBorder = showDesktopBorder;
		windowRoot.removeChild(desktopBorder);
		if (showDesktopBorder) windowRoot.addChild(desktopBorder);
	}

	public boolean getShowDesktopBorder() {
		return showDesktopBorder;
	}

}
