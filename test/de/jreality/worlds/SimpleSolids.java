/*
 * Created on Jul 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.worlds;

import de.jreality.geometry.WingedEdge;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.jreality.util.Pn;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;
import discreteGroup.TriangleGroup;
import discreteGroup.jreality.DiscreteGroupUtility;

/**
 * @author weissman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SimpleSolids implements LoadableScene {
	SceneGraphComponent theWorld;
	/**
	 * 
	 */
	public int getSignature() {
		// TODO Auto-generated method stub
		return Pn.EUCLIDEAN;
	}
	SceneGraphComponent icokit;
	WingedEdge we;
	static double[][] positions;
	static {
		WingedEdge co = DiscreteGroupUtility.archimedeanSolid("3.4.3.4");
		WingedEdge rd = co.polarize();
		positions = co.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[] stretch = Pn.makeStretchMatrix(null, 4.0);
		Rn.matrixTimesVector(positions, stretch, positions);
	}
	
	public SceneGraphComponent makeWorld()	{
		theWorld = new SceneGraphComponent();
		theWorld.setTransformation(new Transformation());
		theWorld.setAppearance(new Appearance());
		showAll();
		return theWorld;
	}

	static String[] showAllNames = {"3.4.3.4","3.4.4.4","3.4.5.4",
		"3.5.3.5","3.6.6","3.8.8","3.10.10","4.6.6","4.6.8","4.6.10","5.6.6","3.3.3.3.4","3.3.3.3.5"};

	public void showAll()	{
		String[] archNames = TriangleGroup.getArchimedeanNames();
		SceneGraphUtilities.removeChildren(theWorld);
		WingedEdge archie, archieP;
		for (int j = 0; j<13; ++j)	{
			SceneGraphComponent aPair = new SceneGraphComponent();
			aPair.setName("aPair"+j);
			aPair.setTransformation(new Transformation());
			if (j>0) aPair.getTransformation().setTranslation(positions[j-1]);
			archie = DiscreteGroupUtility.archimedeanSolid(showAllNames[j]);
			if (archie == null) continue;
			SceneGraphComponent archkit = new SceneGraphComponent();
			archkit.setTransformation(new Transformation());
			archkit.setGeometry(archie);
			aPair.addChild(archkit);
			archieP = archie.polarize();
			double foo = .1, bar = .4;
			double[][] polarColorMap = {{.2 +  foo + bar * Math.random(),foo + bar *Math.random(), foo + bar *Math.random(), 1.0}};
			archieP.setColormap(polarColorMap);
			SceneGraphComponent archPkit = new SceneGraphComponent();
			archPkit.setTransformation(new Transformation());
			archPkit.setGeometry(archieP);	
			aPair.addChild(archPkit);
			theWorld.addChild(aPair);
		}

	}

	ConfigurationAttributes config = null;

	/* (non-Javadoc)
	 * @see de.jreality.portal.WorldMaker#setConfiguration(de.jreality.portal.util.Configuration)
	 */
	public void setConfiguration(ConfigurationAttributes config) {
		this.config = config;
	}

}
