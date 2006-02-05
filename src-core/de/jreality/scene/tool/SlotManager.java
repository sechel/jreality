/*
 * Created on Apr 9, 2005
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

import de.jreality.scene.tool.config.ToolSystemConfiguration;
import de.jreality.scene.tool.config.VirtualMapping;
import de.jreality.util.LoggingSystem;

/**
 * 
 * TODO: document this
 * 
 * Essentially, this class maps tools to slots.
 * 
 * @author weissman
 *  
 */
public class SlotManager {

    /**
     * up-to-date map of (used) slots to activatable Tools
     */
    private final HashMap slot2activation = new HashMap();
    /**
     * up-to-date map of (used) slots to active Tools
     */
    private final HashMap slot2active = new HashMap();
    /**
     * up-to-date map of (used) slots to active Tools
     */
    private final HashMap tool2currentSlots = new HashMap();

    /**
     * up-to-date map of deactivation-slots to active Tools
     */
    private final HashMap slot2deactivation = new HashMap();
    
    private final HashMap virtualMappings = new HashMap();
    private final HashMap virtualMappingsInv = new HashMap();
    
    private final HashMap slotsToMappingsForTool = new HashMap();
    
    private final HashMap virtualSlotsForTool = new HashMap();

    SlotManager(ToolSystemConfiguration config) {
      List virtualMappings = config.getVirtualMappings();
      for (Iterator i = virtualMappings.iterator(); i.hasNext(); ) {
        VirtualMapping vm = (VirtualMapping) i.next();
        getMappingsSourceToTargets(vm.getSourceSlot()).add(vm.getTargetSlot());
        getMappingsTargetToSources(vm.getTargetSlot()).add(vm.getSourceSlot());
      }
    }
    
    private Set getMappingsSourceToTargets(InputSlot slot) {
      if (!virtualMappings.containsKey(slot))
        virtualMappings.put(slot, new HashSet());
      return (Set) virtualMappings.get(slot);
    }
    
    private Set getMappingsTargetToSources(InputSlot slot) {
      if (!virtualMappingsInv.containsKey(slot))
        virtualMappingsInv.put(slot, new HashSet());
      return (Set) virtualMappingsInv.get(slot);
    }

    /**
     * returns a map that maps "raw" slots to slotnames for each tool
     * - this map is up to date:
     * * contains activation slots if tool is inactive
     * * contains activation slots + current slots if tool is activated
     * * contains current slots if tool is always active
     * @param tool 
     * @return the map described above
     */
    private Map getMappingsForTool(Tool tool) {
      if (!slotsToMappingsForTool.containsKey(tool))
        slotsToMappingsForTool.put(tool, new HashMap());
      return (Map) slotsToMappingsForTool.get(tool);
    }

    Set getToolsActivatedBySlot(InputSlot slot) {
        return Collections.unmodifiableSet(getSlot2activation(slot));
    }

    private Set getSlot2activation(InputSlot slot) {
        if (!slot2activation.containsKey(slot))
                slot2activation.put(slot, new HashSet());
        return (Set) slot2activation.get(slot);
    }

    Set getToolsDeactivatedBySlot(InputSlot slot) {
        return Collections.unmodifiableSet(getSlot2deactivation(slot));
    }

    private Set getSlot2deactivation(InputSlot slot) {
        if (!slot2deactivation.containsKey(slot))
                slot2deactivation.put(slot, new HashSet());
        return (Set) slot2deactivation.get(slot);
    }

    Set getActiveToolsForSlot(InputSlot slot) {
        return Collections.unmodifiableSet(getSlot2active(slot));
    }

    private Set getSlot2active(InputSlot slot) {
        if (!slot2active.containsKey(slot))
                slot2active.put(slot, new HashSet());
        return (Set) slot2active.get(slot);
    }

    private Set getTool2currentSlots(Tool tool) {
        if (!tool2currentSlots.containsKey(tool))
            tool2currentSlots.put(tool, new HashSet());
    return (Set) tool2currentSlots.get(tool);
    }

    boolean isActiveSlot(InputSlot slot) {
        return slot2active.containsKey(slot);
    }

    boolean isActivationSlot(InputSlot slot) {
        return slot2activation.containsKey(slot);
    }

    private Set getVirtualSlotsForTool(Tool tool) {
      if (!virtualSlotsForTool.containsKey(tool))
        virtualSlotsForTool.put(tool, new HashSet());
      return (Set) virtualSlotsForTool.get(tool);
  }

