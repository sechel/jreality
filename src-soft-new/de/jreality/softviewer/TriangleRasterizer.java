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

public abstract class TriangleRasterizer {

    public TriangleRasterizer() {
        super();
        // TODO Auto-generated constructor stub
    }
    public abstract void start();
    public abstract void stop();
    public abstract void renderTriangle(final Triangle t, final boolean outline);
    public abstract void setBackground(int argb);
    public abstract int getBackground();
  public abstract void clear();
    public abstract void setWindow(final int xmin, final int xmax, final int ymin, final int ymax);
    public abstract void setSize(final double width, final double height);
}
