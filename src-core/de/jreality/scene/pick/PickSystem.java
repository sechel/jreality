/*
 * Created on Apr 1, 2005
 *
 * This file is part of the de.jreality.scene.tool package.
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
package de.jreality.scene.pick;

import java.util.List;

import de.jreality.scene.SceneGraphComponent;

/**
 * @author brinkman
 * 
 * TODO document PickSystem
 */
public interface PickSystem {
  public abstract void setSceneRoot(SceneGraphComponent root);

  /**
   * 
   * The parameters need to be homogenious coordinates.
   * 
   * @param from
   *          foot point of ray in world coordinates
   * @param to
   *          end point of ray in world coordinates (can be at infinity)
   * @return list of PickResults sorted by distance from foot point
   */
  public abstract List computePick(double[] from, double[] to);

}
