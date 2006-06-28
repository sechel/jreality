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
