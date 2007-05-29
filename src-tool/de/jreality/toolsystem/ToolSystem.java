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


package de.jreality.toolsystem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.pick.PickSystem;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.AnimatorTool;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;
import de.jreality.util.RenderTrigger;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;

/**
 * 
 * TODO: document this
 * 
 * @author weissman
 *  
 */
public class ToolSystem implements ToolEventReceiver {

    static WeakHashMap<Viewer, ToolSystem> globalTable = new WeakHashMap<Viewer, ToolSystem>();
    
	/**
	 * If <i>v</i> has a tool system already associated to it, return it. Otherwise allocate a default one
	 * @param v
	 * @return
	 */
    public static ToolSystem toolSystemForViewer(Viewer v)	{
		
		ToolSystem sm = (ToolSystem) globalTable.get(v);
		if (sm != null) return sm;
		LoggingSystem.getLogger(ToolSystem.class).warning("Viewer has no tool system, allocating default");
		sm = new ToolSystem(v, null, null);
		globalTable.put(v,sm);
		return sm;
	}
	
	/**
	 * This method just looks up and returns the possibly null toolsystem associated to viewer
	 * @param v
	 * @return
	 */
	 public static ToolSystem getToolSystemForViewer(Viewer v)	{
		
		ToolSystem sm = (ToolSystem) globalTable.get(v);
		return sm;
	}
	
	public static void setToolSystemForViewer(Viewer v, ToolSystem ts)	{
		
		ToolSystem sm = (ToolSystem) globalTable.get(v);
		if (sm != null) throw new IllegalStateException("Viewer already has tool system "+sm);
		globalTable.put(v,ts);
	}
	
	private RenderTrigger renderTrigger;

	protected final LinkedList<ToolEvent> compQueue = new LinkedList<ToolEvent>();

	private final LinkedList<ToolEvent> triggerQueue = new LinkedList<ToolEvent>();
	private final HashMap<Tool, List<SceneGraphPath>> toolToPath = new HashMap<Tool, List<SceneGraphPath>>();
	private List<PickResult> pickResults = Collections.emptyList();

	private SceneGraphPath emptyPickPath=new SceneGraphPath();

	protected Viewer viewer;
	private ToolContextImpl toolContext;
	protected DeviceManager deviceManager;
	private ToolManager toolManager;
	private SlotManager slotManager;
	private PickSystem pickSystem;
	private ToolUpdateProxy updater;
	private ToolEventQueue eventQueue;
	ToolSystemConfiguration config;

	protected boolean executing;

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

		private void setRootToLocal(SceneGraphPath rootToLocal) {
			this.rootToLocal = rootToLocal;
			rootToToolComponent = null;
		}

		public SceneGraphPath getRootToLocal() {
			return rootToLocal;
		}

