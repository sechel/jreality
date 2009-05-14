package de.jreality.plugin.view;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import bsh.This;

//import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
//import com.shfarr.ui.plaf.fh.FhLookAndFeel;
//import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;

//import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;
//import de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.swing.JFakeFrameWithGeometry;
import de.jreality.ui.JSliderVR;
//import de.muntjak.tinylookandfeel.TinyLookAndFeel;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;

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

	public static void main(String[] args) throws Exception {
		//UIManager.setLookAndFeel(new TinyLookAndFeel());
		//JFrame.setDefaultLookAndFeelDecorated(true);
		//JFrame.setDefaultLookAndFeelDecorated(true);
		JRViewer v = JRViewer.createViewer();
		v.getController().setManageLookAndFeel(false);
		v.registerPlugin(new WindowManager());
		v.startup();
		
		JFrame f = v.getController().getPlugin(WindowManager.class).createFrame("Hello scene window");
		
		f.setLocation(100,500);
		f.getContentPane().setLayout(new FlowLayout());
		f.getContentPane().add(new JButton("foo 1"));
		f.getContentPane().add(new JButton("foo 2"));
		f.getContentPane().add(new JButton("foo 3"));
		f.getContentPane().add(new JButton("foo 4"));
		f.getContentPane().add(new JButton("foo 5"));
		f.getContentPane().add(new JSliderVR());
		f.pack();
		
		f.setVisible(true);
		
		SideContainerPerspective scp;

	}
}
