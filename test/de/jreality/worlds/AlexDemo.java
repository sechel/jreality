/*
 * Created on Jul 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.worlds;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.quasitiler.alexanderplatz.Alex3DModel;

/**
 * @author weissman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AlexDemo implements LoadableScene {
	
	/* (non-Javadoc)
	 * @see de.jreality.portal.WorldMaker#makeWorld()
	 */
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = new SceneGraphComponent();
		world.setTransformation(new Transformation());
		world.getTransformation().setRotation(-Math.PI/2., 1,0,0);
        SceneGraphComponent sgc = Alex3DModel.createRoot(6, true, true, true);
        SceneGraphComponent scaleComp = new SceneGraphComponent();
        Transformation t = new Transformation();
        t.setStretch(3.);
        scaleComp.setTransformation(t);
        scaleComp.addChild(sgc);
        world.addChild(scaleComp);
        return world;
	}

	/* (non-Javadoc)
	 * @see de.jreality.portal.WorldMaker#getSignature()
	 */
	public int getSignature() {
		// TODO Auto-generated method stub
		return 0;
	}


	ConfigurationAttributes config = null;

	/* (non-Javadoc)
	 * @see de.jreality.portal.WorldMaker#setConfiguration(de.jreality.portal.util.Configuration)
	 */
	public void setConfiguration(ConfigurationAttributes config) {
		this.config = config;
	}

}
