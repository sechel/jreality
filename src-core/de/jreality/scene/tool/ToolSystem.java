/*
 * Created on Apr 2, 2005
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

import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.pick.PickSystem;
import de.jreality.scene.tool.config.ToolSystemConfiguration;
import de.jreality.util.LoggingSystem;

/**
 * 
 * TODO: document this
 * 
 * @author weissman
 *  
 */
public class ToolSystem implements ToolEventReceiver {

  private final LinkedList compQueue = new LinkedList();

  private final LinkedList triggerQueue = new LinkedList();
  private final HashMap toolToPath = new HashMap();
  private List pickResults = Collections.EMPTY_LIST;
  
  private SceneGraphPath emptyPickPath=new SceneGraphPath();
  
  private Viewer viewer;
  private ToolContextImpl toolContext;
  private DeviceManager deviceManager;
  private ToolManager toolManager;
  private SlotManager slotManager;
  private PickSystem pickSystem;
  private ToolUpdateProxy updater;
  private ToolEventQueue eventQueue;
  
  private boolean executing;
  
  private class ToolContextImpl implements ToolContext {

    InputSlot sourceSlot;
    ToolEvent event;
    private SceneGraphPath rootToLocal;
    private SceneGraphPath rootToToolComponent;
    private Tool currentTool;
    
    boolean rejected;

    public Viewer getViewer() {
      return viewer;
    }

    public InputSlot getSource() {
      return event.getInputSlot();
    }

    public DoubleArray getTransformationMatrix(InputSlot slot) {
      return deviceManager.getTransformationMatrix(slot);
    }

    public AxisState getAxisState(InputSlot slot) {
      return deviceManager.getAxisState(slot);
    }

    public long getTime() {
      return event.getTimeStamp();
    }

    public void send(ToolEvent event) {
      eventQueue.addEvent(event);
    }

    public void schedule(Object key, AnimatorTask task) {
      AnimatorTool.getInstance().schedule(key, task);
    }

    public void deschedule(Object key) {
      AnimatorTool.getInstance().deschedule(key);
    }

    private void setRootToLocal(SceneGraphPath rootToLocal) {
      this.rootToLocal = rootToLocal;
      rootToToolComponent = null;
    }

    public SceneGraphPath getRootToLocal() {
      return rootToLocal;
    }

    public SceneGraphPath getRootToToolComponent() {
      if (rootToToolComponent == null) {
        LinkedList list = new LinkedList();
        Iterator i = rootToLocal.reverseIterator();
        for (; i.hasNext();) {
          SceneGraphNode cp = (SceneGraphNode) i.next();
          if (!(cp instanceof SceneGraphComponent))
            continue;
          if (((SceneGraphComponent) cp).getTools().contains(currentTool)) {
            list.addFirst(cp);
            while (i.hasNext())
              list.addFirst(i.next());
          }
        }
        rootToToolComponent = SceneGraphPath.fromList(list);
      }
      return rootToToolComponent;
    }

    public PickResult getCurrentPick() {
      performPick();
      return pickResults.isEmpty() ? null : (PickResult) pickResults.get(0);
    }

    private void setCurrentTool(Tool currentTool) {
      this.currentTool = currentTool;
    }

    public ToolSystem getToolSystem() {
      return ToolSystem.this;
    }

    public void reject() {
      rejected=true;
    }
    
    boolean isRejected() {
      return rejected;
    }
  };

  /**
   * TODO: receive scene root / cam path change from viewer!!
   * TODO: make this constructor package level 
   * @param viewer
   */
  public ToolSystem(Viewer viewer, ToolSystemConfiguration config) {
    this.viewer = viewer;
    toolContext = new ToolContextImpl();
    toolManager = new ToolManager();
    eventQueue = new ToolEventQueue(this);
    deviceManager = new DeviceManager(config, eventQueue, viewer);
    slotManager = new SlotManager(config);
    updater = new ToolUpdateProxy(this);
  }

