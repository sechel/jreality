/*
 * Created on Jul 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.worlds;

import java.awt.Color;
import java.io.File;
import java.util.Vector;

import javax.swing.JMenuBar;

import de.jreality.geometry.WingedEdge;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Pn;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;
import discreteGroup.CrystallographicGroup;
import discreteGroup.DiscreteGroup;
import discreteGroup.DiscreteGroupSceneGraphRepresentation;
import discreteGroup.DiscreteGroupUtility;

/**
 * @author weissman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Cell120 extends AbstractLoadableScene {

	int signature;
	public int getSignature() {
		return signature;
	}
	/* (non-Javadoc)
	 * @see de.jreality.portal.WorldMaker#makeWorld()
	 */
	public SceneGraphComponent makeWorld() {
		
		SceneGraphComponent theWorld = SceneGraphUtilities.createFullSceneGraphComponent("world");
		theWorld.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new Color(240, 20,50));
		//String realname = DiscreteGroup.resourceDir+"/groups/120cell.gens";
		DiscreteGroup tg = null;
		String realname;
		boolean fromFile = config.getBool("fromFile");
		if (fromFile)	{
			realname = config.getProperty("discreteGroupResourceDir",DiscreteGroup.resourceDir)+
			config.getProperty("discreteGroupFile","groups/120cell.gens");
			 tg = DiscreteGroupUtility.initFromFile(realname);	
		} else {
			//TODO
			String groupName = config.getProperty("groupName");
			tg = CrystallographicGroup.instanceOfGroup(groupName);
		}
		int maxNumElements = config.getInt("maxNumberElements");
		tg.setMaxNumberElements(maxNumElements);
		signature = tg.getSignature();
		double radius = .02;
		if (signature == Pn.ELLIPTIC) radius = .02;
		else if (signature == Pn.EUCLIDEAN) radius = .05;
		else radius = .01;
		radius = config.getDouble("beamRadius");
		
		double stretchFactor = config.getDouble("stretchFactor");
		
		DiscreteGroupSceneGraphRepresentation theMainRepn = new  DiscreteGroupSceneGraphRepresentation(tg);
		Vector geom = new Vector();
		geom.clear();
		double[] cp = config.getDoubleArray("centerPoint");
		tg.setCenterPoint(cp);
		WingedEdge standardDD = (WingedEdge) DiscreteGroupUtility.calculateDirichletDomain(null, tg);
		SceneGraphComponent scaledDD = SceneGraphUtilities.createFullSceneGraphComponent("scaled Dirichlet Domain");
		scaledDD.getTransformation().setCenter(cp);
		scaledDD.getTransformation().setStretch(stretchFactor);
		scaledDD.setGeometry(standardDD);
//		SoccerBall sb = new SoccerBall();
//		sb.setRefineLevel(1);
//		SceneGraphComponent sgc = sb.makeWorld() ;
//		sgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);
//		sgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW,false);
//		scaledDD.addChild(sgc);
		//System.out.println("Center: "+Rn.toString(tg.getCenterPoint()));
		//scaledDD.getTransformation().setCenter(tg.getCenterPoint());
		Appearance ap = scaledDD.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		SceneGraphComponent theDD =SceneGraphUtilities.createFullSceneGraphComponent("the Dirichlet Domain");
		theDD.addChild(scaledDD);
		ap = theDD.getAppearance();
		//ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new Color(240, 10,10));
		geom.add(theDD);
		SceneGraphComponent tubes = WingedEdge.createBeamsOnEdges(standardDD, radius, 4, 5);
		geom.add(tubes);			
		SceneGraphComponent sgc = SceneGraphUtilities.collectGeometry(geom, null);
		theMainRepn.setWorldNode(sgc);
		theWorld.addChild(theMainRepn.getRepresentationRoot());
		return theWorld;
	}

	ConfigurationAttributes config = null;
	String configResourceDir = "/homes/geometer/gunn/Software/eclipse/workspace/jReality/test/de/jreality/worlds/";
	/* (non-Javadoc)
	 * @see de.jreality.portal.WorldMaker#setConfiguration(de.jreality.portal.util.Configuration)
	 */
	public void setConfiguration(ConfigurationAttributes config) {
		File f = new File(configResourceDir+"Cell120.props");
		this.config = new ConfigurationAttributes(f, config);
	}

	
	public void customize(JMenuBar menuBar, Viewer viewer) {
		CameraUtility.getCameraNode(viewer).getTransformation().setMatrix(Rn.identityMatrix(4));
		CameraUtility.getCameraNode(viewer).getTransformation().setSignature(getSignature());
		CameraUtility.getCamera(viewer).setSignature(getSignature());
		CameraUtility.getCamera(viewer).reset();

	}
}
