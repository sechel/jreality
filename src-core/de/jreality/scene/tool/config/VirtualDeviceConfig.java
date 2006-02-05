/*
 * Created on Apr 30, 2005
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
package de.jreality.scene.tool.config;

import java.beans.DefaultPersistenceDelegate;
import java.beans.PersistenceDelegate;
import java.util.List;
import java.util.Map;

import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.VirtualDevice;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class VirtualDeviceConfig {
  
  static final PersistenceDelegate DELEGATE = new DefaultPersistenceDelegate(
      new String[]{"virtualDevice", "outSlot", "inSlots", "config"});

  private Class virtualDevice;
  
  public Map getConfig() {
    return config;
  }
  public List getInSlots() {
    return inSlots;
  }
  public InputSlot getOutSlot() {
    return outSlot;
  }
  public Class getVirtualDevice() {
    return virtualDevice;
  }
  
  private InputSlot outSlot;
  private List inSlots;
  private Map config;
  
  public VirtualDeviceConfig(Class virtualDevice, InputSlot outSlot, List inSlots, Map config) {
    this.virtualDevice = virtualDevice;
    this.outSlot = outSlot;
    this.inSlots = inSlots;
    this.config = config;
  }

  public VirtualDevice createDevice() throws InstantiationException, IllegalAccessException {
    VirtualDevice ret = (VirtualDevice) virtualDevice.newInstance();
    ret.initialize(inSlots, outSlot, config);
    return ret;
  }
  
  public String toString() {
    return "VirtualDeviceConfig: "+(virtualDevice != null ? virtualDevice.getName() : "null") +" outSlot="+outSlot+ " inslots="+inSlots.toString()+" config="+config.toString();
  }
}
