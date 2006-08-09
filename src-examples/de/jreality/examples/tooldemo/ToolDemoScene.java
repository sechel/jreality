package de.jreality.examples.tooldemo;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import de.jreality.tools.HeadTransformationTool;
import de.jreality.tools.PickShowTool;
import de.jreality.tools.RotateTool;
import de.jreality.tools.ShipNavigationTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;

public class ToolDemoScene {

  HashMap<SceneGraphComponent, SceneGraphComponent> contentComponents = new HashMap<SceneGraphComponent, SceneGraphComponent>();
  
  private SceneGraphComponent sceneRoot=new SceneGraphComponent(),
                      sceneNode=new SceneGraphComponent(),
                      avatarNode=new SceneGraphComponent(),
                      camNode=new SceneGraphComponent(),
                      lightNode=new SceneGraphComponent(),
                      lightNode2=new SceneGraphComponent(),
                      lightNode3=new SceneGraphComponent(),
                      lightNode4=new SceneGraphComponent(),
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
    lightNode.setName("light 1");
    lightNode2.setName("light 2");
    lightNode3.setName("light 3");
    lightNode4.setName("light 4");
    sceneRoot.addChild(sceneNode);
    
    sceneRoot.setAppearance(rootAppearance);

    terrainAppearance.setAttribute("showLines", false);
    terrainAppearance.setAttribute("showPoints", false);
    terrainAppearance.setAttribute("diffuseColor", Color.white);

    Camera cam = new Camera();
    cam.setNear(0.01);
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

    lightNode4.setLight(light);
    MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,1,1}).assignTo(lightNode3);
    sceneRoot.addChild(lightNode4);

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
    avatarNode.addTool(shipNavigationTool);
    camNode.addTool(new HeadTransformationTool());
    
    sceneRoot.addTool(new PickShowTool(null, 0.005));
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
      terrainNode = Readers.read(Input.getInput("de/jreality/examples/tooldemo/resources/terrain.3ds")).getChildComponent(0);
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
    terrainGeom.setName("terrain Geometry");
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

  public void setContent(ToolDemoContent content) {
    setContent(content.getContent(), content.getContentBounds(), content.getPlacementBounds(), content.getKeepAspectRatio());
  }
  
  public void removeContent(ToolDemoContent content) {
    removeContent(content.getContent());
  }

  public void setContent(SceneGraphComponent content, Rectangle3D bounds, boolean keepRatio) {
    Rectangle3D contentBounds = GeometryUtility.calculateBoundingBox(content);
    setContent(content, contentBounds, bounds, keepRatio);
  }
  
  public void removeContent(SceneGraphComponent cmp) {
    sceneNode.removeChild(contentComponents.remove(cmp));
  }
  
  public void setContent(SceneGraphComponent content, Rectangle3D contentBounds, Rectangle3D bounds, boolean keepRatio) {
    if (contentComponents.containsKey(content)) throw new IllegalArgumentException("already added "+content);
    SceneGraphComponent placement = new SceneGraphComponent();
    contentComponents.put(content, placement);
    placement.setName("placement: ["+content.getName()+"]");
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
    mb.assignTo(placement);
    //contentBounds = GeometryUtility.calculateBoundingBox(content);
    double[] bds = bounds.getCenter();
    bds[0]/=scale[0]; bds[1]/=scale[1]; bds[2]/=scale[2];
    double[] t = Rn.subtract(null, bds, contentBounds.getCenter());
    mb.translate(t);
    mb.assignTo(placement);
    sceneNode.addChild(placement);
    placement.addChild(content);
//    contentBounds = GeometryUtility.calculateBoundingBox(content);
//    System.out.println("cb:\n"+contentBounds);//+"\n\nbb:\n"+bounds);
//    placement.addChild(bounds(contentBounds));
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
  
  public SceneGraphComponent getSceneRoot() {
    return sceneRoot;
  }
  
  public void setLightIntensity(double intensity) {
    light.setIntensity(intensity);
  }

  public double getLightIntensity() {
    return light.getIntensity();
  }

  public SceneGraphComponent getTerrainNode() {
    return terrainNode;
  }

  public static void main(String[] args) throws IOException {
    
    System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
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
    
    final JSlider sl = new JSlider();
    sl.setMinimum(0);
    sl.setMaximum(10000);
    
    JFrame f = new JFrame("skybox selection");
    f.getContentPane().add(l.selectionComponent);
    f.getContentPane().add(sl);
    f.pack();
    f.setVisible(true);
    
    Rectangle3D bounds = new Rectangle3D(new double[][]{{-1,1,-1},{1,3,-3}});
    final SceneGraphComponent schwarz1 = Readers.read(Input.getInput("3ds/schwarz.3ds"));
    final SceneGraphComponent schwarz2 = Readers.read(Input.getInput("3ds/schwarz.3ds"));
    MatrixBuilder mb = MatrixBuilder.euclidean();
    mb.rotateY(-0.39283794);
    mb.assignTo(schwarz2);
    Appearance app = new Appearance();
    app.setAttribute("showPoints", false);
    schwarz1.setAppearance(app);
    schwarz2.setAppearance(app);
    //tds.setContent(schwarz1, bounds, true);
    //bounds = new Rectangle3D(new double[][]{{-11,1,1},{-9,4,-1}});
    tds.setContent(schwarz2, bounds, true);
    
    sl.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        double angle = -0.39283794+(-5000+sl.getValue())*0.000000001;
        System.out.println("angle="+angle);
        MatrixBuilder.euclidean().rotateY(angle).assignTo(schwarz2);
        System.out.println(Arrays.toString(GeometryUtility.calculateBoundingBox(schwarz2).getExtent()));
      }
    });

    //ViewerApp.displayOld(schwarz1);
  }

}
