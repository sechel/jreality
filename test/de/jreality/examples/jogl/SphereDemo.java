/*
 * Created on Sep 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.examples.jogl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.jogl.shader.DefaultMaterialShader;
import de.jreality.jogl.shader.SimpleJOGLShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SphereDemo extends InteractiveViewerDemo {

	boolean useLOD = false;
	
	public JMenuBar createMenuBar()	{
		theMenuBar = super.createMenuBar();
		JMenu testM = new JMenu("Actions");
		ButtonGroup bg = new ButtonGroup();
		final JCheckBoxMenuItem jcc = new JCheckBoxMenuItem("Use level of detail");
		jcc.setSelected(useLOD);
		testM.add(jcc);
		jcc.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				useLOD = !useLOD;
				setLOD();
			}
		});
		theMenuBar.add(testM);
		return theMenuBar;
	}
	
	private void setLOD()	{
		Appearance ap = viewer.getSceneRoot().getAppearance();
		if (ap == null) return;
		double lod = 0.0;
		if (useLOD) lod = 1.0;
		ap.setAttribute(CommonAttributes.LEVEL_OF_DETAIL, lod);
	}
	
	/* (non-Javadoc)
	 * @see de.jreality.jogl.InteractiveViewerDemo#makeWorld()
	 */
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtilities.createFullSceneGraphComponent("world");
		//SimpleJOGLShader sh = new SimpleJOGLShader("SimpleJOGLVertexShader.txt", "SimpleJOGLFragmentShader.txt");
		//world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"useGLShader", true);
		//world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"GLShader", sh);
		
		for (int i = 0; i<6; ++i)	{
			SceneGraphComponent c = SceneGraphUtilities.createFullSceneGraphComponent("sphere"+i);
			c.setGeometry(new Sphere());
			double angle = (2 * Math.PI * i)/6.0;
			c.getTransformation().setTranslation(3 * Math.cos(angle), 3*Math.sin(angle), 0.0);
			float g = (float) (i/5.0);
			c.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new java.awt.Color(g,g,g));
			world.addChild(c);
		}
		return world;
	}
	

	public boolean addBackPlane() {
		
		return false;
	}
	public static void main(String[] args) {
		SphereDemo test = new SphereDemo();
		test.begin();
	}
}
