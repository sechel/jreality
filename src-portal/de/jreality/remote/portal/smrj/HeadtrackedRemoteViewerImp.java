/*
 * Created on 19-Nov-2004
 *
 * This file is part of the jReality package.
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
package de.jreality.remote.portal.smrj;

import java.awt.Color;

import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.remote.util.INetUtilities;
import de.jreality.scene.*;
import de.jreality.scene.proxy.scene.RemoteSceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.*;

/**
 * Portal Viewer - has one camera configured by the ConfigurationAttributes and uses jogl renderer.
 * 
 * @author weissman
 *
 */
public class HeadtrackedRemoteViewerImp extends RemoteViewerImp implements
        HeadtrackedRemoteViewer {

    protected SceneGraphComponent 
        cameraTranslationNode,
        cameraOrientationNode,
        root;

    protected String hostname;

    static double[] correction;
    static {
        double[] axis = ConfigurationAttributes.getDefaultConfiguration().getDoubleArray("camera.correction.axis");
        double angle = ConfigurationAttributes.getDefaultConfiguration().getDouble("camera.correction.angle");
        angle *= (Math.PI*2.)/360.;
        correction = P3.makeRotationMatrix(null, axis, angle);
    }

    HeadtrackedRemoteViewerImp(Viewer viewer) {
        super(viewer);
        // disable vertex draw for performance
        if (viewer.getSceneRoot().getAppearance() == null)
            viewer.getSceneRoot().setAppearance(new Appearance());
        viewer.getSceneRoot().getAppearance().setAttribute(
                CommonAttributes.SMOOTH_SHADING, true);
        viewer.getSceneRoot().getAppearance().setAttribute(
                CommonAttributes.VERTEX_DRAW, false);
        
        // insert an extra transform in the camera path
        // This will handle the translation based on the tracked position
        // The "camera node" itself contains only the orientation rotation
        // which wall the viewer is attached to.
        // It may be possible to combine these two transformations into
        // a single node but for now we separate them for the sake of
        // clarity.
        hostname = INetUtilities.getHostname();
        viewer.getSceneRoot().setName(hostname+" root");
        CameraUtility.getCameraNode(viewer).setName("camNode");
        CameraUtility.getCameraNode(viewer).getCamera().setName("camera");
        System.out.println("orig. cam path:"+viewer.getCameraPath().toString());
        cameraTranslationNode = CameraUtility.getCameraNode(viewer);
        cameraTranslationNode.setTransformation(new Transformation());
        cameraTranslationNode.addChild(makeLights());
        Camera cam = cameraTranslationNode.getCamera();
        cam.setNear(.1);
        cameraTranslationNode.setCamera(null);
        cameraOrientationNode = SceneGraphUtility
                .createFullSceneGraphComponent(hostname + "CameraNode");
        cameraTranslationNode.addChild(cameraOrientationNode);
        cameraOrientationNode.setCamera(cam);
        // lengthen the camera path
        viewer.getCameraPath().pop();
        viewer.getCameraPath().push(cameraOrientationNode);
        viewer.getCameraPath().push(cam);
        viewer.setCameraPath(viewer.getCameraPath());
        initCameraOrientation();
        CameraUtility.getCamera(viewer).setOnAxis(false);
        System.out.println("new cam path:"+viewer.getCameraPath().toString());
                
//      if (config.getBool("portal.fixedHead")) {
//          fixedHead = true;
//          Transformation t = new Transformation();
//          t.setTranslation(config.getDoubleArray("portal.fixedHeadPosition"));
//          viewer.setCameraPosition(t);
//      }
    }

    public void render(double[] headMatrix) {
        sendHeadTransformation(headMatrix);
        render();
    }

    /**
     * sets the camera orientation node from the config file (rotation around
     * given axis)
     */
    private void initCameraOrientation() {
        double[] rot = config.getDoubleArray("camera.orientation");
        cameraOrientationNode.getTransformation().setRotation(
                rot[0] * ((Math.PI * 2.0) / 360.), rot[1], rot[2], rot[3]);
    }

    public void setRemoteSceneRoot(RemoteSceneGraphComponent r) {
        if (root != null) viewer.getSceneRoot().removeChild(root);
        root = (SceneGraphComponent) r;
        if (root != null) { 
            viewer.getSceneRoot().addChild(root);
        }
    }

    Transformation t = new Transformation();
    double[] tmp = new double[16];
    double[] totalOrientation = new double[16]; 
    public void sendHeadTransformation(double[] tm) {
        t.setMatrix(tm);
        //TODO move sensor between the eyes
        Camera cam = CameraUtility.getCamera(viewer);
        cameraTranslationNode.getTransformation().setTranslation(
                t.getTranslation());
        Rn.times(tmp, t.getMatrix(), correction);
        Rn.times(totalOrientation, Rn.inverse(null,
                cameraOrientationNode.getTransformation().getMatrix()), tmp);
        cam.setOrientationMatrix(totalOrientation);
        cam.setViewPort(CameraUtility.calculatePORTALViewport(viewer));
    }
    
    //TODO: throw away this method
    private SceneGraphComponent makeLights()    {
        SceneGraphComponent lights = new SceneGraphComponent();
        lights.setName("lights");
        SpotLight pl = new SpotLight();
        pl.setFalloff(1.0, 0.0, 0.0);
        pl.setColor(new Color(120, 250, 180));
        pl.setConeAngle(Math.PI);
        pl.setIntensity(0.6);
        SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light0");
        l0.setLight(pl);
        lights.addChild(l0);
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(new Color(250, 100, 255));
        dl.setIntensity(0.6);
        l0 = SceneGraphUtility.createFullSceneGraphComponent("light1");
        double[] zaxis = {0,0,1};
        double[] other = {1,1,1};
        l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other));
        l0.setLight(dl);
        lights.addChild(l0);
        return lights;
    }

}
