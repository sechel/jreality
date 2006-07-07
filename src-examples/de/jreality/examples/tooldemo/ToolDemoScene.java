package de.jreality.examples.tooldemo;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JFrame;

import de.jreality.examples.CatenoidHelicoid;
import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.Viewer;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.soft.DefaultViewer;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.HeadTransformationTool;
import de.jreality.tools.PickShowTool;
import de.jreality.tools.RotateTool;
import de.jreality.tools.ShipNavigationTool;
import de.jreality.toolsystem.ToolSystemViewer;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.RenderTrigger;

public class ToolDemoScene {

  SceneGraphComponent rootNode=new SceneGraphComponent(),
                      sceneNode=new SceneGraphComponent(),
                      avatarNode=new SceneGraphComponent(),
                      camNode=new SceneGraphComponent(),
                      lightNode=new SceneGraphComponent(),
                      terrainNode;
  
  SceneGraphPath camPath, avatarPath, emptyPickPath;
  
  public ToolDemoScene() throws IOException {
    
    rootNode.setName("root");
    rootNode.setAppearance(new Appearance());
    sceneNode.setName("scene");
    avatarNode.setName("avatar");
    camNode.setName("camNode");
    lightNode.setName("lights");
    rootNode.addChild(sceneNode);
    
    // prepare terrain
    terrainNode = Readers.read(Input.getInput("/net/MathVis/data/testData3D/3ds/terrain.3ds"));
    terrainNode.setName("terrain");
    MatrixBuilder.euclidean().translate(0, 3, 0).scale(1/3.).assignTo(terrainNode);
    IndexedFaceSet terrainGeom = (IndexedFaceSet) terrainNode.getChildComponent(0).getGeometry();
    GeometryUtility.calculateAndSetNormals(terrainGeom);
    PickUtility.assignFaceAABBTree(terrainGeom);
    terrainNode.setAppearance(new Appearance());
    terrainNode.getAppearance().setAttribute("showLines", false);
    terrainNode.getAppearance().setAttribute("showPoints", false);

    rootNode.addChild(terrainNode);
    
    // lights
    DirectionalLight dl = new DirectionalLight();
    lightNode.setLight(dl);
    MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,1,-1}).assignTo(lightNode);
    rootNode.addChild(lightNode);
    
    Camera cam = new Camera();
    cam.setFar(500);

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
    shipNavigationTool.setGain(7);
    //shipNavigationTool.setGravity(100);
    //shipNavigationTool.setJumpSpeed(20);
    avatarNode.addTool(shipNavigationTool);
    camNode.addTool(new HeadTransformationTool());
    
    terrainNode.addTool(new PickShowTool(null));
    //avatarNode.addTool(new PointerDisplayTool());
    
    sceneNode.addTool(new RotateTool());
    sceneNode.addTool(new DraggingTool());
    
    CatenoidHelicoid catenoidHelicoid = new CatenoidHelicoid(40);
    PickUtility.assignFaceAABBTree(catenoidHelicoid);
    sceneNode.setGeometry(catenoidHelicoid);
    sceneNode.setAppearance(new Appearance());
    sceneNode.getAppearance().setAttribute("showPoints", false);
    sceneNode.getAppearance().setAttribute("showLines", false);
    
  }
  
  public static void main(String[] args) throws IOException {
    ToolDemoScene tds = new ToolDemoScene();
    //System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
    //Viewer viewer = new Viewer();
    de.jreality.scene.Viewer viewer = new DefaultViewer();
    boolean syncRender=true;
    ToolSystemViewer ts = new ToolSystemViewer(viewer, ToolSystemConfiguration.loadDefaultConfiguration(), syncRender);
    ts.setSceneRoot(tds.rootNode);
    ts.setCameraPath(tds.camPath);
    ts.setEmptyPickPath(tds.emptyPickPath);
    ts.setAvatarPath(tds.avatarPath);
    
    JFrame f = new JFrame("[syncRender="+syncRender+"]");
    f.setSize(400, 260);
    f.getContentPane().add((Component) ts.getViewingComponent());
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    if (!syncRender) {
      RenderTrigger rt = new RenderTrigger();
      rt.addSceneGraphComponent(tds.rootNode);
      rt.addViewer(ts);
    }
    ts.initializeTools();
}

}