		public SceneGraphPath getRootToToolComponent() {
			if (rootToToolComponent == null) {
				LinkedList<SceneGraphNode> list = new LinkedList<SceneGraphNode>();
				Iterator i = rootToLocal.reverseIterator();
				for (; i.hasNext();) {
					SceneGraphNode cp = (SceneGraphNode) i.next();
					if (!(cp instanceof SceneGraphComponent))
						continue;
					if (((SceneGraphComponent) cp).getTools().contains(currentTool)) {
						list.addFirst(cp);
						while (i.hasNext())
							list.addFirst((SceneGraphNode) i.next());
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

		public void reject() {
			rejected=true;
		}

		boolean isRejected() {
			return rejected;
		}

		public SceneGraphPath getAvatarPath() {
			return ToolSystem.this.getAvatarPath();
		}

		public PickSystem getPickSystem() {
			return ToolSystem.this.getPickSystem();
		}

		/**
		 * @deprecated why is this method here?
		 */
		public Iterator getSelection() {
			// TODO Auto-generated method stub
			return null;
		}

		public Object getKey() {
			return ToolSystem.this;
		}
	};

	private static ToolSystemConfiguration loadConfiguration() {
	    ToolSystemConfiguration config;
	    try {
	      String toolFile = Secure.getProperty(SystemProperties.TOOL_CONFIG_FILE);
	      config = ToolSystemConfiguration.loadConfiguration(
	          Input.getInput(toolFile)
	      );
	      LoggingSystem.getLogger(ToolSystem.class).config("Using toolconfig="+toolFile);
	    } catch (Exception e1) {
	      config = ToolSystemConfiguration.loadDefaultConfiguration();
	    }
	    return config;
	  }
	/**
	 * 
	 * @param viewer the viewer
	 * @param config the config
	 * @param renderTrigger a rendertrigger to synch or null - the ToolSystem does not take care of
	 * setting/removing the triggers viewer and scene root (on initialize/dispose)
	 */
	public ToolSystem(Viewer viewer, ToolSystemConfiguration config, RenderTrigger renderTrigger) {
		toolContext = new ToolContextImpl();
		toolManager = new ToolManager();
		eventQueue = new ToolEventQueue(this);
		if (config == null) config = loadConfiguration();
		this.config = config;
		this.viewer = viewer;
		deviceManager = new DeviceManager(config, eventQueue, viewer);
		slotManager = new SlotManager(config);
		updater = new ToolUpdateProxy(this);
		this.renderTrigger = renderTrigger;
		// this code moved over from the ToolSystemViewer constructor
	    setPickSystem(new AABBPickSystem());
	    // provide a reasonable default empty pick path
	    emptyPickPath = new SceneGraphPath();
	    emptyPickPath.push(viewer.getSceneRoot());
	}

	Thread getThread() {
		return eventQueue.getThread();
	}

	private boolean initialized;
	public void initializeSceneTools() {
		if (initialized) {
			LoggingSystem.getLogger(this).warning("already initialized!");
			return;
		}
		initialized=true;
		toolManager.cleanUp();
		updater.setSceneRoot(viewer.getSceneRoot());
		// register animator
		SceneGraphPath rootPath = new SceneGraphPath();
		rootPath.push(viewer.getSceneRoot());
		addTool(AnimatorTool.getInstanceImpl(this), rootPath);
		if (emptyPickPath.getLength() == 0) {
			emptyPickPath.push(viewer.getSceneRoot());
		}
		if (pickSystem != null) pickSystem.setSceneRoot(viewer.getSceneRoot());
		eventQueue.start();
	    System.err.println("initializing tool system");
	}

	long renderInterval=20;
	long lastT = System.currentTimeMillis();

	List<ToolEvent> l;

	public void processToolEvent(ToolEvent event) {
		synchronized (mutex) {
			executing=true;
//			if (event.getInputSlot() == InputSlot.getDevice("SystemTime")) {
//			int dt = (int) (System.currentTimeMillis()-event.getTimeStamp());
//			if (dt > 1) {
//			event.axis=new AxisState(event.getAxisState().intValue()+dt);
//			}
//			}
		}
		compQueue.add(event);
		int itarCnt=0;
		do {
			itarCnt++;
			processComputationalQueue();
			processTriggerQueue();
			l = deviceManager.updateImplicitDevices();
			if (l.isEmpty()) break;
			compQueue.addAll(l);
			if (itarCnt > 5000) throw new IllegalStateException("recursion in tool system!");
		} while (true);
		// handle newly added/removed tools
		synchronized (mutex) {
			if (!toolsChanging.isEmpty()) {
				final List<Pair> l = new LinkedList<Pair>(toolsChanging);
				toolsChanging.clear();
				for (Iterator<Pair> i = l.iterator(); i.hasNext(); ) {
					Pair p = i.next();
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
		if (renderTrigger != null && event.getInputSlot() == InputSlot.getDevice("SystemTime")) {
			renderTrigger.finishCollect();
			renderTrigger.startCollect();
		}
	}

	protected void processComputationalQueue() {
		while (!compQueue.isEmpty()) {
			ToolEvent event = (ToolEvent) compQueue.removeFirst();
			deviceManager.evaluateEvent(event, compQueue);
			if (isTrigger(event) && !event.isConsumed()) triggerQueue.add(event);
		}
	}

	private boolean isTrigger(ToolEvent event) {
		InputSlot slot = event.getInputSlot();
		boolean ret = slotManager.isActiveSlot(slot) || slotManager.isActivationSlot(slot);
		return ret;
	}

	protected void processTriggerQueue() {
		if (triggerQueue.isEmpty())	return;

		HashSet<Tool> activatedTools = new HashSet<Tool>();
		HashSet<Tool> deactivatedTools = new HashSet<Tool>();
		HashSet<Tool> stillActiveTools = new HashSet<Tool>();

		SceneGraphPath pickPath = null;

		for (Iterator iter = triggerQueue.iterator(); iter.hasNext();) {
			ToolEvent event = (ToolEvent) iter.next();
			toolContext.event = event;
			InputSlot slot = event.getInputSlot();
			toolContext.sourceSlot = slot;

			AxisState axis = deviceManager.getAxisState(slot);

			boolean noTrigger = true;

			if (axis != null && axis.isPressed()) { // possible activation:

				Set<Tool> candidatesForPick = new HashSet<Tool>(slotManager.getToolsActivatedBySlot(slot));

				Set<Tool> candidates = new HashSet<Tool>();

				// TODO: see if activating more than one Tool for an axis
				// makes sense...
				for (Tool candidate : candidatesForPick) {
					if (!toolManager.needsPick(candidate)) throw new Error();
				}
				if (!candidatesForPick.isEmpty()) {
					// now we need a pick path
					if (pickPath == null)
						pickPath = calculatePickPath();
					int level = pickPath.getLength();
					do {
						Collection<Tool> selection = toolManager.selectToolsForPath(pickPath, level--, candidatesForPick);
						if (selection.isEmpty()) continue;
						LoggingSystem.getLogger(this).finer("selected pick tools:" + selection);
						for (Tool tool : selection)
							registerActivePathForTool(pickPath, tool);

						candidates.addAll(selection);
						// now all Tools in the candidates list need to be
						// processed=activated
						activateToolSet(candidates);
					} while (candidates.isEmpty() && level > 0);
					activatedTools.addAll(candidates);
					noTrigger = candidates.isEmpty();
				}
			}
			if (axis != null && axis.isReleased()) { // possible deactivation
				Set<Tool> deactivated = findDeactivatedTools(slot);
				deactivatedTools.addAll(deactivated);
				deactivateToolSet(deactivated);
				noTrigger = deactivated.isEmpty();
			}

			// process all active tools NEW: only if no tool was (de)activated
			if (noTrigger) {  //activatedTools.isEmpty() && deactivatedTools.isEmpty()
				Set<Tool> active = slotManager.getActiveToolsForSlot(slot);
				stillActiveTools.addAll(active);
				processToolSet(active);
			}
		}
		triggerQueue.clear();
		// NEW: this is now obsolete
		// // don't update used slots for deactivated tools!
		// stillActiveTools.removeAll(deactivatedTools);
		slotManager.updateMaps(stillActiveTools, activatedTools, deactivatedTools);
	}

	private void registerActivePathForTool(SceneGraphPath pickPath, Tool tool) {
		toolToPath.put(tool, Collections.singletonList(pickPath));
	}

	private double[] pointerTrafo = new double[16];

	private double[] currentPointer = new double[16];

	protected final Object mutex=new Object();

	private SceneGraphPath avatarPath;

	private void performPick() {
		if (pickSystem == null) {
			pickResults = Collections.emptyList();
			return;
		}
		currentPointer = deviceManager.getTransformationMatrix(
				InputSlot.getDevice("PointerTransformation")).toDoubleArray(
						currentPointer);
		//if (Rn.equals(pointerTrafo, currentPointer)) return;
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
			toolContext.event.device=slotManager.resolveSlotForTool(tool, toolContext.sourceSlot);
			if (toolContext.event.device == null) {
				LoggingSystem.getLogger(this).warning("activate: resolving "+toolContext.sourceSlot+" failed: "+tool.getClass().getName());
			}
			List paths = getActivePathsForTool(tool);
			for (Iterator it2 = paths.iterator(); it2.hasNext(); ) {
				toolContext.setRootToLocal((SceneGraphPath) it2.next());
				tool.activate(toolContext);
				if (toolContext.isRejected()) {
					iter.remove();
					toolContext.rejected=false;
				}
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
			toolContext.event.device=slotManager.resolveSlotForTool(tool, toolContext.sourceSlot);
			List paths = getActivePathsForTool(tool);
			for (Iterator it2=paths.iterator(); it2.hasNext(); ) {
				toolContext.setRootToLocal((SceneGraphPath) it2.next());
				tool.perform(toolContext);
			}
		}
	}

	private List getActivePathsForTool(Tool tool) {
		List l = (List) toolToPath.get(tool);
		return l == null ? Collections.EMPTY_LIST : l;
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
			toolContext.event.device=slotManager.resolveSlotForTool(tool, toolContext.sourceSlot);
			if (toolContext.event.device == null) {
				LoggingSystem.getLogger(this).warning("deavtivate: resolving "+toolContext.sourceSlot+" failed: "+tool.getClass().getName());
			}
			List paths = getActivePathsForTool(tool);
			for (Iterator it2=paths.iterator(); it2.hasNext(); ) {
				toolContext.setRootToLocal((SceneGraphPath) it2.next());
				tool.deactivate(toolContext);
			}
		}
	}

	/**
	 * the given slot must have AxisState.isReleased() == true
	 * this is garanteed in processTrigger...
	 * 
	 * @param slot
	 * @return
	 */
	private Set<Tool> findDeactivatedTools(InputSlot slot) {
		return slotManager.getToolsDeactivatedBySlot(slot);
	}

	public void setPickSystem(PickSystem pickSystem) {
		this.pickSystem = pickSystem;
		if (pickSystem != null) pickSystem.setSceneRoot(viewer.getSceneRoot());
	}

	public PickSystem getPickSystem() {
		return pickSystem;
	}

	public void setAvatarPath(SceneGraphPath p) {
		avatarPath=p;
		deviceManager.setAvatarPath(avatarPath);
	}

	public SceneGraphPath getAvatarPath() {
		return avatarPath != null ? avatarPath : viewer.getCameraPath();
	}

	public void dispose() {
		eventQueue.dispose();
		deviceManager.dispose();
		updater.dispose();
	}

	final List<Pair> toolsChanging = new LinkedList<Pair>();

	protected static class Pair {
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
			List<SceneGraphPath> l = toolToPath.get(tool);
			if (l == null) {
				l = new LinkedList<SceneGraphPath>();
				toolToPath.put(tool, l);
			}
			try {
				l.add(path);
			} catch (UnsupportedOperationException e) {
				System.out.println("try adding to sigleton: "+tool);
			}
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
		for (Iterator i = getActivePathsForTool(tool).iterator(); i.hasNext(); ) {
			SceneGraphPath activePath = (SceneGraphPath) i.next();
			if (path.isEqual(activePath)) {
				ToolEvent te = new ToolEvent(this, InputSlot.getDevice("remove"), null,
						null);
				toolContext.setCurrentTool(tool);
				toolContext.setRootToLocal(path);
				toolContext.event = te;
				tool.deactivate(toolContext);
				toolToPath.remove(tool);
			}
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
			if (emptyPickPath.getFirstElement().getName() != viewer.getSceneRoot().getName())
				throw new IllegalArgumentException("empty pick path must start at scene root!");
			if (emptyPickPath.getFirstElement() != viewer.getSceneRoot()) {
				LoggingSystem.getLogger(this).warning("Strange situation: same names but different scene roots");
			}
			this.emptyPickPath = emptyPickPath;
		} else {
			this.emptyPickPath = new SceneGraphPath();
			emptyPickPath.push(viewer.getSceneRoot());
		}
	}

}
