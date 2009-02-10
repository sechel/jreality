package de.jreality.tutorial.app;

import static de.jreality.shader.CommonAttributes.BACKGROUND_COLOR;
import static de.jreality.shader.CommonAttributes.BACKGROUND_COLORS;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import de.jreality.geometry.SphereUtility;
import de.jreality.jogl.InfoOverlay;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.GlslPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class GLVertexArrayExample {

	private static SceneGraphComponent world;
	private final static Appearance 
		vertexArrayAp = new Appearance(), 
		displayListAp = new Appearance();
	static boolean vertexArrays = false;
	public static void main(String[] args)	{
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.setGeometry(SphereUtility.sphericalPatch(0, 0.0, 180.0, 90.0, 100, 100, 1.0));
		DefaultGeometryShader dgs = (DefaultGeometryShader) 
   			ShaderUtility.createDefaultGeometryShader(vertexArrayAp, true);
		dgs.setShowLines(true);
		dgs.setShowPoints(false);
		// A non-default shader has to be set using the following method call
		GlslPolygonShader tsps = (GlslPolygonShader) dgs.createPolygonShader("glsl");
		// but the attributes can be set directly using the Appearance.setAttribute() method
		DefaultLineShader dls = (DefaultLineShader) dgs.getLineShader();
		dls.setTubeDraw(true);
		dls.setTubeRadius(.005);
		dls.setDiffuseColor(Color.red);

		dgs = (DefaultGeometryShader) 
   			ShaderUtility.createDefaultGeometryShader(displayListAp, true);
		dgs.setShowLines(true);
		dgs.setShowPoints(false);
		dls = (DefaultLineShader) dgs.getLineShader();
		dls.setTubeDraw(true);
		dls.setTubeRadius(.005);
		dls.setDiffuseColor(Color.red);
		
		updateAp(vertexArrays);
		
		ViewerApp va = new ViewerApp(world);
		va.update();
		va.display();
		CameraUtility.encompass(va.getCurrentViewer());	
		Viewer viewer = va.getCurrentViewer();
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLOR, Color.black);
		viewer.getSceneRoot().getAppearance().setAttribute(BACKGROUND_COLORS,Appearance.INHERITED);
		if (viewer instanceof de.jreality.jogl.Viewer) {
			InfoOverlay io =InfoOverlay.perfInfoOverlayFor((de.jreality.jogl.Viewer)viewer);
			io.setVisible(true);
		}
		Component comp = ((Component) va.getCurrentViewer()
				.getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.err.println("	1: toggle vertex arrays");
						break;
		
					case KeyEvent.VK_1:
						vertexArrays = !vertexArrays;
						updateAp(vertexArrays);
						break;

				}
		
				}
			});

	}
	
	private static void updateAp(boolean vertexArrays)	{
		world.setAppearance(vertexArrays ? vertexArrayAp : displayListAp);
		System.err.println("vertex arrays = "+vertexArrays);
	}

}