  public void initializeSceneTools() {
    toolManager.cleanUp();
    updater.setSceneRoot(viewer.getSceneRoot());
    // register animator
    SceneGraphPath rootPath = new SceneGraphPath();
    rootPath.push(viewer.getSceneRoot());
    addTool(AnimatorTool.getInstance(), rootPath);
    if (emptyPickPath.getLength() == 0) {
      emptyPickPath.push(viewer.getSceneRoot());
    }
    if (pickSystem != null) pickSystem.setSceneRoot(viewer.getSceneRoot());
    eventQueue.start();
  }

  public void processToolEvent(ToolEvent event) {
    synchronized (mutex) {
    	executing=true;
	    assert (compQueue.isEmpty());
	    assert (triggerQueue.isEmpty());
	    compQueue.addAll(deviceManager.updateImplicitDevices());
	    compQueue.add(event);
	    processComputationalQueue();
	    processTriggerQueue();
      if (!toolsChanging.isEmpty()) {
        final List l = new LinkedList(toolsChanging);
        toolsChanging.clear();
        for (Iterator i = l.iterator(); i.hasNext(); ) {
          Pair p = (Pair) i.next();
          i.remove();
          if (p.added) {
            addToolImpl(p.tool, p.path);
          } else {
            removeToolImpl(p.tool, p.path);
          }
        }
      }
	    executing=false;
    }
  }

  private void processComputationalQueue() {
    while (!compQueue.isEmpty()) {
      ToolEvent event = (ToolEvent) compQueue.removeFirst();
      if (isTrigger(event)) triggerQueue.add(event);
      deviceManager.evaluateEvent(event, compQueue);
    }
  }

  private boolean isTrigger(ToolEvent event) {
    InputSlot slot = event.getInputSlot();
    return slotManager.isActiveSlot(slot) || slotManager.isActivationSlot(slot);
  }

  private void processTriggerQueue() {
    if (triggerQueue.isEmpty())
      return;
    HashSet activatedTools = new HashSet();
    HashSet deactivatedTools = new HashSet();
    HashSet stillActiveTools = new HashSet();
    SceneGraphPath pickPath = null;
    for (Iterator iter = triggerQueue.iterator(); iter.hasNext();) {
      ToolEvent event = (ToolEvent) iter.next();
      toolContext.event = event;
      InputSlot slot = event.getInputSlot();
      toolContext.sourceSlot = slot;

      if (deviceManager.getAxisState(slot) != null
          && deviceManager.getAxisState(slot).isPressed()) {
        Set candidates = new HashSet(slotManager.getToolsActivatedBySlot(slot));

        // contains the Tools sitting in the Scene that need a
        // PickPath to get activated - we will choose the Tool(s) closest to
        // the end of the path
        HashSet candidatesForPick = new HashSet();

        // TODO: see if activating more than one Tool for an axis
        // makes sense...
        for (Iterator i = candidates.iterator(); i.hasNext();) {
          Tool candidate = (Tool) i.next();
          if (!toolManager.needsPick(candidate))
            continue;
          candidatesForPick.add(candidate);
          i.remove();
        }
        if (!candidatesForPick.isEmpty()) {
          // now we need a pick path
          if (pickPath == null)
            pickPath = calculatePickPath();
          int level = pickPath.getLength();
          boolean foundPossibleTools;
          do {
            Collection selection = toolManager.selectToolsForPath(pickPath, level--, candidatesForPick);
            foundPossibleTools=!selection.isEmpty();
            LoggingSystem.getLogger(this).finer(
                "selected pick tools:" + selection);
            for (Iterator j = selection.iterator(); j.hasNext();)
              toolToPath.put(j.next(), pickPath);
            candidates.addAll(selection);
            // now all Tools in the candidates list need to be
            // processed=activated
            activateToolSet(candidates);
          } while (candidates.isEmpty() && foundPossibleTools && level > 0);
          activatedTools.addAll(candidates);
        }
      }
      // process all active tools
      Set active = slotManager.getActiveToolsForSlot(slot);
      stillActiveTools.addAll(active);
      processToolSet(active);
      if (deviceManager.getAxisState(slot) != null
          && deviceManager.getAxisState(slot).isReleased()) {
        Set deactivated = findDeactivatedTools(slot);
        deactivatedTools.addAll(deactivated);
        deactivateToolSet(deactivated);
      }
    }
    triggerQueue.clear();
    // don't update used slots for deactivated tools!
    stillActiveTools.removeAll(deactivatedTools);
    slotManager.updateMaps(stillActiveTools, activatedTools, deactivatedTools);
  }

