/*
 * Created on Jul 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.worlds;

import java.util.Vector;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.WingedEdge;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.jreality.util.P3;
import de.jreality.util.Pn;
import de.jreality.util.SceneGraphUtilities;
import discreteGroup.DiscreteGroup;
import discreteGroup.jreality.DiscreteGroupUtility;

/**
 * @author weissman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Cell120 implements LoadableScene {
	DiscreteGroup tg;
	static SceneGraphComponent elkit, cubekit;
	WingedEdge standardDD;
	SceneGraphComponent cpkit, theWorld;
	Geometry vs;
	SceneGraphPath dgPath = null;
	SceneGraphComponent sgn, theDD, scaledDD, wire;
	Vector geom;

	public int getSignature() {
		// TODO Auto-generated method stub
		return Pn.ELLIPTIC;
	}
	/* (non-Javadoc)
	 * @see de.jreality.portal.WorldMaker#makeWorld()
	 */
	public SceneGraphComponent makeWorld() {
		theWorld = SceneGraphUtilities.createFullSceneGraphComponent("world");
		geom = new Vector();
			tg = DiscreteGroupUtility.initFromFile("data/resources/120cell.gens");
			tg.setFinite(true);
		tg.setCenterPoint(P3.originP3);
		standardDD = (WingedEdge) DiscreteGroupUtility.calculateDirichletDomain(tg);
		//CameraUtility.getCameraNode(viewer).getTransformation().setMatrix(Rn.identityMatrix(4));
		theWorld.setTransformation(new Transformation(tg.getSignature()));
		//CameraUtility.getCamera(viewer).setSignature(tg.getSignature());
		//CameraUtility.getCamera(viewer).reset();

		//DiscreteGroupViewportConstraint vc = new DiscreteGroupViewportConstraint( tg, 25.0, 5, null, true);
		//tg.setConstraint(vc);

		tg.update();
		theDD = scaledDD = wire = sgn = null;
		//SceneGraphUtilities.setSignature(viewer.getSceneRoot(), tg.getSignature());
		//viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.FAST_AND_DIRTY_ENABLED,true);
		geom.clear();
		if (scaledDD == null || theDD == null)	{
			scaledDD = new SceneGraphComponent();
			scaledDD.setName("scaledDD");
			scaledDD.setTransformation(new Transformation());
			scaledDD.getTransformation().setStretch(.25);
			Appearance ap = new Appearance();
			ap.setAttribute(CommonAttributes.FACE_DRAW, true);
			ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
			scaledDD.setAppearance(ap);
			theDD = new SceneGraphComponent();
			theDD.setName("theDD");
			theDD.addChild(scaledDD);
			ap = new Appearance();
			//ap.setAttribute(CommonAttributes.FACE_DRAW, false);
			ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.GRAY);
			theDD.setAppearance(ap);
		}
		scaledDD.getTransformation().setCenter(tg.getCenterPoint());
		scaledDD.getTransformation().setUseCenter(true);
		scaledDD.getTransformation().setSignature(tg.getSignature());
		Geometry dd = (Geometry) discreteGroup.jreality.DiscreteGroupUtility.calculateDirichletDomain(tg);
		scaledDD.setGeometry( dd);
		if (wire == null) wire = SceneGraphUtilities.createFullSceneGraphComponent("wireDD");
		dd = (Geometry) discreteGroup.jreality.DiscreteGroupUtility.calculateDirichletDomain(tg);
		dd = GeometryUtility.implode((IndexedFaceSet) dd, -.05);
		wire.getTransformation().setStretch(.995);
		wire.setGeometry(dd);
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		wire.setAppearance(ap);
		theDD.addChild(wire);
		geom.add(theDD);
			//System.err.println("Not yet implemented");
		//else geom.add(tg.getDefaultFundamentalRegion());
		sgn =  discreteGroup.jreality.DiscreteGroupUtility.representAsSceneGraph(tg, sgn, geom);
		SceneGraphUtilities.replaceChild(theWorld,sgn);
		return theWorld;
	}

	ConfigurationAttributes config = null;

	/* (non-Javadoc)
	 * @see de.jreality.portal.WorldMaker#setConfiguration(de.jreality.portal.util.Configuration)
	 */
	public void setConfiguration(ConfigurationAttributes config) {
		this.config = config;
	}

	
}
