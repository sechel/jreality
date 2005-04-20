/*
 * Created on Apr 20, 2005
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
package de.jreality.util;

import java.awt.Color;

import de.jreality.scene.Appearance;
import de.jreality.scene.Transformation;
import junit.framework.TestCase;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class AttributeEntityTest extends TestCase {

  public static void main(String[] args) {
    junit.swingui.TestRunner.run(AttributeEntityTest.class);
  }
  
  public void testTexture2D() {
    Appearance app1 = new Appearance();
    Appearance app2 = new Appearance();
    
    Texture2DInterface tex = (Texture2DInterface) AttributeEntityFactory.createEntity(Texture2DInterface.class);
    tex.registerAppearance(app1, "texture2d");
    tex.registerAppearance(app2, "lightMap");
    tex.setApplyMode(Texture2DInterface.GL_LINEAR_MIPMAP_LINEAR);
    tex.setSScale(0.006);
    tex.setTScale(3.14);
    tex.setTextureTransformation(new Transformation());
    System.out.println("App1:"+app1);
    System.out.println("\n\nApp2:"+app2);
  }

  public void testTexture2DRemove() {
    
    Appearance app1 = new Appearance();
    Appearance app2 = new Appearance();
    
    Texture2DInterface tex = (Texture2DInterface) AttributeEntityFactory.createEntity(Texture2DInterface.class);
    tex.registerAppearance(app1, "texture2d");
    tex.registerAppearance(app2, "lightMap");
    tex.setApplyMode(Texture2DInterface.GL_LINEAR_MIPMAP_LINEAR);
    tex.setSScale(0.006);
    tex.setTScale(3.14);
    tex.setTextureTransformation(new Transformation());
    System.out.println("App1:"+app1);
    System.out.println("\n\nApp2:"+app2);
    tex.unregisterAppearance(app1);
    System.out.println("App1:"+app1);
  }
  
  public void testDefaultShader() {
    Appearance app1 = new Appearance();
    Appearance app2 = new Appearance();
    
    DefaultShaderInterface def = (DefaultShaderInterface) AttributeEntityFactory.createEntity(DefaultShaderInterface.class);
    def.registerAppearance(app1, "");
    def.polygonShader().setDiffuseColor(new Color(122, 13,188));
    def.polygonShader().setSpecularCoefficient(0.81);
    def.setShowLines(false);
    def.setLineShader("twoSide");
    def.lineShader().front().setAmbientColor(new Color(22,11,33));
    def.setTexture2d((Texture2DInterface) AttributeEntityFactory.createEntity(Texture2DInterface.class));
    def.getTexture2d().registerAppearance(app1, "texture2d");
    def.getTexture2d().setApplyMode(Texture2DInterface.GL_CLAMP);
    def.getTexture2d().setMagFilter(Texture2DInterface.GL_ONE_MINUS_SRC_COLOR);
    System.out.println(app1);
  }
}
