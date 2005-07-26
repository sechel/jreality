/*
 * Created on Mar 22, 2005
 *
 */
package de.jreality.scene.tool;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickSystem;
import de.jreality.soft.DefaultViewer;
import de.jreality.util.Rn;

/**
 * @author brinkman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PickTestScene {

	Viewer viewer = new de.jreality.jogl.Viewer();
	//Viewer viewer = new DefaultViewer();
	JFrame frame = new JFrame("viewer");
	PickSystem picksys;
	
	public PickTestScene() {
		 try {
			picksys = (PickSystem) Class.forName("de.jreality.jme.intersection.proxy.JmePickSystem").newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	MouseListener mouselisten = new MouseListener() {

		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void mousePressed(MouseEvent event) {
			double[] mouse = mouseToWorld(viewer, event.getX(), event.getY());
			Transformation cam = new Transformation(viewer.getCameraPath().getMatrix(null));
			double[] camPos = cam.getTranslation();
      System.out.println("camPos: ["+camPos[0]+","+camPos[1]+","+camPos[2]+","+camPos[3]+"]");
      
			double[] direction = Rn.subtract(null, mouse, camPos);
			System.out.println("direction: ["+direction[0]+","+direction[1]+","+direction[2]+/*","+direction[3]+*/"]");
      List lst = picksys.computePick(camPos, direction);
			System.out.println(lst);
		}

		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	private static double[] mouseToWorld(Viewer v, int x, int y) {
		double x1 = v.getViewingComponent().getWidth();
		double y1 = v.getViewingComponent().getHeight();
		double[] mouseToNDC = new double[] { -1 + 2 * x / x1, 1 - 2 * y / y1, 1 };
		Camera cam = (Camera) v.getCameraPath().getLastElement();
		return Rn.matrixTimesVector(null, 
                Rn.inverse(null, 
                        Rn.times(null, 
                                cam.getCameraToNDC(), 
                                v.getCameraPath().getInverseMatrix(null)
                        )
                ),
                mouseToNDC);
	}
	void createScene() {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("test root");
		SceneGraphComponent camNode = new SceneGraphComponent();
		camNode.setName("test camera");
		camNode.setTransformation(new Transformation());
		camNode.getTransformation().setTranslation(0, 0, 12);
		Camera cam = new Camera();
		camNode.setCamera(cam);
		camNode.setLight(new PointLight());
		
		SceneGraphComponent scene = new SceneGraphComponent();
		SceneGraphComponent sphere = new SceneGraphComponent();
		//sphere.setGeometry(new Sphere());
		IndexedFaceSet ifs = new CatenoidHelicoid(10);
		ifs.buildEdgesFromFaces();
		sphere.setGeometry(ifs);
		root.addChild(scene);
		scene.addChild(sphere);
		root.addChild(camNode);
		SceneGraphPath camPath = new SceneGraphPath();
		camPath.push(root);
		camPath.push(camNode);
		camPath.push(cam);
		viewer.setSceneRoot(root);
		viewer.setCameraPath(camPath);
//		new InputDeviceHandler(viewer);
//		camNode.addTool(new EgoShooterTool());
		
		picksys.setSceneRoot(root);
	
    ToolSystem ts = new ToolSystem(viewer);
    ts.setPickSystem(picksys);
        
		frame.setVisible(true);
		frame.getContentPane().add(viewer.getViewingComponent());
		frame.setSize(800, 600);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent arg0) {
				System.exit(0);
			}
		});
		viewer.getViewingComponent().addMouseListener(mouselisten);
	}
	
	public void render() {
		while (true) {
			viewer.render();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		PickTestScene tts = new PickTestScene();
		tts.createScene();
		tts.render();
	}
}
