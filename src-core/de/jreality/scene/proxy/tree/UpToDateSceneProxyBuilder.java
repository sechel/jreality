/*
 * Created on Mar 17, 2005
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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.logging.Level;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.event.SceneGraphComponentEvent;
import de.jreality.scene.event.SceneGraphComponentListener;
import de.jreality.util.LoggingSystem;


/**
 *
 * This class creates a proxy tree for the given sceneGraph from the root node
 * and cares about adding and removing of objects.
 * 
 * Updating the proxy tree for other changes is handled differently,
 * as there is no need for the factory mechanism then. just forward the
 * events to the implemetation
 *
 *
 * TODO: thread issues!!
 * 
 * @author weissman
 *
 */
public class UpToDateSceneProxyBuilder extends SceneProxyTreeBuilder implements SceneGraphComponentListener {

  private final Object mutex = new Object();
  
  protected IdentityHashMap nodeEntityMap = new IdentityHashMap();
  
  protected EntityFactory entityFactory;
  
  protected final Level loglevel = Level.FINE;		// somebody set this to INFO, which generates just too much -gunn
  public UpToDateSceneProxyBuilder(SceneGraphComponent root) {
    super(root);
  }
  
  protected SceneGraphVisitor attatchListeners = new SceneGraphVisitor() {
    public void visit(SceneGraphComponent c) {
      c.addSceneGraphComponentListener(UpToDateSceneProxyBuilder.this);
    }
  };
  protected SceneGraphVisitor detatchListeners = new SceneGraphVisitor() {
    public void visit(SceneGraphComponent c) {
      c.removeSceneGraphComponentListener(UpToDateSceneProxyBuilder.this);
    }
  };
  
  /**
   * TODO: synchronize and signal error when called twice?
   */
  public SceneTreeNode createProxyTree() {
    if (entityFactory == null)
      entityFactory = new EntityFactory();
    return super.createProxyTree();
  }
  /**
   * registers this class as container listener for each component.
   * so new childs are automatically added to the proxy tree
   * and removed childs get removed from it
   */
  protected void postCreate(SceneTreeNode proxy2) {
    SceneGraphNode node = proxy2.getNode();
    SceneGraphNodeEntity sge = (SceneGraphNodeEntity) nodeEntityMap.get(node);
    if (sge == null) {
      sge = entityFactory.createEntity(node);
      nodeEntityMap.put(node, sge);
      node.accept(attatchListeners);
      LoggingSystem.getLogger(this).log(loglevel, 
          "adding entity+listener for {0}", node.getName());
    }
    sge.addTreeNode(proxy2);
  }

  public void childAdded(SceneGraphComponentEvent ev) {
    synchronized (mutex) {
      SceneGraphComponent parent = (SceneGraphComponent) ev.getSceneGraphComponent();
      SceneGraphNode newChild = (SceneGraphNode) ev.getNewChildElement();
      LoggingSystem.getLogger(this).log(loglevel, 
          "handling add event: {0} added to {1} [ {2} ]", new Object[]{newChild.getName(), parent.getName(), ev.getSourceNode().getName()});
      SceneGraphNodeEntity sge = (SceneGraphNodeEntity) nodeEntityMap.get(parent);
      if (sge == null) {
        LoggingSystem.getLogger(this).warning("entity for registered component is null -> was disposed by other thread...");
        // maybe check if this class is no longer a listener
        return;
      }
      if (sge.isEmpty()) throw new IllegalStateException("empty entity node");
      // iterate parent tree nodes, assign it as the traversal's
      // parent and visit the new child -
      // this results in attatching the whole 
      // tree below the new child to each tree node
      // of the parent entity
      for (Iterator i = sge.getTreeNodes(); i.hasNext(); ) {
        traversal.proxyParent = (SceneTreeNode) i.next();
        LoggingSystem.getLogger(this).log(loglevel, 
            "attatching child {0} to {1}", new Object[]{newChild.getName(), parent.getName()});
        ev.getNewChildElement().accept(traversal);
      }
    }
  }

  public void childRemoved(SceneGraphComponentEvent ev) {
    synchronized (mutex) {
      SceneGraphComponent parent = (SceneGraphComponent) ev.getSceneGraphComponent();
      SceneGraphNode prevChild = (SceneGraphNode) ev.getOldChildElement();
      SceneGraphNodeEntity parentEntity = (SceneGraphNodeEntity) nodeEntityMap.get(parent);
      LoggingSystem.getLogger(this).log(loglevel, 
          "handling remove event: {0} removed from {1}", new Object[]{prevChild.getName(), parent.getName()});
      if (parentEntity == null) {
        throw new Error("event from unknown container");
      }
      // remove child from all parent tree nodes
      ArrayList disposedEntities = new ArrayList();
      for (Iterator i = parentEntity.getTreeNodes(); i.hasNext(); ) {
        SceneTreeNode deleted = ((SceneTreeNode)i.next()).removeChildForNode(prevChild);
        deleted.dispose(disposedEntities);
      }
      for (Iterator i = disposedEntities.iterator(); i.hasNext(); )
        disposeEntity((SceneGraphNodeEntity) i.next(), true);
    }
  }
  
  /**
   * move the listener from this class to the entity itself?
   * NO: so we can syncronize all changes in this class!
   * @param entity
   */
  private void disposeEntity(SceneGraphNodeEntity entity, boolean assertEmpty) {
    if (assertEmpty && !entity.isEmpty()) throw new IllegalStateException("not empty");
    LoggingSystem.getLogger(this).log(loglevel, 
        "disposing entity+listener for removed child {0}", new Object[]{entity.getNode().getName()});
    nodeEntityMap.remove(entity.getNode());
    if (entity.getNode() instanceof SceneGraphComponent)
      ((SceneGraphComponent)entity.getNode()).removeSceneGraphComponentListener(this);
    entityFactory.disposeEntity(entity);
  }

  public void childReplaced(SceneGraphComponentEvent ev) {
    synchronized (mutex) {
      childRemoved(ev);
      childAdded(ev);
    }
  }

  public EntityFactory getEntityFactory() {
    return entityFactory;
  }
  public void setEntityFactory(EntityFactory ef) {
    if (treeRoot != null) throw new IllegalStateException("can't change policy after proxy creation");
    this.entityFactory = ef;
  }
  public void visibilityChanged(SceneGraphComponentEvent ev) {
  }
  
  public void dispose() {
    SceneGraphVisitor disposeVisitor = new SceneGraphVisitor() {
      public void visit(SceneGraphNode n) {
        SceneGraphNodeEntity entity = (SceneGraphNodeEntity) nodeEntityMap.get(n);
        disposeEntity(entity, false);
      };
    };
    root.accept(disposeVisitor);
  }
}
