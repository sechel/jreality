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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
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

package de.jreality.reader;

import java.io.IOException;

import de.jreality.math.Matrix;
import de.jreality.reader.quake3.Quake3Converter;
import de.jreality.reader.quake3.Quake3Loader;
import de.jreality.util.Input;

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
