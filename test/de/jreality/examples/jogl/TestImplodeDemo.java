/*
 * Created on May 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.examples.jogl;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Torus;
import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestImplodeDemo extends InteractiveViewerDemo {

	/**
	 * 
	 */
	public TestImplodeDemo() {
		super();
	}

	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setTransformation(new Transformation());
		root.setName("theWorld");
		Appearance ap1 = new Appearance();
		root.setAppearance(ap1);
		root.getAppearance().setAttribute("normalScale",0.05);
		for (int i = 0; i< 5; ++i)	{
			Torus torus= new Torus(0.5, 0.3, 20, 30);
			torus.setName("torus"+i);
			GeometryUtility.calculateAndSetNormals(torus);
			SceneGraphComponent globeNode = new SceneGraphComponent();
			globeNode.setName("comp"+i);
			Transformation gt= new Transformation();
			gt.setTranslation(-5.0 + 2.0* i, 0, 0.0);
			globeNode.setTransformation(gt);
			if (i!=0) globeNode.setGeometry(GeometryUtility.implode(torus, -.9 + .4 * i));
			else globeNode.setGeometry(GeometryUtility.truncate(torus));
			ap1 = new Appearance();
			globeNode.setAppearance(ap1);
			root.addChild(globeNode);
		}
		viewer.getCameraPath().getLastComponent().getTransformation().setTranslation(0.0d, 0.0d, 4.0d);
		return root;
	}
 
   public static void main(String argv[])	{
	   TestImplodeDemo test = new TestImplodeDemo();
	   Logger.getLogger("de.jreality").setLevel(Level.INFO);
	   Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);
	   if (argv != null && argv.length > 0)	{
		   Logger.getLogger("de.jreality").log(Level.INFO, "arguments are {0}",argv[0]);
	   }
	   test.begin();
   }
}

