package de.jreality.examples.tooldemo;

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

public class He2Demo implements ChangeListener {

  Appearance app = new Appearance();
  SceneGraphComponent cmp = new SceneGraphComponent();
  
  private void init() throws IOException {
    app.setAttribute(CommonAttributes.EDGE_DRAW, false);
    app.setAttribute(CommonAttributes.VERTEX_DRAW, false);
    app.setAttribute("diffuseColor", java.awt.Color.white);
    cmp.setAppearance(app);
    ImageData img = ImageData.load(Input.getInput("textures/metal_basic88.png"));
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
  }
  
  ToolDemoContent content;
  
  public He2Demo() throws IOException {
    init();
    content = new ToolDemoContent(cmp, new Rectangle3D(new double[][]{{-20,0, -20},{20,30,20}}));
    content.setKeepAspectRatio(false);
  }
  
  public ToolDemoContent getContent() {
    return content;
  }
  
  public void stateChanged(ChangeEvent e) {
    Landscape l = (Landscape) e.getSource();
    setReflectionMap(l.getSelectedCubeMap());
  }
  
  public void setReflectionMap(ImageData[] selectedCubeMap) {
    System.out.println("creating reflection map");
    CubeMap cm = TextureUtility.createReflectionMap(app, "polygonShader", selectedCubeMap);
    cm.setBlendColor(new java.awt.Color(1.0f, 0.0f, 0.0f, .3f));
  }

  public static void main(String[] args) throws IOException {
    //System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
    //System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
    //System.setProperty("de.jreality.ui.viewerapp.autoRender", "false");
    System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
    ToolDemoScene tds = new ToolDemoScene();
    tds.setAvatarPosition(0, 0, 25);
    tds.update();
    
    He2Demo he2 = new He2Demo();
    tds.setContent(he2.getContent());
    
    ViewerApp va = tds.display();
    //va.setAttachBeanShell(true);
    //va.setAttachNavigator(true);
    
    Landscape l = new Landscape("dusk");
    l.addChangeListener(he2);
    he2.setReflectionMap(l.getSelectedCubeMap());
    l.setToolScene(tds);
    
    ScenePanel sp = new ScenePanel();
    sp.setPanelWidth(0.4);
    sp.getFrame().getContentPane().add(l.getSelectionComponent());
    sp.getFrame().pack();
    tds.getTerrainNode().addTool(sp.getPanelTool());
    
    va.update();
    va.display();
  }

}
