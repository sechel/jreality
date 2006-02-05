/*
 * Created on May 31, 2005
 *
 * This file is part of the de.jreality.scene.pick package.
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
 * @author weissman
 *
 **/
public class SoftPickSystem implements PickSystem {

  PickAction pa;
  
  public void setSceneRoot(SceneGraphComponent root) {
    pa = new PickAction(root);
  }

  public List computePick(double[] from, double[] to) {
    pa.setPickSegment(from, to);
    return (List) pa.visit();
  }

}
