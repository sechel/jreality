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

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.*;
import de.jreality.ui.viewerapp.ViewerApp;

public class TestSoft {

    public TestSoft() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        double[][] points = {{0, 1, 0},{1, 0, 0},{0, -1, 0}, {-1, 0, 0}};
        int[][] faces = {{0, 1, 2, 3}};

        IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();

        ifsf.setGenerateEdgesFromFaces(true); // or false
        ifsf.setGenerateFaceNormals(true);
        ifsf.setGenerateVertexNormals(false);

        ifsf.setVertexCount(4);
        ifsf.setFaceCount(1);
        ifsf.setVertexCoordinates(points);
        ifsf.setFaceIndices(faces);

        ifsf.update();

        IndexedFaceSet faceSet = ifsf.getIndexedFaceSet();
        
        //ViewerApp.display(faceSet);
        
        Appearance app = new Appearance();
        app.setAttribute(CommonAttributes.TRANSPARENCY, 0);
//        app.setAttribute(CommonAttributes.VERTEX_DRAW, true);
//        app.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, 0.07);
//        app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.yellow);
//        app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, 0.07);
        
        DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(app, true);
        dgs.setShowPoints(Boolean.TRUE);
        dgs.setShowLines(Boolean.TRUE);
        DefaultPolygonShader polyShader = (DefaultPolygonShader) dgs.getPolygonShader();
        DefaultPointShader pointShader = (DefaultPointShader) dgs.getPointShader();
        DefaultLineShader lineShader = (DefaultLineShader) dgs.getLineShader();
        
        polyShader.setDiffuseColor(Color.green);
        lineShader.setDiffuseColor(Color.yellow);
        lineShader.setTubeRadius(new Double(0.07));
        pointShader.setPointRadius(new Double(0.07));
        
        SceneGraphComponent cmp = new SceneGraphComponent();
        
        cmp.setAppearance(app);
        cmp.setGeometry(faceSet);
        
        System.setProperty("de.jreality.scene.Viewer", "de.jreality.softviewer.SoftViewer");
        //System.setProperty("de.jreality.scene.Viewer", "de.jreality.jogl.Viewer");
        ViewerApp.display(cmp);

    }

}
