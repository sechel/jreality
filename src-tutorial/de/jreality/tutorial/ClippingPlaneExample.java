package de.jreality.tutorial;

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
	static double[][] texc = {{0,0},{1,0},{1,1},{0,1}};
	
	  public static void main(String[] args) throws IOException {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("world");
		Appearance withTex = new Appearance();
		
			SceneGraphComponent theComponent = SceneGraphUtility.createFullSceneGraphComponent("theClipIcon");
			theComponent.setAppearance(withTex);
			double[][] vv = {{0,-1,0},{0,1,0},{1,1,0},{1,-1,0}};
			double[][] texc = {{0,0},{1,0},{1,1} ,{0,1}};
			IndexedFaceSet square = IndexedFaceSetUtility.constructPolygon(vv);
			square.setVertexAttributes(Attribute.TEXTURE_COORDINATES,StorageModel.DOUBLE_ARRAY.array(2).createReadOnly(texc));
			theComponent.setGeometry(square);
			theComponent.getTransformation().setMatrix(P3.makeTranslationMatrix(null, new double[]{0d,0d,.5d}, Pn.EUCLIDEAN));
			
			SceneGraphComponent cp2 =  SceneGraphUtility.createFullSceneGraphComponent("theClipPlane");
			cp2.getTransformation().setMatrix(P3.makeTranslationMatrix(null, new double[]{0d,0d,.01d}, Pn.EUCLIDEAN));
			cp2.setGeometry(new ClippingPlane());
			theComponent.addTool(new RotateTool());
			theComponent.addChild(cp2);
			
			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("sphere");
			PickUtility.setPickable(sgc, false);
			sgc.addChild(SphereUtility.tessellatedCubeSphere(SphereUtility.SPHERE_SUPERFINE));
			sgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"name","twoSide");
			sgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER,TwoSidePolygonShader.class);
			sgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+".front."+CommonAttributes.DIFFUSE_COLOR, new Color(0,204,204));
			sgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+".back."+CommonAttributes.DIFFUSE_COLOR, new Color(204,204,0));
			sgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
			root.addChild(sgc);
			root.addChild(theComponent);
			root.addTool(new ClickWheelCameraZoomTool());
		    ViewerApp.display(root);
	}
 }
