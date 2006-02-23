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
import de.jreality.util.Input;

/**
 * Entry point to load a jReality SceneGraph from some sort of resource.
 */
public final class Readers
{
  // avoid instanciating this class
  private Readers() {}
  /**
   * returns a reader for the given format if available - null if not.
   * @param format String representation for the format to read.
   * @return a matching reader or null.
   */
  public static SceneReader findReader(String format)
  {
    Exception ex;
    try
    {
      return (SceneReader)Class.forName("de.jreality.reader.Reader"+format).newInstance();
    } catch (InstantiationException e)
    {
      ex=e;
    } catch (IllegalAccessException e)
    {
      ex=e;
    } catch (ClassNotFoundException e)
    {
      return null;
    }
    IllegalStateException rtex=new IllegalStateException("invalid reader");
    rtex.initCause(ex);
    throw rtex;
  }
  /**
   * Reads the scene from the given input with the given SceneReader.
   * @param reader the SceneReader to read with.
   * @param input the Input to read with the given SceneReader.
   * @return the root component of the read scene.
   * @throws IOException
   */
  public static SceneGraphComponent read(SceneReader reader, Input input)
    throws IOException
  {
    reader.setInput(input);
    return reader.getComponent();
  }

  /**
   * Reads the scene from the given input with the SceneReader for the given format.
   * @param format the format of the given input.
   * @param input the Input to read with the SceneReader for the given format.
   * @return the root component of the read scene.
   * @throws IOException
   */
  public static SceneGraphComponent read(String format, Input input)
    throws IOException
  {
    return read(findReader(format), input);
  }
  
  /**
   * reads the given file with the matching reader for the file ending.
   * @param file the file to read.
   * 
   * @throws IOException
   */
  public static SceneGraphComponent read(File file) throws IOException {
    return read(Input.getInput(file));
  }

  /**
   * reads the given url with the matching reader for the file ending.
   * NOTE: the URL must end with a supported file extension.
   * 
   * @param url the url to read.
   * 
   * @throws IOException
   */
  public static SceneGraphComponent read(URL url) throws IOException {
    return read(Input.getInput(url));
  }

  /**
   * determines the format of the given input depending on the file ending
   * of the URL. That means it can only read Inputs based on filesand URLs.
   * 
   * @param input
   * @return rhe root component of the read input
   * 
   * @throws IOException
   */
  public static SceneGraphComponent read(Input input) throws IOException {
    SceneGraphComponent sgc = null;
    //String lc = input.toURL().toExternalForm();
    String lc = input.getDescription();
    String format = findFormat(lc);
    if (format == null) throw new IllegalArgumentException("unknown file format");
    return read(format, input);
  }
public static String findFormat(String resourceName) {
	  String format=null;
    String lcName = resourceName.toLowerCase();
    if (lcName.endsWith(".top")) {
      format = "POLY";
    } else if (lcName.endsWith(".pov")) {
      format = "POV";
    } else if (lcName.endsWith(".bsh")) {
      format = "BSH";
    } else if (lcName.endsWith(".pov")) {
      format = "POV";
    } else if (lcName.endsWith(".bsp")) {
      format = "BSP";
    } else if (lcName.endsWith(".ase")) {
      format = "ASE";
    } else if (lcName.endsWith(".obj")) {
      format = "OBJ";
    } else if (lcName.endsWith(".3ds")) {
      format = "3DS";
    } else if (lcName.endsWith(".jvx")) {
      format = "JVX";
    } else if (lcName.endsWith(".jrs")) {
      format = "JRS";
    } else if (lcName.endsWith(".wrl")) {
      format = "VRML";
    } else if (lcName.endsWith(".m")) {
      format = "MATHEMATICA";
    } else if (lcName.endsWith("off") || lcName.endsWith("quad") || lcName.endsWith("mesh") || lcName.endsWith("vect") || lcName.endsWith("bez") 
        || lcName.endsWith(".grp") || lcName.endsWith(".oogl") || lcName.endsWith(".list") || lcName.endsWith("inst")){
      format = "OOGL";
    }
	return format;
}

}
