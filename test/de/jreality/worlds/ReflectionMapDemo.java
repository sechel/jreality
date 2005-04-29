/*
 * Created on Mar 2, 2005
 *
 */
package de.jreality.worlds;
import de.jreality.geometry.SphereHelper;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.ReflectionMap;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Texture2D;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author Charles Gunn
 *
 */
public class ReflectionMapDemo extends AbstractJOGLLoadableScene {

	static String resourceDir = "./";
	static {
		String foo = System.getProperty("resourceDir");
		if (foo != null)	resourceDir  = foo;
	}
	//static String resourceDir = "/Users/gunn/Library/Textures/";

	Texture2D[] faceTex;
	/**
	 * 
	 */
	public ReflectionMapDemo() {
		super();
	}

	static double[][] square = {{-1,-1,0},{1,-1,0},{1,1,0},{-1,1,0}};
	static double[][] texc = {{0,0},{1,0},{1,1},{0,1}};

	public boolean isEncompass()	{ return true; }
	public boolean addBackPlane()	{ return false; }
	String configResourceDir = "/homes/geometer/gunn/Software/eclipse/workspace/jReality/";
	
	public SceneGraphComponent makeWorld() {

		SceneGraphComponent world = SceneGraphUtilities.createFullSceneGraphComponent("reflectionMap");
		SceneGraphComponent child = SceneGraphUtilities.createFullSceneGraphComponent("child");
		child.setGeometry(SphereHelper.tessellatedIcosahedra[4]);
		child.getTransformation().setStretch(1.0, 0.6, 1.3);
		world.addChild(child);
		String[] texNameSuffixes = {"rt","lf","up", "dn","bk","ft"};
		ReflectionMap refm = ReflectionMap.reflectionMapFactory("textures/desertstorm/desertstorm_", texNameSuffixes, "JPG");
		world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"reflectionMap", refm);
		world.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);
		return world;
	}
}

