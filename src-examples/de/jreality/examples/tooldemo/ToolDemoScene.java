package de.jreality.examples.tooldemo;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JFrame;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.Attribute;
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
import de.jreality.util.Rectangle3D;

public class ToolDemoScene {

  private SceneGraphComponent sceneRoot=new SceneGraphComponent(),
                      sceneNode=new SceneGraphComponent(),
                      avatarNode=new SceneGraphComponent(),
                      camNode=new SceneGraphComponent(),
                      lightNode=new SceneGraphComponent(),
                      lightNode2=new SceneGraphComponent(),
                      lightNode3=new SceneGraphComponent(),
                      terrainNode;
  
  private Appearance terrainAppearance=new Appearance(),
                     rootAppearance=new Appearance();
  
  private DirectionalLight light = new DirectionalLight();
  
  private SceneGraphPath cameraPath, avatarPath, emptyPickPath;
  
  private boolean terrain=true;
  
  public ToolDemoScene() {

    sceneRoot.setName("root");
    sceneNode.setName("scene");
    avatarNode.setName("avatar");
    camNode.setName("camNode");
    lightNode.setName("lights");
    sceneRoot.addChild(sceneNode);
    
    sceneRoot.setAppearance(rootAppearance);

    terrainAppearance.setAttribute("showLines", false);
    terrainAppearance.setAttribute("showPoints", false);
    terrainAppearance.setAttribute("diffuseColor", Color.white);

    Camera cam = new Camera();
    cam.setFar(1500);

    // lights
    light.setIntensity(0.4);
    lightNode.setLight(light);
    MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,1,-1}).assignTo(lightNode);
    sceneRoot.addChild(lightNode);

    lightNode2.setLight(light);
    MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{1,1,-1}).assignTo(lightNode2);
    sceneRoot.addChild(lightNode2);
    
    lightNode3.setLight(light);
    MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{1,1,1}).assignTo(lightNode3);
    sceneRoot.addChild(lightNode3);

    lightNode3.setLight(light);
    MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,1,1}).assignTo(lightNode3);
    sceneRoot.addChild(lightNode3);

    // prepare paths
    sceneRoot.addChild(avatarNode);
    avatarNode.addChild(camNode);
    camNode.setCamera(cam);
    cameraPath = new SceneGraphPath();
    cameraPath.push(sceneRoot);
    emptyPickPath=cameraPath.pushNew(sceneNode);
    cameraPath.push(avatarNode);
    cameraPath.push(camNode);
    avatarPath=cameraPath.popNew();
    cameraPath.push(cam);
    
    MatrixBuilder.euclidean().translate(0,1.7,0).assignTo(camNode);
        
    // add tools
    ShipNavigationTool shipNavigationTool = new ShipNavigationTool();
    shipNavigationTool.setGain(7);
    //shipNavigationTool.setGravity(100);
    //shipNavigationTool.setJumpSpeed(20);
    avatarNode.addTool(shipNavigationTool);
    camNode.addTool(new HeadTransformationTool());
    
    //sceneRoot.addTool(new PickShowTool(null));
    //avatarNode.addTool(new PointerDisplayTool());
    
    //sceneNode.addTool(new RotateTool());
    //DraggingTool draggingTool = new DraggingTool();
    //draggingTool.setMoveChildren(true);
    //sceneNode.addTool(draggingTool);

  }
  
  public void update() throws IOException {
    if (terrainNode != null) {
      sceneRoot.removeChild(terrainNode);
    }
    // prepare terrain
    if (terrain) {
      terrainNode = Readers.read(Input.getInput("terrain.3ds")).getChildComponent(0);
      MatrixBuilder.euclidean().scale(1/3.).translate(0,9,0).assignTo(terrainNode);
      System.out.println(GeometryUtility.calculateBoundingBox(terrainNode).getMinY());
      } else {
      terrainNode = new SceneGraphComponent();
      MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(terrainNode);
      terrainNode.setGeometry(Primitives.plainQuadMesh(100, 100, 1, 1));
    }
    terrainNode.setName("terrain");
    IndexedFaceSet terrainGeom = (IndexedFaceSet) terrainNode.getGeometry();
    GeometryUtility.calculateAndSetNormals(terrainGeom);
    PickUtility.assignFaceAABBTree(terrainGeom);

    terrainNode.setAppearance(terrainAppearance);
    sceneRoot.addChild(terrainNode);
  }
  
  public void setTerrainTexture(ImageData tex, double scale) {
    Texture2D t = TextureUtility.createTexture(terrainAppearance, "polygonShader", tex);
    t.setTextureMatrix(MatrixBuilder.euclidean().scale(scale).getMatrix());
  }
  
  public void setSkyBox(ImageData[] imgs) {
    TextureUtility.createSkyBox(rootAppearance, imgs);
  }
  
  public void setContent(SceneGraphComponent content, Rectangle3D bounds, boolean keepRatio) {
    Rectangle3D contentBounds = GeometryUtility.calculateBoundingBox(content);
    setContent(content, contentBounds, bounds, keepRatio);
  }
  
  public void setContent(SceneGraphComponent content, Rectangle3D contentBounds, Rectangle3D bounds, boolean keepRatio) {
    double[] exc = contentBounds.getExtent();
    double[] exb = bounds.getExtent();
    double[] scale = new double[3];
    scale[0]=Math.abs(exb[0]/exc[0]);
    scale[1]=Math.abs(exb[1]/exc[1]);
    scale[2]=Math.abs(exb[2]/exc[2]);
    if (keepRatio) {
      if (scale[1]<scale[0]) scale[0]=scale[1];
      else scale[1]=scale[0];
      if (scale[1]<scale[2]) scale[2]=scale[1];
      else scale[0]=scale[1]=scale[2];
    }
    MatrixBuilder mb = MatrixBuilder.euclidean().scale(scale);
    mb.assignTo(content);
    //contentBounds = GeometryUtility.calculateBoundingBox(content);
    double[] contentCenter = contentBounds.getCenter();
    contentCenter[0]*=scale[0]; contentCenter[1]*=scale[1]; contentCenter[2]*=scale[2];
    double[] t = Rn.subtract(null, bounds.getCenter(), contentCenter);
    t[0]/=scale[0]; t[1]/=scale[1]; t[2]/=scale[2];
    mb.translate(t);
    mb.assignTo(content);
    sceneNode.addChild(content);
    
    contentBounds = GeometryUtility.calculateBoundingBox(content);
    System.out.println("cb:\n"+contentBounds+"\n\nbb:\n"+bounds);
    
    //sceneRoot.addChild(bounds(bounds));
  }
  
  SceneGraphComponent bounds(Rectangle3D bounds) {
    SceneGraphComponent ret = new SceneGraphComponent();
    IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
    ilsf.setVertexCount(8);
    ilsf.setLineCount(12);
    IndexedFaceSet cube = Primitives.cube();
    ilsf.setVertexCoordinates(cube.getVertexAttributes(Attribute.COORDINATES));
    ilsf.setEdgeIndices(cube.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null));
    ilsf.update();
    ret.setGeometry(ilsf.getIndexedLineSet());
    double[] scale = bounds.getExtent();
    scale[0]/=2; scale[1]/=2; scale[2]/=2;
    MatrixBuilder mb = MatrixBuilder.euclidean().scale(scale);
    double[] t = bounds.getCenter();
    t[0]/=scale[0]; t[1]/=scale[1]; t[2]/=scale[2];
    mb.translate(t).assignTo(ret);
    return ret;
  }
 
  public ViewerApp display() {
    return new ViewerApp(sceneRoot, cameraPath, emptyPickPath, avatarPath);
  }
  
  public static void main(String[] args) throws IOException {
    System.out.println(Thread.currentThread().getContextClassLoader().getResource("terrain.3ds"));
    //System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
    //System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
    //System.setProperty("de.jreality.ui.viewerapp.autoRender", "false");
    System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
    
    ToolDemoScene tds = new ToolDemoScene();
    //tds.terrain=false;
    tds.update();

    ViewerApp vApp = tds.display();
    vApp.update();
    vApp.display();

    Landscape l = new Landscape();
    l.setToolScene(tds);
    
    JFrame f = new JFrame("skybox selection");
    f.getContentPane().add(l.selectionComponent);
    f.pack();
    f.setVisible(true);
    
    Rectangle3D bounds = new Rectangle3D(new double[][]{{-1,1,1},{1,14,-1}});
    SceneGraphComponent schwarz1 = Readers.read(Input.getInput("3ds/schwarz.3ds"));
    SceneGraphComponent schwarz2 = Readers.read(Input.getInput("3ds/schwarz.3ds"));
    Appearance app = new Appearance();
    app.setAttribute("showPoints", false);
    schwarz1.setAppearance(app);
    schwarz2.setAppearance(app);
    tds.setContent(schwarz1, bounds, true);
    bounds = new Rectangle3D(new double[][]{{-11,1,1},{-9,4,-1}});
    tds.setContent(schwarz2, bounds, false);
    
    //ViewerApp.displayOld(schwarz1);
  }

  public SceneGraphComponent getSceneRoot() {
    return sceneRoot;
  }
  
  public void setLightIntensity(double intensity) {
    light.setIntensity(intensity);
  }

  public double getLightIntensity() {
    return light.getIntensity();
  }

}
