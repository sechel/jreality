/*
 * Created on Aug 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.examples.jogl;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.Timer;

import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.FramedCurve;
import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Texture2D;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;



/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestTextureDemo extends InteractiveViewerDemo {
	/**
	 * 
	 */
	public TestTextureDemo() {
		super();
	}
	boolean animate = false;
	public JMenuBar createMenuBar()	{
		theMenuBar = super.createMenuBar();
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
		return theMenuBar;
	}
	static double[][] square = {{-1,-1,0},{1,-1,0},{1,1,0},{-1,1,0}};
	static double[][] texc = {{0,0},{1,0},{1,1},{0,1}};
	FramedCurve frameCurve;
	Timer cameraMove;
	
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("world");
		Appearance ap1 = new Appearance();
		root.setAppearance(ap1);
		double[] stretch = {1.0, 1.0, 0.0, 1.0};
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureEnabled",true);
		Texture2D tex2d = null;
		Image theImage = null;
			tex2d = null;
			theImage = null;
			try {
				tex2d = new Texture2D("/gunn/desertstorm/desertstorm_ft.JPG");//"/gunn/Software/eclipse/workspace/jReality/test/de/jreality/examples/resources/grid256rgba.png");
				//tex2d = new Texture2D("test/de/jreality/examples/resources/out.tiff"); //256rgba.png");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			theImage = tex2d.getImage();
			ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"texture2d",tex2d);
			//tex2d.setTextureMatrix(new Transformation(P3.makeStretchMatrix(null, stretch)));			
		
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
			double[][] vv = {{0,0,0},{1,0,0},{1,1,0},{0,1,0}};
			double[][] texc = {{0,0},{1,0},{1,1} ,{0,1}};
			//tex2d.setApplyMode(modes[0]);
			IndexedFaceSet square = GeometryUtility.constructPolygon(vv);
			square.setVertexAttributes(Attribute.TEXTURE_COORDINATES,StorageModel.DOUBLE_ARRAY.array(2).createReadOnly(texc));
			SceneGraphComponent sgc = SceneGraphUtilities.createFullSceneGraphComponent("testTexture");
			sgc.setGeometry(square);
			root.addChild(sgc);
		} else {
			java.awt.Color[] colors = {java.awt.Color.RED,java.awt.Color.RED, java.awt.Color.RED, java.awt.Color.RED, java.awt.Color.BLACK};
			
//			stretch[0] = 2.0;
//			for (int i = 0; i< 5; ++i)	{
//				Torus torus= new Torus(1.0, 0.6, 50, 50);
//				torus.setName("torus"+i);
//				GeometryUtility.calculateAndSetNormals(torus);
//				StorageModel sm = StorageModel.DOUBLE_ARRAY.array(2);
//				torus.setVertexAttributes(Attribute.TEXTURE_COORDINATES, sm.createReadOnly(GeometryUtility.calculateTextureCoordinates(torus)));
//				SceneGraphComponent globeNode = new SceneGraphComponent();
//				globeNode.setName("torus"+i);
//				Transformation gt= new Transformation();
//				gt.setTranslation(-5.0 + 3.0* i, 0, 0.0);
//				globeNode.setTransformation(gt);
//				globeNode.setGeometry(torus);
//				ap1 = new Appearance();
//				ap1.setAttribute(CommonAttributes.EDGE_DRAW, false);
//				ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, colors[i]);
//				stretch[0] = stretch[1] = (i+1)/2.0;
//				tex2d = new Texture2D(theImage);
//				tex2d.setTextureMatrix(new Transformation(P3.makeStretchMatrix(null, stretch)));
//				tex2d.setApplyMode(modes[i]);
//				tex2d.setChannelArithmeticMatrix(channelMatrices[i]);
//				ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"texture2d",tex2d);
//				ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureMatrix",P3.makeStretchMatrix(null, stretch));
//				globeNode.setAppearance(ap1);
//				root.addChild(globeNode);
//			}
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
   public static void main(String argv[])	{
	   TestTextureDemo test = new TestTextureDemo();
	   Logger.getLogger("de.jreality").setLevel(Level.INFO);
	   Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);
	   if (argv != null && argv.length > 0)	{
		   Logger.getLogger("de.jreality").log(Level.INFO, "arguments are {0}",argv[0]);
	   }
	   test.begin();
   }
}
