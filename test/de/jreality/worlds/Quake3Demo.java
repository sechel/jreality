/*
 * Created on Mar 9, 2005
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
package de.jreality.worlds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import de.jreality.reader.Readers;
import de.jreality.reader.quake3.Quake3Converter;
import de.jreality.scene.FactoredTransformation;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jreality.util.CameraUtility;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class Quake3Demo extends AbstractLoadableScene implements TransformationListener {

    Quake3Converter conv;
    SceneGraphComponent camNode;
    
    public void customize(JMenuBar menuBar, Viewer viewer) {
        SceneGraphComponent camNode = CameraUtility.getCameraNode(viewer);
        camNode.getTransformation().addTransformationListener(this);
    }
    
    public SceneGraphComponent makeWorld() {
        conv = new Quake3Converter();
        String mapName = JOptionPane.showInputDialog("please enter map name [ctf<1-4> | dm<0-19> | tourney<1-6>]");
        if (mapName != null) {
            try {
                conv.setInput(Readers.resolveSystemResource("maps/q3"+mapName+".bsp"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            JFileChooser fc = new JFileChooser();
            fc.showOpenDialog(null);
            try {
                conv.setInput(Readers.getInput(fc.getSelectedFile()));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return conv.getComponent();
    }

    volatile boolean setting;
    public void transformationMatrixChanged(TransformationEvent ev) {
        if (setting) return;
        setting = true;
//        System.out.println("Quake3Demo.transformationMatrixChanged()");
        conv.setVisibility((FactoredTransformation) ev.getTransformation());
        setting = false;
    }

}
