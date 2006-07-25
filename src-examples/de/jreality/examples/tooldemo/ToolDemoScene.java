package de.jreality.examples.tooldemo;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JFrame;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CubeMap;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.HeadTransformationTool;
import de.jreality.tools.PickShowTool;
import de.jreality.tools.RotateTool;
import de.jreality.tools.ShipNavigationTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;

public class ToolDemoScene {

  private SceneGraphComponent rootNode=new SceneGraphComponent(),
                      sceneNode=new SceneGraphComponent(),
                      avatarNode=new SceneGraphComponent(),
                      camNode=new SceneGraphComponent(),
                      lightNode=new SceneGraphComponent(),
                      terrainNode;
  
  private Appearance terrainAppearance=new Appearance(),
                     rootAppearance=new Appearance();
  
  private SceneGraphPath camPath, avatarPath, emptyPickPath;
  
  private boolean terrain=true;
  
  public ToolDemoScene() {

    rootNode.setName("root");
    sceneNode.setName("scene");
    avatarNode.setName("avatar");
    camNode.setName("camNode");
    lightNode.setName("lights");
    rootNode.addChild(sceneNode);
    
    rootNode.setAppearance(rootAppearance);

    terrainAppearance.setAttribute("showLines", false);
    terrainAppearance.setAttribute("showPoints", false);
    terrainAppearance.setAttribute("diffuseColor", Color.white);

    Camera cam = new Camera();
    cam.setFar(1500);

    // lights
    DirectionalLight dl = new DirectionalLight();
    lightNode.setLight(dl);
    MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,1,-1}).assignTo(lightNode);
    rootNode.addChild(lightNode);

    // prepare paths
    rootNode.addChild(avatarNode);
    avatarNode.addChild(camNode);
    camNode.setCamera(cam);
    camPath = new SceneGraphPath();
    camPath.push(rootNode);
    emptyPickPath=camPath.pushNew(sceneNode);
    camPath.push(avatarNode);
    camPath.push(camNode);
    avatarPath=camPath.popNew();
    camPath.push(cam);
    
    MatrixBuilder.euclidean().translate(0,1.7,0).assignTo(camNode);
        
    // add tools
    ShipNavigationTool shipNavigationTool = new ShipNavigationTool();
    shipNavigationTool.setGain(17);
    //shipNavigationTool.setGravity(100);
    //shipNavigationTool.setJumpSpeed(20);
    avatarNode.addTool(shipNavigationTool);
    camNode.addTool(new HeadTransformationTool());
    
    rootNode.addTool(new PickShowTool(null));
    //avatarNode.addTool(new PointerDisplayTool());
    
    sceneNode.addTool(new RotateTool());
    sceneNode.addTool(new DraggingTool());

  }
  
  public void update() throws IOException {
    if (terrainNode != null) {
      rootNode.removeChild(terrainNode);
    }
    // prepare terrain
    if (terrain) {
      terrainNode = Readers.read(Input.getInput("terrain.3ds")).getChildComponent(0);
      MatrixBuilder.euclidean().translate(0,9,0).assignTo(terrainNode);
      System.out.println(GeometryUtility.calculateBoundingBox(terrainNode).getMinY());
      } else {
      terrainNode = new SceneGraphComponent();
      MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(terrainNode);
      terrainNode.setGeometry(Primitives.plainQuadMesh(300, 300, 1, 1));
    }
    terrainNode.setName("terrain");
    IndexedFaceSet terrainGeom = (IndexedFaceSet) terrainNode.getGeometry();
    GeometryUtility.calculateAndSetNormals(terrainGeom);
    PickUtility.assignFaceAABBTree(terrainGeom);

    terrainNode.setAppearance(terrainAppearance);
    rootNode.addChild(terrainNode);
  }
  
  public void setTerrainTexture(ImageData tex, double scale) {
    Texture2D t = TextureUtility.createTexture(terrainAppearance, "polygonShader", tex);
    t.setTextureMatrix(MatrixBuilder.euclidean().scale(scale).getMatrix());
  }
  
  public void setSkyBox(ImageData[] imgs) {
    TextureUtility.createSkyBox(rootAppearance, imgs);
  }
  
  public static void main(String[] args) throws IOException {
    System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
    //System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
    //System.setProperty("de.jreality.ui.viewerapp.autoRender", "false");
    System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
    
    ToolDemoScene tds = new ToolDemoScene();
    tds.update();
//    tds.setTerrainTexture(ImageData.load(Input.getInput("grid.jpeg")), 5);
//    tds.setTerrainTexture(ImageData.load(Input.getInput("monkey.jpeg")), 1);
    ViewerApp.display(tds.rootNode, tds.camPath, tds.emptyPickPath, tds.avatarPath);
    
    Landscape l = new Landscape();
    l.setToolScene(tds);
    
    JFrame f = new JFrame("skybox selection");
    f.getContentPane().add(l.selectionComponent);
    f.pack();
    f.setVisible(true);
  }

}
