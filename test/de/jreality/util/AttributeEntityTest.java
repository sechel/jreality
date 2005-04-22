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
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import de.jreality.reader.Input;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.Texture2D;
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
  
  public void testTexture2dSingleton() throws Exception {
    Appearance app1 = new Appearance();

    AttributeImage ai = AttributeImage.load(Readers.resolveDataInput("/home/gollwas/miaomiamologo.jpeg"));
    
    Texture2DInterface tex = (Texture2DInterface) AttributeEntityFactory.createWriter(Texture2DInterface.class, "texture2d", app1);
    tex.setImage(ai);
    tex.setApplyMode(Texture2DInterface.GL_LINEAR_MIPMAP_LINEAR);
    tex.setSScale(0.006);
    
    System.out.println(tex);
    
    EffectiveAppearance ea = EffectiveAppearance.create();
    ea = ea.create(app1);
    
    Texture2DInterface t2 = (Texture2DInterface) AttributeEntityFactory.createReader(Texture2DInterface.class, "texture2d", ea);
    
    System.out.println(t2);
    
    System.out.println("repeatS="+t2.getRepeatS());
    
    assertEquals(t2.getSScale(), 0.006, 0);
    assertEquals(t2.getTScale(), 1, 0);
    assertEquals(t2.getApplyMode(), Texture2DInterface.GL_LINEAR_MIPMAP_LINEAR);
    tex.setTScale(3.14);
    assertEquals(t2.getTScale(), 3.14, 0);
//    assertEquals(t2.getTextureTransformation(), t);
  }

  public void testTexture2dCollection() {
    
    Appearance app1 = new Appearance();
    Appearance app2 = new Appearance();
    Appearance app3 = new Appearance();
    
    LinkedList ll = new LinkedList();
    ll.add(app1);
    ll.add(app2);
    ll.add(app3);
    
    Texture2DInterface tex = (Texture2DInterface) AttributeEntityFactory.createWriter(Texture2DInterface.class, "texture2d", ll);

    tex.writeDefaults();
    
    tex.setApplyMode(Texture2DInterface.GL_LINEAR_MIPMAP_LINEAR);
    tex.setSScale(0.006);
    Transformation t = new Transformation();
    tex.setTextureTransformation(t);
    
    EffectiveAppearance ea1 = EffectiveAppearance.create();
    ea1 = ea1.create(app1);
    EffectiveAppearance ea2 = EffectiveAppearance.create();
    ea2 = ea2.create(app2);
    EffectiveAppearance ea3 = EffectiveAppearance.create();
    ea3 = ea3.create(app3);
    
    
    Texture2DInterface t1 = (Texture2DInterface) AttributeEntityFactory.createReader(Texture2DInterface.class, "texture2d", ea1);
    Texture2DInterface t2 = (Texture2DInterface) AttributeEntityFactory.createReader(Texture2DInterface.class, "texture2d", ea2);
    Texture2DInterface t3 = (Texture2DInterface) AttributeEntityFactory.createReader(Texture2DInterface.class, "texture2d", ea3);
        
    assertEquals(t1.getSScale(), t2.getSScale(), 0);
    assertEquals(t2.getSScale(), t3.getSScale(), 0);
    assertEquals(t3.getSScale(), t1.getSScale(), 0);
    
    assertEquals(t1.getTScale(), t2.getTScale(), 0);
    assertEquals(t2.getTScale(), t3.getTScale(), 0);
    assertEquals(t3.getTScale(), t1.getTScale(), 0);
    
    
    
    assertEquals(t2.getTScale(), 1, 0);
    assertEquals(t2.getApplyMode(), Texture2DInterface.GL_LINEAR_MIPMAP_LINEAR);
    tex.setTScale(3.14);
    assertEquals(t2.getTScale(), 3.14, 0);
    assertEquals(t2.getTextureTransformation(), t);
  }
  
  public void testDefaultShader() throws Exception {
    System.out.println("\n");
    Appearance a = new Appearance();
    DefaultShaderInterface di = (DefaultShaderInterface) AttributeEntityFactory.createWriter(DefaultShaderInterface.class, "plygonShader", a);
    //di.writeDefaults();
    System.out.println(di);
    System.out.println(a);
    System.out.println("\n");
    EffectiveAppearance ea = EffectiveAppearance.create();
    ea = ea.create(a);
    di = (DefaultShaderInterface) AttributeEntityFactory.createReader(DefaultShaderInterface.class, "plygonShader", ea);
    System.out.println(di);
    
  }

}
