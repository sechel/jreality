package de.jreality.plugin.view;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.UnsupportedLookAndFeelException;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
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
		resX=800,
		resY=600;
	
	private double
		distance=2;
	private double screenWidth=2;
	
	public WindowManager() {
		windowRoot.setAppearance(new Appearance());
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(windowRoot.getAppearance(), false);
		DefaultLineShader dls = (DefaultLineShader) dgs.getLineShader();
		dls.setTubeRadius(1.0);
		windowRoot.setGeometry(IndexedLineSetUtility.createCurveFromPoints(new double[][]{{0,0,0},{resX,0,0},{resX,resY,0},{0,resY,0}}, true));
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
		JFakeFrameWithGeometry f = new JFakeFrameWithGeometry("A fake frame...");
		f.setLocation(100,500);
		windowRoot.addChild(f.getSceneGraphComponent());
		f.setLayout(new FlowLayout());
		f.add(new JButton("foo 1"));
		f.add(new JButton("foo 2"));
		f.add(new JButton("foo 3"));
		f.add(new JButton("foo 4"));
		f.add(new JButton("foo 5"));
		f.add(new JSlider());
		f.pack();
		
		f.setVisible(true);
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		//JFrame.setDefaultLookAndFeelDecorated(true);
		JRViewer v = JRViewer.createViewer();
		v.registerPlugin(new WindowManager());
		v.startup();
	}
}
