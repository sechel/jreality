/*
 * Created on Jun 30, 2005
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

import java.util.*;
import java.util.Collections;
import java.util.HashMap;
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
