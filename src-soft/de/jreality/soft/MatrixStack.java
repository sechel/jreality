/*
 * Created on Dec 6, 2003
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
package de.jreality.soft;

/**
 * This is a stack of double[16] arrays which keeps the unused arrays for later reuse
 * it grows dynamically. but does not shrink jet...
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 * @deprecated this class is no longer used!
 */
public final class MatrixStack {
	private double[][] stack = new double[10][16];
	private int pos = 0;
	/**
	 * 
	 */
	public MatrixStack() {
		super();
	}

	public final void push() {
		pos++;
		if(pos==stack.length) {
			double [][] newstack = new double[stack.length+10][];
			System.arraycopy(stack,0,newstack,0,stack.length);
			stack = newstack;
		}
	}
	public final double[] pop() {
		pos--;
		return stack[pos];
	}
	public final double[] current() {
		return stack[pos];
	}
	public final void removeAll() {
		pos = 0;
	}
}
