package de.jreality.examples.tooldemo;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.swing.ScenePanel;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;

public class SchwarzDemo extends ToolDemoContent {
  
  private static SceneGraphComponent domain() {
    SceneGraphComponent domain;
    try {
      domain = Readers.read(Input.getInput("3ds/schwarz.3ds"));
    } catch (IOException e) {
      RuntimeException re = new RuntimeException("could not load schwarz: "+e.getMessage());
      re.initCause(e);
      throw re;
    }
    MatrixBuilder mb = MatrixBuilder.euclidean();
    mb.rotateY(-0.39283794);
    mb.translate(0,-0.5,0);
    mb.scale(1/16.203125);
    mb.assignTo(domain);
    domain=GeometryUtility.flatten(domain);
    domain=domain.getChildComponent(0);
    domain.setName("domain");
    PickUtility.assignFaceAABBTrees(domain);
    domain.getGeometry().setName("schwarz");
    // now domain is aligned along x,y,z axes and scaled to size 1,1,1
    return domain;
  }

  final SceneGraphComponent domain=domain();

  public SchwarzDemo() throws IOException {
    super(new SceneGraphComponent(),
        new Rectangle3D(new double[][]{{-5,0,-5},{5,10,-15}}),  
        new Rectangle3D(new double[][]{{-.5,-.5,-.5},{.5,.5,.5}})
    );
    domain.addTool(new AbstractTool(InputSlot.getDevice("PanelActivation")) {
      public void activate(ToolContext tc) {
        PickResult pick = tc.getCurrentPick();
        double[] coords = pick.getObjectCoordinates();
        // get max dir:
        int dir=0;
        if (Math.abs(coords[1])>Math.abs(coords[0])) dir=1;
        if (Math.abs(coords[2])>Math.abs(coords[dir])) dir=2;
        double[] trans=new double[3];
        trans[dir]=Math.signum(coords[dir]);
        SceneGraphComponent newCmp = new SceneGraphComponent();
        newCmp.setGeometry(domain.getGeometry());
        MatrixBuilder.euclidean().translate(trans).assignTo(newCmp);
        //pick.getPickPath().getLastComponent().addChild(newCmp);
        tc.getRootToLocal().getLastComponent().addChild(newCmp);
      }
    });
    Appearance app = new Appearance();
    app.setAttribute("showPoints", false);
    getContent().setAppearance(app);
    getContent().addChild(domain);
    Texture2D tex = TextureUtility.createTexture(app, "polygonShader", Input.getInput("textures/schwarz.png"));
    tex.setBlendColor(Color.blue);
  }
  
  public static void main(String[] args) throws IOException {
    System.setProperty("jreality.data", "/net/MathVis/data/testData3D");
    //System.setProperty("de.jreality.scene.Viewer", "de.jreality.soft.DefaultViewer");
    //System.setProperty("de.jreality.ui.viewerapp.autoRender", "false");
    System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
    ToolDemoScene tds = new ToolDemoScene();
    tds.update();
    
    SchwarzDemo schwarzDemo = new SchwarzDemo();
    tds.setContent(schwarzDemo);
    
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
    
    
    
//    MenuFactory menu = new MenuFactory(va);
//    menu.addMenuToFrame();
//    menu.addContextMenuToNavigator();

  }
}
