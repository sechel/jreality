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

package de.jreality.scene;

import java.util.Iterator;

import de.jreality.math.Rn;
import de.jreality.scene.event.SceneGraphComponentEvent;
import de.jreality.scene.event.SceneGraphComponentListener;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationEventMulticaster;
import de.jreality.scene.event.TransformationListener;


/**
 *
 * This class observes the given path for changes of the resulting
 * path matrix.
 * <b>Note: this class makes a private copy of the path and
 * therefore doesn't care about changes of the path itself!</b>
 * If the observed path changes, call setPath(myPath) to observe
 * the changed path.
 * 
 * TODO: add listeners to changes other than transformation
 * 
 * @author weissman
 *
 */
public class SceneGraphPathObserver implements TransformationListener, SceneGraphComponentListener {

  SceneGraphPath currentPath;
  private TransformationListener transformationListener;
  
  /** this is just a temp storage for creating trafo events */
  private double[] matrixData = new double[16];
  
  /** to see if a change event really results in a change of the paths matrix */
  private double[] oldMatrix = new double[16];
  
  public SceneGraphPathObserver() {
    currentPath = new SceneGraphPath();
  }
  
  public SceneGraphPathObserver(SceneGraphPath path) {
    currentPath = (SceneGraphPath) path.clone();
    addListeners();
  }
  
  /**
   * TODO: remove only the listeners from nodes that 
   * are not part of the new path!
   * 
   * calling this method results in changing the observed path.
   * TransformationChangedEvents will be generated only if the new
   * given path changes.
   * @param newPath
   */
  public void setPath(SceneGraphPath newPath) {
    removeListeners();
    if (newPath != null) currentPath = (SceneGraphPath) newPath.clone();
    else currentPath.clear();
    addListeners();
    calculateCurrentMatrix();
  }
  
  private void calculateCurrentMatrix() {
    currentPath.getMatrix(oldMatrix);
  }
  
  private boolean hasChanged() {
    return !Rn.equals(oldMatrix, matrixData);
  }

  public void dispose() {
    removeListeners();
    currentPath.clear();
  }
    
  private void addListeners() {
    for (Iterator i = currentPath.iterator(); i.hasNext(); ) {
      Object node = i.next();
      if (node instanceof SceneGraphComponent) {
        ((SceneGraphComponent)node).addSceneGraphComponentListener(this);
        Transformation trafo = ((SceneGraphComponent)node).getTransformation();
        if (trafo != null) trafo.addTransformationListener(this);      }
    }
  }

  private void removeListeners() {
    for (Iterator i = currentPath.iterator(); i.hasNext(); ) {
      Object node = i.next();
      if (node instanceof SceneGraphComponent) {
        SceneGraphComponent component = (SceneGraphComponent)node;
        component.removeSceneGraphComponentListener(this);
        Transformation trafo = component.getTransformation();
        if (trafo != null) trafo.removeTransformationListener(this);
      }
    }
  }

  public void addTransformationListener(TransformationListener listener) {
    transformationListener=
      TransformationEventMulticaster.add(transformationListener, listener);
  }
  public void removeTransformationListener(TransformationListener listener) {
    transformationListener=
      TransformationEventMulticaster.remove(transformationListener, listener);
  }

  /**
   * Tell the outside world that this transformation has changed.
   * This methods takes no parameters and is equivalent
   * to "everything has/might have changed".
   * 
   * Regard that the given Transformation is not part of the 
   * scene, even though it extends SceneGraphNode - this will be changed!
   */
  protected void fireTransformationChanged() {
    final TransformationListener l=transformationListener;
    if(l != null) {
      currentPath.getMatrix(matrixData);
      if (hasChanged()) {
        System.arraycopy(matrixData, 0, oldMatrix, 0, 16);
        l.transformationMatrixChanged(new TransformationEvent(new Transformation(matrixData)));
      }
    }
  }

  public void transformationMatrixChanged(TransformationEvent ev) {
    fireTransformationChanged();
  }

  public void childAdded(SceneGraphComponentEvent ev) {
    if (ev.getChildType() == SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION) {
      ((Transformation)ev.getNewChildElement()).addTransformationListener(this);
      fireTransformationChanged();
    }
  }

  public void childRemoved(SceneGraphComponentEvent ev) {
    if (ev.getChildType() == SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION) {
      ((Transformation)ev.getOldChildElement()).removeTransformationListener(this);
      fireTransformationChanged();
    }
  }

  public void childReplaced(SceneGraphComponentEvent ev) {
    if (ev.getChildType() == SceneGraphComponentEvent.CHILD_TYPE_TRANSFORMATION) {
      childRemoved(ev);
      childAdded(ev);
    }
  }

  public void visibilityChanged(SceneGraphComponentEvent ev) {
  }
  
}
