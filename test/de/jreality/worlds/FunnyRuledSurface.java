/*
 * Created on Apr 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.worlds;
import de.jreality.geometry.TubeUtility;
import de.jreality.geometry.WingedEdge;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Pn;


/**
 * @author gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FunnyRuledSurface extends AbstractLoadableScene {
	SceneGraphComponent icokit;
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
		//SceneGraphComponent tubes = WingedEdge.createTubesOnEdges(oloid, .05, 8, 8);
		SceneGraphComponent tubes = TubeUtility.sticks(oloid, .05, Pn.EUCLIDEAN);
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

	
	public int getSignature() {
		// TODO Auto-generated method stub
		return Pn.EUCLIDEAN;
	}
	
	public boolean isEncompass()	{return true; }
	public boolean addBackPlane()	{ return true; }
	
	public void setConfiguration(ConfigurationAttributes config) {
		// TODO Auto-generated method stub

	}


}
