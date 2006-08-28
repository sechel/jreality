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

import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.EncompassTool;
import de.jreality.tools.FlyTool;
import de.jreality.tools.HeadTransformationTool;
import de.jreality.tools.PickShowTool;
import de.jreality.tools.RotateTool;
import de.jreality.tools.ShipNavigationTool;
import de.jreality.util.LoggingSystem;


public class JrSceneFactory {
  
  
	public static JrScene getDefaultDesktopScene() {
		return getDefaultScene(false);
	}
	
	public static JrScene getDefaultPortalScene() {
		//return getDefaultScene(true);
		SceneGraphComponent sceneRoot=new SceneGraphComponent(),
		sceneNode=new SceneGraphComponent(),
		avatarNode=new SceneGraphComponent(),
		camNode=new SceneGraphComponent(),
		lightNode=new SceneGraphComponent(),
		lightNode2=new SceneGraphComponent(),
		lightNode3=new SceneGraphComponent(),
		lightNode4=new SceneGraphComponent();

		Appearance terrainAppearance=new Appearance(),
		rootAppearance=new Appearance();

		DirectionalLight light = new DirectionalLight();

		SceneGraphPath cameraPath, avatarPath, emptyPickPath;

		boolean portal = true;

		sceneRoot.setName("root");
		sceneNode.setName("scene");
		avatarNode.setName("avatar");
		camNode.setName("camNode");
		lightNode.setName("light 1");
		lightNode2.setName("light 2");
		lightNode3.setName("light 3");
		lightNode4.setName("light 4");
		sceneRoot.addChild(sceneNode);

		sceneRoot.setAppearance(rootAppearance);

		terrainAppearance.setAttribute("showLines", false);
		terrainAppearance.setAttribute("showPoints", false);
		terrainAppearance.setAttribute("diffuseColor", Color.white);

		Camera cam = new Camera();
		cam.setNear(0.01);
		cam.setFar(1500);

		if (portal) {
			cam.setOnAxis(false);
			cam.setStereo(true);
		}

//		lights
		light.setIntensity(0.4);
		lightNode.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,1,-1}).assignTo(lightNode);
		sceneRoot.addChild(lightNode);

		lightNode2.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{1,1,-1}).assignTo(lightNode2);
		sceneRoot.addChild(lightNode2);

		lightNode3.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{1,1,1}).assignTo(lightNode3);
		sceneRoot.addChild(lightNode3);

		lightNode4.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,1,1}).assignTo(lightNode3);
		sceneRoot.addChild(lightNode4);

		// default tools
	    RotateTool rotateTool = new RotateTool();
	    rotateTool.setFixOrigin(false);
	    rotateTool.setMoveChildren(false);
	    rotateTool.setUpdateCenter(false);
	    rotateTool.setAnimTimeMin(250.0);
	    rotateTool.setAnimTimeMax(750.0);
	    DraggingTool draggingTool = new DraggingTool();
	    draggingTool.setMoveChildren(false);
	    sceneNode.addTool(rotateTool);
	    sceneNode.addTool(draggingTool);
		
//		prepare paths
		sceneRoot.addChild(avatarNode);
		avatarNode.addChild(camNode);
		camNode.setCamera(cam);
		cameraPath = new SceneGraphPath();
		cameraPath.push(sceneRoot);
		emptyPickPath=cameraPath.pushNew(sceneNode);
		cameraPath.push(avatarNode);
		cameraPath.push(camNode);
		avatarPath=cameraPath.popNew();
		cameraPath.push(cam);

		MatrixBuilder.euclidean().translate(0,1.7,0).assignTo(camNode);

//		add tools
		ShipNavigationTool shipNavigationTool = new ShipNavigationTool();
		//avatarNode.addTool(shipNavigationTool);
		if (portal) shipNavigationTool.setPollingDevice(false);

		if (!portal) camNode.addTool(new HeadTransformationTool());
		else {
			try {
				Tool t = (Tool) Class.forName("de.jreality.tools.PortalHeadMoveTool").newInstance();
				camNode.addTool(t);
			} catch (Throwable t) {
//				XXX
			}
		}

		sceneRoot.addTool(new PickShowTool(null, 0.005));

		JrScene scene = new JrScene(sceneRoot);
		
		scene.addPath("avatarPath", avatarPath);
		scene.addPath("cameraPath", cameraPath);
		scene.addPath("emptyPickPath", emptyPickPath);
		
		return scene;
	}
  /**
   * Get the default scene for desktop environment.
   * @return the default desktop scene
   */
  //replaces de.jreality.ui.viewerapp.desktop-scene.jrs
  private static JrScene getDefaultScene(boolean portal) {
    
    //sceneRoot of the JrScene
    SceneGraphComponent sceneRoot = new SceneGraphComponent();
    sceneRoot.setName("root");
    sceneRoot.setVisible(true);
    Appearance app = new Appearance();
    app.setName("root appearance");
    ShaderUtility.createRootAppearance(app);
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
    //avatar
    SceneGraphComponent avatar = new SceneGraphComponent();
    avatar.setName("avatar");
    avatar.setVisible(true);
    trafoMatrix = Rn.identityMatrix(4);
    if (!portal) trafoMatrix[11] = 16;
    trafo = new Transformation(trafoMatrix);
    trafo.setName("avatar trafo");
    avatar.setTransformation(trafo);
    sceneRoot.addChild(avatar);
    //children of avatar
    //camera
    SceneGraphComponent cameraNode = new SceneGraphComponent();
    cameraNode.setName("cameraNode");
    cameraNode.setVisible(true);
    trafoMatrix = Rn.identityMatrix(4);
    trafo = new Transformation(trafoMatrix);
    trafo.setName("camera trafo");
    cameraNode.setTransformation(trafo);
    Camera camera = new Camera(); 
    camera.setName("camera");
    if (!portal) {
	    camera.setFar(50.0);
	    camera.setFieldOfView(30.0);
	    camera.setFocus(3.0);
	    camera.setNear(3.0);
	    camera.setOnAxis(true);
	    camera.setStereo(false);
    } else {
    	camera.setOnAxis(false);
    	camera.setStereo(true);
    }
    cameraNode.setCamera(camera);
    light = new DirectionalLight();
    light.setName("camera light");
    light.setColor(new Color(255,255,255,255));
    light.setIntensity(0.75);
    cameraNode.setLight(light);
    avatar.addChild(cameraNode);
    
    if (portal) {
        FlyTool flyTool = new FlyTool();
        flyTool.setGain(1.0);
        avatar.addTool(flyTool);
        Tool portalHeadMoveTool;
        try {
          portalHeadMoveTool = (Tool) Class.forName("de.jreality.tools.PortalHeadMoveTool").newInstance();
          System.out.println("added HeadMoveTool");
          avatar.addTool(portalHeadMoveTool);
        } catch (Exception e) {
          LoggingSystem.getLogger(JrSceneFactory.class).log(Level.WARNING, "failed to create PortalHeadMoveTool", e);
        }
    }
    
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