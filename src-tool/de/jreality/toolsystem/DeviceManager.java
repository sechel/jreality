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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.config.RawDeviceConfig;
import de.jreality.toolsystem.config.RawMapping;
import de.jreality.toolsystem.config.ToolSystemConfiguration;
import de.jreality.toolsystem.config.VirtualConstant;
import de.jreality.toolsystem.config.VirtualDeviceConfig;
import de.jreality.toolsystem.config.VirtualMapping;
import de.jreality.toolsystem.raw.RawDevice;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoggingSystem;


/**
 *
 * TODO: document this
 *
 * @author weissman
 *
 */
class DeviceManager {
  
  Viewer viewer;
  
  /**
   * contains a up-to-date map of (used) slots to used virtual devices
   */
  private final HashMap slot2virtual = new HashMap();
  
  /**
   * contains the current axis states
   * (used for VirtualDevice-/Tool-Context)
   */
  private final HashMap slot2axis = new HashMap();
  
  /**
   * contains the current transformations
   * (used for VirtualDevice-/Tool-Context)
   */
  private final HashMap slot2transformation = new HashMap();
  
  private HashMap slots2virtualMappings = new HashMap();
  
  private VirtualDeviceContextImpl virtualDeviceContext = new VirtualDeviceContextImpl();
  
  private class VirtualDeviceContextImpl implements VirtualDeviceContext {

    ToolEvent event;
    
    public AxisState getAxisState(InputSlot slot) throws MissingSlotException {
      AxisState axisState = DeviceManager.this.getAxisState(slot);
      if (axisState == null) throw new MissingSlotException(slot);
      return axisState;
    }

    public DoubleArray getTransformationMatrix(InputSlot slot) throws MissingSlotException {
      DoubleArray transformationMatrix = DeviceManager.this.getTransformationMatrix(slot);
      if (transformationMatrix == null) throw new MissingSlotException(slot);
      return transformationMatrix;
    }
    
    public ToolEvent getEvent() {
      return event;
    }
    private void setEvent(ToolEvent event) {
      this.event = event;
    }
  };
  
  ConfigurationAttributes toolConfig;
  
  // maps raw devices via name. e.g.: Mouse->de.jreality.scene.tool.DeviceMouse
  private Map rawDevices = new HashMap();

  private final ToolEventQueue eventQueue;
  
  private InputSlot avatarSlot = InputSlot.getDevice("AvatarTransformation");
  private InputSlot worldToCamSlot = InputSlot.getDevice("WorldToCamera");
  private InputSlot camToNDCSlot = InputSlot.getDevice("CameraToNDC");
  private double[] avatarTrafo = new Matrix().getArray();
  private double[] worldToCamTrafo = new Matrix().getArray();
  private double[] camToNDCTrafo = new Matrix().getArray();

  // TODO: remove this
  HashSet debugSlots=new HashSet();

  private SceneGraphPath avatarPath;

