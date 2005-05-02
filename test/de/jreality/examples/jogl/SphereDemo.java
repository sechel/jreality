/*
 * Created on Sep 15, 2004
 *
*/
package de.jreality.examples.jogl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.SphereHelper;
import de.jreality.jogl.AbstractDeformation;
import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.jogl.shader.SimpleJOGLShader;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.proxy.CopyScene;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author gunn
 *
 */
public class SphereDemo extends InteractiveViewerDemo {

	boolean useLOD = true;
	
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
		//SimpleJOGLShader sh = new SimpleJOGLShader("brick.vert", "brick.frag");//null,"SimpleJOGLFragmentShader-01.txt");
		//world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"useGLShader", true);
		//world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"GLShader", sh);
		world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER, "brick");
		world.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		for (int i = 0; i<6; ++i)	{
		double[] brickSize = new double[2];
		double[] brickPct = new double[2];
		double[] lightPosition = {0,0,4};
			SceneGraphComponent c = SceneGraphUtilities.createFullSceneGraphComponent("sphere"+i);
			c.setGeometry(SphereHelper.tessellatedIcosahedra[i]); //new Sphere()); //
			double angle = (2 * Math.PI * i)/6.0;
			c.getTransformation().setTranslation(3 * Math.cos(angle), 3*Math.sin(angle), 0.0);
			float g = (float) (i/5.0);
			c.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new java.awt.Color(g,g,g));
			c.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, true);
			float r = (float) (i/6.0);
			float b = 1f - r;
			brickSize[0] = .1+r*.5;
			brickSize[1] = .2+r*.3;
			brickPct[0] = .4 + .1*i;
			brickPct[1] = .7 + .05*i;
			lightPosition[0] = i-2.0;
			c.getAppearance().setAttribute("polygonShader.specularCoefficient", (double)(i*.15));
			c.getAppearance().setAttribute("polygonShader.BrickColor", new java.awt.Color(r,0f,b));
			c.getAppearance().setAttribute("polygonShader.MortarColor", new java.awt.Color(b,1f,r));
			c.getAppearance().setAttribute("polygonShader.BrickSize", brickSize);
			System.out.println("Brick size is "+Rn.toString(brickSize));
			c.getAppearance().setAttribute("polygonShader.BrickPct", brickPct);
			c.getAppearance().setAttribute("polygonShader.LightPosition", lightPosition);
			world.addChild(c);
		}
		//SceneGraphComponent flatt = GeometryUtility.flatten(world);
		CopyScene cp = new CopyScene();
		SceneGraphComponent flatt = (SceneGraphComponent) cp.createProxyScene(world);
		AbstractDeformation ad = new AbstractDeformation()		{
			public double[] valueAt(double[] in, double[] out)	{
				if (out == null || out.length != in.length) out = new double[in.length];
				for (int i = 0; i<in.length; ++i)	out[i] = in[i];
				out[1] *= 2;
				out[2] *= .5;
				return out;
			}
		};
		AbstractDeformation.deform(flatt, ad);
		return world;
	}
	

	public boolean addBackPlane() {
		
		return false;
	}
	public boolean isEncompass() {
		
		return true;
	}
	public static void main(String[] args) {
		SphereDemo test = new SphereDemo();
		test.begin();
	}
}
