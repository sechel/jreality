/*
 * Created on Apr 23, 2005
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

import java.io.IOException;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.Texture2D;
import de.jreality.util.AttributeEntityFactory;
import de.jreality.util.ImageData;
import de.jreality.util.MatrixBuilder;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class Texture2DShaderTest extends AbstractJOGLLoadableScene {

  public SceneGraphComponent makeWorld() {
    SceneGraphComponent root = new SceneGraphComponent();
    SceneGraphComponent geomComp = new SceneGraphComponent();
    QuadMeshShape heli = new CatenoidHelicoid(20);
    GeometryUtility.calculateAndSetNormals(heli);
    GeometryUtility.calculateAndSetTextureCoordinates(heli);
    Appearance a = new Appearance();
    a.setAttribute("polygonShader.diffuseColor", java.awt.Color.white);
    a.setAttribute(CommonAttributes.EDGE_DRAW, false);
    Texture2D tex = (Texture2D) AttributeEntityFactory.createAttributeEntity(Texture2D.class, "polygonShader.texture2d", a);
    try {
      tex.setImage(ImageData.load(Readers.getInput("final.jpg")));
    } catch (IOException e) {
      e.printStackTrace();
    }
    tex.setTextureMatrix(MatrixBuilder.euclidian().scale(3).getMatrix());
    root.addChild(geomComp);
    geomComp.setGeometry(heli);
    geomComp.setAppearance(a);
    return root;
  }

}
