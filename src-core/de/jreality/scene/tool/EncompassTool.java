/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.jreality.scene.tool;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.CameraUtility;

/**
 *
 * TODO: document this
 *
 * @author brinkman
 *
 */
public class EncompassTool extends Tool {
	
	double margin = 1.25;		// value greater than one creates a margin around the encompassed object  

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
