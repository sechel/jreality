/*
 * Created on Mar 8, 2005
 *
 * This file is part of the de.jreality.reader package.
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

import de.jreality.reader.quake3.Quake3Converter;
import de.jreality.reader.quake3.Quake3Loader;
import de.jreality.scene.Transformation;
import de.jreality.util.Matrix;

/**
 * 
 * This class reads a bsp file including textures and lightmaps.
 * Further more this class can do visibility culling for a given head/camera
 * position. So in contrast to other readers one should keep the reader reference
 * and call setViewTransformation(camTrafo) for culling geometries that are
 * not in the field of view.
 * 
 * @author weissman
 **/
public class ReaderBSP extends AbstractReader {

  private Quake3Converter conv;
  
  public void setInput(Input in) throws IOException {
    super.setInput(in);
    Quake3Loader loader = new Quake3Loader();
    loader.load(input.getInputStream());
    conv = new Quake3Converter(input);
    conv.convert(loader);
    this.root = conv.getComponent();
  }
  
  /**
   * do visibility culling for the given camera position.
   * 
   * @param trafo the current camera position matrix
   */
  public void setViewTransformation(Matrix trafo) {
    conv.setVisibility(trafo.getColumn(3));
  }

}
