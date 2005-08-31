/*
 * Created on Apr 13, 2005
 *
 * This file is part of the de.jreality.portal package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.portal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JFrame;

import de.jreality.jogl.Viewer;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.proxy.scene.RemoteSceneGraphComponent;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.smrj.ClientFactory;

/**
 * @author weissman
 *
 */
public class PortalJoglClientViewer implements RemoteJoglViewer, ClientFactory.ResetCallback {

  private Viewer viewer;
  private JFrame frame;
  SceneGraphPath portalPath;
  SceneGraphComponent headComponent;
  
  private SceneGraphComponent cameraTranslationNode;
  private SceneGraphComponent cameraOrientationNode;
  
  Camera cam;

  // this field moves the sensor from the middle
  // of the glasses to its real position - rotate and translate to the left... 
  static double[] sensorCorrection;
  static {
    double angle = -Math.PI/2;
    //sensorOrientationCorrection = P3.makeRotationMatrix(null, axis, angle);
    sensorCorrection = MatrixBuilder.euclidean().rotateX(angle).translate(-0.08, 0, 0).getMatrix().getArray();
  }

  ConfigurationAttributes config;

  boolean hasCamPath;

  boolean hasSceneRoot;

  private static final class Singleton {
    private static final PortalJoglClientViewer instance = new PortalJoglClientViewer();
  }

  public static PortalJoglClientViewer getInstance() {
    Singleton.instance.initFrame();
    return Singleton.instance;
  }

  private PortalJoglClientViewer() {
    viewer = new de.jreality.jogl.Viewer();
    config = ConfigurationAttributes.getDefaultConfiguration();
    // frame settings
    frame = new JFrame(config.getProperty("frame.title", "no title"));
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    frame.getContentPane().add(viewer.getViewingComponent());
    init();
    initFrame();
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

  protected void initFrame() {
    frame.validate();
    frame.show();
  }

  protected void init() {
    if (config.getBool("frame.fullscreen")) {
      // disable mouse cursor in fullscreen mode
      BufferedImage cursorImg = new BufferedImage(16, 16,
          BufferedImage.TYPE_INT_ARGB);
      Graphics2D gfx = cursorImg.createGraphics();
      gfx.setColor(new Color(0, 0, 0, 0));
      gfx.fillRect(0, 0, 16, 16);
      gfx.dispose();
      frame.setCursor(frame.getToolkit().createCustomCursor(cursorImg,
          new Point(), ""));
      frame.dispose();
      frame.setUndecorated(true);
      frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(frame);
    } else {
      frame
          .setSize(config.getInt("frame.width"), config.getInt("frame.height"));
    }
    viewer.setStereoType(de.jreality.jogl.Viewer.CROSS_EYED_STEREO);
    viewer.setAutoSwapMode(config.getBool("viewer.autoBufferSwap"));
  }

  public void setManualSwapBuffers(boolean b) {
    viewer.setAutoSwapMode(!b);
  }

  public void swapBuffers() {
    viewer.swapBuffers();
  }

  /**
   * TODO !!
   */
  public void setUseDisplayLists(boolean b) {
    //        viewer.getRenderer().setUseDisplayLists(b);
  }

  public void waitForRenderFinish() {
    viewer.waitForRenderFinish();
  }

  public void setRemoteSceneRoot(RemoteSceneGraphComponent r) {
    System.out.println("PortalJoglClientViewer.setRemoteSceneRoot() root="
        + r.getName());
    viewer.setSceneRoot((SceneGraphComponent) r);
    hasSceneRoot = r != null;
  }

  public void setRemoteCameraPath(List list) {
    // dispose artificial camera path
    if (viewer.getCameraPath() != null) {
      SceneGraphPath last = viewer.getCameraPath();
      last.pop(); // Camera
      last.getLastComponent().setCamera(null);
      last.pop(); // Orientation
      last.pop(); // Translation
      last.getLastComponent().removeChild(cameraTranslationNode);
    }
    hasCamPath = !(list == null || list.isEmpty());
    // empty path => reset fields
    if (list == null || list.isEmpty()) {
      viewer.setCameraPath(null);
      headComponent = null;
      portalPath = null;
      cam = null;
      return;
    }
    // new camera path => extract headComponent and set artificial camera path
    SceneGraphPath camPath = SceneGraphPath.fromList(list);
    System.out.println("PortalJoglClientViewer.setRemoteCameraPath() "+camPath);

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

  double[] tmpHead = new double[16];
  public void render() {
    if (!hasSceneRoot || !hasCamPath) return;
    setHeadMatrix(headComponent.getTransformation().getMatrix(tmpHead));
    viewer.render();
  }

  public void setSignature(int sig) {
    viewer.setSignature(sig);
  }

  public void resetCalled() {
    System.out.println("disposing prev viewer instance");
    setRemoteSceneRoot(null);
    setRemoteCameraPath(null);
    frame.hide();
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

  public void setRemoteAuxiliaryRoot(RemoteSceneGraphComponent r) {
    viewer.setAuxiliaryRoot((SceneGraphComponent) r);
  }

}
