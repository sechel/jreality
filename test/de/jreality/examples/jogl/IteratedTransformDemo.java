/*
 * Created on Feb 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.examples.jogl;
import de.jreality.geometry.Primitives;
import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;



/**
 * @author gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class IteratedTransformDemo extends InteractiveViewerDemo {
	SceneGraphComponent icokit;
	/**
	 * 
	 */
	public IteratedTransformDemo() {
		super();
		// TODO Auto-generated constructor stub
	}

		public SceneGraphComponent makeWorld()	{
			SceneGraphComponent theRow;
			IndexedFaceSet ico = Primitives.coloredCube();
			icokit = new SceneGraphComponent();
			icokit.setName("theLeaf");
			icokit.setGeometry(ico);
			double a = 0.3;
			double[] tlate = {-a, 0.0, 0.0, 1.0};
			//double[] stretchV = {.5d, .5d, .5d};
			SceneGraphComponent theWorld = new SceneGraphComponent();
			theWorld.setName("theWorld");
			Transformation t = new Transformation();
			t.setRotation(0.1, 1d, 0d, 0d);
			t.setTranslation(tlate);
			t.setStretch(.9,.9,.9);
			IteratedTransform it = new IteratedTransform(t, 50, icokit);
			it.setName("iteratedTransform");
			//it.setIterationCount(40);
			//it.addChild(icokit);
			theWorld.addChild(it);
			theWorld.setAppearance(new Appearance());
			return theWorld;
		}
	
		public static void main(String argv[])	{
			IteratedTransformDemo test = new IteratedTransformDemo();
			test.begin();
		}
	
}
