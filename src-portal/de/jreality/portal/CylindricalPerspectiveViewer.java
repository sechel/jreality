package de.jreality.portal;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.Viewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.GlslProgram;

public class CylindricalPerspectiveViewer extends Viewer {

	GlslProgram cylProg;
	
	public CylindricalPerspectiveViewer() {
		super();
		System.out.println("CYLINDRICAL PERSPECTIVE VIEWER");
		renderer=new JOGLRenderer(this) {
			@Override
			protected void setupLeftEye(int width, int height) {
				super.setupLeftEye(width, height);
				if (cylProg != null) cylProg.setUniform("eye", 0.);
				System.out.println("\tLEFT EYE");
			}
			@Override
			protected void setupRightEye(int width, int height) {
				super.setupRightEye(width, height);
				if (cylProg != null) cylProg.setUniform("eye", 1.);
				System.out.println("\tRIGHT EYE");
			}
		};
	}
	
	@Override
	public void setSceneRoot(SceneGraphComponent r) {
		if (r != null && r.getAppearance()!=null && GlslProgram.hasGlslProgram(r.getAppearance(), "polygonShader")) {
			cylProg=new GlslProgram(r.getAppearance(), "polygonShader");
			System.out.println("found program: ");
		} else {
			cylProg=null;
			System.out.println("NO PROGRAM!!!");
		}
		super.setSceneRoot(r);
	}

	@Override
	public void render() {
		if (cylProg == null) {
			SceneGraphComponent r = getSceneRoot();
			if (r != null && r.getAppearance()!=null && GlslProgram.hasGlslProgram(r.getAppearance(), "polygonShader")) {
				cylProg=new GlslProgram(r.getAppearance(), "polygonShader");
				System.out.println("found program: ");
			} else {
				cylProg=null;
				System.out.println("NO PROGRAM!!!");
			}
		}
		super.render();
	}
	
}
