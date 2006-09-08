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

public class Triangle extends AbstractPolygon {
    private final double[] p0 = new double[VERTEX_LENGTH];

    private final double[] p1 = new double[VERTEX_LENGTH];

    private final double[] p2 = new double[VERTEX_LENGTH];

    public Triangle() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public final double[] getPoint(int i) {
        switch (i) {
        case 0:
            return p0;
        case 1:
            return p1;
        case 2:
            return p2;
        default:
            throw new IllegalArgumentException(
                    "a triangle has only three points");
        }
    }

    public final double[] getP1() {
        return p1;
    }

    public final double[] getP2() {
        return p2;
    }

    public final double[] getP0() {
        return p0;
    }

    public final double getP1(final int i) {
        return p1[i];
    }

    public final double getP2(final int i) {
        return p2[i];
    }

    public final double getP0(final int i) {
        return p0[i];
    }

    public final void setP1(final int i, final double v) {
        p1[i] = v;
    }

    public final void setP2(final int i, final double v) {
        p2[i] = v;
    }

    public final void setP0(final int i, final double v) {
        p0[i] = v;
    }

    public final double getCenterZ() {
        return (p0[SZ] + p1[SZ] + p2[SZ]) / 3;
    }

    @Override
    public int getLength() {
        return 3;
    }

}
