/*
 * Created on Apr 15, 2004
 *
 */
package de.jreality.examples.jogl;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.reader.Parser3DS;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;


/**
 * @author gunn
 *
 */
public class Read3DSDemo extends InteractiveViewerDemo {
	SceneGraphComponent icokit;
	/**
	 * 
	 */
	public Read3DSDemo() {
		super();
	}

	public SceneGraphComponent makeWorld()	{
		Logger.getLogger("de.jreality").setLevel(Level.WARNING);
		SceneGraphComponent s1 = Parser3DS.readFromFile("face.3ds");
		SceneGraphComponent theWorld = new SceneGraphComponent();
		s1.setTransformation(new Transformation());
		//s1.getTransformation().setStretch(3e-04);
		//s1.getTransformation().setTranslation(-9.5,-7.5,0);

		theWorld.setName("world");
		theWorld.addChild(s1);
		theWorld.setAppearance(new Appearance());
		
		//viewer.getCamera().setNear(.01);
		//viewer.getCamera().setFar(1000.0);
		return theWorld;
	}
	
	public boolean isEncompass()	{return true; }

	public static void main(String argv[])	{
		Read3DSDemo test = new Read3DSDemo();
		test.begin();
	}
	

}
