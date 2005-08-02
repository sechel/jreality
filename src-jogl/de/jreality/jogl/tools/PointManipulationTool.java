/*
 * Created on Aug 18, 2004
 *
 */
package de.jreality.jogl.tools;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Level;

import javax.swing.KeyStroke;

import de.jreality.geometry.Primitives;
import de.jreality.jogl.HelpOverlay;
import de.jreality.jogl.InteractiveViewer;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.scene.pick.PickPoint;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.math.*;


/**
 * @author gunn
 *
 */
public class PointManipulationTool extends AbstractMouseTool implements TransformationListener {
	PickPoint thePoint;
  
  SceneGraphPathObserver pickPathObserver;
  
	SceneGraphComponent theRepn, theTransKit, frameIconKit, theScaleKit;
	double[] stretch;
	double[] origMatrix;
	double transStep = 0.1;
	double iconScale = 0.1;
	Rotator	theRotator;
	SceneGraphPath pathToIcon;
	MouseManipulationListener mml = null;
	private double scaleFactor;

	PointManipulationTool()	{
		super();
    pickPathObserver=new SceneGraphPathObserver();
    pickPathObserver.addTransformationListener(this);
		mml = new MouseManipulationListener();
	}
	
	public boolean startTrackingAt(MouseEvent e) {
		
		super.startTrackingAt(e);
		if (theViewer.getSelectionManager().getPickPoint() != null) 
			thePoint = theViewer.getSelectionManager().getPickPoint();
		if (thePoint == null) return false;
		// TODO this needs to be calculated based on screen extent
		frameIconKit.getTransformation().setStretch(.1);	
		theTransKit.getTransformation().setTranslation(thePoint.getPointObject());
		theRepn.getTransformation().setMatrix(thePoint.getPickPath().getMatrix(null));
		stretch = theScaleKit.getTransformation().getStretch();
		origMatrix = frameIconKit.getTransformation().getMatrix();
		
		if (theRotator == null) 		theRotator = new Rotator();
		theRotator.setCamera(CameraUtility.getCamera(theViewer));
		theRotator.setAnchor(anchor);
		Graphics3D gc = new Graphics3D(theViewer);
		gc.setObjectToWorld(pathToIcon.getMatrix(null));
		//TODO have the rotator handle this stuff
		theRotator.setObjectToCamera(gc.getObjectToCamera());
	
		return true;
	}
	
	public boolean track(MouseEvent e) {
		super.track(e);
		if (thePoint == null)	return false;
	
		int which = 0;
		if (e.getButton() == MouseEvent.NOBUTTON)	{
			if (e.isAltDown())		which = 1;
			else if (e.isMetaDown()) which = 2;
		}
		if (e.getButton() == MouseEvent.BUTTON1) which = 0;
		else if (e.getButton() == MouseEvent.BUTTON2) which = 1;
		else //if (e.getButton() == MouseEvent.BUTTON3) 
			which = 2;
		
		Quaternion q = null;
		switch(which)	{
			case 0:
				q = theRotator.getRotationXY(current);
				break;
			case 1:
				scaleFactor = Math.exp(diff[1] * .1);			
				theScaleKit.getTransformation().setStretch(Rn.times(null, scaleFactor, stretch));
				break;
			case 2:
				q = theRotator.getRotationZ(current);
				break;
		}

		if (which == 0 || which == 2)		{
			double[] rot = Quaternion.quaternionToRotationMatrix(null, q);
			frameIconKit.getTransformation().setMatrix(Rn.times(null, origMatrix, rot));
		}
		theViewer.render();
		return true;
	}
	public boolean endTracking(MouseEvent e) {
		
		super.endTracking(e);
		if (thePoint == null)	return false;
		return true;
	}
	
	static double[][] axisVerts = {{-1,0,0},{1,0,0},{0,-1,0},{0,1,0},{0,0,-1},{0,0,1}};
	static int[][] indices = {{0,1},{2,3},{4,5}};
	
  public void transformationMatrixChanged(TransformationEvent ev) {
  		theRepn.getTransformation().setMatrix(ev.getTransformationMatrix());
	}

