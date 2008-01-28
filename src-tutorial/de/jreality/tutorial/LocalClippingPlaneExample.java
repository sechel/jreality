package de.jreality.tutorial;

import java.awt.Color;
import java.io.IOException;

import javax.media.j3d.GeometryUpdater;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.jogl.JOGLClippingPlane;
import de.jreality.scene.Viewer;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.TwoSidePolygonShader;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;


public class LocalClippingPlaneExample{
		double[][] vv = {{-1,-1,0},{1,-1,0},{1,1,0},{-1,1,0}};
		double[][] black = {{0,0,0,0}};
		double[][] blacks = {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
		static Viewer viewer;
		Transformation clipTform;
		SceneGraphPath clipper  = null;
		double separation = .2;
		SceneGraphComponent clipPlane1SGC, clipPlane2SGC;
		SceneGraphComponent worldSGC;
		SceneGraphComponent clipIcon1, clipIcon2;

		  public static void main(String[] args) throws IOException {
		  LocalClippingPlaneExample lcpe = new LocalClippingPlaneExample();
		  SceneGraphComponent root = lcpe.makeExample();
	    ViewerApp va = ViewerApp.display(root);
	    viewer = va.getViewer();
	}
	  
	  SceneGraphComponent makeExample()	{
		  	worldSGC = makeWorld();
			SceneGraphComponent sliceBox = sliceBoxForSGC(worldSGC);
			SceneGraphComponent root = new SceneGraphComponent();
			root.setName("world");
			root.addChild(sliceBox);
			root.addTool(new ClickWheelCameraZoomTool());
			return root;
	  }
	  protected SceneGraphComponent makeWorld()	{
			
			SceneGraphComponent world;
			world = SceneGraphUtility.createFullSceneGraphComponent("sphere");
			PickUtility.setPickable(world, false);
			world.addChild(SphereUtility.tessellatedCubeSphere(SphereUtility.SPHERE_SUPERFINE));
			world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"name","twoSide");
			world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER,TwoSidePolygonShader.class);
			world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+".front."+CommonAttributes.DIFFUSE_COLOR, new Color(0,204,204));
			world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+".back."+CommonAttributes.DIFFUSE_COLOR, new Color(204,204,0));
			world.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
			return world;
	  }
	  
	  protected SceneGraphComponent sliceBoxForSGC(SceneGraphComponent world)	{
			ClippingPlane clippingPlane1, clippingPlane2;
			SceneGraphComponent clipPlane2SGC;
			clipIcon1 = SceneGraphUtility.createFullSceneGraphComponent("theClipIcon");
			clipTform = clipIcon1.getTransformation();
			IndexedFaceSet square = IndexedFaceSetUtility.constructPolygon(vv);
			square.setFaceAttributes(Attribute.COLORS,StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(black));
			clipIcon1.setGeometry(square);
			clipIcon1.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .01);
			clipIcon1.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.white);
			clipIcon2 = SceneGraphUtility.createFullSceneGraphComponent("theClipIcon");
			clipIcon2.setGeometry(square);
			MatrixBuilder.euclidean().translate(0,0,-separation).assignTo(clipIcon2);
			clipIcon1.addChild(clipIcon2);
			clipPlane1SGC = SceneGraphUtility.createFullSceneGraphComponent("theClipPlane");
			clipPlane1SGC.setTransformation(clipTform);
			clippingPlane1 = new ClippingPlane();
			clippingPlane1.setLocal(true);
			clipPlane1SGC.setGeometry(clippingPlane1);
			
			clipPlane2SGC = SceneGraphUtility.createFullSceneGraphComponent("theClipPlane");
			clippingPlane2 = new ClippingPlane();
			clippingPlane2.setLocal(true);
			clipPlane2SGC.setGeometry(clippingPlane2);
			MatrixBuilder.euclidean().translate(0,0,-separation).reflect(new double[]{0,0,1,0}).assignTo(clipPlane2SGC);
			clipPlane1SGC.addChild(clipPlane2SGC);
			clipper = new SceneGraphPath(clipPlane1SGC, clipPlane2SGC);
			clipPlane2SGC.addChild(world);
			
			SceneGraphComponent cubeSGC = SceneGraphUtility.createFullSceneGraphComponent("sphere");
			Rectangle3D bbox = GeometryUtility.calculateBoundingBox(world);
			double[] extents = bbox.getExtent();
			IndexedFaceSet cube = Primitives.box(extents[0], extents[1], extents[2],false);
			MatrixBuilder.euclidean().translate(bbox.getCenter()).assignTo(cubeSGC);
			cube.setFaceAttributes(Attribute.COLORS,StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(blacks));
			cubeSGC.setGeometry(cube);
			cubeSGC.addTool(new SimpleDragTool());
			cubeSGC.addChild(clipPlane1SGC);
			cubeSGC.addChild(clipIcon1);
			return cubeSGC;
	  }
	  private final InputSlot pointerSlot = InputSlot.getDevice("PointerTransformation");
	  private final InputSlot activeSlot = InputSlot.getDevice("PrimaryAction");
	  public class SimpleDragTool extends AbstractTool {
		boolean dragging = false, firstTime = false;
		double[] originalCoords;
		double[] originalMat;
		double originald = 0;
		int whichFace;
		int oldIndex0 = -1, oldDirection = -1, index0, index1, index2, sign;
		boolean sameFace = true;
		public SimpleDragTool() {
			super(activeSlot);
			addCurrentSlot(pointerSlot, "triggers drag events");
		}

		public void setSeparation(double d)	{
			separation = d;
			MatrixBuilder.euclidean().translate(0,0,-separation).reflect(new double[]{0,0,1,0}).assignTo(clipPlane2SGC);
			MatrixBuilder.euclidean().translate(0,0,-separation).assignTo(clipIcon2);
		}
		
		public double getSeparation()	{
			return separation;
		}
		@Override
		public void activate(ToolContext tc) {
			super.activate(tc);
			if (tc.getCurrentPick() != null && tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_FACE) {
				dragging = true;
				whichFace = tc.getCurrentPick().getIndex();
				index0 = whichFace % 3;
				index1 = (index0+1)%3;
				index2 = (index0+2)%3;
				sign = -1; //(whichFace < 3) ? -1 : 1;
				originalCoords = tc.getCurrentPick().getObjectCoordinates().clone();
				if (index0 == oldIndex0)	{
					originalMat = clipPlane1SGC.getTransformation().getMatrix();
					sameFace = true;
				} else	{
					originalMat = Rn.identityMatrix(4);
					originald = 0.0;
					sameFace = false;
				}
				oldIndex0 = index0;
				System.err.println("activate "+Rn.toString(originalCoords));
				firstTime = true;
			}
		}

		@Override
		public void deactivate(ToolContext tc) {
			super.deactivate(tc);
			dragging = false;
		}

		@Override
		public void perform(ToolContext tc) {
			if (!dragging || originalCoords == null || tc.getCurrentPick() == null) return;
			if (tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_FACE) {
				double[] newCoords = tc.getCurrentPick().getObjectCoordinates();
				double[] dd = Rn.subtract(null, originalCoords, newCoords);
				int direction = -1;
				if (firstTime)	{
					direction = (Math.abs(dd[index1]) > Math.abs(dd[index2])) ? index1 : index2;
					if (!sameFace || direction != oldDirection)	{
						double[] to = new double[3];
						to[direction] = sign*1;
						double[] zaxis = {0,0,1};
						MatrixBuilder.euclidean().rotateFromTo(zaxis, to).assignTo(originalMat);
						originald = 0.0;
						oldDirection = direction;						
					}
					firstTime = false;
				} else direction = oldDirection;
				double d = sign*dd[direction];
				double[] newCP = new double[4];
				newCP[direction] = -1;
				newCP[3] = d + originald;
//				clippingPlane1.setPlaneEquation(newCP);
				Matrix foo = new Matrix();
				double[] tlate = new double[3];
				tlate[direction] = d;
				MatrixBuilder.euclidean().translate(tlate).assignTo(foo);
				clipPlane1SGC.getTransformation().setMatrix(
						Rn.times(null, 
								foo.getArray(), originalMat));
				worldSGC.getTransformation().setMatrix(clipper.getInverseMatrix(null));
				viewer.renderAsync();
			}
		}

	}
}
