package de.jreality.portal;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.jreality.jogl.Viewer;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.pick.PickSystem;
import de.jreality.scene.tool.DraggingTool;
import de.jreality.scene.tool.EncompassTool;
import de.jreality.scene.tool.RotateTool;
import de.jreality.scene.tool.ShipNavigationTool;
import de.jreality.scene.tool.ToolSystemViewer;
import de.jreality.scene.tool.config.ToolSystemConfiguration;
import de.jreality.shader.CommonAttributes;
import de.jreality.soft.DefaultViewer;
import de.jreality.util.RenderTrigger;
import de.jreality.util.SceneGraphUtility;
import de.smrj.tcp.TCPBroadcasterIO;
import de.smrj.tcp.TCPBroadcasterNIO;

public class PortalFileViewer {
  
  private static ToolSystemViewer viewer;
  private static SceneGraphComponent defaultRoot;
  private static SceneGraphPath defaultCamPath;
  private static double fileScale = 1;
  private static SceneGraphComponent world=new SceneGraphComponent();
  
  private static String[] filenames;
  
  private static List fileNodes = new LinkedList();
  
  private static String usage(CmdLineParser parser) {
    String ret = "usage: java PortalServerImplementation "
        + parser.usageString() + " <filename> .. <filename>\n";
    ret += parser.descriptionString();
    ret += "\tfilename(s)\t3D data file\n";
    return ret;
  }

  public static void main(String[] args) throws Exception {
    CmdLineParser parser = new CmdLineParser();
    CmdLineParser.Option ioOption = parser.addBooleanOption("io");
    parser.addDescription("io", "use blocking io");
    CmdLineParser.Option propOpt = parser.addStringOption('p', "properties");
    parser.addDescription("properties", "the jreality property file to use");
    CmdLineParser.Option scaleOpt = parser.addDoubleOption('s', "scale");
    parser.addDescription("scale", "the global scale for displaying the given scene");
    CmdLineParser.Option helpOpt = parser.addBooleanOption('h', "help");
    parser.addDescription("help", "print this usage message");
    CmdLineParser.Option viewerOpt = parser.addStringOption('v', "viewer");
    parser.addDescription("viewer", "select viewer type [soft, jogl, portal]");
    
    parser.parse(args);
    if (((Boolean) parser.getOptionValue(helpOpt)).booleanValue()) {
      System.out.println(usage(parser));
      System.exit(0);
    }
    String propFile = (String) parser.getOptionValue(propOpt);
    if (propFile != null) {
      System.setProperty("jreality.config", new File(propFile).getAbsolutePath());
    }
    System.out.println("jreality.config: "+System.getProperty("jreality.config"));
    String[] dataArgs = parser.getRemainingArgs();
    if (dataArgs == null) {
      System.err.println("need filename(s)!\n"+usage(parser));
      System.exit(2);
    }
    Boolean b = (Boolean) parser.getOptionValue(ioOption);
    boolean io = b.booleanValue();
    if (io) System.err.println("Warning: using blocking IO");
    Double scale = (Double) parser.getOptionValue(scaleOpt);
    if (scale != null) fileScale = scale.doubleValue();
    String viewerType = (String) parser.getOptionValue(viewerOpt);
    if (viewerType == null || viewerType.equalsIgnoreCase("PORTAL")) {
      viewer = new ToolSystemViewer(new PortalServerViewer(
          io ? new TCPBroadcasterIO(8868).getRemoteFactory()
              : new TCPBroadcasterNIO(8868).getRemoteFactory()), ToolSystemConfiguration.loadDefaultPortalConfiguration());
    } else {
      ToolSystemConfiguration tsc = ToolSystemConfiguration.loadDefaultDesktopConfiguration();
      if (viewerType.equalsIgnoreCase("SOFT")) {
        viewer = new ToolSystemViewer(new DefaultViewer(), tsc);
      } else if (viewerType.equalsIgnoreCase("JOGL")){
        viewer = new ToolSystemViewer(new Viewer(), tsc);
      } else {
        System.err.println("unknown viewer: "+viewerType+"\n"+usage(parser));
        System.exit(2);
      }
      JFrame f = new JFrame("jReality");
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      f.setSize(400, 300);
      f.getContentPane().add("Center", viewer.getViewingComponent());
      f.show();
    }
    setUpWorld();
    for (int i = 0; i < dataArgs.length; i++) {
      loadFile(dataArgs[i]);
      System.out.println("loaded file ["+dataArgs[i]+"]");
    }
    if (dataArgs.length > 1) {
      filenames = dataArgs;
      createToggleFrame();
    }
    applyBackgroundColor(new java.awt.Color(1f, 0.2f, 0.2f));
  }

  private static void createToggleFrame() {
    JFrame jf = new JFrame("jReality control");
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(fileNodes.size(), 1));
    for (int i = 0; i < filenames.length; i++) {
      final SceneGraphComponent cmp = (SceneGraphComponent) fileNodes.get(i);
      final JCheckBox cb = new JCheckBox(filenames[i]);
      cb.setSelected(true);
      cb.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cmp.setVisible(cb.isSelected());
        }
      });
      panel.add(cb);
    }
    jf.getContentPane().add("Center", panel);
    jf.pack();
    jf.setLocation(400, 10);
    jf.show();
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

  private static void loadFile(String name) {
    long t = System.currentTimeMillis();
    try {
      de.jreality.scene.SceneGraphComponent file = Readers.read(new File(name));
      System.out.println("PortalServerViewer.loadFile() load file done. Now set up world:");
      world.addChild(file);
      fileNodes.add(file);
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
  private static void setUpWorld() {
      
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
      defaultRoot.addChild(world);
      world.addTool(new DraggingTool());
      world.addTool(new EncompassTool());
      world.addTool(new RotateTool());
      world.setTransformation(new Transformation());
      world.getTransformation().setMatrix(
          MatrixBuilder.euclidian().scale(fileScale ).getMatrix()
              .getArray());
      viewer.setSceneRoot(defaultRoot);
      viewer.setCameraPath(defaultCamPath);
      SceneGraphPath avatarPath = defaultCamPath.popNew();
      avatarPath.pop();
      viewer.setAvatarPath(avatarPath);
      SceneGraphPath defaultPick = new SceneGraphPath();
      defaultPick.push(defaultRoot);
      defaultPick.push(world);
      viewer.setEmptyPickPath(defaultPick);
      PickSystem ps = new AABBPickSystem();
      viewer.setPickSystem(ps);
      RenderTrigger rt = new RenderTrigger();
      rt.addViewer(viewer);
      rt.addSceneGraphComponent(defaultRoot);
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
