/*
 * Created on Aug 23, 2004
 *
 */
package de.jreality.worlds;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.Timer;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.SphereHelper;
import de.jreality.jogl.FramedCurve;
import de.jreality.jogl.InteractiveViewer;
import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Texture2D;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.CameraUtility;
import de.jreality.util.P3;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;



/**
 * @author gunn
 *
 */
public class TestClippingPlane extends AbstractJOGLLoadableScene {
	Viewer viewer;
	/**
	 * 
	 */
	public TestClippingPlane() {
		super();
	}
	static String resourceDir = "/homes/geometer/gunn/Pictures/textures/";
	static {
		String foo = System.getProperty("resourceDir");
		if (foo != null)	resourceDir  = foo;
	}
	boolean animate = false;
	public void customize(JMenuBar theMenuBar, Viewer v)	{
		viewer = v;
		JMenu testM = new JMenu("Actions");
		ButtonGroup bg = new ButtonGroup();
		final JCheckBoxMenuItem jca = new JCheckBoxMenuItem("Automate");
		jca.setSelected(false);
		testM.add(jca);
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				animate = !animate;
				if (animate) cameraMove.start();
				else cameraMove.stop();
				viewer.getViewingComponent().requestFocus();
			}
		});
		final JCheckBoxMenuItem jcb = new JCheckBoxMenuItem("Apply to camera");
		jcb.setSelected(applyToCamera);
		testM.add(jcb);
		jcb.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				applyToCamera = !applyToCamera;
				viewer.getViewingComponent().requestFocus();
			}
		});
		theMenuBar.add(testM);
	}
	static double[][] square = {{0,-1,0},{1,-1,0},{1,1,0},{0,1,0}};
	static double[][] texc = {{0,0},{1,0},{1,1},{0,1}};
	FramedCurve frameCurve;
	Timer cameraMove;
	
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("world");
		Appearance ap1 = new Appearance();
		root.setAppearance(ap1);
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		ap1.setAttribute(CommonAttributes.SPECULAR_COLOR, Color.YELLOW);
		//ap1.setAttribute(CommonAttributes.SPECULAR_COEFFICIENT, 0.0);
		
		int[] modes = { Texture2D.GL_REPLACE, Texture2D.GL_MODULATE,Texture2D.GL_DECAL, Texture2D.GL_BLEND, Texture2D.GL_ADD};
		double[][] channelMatrices = new double[5][];
		for (int i = 0; i<5; ++i)	{
			channelMatrices[i] = Rn.identityMatrix(4);
			// replace alpha channel by blue channel
			channelMatrices[i][15] = 0;
			channelMatrices[i][14] = 1;
			channelMatrices[i][11] = 1;
			channelMatrices[i][10] = 0;
		}
		
		boolean simple = true;
		if (simple)	{
			SceneGraphComponent cp =  SceneGraphUtilities.createFullSceneGraphComponent("theClipIcon");
			ap1 = cp.getAppearance();
			ap1.setAttribute(CommonAttributes.VERTEX_DRAW, true);
			ap1.setAttribute(CommonAttributes.EDGE_DRAW, true);
			ap1.setAttribute(CommonAttributes.FACE_DRAW, true);
			ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
			ap1.setAttribute(CommonAttributes.DIFFUSE_COEFFICIENT, 1.0);
			ap1.setAttribute(CommonAttributes.TRANSPARENCY, 0.0);
			ap1.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
			ap1.setAttribute(CommonAttributes.SPECULAR_COEFFICIENT, 0.2);
			Texture2D tex2d = null;
			try {
				tex2d = new Texture2D(Readers.getInput("textures/weaveRGBABright.png"));
				ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"texture2d",tex2d);
			} catch (IOException e) {
				e.printStackTrace();
			}
			tex2d.setTextureMatrix(P3.makeStretchMatrix(null,new double[]{8,4,1}));
			double[][] vv = {{0,-1,0},{0,1,0},{1,1,0},{1,-1,0}};
			double[][] texc = {{0,0},{1,0},{1,1} ,{0,1}};
			IndexedFaceSet square = IndexedFaceSetUtility.constructPolygon(vv);
			square.setVertexAttributes(Attribute.TEXTURE_COORDINATES,StorageModel.DOUBLE_ARRAY.array(2).createReadOnly(texc));
			cp.setGeometry(square);
			cp.getTransformation().setTranslation(0,0,.5);
			
			SceneGraphComponent cp2 =  SceneGraphUtilities.createFullSceneGraphComponent("theClipPlane");
			cp2.getTransformation().setTranslation(0d, 0d, .01d);
			cp2.setGeometry(new ClippingPlane());
			cp.addChild(cp2);
			
			SceneGraphComponent sgc = SceneGraphUtilities.createFullSceneGraphComponent("sphere");
			sgc.addChild(SphereHelper.SPHERE_SUPERFINE);
			//sgc.setGeometry(new Sphere());
			sgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.GREEN);
			sgc.getAppearance().setAttribute(CommonAttributes.SPECULAR_COLOR, java.awt.Color.GREEN);
			sgc.getAppearance().setAttribute(CommonAttributes.SPECULAR_COEFFICIENT, 1.0);
			sgc.getAppearance().setAttribute(CommonAttributes.SPECULAR_EXPONENT, 60.0);
			root.addChild(sgc);
			root.addChild(cp);
		}	
		return root;
	}
	SceneGraphComponent camNode = null;
	Transformation tt = new Transformation();
	int tick = 0;
	int tps = 600;
	int n;
	boolean applyToCamera = false;
	public void updateCameraPosition()		{
		if (camNode == null) camNode = CameraUtility.getCameraNode(viewer);
		double time =(tick % tps)/((double) tps);
		frameCurve.getValueAtTime(time, tt);
		//System.out.println("Rotation: "+tt.getRotationQuaternion().toString());
		if (applyToCamera) camNode.getTransformation().setMatrix(tt.getMatrix());
		viewer.render();
		tick++;
		if (tick >= tps) tick = 0;
	}
	
 	public boolean addBackPlane()	{return false;}
 	
	public boolean isEncompass() {
		return true;
	}
// 	public SceneGraphComponent makeLights()	{
// 		SceneGraphComponent spot = SceneGraphUtilities.createFullSceneGraphComponent("Spot");
// 		SpotLight sl = new SpotLight();
// 		//PointLight sl = new PointLight();
// 		//DirectionalLight sl = new DirectionalLight();
// 		sl.setColor(Color.YELLOW);
//		sl.setConeAngle(Math.PI/6.0 );
//		sl.setConeDeltaAngle(Math.PI/20.0);
// 		sl.setDistribution(2.0);
// 		sl.setIntensity(1.0);
// 		double[] atten = {0.5, 0.5,0.0};
// 		sl.setFalloff(atten);
// 		spot.getTransformation().setRotation(Math.PI, 1.0, 0.0, 0.0);
// 		//spot.getTransformation().setTranslation(.25, .5, .25);
// 		spot.setLight(sl);
// 		return spot;
// 	}
 }
