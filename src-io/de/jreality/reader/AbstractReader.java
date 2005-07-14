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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import de.jreality.scene.SceneGraphComponent;

/**
 * Abstract class for a SceneReader. Implement setInput so that
 * the Input is processed and the component assigned to root.
 */
public abstract class AbstractReader implements SceneReader
{
  protected SceneGraphComponent root;
  protected Input input;

  public void setInput(Input input) throws IOException
  {
    this.input=input;
  }

  public SceneGraphComponent getComponent()
  {
    return root;
  }

  /**
   * convenience method for reading from an Input.
   * 
   * @param input the input
   * @return the root component of the scenegraph created from the input
   * @throws IOException
   */
  public SceneGraphComponent read(Input input) throws IOException
  {
    setInput(input);
    return getComponent();
  }
  
  /**
   * convenience method for reading from a URL.
   * 
   * @param input the input URL
   * @return the root component of the scenegraph created from the input
   * @throws IOException
   */
  public SceneGraphComponent read(URL input) throws IOException
  {
    setInput(new Input(input));
    return getComponent();
  }
  
  /**
   * convenience method for reading a File.
   * 
   * @param input the input file
   * @return the root component of the scenegraph created from the input
   * @throws IOException
   */
  public SceneGraphComponent read(File input) throws IOException
  {
    setInput(new Input(input));
    return getComponent();
  }
}
