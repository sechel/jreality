/*
 * Created on Apr 15, 2005
 *
 * This file is part of the de.jreality.util package.
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

import junit.framework.TestCase;

/**
 * @author weissman
 *
 **/
public class MatrixTest extends TestCase {

    public void testGetColumn() {
        Matrix m = new Matrix();
        double[] thirdColumn = m.getColumn(2);
        assertEquals(thirdColumn, new double[]{0,0,1,0});
    }
    
    static void assertEquals(double[] v1, double[] v2) {
        // null test missing
        assert (v1.length == v2.length);
        for (int i = 0; i < v1.length; i++) assertEquals(v1[i], v2[i], 0);
    }
}
