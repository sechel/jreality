/*
 * Author	gunn
 * Created on May 30, 2005
 *
 */
package de.jreality.util;

import java.util.Vector;

import charlesgunn.jreality.pick.PickAction;
import charlesgunn.jreality.pick.PickPoint;

import de.jreality.geometry.Primitives;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import junit.framework.TestCase;

/**
 * @author gunn
 *
 */
public class TestWorldPick extends TestCase {

	
	
	public void testWorldPick()	{
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("root");
		SceneGraphComponent world = new SceneGraphComponent();
		world.setName("world1");
		world.setGeometry(new Sphere()); //Primitives.cube()); //
		world.getGeometry().setName("sphere");
		world.setTransformation(new Transformation());
		world.getTransformation().setTranslation(0.0, 0.0, 1.0);
		root.addChild(world);
		world = new SceneGraphComponent();
		world.setName("world2");
		world.setGeometry(Primitives.cube()); //
		world.getGeometry().setName("cube");
		world.setTransformation(new Transformation());
		world.getTransformation().setTranslation(0.0, 0.0, -1.0);
		root.addChild(world);
/*
		charlesgunn.jreality.InteractiveViewer iv = new charlesgunn.jreality.InteractiveViewer(null, root);
		PickAction pa = new PickAction(iv);
		double[] p0 = {2,2,2,1};
		double[] p1 = {-2,-2,-2,1};
		pa.setPickSegment(p0, p1);
		Vector hits = (Vector) pa.visit();
		if (hits != null )	{
			for (int i = 0; i< hits.size(); ++i)	{
				PickPoint thisone = (PickPoint) hits.get(i);
				System.out.println(thisone.toString());				
			}
		}
*/
	}
}
