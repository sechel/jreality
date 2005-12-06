package de.jreality.portal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.beans.Statement;
import java.util.List;

import javax.swing.JFrame;

import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.proxy.scene.RemoteSceneGraphComponent;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;

public class HeadTrackedViewer implements Viewer, RemoteViewer {

  Viewer viewer;
  private ConfigurationAttributes config;
  private SceneGraphComponent cameraTranslationNode;
  private SceneGraphComponent cameraOrientationNode;
  double[] tmpHead = new double[16];
  private boolean hasSceneRoot;
  private boolean hasCamPath;
  private SceneGraphComponent headComponent;
  SceneGraphPath portalPath;

  Camera cam;

  // this field moves the sensor from the middle
  // of the glasses to its real position - rotate and translate to the left... 
  static double[] sensorCorrection;
  static {
    double angle = -Math.PI/2;
    //sensorOrientationCorrection = P3.makeRotationMatrix(null, axis, angle);
    sensorCorrection = MatrixBuilder.euclidean().rotateX(angle).translate(-0.08, 0, 0).getMatrix().getArray();
  }
  
  public static HeadTrackedViewer createFullscreen(Class viewerClass) {
    System.setProperty("de.jreality.portal.HeadTrackedViewer", viewerClass.getName());
    return createFullscreen();
  }

  public static HeadTrackedViewer createFullscreen() {
    JFrame frame = new JFrame("no title");
    HeadTrackedViewer hv = new HeadTrackedViewer();
    frame.getContentPane().add(hv.getViewingComponent());
    // disable mouse cursor in fullscreen mode
    BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D gfx = cursorImg.createGraphics();
    gfx.setColor(new Color(0, 0, 0, 0));
    gfx.fillRect(0, 0, 16, 16);
    gfx.dispose();
    frame.setCursor(frame.getToolkit().createCustomCursor(cursorImg,
        new Point(), ""));
    frame.dispose();
    frame.setUndecorated(true);
    frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(frame);
    frame.validate();
    frame.show();
    return hv;
  }
  
  public HeadTrackedViewer() {
    String delegated = System.getProperty("de.jreality.portal.HeadTrackedViewer", "de.jreality.jogl.Viewer");
    try {
      viewer = (Viewer) Class.forName(delegated).newInstance();
    } catch (Exception e) {
      throw new Error("Viewer creation failed!");
    }
    init();
  }
  
  public HeadTrackedViewer(Class delegateClass) {
    try {
      viewer = (Viewer) delegateClass.newInstance();
    } catch (Exception e) {
      throw new Error("Viewer creation failed!");
    }
    init();
  }

  private void init() {
    config = ConfigurationAttributes.getDefaultConfiguration();
    cameraTranslationNode = new SceneGraphComponent();
    cameraTranslationNode.setTransformation(new Transformation());

    cameraOrientationNode = new SceneGraphComponent();
    // set camera orientation to value from config file...
    double[] rot = config.getDoubleArray("camera.orientation");
    MatrixBuilder.euclidian()
      .rotate(rot[0] * ((Math.PI * 2.0) / 360.),
        new double[] { rot[1], rot[2], rot[3] })
      .assignTo(cameraOrientationNode);

    cameraTranslationNode.addChild(cameraOrientationNode);
  }

  public SceneGraphComponent getAuxiliaryRoot() {
    return viewer.getAuxiliaryRoot();
  }

  public SceneGraphPath getCameraPath() {
    return viewer.getCameraPath();
  }

  public SceneGraphComponent getSceneRoot() {
    return viewer.getSceneRoot();
  }

  public int getSignature() {
    return viewer.getSignature();
  }

  public Component getViewingComponent() {
    return viewer.getViewingComponent();
  }

  public boolean hasViewingComponent() {
    return viewer.hasViewingComponent();
  }