    /**
     * returns the original (trigger) slots for the given slot
     * @param slot
     * @return
     */
    Set resolveSlot(InputSlot slot) {
      Set ret = new HashSet();
      findTriggerSlots(ret, slot);
      return ret;
    }
    private void findTriggerSlots(Set l, InputSlot slot) {
      Set sources = getMappingsTargetToSources(slot);
      //Set sources = getMappingsSourceToTargets(slot);
      if (sources.isEmpty()) {
        l.add(slot);
        return;
      }
      for (Iterator i = sources.iterator(); i.hasNext(); )
        findTriggerSlots(l, (InputSlot) i.next());
    }
    private Set resolveSlots(List slotSet) {
      Set ret = new HashSet();
      for (Iterator i = slotSet.iterator(); i.hasNext(); )
        findTriggerSlots(ret, (InputSlot) i.next());
      return ret;
    }

    /**
     * updates the maps for the current tool system state
     * 
     * @param activeTools tools that are still active
     * @param activatedTools tools activated recently
     * @param deactivatedTools tools deactivated recently
     */
    void updateMaps(final Set activeTools, final Set activatedTools, final Set deactivatedTools) {
        // handle newly activated tools
        for (Iterator i = activatedTools.iterator(); i.hasNext();) {
            final Tool tool = (Tool) i.next();
            // update slot2active
            for (Iterator i2 = tool.getCurrentSlots().iterator(); i2.hasNext();) {
                final InputSlot slot = (InputSlot) i2.next();
                Set resolvedSlots = resolveSlot(slot);
                for (Iterator i3 = resolvedSlots.iterator(); i3.hasNext(); ) {
                  InputSlot resolvedSlot = (InputSlot) i3.next();
                  getSlot2active(resolvedSlot).add(tool);
                  getMappingsForTool(tool).put(resolvedSlot, slot);
                }
            }
            
            // remember all currently used slots for activated tool
            Set currentSlots = resolveSlots(tool.getCurrentSlots());
            getTool2currentSlots(tool).addAll(currentSlots);
            getVirtualSlotsForTool(tool).addAll(tool.getCurrentSlots());

            // update slot2activation
            for (Iterator i2 = tool.getActivationSlots().iterator(); i2.hasNext();) {
                final InputSlot slot = (InputSlot) i2.next();
                final Set resolvedSlots = resolveSlot(slot);
                for (Iterator i3 = resolvedSlots.iterator(); i3.hasNext(); ) {
                  InputSlot resolvedSlot = (InputSlot) i3.next();
                  getSlot2activation(resolvedSlot).remove(tool);
                  getSlot2deactivation(resolvedSlot).add(tool);
                }
            }
        }

        // handle newly deactivated tools
        for (Iterator i = deactivatedTools.iterator(); i.hasNext();) {
            final Tool tool = (Tool) i.next();
            // update slot2active
            for (Iterator i2 = tool.getCurrentSlots().iterator(); i2.hasNext();) {
              final InputSlot slot = (InputSlot) i2.next();
              Set resolvedSlots = resolveSlot(slot);
              for (Iterator i3 = resolvedSlots.iterator(); i3.hasNext(); ) {
                InputSlot resolvedSlot = (InputSlot) i3.next();
                getSlot2active(resolvedSlot).remove(tool);
                getMappingsForTool(tool).remove(resolvedSlot);
              }
            }
            // update slot2activation
            for (Iterator i2 = tool.getActivationSlots().iterator(); i2.hasNext();) {
              final InputSlot slot = (InputSlot) i2.next();
              final Set resolvedSlots = resolveSlot(slot);
              for (Iterator i3 = resolvedSlots.iterator(); i3.hasNext(); ) {
                InputSlot resolvedSlot = (InputSlot) i3.next();
                getSlot2activation(resolvedSlot).add(tool);
                getSlot2deactivation(resolvedSlot).remove(tool);
              }
            }
            
            getVirtualSlotsForTool(tool).clear();
            getVirtualSlotsForTool(tool).addAll(tool.getActivationSlots());
        }
        
        //update used slots for still active tools
        for (Iterator i = activeTools.iterator(); i.hasNext(); ) {
            Tool tool = (Tool) i.next();
            Set newUsed = resolveSlots(tool.getCurrentSlots());
            Set oldUsed = getTool2currentSlots(tool);
            // contains all newly used slots for the tool
            Set added = new HashSet(newUsed);
            added.removeAll(oldUsed);
            // contains all no-longer-used slots for the tool
            Set removed = new HashSet(oldUsed);
            removed.removeAll(newUsed);
            for (Iterator i2 = added.iterator(); i2.hasNext(); ) {
                InputSlot slot = (InputSlot) i2.next();
                getSlot2active(slot).add(tool);
            }
            for (Iterator i2 = removed.iterator(); i2.hasNext(); ) {
                InputSlot slot = (InputSlot) i2.next();
                getSlot2active(slot).remove(tool);
            }
            getTool2currentSlots(tool).removeAll(removed);
            getTool2currentSlots(tool).addAll(added);
            
            Set oldUsedVirtual = new HashSet(getVirtualSlotsForTool(tool));

            Set newUsedVirtual = new HashSet();
            newUsedVirtual.addAll(tool.getActivationSlots());
            newUsedVirtual.addAll(tool.getCurrentSlots());
            
            oldUsedVirtual.removeAll(newUsedVirtual); // these are no longer used
            
            newUsedVirtual.removeAll(getVirtualSlotsForTool(tool)); // these are new used
            
            // update the map
            getVirtualSlotsForTool(tool).removeAll(oldUsedVirtual);
            getVirtualSlotsForTool(tool).addAll(newUsedVirtual);
            
            for (Iterator j = getMappingsForTool(tool).values().iterator(); j.hasNext(); ) {
              if (oldUsedVirtual.contains(j.next())) j.remove();
            }
//            for (Iterator j = removed.iterator(); j.hasNext(); ) {
//              InputSlot slot = (InputSlot) j.next();
//              Set origSlots = resolveSlot(slot);
//              for (Iterator k = origSlots.iterator(); k.hasNext(); )
//                getMappingsForTool(tool).remove(k.next());
//            }

            for (Iterator j = newUsedVirtual.iterator(); j.hasNext(); ) {
              InputSlot newSlot = (InputSlot) j.next();
              Set sourceSlots = resolveSlot(newSlot);
              for (Iterator k = sourceSlots.iterator(); k.hasNext(); )
                getMappingsForTool(tool).put(k.next(), newSlot);
            }
            
        }
    }

