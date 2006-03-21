/*
 * Created on Apr 10, 2005
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
package de.jreality.scene.tool;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import de.jreality.math.*;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.util.CameraUtility;
import de.jreality.util.LoggingSystem;

/**
 *
 * TODO: document this
 *
 * @author brinkman
 *
 */
public class EncompassTool extends Tool {
	
	double margin = 1.0;		// value greater than one creates a margin around the encompassed object  

  transient List usedSlots = new LinkedList();

  transient List activationSlots = Collections.EMPTY_LIST;

  static InputSlot encompassSlot = InputSlot.getDevice("EncompassActivation");

  public EncompassTool() {
    usedSlots.add(encompassSlot);
  }

  public List getActivationSlots() {
    return activationSlots;
  }

  public List getCurrentSlots() {
    return usedSlots;
  }

  public List getOutputSlots() {
    return Collections.EMPTY_LIST;
  }

  transient SceneGraphComponent comp;

  transient Matrix centerTranslation = new Matrix();
  
  public void activate(ToolContext tc) {
  }

  public void perform(ToolContext tc) {
    if (tc.getAxisState(encompassSlot).isPressed()) {
      if (false) {
        CameraUtility.encompass(tc.getViewer());
      }
      // TODO get the signature from the effective appearance of avatar path
      CameraUtility.encompass(tc.getToolSystem().getAvatarPath(), tc.getRootToLocal(), tc.getViewer().getCameraPath(), margin, tc.getViewer().getSignature());
    }
  }

  public void deactivate(ToolContext tc) {
  }
  
  public void setMargin(double p)	{
	  margin = p;
  }
  
  public double getMargin()	{
	  return margin;
  }
}
