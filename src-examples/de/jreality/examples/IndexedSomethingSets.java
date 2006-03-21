/*
 * $Id: IndexedSomethingSets.java,v 1.6 2006/03/21 08:02:27 gunn Exp $
 * 
 * A short illustration of how to use Indexed*Set.
 * 
 * If this doesn't work because Java can't find libjogl.so, add
 *	-Djava.library.path=/usr/local/lib (or wherever your libjogl.so lives)
 * as an argument to your java virtual machine (use the Run.. menu item to
 * do this if you're using eclipse).
 */

package de.jreality.examples;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.Viewer;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.shader.CommonAttributes;


public class IndexedSomethingSets {
    public static void main(String[] args) {
        SceneGraphComponent rootNode=new SceneGraphComponent();
        SceneGraphComponent pointNode=new SceneGraphComponent();
        SceneGraphComponent lineNode=new SceneGraphComponent();
        SceneGraphComponent faceNode=new SceneGraphComponent();
        SceneGraphComponent cameraNode=new SceneGraphComponent();
        SceneGraphComponent lightNode=new SceneGraphComponent();
 
        rootNode.addChild(pointNode);
        rootNode.addChild(lineNode);
        rootNode.addChild(faceNode);
        rootNode.addChild(cameraNode);
        cameraNode.addChild(lightNode);
        
        Camera camera=new Camera();
        Light light=new DirectionalLight();
        
        double vertices[][]=new double[6][];
        double flatvertices[]=new double[18];
        for(int i=0; i<6; i++) {
        	vertices[i]=new double[] {Math.cos(2*Math.PI*i/6), Math.sin(2*Math.PI*i/6), 0};
        	flatvertices[3*i]=vertices[i][0];
        	flatvertices[3*i+1]=vertices[i][1];
        	flatvertices[3*i+2]=vertices[i][2];
        }
        
        PointSet pointset=new PointSet();
        pointset.setVertexCountAndAttributes(Attribute.COORDINATES,
        		new DoubleArrayArray.Array(vertices));
        	
        IndexedLineSet lineset=new IndexedLineSet();
        lineset.setVertexCountAndAttributes(Attribute.COORDINATES,
        		new DoubleArrayArray.Inlined(flatvertices, 3));
        lineset.setEdgeCountAndAttributes(Attribute.INDICES,
        		new IntArrayArray.Array(new int[][] {{0, 1, 2, 3, 4, 5, 0}}));
        
        IndexedFaceSet faceset=new IndexedFaceSet();
        faceset.setVertexCountAndAttributes(Attribute.COORDINATES,
        		new DoubleArrayArray.Array(vertices));
        faceset.setFaceCountAndAttributes(Attribute.INDICES,
        		new IntArrayArray.Array(new int[][] {{0, 1, 2, 3, 4, 5, 0}}));
        GeometryUtility.calculateAndSetNormals(faceset);
        
        pointNode.setGeometry(pointset);
        lineNode.setGeometry(lineset);
        faceNode.setGeometry(faceset);
        cameraNode.setCamera(camera);
        lightNode.setLight(light);
        
        MatrixBuilder.euclidean().translate(0, 0, 4).assignTo(cameraNode);
        MatrixBuilder.euclidean().rotate(-Math.PI/4, 1, 1, 0).assignTo(lightNode);

    	Appearance rootApp= new Appearance();
        rootApp.setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0f, .1f, .1f));
        rootApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(1f, 0f, 0f));
        rootApp.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,true);
     
    	Appearance pointApp= new Appearance();
        pointApp.setAttribute(CommonAttributes.DIFFUSE_COLOR, new Color(0f, 0f, 1f));
        pointApp.setAttribute(CommonAttributes.VERTEX_DRAW, true);
        pointApp.setAttribute(CommonAttributes.SPHERES_DRAW, true);
        pointApp.setAttribute(CommonAttributes.POINT_RADIUS, .1);
        
    Appearance faceApp= new Appearance();
        faceApp.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(0f, 1f, 1f));
       faceApp.setAttribute(CommonAttributes.FACE_DRAW, true);
         
       rootNode.setAppearance(rootApp);
       pointNode.setAppearance(pointApp);
       faceNode.setAppearance(faceApp);

       Viewer viewer=new Viewer();
        viewer.setSceneRoot(rootNode);
        
        SceneGraphPath cameraPath=new SceneGraphPath();
        cameraPath.push(rootNode);
        cameraPath.push(cameraNode);
        cameraPath.push(camera);
        viewer.setCameraPath(cameraPath);

        Frame frame=new Frame();
        frame.add(viewer.getViewingComponent());
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setSize(780, 580);
        frame.validate();
        frame.setVisible(true);
        
        viewer.render();
   }
}
