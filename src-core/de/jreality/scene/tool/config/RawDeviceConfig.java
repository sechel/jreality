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

import de.jreality.scene.tool.RawDevice;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class RawDeviceConfig {

  public static final PersistenceDelegate DELEGATE = new DefaultPersistenceDelegate(
      new String[]{"rawDevice", "deviceID"}
  );
  
  private final String deviceID;
  private final String rawDevice;
  
  public RawDeviceConfig(String type, String deviceID) {
    this.deviceID=deviceID;
    this.rawDevice = type;
  }
  
  public String getRawDevice() {
    return rawDevice;
  }
  
  public String getDeviceID() {
    return deviceID;
  }
  
  public String toString() {
    return deviceID + "["+rawDevice+"]";
  }
  
  public RawDevice createDevice() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    RawDevice dev = (RawDevice) Class.forName(rawDevice).newInstance();
    return dev;
  }
}
