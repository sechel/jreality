package de.jreality.jogl;

import java.awt.Frame;
import java.io.IOException;

import javax.swing.JOptionPane;

import de.jreality.io.JrScene;
import de.jreality.reader.ReaderJRS;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.tool.ToolSystemViewer;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.RenderTrigger;
import de.smokering.util.PointSets;

public class CalculationTest {

//  public static void main(String[] args) {
//    ExampleCalculation ev = new ExampleCalculation();
//    ev.setValues(new float[]{1.1f,1.2f,1.3f,1.4f,2.1f,2.2f,2.3f,2.4f,3.1f,3.2f,3.3f,3.4f}); //,4.1f,4.2f,4.3f,4.4f});
//    ev.triggerCalculation();
//    ToolSystemViewer viewer = new ToolSystemViewer(new GpgpuViewer(ev));
//    try {
//      ReaderJRS r = new ReaderJRS();
//      r.setInput(new Input(ViewerApp.class.getResource("desktop-scene.jrs")));
//      JrScene scene = r.getScene();
//      viewer.setSceneRoot(scene.getSceneRoot());
//      viewer.setCameraPath(scene.getPath("cameraPath"));
//      viewer.setEmptyPickPath(scene.getPath("emptyPickPath"));
//    } catch (IOException ioe) {
//      JOptionPane.showMessageDialog(null, "Load failed: "+ioe.getMessage());
//    }
//    viewer.setPickSystem(new AABBPickSystem());
//    RenderTrigger rt = new RenderTrigger();
//    rt.addSceneGraphComponent(viewer.getSceneRoot());
//    rt.addViewer(viewer);
//    Frame f = new Frame("bla");
//    f.add("Center", viewer.getViewingComponent());
//    f.setSize(200, 200);
//    f.show();
//  }
  
  public static void main(String[] args) {
    SmokeCalculation ev = new SmokeCalculation();
//    ev.setValues(new float[]{1.1f,1.2f,1.3f,1.4f,2.1f,2.2f,2.3f,2.4f,3.1f,3.2f,3.3f,3.4f}); //,4.1f,4.2f,4.3f,4.4f});
//    ev.setValues(new float[]{1,0,0,1, 1,0,0,1, 1,0,0,1}); //,4.1f,4.2f,4.3f,4.4f});
//    ev.setData(new float[]{0.01f, 0,1,0,0, 1,0,0,1, 0,-1,0,1, -1,0,0,1, 0,1,0,1,   0,1,0,0, 1,0,0,1, 0,-1,0,1, -1,0,0,1, 0,1,0,1});
    ev.setData(new float[]{0.001f, 0,2,0,0, 2,0,0,1, 0,-2,0,1, -2,0,0,1, 0,2,0,1,   0,2,0,0, 2,0,0,1, 0,-2,0,1, -2,0,0,1, 0,2,0,1});
    ev.setRo(0.001);
    ev.triggerCalculation();
    if (false) {
      GpgpuViewer gv = new GpgpuViewer();
      gv.setCalculation(ev);
      ToolSystemViewer viewer = new ToolSystemViewer(gv);
      try {
        ReaderJRS r = new ReaderJRS();
        r.setInput(new Input(ViewerApp.class.getResource("desktop-scene.jrs")));
        JrScene scene = r.getScene();
        viewer.setSceneRoot(scene.getSceneRoot());
        viewer.setCameraPath(scene.getPath("cameraPath"));
        viewer.setEmptyPickPath(scene.getPath("emptyPickPath"));
      } catch (IOException ioe) {
        JOptionPane.showMessageDialog(null, "Load failed: "+ioe.getMessage());
      }
      viewer.setPickSystem(new AABBPickSystem());
      RenderTrigger rt = new RenderTrigger();
      rt.addSceneGraphComponent(viewer.getSceneRoot());
      rt.addViewer(viewer);
      Frame f = new Frame("bla");
      f.add("Center", viewer.getViewingComponent());
      f.setSize(200, 200);
      f.show();
    } else {
      ev.setDisplayTexture(true);
//      ev.setValues(PointSets.randomFloats(64*64, null, 0, 1));
      ev.setValues(PointSets.cubeGridFloats(32, null, 0.01));
      ev.triggerCalculation();
      GpgpuUtility.run(ev);
    }
  }

}
