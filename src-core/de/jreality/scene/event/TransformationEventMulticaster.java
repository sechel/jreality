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


package de.jreality.scene.event;


public final class TransformationEventMulticaster implements TransformationListener
{
  private final TransformationListener a, b;
  private TransformationEventMulticaster(TransformationListener a, TransformationListener b) {
      this.a = a; this.b = b;
  }
  private TransformationListener remove(TransformationListener oldl) {
    if(oldl == a)  return b;
    if(oldl == b)  return a;
    TransformationListener a2 = remove(a, oldl);
    TransformationListener b2 = remove(b, oldl);
    if(a2 == a && b2 == b) return this;
    return add(a2, b2);
  }
  public static TransformationListener add(TransformationListener a, TransformationListener b)
  {
    final TransformationListener result;
    if(a==null) result=b; else if(b==null) result=a;
    else result=new TransformationEventMulticaster(a, b);
    return result;
  }
  public static TransformationListener remove(TransformationListener l, TransformationListener oldl)
  {
    final TransformationListener result;
    if(l==oldl||l==null) result=null;
    else if(l instanceof TransformationEventMulticaster)
      result=((TransformationEventMulticaster)l).remove(oldl);
    else result=l;
    return result;
  }
  /**
   * Recast the event.
   * @see de.jreality.scene.event.TransformationListener#transformationMatrixChanged(TransformationEvent)
   */
  public void transformationMatrixChanged(TransformationEvent ev)
  {
    a.transformationMatrixChanged(ev); b.transformationMatrixChanged(ev);
  }
}
