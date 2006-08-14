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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
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


package de.jreality.tools;

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;


/**
 * Rotates a SceneGraphComponent automatically when displaying.<br>
 * Use it the following: 
 * <code>cmp.addTool(new StartRotation(0.02, new double[]{1,1,-1}));</code><br>
 * Stop the rotation with left mouse click.
 */
public class StartRotation  extends AbstractTool {

  private static InputSlot actSlot = InputSlot.getDevice("SystemTime");
  private static InputSlot deactSlot = InputSlot.getDevice("RotateActivation");
  
  private final double angle;
  private final double[] axis;
  private boolean isRunning = false;
  
  
  public StartRotation(double angle, double[] axis) {
    super(null);

    //TODO: setMethods instead of param?
    this.angle = angle;
    this.axis = axis;
    
    addCurrentSlot(actSlot, "Need notification to perform once.");
  }
  
  public void perform(ToolContext tc) {

    final SceneGraphComponent cmp = tc.getRootToToolComponent().getLastComponent();
    
    if (isRunning) {
      removeCurrentSlot(deactSlot);
      AnimatorTool.getInstance().deschedule(cmp);
      cmp.removeTool(this);
      return; 
    }
    
    isRunning = true;
    
    AnimatorTask task = new AnimatorTask() {

      public boolean run(double time, double dt) {
        MatrixBuilder m = MatrixBuilder.euclidean(cmp.getTransformation());
        m.rotate(0.05*dt*angle, axis);
        m.assignTo(cmp);
        return true;
      }
    };

    AnimatorTool.getInstance().schedule(cmp, task);
    
    removeCurrentSlot(actSlot);
    addCurrentSlot(deactSlot);
  }
}