  private double[] pointerTrafo = new double[16];

  private double[] currentPointer = new double[16];

private final Object mutex=new Object();

private SceneGraphPath avatarPath;

  private void performPick() {
    currentPointer = deviceManager.getTransformationMatrix(
          InputSlot.getDevice("PointerTransformation")).toDoubleArray(
          currentPointer);
    if (Rn.equals(pointerTrafo, currentPointer)) return;
    Rn.copy(pointerTrafo, currentPointer);
    double[] to = new double[] { -pointerTrafo[2], -pointerTrafo[6],
        -pointerTrafo[10], -pointerTrafo[14], };
    double[] from = new double[] { pointerTrafo[3], pointerTrafo[7],
        pointerTrafo[11], pointerTrafo[15], };
    pickResults = pickSystem.computePick(from, to);
  }

  private SceneGraphPath calculatePickPath() {
    performPick();
    if (pickResults.isEmpty()) {
      return emptyPickPath;
    }
    PickResult result = (PickResult) pickResults.get(0);
    LoggingSystem.getLogger(this).fine("ToolSystem.calculatePickPath() <HIT>");
    return result.getPickPath();
  }

  /**
   * calls perform(ToolContext tc) for all tools in the given Set
   * removes Tools from the set if the tool rejected the activation.
   * 
   * @param toolSet
   * @return false if the current level of tools rejected the context...
   */
  private void activateToolSet(Set toolSet) {
    for (Iterator iter = toolSet.iterator(); iter.hasNext();) {
      Tool tool = (Tool) iter.next();
      toolContext.setCurrentTool(tool);
      toolContext.setRootToLocal((SceneGraphPath) toolToPath.get(tool));
      toolContext.event.device=slotManager.resolveSlotForTool(tool, toolContext.sourceSlot);
      if (toolContext.event.device == null) {
        LoggingSystem.getLogger(this).warning("activate: resolving "+toolContext.sourceSlot+" failed: "+tool.getClass().getName());
      }
      tool.activate(toolContext);
      if (toolContext.isRejected()) {
        iter.remove();
        toolContext.rejected=false;
      }
    }
  }

  /**
   * calls perform(ToolContext tc) for all tools in the given Set
   * 
   * @param toolSet
   */
  private void processToolSet(Set toolSet) {
    for (Iterator iter = toolSet.iterator(); iter.hasNext();) {
      Tool tool = (Tool) iter.next();
      toolContext.setCurrentTool(tool);
      toolContext.setRootToLocal((SceneGraphPath) toolToPath.get(tool));
      toolContext.event.device=slotManager.resolveSlotForTool(tool, toolContext.sourceSlot);
      tool.perform(toolContext);
    }
  }

  /**
   * calls perform(ToolContext tc) for all tools in the given Set
   * 
   * @param toolSet
   */
  private void deactivateToolSet(Set toolSet) {
    for (Iterator iter = toolSet.iterator(); iter.hasNext();) {
      Tool tool = (Tool) iter.next();
      toolContext.setCurrentTool(tool);
      toolContext.setRootToLocal((SceneGraphPath) toolToPath.get(tool));
      toolContext.event.device=slotManager.resolveSlotForTool(tool, toolContext.sourceSlot);
      if (toolContext.event.device == null) {
        LoggingSystem.getLogger(this).warning("deavtivate: resolving "+toolContext.sourceSlot+" failed: "+tool.getClass().getName());
      }
      tool.deactivate(toolContext);
    }
  }

