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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
class AnimatorTool extends Tool implements AnimationSystem {
  
  private static AnimatorTool instance = new AnimatorTool();
  
  static AnimatorTool getInstance() {
    return instance;
  }
  
  private AnimatorTool() {}

  private IdentityHashMap animators = new IdentityHashMap();
  private final Object mutex = new Object();

  InputSlot timer = InputSlot.getDevice("SystemTime");
  List slots = new LinkedList();
  { slots.add(timer); }
  

  public List getActivationSlots() {
    return Collections.EMPTY_LIST;
  }
  public List getCurrentSlots() {
    return slots;
  }

  public List getOutputSlots() {
    return Collections.EMPTY_LIST;
  }

  public void activate(ToolContext tc) {
  }

  private double time;
  private double dtime;
  
  private AnimatorContext context = new AnimatorContext() {
    public double getTime() {
      return time;
    }
    public double getDTime() {
      return dtime;
    }
  };
  
  public void perform(ToolContext tc) {
    if (animators.isEmpty()) return;
    time = System.currentTimeMillis();
    dtime = tc.getAxisState(timer).doubleValue();
    synchronized (mutex) {
      for (Iterator i = animators.values().iterator(); i.hasNext(); ) {
        AnimatorTask task = (AnimatorTask)i.next();
        if (!task.run(context)) {
          i.remove();
        }
      }
    } 
  }

  public void deactivate(ToolContext tc) {
  }
  
  public void schedule(Object key, AnimatorTask task) {
    synchronized (mutex) {
      animators.put(key, task);
    }
  }
  
  public void deschedule(Object key) {
    synchronized (mutex) {
      animators.remove(key);
    }
  }

}
