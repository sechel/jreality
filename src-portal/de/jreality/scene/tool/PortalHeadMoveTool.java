/*
 * Created on May 30, 2005
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
package de.jreality.scene.tool;

import java.util.Collections;
import java.util.List;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;

/**
 * @author weissman
 *
 **/
public class PortalHeadMoveTool extends Tool {
  
  final transient InputSlot headSlot = InputSlot.getDevice("AvatarShipTransformation");
  final transient List used = Collections.singletonList(headSlot);
  
  public List getActivationSlots() {
    return Collections.EMPTY_LIST;
  }

  public List getCurrentSlots() {
    return used;
  }

  public List getOutputSlots() {
    return Collections.EMPTY_LIST;
  }

  transient double[] tmp = new double[16];
  transient Viewer viewer;
  
  public void perform(ToolContext tc) {
    if (viewer == null) {
      viewer = tc.getViewer();
    }
    SceneGraphComponent head = tc.getRootToToolComponent().getLastComponent();
    if (head.getTransformation() == null) head.setTransformation(new Transformation());
    head.getTransformation().setMatrix(tc.getTransformationMatrix(headSlot).toDoubleArray(tmp));
    viewer.render();
  }

  public void activate(ToolContext tc) {
  }

  public void deactivate(ToolContext tc) {
  }
  
}
