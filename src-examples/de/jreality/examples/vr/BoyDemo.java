package de.jreality.examples.vr;

import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.swing.ScenePanel;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.vr.ViewerVR;

public class BoyDemo {
	
	public static void main(String[] args) throws IOException {
		//System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
		//System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
		//System.setProperty("de.jreality.ui.viewerapp.autoRender", "false");
		System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
		ViewerVR tds = new ViewerVR();
		
		
		Appearance app = new Appearance();
		SceneGraphComponent cmp = new SceneGraphComponent();
		app.setAttribute(CommonAttributes.EDGE_DRAW, false);
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		app.setAttribute("diffuseColor", java.awt.Color.white);
		cmp.setAppearance(app);
		ImageData img = ImageData.load(Input.getInput("textures/boysurface.png"));
		SceneGraphComponent he = Readers.read(Input.getInput("3ds/boy.3ds"));
		he.removeChild(he.getChildComponent(0));
		cmp.addChild(he);
		he.setAppearance(new Appearance());
		Texture2D tex = TextureUtility.createTexture(he.getAppearance(), "polygonShader", img, false);
		tex.setTextureMatrix(MatrixBuilder.euclidean().scale(50, 75, 0).getMatrix());
		PickUtility.assignFaceAABBTrees(cmp);
		
		tds.setContent(cmp);
		
		ViewerApp va = tds.display();
		va.update();
		va.display();
	}
	
}
