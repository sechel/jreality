/*
 * Created on Mar 8, 2005
 *
 * This file is part of the de.jreality.geometry package.
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
package de.jreality.scene.proxy.scene;

import de.jreality.scene.PointSet;
import de.jreality.scene.data.DataList;

/**
 * @author weissman
 *
 **/
public interface RemoteLabelSet extends RemoteGeometry {
    public abstract void setPositions(DataList positions);
    public abstract void setBitmapFont(int bitmapFont);
    public abstract void setNDCOffset(double[] offset);
    public abstract void setLabels(String[] labels);
}