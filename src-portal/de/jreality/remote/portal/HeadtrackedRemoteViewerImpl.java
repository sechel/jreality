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
package de.jreality.remote.portal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;

import javax.swing.JFrame;

import net.java.games.jogl.GLDrawable;
import de.jreality.jogl.Viewer;
import de.jreality.remote.RemoteViewerImpl;
import de.jreality.remote.util.INetUtilities;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SpotLight;
import de.jreality.scene.Transformation;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphComponent;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphElementsFactoryImpl;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.P3;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;

/**
 * Portal Viewer - has one camera configured by the ConfigurationAttributes and uses jogl renderer.
 * 
 * @author weissman
 *
 */
public class HeadtrackedRemoteViewerImpl extends RemoteViewerImpl implements
		HeadtrackedRemoteViewer {

	protected SceneGraphComponent cameraTranslationNode,
	cameraOrientationNode, root, navigationNode;

	String hostname;

	ConfigurationAttributes config;

	static double[] correction;
	
	private final de.jreality.jogl.InteractiveViewer viewer;
	static {
		double[] axis = ConfigurationAttributes.getSharedConfiguration().getDoubleArray("camera.correction.axis");
		double angle = ConfigurationAttributes.getSharedConfiguration().getDouble("camera.correction.angle");
		angle *= (Math.PI*2.)/360.;
		correction = P3.makeRotationMatrix(null, axis, angle);
	}
	JFrame f;
	/**
	 * @param viewer
	 * @throws RemoteException
	 * @throws RemoteException
	 */
	public HeadtrackedRemoteViewerImpl(de.jreality.jogl.InteractiveViewer viewer) throws RemoteException {
		super(viewer);
        this.viewer = viewer;
		this.config = ConfigurationAttributes.getSharedConfiguration();
		( (GLDrawable) viewer.getViewingComponent() ).setAutoSwapBufferMode(config.getBool("viewer.autoBufferSwap"));
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
		cameraOrientationNode = SceneGraphUtilities
				.createFullSceneGraphComponent(hostname + "CameraNode");
		cameraTranslationNode.addChild(cameraOrientationNode);
		cameraOrientationNode.setCamera(cam);
		// lengthen the camera path
		viewer.getCameraPath().pop();
		viewer.getCameraPath().push(cameraOrientationNode);
		viewer.getCameraPath().push(cam);
		viewer.setCameraPath(viewer.getCameraPath());
		initCameraOrientation();
		System.out.println("new cam path:"+viewer.getCameraPath().toString());
		viewer.getSceneRoot().getAppearance().setAttribute(
				CommonAttributes.BACKGROUND_COLOR, Color.DARK_GRAY);
		navigationNode = new SceneGraphComponent();
		navigationNode.setTransformation(new Transformation());
		viewer.getSceneRoot().addChild(navigationNode);
		viewer.setStereoType(Viewer.CROSS_EYED_STEREO);
		viewer.getHelpOverlay().setVisible(config.getBool("viewer.showFPS"));
		CameraUtility.getCamera(viewer).setStereo(config.getBool("camera.stereo"));
		CameraUtility.getCamera(viewer).setEyeSeparation(
				config.getDouble("camera.eyeSeparation"));
		CameraUtility.getCamera(viewer).setOnAxis(false);
		viewer.setAutoSwapMode(config.getBool("viewer.autoBufferSwap"));
		// frame settings
		f = new JFrame(config.getProperty("frame.title", "no title"));
		if (config.getBool("frame.fullscreen")) {
			BufferedImage cursorImg = new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
			Graphics2D gfx = cursorImg.createGraphics();
			gfx.setColor(new Color(0,0,0,0));
			gfx.fillRect(0,0,16,16);
			gfx.dispose();
			f.setCursor(f.getToolkit().createCustomCursor(cursorImg, new Point(), ""));
			f.dispose();
			f.setUndecorated(true);
			f.getGraphicsConfiguration().getDevice().setFullScreenWindow(f);
		} else {
			f.setSize(config.getInt("frame.width"), config
					.getInt("frame.height"));
			f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					disconnect();
				}
			});
		}
    de.jreality.util.CameraUtility.getCamera(viewer).setNear(config.getDouble("camera.nearPlane"));
    de.jreality.util.CameraUtility.getCamera(viewer).setFar(config.getDouble("camera.farPlane"));
		f.getContentPane().add(viewer.getViewingComponent());
    f.validate();
		f.show();