    /**
     * @param tool
     */
  void registerTool(Tool tool) {
    System.out.println("register tool"+tool);
    List activationSlots = tool.getActivationSlots();
    if (activationSlots.isEmpty()) {
      // permanently active tool
      getVirtualSlotsForTool(tool).addAll(tool.getCurrentSlots());
      for (Iterator i = tool.getCurrentSlots().iterator(); i.hasNext();) {
        InputSlot mappedSlot = (InputSlot) i.next();
        Set resolvedSlots = resolveSlot(mappedSlot);
        getTool2currentSlots(tool).addAll(resolvedSlots);
        for (Iterator i2 = resolvedSlots.iterator(); i2.hasNext();) {
          InputSlot resolvedSlot = (InputSlot) i2.next();
          getMappingsForTool(tool).put(resolvedSlot, mappedSlot);
          getSlot2active(resolvedSlot).add(tool);
        }
      }
    } else {
      getVirtualSlotsForTool(tool).addAll(tool.getActivationSlots());
      for (Iterator i = tool.getActivationSlots().iterator(); i.hasNext();) {
        InputSlot mappedSlot = (InputSlot) i.next();
        Set resolvedSlots = resolveSlot(mappedSlot);
        for (Iterator i2 = resolvedSlots.iterator(); i2.hasNext();) {
          InputSlot resolvedSlot = (InputSlot) i2.next();
          getMappingsForTool(tool).put(resolvedSlot, mappedSlot);
          getSlot2activation(resolvedSlot).add(tool);
        }
      }
    }
    LoggingSystem.getLogger(this).info(
        "registered Tool " + tool.getClass().getName());
  }

  void unregisterTool(Tool tool) {
    List activationSlots = tool.getActivationSlots();
    if (activationSlots.isEmpty()) {
      // permanently active tool
      for (Iterator i = tool.getCurrentSlots().iterator(); i.hasNext();) {
        InputSlot mappedSlot = (InputSlot) i.next();
        Set resolvedSlots = resolveSlot(mappedSlot);
        for (Iterator i2 = resolvedSlots.iterator(); i2.hasNext();) {
          InputSlot resolvedSlot = (InputSlot) i2.next();
          getSlot2active(resolvedSlot).remove(tool);
        }
      }
    } else {
      for (Iterator i = tool.getActivationSlots().iterator(); i.hasNext();) {
        InputSlot mappedSlot = (InputSlot) i.next();
        Set resolvedSlots = resolveSlot(mappedSlot);
        for (Iterator i2 = resolvedSlots.iterator(); i2.hasNext();) {
          InputSlot resolvedSlot = (InputSlot) i2.next();
          getSlot2activation(resolvedSlot).remove(tool);
        }
      }
    }
    getMappingsForTool(tool).clear();
    getTool2currentSlots(tool).clear();
    getVirtualSlotsForTool(tool).clear();
    LoggingSystem.getLogger(this).info(
        "unregistered Tool " + tool.getClass().getName());
  }

    /**
     * returns the name under which the given slot is expected by the tool
     * 
     * @param tool
     * @param sourceSlot
     * @return the sourceSlot or the mapped slot (which is handled by the tool)
     */
    InputSlot resolveSlotForTool(Tool tool, InputSlot sourceSlot) {
      return (InputSlot) getMappingsForTool(tool).get(sourceSlot);
    }
}
