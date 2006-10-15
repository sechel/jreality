package de.jreality.examples.vr;

import java.io.IOException;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.tools.DuplicateTriplyPeriodicTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.vr.ViewerVR;

public class SchwarzDemo {

	private SceneGraphComponent domain;

	private SceneGraphComponent cmp = new SceneGraphComponent();
	
	public SchwarzDemo() throws IOException {
		
		try {
			//domain = Readers.read(Input.getInput("obj/SawTooth.obj"));
			domain = Readers.read(Input.getInput("3ds/schwarz.3ds"));
		} catch (IOException e) {
			RuntimeException re = new RuntimeException("could not load schwarz: "+e.getMessage());
			re.initCause(e);
			throw re;
		}
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.rotateY(-0.39283794);
		mb.assignTo(domain);
		SceneGraphComponent parent = new SceneGraphComponent();
		parent.setName("parent");
		cmp.addChild(parent);
		parent.addChild(domain);
		Rectangle3D bb = GeometryUtility.calculateChildrenBoundingBox(parent);
		double[] center = bb.getCenter();
		double[] latticeSpacing = bb.getExtent();
		domain.setName("domain");
		PickUtility.assignFaceAABBTrees(domain);
		domain.addTool(new DuplicateTriplyPeriodicTool(
				latticeSpacing[0],latticeSpacing[1],latticeSpacing[2],
				center[0], center[1], center[2]));
		Appearance app = new Appearance();
		app.setAttribute("showPoints", false);
		TextureUtility.createTexture(app, "polygonShader", Input.getInput("textures/schwarz.png"));
		cmp.setAppearance(app);
		cmp.setName("cmp");
	}
	
	public static void main(String[] args) throws IOException {
		ViewerVR tds = new ViewerVR();
		tds.setContent(new SchwarzDemo().cmp);
		tds.setDiam(5);
		
		ViewerApp va = tds.display();
		//va.setAttachBeanShell(true);
		//va.setAttachNavigator(true);
		
		va.update();
		va.display();
	}
}
