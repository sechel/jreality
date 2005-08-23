package de.jreality.portal;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.reader.Readers;
import de.jreality.scene.*;
import de.jreality.scene.pick.PickSystem;
import de.jreality.scene.pick.SoftPickSystem;
import de.jreality.scene.tool.DraggingTool;
import de.jreality.scene.tool.ShipNavigationTool;
import de.jreality.scene.tool.ToolSystemViewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.smrj.tcp.TCPBroadcasterIO;
import de.smrj.tcp.TCPBroadcasterNIO;

public class PortalFileViewer {
  
  private static ToolSystemViewer viewer;
  private static SceneGraphComponent defaultRoot;
  private static SceneGraphPath defaultCamPath;
  private static double fileScale = 1;

  private static String usage(CmdLineParser parser) {
    String ret = "usage: java PortalServerImplementation "
        + parser.usageString() + " <filename | classname>\n";
    ret += parser.descriptionString();
    ret += "\tfilename\t3D data file\n";
    ret += "\tclassname\tfull classname of class that implements de.jreality.scene.LoadableScene\n";
    return ret;
  }

  public static void main(String[] args) throws Exception {
    CmdLineParser parser = new CmdLineParser();
    CmdLineParser.Option ioOption = parser.addBooleanOption("io");
    parser.addDescription("io", "use blocking io");
    CmdLineParser.Option propOpt = parser
        .addStringOption('p', "properties");
    parser
        .addDescription("properties",
            "the jreality property file to use");
    CmdLineParser.Option scaleOpt = parser.addDoubleOption('s', "scale");
    parser.addDescription("scale",
        "the global scale for displaying the given scene");
    CmdLineParser.Option helpOpt = parser.addBooleanOption('h', "help");
    parser.addDescription("help", "print this usage message");
    parser.parse(args);
    if (((Boolean) parser.getOptionValue(helpOpt)).booleanValue()) {
      System.out.println(usage(parser));
      System.exit(0);
    }
    String propFile = (String) parser.getOptionValue(propOpt);
    if (propFile != null) {
      System.setProperty("jreality.config", new File(propFile).getAbsolutePath());
    }
    System.out.println("jreality.config: "
        + System.getProperty("jreality.config"));
    String[] dataArgs = parser.getRemainingArgs();
    if (dataArgs == null || dataArgs.length != 1) {
      System.err.println(usage(parser));
      System.exit(2);
    }
    Boolean b = ((Boolean) parser.getOptionValue(ioOption));
    boolean io = b.booleanValue();
    if (io) System.err.println("Warning: using blocking IO");
    Double scale = (Double) parser.getOptionValue(scaleOpt);
    if (scale != null) fileScale = scale.doubleValue();
    
    viewer = new ToolSystemViewer(new PortalServerViewer(
        io ? new TCPBroadcasterIO(8868).getRemoteFactory()
            : new TCPBroadcasterNIO(8868).getRemoteFactory()));

    String name = dataArgs[0];
    loadFile(name);
    System.out.println("loaded file ["+name+"]");
    applyBackgroundColor(new java.awt.Color(0f, 0.2f, 0.2f));
  }

  private static void applyBackgroundColor(Color color) {
    if (defaultRoot != null) {
      Appearance ap = defaultRoot.getAppearance();
      if (ap == null) {
        ap = new Appearance();
        defaultRoot.setAppearance(ap);
      }
      ap.setAttribute(CommonAttributes.BACKGROUND_COLOR, color);
    }
  }

  private static void initDefaultScene() {
    defaultRoot = new SceneGraphComponent();
    defaultRoot.setName("root node");
    SceneGraphComponent portal = new SceneGraphComponent();
    portal.setTransformation(new Transformation());
    portal.setName("portal node");
    SceneGraphComponent camNode = new SceneGraphComponent();
    camNode.setName("camera node");
    defaultRoot.addChild(portal);
    portal.addChild(camNode);
    ShipNavigationTool shipNavigationTool = new ShipNavigationTool();
    shipNavigationTool.setGravity(0);
    portal.addTool(shipNavigationTool);
    Camera camera = new Camera();
    camNode.setCamera(camera);
    defaultCamPath = new SceneGraphPath();
    defaultCamPath.push(defaultRoot);
    defaultCamPath.push(portal);
    defaultCamPath.push(camNode);
    defaultCamPath.push(camera);
    defaultRoot.addChild(makeLights());
  }

  private static void loadFile(String name) {
    long t = System.currentTimeMillis();
    try {
      de.jreality.scene.SceneGraphComponent world = Readers
          .read(new File(name));
      System.out
          .println("PortalServerViewer.loadFile() load file done. Now set up world:");
      setUpWorld(world);
      System.out.println("Set up world done...");
      long s = System.currentTimeMillis() - t;
      System.out.println("loaded file " + name + " successful. [" + s
          + "ms]");
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("loading file " + name + " failed!");
    }
  }

  /**
   * @param world
   */
  private static void setUpWorld(de.jreality.scene.SceneGraphComponent world) {
    if (world != null) {
      if (defaultRoot == null || !defaultCamPath.isValid()) {
        initDefaultScene();
      }
      defaultRoot.addChild(world);
      world.addTool(new DraggingTool());
      world.setTransformation(new Transformation());
      world.getTransformation().setMatrix(
          MatrixBuilder.euclidian().scale(fileScale ).getMatrix()
              .getArray());
      viewer.setSceneRoot(defaultRoot);
      viewer.setCameraPath(defaultCamPath);
      SceneGraphPath avatarPath = defaultCamPath.popNew();
      avatarPath.pop();
      viewer.setAvatarPath(avatarPath);
      PickSystem ps = new SoftPickSystem();
      viewer.setPickSystem(ps);
    }
  }

  static SceneGraphComponent makeLights() {
    SceneGraphComponent lights = new SceneGraphComponent();
    lights.setName("lights");
    SpotLight pl = new SpotLight();
    pl.setFalloff(1.0, 0.0, 0.0);
    pl.setColor(new Color(120, 250, 180));
    pl.setConeAngle(Math.PI);
    pl.setIntensity(0.6);
    SceneGraphComponent l0 = SceneGraphUtility
        .createFullSceneGraphComponent("light0");
    l0.setLight(pl);
    lights.addChild(l0);
    DirectionalLight dl = new DirectionalLight();
    dl.setColor(new Color(250, 100, 255));
    dl.setIntensity(0.6);
    l0 = SceneGraphUtility.createFullSceneGraphComponent("light1");
    double[] zaxis = { 0, 0, 1 };
    double[] other = { 1, 1, 1 };
    l0.getTransformation().setMatrix(
        P3.makeRotationMatrix(null, zaxis, other));
    l0.setLight(dl);
    lights.addChild(l0);
    return lights;
  }

}
