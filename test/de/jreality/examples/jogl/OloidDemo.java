/*
 * Created on Apr 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.examples.jogl;
import de.jreality.geometry.TubeUtility;
import de.jreality.geometry.WingedEdge;
import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.scene.*;


/**
 * @author gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class OloidDemo extends InteractiveViewerDemo {
	SceneGraphComponent icokit;
	/**
	 * 
	 */
	public OloidDemo() {
		super();
	}
	public SceneGraphComponent makeWorld()	{
		
		
		WingedEdge oloid = new WingedEdge(3.0d);
		int num = 50;
		for (int i = 0; i<=num; ++i)  	{
			double angle = 2.0 * Math.PI * ( i/((double) num));
			double[] plane = {Math.cos(angle), Math.sin(angle), .5 * Math.cos(2*angle), -1d};
			oloid.cutWithPlane(plane);
		} 
		oloid.update();
		SceneGraphComponent oloidkit = new SceneGraphComponent();
		//oloidkit.addChild(oloid);
		oloidkit.setGeometry(oloid);
		SceneGraphComponent tubes = WingedEdge.createTubesOnEdges(oloid, .05, 8, 8);
		//SceneGraphComponent tubes = TubeUtility.createTubesOnEdges(oloid, .05);
		oloidkit.addChild(tubes);
		
		//SceneGraphComponent s1 = Parser3DS.readFromFile("/homes/geometer/gunn/tmp/read3DS/models/space011.3ds");
		SceneGraphComponent theWorld = new SceneGraphComponent();
		theWorld.setName("world");
		Transformation tt = new Transformation();
		tt.setRotation(Math.PI/3.0,1d,0d,0d);
		tt.setTranslation(1d,1d,0d);
		//theWorld.addTransform(tt);
		theWorld.addChild(oloidkit);
		//theWorld.addChild(s1);
		theWorld.setAppearance(new Appearance());
		return theWorld;
	}

	public static void main(String argv[])	{
		OloidDemo test = new OloidDemo();
		test.begin();
	}


}
