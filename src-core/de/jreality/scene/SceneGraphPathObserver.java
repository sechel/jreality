/*
 * Created on Mar 19, 2005
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
package de.jreality.scene;

import java.util.Iterator;

import de.jreality.math.Rn;
import de.jreality.scene.event.*;


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