  DeviceManager(ToolSystemConfiguration config, ToolEventQueue queue, Viewer viewer) {

    debugSlots.add(InputSlot.getDevice("Secondary"));
    debugSlots.add(InputSlot.getDevice("PrimaryAction"));
    debugSlots.add(InputSlot.getDevice("SecondaryAction"));
//    debugSlots.add(InputSlot.getDevice("DragAlongPointer"));
//      debugSlots.add(InputSlot.getDevice("ForwardBackwardAxis"));
//      debugSlots.add(InputSlot.getDevice("LeftRightAxis"));
//      debugSlots.add(InputSlot.getDevice("HorizontalShipRotationEvolution"));
      
    this.viewer=viewer;
    
    eventQueue=queue;
    
    // raw devices
    for (Iterator i = config.getRawConfigs().iterator(); i.hasNext(); ) {
      RawDeviceConfig rdc = (RawDeviceConfig) i.next();
      try {
        RawDevice rd = rdc.createDevice();
        rd.initialize(viewer);
        rd.setEventQueue(eventQueue);
        rawDevices.put(rdc.getDeviceID(), rd);
        LoggingSystem.getLogger(this).config("Started rawdevice "+rd);
      } catch (Exception e) {
        LoggingSystem.getLogger(this).info("Couldn't create RawDevice "+rdc);
      }
    }

    // mapping raw slots -> inputSlots
    for (Iterator i = config.getRawMappings().iterator(); i.hasNext(); ) {
      RawMapping rm = (RawMapping) i.next();
      RawDevice rd = (RawDevice) rawDevices.get(rm.getDeviceID());
      if (rd == null) {
        LoggingSystem.getLogger(this).info("Ignoring mapping "+rm);
      }
      else {
        // map and set initial value for InputSlot
        try {
          ToolEvent initialValue = rd.mapRawDevice(rm.getSourceSlot(), rm.getTargetSlot());
          if (initialValue.getInputSlot() != rm.getTargetSlot()) throw new IllegalStateException("different slot not allowed in init");
          setTransformationMatrix(initialValue.getInputSlot(), initialValue.getTransformation());
          setAxisState(initialValue.getInputSlot(), initialValue.getAxisState());
          LoggingSystem.getLogger(this).config("Mapped "+rm);
        } catch (Exception e) {
          // ignore unknown slots
          LoggingSystem.getLogger(this).config("cannot map slot "+rm);
        }
      }
    }
    
    // virtual constants
    for (Iterator i = config.getVirtualConstants().iterator(); i.hasNext(); ) {
      VirtualConstant vc = (VirtualConstant) i.next();
      setTransformationMatrix(vc.getSlot(), vc.getTransformationMatrix());
      setAxisState(vc.getSlot(), vc.getAxisState());
      LoggingSystem.getLogger(this).config("Created virtual constant: "+vc);
    }
    
    //implicit devices
    setTransformationMatrix(avatarSlot, new DoubleArray(avatarTrafo));
    setTransformationMatrix(worldToCamSlot, new DoubleArray(worldToCamTrafo));
    setTransformationMatrix(camToNDCSlot, new DoubleArray(camToNDCTrafo));
    
    // virtual devices
    for (Iterator i = config.getVirtualConfigs().iterator(); i.hasNext(); ) {
      VirtualDeviceConfig vc = (VirtualDeviceConfig) i.next();
      try {
        VirtualDevice v = vc.createDevice();
        boolean firstSlot = true;
        for (Iterator in = vc.getInSlots().iterator(); in.hasNext(); ) {
          InputSlot currentSlot = (InputSlot) in.next();
          virtualDeviceContext.setEvent(new ToolEvent(this, currentSlot, getAxisState(currentSlot), getTransformationMatrix(currentSlot)));
          
          ToolEvent initialValue = null;
          try {
            initialValue = v.process(virtualDeviceContext);
            if (initialValue == null) initialValue = v.process(virtualDeviceContext);
          } catch (MissingSlotException me) {
            // second try for evolution device
            initialValue = v.process(virtualDeviceContext);
          }
          if (initialValue != null) {
            setTransformationMatrix(initialValue.getInputSlot(), initialValue.getTransformation());
            setAxisState(initialValue.getInputSlot(), initialValue.getAxisState());
            LoggingSystem.getLogger(this).fine(initialValue.toString());
          } else {
            if (firstSlot) throw new IllegalStateException(v+" returned null twice");
          }
          getDevicesForSlot(currentSlot).add(v);
          firstSlot = false;
        }
      } catch (Exception e) {
        LoggingSystem.getLogger(this).info("Virtual device failed: "+vc);
        LoggingSystem.getLogger(this).log(Level.INFO, "error was:", e);
        continue;
      }
      LoggingSystem.getLogger(this).config("Created virtual device: "+vc);
    }
    
    List mappings = config.getVirtualMappings();
    for (Iterator i = mappings.iterator(); i.hasNext(); ) {
      VirtualMapping vm = (VirtualMapping) i.next();
      getMappingsTargetToSources(vm.getTargetSlot()).add(vm.getSourceSlot());
    }
    // set values for virtual mappings
    // NOTE: this is not well defined - from now on always the latest value for
    // a mapping is the resulting value for the mapping..
    for (Iterator i = mappings.iterator(); i.hasNext(); ) {
      VirtualMapping vm = (VirtualMapping) i.next();
      List slots = resolveSlot(vm.getTargetSlot());
      for (Iterator j = slots.iterator(); j.hasNext(); ) {
        InputSlot rawSlot = (InputSlot) j.next();
        getVirtualMappingsForSlot(rawSlot).add(vm.getTargetSlot());
        setTransformationMatrix(vm.getTargetSlot(), getTransformationMatrix(rawSlot));
        setAxisState(vm.getTargetSlot(), getAxisState(rawSlot));
        LoggingSystem.getLogger(this).fine("set value for mapped slot ["+vm.getTargetSlot()+"] from rawslot ["+rawSlot+"] to "+getAxisState(rawSlot)+" || "+getTransformationMatrix(rawSlot));
      }
    }

  }

