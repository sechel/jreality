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
import de.jreality.math.*;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.proxy.scene.RemoteSceneGraphComponent;
import de.jreality.shader.CommonAttributes;
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
	
    private SceneGraphComponent cameraTranslationNode;
    private SceneGraphComponent cameraOrientationNode;
    static double[] correction;
    static {
        double[] axis = ConfigurationAttributes.getDefaultConfiguration().getDoubleArray("camera.correction.axis");
        double angle = ConfigurationAttributes.getDefaultConfiguration().getDouble("camera.correction.angle");
        angle *= (Math.PI*2.)/360.;
        correction = P3.makeRotationMatrix(null, axis, angle);
    }

	ConfigurationAttributes config;
	
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
	    
	    // TODO: this is a hack!
	    cameraTranslationNode.addChild(PortalServerViewer.makeLights());
	    
	    cameraOrientationNode = new SceneGraphComponent();
	    cameraOrientationNode.setTransformation(new Transformation());
        double[] rot = config.getDoubleArray("camera.orientation");
        Matrix m = new Matrix(cameraOrientationNode.getTransformation().getMatrix());
        MatrixBuilder.euclidian(m).rotate(
                rot[0] * ((Math.PI * 2.0) / 360.),
                new double[] {rot[1], rot[2], rot[3]}
        );
        cameraOrientationNode.getTransformation().setMatrix(m.getArray());
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
			frame.setSize(config.getInt("frame.width"), config
					.getInt("frame.height"));
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
		viewer.setSceneRoot((SceneGraphComponent) r);
	}

	public void setRemoteCameraPath(List list) {
		if (viewer.getCameraPath() != null) {
			SceneGraphPath last = viewer.getCameraPath();
			last.pop(); // Camera
			last.getLastComponent().setCamera(null);
			last.pop(); // Orientation
			last.pop(); // Translation
			last.getLastComponent().removeChild(cameraTranslationNode);
		}
		if (list == null || list.isEmpty()) {
			viewer.setCameraPath(null);
			return;
		}
		SceneGraphPath camPath = SceneGraphPath.fromList(list);
		Camera cam = (Camera) camPath.getLastElement();
		
		// make stereo settings
        cam.setStereo(config.getBool("camera.stereo"));
        cam.setEyeSeparation(config.getDouble("camera.eyeSeparation"));
        cam.setNear(config.getDouble("camera.nearPlane"));
        cam.setFar(config.getDouble("camera.farPlane"));
        
        cam.setOnAxis(false);
		
		camPath.pop();
		camPath.getLastComponent().addChild(cameraTranslationNode);
		cameraOrientationNode.setCamera(cam);
		camPath.push(cameraTranslationNode);
		camPath.push(cameraOrientationNode);
		camPath.push(cam);
		viewer.setCameraPath(camPath);
	}

	public void render(double[] headMatrix) {
		setHeadMatrix(headMatrix);
		viewer.render();
	}

	public void setSignature(int sig) {
		viewer.setSignature(sig);
	}

	public void reset() {
		init();
	}

    public void resetCalled() {
        System.out.println("disposing prev viewer instance");
        setRemoteSceneRoot(null);
        setRemoteCameraPath(null);
        frame.hide();
    }

    double[] tmp = new double[16];
    double[] totalOrientation = new double[16];
    
    public void setHeadMatrix(double[] tm) {
        FactoredMatrix t = new FactoredMatrix(tm);
        FactoredMatrix trans = new FactoredMatrix();
        trans.setTranslation(t.getTranslation());
        //TODO move sensor between the eyes
        Camera cam = CameraUtility.getCamera(viewer);
        cameraTranslationNode.getTransformation().setMatrix(trans.getArray());
        Rn.times(tmp, trans.getArray(), correction);
        Rn.times(totalOrientation, Rn.inverse(null,
                cameraOrientationNode.getTransformation().getMatrix()), tmp);
        cam.setOrientationMatrix(totalOrientation);
        cam.setViewPort(CameraUtility.calculatePORTALViewport(viewer));
    }
}
