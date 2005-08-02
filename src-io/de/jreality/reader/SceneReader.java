/*
 * Created on Mar 4, 2005
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
package de.jreality.reader;

import java.io.IOException;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;

/**
 * Interface for reading a scene from some sort of Input.
 * 
 */
public interface SceneReader
{
  /**
   * set the input to read the scene from
   * @param input the Input of the resource to read.
   * @throws IOException
   */
  public void setInput(Input input) throws IOException;
  
  /**
   * provides the root component for the scene that was read.
   * @return the root of the read scene.
   */
  public SceneGraphComponent getComponent();
}
