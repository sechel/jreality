/*
 * Created on Jan 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.examples.jogl;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLDrawable;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.TubeUtility;
import de.jreality.jogl.HelpOverlay;
import de.jreality.jogl.InfoOverlay;
import de.jreality.jogl.InteractiveViewer;
import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.reader.OOGLReader;
import de.jreality.scene.Appearance;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.pick.PickPoint;
import de.jreality.util.P3;
import de.jreality.util.Pn;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SnowClipper extends InteractiveViewerDemo {

	public SnowClipper()	{
		super();
	}
	SceneGraphPath toClipPlane, toClipPlane2, toSculpture, selectionList[];
	SceneGraphComponent snowSculpture, clipPlaneJiggler, clipPlaneJiggler2 ;
	InfoOverlay iolay;
	Vector infoStrings;
	double[] clippingPlane, clippingPlane2, pickPoint;
	static String fileToLoad = "/homes/geometer/gunn/Documents/Models/snowSculpture/taperedscu.off";
	static double snowSculptureScale = 25.0;
	static {
		String foo = System.getProperty("snowSculptureFile");
		if (foo != null) fileToLoad = foo;
		foo = System.getProperty("snowSculptureScale");
		if (foo != null) snowSculptureScale = Double.parseDouble(foo);
	}
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtilities.createFullSceneGraphComponent("snowClipperWorld");
		OOGLReader or = new OOGLReader();
		SceneGraphComponent manipulator = SceneGraphUtilities.createFullSceneGraphComponent();
		world.addChild(manipulator);
		snowSculpture = or.readFromFile(fileToLoad);
		snowSculpture.setName("snowSculptureModel");
		manipulator.addChild(snowSculpture);
		snowSculpture.setTransformation(new Transformation());
		snowSculpture.getTransformation().setStretch(snowSculptureScale);		// (-.2,.2) -> (-60, 60)" 
		snowSculpture.getTransformation().setTranslation(0,0,72);
		snowSculpture.getTransformation().setIsEditable(false);
		
		if (snowSculpture.getAppearance() == null)  snowSculpture.setAppearance(new Appearance());
		snowSculpture.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, false);
		snowSculpture.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		
		 clipPlaneJiggler =  SceneGraphUtilities.createFullSceneGraphComponent("Plane1");
		Appearance ap1 = clipPlaneJiggler.getAppearance();
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap1.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		double[][] vv = {{-1,-1,0},{1,-1,0},{1,1,0},{-1,1,0}};
		IndexedFaceSet square = GeometryUtility.constructPolygon(vv);
		clipPlaneJiggler.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.75);
		clipPlaneJiggler.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		clipPlaneJiggler.getTransformation().setTranslation(0,0,120);
		clipPlaneJiggler.getTransformation().setStretch(160);
		
		//boolean 
//		SceneGraphComponent cp  =  SceneGraphUtilities.createFullSceneGraphComponent("fineAdjustment1");
		clipPlaneJiggler.setGeometry(square);
//		cp.addChild(clipPlaneJiggler);
//		clipPlaneJiggler.getTransformation().setIsEditable(false);
		world.addChild(clipPlaneJiggler);
		//world.getTransformation().setRotation(-Math.PI/2, 1,0,0);

		clipPlaneJiggler2 =  SceneGraphUtilities.createFullSceneGraphComponent("Plane2");
		ap1 = clipPlaneJiggler2.getAppearance();
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap1.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
		clipPlaneJiggler2.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.75);
		clipPlaneJiggler2.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		clipPlaneJiggler2.getTransformation().setTranslation(0,0,60);
		clipPlaneJiggler2.getTransformation().setStretch(160);
//	    clipPlaneJiggler2 =  SceneGraphUtilities.createFullSceneGraphComponent("fineAdjustment2");
		clipPlaneJiggler2.setGeometry(square);
//		clipPlaneJiggler2.addChild(cp);
//		clipPlaneJiggler2.getTransformation().setIsEditable(false);
		world.addChild(clipPlaneJiggler2);

		world.getTransformation().setCenter(new double[] {0,0,72});
		manipulator.addChild(GeometryUtility.clippingPlane(new double[]{1,0,0,-60}));
		manipulator.addChild(GeometryUtility.clippingPlane(new double[]{-1,0,0,-60}));
		manipulator.addChild(GeometryUtility.clippingPlane(new double[]{0,1,0,-60}));
		manipulator.addChild(GeometryUtility.clippingPlane(new double[]{0,-1,0,-60}));
		manipulator.addChild(GeometryUtility.clippingPlane(new double[]{0,0,1,-144}));
		manipulator.addChild(GeometryUtility.clippingPlane(new double[]{0,0,-1,-4}));
		
		SceneGraphComponent frame = SceneGraphUtilities.createFullSceneGraphComponent("frame");
		frame.getTransformation().setStretch(59,59,71);
		frame.getTransformation().setTranslation(0,0,72);
		IndexedFaceSet tet = Primitives.cube();
		frame.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
		frame.addChild(TubeUtility.ballAndStick(tet,.01,.01, null, null));
		manipulator.addChild(frame);

		SceneGraphPath toWorld = new SceneGraphPath();
		toWorld.push(viewer.getSceneRoot());
		toWorld.push(world);

		toSculpture = new SceneGraphPath();
		toSculpture.push(viewer.getSceneRoot());
		toSculpture.push(world);
		toSculpture.push(manipulator);

		toClipPlane = new SceneGraphPath();
		toClipPlane.push(viewer.getSceneRoot());
		toClipPlane.push(world);
//		toClipPlane.push(cp);
		toClipPlane.push(clipPlaneJiggler);
		toClipPlane2 = new SceneGraphPath();
		toClipPlane2.push(viewer.getSceneRoot());
		toClipPlane2.push(world);
//		toClipPlane.push(cp);
		toClipPlane2.push(clipPlaneJiggler2);
		
		selectionList = new SceneGraphPath[4];
		selectionList[0] = toWorld;
		selectionList[1] = toSculpture;
		selectionList[2] = toClipPlane;
		selectionList[3] = toClipPlane2;
		
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new java.awt.Color(60, 60, 60));
		viewer.getSelectionManager().getPickPointAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, .005);
		viewer.getSelectionManager().setRenderSelection(true);
		viewer.getViewingComponent().addKeyListener(new KeyAdapter()	{
			
		    double scaleFactor = .005;
		    int selection = 0;
			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					System.out.println("	6:  toggle info display");
					System.out.println("	7:  increase plane movement increment");
					System.out.println("shift-7:  decrease plane movement increment");
					System.out.println("	8:  dump plane info to stdout");
					System.out.println("	9:  dump pickpoint info to stdout");
					System.out.println("	0:  cycle through selection list");
					System.out.println("	up/down arrows: move white plane ");
					System.out.println("	left/right arrows: move red plane ");
					break;

				case KeyEvent.VK_0:		// cycle through selection
					selection = (selection + 1) % selectionList.length;
				 	viewer.getSelectionManager().setSelection(selectionList[selection]);
				 	System.out.println("Cycling selection");
				 	viewer.render();
					break;
					
				case KeyEvent.VK_6:
					iolay.setVisible(!iolay.isVisible());
			        break;
					
				case KeyEvent.VK_7:
					if (e.isShiftDown()) scaleFactor *= .75;
					else scaleFactor *= 1.3333;
					System.out.println("Plane movement increment is "+scaleFactor);
			        break;
					
				case KeyEvent.VK_8:
					System.out.println("White plane coordinates: "+Rn.toString(clippingPlane, 6));
					System.out.println("Red   plane coordinates: "+Rn.toString(clippingPlane2, 6));
					break;
					
				case KeyEvent.VK_9:
					if (pickPoint != null) System.out.println("Pick Point coordinates: "+Rn.toString(pickPoint, 6));
					break;

				
				case KeyEvent.VK_UP:
		    			moveZPlane(1, clipPlaneJiggler);
		    			break;
	    			
			    case KeyEvent.VK_DOWN:
		    			moveZPlane(-1, clipPlaneJiggler);
		    			break;
			   
			    case KeyEvent.VK_LEFT:
	    				moveZPlane(-1, clipPlaneJiggler2);
	    				break;
	    			
		    
			    case KeyEvent.VK_RIGHT:
	    				moveZPlane(1, clipPlaneJiggler2);
	    				break;
					}
			}
			/**
			 * @param shift
			 */
			private void moveZPlane(double scale, SceneGraphComponent jiggler) {
				//System.out.println("Moving the plane");
	    			double[] shift = {0,0,scale*scaleFactor};
				double[] movePlaneInZ = P3.makeTranslationMatrix(null, shift, Pn.EUCLIDEAN);
				//jiggler.getTransformation().setIsEditable(true);
				jiggler.getTransformation().multiplyOnRight(movePlaneInZ);
				//jiggler.getTransformation().setIsEditable(false);
				viewer.render();
			}

		});
		iolay = new InfoOverlay((InteractiveViewer) viewer);
		iolay.setVisible(true);
		if ((viewer.getViewingComponent() instanceof GLCanvas))
			((GLDrawable) viewer.getViewingComponent()).addGLEventListener(iolay);	 		
 		infoStrings = new Vector();
 		iolay.setInfoStrings(infoStrings);
	
		javax.swing.Timer followTimer = new javax.swing.Timer(200, new ActionListener()	{
			public void actionPerformed(ActionEvent e) {updateInfoOverlay(); }
		} ) ;
		followTimer.start();
		
		return world;
	}
		private void updateInfoOverlay() {
			infoStrings.clear();
			//infoStrings.add("Realtime data");
			calculateClippingPlanes();
			if (clippingPlane != null) infoStrings.add("White clipping plane:    "+Rn.toString(clippingPlane, 7));
			if (clippingPlane2 != null) infoStrings.add("Red clipping plane:    "+Rn.toString(clippingPlane2, 7));
			PickPoint pickPoint2 = viewer.getSelectionManager().getPickPoint();
			if (pickPoint2 != null)  {
				SceneGraphPath sgp = pickPoint2.getPickPath();
				pickPoint = pickPoint2.getPointObject();
				if (sgp.getLastComponent() == snowSculpture) {
					if (pickPoint != null )  {
						pickPoint = Rn.matrixTimesVector(null, snowSculpture.getTransformation().getMatrix(), pickPoint);
						infoStrings.add("Sculpture pick point:    "+Rn.toString(pickPoint, 7));
					}					
				} //else infoStrings.add("Pick point:    "+Rn.toString(pickPoint, 7));
			}
			infoStrings.add("Framerate: "+Double.toString(viewer.getRenderer().getFramerate()));
			infoStrings.add("Time: "+System.currentTimeMillis());
			viewer.render();
		}
		/**
		 * @param zdir
		 */
		private void calculateClippingPlanes() {
			clippingPlane = calculateClippingPlane(toClipPlane);
			clippingPlane2 = calculateClippingPlane(toClipPlane2);
		}
		
	/**
		 * 
		 */
		private double[] calculateClippingPlane(SceneGraphPath clipPlanePath) {
			double[] zdir = {0,0,1,0};
			
			double[] toS = toSculpture.getInverseMatrix(null);
			double[] fromC = clipPlanePath.getMatrix(null);
			double[] cToS = Rn.times(null, toS, fromC);
			double[] cToSN = Rn.transpose(null, Rn.inverse(null, cToS));
//			double tmp = cToSN[1]; cToSN[1] = cToSN[4]; cToSN[4] = tmp;
//			tmp = cToSN[2]; cToSN[2] = cToSN[8]; cToSN[8] = tmp;
//			tmp = cToSN[6]; cToSN[6] = cToSN[9]; cToSN[9] = tmp;
//			System.out.println("CtoS\n"+Rn.matrixToString(cToSN));
			double[] clippingPlane = Rn.matrixTimesVector(null, cToSN, zdir);
			Pn.normalizePlane(clippingPlane, clippingPlane);
			return clippingPlane;
		}
	public boolean addBackPlane() { return false; }
	
	public static void main(String[] args) {
		SnowClipper test = new SnowClipper();
		test.begin();
	}

}