  /**
   * @param slot
   * @return
   */
  AxisState getAxisState(InputSlot slot) {
    return (AxisState) slot2axis.get(slot);
  }

  /**
   * @param slot
   * @return
   */
  DoubleArray getTransformationMatrix(InputSlot slot) {
    return (DoubleArray) slot2transformation.get(slot);
  }

  /**
   * @param slot
   * @param axisState
   */
  void setAxisState(InputSlot slot, AxisState axisState) {
    slot2axis.put(slot, axisState);
    if (axisState != null && debugSlots.contains(slot)) LoggingSystem.getLogger(this).fine(slot+": "+axisState);
  }

  /**
   * @param slot
   * @param transformation
   */
  void setTransformationMatrix(InputSlot slot, DoubleArray transformation) {
    slot2transformation.put(slot, transformation);
    if (transformation != null && debugSlots.contains(slot)) LoggingSystem.getLogger(this).fine(slot+"\n"+Rn.matrixToString(transformation.toDoubleArray(null)));
  }
  
  Set getDevicesForSlot(InputSlot slot) {
    if (!slot2virtual.containsKey(slot)) slot2virtual.put(slot, new HashSet());
    return (Set) slot2virtual.get(slot);
  }

  List getVirtualMappingsForSlot(InputSlot slot) {
    if (!slots2virtualMappings.containsKey(slot))
      slots2virtualMappings.put(slot, new LinkedList());
    return (List) slots2virtualMappings.get(slot);
  }
  
