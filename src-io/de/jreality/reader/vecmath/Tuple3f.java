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

package de.jreality.reader.vecmath;

public class Tuple3f {

  public double x;

  public double y;

  public double z;

  public Tuple3f() {
    this.x = 0.0f;
    this.y = 0.0f;
    this.z = 0.0f;
  }

  public Tuple3f(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Tuple3f(double[] t) {
    this.x = t[0];
    this.y = t[1];
    this.z = t[2];
  }

  public Tuple3f(Tuple3f t1) {
    this.x = t1.x;
    this.y = t1.y;
    this.z = t1.z;
  }

  public final void set(Tuple3f t1) {
    this.x = t1.x;
    this.y = t1.y;
    this.z = t1.z;
  }

  public final void add(Tuple3f t1) {
    this.x += t1.x;
    this.y += t1.y;
    this.z += t1.z;
  }

  public final void scale(double s) {
    this.x *= s;
    this.y *= s;
    this.z *= s;
  }

  public final void interpolate(Tuple3f t1, Tuple3f t2, double alpha) {
    this.x = (1 - alpha) * t1.x + alpha * t2.x;
    this.y = (1 - alpha) * t1.y + alpha * t2.y;
    this.z = (1 - alpha) * t1.z + alpha * t2.z;

  }

}