  /**
   * the given slot must have AxisState.isReleased() == true
   * this is garanteed in processTrigger...
   * 
   * @param slot
   * @return
   */
  private Set findDeactivatedTools(InputSlot slot) {
    Set deactivated = new HashSet();
    for (Iterator iter = slotManager.getToolsDeactivatedBySlot(slot).iterator(); iter.hasNext();) {
      Tool tool = (Tool) iter.next();
      boolean deactivate = true;
      loop: for (Iterator iter2 = tool.getActivationSlots().iterator(); iter2.hasNext(); ) {
        InputSlot toolSlot = (InputSlot) iter2.next();
        AxisState state = deviceManager.getAxisState(toolSlot);
        if (state == null) {
          LoggingSystem.getLogger(this).warning("Deactivation axis is null");
          continue;
        }
        if (!state.isReleased()) {
          deactivate = false;
          break loop;
        }
      }
      if (deactivate)
        deactivated.add(tool);
    }
    return deactivated;
  }

  public void setPickSystem(PickSystem pickSystem) {
    this.pickSystem = pickSystem;
    pickSystem.setSceneRoot(viewer.getSceneRoot());
  }

  public PickSystem getPickSystem() {
    return pickSystem;
  }

  public AnimationSystem getAnimationSystem() {
    return AnimatorTool.getInstance();
  }

  public void setAvatarPath(SceneGraphPath p) {
    avatarPath=p;
    deviceManager.setAvatarPath(avatarPath);
  }
  
  public SceneGraphPath getAvatarPath() {
    return avatarPath;
  }

  public void dispose() {
    eventQueue.dispose();
    deviceManager.dispose();
    updater.dispose();
  }
  
  final List toolsChanging = new LinkedList();
  
  private static class Pair {
	  final Tool tool;
	  final SceneGraphPath path;
    final boolean added;
	  Pair(Tool tool, SceneGraphPath path, boolean added) {
		  this.path=path;
		  this.tool=tool;
      this.added=added;
	  }
  }
  
  void addTool(Tool tool, SceneGraphPath path) {
	synchronized (mutex) {
	  if (executing)
        toolsChanging.add(new Pair(tool, path, true));
	  else addToolImpl(tool, path);
	}
  }

  void removeTool(Tool tool, SceneGraphPath path) {
	synchronized (mutex) {
	  if (executing)
        toolsChanging.add(new Pair(tool, path, false));
	  else removeToolImpl(tool, path);
	}
  }
  
  /**
   * TODO: sync
   * @param tool
   * @param path
   */
  void addToolImpl(Tool tool, SceneGraphPath path) {
    boolean first = toolManager.addTool(tool, path);
    if (!toolManager.needsPick(tool)) {
      // TODO: check that
      if (toolToPath.containsKey(tool))
        throw new IllegalStateException("duplicate tool w/o activation");
      toolToPath.put(tool, path);
    }
    if (first)
      slotManager.registerTool(tool);
    LoggingSystem.getLogger(this).info(
        "first=" + first + " tool=" + tool + "   path=" + path);
  }

  /**
   * TODO: sync
   * @param tool
   * @param path
   */
  void removeToolImpl(Tool tool, SceneGraphPath path) {
    boolean last = toolManager.removeTool(tool, path);
    SceneGraphPath activePath = (SceneGraphPath) toolToPath.get(tool);
    if (path.isEqual(activePath)) {
      ToolEvent te = new ToolEvent(this, InputSlot.getDevice("remove"), null,
          null);
      toolContext.setCurrentTool(tool);
      toolContext.setRootToLocal(path);
      toolContext.event = te;
      tool.deactivate(toolContext);
      toolToPath.remove(tool);
    }
    if (last)
      slotManager.unregisterTool(tool);
    LoggingSystem.getLogger(this).info(
        "last=" + last + " tool=" + tool + " path=" + path);
  }

  public SceneGraphPath getEmptyPickPath() {
    return emptyPickPath;
  }

  public void setEmptyPickPath(SceneGraphPath emptyPickPath) {
    if (emptyPickPath != null) {
      if (emptyPickPath.getFirstElement() != viewer.getSceneRoot())
        throw new IllegalArgumentException("empty pick path must start at scene root!");
      this.emptyPickPath = emptyPickPath;
    } else {
      this.emptyPickPath = new SceneGraphPath();
      emptyPickPath.push(viewer.getSceneRoot());
    }
  }

}
