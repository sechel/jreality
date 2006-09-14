package de.jreality.examples.vr;

import java.io.IOException;

import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.vr.ViewerVR;

public class WenteDemo {

	  public static void main(String[] args) throws IOException {
  Appearance app = new Appearance();
  SceneGraphComponent cmp = new SceneGraphComponent();
  
    app.setAttribute(CommonAttributes.EDGE_DRAW, false);
    app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
    app.setAttribute("diffuseColor", java.awt.Color.white);
    cmp.setAppearance(app);
    ImageData img = ImageData.load(Input.getInput("textures/boysurface.png"));
    SceneGraphComponent he = Readers.read(Input.getInput("3ds/wente.3ds"));
    cmp.addChild(he);
    he.setAppearance(new Appearance());
    Texture2D tex = TextureUtility.createTexture(he.getAppearance(), "polygonShader", img, false);
    tex.setTextureMatrix(MatrixBuilder.euclidean().scale(50, 75, 0).getMatrix());
    PickUtility.assignFaceAABBTrees(cmp);
//    MatrixBuilder.euclidean()
//      .rotateY(-1)
//      .reflect( new double[]{1,0,0,0})
//      .rotateX(Math.PI/2)
//      .assignTo(cmp);
  
    //System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
    //System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
    //System.setProperty("de.jreality.ui.viewerapp.autoRender", "false");
    System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
    ViewerVR tds = new ViewerVR();

    tds.setContent(cmp);
    
    ViewerApp va = tds.display();
    //va.setAttachBeanShell(true);
    //va.setAttachNavigator(true);
    
    va.update();
    va.display();
  }

}
