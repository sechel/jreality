/*
 * Created on 06.09.2006
 *
 * This file is part of the de.jreality.soft package.
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
package de.jreality.softviewer;

import java.util.Comparator;

public class TriangleComparator implements Comparator {

    public TriangleComparator() {
        super();
        // TODO Auto-generated constructor stub
    }

    public int compare(Object o1, Object o2) {
        double z = ( (Triangle)o1).getCenterZ();
        z -= ( (Triangle)o2).getCenterZ();
        return z<0?1:(z>0?-1:0);
        }
}
