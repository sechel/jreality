/*
 * Created on Apr 15, 2004
 *
 */
package de.jreality.examples.jsyzygy;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.reader.Parser3DS;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;


/**
 * @author gunn
 *
 */
public class Read3DSDemo {
	SceneGraphComponent icokit;
	/**
	 * 
	 */
	public Read3DSDemo() {
		super();
	}
		public static SceneGraphComponent generateScene(/*SceneGraphComponent r, */String filename, double scale)	{
			
			Logger.getLogger("de.jreality").setLevel(Level.WARNING);
			SceneGraphComponent s1 = Parser3DS.readFromFile(filename);
			SceneGraphComponent theWorld = new SceneGraphComponent();
			s1.setTransformation(new Transformation());
//			s1.getTransformation().setTranslation(-25400, -21400 ,-10 );
			//Transformation stretch = new Transformation();
			s1.getTransformation().setStretch(scale);
			s1.getTransformation().setRotation(-Math.PI/3.0,1d,0d,0d);
			//s1.getTransformation().multiplyOnLeft(stretch);



			//SceneGraphComponent s1 = Parser3DS.readFromFile("test/de/jreality/examples/resources/space001.3ds");
			//SceneGraphComponent theWorld = new SceneGraphComponent();
//			theWorld.setName("world");
//			Transformation tt = new Transformation();
//			tt.setRotation(Math.PI/3.0,1d,0d,0d);
//			tt.setTranslation(1d,1d,0d);
//			tt.setStretch(0.05);
//			//s1.setTransformation(tt);
//			theWorld.addChild(s1);
			//s1.setAppearance(new Appearance());
			//r.addChild(theWorld);
			return s1;
		}
	
	
	
		public static void main(String argv[])	{
		    double scale = 1;
		    try {
		        scale = Double.parseDouble(argv[1]);
		    } catch (Exception e) {}
			// Read3DSDemo test = new Read3DSDemo();
			de.jreality.jsyzygy.JsyzygyViewer viewer = de.jreality.jsyzygy.JsyzygyViewer.createInstance();
			viewer.addSceneGraphComponent(Read3DSDemo.generateScene(argv[0], scale));
			viewer.makeSimpleWand(de.jreality.examples.jsyzygy.Wand.makeWandComponent(new SceneGraphComponent()));
			viewer.initSomeLight();
		}
	

}
