package de.jreality.examples.tooldemo;

import java.io.IOException;

import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.swing.ScenePanel;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;

public class He2Demo extends ToolDemoContent {

  private static SceneGraphComponent content() throws IOException {
    SceneGraphComponent cmp = new SceneGraphComponent();
    Appearance app = new Appearance();
    app.setAttribute(CommonAttributes.EDGE_DRAW, false);
    app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
    app.setAttribute("diffuseColor", java.awt.Color.white);
    cmp.setAppearance(app);
    ImageData img = ImageData.load(Input.getInput("textures/bluemetalsupport2.png"));
    SceneGraphComponent he = Readers.read(Input.getInput("obj/He2SmallTower.obj"));
    cmp.addChild(he);
    SceneGraphComponent boundary = Readers.read(Input.getInput("obj/He2SmallTowerBoundary.obj"));
    cmp.addChild(boundary);
    he.setAppearance(new Appearance());
    boundary.setAppearance(new Appearance());
    Texture2D tex = TextureUtility.createTexture(he.getAppearance(), "polygonShader", img, false);
    tex.setTextureMatrix(MatrixBuilder.euclidean().scale(10).getMatrix());
    tex = TextureUtility.createTexture(boundary.getAppearance(), "polygonShader", img, false);
    tex.setTextureMatrix(MatrixBuilder.euclidean().scale(2,400,1).getMatrix());
    PickUtility.assignFaceAABBTrees(cmp);
    MatrixBuilder.euclidean()
//      .rotateY(-1)
//      .reflect( new double[]{1,0,0,0})
      .rotateX(Math.PI/2)
      .assignTo(cmp);
    return cmp;
  }
  
  public He2Demo() throws IOException {
    super(content(), new Rectangle3D(new double[][]{{-20,0,-5},{20,40,-45}}));
    setKeepAspectRatio(false);
  }
  
  public static void main(String[] args) throws IOException {
    //System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
    //System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
    //System.setProperty("de.jreality.ui.viewerapp.autoRender", "false");
    System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
    ToolDemoScene tds = new ToolDemoScene();
    tds.update();
    
    He2Demo he2 = new He2Demo();
    tds.setContent(he2);
    
    ViewerApp va = tds.display();
    //va.setAttachBeanShell(true);
    //va.setAttachNavigator(true);
    
    va.update();
    va.display();

    Landscape l = new Landscape("night");
    l.setToolScene(tds);
    
    ScenePanel sp = new ScenePanel();
    sp.setPanelWidth(0.4);
    sp.getFrame().getContentPane().add(l.getSelectionComponent());
    sp.getFrame().pack();
    tds.getTerrainNode().addTool(sp.getPanelTool());
  }

}
