/*
 * Created on Jul 29, 2004
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
package de.jreality.remote.util;

/**
 * @author gollwas
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public final class Debug {

	private final static boolean OUTPUT = false;
	
	public static final void debug() {
		if (!OUTPUT) return;
		final String m = new Exception().getStackTrace()[1].getMethodName();
		System.out.println(Thread.currentThread().getName() + " " + m);
	}

	public static final void debug(String message) {
		if (!OUTPUT) return;
		final String m = new Exception().getStackTrace()[1].getMethodName();
		System.out.println(Thread.currentThread().getName() + " " + m + "->"
				+ message);
	}

	public static final void debug(final int i) {
		if (!OUTPUT) return;
		final String m = new Exception().getStackTrace()[i].getMethodName();
		System.out.println(Thread.currentThread().getName() + " " + m);
	}

	public static final void debug(int i, String message) {
		if (!OUTPUT) return;
		final String m = new Exception().getStackTrace()[i].getMethodName();
		System.out.println(Thread.currentThread().getName() + " " + m + "->"
				+ message);
	}
}