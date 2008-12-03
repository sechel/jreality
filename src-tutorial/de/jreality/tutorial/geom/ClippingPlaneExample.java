package de.jreality.tutorial.geom;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.Timer;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TwoSidePolygonShader;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.tools.RotateTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;


public class ClippingPlaneExample{
		static double[][] square = {{0,-1,0},{1,-1,0},{1,1,0},{0,1,0}};
	
	  public static void main(String[] args) throws IOException {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("world");
		
		final SceneGraphComponent clipIcon = SceneGraphUtility.createFullSceneGraphComponent("theClipIcon");
		double[][] vv = {{-1,-1,0},{-1,1,0},{1,1,0},{1,-1,0}};
		IndexedFaceSet square = IndexedFaceSetUtility.constructPolygon(vv);
		// set color to be completely transparent
		square.setFaceAttributes(Attribute.COLORS, 
				StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(new double[][]{{0,0,1,0}}));
	
		clipIcon.setGeometry(square);
		clipIcon.getTransformation().setMatrix(P3.makeTranslationMatrix(null, new double[]{0d,0d,.5d}, Pn.EUCLIDEAN));
		
		// The clip plane itself is a child of the clip icon, so when I move the icon the plane moves
		SceneGraphComponent clipPlane =  SceneGraphUtility.createFullSceneGraphComponent("theClipPlane");
		// the icon for the clipping plane shouldn't get clipped away; move it slightly 
		clipPlane.getTransformation().setMatrix(P3.makeTranslationMatrix(null, new double[]{0d,0d,.01d}, Pn.EUCLIDEAN));
		ClippingPlane cp =  new ClippingPlane();
		cp.setLocal(true);
		clipPlane.setGeometry(cp);
		// add a rotate tool to the clip icon
		final SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("sphere");
		clipIcon.addTool(new RotateTool() {

			@Override
			public void perform(ToolContext tc) {
				super.perform(tc);
				sgc.getTransformation().setMatrix(
						Rn.inverse(null, clipIcon.getTransformation().getMatrix()));
			}
			
		});
		clipIcon.addChild(clipPlane);
		
		sgc.setPickable( false);
		sgc.addChild(SphereUtility.tessellatedCubeSphere(SphereUtility.SPHERE_SUPERFINE));
		sgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"name","twoSide");
		sgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER,TwoSidePolygonShader.class);
		sgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+".front."+CommonAttributes.DIFFUSE_COLOR, new Color(0,204,204));
		sgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+".back."+CommonAttributes.DIFFUSE_COLOR, new Color(204,204,0));
		sgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		clipPlane.addChild(sgc);
		root.addChild(clipIcon);
		root.addTool(new ClickWheelCameraZoomTool());
	    ViewerApp.display(root);
	}
 }
