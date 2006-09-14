package de.jreality.examples.vr;

import java.io.IOException;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.vr.ViewerVR;

public class SchwarzDemo {
	
	Appearance app = new Appearance();
	
	private static SceneGraphComponent domain() {
		SceneGraphComponent domain;
		try {
			domain = Readers.read(Input.getInput("3ds/schwarz.3ds"));
		} catch (IOException e) {
			RuntimeException re = new RuntimeException("could not load schwarz: "+e.getMessage());
			re.initCause(e);
			throw re;
		}
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.rotateY(-0.39283794);
		mb.translate(0,-0.5,0);
		mb.scale(1/16.203125);
		mb.assignTo(domain);
		domain=GeometryUtility.flatten(domain);
		domain=domain.getChildComponent(0);
		domain.setName("domain");
		PickUtility.assignFaceAABBTrees(domain);
		domain.getGeometry().setName("schwarz");
		// now domain is aligned along x,y,z axes and scaled to size 1,1,1
		return domain;
	}
	
	final SceneGraphComponent domain=domain();
	SceneGraphComponent cmp = new SceneGraphComponent();
	
	public SchwarzDemo() throws IOException {
		domain.addTool(new AbstractTool(InputSlot.getDevice("PanelActivation")) {
			public void activate(ToolContext tc) {
				PickResult pick = tc.getCurrentPick();
				double[] coords = pick.getObjectCoordinates();
				// get max dir:
				int dir=0;
				if (Math.abs(coords[1])>Math.abs(coords[0])) dir=1;
				if (Math.abs(coords[2])>Math.abs(coords[dir])) dir=2;
				double[] trans=new double[3];
				trans[dir]=Math.signum(coords[dir]);
				SceneGraphComponent newCmp = new SceneGraphComponent();
				newCmp.setGeometry(domain.getGeometry());
				MatrixBuilder.euclidean().translate(trans).assignTo(newCmp);
				//pick.getPickPath().getLastComponent().addChild(newCmp);
				tc.getRootToLocal().getLastComponent().addChild(newCmp);
			}
		});
		app.setAttribute("showPoints", false);
		cmp.setAppearance(app);
		cmp.addChild(domain);
		Texture2D tex = TextureUtility.createTexture(app, "polygonShader", Input.getInput("textures/schwarz.png"));
	}
	
	public static void main(String[] args) throws IOException {
		//System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
		//System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
		//System.setProperty("de.jreality.ui.viewerapp.autoRender", "false");
		System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
		ViewerVR tds = new ViewerVR();
		tds.setContent(new SchwarzDemo().cmp);
		tds.alignContent(5, 0.3, null);
		
		ViewerApp va = tds.display();
		//va.setAttachBeanShell(true);
		//va.setAttachNavigator(true);
		
		va.update();
		va.display();
		
	}
}