	public boolean attachToViewer(InteractiveViewer v) {
		super.attachToViewer(v);
		thePoint = theViewer.getSelectionManager().getPickPoint();
		if (thePoint == null) return false;
		pickPathObserver.setPath(thePoint.getPickPath());
		if (theRepn == null) 		{
			//JOGLConfiguration.theLog.log(Level.FINE,"Attaching to viewer");
			frameIconKit = SceneGraphUtility.createFullSceneGraphComponent("sphere");
			//theSphereKit.setGeometry(SphereHelper.SPHERE_FINE);
			frameIconKit.addChild(Primitives.cameraIcon(1.0));
			SceneGraphComponent sph = Primitives.sphere(.1, null);
			sph.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.FACE_DRAW, true);
			frameIconKit.addChild(sph);
			frameIconKit.getTransformation().setStretch(iconScale);	
			frameIconKit.getTransformation().setDefaultMatrix();
			theScaleKit = SceneGraphUtility.createFullSceneGraphComponent("scaleKit");
			theScaleKit.addChild(frameIconKit);
			IndexedLineSet axes = new IndexedLineSet(6,3);
			axes.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(axisVerts));
			axes.setEdgeAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.array(2).createReadOnly(indices));
			theScaleKit.setGeometry(axes);
			theTransKit = SceneGraphUtility.createFullSceneGraphComponent("transKit");
			theTransKit.addChild(theScaleKit);
			theRepn = SceneGraphUtility.createFullSceneGraphComponent("pointManipulation");
			theRepn.addChild(theTransKit);
			pathToIcon = new SceneGraphPath();
			pathToIcon.push(theRepn);
			pathToIcon.push(theTransKit);
			pathToIcon.push(theScaleKit);
			pathToIcon.push(frameIconKit);
		}
		// fill in the variable parts
		// TODO this needs to be calculated based on screen extent
		theTransKit.getTransformation().setTranslation(thePoint.getPointObject());
		theRepn.getTransformation().setMatrix(thePoint.getPickPath().getMatrix(null));
		frameIconKit.getTransformation().resetMatrix();
		
		theViewer.addAuxiliaryComponent(theRepn);
		theViewer.render();
		theViewer.getViewingComponent().addKeyListener(mml);
		return true;
	}
	
	public boolean detachFromViewer() {
		pickPathObserver.dispose();
		theViewer.removeAuxiliaryComponent(theRepn);
		theViewer.getViewingComponent().removeKeyListener(mml);
		super.detachFromViewer();
		return true;
	}
	/**
	 * @author Charles Gunn
	 *
	 */
	public class MouseManipulationListener extends KeyAdapter {
		private boolean showHelp = false;
		
		HelpOverlay helpOverlay;	
		int value = 0;
		double[] trans = new double[3];
		/**
		 * 
		 */
		public MouseManipulationListener() {
			super();
			
			helpOverlay = new HelpOverlay(theViewer);
			helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_1,0), "Encompass");
		}

		public void keyPressed(KeyEvent e)	{
				double foo = transStep* scaleFactor * iconScale;
				Rn.setToValue(trans,0.0);
				switch(e.getKeyCode())	{

					case KeyEvent.VK_LEFT:	
						trans[0] = -foo;
						move();
						break;

					case KeyEvent.VK_RIGHT:
						trans[0] = foo;
						move();
				        break;
						
					case KeyEvent.VK_DOWN:
						if (e.isShiftDown()) trans[2] = -foo;
						else trans[1] = -foo;
						move();
				        break;
					
					case KeyEvent.VK_UP:
						if (e.isShiftDown())  trans[2] = foo;
						else trans[1] = foo;
						move();
						break;
				}
				JOGLConfiguration.theLog.log(Level.FINE,"Value is "+value);
			}
		
		private void move()	{
			theTransKit.getTransformation().multiplyOnRight(P3.makeTranslationMatrix(null, trans, Pn.EUCLIDEAN));
			theViewer.render();
		}

	}
	
}
