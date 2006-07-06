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


package de.jreality.io;

import java.awt.Color;
import java.util.logging.Level;

import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.Tool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.EncompassTool;
import de.jreality.tools.FlyTool;
import de.jreality.tools.RotateTool;
import de.jreality.util.LoggingSystem;


public class JrSceneFactory {
  
  
  /**
   * Get the default scene for desktop environment.
   * @return the default desktop scene
   */
  //replaces de.jreality.ui.viewerapp.desktop-scene.jrs
  public static JrScene getDefaultDesktopScene() {
    
    //sceneRoot of the JrScene
    SceneGraphComponent sceneRoot = new SceneGraphComponent();
    sceneRoot.setName("root");
    sceneRoot.setVisible(true);
    Appearance app = new Appearance();
    app.setName("root appearance");
    sceneRoot.setAppearance(app);
    //children of sceneRoot
    //scene
    SceneGraphComponent scene = new SceneGraphComponent();
    scene.setName("scene");
    scene.setVisible(true);
    Transformation trafo = new Transformation(Rn.identityMatrix(4));
    trafo.setName("scene trafo");
    scene.setTransformation(trafo);
    EncompassTool encompassTool = new EncompassTool();
    RotateTool rotateTool = new RotateTool();
    rotateTool.setFixOrigin(false);
    rotateTool.setMoveChildren(false);
    rotateTool.setUpdateCenter(false);
    rotateTool.setAnimTimeMin(250.0);
    rotateTool.setAnimTimeMax(750.0);
    DraggingTool draggingTool = new DraggingTool();
    draggingTool.setMoveChildren(false);
    scene.addTool(encompassTool);
    scene.addTool(rotateTool);
    scene.addTool(draggingTool);
    sceneRoot.addChild(scene);
    //lightComp 1
    SceneGraphComponent lightNode = new SceneGraphComponent();
    lightNode.setName("lightNode");
    lightNode.setVisible(true);
    double[] trafoMatrix = new double[]{
        0.8535533905932737,  0.14644660940672619, -0.4999999999999999, 0.0,
        0.14644660940672619, 0.8535533905932737,   0.4999999999999999, 0.0, 
        0.4999999999999999, -0.4999999999999999,   0.7071067811865476, 0.0,
        0.0,                 0.0,                  0.0,                1.0
    };
    trafo = new Transformation(trafoMatrix);
    trafo.setName("lightNode trafo");
    lightNode.setTransformation(trafo);
    Light light = new DirectionalLight();
    light.setName("light");
    light.setColor(new Color(255,255,255,255));
    light.setIntensity(0.75);
    lightNode.setLight(light);
    sceneRoot.addChild(lightNode);
//  //lightNode 2   SAME AS LIGHTNODE
//  SceneGraphComponent lightNode2 = new SceneGraphComponent();
//  lightNode2.setName("lightNode 2");
//  lightNode2.setVisible(true);
//  trafo = new Transformation(trafoMatrix);
//  trafo.setName("lightNode 2 trafo");
//  lightNode2.setTransformation(trafo);
//  lightNode2.setLight(light);
//  sceneRoot.addChild(lightNode2);
    //avatar
    SceneGraphComponent avatar = new SceneGraphComponent();
    avatar.setName("avatar");
    avatar.setVisible(true);
    trafo = new Transformation(Rn.identityMatrix(4));
    trafo.setName("avatar trafo");
    avatar.setTransformation(trafo);
    sceneRoot.addChild(avatar);
    //children of avatar
    //camera
    SceneGraphComponent cameraNode = new SceneGraphComponent();
    cameraNode.setName("cameraNode");
    cameraNode.setVisible(true);
    trafoMatrix = Rn.identityMatrix(4);
    trafoMatrix[11] = 16.0;
    trafo = new Transformation(trafoMatrix);
    trafo.setName("camera trafo");
    cameraNode.setTransformation(trafo);
    Camera camera = new Camera(); 
    camera.setName("camera");
    camera.setEyeSeparation(0.1);
    camera.setFar(50.0);
    camera.setFieldOfView(30.0);
    camera.setFocus(3.0);
    camera.setNear(3.0);
    camera.setOnAxis(true);
    camera.setOnAxis(true);
    camera.setPerspective(true);
    camera.setStereo(false);
    cameraNode.setCamera(camera);
    light = new DirectionalLight();
    light.setName("camera light");
    light.setColor(new Color(255,255,255,255));
    light.setIntensity(0.75);
    cameraNode.setLight(light);
    avatar.addChild(cameraNode);
    
    //create JrScene
    JrScene defaultScene = new JrScene(sceneRoot);
    
    //create paths
    //cameraPath
    SceneGraphPath cameraPath = new SceneGraphPath();
    cameraPath.push(sceneRoot);
    cameraPath.push(avatar);
    cameraPath.push(cameraNode);
    cameraPath.push(camera);
    defaultScene.addPath("cameraPath", cameraPath);
    //avatarPath
    SceneGraphPath avatarPath = new SceneGraphPath();
    avatarPath.push(sceneRoot);
    avatarPath.push(avatar);
    defaultScene.addPath("avatarPath", avatarPath);
    //emptyPickPath
    SceneGraphPath emptyPickPath = new SceneGraphPath();
    emptyPickPath.push(sceneRoot);
    emptyPickPath.push(scene);
    defaultScene.addPath("emptyPickPath", emptyPickPath);
    
    return defaultScene;
  }
  
  
  /**
   * Get the default scene for PORTAL environment.<br>
   * (differences to desktop-scene: additional tools 
   * cameraNode.PortalHeadMoveTool and avatar.FlyTool)
   * @return the default PORTAL scene
   */
  //replaces de.jreality.ui.viewerapp.portal-scene.jrs
  public static JrScene getDefaultPortalScene() {
    
    //sceneRoot of the JrScene
    SceneGraphComponent sceneRoot = new SceneGraphComponent();
    sceneRoot.setName("root");
    sceneRoot.setVisible(true);
    Appearance app = new Appearance();
    app.setName("root appearance");
    sceneRoot.setAppearance(app);
    //children of sceneRoot
    //scene
    SceneGraphComponent scene = new SceneGraphComponent();
    scene.setName("scene");
    scene.setVisible(true);
    Transformation trafo = new Transformation(Rn.identityMatrix(4));
    trafo.setName("scene trafo");
    scene.setTransformation(trafo);
    EncompassTool encompassTool = new EncompassTool();
    RotateTool rotateTool = new RotateTool();
    rotateTool.setFixOrigin(false);
    rotateTool.setMoveChildren(false);
    rotateTool.setUpdateCenter(false);
    rotateTool.setAnimTimeMin(250.0);
    rotateTool.setAnimTimeMax(750.0);
    DraggingTool draggingTool = new DraggingTool();
    draggingTool.setMoveChildren(false);
    scene.addTool(encompassTool);
    scene.addTool(rotateTool);
    scene.addTool(draggingTool);
    sceneRoot.addChild(scene);
    //lightComp 1
    SceneGraphComponent lightNode = new SceneGraphComponent();
    lightNode.setName("lightNode");
    lightNode.setVisible(true);
    double[] trafoMatrix = new double[]{
        0.8535533905932737,  0.14644660940672619, -0.4999999999999999, 0.0,
        0.14644660940672619, 0.8535533905932737,   0.4999999999999999, 0.0, 
        0.4999999999999999, -0.4999999999999999,   0.7071067811865476, 0.0,
        0.0,                 0.0,                  0.0,                1.0
    };
    trafo = new Transformation(trafoMatrix);
    trafo.setName("lightNode trafo");
    lightNode.setTransformation(trafo);
    Light light = new DirectionalLight();
    light.setName("light");
    light.setColor(new Color(255,255,255,255));
    light.setIntensity(0.75);
    lightNode.setLight(light);
    sceneRoot.addChild(lightNode);
//  //lightNode 2   SAME AS LIGHTNODE
//  SceneGraphComponent lightNode2 = new SceneGraphComponent();
//  lightNode2.setName("lightNode 2");
//  lightNode2.setVisible(true);
//  trafo = new Transformation(trafoMatrix);
//  trafo.setName("lightNode 2 trafo");
//  lightNode2.setTransformation(trafo);
//  lightNode2.setLight(light);
//  sceneRoot.addChild(lightNode2);
    //avatar
    SceneGraphComponent avatar = new SceneGraphComponent();
    avatar.setName("avatar");
    avatar.setVisible(true);
    trafo = new Transformation(Rn.identityMatrix(4));
    trafo.setName("avatar trafo");
    avatar.setTransformation(trafo);
    FlyTool flyTool = new FlyTool();
    flyTool.setGain(1.0);
    avatar.addTool(flyTool);
    sceneRoot.addChild(avatar);
    //children of avatar
    //camera
    SceneGraphComponent cameraNode = new SceneGraphComponent();
    cameraNode.setName("cameraNode");
    cameraNode.setVisible(true);
    trafoMatrix = Rn.identityMatrix(4);
    trafoMatrix[11] = 16.0;
    trafo = new Transformation(trafoMatrix);
    trafo.setName("camera trafo");
    cameraNode.setTransformation(trafo);
    Camera camera = new Camera(); 
    camera.setName("camera");
    camera.setEyeSeparation(0.1);
    camera.setFar(50.0);
    camera.setFieldOfView(30.0);
    camera.setFocus(3.0);
    camera.setNear(3.0);
    camera.setOnAxis(true);
    camera.setOnAxis(true);
    camera.setPerspective(true);
    camera.setStereo(false);
    cameraNode.setCamera(camera);
    light = new DirectionalLight();
    light.setName("camera light");
    light.setColor(new Color(255,255,255,255));
    light.setIntensity(0.75);
    cameraNode.setLight(light);
    Tool portalHeadMoveTool;
    try {
      portalHeadMoveTool = (Tool) Class.forName("de.jreality.portal.tools.PortalHeadMoveTool").newInstance();
      avatar.addTool(portalHeadMoveTool);
    } catch (Exception e) {
      LoggingSystem.getLogger(JrSceneFactory.class).log(Level.WARNING, "failed to create PortalHeadMoveTool", e);
    }
    avatar.addChild(cameraNode);
    
    //create JrScene
    JrScene defaultScene = new JrScene(sceneRoot);
    
    //create paths
    //cameraPath
    SceneGraphPath cameraPath = new SceneGraphPath();
    cameraPath.push(sceneRoot);
    cameraPath.push(avatar);
    cameraPath.push(cameraNode);
    cameraPath.push(camera);
    defaultScene.addPath("cameraPath", cameraPath);
    //avatarPath
    SceneGraphPath avatarPath = new SceneGraphPath();
    avatarPath.push(sceneRoot);
    avatarPath.push(avatar);
    defaultScene.addPath("avatarPath", avatarPath);
    //emptyPickPath
    SceneGraphPath emptyPickPath = new SceneGraphPath();
    emptyPickPath.push(sceneRoot);
    emptyPickPath.push(scene);
    defaultScene.addPath("emptyPickPath", emptyPickPath);
    
    return defaultScene;
  }
  
}