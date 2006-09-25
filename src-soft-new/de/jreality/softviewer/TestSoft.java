/*
 * Created on 08.09.2006
 *
 * This file is part of the de.jreality.soft package.
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
package de.jreality.softviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.shader.*;
import de.jreality.soft.PSViewer;
import de.jreality.ui.viewerapp.ViewerApp;

public class TestSoft {

    public TestSoft() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    static Viewer  viewer;

    /**
     * @param args
     */
    public static void main(String[] args) {
        // classic mutually obstructing
        //double[][] points = {{0, 1, 0.1},{1, 0, 0},{0, -1, -0.1}};
        // mutually intersecting
        double[][] points = {{0, 1, 0.1},{1, 0, 1},{0, -1, -0.1}};
        // mutually aligned
        //double[][] points = {{0, 0, 0},{1, 0, 0},{Math.cos(Math.PI*2./3), Math.sin(Math.PI*2./3), 0}};
        // mutually aligned 2
        //double[][] points = {{0, 0, 0},{1, 0, 0},{Math.cos(Math.PI*2./3), Math.sin(Math.PI*2./3), 0}};


        int[][] faces = {{0, 1, 2}};

        IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();

        ifsf.setGenerateEdgesFromFaces(true); // or false
        ifsf.setGenerateFaceNormals(true);
        ifsf.setGenerateVertexNormals(false);

        ifsf.setVertexCount(3);
        ifsf.setFaceCount(1);
        ifsf.setVertexCoordinates(points);
        ifsf.setFaceIndices(faces);

        ifsf.update();

        IndexedFaceSet faceSet = ifsf.getIndexedFaceSet();
        
        faceSet.setEdgeCountAndAttributes(Attribute.INDICES,new IntArrayArray.Array(new int[][] {{0,2}}));
        //ViewerApp.display(faceSet);
        
        Appearance app = new Appearance();
        app.setAttribute(CommonAttributes.TRANSPARENCY, 0);
//        app.setAttribute(CommonAttributes.VERTEX_DRAW, true);
//        app.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, 0.07);
//        app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.yellow);
//        app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, 0.07);
        
        DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(app, true);
        dgs.setShowPoints(true);
        dgs.setShowLines( true );
        DefaultPolygonShader polyShader = (DefaultPolygonShader) dgs.getPolygonShader();
        DefaultPointShader pointShader = (DefaultPointShader) dgs.getPointShader();
        DefaultLineShader lineShader = (DefaultLineShader) dgs.getLineShader();
        
        //polyShader.setDiffuseColor(Color.green);
        lineShader.setDiffuseColor(Color.yellow);
        lineShader.setTubeRadius(new Double(0.07));
        pointShader.setPointRadius(new Double(0.07));
        
        SceneGraphComponent cmp = new SceneGraphComponent();
        SceneGraphComponent cmp1 = new SceneGraphComponent();
        SceneGraphComponent cmp2 = new SceneGraphComponent();
        SceneGraphComponent cmp3 = new SceneGraphComponent();

        cmp.setAppearance(app);
        cmp.addChild(cmp1);
        cmp.addChild(cmp2);
        cmp.addChild(cmp3);
        cmp1.setGeometry(faceSet);
        cmp2.setGeometry(faceSet);
        cmp3.setGeometry(faceSet);
        
        MatrixBuilder.euclidean().rotate(2. * Math.PI/3.,0,0,1).translate(0*.2,0,0).assignTo(cmp1);
        MatrixBuilder.euclidean().rotate(4. * Math.PI/3.,0,0,1).translate(0*.2,0,0).assignTo(cmp2);
        //MatrixBuilder.euclidean().rotate(1. * Math.PI/2.,0,1,0).translate(0.5,-.6,0).assignTo(cmp1);
        //MatrixBuilder.euclidean().rotate(1. * Math.PI/2.,0,1,0).translate(-0.3,-.3,0.1).assignTo(cmp2);
        
        app = new Appearance();
        app.setAttribute(CommonAttributes.DIFFUSE_COLOR,Color.RED);
        cmp2.setAppearance(app);

        app = new Appearance();
        app.setAttribute(CommonAttributes.DIFFUSE_COLOR,Color.GREEN);
        cmp3.setAppearance(app);
        
        System.setProperty("de.jreality.scene.Viewer", "de.jreality.softviewer.SoftViewer");
        //System.setProperty("de.jreality.scene.Viewer", "de.jreality.jogl.Viewer");
        ViewerApp va = ViewerApp.display(cmp);
        viewer =  va.getViewer();
        Component c = (Component) va.getViewer().getViewingComponent();
        c.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                System.out.println(" typed");
                if(e.getKeyChar() =='p') {
                    System.out.println("writing");
                    Dimension d = viewer.getViewingComponentSize();
                    PSRenderer ps;
                    try {
                        ps = new PSRenderer(new PrintWriter(new File("/tmp/test.ps")),d.width,d.height);
                        ps.setSceneRoot(viewer.getSceneRoot());
                        ps.setCameraPath(viewer.getCameraPath());
                        ps.render();
                    } catch (FileNotFoundException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }

            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub
                
            }

            public void keyReleased(KeyEvent e) {
                // TODO Auto-generated method stub
                
            }
            
        });
    }

}
