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

import de.jreality.math.Matrix;
import junit.framework.TestCase;

/**
 * @author weissman
 *
 **/
public class MatrixTest extends TestCase {

	final Matrix id = new Matrix(new double[] {
			1, 0, 0, 0,
			0, 1, 0, 0,
			0, 0, 1, 0,
			0, 0, 0, 1}
	);
	Matrix rnd;
	final double[] range = new double[] {
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11,
			12, 13, 14, 15
	};
	
	protected void setUp() {
	     double[] v = new double[16];
	     do {
	     	for(int i=0; i<16; i++) {
	     		v[i]=Math.random();
	     	}
	     	rnd = new Matrix(v);
	     } while (rnd.getDeterminant()<Matrix.TOLERANCE);  
	}
	
	public void testConstructor() {
		Matrix m0 = new Matrix();
		Matrix m1 = new Matrix(range);
		Matrix m2 = new Matrix(m1);
		assertTrue(m1.getArray().length==m2.getArray().length);
		assertTrue(m1.getArray().length==16);
		assertFalse(m1.getArray()==m2.getArray());
		assertTrue(m1.getArray()==range);
		for(int i=0; i<16; i++) {
			assertTrue(m1.getArray()[i] == m2.getArray()[i]);
			assertTrue(m0.getArray()[i] == id.getArray()[i]);
		}
	}
	
	public void testEquals() {
		Matrix m1 = new Matrix(range);
		Matrix m2 = new Matrix(m1);
		assertTrue(m1.equals(m2));
		for(int i=0; i<16; i++) {
			double tmp = m1.getArray()[i];
			m1.getArray()[i]=-1;
			assertFalse(m1.equals(m2));
			m1.getArray()[i]=tmp;
		}
		
	}
	
	public void testAssign() {
		Matrix m = new Matrix();
		double[] v = m.getArray();
		m.assignFrom(range);
		assertTrue(m.getArray()==v);
		for(int i=0; i<16; i++) {
			assertTrue(m.getArray()[i]==range[i]);
		}
		m.assignFrom(rnd);
		assertTrue(m.getArray()==v);
		for(int i=0; i<16; i++) {
			assertTrue(m.getArray()[i]==rnd.getArray()[i]);
		}
		m.assignIdentity();
		assertTrue(m.getArray()==v);
		assertTrue(m.equals(id));
	}
	
	public void testEntry() {
		Matrix m = new Matrix(rnd);
		for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
				assertTrue(m.getArray()[4*i+j]==m.getEntry(i, j));
				double x=Math.random();
				m.setEntry(i, j, x);
				assertTrue(m.getEntry(i, j)==x);
			}
		}
	}
	
	public void testTrace() {
		Matrix m = new Matrix(range);
		assertTrue(m.getTrace()==30);
	}
	
	public void testTranspose() {
		Matrix m = new Matrix(rnd);
		m.transpose();
		Matrix mm = m.getTranspose();
		for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
				assertTrue(m.getEntry(i, j)==rnd.getEntry(j, i));
				assertTrue(mm.getEntry(i, j)==rnd.getEntry(i, j));
			}
		}
	}
	
	public void testWrite() {
		double[] v = rnd.writeToArray(null);
		assertTrue(v!=rnd.getArray());
		for(int i=0; i<16; i++) {
			assertTrue(v[i]==rnd.getArray()[i]);
		}
		(new Matrix(range)).writeToArray(v);
		for(int i=0; i<16; i++) {
			assertTrue(v[i]==range[i]);
		}
	}
	
	
/*   
  
  TODO: Write tests for the following methods:
  
    public static Matrix product(Matrix A, Matrix B)
    public static Matrix sum(Matrix A, Matrix B)
    public static Matrix conjugate(Matrix A, Matrix B)
    public double getDeterminant()
    public double[] getRow(int i) 
    public void setRow(int i, double[] v)
    public double[] getColumn(int i)
    public void setColumn(int i, double[] v)
    public void multiplyOnRight(double[] T)
    public void multiplyOnRight(Matrix T)
    public void multiplyOnLeft(double[] T)
    public void multiplyOnLeft(Matrix T)
    public void conjugateBy(Matrix T)
    public void add(Matrix T)
    public Matrix getInverse() 
    public void invert()
    public String toString()
    
Done, sort of:
    public Matrix()
    public Matrix(Matrix T)
    public Matrix(double[] m)
    public boolean equals(Matrix T) 
    public void assignFrom(double[] initValue)
    public void assignFrom(Matrix initValue) 
    public void assignIdentity()
    public double[] getArray()
    public double getEntry(int row, int column)
    public void setEntry(int row, int column, double value)
    public double getTrace()
    public void scale(double lambda)
    public Matrix getTranspose()
    public void transpose()
    public double[] writeToArray(double[] aMatrix)
 
*/
}
