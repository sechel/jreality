package de.jreality.portal;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.Viewer;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.GlslProgram;
import de.jreality.util.Input;

public class CylindricalPerspectiveViewer extends Viewer {

	GlslProgram cylProg;
	
	public CylindricalPerspectiveViewer() {
		super();
		super.setStereoType(HARDWARE_BUFFER_STEREO);
		System.out.println("CYLINDRICAL PERSPECTIVE VIEWER");
		renderer=new JOGLRenderer(this) {
			@Override
			protected void setupLeftEye(int width, int height) {
				super.setupLeftEye(width, height);
				if (cylProg != null) cylProg.setUniform("eye", 0.);
				//System.out.println("\tLEFT EYE");
			}
			@Override
			protected void setupRightEye(int width, int height) {
				super.setupRightEye(width, height);
				if (cylProg != null) cylProg.setUniform("eye", 1.);
				//System.out.println("\tRIGHT EYE");
			}
		};
	}
	
	@Override
	public void setSceneRoot(SceneGraphComponent r) {
		checkProgram(r);
		super.setSceneRoot(r);
	}

	private void checkProgram(SceneGraphComponent sceneRoot) {
		if (sceneRoot != null && cylProg == null && sceneRoot.getAppearance()!=null) {
			try {
				cylProg = new GlslProgram(sceneRoot.getAppearance(), "polygonShader", Input.getInput(getClass().getResource("cylStereoVertexShader.glsl")), null);
				sceneRoot.getAppearance().setAttribute("polygonShadername", "glsl");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			cylProg=null;
			System.out.println("NO PROGRAM!!!");
		}
	}

	@Override
	public void render() {
		if (cylProg == null) checkProgram(getSceneRoot());
		if (getStereoType() != HARDWARE_BUFFER_STEREO) setStereoType(HARDWARE_BUFFER_STEREO);
		super.render();
	}

	public void setParameters(Camera cam) {
	    if (cylProg != null) {
	    	Rectangle2D cv = cam.getViewPort();
	    	//System.out.println("setting viewport: "+cv);
	    	cylProg.setUniform("cv", new double[]{cv.getMinX(), cv.getMaxX(), cv.getMinY(), cv.getMaxY()});
	    	cylProg.setUniform("d", cam.getFocus());
	    	cylProg.setUniform("eyeSep", cam.getEyeSeparation());
	    	cylProg.setUniform("near", cam.getNear());
	    	cylProg.setUniform("far", cam.getFar());
	    }
	}
	
}
