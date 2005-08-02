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

import de.jreality.util.math.Matrix;
import de.jreality.util.math.MatrixBuilder;
import junit.framework.TestCase;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class MatrixBuilderTest extends TestCase {

  public static void main(String[] args) {
    junit.swingui.TestRunner.run(MatrixBuilderTest.class);
  }

  public void testOne() {
    OldTransformation o = new OldTransformation();
    Matrix m = MatrixBuilder.euclidian()
    .translate(3,3,3)
    .rotate(Math.PI, new double[]{-3,3,0})
    .translate(3,3,3)
    .rotate(Math.PI, new double[]{-3,3,0})
    .getMatrix();
    FactoredMatrixTest.compareTrafos(o.getMatrix(), m.getArray());
  }
  public void testTwo() {
    OldTransformation o = new OldTransformation();
    Matrix m = MatrixBuilder.euclidian()
    .translate(21,3,9)
    .scale(7,1,3)
    .translate(-3,-3,-3)
    .scale(1./7., 1., 1./3.)
    .getMatrix();
    FactoredMatrixTest.compareTrafos(o.getMatrix(), m.getArray());
  }
}
