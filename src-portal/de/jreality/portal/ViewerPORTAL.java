/*
 * Created on Jul 14, 2004
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

package de.jreality.portal;

import java.awt.Color;

import net.java.games.jogl.GLDrawable;
import de.jreality.jogl.Viewer;
import de.jreality.portal.util.INetUtilities;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.P3;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;

/**
 * This class is designed to work as a JOGL viewer in the PORTAL. Since there
 * can only be one such viewer active at a time, the constructor is private and
 * we only provide a single shared instance of the viewer.
 * 
 * The viewer initializes itself based on the value of the hostname, as this
 * determines the wall to which the viewer is attached. The camera
 * transformation is set accordingly.
 * 
 * @author weissman
 *  
 */
public class ViewerPORTAL extends Viewer {

	private static ViewerPORTAL instance;

	private SceneGraphComponent cameraTranslationNode,
			cameraOrientationNode, world, navigationNode;

	String hostname;

	ConfigurationAttributes config;

	/**
	 * private constructor to ensure that there is only one instance
	 *
	 */
	private ViewerPORTAL() {
		// the following call creates a viewer with a trivial scene graph:
		// a root with a single child containing a camera.
		super();
		this.config = ConfigurationAttributes.getSharedConfiguration();
		( (GLDrawable) getViewingComponent() ).setAutoSwapBufferMode(config.getBool("viewer.autoswap"));
		if (getSceneRoot().getAppearance() == null)
			getSceneRoot().setAppearance(new Appearance());
		getSceneRoot().getAppearance().setAttribute(
				CommonAttributes.SMOOTH_SHADING, true);
		getSceneRoot().getAppearance().setAttribute(
				CommonAttributes.FACE_NORMALS, true);

		// insert an extra transform in the camera path
		// This will handle the translation based on the tracked position
		// The "camera node" itself contains only the orientation rotation
		// which wall the viewer is attached to.
		// It may be possible to combine these two transformations into
		// a single node but for now we separate them for the sake of
		// clarity.
		hostname = INetUtilities.getHostname();
		cameraOrientationNode = cameraTranslationNode = CameraUtility
				.getCameraNode(this);
		cameraTranslationNode.setTransformation(new Transformation());
		Camera cam = cameraTranslationNode.getCamera();
		cam.setNear(.1);
		cameraTranslationNode.setCamera(null);
		cameraOrientationNode = SceneGraphUtilities
				.createFullSceneGraphComponent(hostname + "CameraNode");
		cameraTranslationNode.addChild(cameraOrientationNode);
		cameraOrientationNode.setCamera(cam);
		// lengthen the camera path
		getCameraPath().pop();
		getCameraPath().push(cameraOrientationNode);
		getCameraPath().push(cam);
		setCameraPath(getCameraPath());
		initCameraOrientation();
		getSceneRoot().getAppearance().setAttribute(
				CommonAttributes.BACKGROUND_COLOR, Color.DARK_GRAY);
		world = null;
		navigationNode = new SceneGraphComponent();
		navigationNode.setTransformation(new Transformation());
		getSceneRoot().addChild(navigationNode);
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

	public static ViewerPORTAL getSharedInstance() {
		if (instance == null) {
			instance = new ViewerPORTAL();
		}
		return instance;
	}

	public void setWorld(SceneGraphComponent w) {
		if (world != null)
			navigationNode.removeChild(world);
		instance.world = w;
		navigationNode.addChild(world);
		if (instance.getSceneRoot().getAppearance() == null)
			instance.getSceneRoot().setAppearance(new Appearance());
	}

	/**
	 * sets the Transformation of the navigation node. for simple navigation tool
	 * @param t
	 */
	public void setNavigationTransformation(Transformation t) {
		if (world == null)
			return;
		instance.navigationNode.getTransformation().setMatrix(t.getMatrix());
	}

	static double[] correction;
	static {
		double[] axis = ConfigurationAttributes.getSharedConfiguration().getDoubleArray("camera.correction.axis");
		double angle = ConfigurationAttributes.getSharedConfiguration().getDouble("camera.correction.angle");
		angle *= (Math.PI*2.)/360.;
		correction = P3.makeRotationMatrix(null, axis, angle);
	}

	/**
	 * 
	 * @param t current head || camera position
	 */
	public void setCameraPosition(Transformation t) {
		//TODO move sensor between the eyes
		Camera cam = CameraUtility.getCamera(instance);
		cameraTranslationNode.getTransformation().setTranslation(
				t.getTranslation());
		double[] tmp = Rn.times(null, t.getMatrix(), correction);
		double[] totalOrientation = Rn.times(null, Rn.inverse(null,
				cameraOrientationNode.getTransformation().getMatrix()), tmp);
		cam.setOrientationMatrix(totalOrientation);
		cam.setViewPort(CameraUtility.calculatePORTALViewport(instance, t));
	}

	/**
	 * not yet used - for synchronizing buffer swaps
	 *
	 */
	public void swapBuffers() {
		((GLDrawable) instance.getViewingComponent()).swapBuffers();
	}
	
}