  public void render() {
    if (!hasSceneRoot || !hasCamPath) return;
    setHeadMatrix(headComponent.getTransformation().getMatrix(tmpHead));
    viewer.render();
  }

  public void setAuxiliaryRoot(SceneGraphComponent ar) {
    viewer.setAuxiliaryRoot(ar);
  }

  public void setCameraPath(SceneGraphPath camPath) {
    // dispose artificial camera path
    if (viewer.getCameraPath() != null) {
      SceneGraphPath last = viewer.getCameraPath();
      last.pop(); // Camera
      last.getLastComponent().setCamera(null);
      last.pop(); // Orientation
      last.pop(); // Translation
      last.getLastComponent().removeChild(cameraTranslationNode);
    }
    hasCamPath = !(camPath == null || camPath.getLength() == 0);
    // empty path => reset fields
    if (camPath == null || camPath.getLength() == 0) {
      viewer.setCameraPath(null);
      headComponent = null;
      portalPath = null;
      cam = null;
      return;
    }
    // new camera path => extract headComponent and set artificial camera path
    cam = (Camera) camPath.getLastElement();
    // TODO: do these settings on client side...
    cam.setStereo(config.getBool("camera.stereo"));
    cam.setEyeSeparation(config.getDouble("camera.eyeSeparation"));
    cam.setOnAxis(false);

    camPath.pop();

    headComponent = camPath.getLastComponent();
    
    camPath.pop(); // now this should be the portal path
    portalPath = (SceneGraphPath) camPath.clone();
    
    // add camera position and orientation, add camera there
    camPath.getLastComponent().addChild(cameraTranslationNode);
    cameraOrientationNode.setCamera(cam);
    
    // build the right camera path
    camPath.push(cameraTranslationNode);
    camPath.push(cameraOrientationNode);
    camPath.push(cam);
    
    // set camera path to viewer
    viewer.setCameraPath(camPath);
  }

  public void setSceneRoot(SceneGraphComponent r) {
    viewer.setSceneRoot(r);
  }

  public void setSignature(int sig) {
    viewer.setSignature(sig);
  }
  
  double[] tmp1 = new double[16];
  double[] tmp2 = new double[16];
  FactoredMatrix headMatrix = new FactoredMatrix();
  FactoredMatrix headTranslation = new FactoredMatrix();
  FactoredMatrix portalMatrix = new FactoredMatrix();
  Matrix totalOrientation = new Matrix();
  Matrix world2cam = new Matrix();

  private void setHeadMatrix(double[] head) {
    headMatrix.assignFrom(head);
    headTranslation.setTranslation(headMatrix.getTranslation());

    headTranslation.assignTo(cameraTranslationNode);
    
    headTranslation.multiplyOnRight(sensorCorrection);
    headMatrix.multiplyOnRight(sensorCorrection); // tmp = headMatrix
    totalOrientation.assignFrom(cameraOrientationNode.getTransformation());
    totalOrientation.invert();
    totalOrientation.multiplyOnRight(headMatrix);
    cam.setOrientationMatrix(totalOrientation.getArray());
    
    portalMatrix.assignFrom(portalPath.getMatrix(tmp1));
    world2cam.assignFrom(viewer.getCameraPath().getInverseMatrix(tmp2));
    CameraUtility.setPORTALViewport(world2cam, portalMatrix, cam);
  }

  Statement waitStatement;
  public void waitForRenderFinish() {
    if (waitStatement == null) waitStatement = new Statement(viewer, "waitForRenderFinish", null);
    try {
      waitStatement.execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setRemoteSceneRoot(RemoteSceneGraphComponent r) {
    setSceneRoot((SceneGraphComponent) r);
  }

  public void setRemoteAuxiliaryRoot(RemoteSceneGraphComponent r) {
    setAuxiliaryRoot((SceneGraphComponent) r);
  }

  public void setRemoteCameraPath(List list) {
    setCameraPath(SceneGraphPath.fromList(list));
  }

}
