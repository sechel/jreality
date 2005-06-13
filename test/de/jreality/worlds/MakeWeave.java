 /*
  * Created on May 12, 2004
  *
  */
 package de.jreality.worlds;
 import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.QuadMeshUtility;
import de.jreality.geometry.TubeUtility;
import de.jreality.jogl.inspection.FancySlider;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Texture2D;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.MatrixBuilder;
import de.jreality.util.P3;
import de.jreality.util.Pn;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;

 /**
  * @author Charles Gunn
  *
  */
 public class MakeWeave extends AbstractJOGLLoadableScene {

 	static double x = 1;
 	static double[][] circle =  {{x, 0, 0, 1},	 {x, 1,0, 1}, {0,2, 0, 2}, {-x, 1,0, 1}, {-x, 0, 0, 1},{-x, -1,0, 1},{0,-2, 0, 2},{x, -1,0, 1},{x, 0, 0, 1}};	
     static double yval = .5;
 	static double[][] form = {{0,yval,0.0},{1,yval,1.5},{2,yval,0}};
  	SceneGraphComponent geometryHome = null;
 	QuadMeshShape qmpatch = null;
 	int refineLevel = 3;
 	double radius = 0.4;
 	public SceneGraphComponent makeWorld() {
  	   SceneGraphComponent world = SceneGraphUtilities.createFullSceneGraphComponent("world");
  	   SceneGraphComponent copies5 = SceneGraphUtilities.createFullSceneGraphComponent("copies5");
	   SceneGraphComponent order4 = SceneGraphUtilities.createFullSceneGraphComponent("order4");
	   SceneGraphComponent order2 = SceneGraphUtilities.createFullSceneGraphComponent("order20");
	   SceneGraphComponent order20 = SceneGraphUtilities.createFullSceneGraphComponent("order20");
	   SceneGraphComponent order21 = SceneGraphUtilities.createFullSceneGraphComponent("order21");
	   geometryHome = SceneGraphUtilities.createFullSceneGraphComponent("geometryHome ");
	   geometryHome.getTransformation().setTranslation(-.5,0,-.75);
	   order20.addChild(geometryHome);
 	   order21.addChild(geometryHome);
	   order21.getTransformation().setRotation(Math.PI,0,1,0);
 	   order2.addChild(order20);
	   order2.addChild(order21);
	   for (int i = 0; i<4; ++i)	{
	 	   SceneGraphComponent sgc = SceneGraphUtilities.createFullSceneGraphComponent("order4"+i);
		   sgc.getTransformation().setRotation(i*Math.PI/2,0,0,1);
		   sgc.addChild(order2);
		   order4.addChild(sgc);
	   }
	   double[][] tlates = {{0,0,0},{2,0,0},{0,2,0},{-2,0,0},{0,-2,0}};
	   for (int j = 0; j<5; ++j)	{
	 	   SceneGraphComponent sgc = SceneGraphUtilities.createFullSceneGraphComponent("tlatexy"+j);
		   sgc.getTransformation().setTranslation(tlates[j]);
		   sgc.addChild(order4);
		   copies5.addChild(sgc);
	   }
	   world.addChild(copies5);
		Texture2D tex2d = null;
		try {
			tex2d = new Texture2D(Readers.getInput("textures/weaveRGBABright.png"));
			world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"texture2d",tex2d);
		} catch (IOException e) {
			e.printStackTrace();
		}
		double[] mat = new double[16];
		MatrixBuilder.euclidian().rotate(Math.PI/4.0,0,0,1).scale(8,8,1).assignTo(mat);
		tex2d.setTextureMatrix(mat);
	   
 	   Appearance ap1 = world.getAppearance();
 	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
 	   ap1.setAttribute(CommonAttributes.SPECULAR_EXPONENT, 100.0);
 	   ap1.setAttribute(CommonAttributes.SPECULAR_COEFFICIENT, 0.1);
 	   ap1.setAttribute(CommonAttributes.DIFFUSE_COEFFICIENT, 1.0);
  	   ap1.setAttribute(CommonAttributes.EDGE_DRAW, false);
 	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH,1.0);

 	   double[][][] tubePoints = TubeUtility.makeTubeAsBezierPatchMesh(form, radius, circle, TubeUtility.PARALLEL,false, Pn.EUCLIDEAN);
 	   	BezierPatchMesh bpm = new BezierPatchMesh(2, 3, tubePoints);
 	   	for (int j = 1; j<= refineLevel; ++j)	bpm.refine();
 		qmpatch = QuadMeshUtility.representBezierPatchMeshAsQuadMesh(qmpatch, bpm);	   
 		GeometryUtility.calculateAndSetTextureCoordinates(qmpatch);
  		geometryHome.setGeometry(qmpatch);	   
  	  return world;
 	}
 	
 	public void setConfiguration(ConfigurationAttributes config) {
 	}

 	public int getSignature() {
 		return Pn.EUCLIDEAN;
 	}

 	public boolean addBackPlane() {
 		return false;
 	}
 	public boolean isEncompass() {
 		return true;
 	}

	public Component getInspector() {
		Box container = Box.createVerticalBox();
		FancySlider ballCount = new FancySlider.Double("radius", SwingConstants.HORIZONTAL, 0.0, 1.0, 0.5);
	    ballCount.textField.addPropertyChangeListener(new PropertyChangeListener()	{
		    public void propertyChange(PropertyChangeEvent e) {
		        if ("value".equals(e.getPropertyName())) {
		            Number value = (Number)e.getNewValue();
		            if (value != null) {
		                setRadius(value.doubleValue());
		            }
		        }
		    }	       	
	       });
	    //ballCount.setAlignmentX(1.0f);
		container.add(ballCount);
//		return super.getInspector();
		return container;
	}

	/**
	 * @param d
	 */
	protected void setRadius(double d) {
		   	double[][][] tubePoints = TubeUtility.makeTubeAsBezierPatchMesh(form, d, circle, TubeUtility.PARALLEL,false, Pn.EUCLIDEAN);
 	   		BezierPatchMesh bpm = new BezierPatchMesh(2, 3, tubePoints);
 		   	for (int j = 1; j<= refineLevel; ++j)	bpm.refine();
 		   	qmpatch = QuadMeshUtility.representBezierPatchMeshAsQuadMesh(qmpatch, bpm);	   
	}
	public void customize(JMenuBar menuBar, Viewer viewer) {
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor",java.awt.Color.BLACK);
		CameraUtility.getCamera(viewer).setPerspective(false);
		super.customize(menuBar, viewer);
	}
 }
