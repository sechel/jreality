/*
 * Created on Apr 18, 2005
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

import de.jreality.util.math.FactoredMatrix;
import de.jreality.util.math.Rn;
import junit.framework.TestCase;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class FactoredMatrixTest extends TestCase {

  public static void main(String[] args) {
    junit.swingui.TestRunner.run(FactoredMatrixTest.class);
  }

  public void testConstructor() {
    OldTransformation t = new OldTransformation();
    FactoredMatrix fm = new FactoredMatrix();
    compareTrafos(t.getMatrix(), fm.getArray());
  }
  
  public void testTranslation() {
    OldTransformation t = new OldTransformation();
    FactoredMatrix fm = new FactoredMatrix();
    t.setTranslation(2,3,4);
    fm.setTranslation(2,3,4);
    compareTrafos(t.getMatrix(), fm.getArray());
  }
  
  public void testRotation() {
    OldTransformation t = new OldTransformation();
    FactoredMatrix fm = new FactoredMatrix();
    double rot = Math.random() * Math.PI * 2.;
    t.setRotation(rot, 1,1,1);
    fm.setRotation(rot, 1,1,1);
    compareTrafos(t.getMatrix(), fm.getArray());
  }
  
  public void testSequence() {
    OldTransformation t = new OldTransformation();
    FactoredMatrix fm = new FactoredMatrix();
    double rot = Math.random() * Math.PI * 2.;

    t.setRotation(rot, 1,1,1);
    t.setStretch(3);
    t.setStretch(1,1,4);
    t.setTranslation(2,8,8);

    fm.setRotation(rot, 1,1,1);
    fm.setStretch(3);
    fm.setStretch(1,1,4);
    fm.setTranslation(2,8,8);
    
    compareTrafos(t.getMatrix(), fm.getArray());
  }
  
  public static void compareTrafos(double[] d1, double[] d2) {
    System.out.println("d1:\n"+Rn.matrixToString(d1));
    System.out.println("d2:\n"+Rn.matrixToString(d2));
    for (int i = 0; i < 16; i++)
      assertEquals(d1[i], d2[i], 1e-11);
  }

}
