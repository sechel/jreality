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

package de.jreality.remote.portal.smrj;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JFrame;

import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphComponent;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphPath;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;

/**
 * This is a simple remote viewer that encapsulates the Viewer given to the
 * Constructor. It reads window properties such as size and title from the
 * Properies file set as "jreality.config" default value for this file is
 * "jreality.props" (in the current folder).
 * 
 * The attributes used are: <br>
 * <ul>
 * <li>frame.title
 * <li>frame.width
 * <li>frame.height
 * <li>camera.name (The default camera name is "defaultCamera" so make sure to
 * have such a camera path in the remote SceneGraph)
 * </ul>
 * 
 * @author weissman
 *  
 */
public class RemoteViewerImp implements RemoteViewer {

    Viewer viewer;
    ConfigurationAttributes config;
    JFrame f;

    public RemoteViewerImp(Viewer viewer) {
        this.viewer = viewer;
        if (!viewer.hasViewingComponent())
                throw new RuntimeException("expecting viewer with component!");
        Thread.currentThread().setName("RemoteViewerImpl");
        config = ConfigurationAttributes.getSharedConfiguration();

        // frame settings
        f = new JFrame(config.getProperty("frame.title", "no title"));
        if (config.getBool("frame.fullscreen")) {
            // disable mouse cursor in fullscreen mode
            BufferedImage cursorImg = new BufferedImage(16, 16,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D gfx = cursorImg.createGraphics();
            gfx.setColor(new Color(0, 0, 0, 0));
            gfx.fillRect(0, 0, 16, 16);
            gfx.dispose();
            f.setCursor(f.getToolkit().createCustomCursor(cursorImg,
                    new Point(), ""));
            f.dispose();
            f.setUndecorated(true);
            f.getGraphicsConfiguration().getDevice().setFullScreenWindow(f);
        } else {
            f.setSize(config.getInt("frame.width"), config
                    .getInt("frame.height"));
            f.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    quit();
                }
            });
        }
        CameraUtility.getCamera(viewer).setStereo(
                config.getBool("camera.stereo"));
        CameraUtility.getCamera(viewer).setEyeSeparation(
                config.getDouble("camera.eyeSeparation"));
        de.jreality.util.CameraUtility.getCamera(viewer).setNear(
                config.getDouble("camera.nearPlane"));
        de.jreality.util.CameraUtility.getCamera(viewer).setFar(
                config.getDouble("camera.farPlane"));
        f.getContentPane().add(viewer.getViewingComponent());
        f.validate();
        f.show();
    }

    public RemoteSceneGraphComponent getRemoteSceneRoot() {
        return (RemoteSceneGraphComponent) viewer.getSceneRoot();
    }

    public void setRemoteSceneRoot(RemoteSceneGraphComponent r) {
        System.out.println("Setting scene root to [" + r.toString() + "] ");
        viewer.setSceneRoot((de.jreality.scene.SceneGraphComponent) r);
    }

    public void setRemoteCameraPath(List list) {
        SceneGraphPath sgp = SceneGraphPath.fromList(list);
        System.out.println("[RemoteViewer->setCameraPath()] CameraPath: "
                + sgp.toString());
        viewer.setCameraPath(sgp);
        f.setVisible(list != null);
        try {
            Thread.sleep(10);
        } catch (Exception e) {
        }
        render();

    }

    public void render() {
        if (f.isVisible() && viewer.getSceneRoot() != null
                && viewer.getCameraPath() != null) {
            viewer.render();
        }
    }

    public int getSignature() {
        return viewer.getSignature();
    }

    public void setSignature(int sig) {
        viewer.setSignature(sig);
    }

    public void quit() {
        System.exit(0);
    }

}