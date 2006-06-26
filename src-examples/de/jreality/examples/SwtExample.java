package de.jreality.examples;

import javax.swing.JFrame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import de.jreality.jogl.SwtQueue;
import de.jreality.jogl.SwtViewer;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.tool.DraggingTool;
import de.jreality.scene.tool.RotateTool;
import de.jreality.scene.tool.ToolSystemViewer;
import de.jreality.swing.JRJComponent;
import de.jreality.util.RenderTrigger;

public class SwtExample {

  public static void main(String[] args) throws Exception {
    
    // create scene
    
    SceneGraphComponent rootNode=new SceneGraphComponent();
    SceneGraphComponent geometryNode=new SceneGraphComponent();
    SceneGraphComponent cameraNode=new SceneGraphComponent();
    SceneGraphComponent lightNode=new SceneGraphComponent();
    
    rootNode.addChild(geometryNode);
    rootNode.addChild(cameraNode);
    cameraNode.addChild(lightNode);
    
    final CatenoidHelicoid geom=new CatenoidHelicoid(50);
    geom.setAlpha(Math.PI/2.-0.3);
    
    Camera camera=new Camera();
    Light light=new DirectionalLight();
    
    geometryNode.setGeometry(geom);
    cameraNode.setCamera(camera);
    lightNode.setLight(light);

    Appearance app=new Appearance();
    //app.setAttribute(CommonAttributes.FACE_DRAW, false);
    //app.setAttribute("diffuseColor", Color.red);
    //app.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
    //app.setAttribute(CommonAttributes.TRANSPARENCY, 0.4);
    //app.setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.blue);
    rootNode.setAppearance(app);
    
    MatrixBuilder.euclidean().rotateY(Math.PI/6).assignTo(geometryNode);
    MatrixBuilder.euclidean().translate(0, 0, 12).assignTo(cameraNode);
    MatrixBuilder.euclidean().rotate(-Math.PI/4, 1, 1, 0).assignTo(lightNode);

    SceneGraphPath cameraPath=new SceneGraphPath();
    cameraPath.push(rootNode);
    cameraPath.push(cameraNode);
    cameraPath.push(camera);
    
    final ToolSystemViewer viewer;
    
    // true for SWT, false for AWT
    if (true) {
      // create Shell, GLCanvas and SwtViewer 
      SwtQueue f = SwtQueue.getInstance();
      final Shell shell = f.createShell();
      final GLCanvas[] can = new GLCanvas[1];
      
      f.waitFor(new Runnable() {
      public void run() {
        shell.setLayout(new FillLayout());
        Composite comp = new Composite(shell, SWT.NONE);
        comp.setLayout(new FillLayout());
        GLData data = new GLData ();
        data.doubleBuffer = true;
        System.out.println("data.depthSize="+data.depthSize);
        data.depthSize = 8;
        can[0] = new GLCanvas(comp, SWT.NONE, data);
        can[0].setCurrent();
        shell.setText("SWT");
        shell.setSize(640, 480);
        shell.open();
      }
      });
      final SwtViewer swtViewer=new SwtViewer(can[0]);
      
      // enable tools
      viewer = new ToolSystemViewer(swtViewer);
    }
    else {
      viewer = new ToolSystemViewer(new de.jreality.jogl.Viewer());
      JFrame f = new JFrame("AWT");
      f.setSize(640, 480);
      f.getContentPane().add(viewer.getViewingComponent());
      f.setVisible(true);
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    viewer.setPickSystem(new AABBPickSystem());
    
    viewer.setSceneRoot(rootNode);
    viewer.setCameraPath(cameraPath);

    viewer.initializeTools();
    
    // add tools
    geometryNode.addTool(new RotateTool());
    geometryNode.addTool(new DraggingTool());
    
    PaintComponent pc = new PaintComponent();
    JRJComponent jrj = new JRJComponent();
    jrj.add(pc);
    
    geometryNode.setAppearance(jrj.getAppearance());
    geometryNode.addTool(jrj.getTool());
    
    // add a render trigger for auto redraw
    RenderTrigger rt = new RenderTrigger();
    rt.addViewer(viewer);
    rt.addSceneGraphComponent(rootNode);
    
  }

}
