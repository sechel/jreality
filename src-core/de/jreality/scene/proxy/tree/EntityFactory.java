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
package de.jreality.scene.proxy.tree;


import de.jreality.scene.*;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.scene.event.GeometryListener;
import de.jreality.scene.event.TransformationListener;


/**
 *
 * This class produces SceneGraphNodeEntities, based on the
 * desired update behavior for the proxy.
 * 
 * inherit this class and overwrite the methods
 * 
 * <li>produceTransformationEntity
 * <li>produceAppearanceEntity
 * <li>produceGeometryEntity
 * <br>
 * as needed. These are only called if the corresponding update method
 * is set to true, then the Entities are attatched as Listeners to
 * the SceneGraphNode.
 * 
 * @author weissman
 *
 */
public class EntityFactory {

  private SceneGraphNodeEntity produced;

  private boolean updateTransformation;
  private boolean updateAppearance;
  private boolean updateGeometry;
  
  public boolean isUpdateAppearance() {
    return updateAppearance;
  }
  public void setUpdateAppearance(boolean updateAppearance) {
    this.updateAppearance = updateAppearance;
  }
  public boolean isUpdateGeometry() {
    return updateGeometry;
  }
  public void setUpdateGeometry(boolean updateGeometry) {
    this.updateGeometry = updateGeometry;
  }
  public boolean isUpdateTransformation() {
    return updateTransformation;
  }
  public void setUpdateTransformation(boolean updateTransformation) {
    this.updateTransformation = updateTransformation;
  }
  
  SceneGraphNodeEntity createEntity(SceneGraphNode node) {
    node.accept(createTraversal);
    return produced;
  }
  private SceneGraphVisitor createTraversal = new SceneGraphVisitor() {
    public void visit(Appearance a) {
      if (updateAppearance) {
        produced=produceAppearanceEntity(a);
        a.addAppearanceListener((AppearanceListener) produced);
      }
      else super.visit(a);
    }
    public void visit(Geometry g) {
      if (updateGeometry) {
        produced=produceGeometryEntity(g);
        g.addGeometryListener((GeometryListener) produced);
      }
      else super.visit(g);
    }
    public void visit(Transformation t) {
      if (updateTransformation) {
        produced=produceTransformationEntity(t);
        t.addTransformationListener((TransformationListener) produced);
      }
      else super.visit(t);
    }
    public void visit(SceneGraphNode n) {
      produced=produceSceneGraphNodeEntity(n);
    }
  };
  
  public void disposeEntity(SceneGraphNodeEntity entity) {
    produced=entity;
    entity.getNode().accept(disposeTraversal);
    entity.dispose();
  }
  
  private SceneGraphVisitor disposeTraversal = new SceneGraphVisitor() {
    public void visit(Appearance a) {
      if (updateAppearance) {
        a.removeAppearanceListener((AppearanceListener) produced);
      }
    }
    public void visit(Geometry g) {
      if (updateGeometry) {
        g.removeGeometryListener((GeometryListener) produced);
      }
    }
    public void visit(Transformation t) {
      if (updateTransformation) {
        t.removeTransformationListener((TransformationListener) produced);
      }
    }
  };

  protected SceneGraphNodeEntity produceSceneGraphNodeEntity(SceneGraphNode n) {
    return new SceneGraphNodeEntity(n);
  }
  /**
   * this method must return a SceneGraphNodeEntity that
   * implements TransformationListener!
   */
  protected SceneGraphNodeEntity produceTransformationEntity(Transformation t) {
    throw new IllegalStateException("not implemented");
  }
  /**
   * this method must return a SceneGraphNodeEntity that
   * implements AppearanceListener!
   */
  protected SceneGraphNodeEntity produceAppearanceEntity(Appearance a) {
    throw new IllegalStateException("not implemented");
  }
  /**
   * this method must return a SceneGraphNodeEntity that
   * implements GeometryListener!
   */
  protected SceneGraphNodeEntity produceGeometryEntity(Geometry g) {
    throw new IllegalStateException("not implemented");
  }
}
