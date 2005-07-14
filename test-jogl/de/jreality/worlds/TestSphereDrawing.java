/*
 * Created on May 12, 2004
 *
 */
package de.jreality.worlds;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Torus;
import de.jreality.jogl.InteractiveViewer;
import de.jreality.jogl.ViewerKeyListener;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Pn;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author Charles Gunn
 *
 */
public class TestSphereDrawing extends AbstractJOGLLoadableScene {

	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = SceneGraphUtilities.createFullSceneGraphComponent("theWorld");
		Appearance ap1 = root.getAppearance();
		ap1.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER, "implode");
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(0,204,204));
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".implodeFactor", -.6);
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .006);
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(210, 150, 0));
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, true);
		ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .012);
		ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(250, 10, 250));
		for (int i = 0; i< 1; ++i)	{
			Torus torus= new Torus(0.5, 0.3, 20, 30);
			torus.setName("torus"+i);
			GeometryUtility.calculateAndSetNormals(torus);
//			int lim = torus.getMaxU() + torus.getMaxV();
//			lim = lim + 2;
//			double[][] edgeColors = new double[lim][4];
//			for (int j = 0; j<lim; ++j)	{
//				edgeColors[j][0] = j/((double) lim);
//				edgeColors[j][1] = 0.0;
//				edgeColors[j][2] = (lim-j)/((double) lim);
//				edgeColors[j][3] = 1.0;
//			}
//			torus.setEdgeAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(4).createWritableDataList(edgeColors));
			SceneGraphComponent globeNode = new SceneGraphComponent();
			globeNode.setName("comp"+i);
			Transformation gt= new Transformation();
			//gt.setTranslation(-5.0 + 2.0* i, 0, 0.0);
			globeNode.setTransformation(gt);
			//if (i!=0) globeNode.setGeometry(GeometryUtility.implode(torus, -.9 + .4 * i));
			//else globeNode.setGeometry(GeometryUtility.truncate(torus));
			globeNode.setGeometry(torus);
			root.addChild(globeNode);
		}
		//CameraUtility.getCameraNode(viewer).getTransformation().setTranslation(0.0d, 0.0d, 4.0d);
		return root;
	}
 
	public boolean addBackPlane()	{return false;}
	public void setConfiguration(ConfigurationAttributes config) {
	}

	public int getSignature() {
		return Pn.EUCLIDEAN;
	}
	public boolean isEncompass() {
		return true;
	}
	public void customize(JMenuBar menuBar, Viewer v) {
		final Viewer viewer = v;
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,80, 60));
		viewer.getViewingComponent().addKeyListener(new KeyAdapter()	{
			
		    double scaleFactor = .05;
		    int selection = 0;
			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					System.out.println("	7:  increase/decrease implode factor increment");
					System.out.println("shift-7:  decrease plane movement increment");
					System.out.println("	8:  dump plane info to stdout");
					System.out.println("	9:  dump pickpoint info to stdout");
					System.out.println("	0:  cycle through selection list");
					System.out.println("	up/down arrows: move white plane ");
					System.out.println("	left/right arrows: move red plane ");
					break;

				case KeyEvent.VK_7:
					if ( !(viewer instanceof InteractiveViewer)) break;
					ViewerKeyListener.modulateValueAdditive((InteractiveViewer) viewer, CommonAttributes.POLYGON_SHADER+".implodeFactor", 0.5, .1, -1.0, 1.0, !e.isShiftDown());
				    viewer.render();
					break;
					}
			}
		});
	}
}

