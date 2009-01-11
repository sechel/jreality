/*
 * Created on Jan 11, 2009
 *
 */
package de.jreality.tutorial.viewer;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;

import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.jogl.JOGLSphereHelper;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.CameraEvent;
import de.jreality.scene.event.CameraListener;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class CameraListenerExample {

	public static void main(String[] args) {
		
		CameraListenerExample cle = new CameraListenerExample();
		cle.doIt();
	}
	
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("sphere");
	private DefaultPolygonShader dps;
	private DefaultLineShader dls;
	private Viewer viewer;
	private SceneGraphPath camPath;
	private SceneGraphPath spherePath;
	private IndexedFaceSet[] levelsOfDetailSpheres;
	private double[] levels;
	private Color[] colors;
	private int numLevels = 6, lastLevel = -1;
	public void doIt() {
		
		world.setGeometry(Primitives.icosahedron());
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(world.getAppearance(), true);
		dgs.setShowFaces(true);
		dgs.setShowLines(true);
		dgs.setShowPoints(false);
		dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
		dps.setDiffuseColor(Color.blue);
		dls = (DefaultLineShader) dgs.createLineShader("default");
		dls.setTubeDraw(true);
		dls.setLineWidth(2.0);		// in case you want no tubes...
		dls.setDiffuseColor(Color.white);

		world.getAppearance().setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		ViewerApp va = new ViewerApp(world);
		viewer = va.getCurrentViewer();
		va.update();
		va.display();
		camPath = viewer.getCameraPath();
		spherePath = SceneGraphUtility.getPathsToNamedNodes(
				viewer.getSceneRoot(), 
				"sphere").get(0);
		levelsOfDetailSpheres = new IndexedFaceSet[numLevels];
		levels = new double[numLevels];
		colors = new Color[numLevels];
		double base = .15;
		for (int i = 0; i<numLevels; ++i) {
			levelsOfDetailSpheres[i] = SphereUtility.tessellatedIcosahedronSphere(i);
			levels[i] = base * (i+1);
			colors[i] = new Color(i/(numLevels-1f), 0, ((numLevels-1)-i)/(numLevels-1f));
		}
		CameraListener camListener = new CameraListener() {
			public void cameraChanged(CameraEvent ev) {
				update();
			}
			
		};
		update();
		CameraUtility.getCamera(viewer).addCameraListener(camListener);
	}
	
	public void update()	{
		double[] s2w = spherePath.getMatrix(null);
		double[] w2c = camPath.getInverseMatrix(null);
		double[] c2ndc = CameraUtility.getCameraToNDC(viewer);
		double[] s2ndc = Rn.times(null, Rn.times(null, c2ndc, w2c), s2w);
//			System.err.println("s2ndc = "+Rn.matrixToString(s2ndc));
		double size = JOGLSphereHelper.getNDCExtent(s2ndc);
		double logs = Math.log(size)+Math.log(40);
		if (logs < 0) logs = 0;
		if (logs > (numLevels-1)) logs = (numLevels-1);
		int which = (int) logs;
		System.err.println("size = "+size+" which = "+which);
		if (which == lastLevel) return;
		world.setGeometry(levelsOfDetailSpheres[which]);
		dls.setTubeRadius(.033*Math.pow(.5, which));
		dps.setDiffuseColor(colors[which]);
		lastLevel = which;
	}
	

	
}
