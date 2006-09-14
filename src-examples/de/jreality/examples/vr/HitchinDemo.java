package de.jreality.examples.vr;

import java.io.IOException;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.vr.ViewerVR;

public class HitchinDemo {
	
	public static void main(String[] args) throws IOException {
		Appearance app = new Appearance();
		SceneGraphComponent cmp = new SceneGraphComponent();
		
		app.setAttribute(CommonAttributes.EDGE_DRAW, false);
		app.setAttribute(CommonAttributes.TUBE_RADIUS, .01);
		app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		//app.setAttribute(CommonAttributes.POLYGON_SHADER, "implode");
		//app.setAttribute(CommonAttributes.FACE_DRAW, false);
		//app.setAttribute(CommonAttributes.BACK_FACE_CULLING_ENABLED,true);
		app.setAttribute("diffuseColor", new java.awt.Color(0,0,255));
		cmp.setAppearance(app);
		//ImageData img = ImageData.load(Input.getInput("textures/boysurface.png"));
		IndexedFaceSet f = (IndexedFaceSet) Readers.read(Input.getInput("obj/cmcS3_g2_sym.obj")).getChildComponent(0).getGeometry();
		//IndexedFaceSet f = (IndexedFaceSet) Readers.read(Input.getInput("obj/cmcS3_g2_piece.obj")).getChildComponent(0).getGeometry();
		//IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(f);
		//f=JoinGeometry.meltFace(f);
		//GeometryUtility.calculateAndSetNormals(f);
		f=IndexedFaceSetUtility.implode(f,.3);
		cmp.setGeometry(f);
		//Texture2D tex = TextureUtility.createTexture(he.getAppearance(), "polygonShader", img, false);
		//tex.setTextureMatrix(MatrixBuilder.euclidean().scale(50, 75, 0).getMatrix());
		PickUtility.assignFaceAABBTrees(cmp);
//		MatrixBuilder.euclidean()
//		.rotateY(-1)
//		.reflect( new double[]{1,0,0,0})
//		.rotateX(Math.PI/2)
//		.assignTo(cmp);
		
		//System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
		//System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
		//System.setProperty("de.jreality.ui.viewerapp.autoRender", "false");
		System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
		ViewerVR tds = new ViewerVR();
		
		tds.setContent(cmp);
		
		ViewerApp va = tds.display();
		va.setAttachBeanShell(true);
		va.setAttachNavigator(true);
		va.setShowMenu(true);
		
		va.update();
		va.display();
	}
	
}