  /**
   * 
   * iterates over virtual devices that depend on the slot of
   * a given event, posts new tool events generated by those
   * devices to compQueue
   * 
   * @param event the event to evaluate
   * @param compQueue the queue to post new events to
   */
  void evaluateEvent(ToolEvent event, LinkedList compQueue) {
    LoggingSystem.getLogger(this).finest("evaluating event: "+event);
    InputSlot slot = event.getInputSlot();
    setAxisState(slot, event.getAxisState());
    setTransformationMatrix(slot, event.getTransformation());
    for (Iterator i = getVirtualMappingsForSlot(slot).iterator(); i.hasNext(); ) {
      InputSlot mapSlot = (InputSlot) i.next();
      setAxisState(mapSlot, event.getAxisState());
      setTransformationMatrix(mapSlot, event.getTransformation());
    }
    virtualDeviceContext.setEvent(event);
    for(Iterator iter = getDevicesForSlot(slot).iterator(); iter.hasNext();) {
      VirtualDevice device = (VirtualDevice) iter.next();
      try {
        ToolEvent newEvent = device.process(virtualDeviceContext);
        if (newEvent!=null) compQueue.add(newEvent);
      } catch (MissingSlotException mse) {
        LoggingSystem.getLogger(this).log(Level.WARNING, "slot for virtual device missing", mse);
      }
    }
  }

/**
 * update implicit devices, i.e., CameraToWorld and RootToCamera
 * 
 * Note that this method doesn't post any tool events because
 * any tools or virtual devices that depend on the camera
 * won't need this information until they're triggered by
 * some other event.
 * 
 * TODO: viewer.getCameraPath().getInverseMatrix may not always
 * be up to date due to threading issues --- implement some other
 * way of obtaining this matrix, e.g., by adding this functionality
 * to camera utilities
 */
public List updateImplicitDevices() {
	    boolean worldToCamChanged=false, camToNDCChanged=false, avatarChanged = false;
      double[] matrix;
      if (viewer.getCameraPath() != null) {
        matrix = viewer.getCameraPath().getInverseMatrix(null);
  	    if (!Rn.equals(matrix, worldToCamTrafo)) {
	          Rn.copy(worldToCamTrafo, matrix);
	          worldToCamChanged = true;
	      }
        double asp=viewer.getViewingComponentSize().getWidth()/viewer.getViewingComponentSize().getHeight();
        if (Double.isNaN(asp)) matrix = Rn.identityMatrix(4);
        else matrix = CameraUtility.getCameraToNDC((Camera) viewer.getCameraPath().getLastElement(), asp);
        if (!Rn.equals(matrix, camToNDCTrafo)) {
            Rn.copy(camToNDCTrafo, matrix);
            camToNDCChanged = true;
        }
      }
      if (avatarPath != null) matrix = avatarPath.getMatrix(null);
      else matrix = viewer.getCameraPath().getMatrix(null);
      if (!Rn.equals(matrix, avatarTrafo)) {
          Rn.copy(avatarTrafo, matrix);
          avatarChanged = true;
      }
	    if (!worldToCamChanged && !camToNDCChanged && !avatarChanged) return Collections.EMPTY_LIST;
	    List ret = new LinkedList();
	    if (worldToCamChanged) ret.add(new ToolEvent(this, worldToCamSlot, new DoubleArray(worldToCamTrafo)));
      if (camToNDCChanged) ret.add(new ToolEvent(this, camToNDCSlot, new DoubleArray(camToNDCTrafo)));
      if (avatarChanged) ret.add(new ToolEvent(this, avatarSlot, new DoubleArray(avatarTrafo)));
	    return ret;
	}

  void setAvatarPath(SceneGraphPath p) {
    avatarPath = p;
  }

  /*************** COPY AND PASTE FROM SLOTMANAGER *************/
  // TODO!!!!!!!!!!!!!!!!!!!!!!!
  
  /**
   * returns the original (trigger) slots for the given slot
   * @param slot
   * @return
   */
  List resolveSlot(InputSlot slot) {
    List ret = new LinkedList();
    findTriggerSlots(ret, slot);
    return ret;
  }
  private void findTriggerSlots(List l, InputSlot slot) {
    Set sources = getMappingsTargetToSources(slot);
    if (sources.isEmpty()) {
      l.add(slot);
      return;
    }
    for (Iterator i = sources.iterator(); i.hasNext(); )
      findTriggerSlots(l, (InputSlot) i.next());
  }

  private Set getMappingsTargetToSources(InputSlot slot) {
    if (!virtualMappingsInv.containsKey(slot))
      virtualMappingsInv.put(slot, new HashSet());
    return (Set) virtualMappingsInv.get(slot);
  }
  
  private final HashMap virtualMappingsInv = new HashMap();

  public void dispose() {
    for (Iterator rawDevs = rawDevices.keySet().iterator(); rawDevs.hasNext(); ) {
      String devName = (String) rawDevs.next();
      RawDevice dev = (RawDevice) rawDevices.get(devName);
      LoggingSystem.getLogger(this).fine("disposing raw device ["+devName+"]"+dev);
      dev.dispose();
    }
    slot2axis.clear();
    slot2transformation.clear();
    slot2virtual.clear();
    slots2virtualMappings.clear();
    avatarPath = null;
    avatarTrafo=new Matrix().getArray();
    camToNDCTrafo=new Matrix().getArray();
    worldToCamTrafo=new Matrix().getArray();
  }

}


