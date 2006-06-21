/*
 * Created on 15.02.2006
 *
 * This file is part of the de.jreality.swing.test package.
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
package de.jreality.swing.test;

import java.awt.Color;
import java.io.IOException;

import de.jreality.examples.CatenoidHelicoid;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.tool.RotateTool;
import de.jreality.scene.tool.ToolSystemViewer;
import de.jreality.scene.tool.config.ToolSystemConfiguration;
import de.jreality.shader.CommonAttributes;
import de.jreality.swing.JFakeFrame;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.RenderTrigger;
import de.jreality.util.SceneGraphUtility;

public class JRonJRTest {

    public JRonJRTest() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        CatenoidHelicoid catenoid = new CatenoidHelicoid(50);
        catenoid.setAlpha(Math.PI / 2. - 0.3);

        SceneGraphComponent catComp = new SceneGraphComponent();
        Transformation gt = new Transformation();

        catComp.setTransformation(gt);
        catComp.setGeometry(catenoid);

        SceneGraphComponent catComp2 = new SceneGraphComponent();
        Transformation gt2 = new Transformation();

        catComp2.setTransformation(gt2);
        catComp2.setGeometry(catenoid);
        catComp2.setGeometry(new CatenoidHelicoid(20));
        Appearance a = new Appearance();
        a.setAttribute(CommonAttributes.EDGE_DRAW,true);
        a.setAttribute(CommonAttributes.DIFFUSE_COLOR,Color.BLUE.brighter());
        a.setAttribute("lineShader."+CommonAttributes.DIFFUSE_COLOR,Color.BLACK);
        catComp2.setAppearance(a);
        Viewer v2 = createViewer(catComp2);
        JFakeFrame f = new JFakeFrame();
        f.getContentPane().add(v2.getViewingComponent());

        catComp.addTool(f.getTool());
        f.setSize(512, 512);
        f.validate();
        f.setVisible(true);
        System.out.print("setting appearance ");
        catComp.setAppearance(f.getAppearance());
        System.out.println("done");
        ViewerApp.display(catComp);
        System.out.println("frame size " + f.getSize());
    }

    private static ToolSystemViewer createViewer(SceneGraphComponent root) {
        Viewer viewer = new de.jreality.soft.DefaultViewer();
        //Viewer viewer = new de.jreality.jogl.Viewer();

        // renderTrigger.addViewer(viewerSwitch);

        ToolSystemConfiguration cfg = null;

        try {
            cfg = ToolSystemConfiguration.loadDefaultDesktopConfiguration();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (cfg == null)
            throw new IllegalStateException("couldn't load tool config");
        ToolSystemViewer v = new ToolSystemViewer(viewer, cfg);
        v.setPickSystem(new AABBPickSystem());
        SceneGraphComponent scene = new SceneGraphComponent();

        scene.addChild(root);
        SceneGraphComponent camNode = new SceneGraphComponent();
        Camera c = new Camera();
        camNode.setCamera(c);
        camNode.setTransformation(new Transformation());
        MatrixBuilder.euclidean().translate(0, 0, 20).assignTo(camNode);
        RotateTool rt = new RotateTool();
        rt.setMoveChildren(true);
        root.addTool(rt);
        //scene.addTool(new ScaleTool());
        //scene.addTool(new TranslateTool());
        //scene.addTool(new EncompassTool());
        scene.addChild(camNode);
        SceneGraphPath cp = (SceneGraphPath) SceneGraphUtility.getPathsBetween(
                scene, c).get(0);

        Appearance a = new Appearance();
        a.setAttribute(CommonAttributes.BACKGROUND_COLOR, Color.WHITE);
        scene.setAppearance(a);

        SceneGraphComponent lightNode = new SceneGraphComponent();
        Transformation lt = new Transformation();
        MatrixBuilder.euclidean().rotate(-Math.PI / 4, 1, 1, 0).assignTo(lt);
        // lt.setRotation(-Math.PI / 4, 1, 1, 0);
        lightNode.setTransformation(lt);
        DirectionalLight light = new DirectionalLight();
        lightNode.setLight(light);
        // root.addChild(lightNode);

        SceneGraphComponent lightNode2 = new SceneGraphComponent();
        Transformation lt2 = new Transformation();
        // lt2.assignScale(-1);
        MatrixBuilder.euclidean().rotate(-Math.PI / 4, 1, 1, 0).assignTo(lt2);
        // lt.setRotation(-Math.PI / 4, 1, 1, 0);
        lightNode2.setTransformation(lt2);
        DirectionalLight light2 = new DirectionalLight();

        lightNode2.setLight(light2);

        scene.addChild(lightNode);
        scene.addChild(lightNode2);

        v.setSceneRoot(scene);
        v.setCameraPath(cp);
        v.setAvatarPath(cp);
        v.initializeTools();
        RenderTrigger trigger = new RenderTrigger();
        trigger.addSceneGraphComponent(root);
        trigger.addViewer(v);
        return v;
    }

}
