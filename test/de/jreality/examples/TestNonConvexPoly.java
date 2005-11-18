/*
 * Created on 28.04.2004
 *
 * This file is part of the de.jreality.examples package.
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
package de.jreality.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.proxy.treeview.SceneTreeViewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.soft.DefaultViewer;
import de.jreality.soft.MouseTool;
import de.jreality.soft.SVGViewer;
import de.jreality.util.SceneGraphUtility;

/**
 * 
 * @version $Version$
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class TestNonConvexPoly implements KeyListener{
    private DefaultViewer v;
    private Frame         frame;
    private MouseTool     mouseTool;
    private Camera        firstCamera;
    /**
     * 
     */
    public TestNonConvexPoly() {
        super();
        v= new DefaultViewer(false);
        v.setBackground(Color.BLUE);
        frame= new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setLayout(new BorderLayout());
        frame.add(v, BorderLayout.CENTER);

        mouseTool= new MouseTool(v);
        mouseTool.setViewer(v.getViewingComponent());
        v.getViewingComponent().addKeyListener(this);
        //checkMouseWheel();

        //
        // the scene root;
        //
        SceneGraphComponent root = new SceneGraphComponent();
        buildView(root);
        buildScene(root);
        
        
        
        
        //v.setBackground(1,1,1);       
        v.setSceneRoot(root);
        v.setCameraPath(((SceneGraphPath) SceneGraphUtility.getPathsBetween(root, firstCamera).get(0)));
        mouseTool.setRoot(root);

        frame.add(SceneTreeViewer.getViewerComponent(root, v), BorderLayout.WEST);
        frame.setSize(780, 580);
        frame.validate();
        frame.setVisible(true);

        v.render();
    }

    /**
     * @param root
     */
    private void buildScene(SceneGraphComponent root) {
        double points[] =
            { 1, 1, 1, 
             -1, 1,-1,
             -1,-1, 1};
//        -1,-1, 1,
//        1,-1,-1};
        
//        double points[] =
//            { 1, 1, 0, 
//                -1, 1,0,
//                -1,-1,0,
//                1,-1,0};
//        
        IndexedFaceSet fs = new IndexedFaceSet();
        fs.setVertexCountAndAttributes(Attribute.COORDINATES,
          new DoubleArrayArray.Inlined(points, 3));
//        QuadMeshShape qm = new QuadMeshShape(2,2,false,false);
//        qm.setPoints(points);
//        qm.calculateNormals();
//        qm.buildEdgesFromFaces();
        
        fs.setFaceCountAndAttributes(Attribute.INDICES,
          StorageModel.INT_ARRAY.inlined(3).createReadOnly(new int[] {
            0,1,2}));
        GeometryUtility.calculateAndSetFaceNormals(fs);
        GeometryUtility.calculateAndSetVertexNormals(fs);
        SceneGraphComponent sc =new SceneGraphComponent();
        sc.setGeometry(fs);
        //sc.addGeometry(new UnitSphere());
        Appearance ap= new Appearance();
        sc.setAppearance(ap);
                
        /*
        //ap.setAttribute("transparency", 0.);
        ap.setAttribute("diffuseColor", new Color(0.6f, 0.2f, 0.2f));
        ap.setAttribute("showPoints", true);
        //ap.setAttribute("outline", true);
        ap.setAttribute("lineShader.polygonShader", "flat");
        ap.setAttribute("lineShader.lineWidth", 0.04);
        ap.setAttribute("lineShader.diffuseColor",new Color(0.2f, 0.2f, 0.4f) );
        ap.setAttribute("pointShader.vertexShader", "constant");
        ap.setAttribute("pointShader.pointRadius", 0.1);
        ap.setAttribute("pointShader.outlineFraction", 0.);
        */
        
        ap.setAttribute(CommonAttributes.POLYGON_SHADER, "default");
        //ap.setAttribute(CommonAttributes.POLYGON_SHADER+".vertexShader", "constant");
        ap.setAttribute(CommonAttributes.TRANSPARENCY, 0);
        ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
        ap.setAttribute(CommonAttributes.FACE_DRAW, true);
        ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
        ap.setAttribute("color", new Color(1f,0f,0f));
        //ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER, "default");
        //ap.setAttribute("lineShader.polygonShader.vertexShader", "constant");
        //ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(0.2f,.6f,.4f));
        //ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TRANSPARENCY, 0.0);
        //ap.setAttribute(CommonAttributes.POINT_RADIUS, 0.05);
        //ap.setAttribute(CommonAttributes.LINE_WIDTH, 0.05);
        
        
        
        root.addChild(sc);
        
        
        ClippingPlane cp =new ClippingPlane();
        SceneGraphComponent cpc = new SceneGraphComponent();
        cpc.setGeometry(cp);
        root.addChild(cpc);
    }

    public static void main(String[] args) {
            Logger.getLogger("de.jreality").setLevel(Level.INFO);
            Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);
            TestNonConvexPoly t= new TestNonConvexPoly();
    }
    
    
    private SceneGraphComponent buildView(SceneGraphComponent root) {

        //
        // camera
        //
        SceneGraphComponent cameraNode= new SceneGraphComponent();
        Transformation ct= new Transformation();
        ct.setTranslation(0, 0, 16);
        cameraNode.setTransformation(ct);
        firstCamera= new Camera();
        cameraNode.setCamera(firstCamera);

        SceneGraphComponent lightNode= new SceneGraphComponent();
        Transformation lt= new Transformation();
        lt.setRotation(-Math.PI / 4, 1, 1, 0);
        lightNode.setTransformation(lt);
        DirectionalLight light= new DirectionalLight();
        lightNode.setLight(light);
        cameraNode.addChild(lightNode);

        SceneGraphComponent lightNode2= new SceneGraphComponent();
        Transformation lt2= new Transformation();
        //   lt2.assignScale(-1);
        lt.setRotation(-Math.PI / 4, 1, 1, 0);
        lightNode2.setTransformation(lt2);
        DirectionalLight light2= new DirectionalLight();
        lightNode2.setLight(light2);
        cameraNode.addChild(lightNode2);

        mouseTool.setCamera(firstCamera);

        Appearance ap= new Appearance();
        ap.setAttribute("diffuseColor", new Color(1f, 0f, 0f));
        ap.setAttribute("lightingEnabled", true);
        
        root.setAppearance(ap);


        root.addChild(cameraNode);
        return root;
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == 's') {
            SVGViewer svgv =new SVGViewer("test.svg");
            svgv.initializeFrom(v);
            System.out.println(" Rendering SVG into test.svg");
            svgv.render();
        }
    }
    
}
