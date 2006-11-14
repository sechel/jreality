/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.portal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.beans.Statement;
import java.util.List;

import javax.swing.JFrame;

import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;

import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.proxy.scene.RemoteSceneGraphComponent;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Secure;
import de.smrj.ClientFactory;

/**
 * TODO: make configuration better...
 * 
 * @author gollwas
 *
 */
public class HeadTrackedViewer implements Viewer, RemoteViewer, ClientFactory.ResetCallback {

  Viewer viewer;
  private ConfigurationAttributes config;
  private SceneGraphComponent cameraTranslationNode;
  private SceneGraphComponent cameraOrientationNode;
  double[] tmpHead = new double[16];
  private boolean hasSceneRoot;
  private boolean hasCamPath;
  private SceneGraphComponent headComponent;
  SceneGraphPath portalPath;
  SceneGraphPath cameraPath;

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

  private static JFrame frame;
  private static HeadTrackedViewer hv;
  
  public static HeadTrackedViewer createFullscreen() {
    if (frame == null) {
    frame = new JFrame("no title");
    hv = new HeadTrackedViewer();
    frame.getContentPane().add((Component) hv.getViewingComponent());
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
    frame.setVisible(true);
    }
    return hv;
  }
  
  public HeadTrackedViewer() {
    String delegated = Secure.getProperty("de.jreality.portal.HeadTrackedViewer", "de.jreality.jogl.Viewer");
    try {
      viewer = (Viewer) Class.forName(delegated).newInstance();
    } catch (Exception e) {
      throw new Error("Viewer creation failed!");
    }
//    try {
//      Statement configStatement = new Statement(viewer, "setAutoSwapMode", new Object[]{Boolean.FALSE});
//      configStatement.execute();
//    } catch (Exception e) {
//      LoggingSystem.getLogger(this).config("viewer cant set auto swap mode");
//    }
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

    cameraTranslationNode.setName("cam Translation");
    
    cameraOrientationNode = new SceneGraphComponent();
    cameraOrientationNode.setName("cam Orientation");
    // set camera orientation to value from config file...
    double[] rot = config.getDoubleArray("camera.orientation");
    MatrixBuilder mb = MatrixBuilder.euclidean();
    if (rot != null)  mb.rotate(rot[0] * ((Math.PI * 2.0) / 360.), new double[] { rot[1], rot[2], rot[3] });
    mb.assignTo(cameraOrientationNode);

    cameraTranslationNode.addChild(cameraOrientationNode);
  }

  public SceneGraphComponent getAuxiliaryRoot() {
    return viewer.getAuxiliaryRoot();
  }

  public SceneGraphPath getCameraPath() {
    return cameraPath;
  }

  public SceneGraphComponent getSceneRoot() {
    return viewer.getSceneRoot();
  }

  public int getSignature() {
    return viewer.getSignature();
  }

  public Object getViewingComponent() {
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
    cameraPath = (SceneGraphPath) camPath.clone();
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
    //cam.setStereo(config.getBool("camera.stereo"));
    //cam.setEyeSeparation(config.getDouble("camera.eyeSeparation"));
    cam.setOnAxis(false);

    camPath.pop();

    headComponent = camPath.getLastComponent();
    
    camPath.pop(); // now this should be the portal path
    portalPath = (SceneGraphPath) camPath.clone();
    
    // add camera position and orientation, add camera there
    // DONT CHANGE SCENEGRAPH
//    camPath.getLastComponent().addChild(cameraTranslationNode);
    cameraOrientationNode.setCamera(cam);
    if (cam.isOnAxis()) {
        LoggingSystem.getLogger(CameraUtility.class).info("portal camera is on-axis: changing to off-axis");
        cam.setOnAxis(false);
      }
      
    if (!cam.isStereo()) {
        LoggingSystem.getLogger(CameraUtility.class).info("portal camera is not stereo: changing to stereo");
        cam.setStereo(true);
      }
      
   
    // build the right camera path
    camPath.push(cameraTranslationNode);
    camPath.push(cameraOrientationNode);
    camPath.push(cam);
    
    // set camera path to viewer
    viewer.setCameraPath(camPath);
    
    // hack
    setHeadMatrix(Rn.identityMatrix(4));
  }

  public void setSceneRoot(SceneGraphComponent r) {
	hasSceneRoot = !(r == null);
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
    PortalCoordinateSystem.setPORTALViewport(world2cam, portalMatrix, cam);
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

  public void resetCalled() {
	frame.setVisible(false);
	frame.dispose();
	frame = null;
  }

  public Dimension getViewingComponentSize() {
    return viewer.getViewingComponentSize();
  }

  public boolean canRenderAsync() {
    return viewer.canRenderAsync();
  }

  public void renderAsync() {
    viewer.renderAsync();
  }

//  Statement swapStatement;
//  public void swapBuffers() {
//	    if (swapStatement == null) swapStatement = new Statement(viewer, "swapBuffers", null);
//	    try {
//	    	swapStatement.execute();
//	    } catch (Exception e) {
//	      e.printStackTrace();
//	    }
//  }
}