//		if (config.getBool("portal.fixedHead")) {
//			fixedHead = true;
//			Transformation t = new Transformation();
//			t.setTranslation(config.getDoubleArray("portal.fixedHeadPosition"));
//			viewer.setCameraPosition(t);
//		}
	}

	public void render() {
		viewer.render();
	}
	
	public void swapBuffers() {
		viewer.swapBuffers();
	}


	public void waitForRenderFinish() {
		viewer.waitForRenderFinish();
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
		if (r != null) { 
			root = getLocal(r);
      viewer.getSceneRoot().addChild(root);
		}
		else root = null;
	}

	Transformation t = new Transformation();
	/**
	 * 
	 * @param t current head || camera position
	 * @throws RemoteException
	 */
	public void sendHeadTransformation(double[] tm) {
		t.setMatrix(tm);
		//TODO move sensor between the eyes
		Camera cam = CameraUtility.getCamera(viewer);
		cameraTranslationNode.getTransformation().setTranslation(
				t.getTranslation());
		double[] tmp = Rn.times(null, t.getMatrix(), correction);
		double[] totalOrientation = Rn.times(null, Rn.inverse(null,
				cameraOrientationNode.getTransformation().getMatrix()), tmp);
		cam.setOrientationMatrix(totalOrientation);
		cam.setViewPort(CameraUtility.calculatePORTALViewport(viewer, t));
	}
	
	private SceneGraphComponent makeLights()	{
		SceneGraphComponent lights = new SceneGraphComponent();
		lights.setName("lights");
		SpotLight pl = new SpotLight();
		//PointLight pl = new PointLight();
		//DirectionalLight pl = new DirectionalLight();
		pl.setFalloff(1.0, 0.0, 0.0);
		pl.setColor(new Color(120, 250, 180));
		pl.setConeAngle(Math.PI);

		pl.setIntensity(0.6);
		SceneGraphComponent l0 = SceneGraphUtilities.createFullSceneGraphComponent("light0");
		l0.setLight(pl);
		lights.addChild(l0);
		DirectionalLight dl = new DirectionalLight();
		dl.setColor(new Color(250, 100, 255));
		dl.setIntensity(0.6);
		l0 = SceneGraphUtilities.createFullSceneGraphComponent("light1");
		double[] zaxis = {0,0,1};
		double[] other = {1,1,1};
		l0.getTransformation().setMatrix( P3.makeRotationMatrix(null, zaxis, other));
		l0.setLight(dl);
		lights.addChild(l0);
		
		return lights;
	}

	public static void main(String args[]) throws Exception {
		String hostname = INetUtilities.getHostname();
		ConfigurationAttributes config = ConfigurationAttributes.getDefaultConfiguration();
		try {
			HeadtrackedRemoteViewerImpl obj = new HeadtrackedRemoteViewerImpl(new de.jreality.jogl.InteractiveViewer());
			obj.bind();
			obj.connect();
		} catch (Exception e) {
			System.out.println("RemoteViewer err: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void setBackgroundColor(java.awt.Color color) {
		viewer.setBackgroundColor(color);
	}
	

	public de.jreality.jogl.InteractiveViewer getViewer() {
		return viewer;
	}

	public void setManualSwapBuffers(boolean b) {
		viewer.setAutoSwapMode(!b);
	}

	public void setUseDisplayLists(boolean b) {
		viewer.getRenderer().setUseDisplayLists(b);
	}
}
