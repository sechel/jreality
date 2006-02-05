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

import java.util.*;
import java.util.logging.Level;

import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.Geometry;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.util.LoggingSystem;


/**
 * This class represents a tree version of a SceneGraphNode.
 * 
 * Such a proxy class exists for each unique path to a SceneGraphNode.
 * 
 * Typical application would be calculating a BoundingBox in
 * world coordinates - in contrast to the BoundingBox in local
 * coordinates, which would typically belong to the corresponding
 * Entity. 
 *
 * @author weissman
 *
 */
 public class SceneTreeNode {
  
  private SceneTreeNode parent=null;
  private SceneGraphNode node;
  private IdentityHashMap childrenMap = new IdentityHashMap();
  private List childList;
  private List childrenRO;
  private ProxyConnector connector;
  private Object proxy;
  private SceneGraphNodeEntity entity;
  
  protected final boolean isComponent;
  private boolean hasTrafo;
  private boolean hasApp;
  private boolean hasCam;
  private boolean hasLight;
  private boolean hasGeom;
  
  protected SceneTreeNode(SceneGraphNode node) {
    this.node = node;
    isComponent = node instanceof SceneGraphComponent;
    if (isComponent) {
      childList = new ArrayList(((SceneGraphComponent)node).getChildComponentCount()+5);
      childrenRO  = Collections.unmodifiableList(childList);
    } else {
      childList = Collections.EMPTY_LIST;
      childrenRO = Collections.EMPTY_LIST;
    }
  }
  
  private void setParent(SceneTreeNode parent) {
    if (this.parent != null) throw new IllegalStateException("parent already set!");
    this.parent = parent;
  }

  public boolean isLeaf() {
    return childrenMap.size()==0;
  }
  
  public List getChildren() {
    return childrenRO;
  }
  
  public SceneGraphNode getNode() {
    return node;
  }
  public SceneTreeNode getParent() {
    return parent;
  }
  public Object getProxy() {
    return proxy;
  }
  public void setProxy(Object proxy) {
    this.proxy = proxy;
  }
  SceneTreeNode findNodeForPath(Iterator i) {
    if (!i.hasNext()) return this;
    Object nextSGN = i.next();
    if (!isComponent || !childrenMap.containsKey(nextSGN)) throw new IllegalStateException("path doesn't match!");
    return ((SceneTreeNode) childrenMap.get(nextSGN)).findNodeForPath(i);
  }
  public int addChild(SceneTreeNode child) {
    int idx = 0;
    childrenMap.put(child.getNode(), child);
    child.setParent(this);
    
    //computeChildren();
    if (child.getNode() instanceof SceneGraphComponent) {
      childList.add(child);
      idx=childList.size()-1;
    } else {
      SceneGraphNode ch = child.getNode();
      if (ch instanceof Transformation) {
        if (hasTrafo) {
          childList.set(idx, child);
        } else {
          childList.add(idx, child);
        }
        hasTrafo = true;
      } else if (ch instanceof Appearance) {
        if (hasTrafo) idx++;
        if (hasApp) {
          childList.set(idx, child);
        } else {
          childList.add(idx, child);
        }
        hasApp = true;
      } else if (ch instanceof Camera) {
        if (hasTrafo) idx++;
        if (hasApp) idx++;
        if (hasCam) {
          childList.set(idx, child);
        } else {
          childList.add(idx, child);
        }
        hasCam = true;
      } else if (ch instanceof Light) {
        if (hasTrafo) idx++;
        if (hasApp) idx++;
        if (hasCam) idx++;
        if (hasLight) {
          childList.set(idx, child);
        } else {
          childList.add(idx, child);
        }
        hasLight = true;
      } else if (ch instanceof Geometry) {
        if (hasTrafo) idx++;
        if (hasApp) idx++;        
        if (hasCam) idx++;
        if (hasLight) idx++;
        if (hasGeom) {
          childList.set(idx, child);
        } else {
          childList.add(idx, child);
        }
        hasGeom = true;
      }
    }
    connector.add(getProxy(), child.getProxy());
    return idx;
  }
  public SceneGraphPath toPath() {
    // fill list in reverse order
    LinkedList ll = new LinkedList();
    ll.add(this.getNode());
    for (SceneTreeNode n = this; n.getParent()!= null; n = n.getParent())
      ll.add(n.getParent().getNode());
    // fill arraylist in correct oder
    ArrayList al = new ArrayList(ll);
    int ind = ll.size()-1;
    for (Iterator i = ll.iterator(); i.hasNext(); ind--) {
      al.set(ind, i.next());
    }
    return SceneGraphPath.fromList(al);
  }
  void setConnector(ProxyConnector connector) {
    this.connector = connector;
  }
  public SceneGraphNodeEntity getEntity() {
    return entity;
  }
  void setEntity(SceneGraphNodeEntity entity) {
    this.entity = entity;
  }
  SceneTreeNode removeChildForNode(SceneGraphNode prevChild) {
    if (!childrenMap.containsKey(prevChild)) throw new IllegalStateException("unknown child!");
    SceneTreeNode ret = getTreeNodeForChild(prevChild);
    removeChild(ret);
    if (prevChild instanceof Transformation) hasTrafo = false;
    if (prevChild instanceof Appearance) hasApp = false;
    if (prevChild instanceof Camera) hasCam = false;
    if (prevChild instanceof Light) hasLight = false;
    if (prevChild instanceof Geometry) hasGeom = false;
    return ret;
  }
  protected int removeChild(SceneTreeNode prevChild) {
    int ret = childList.indexOf(prevChild);
    childrenMap.remove(prevChild);
    childList.remove(ret);
    return ret;
  }
  
  public SceneTreeNode getTreeNodeForChild(SceneGraphNode prevChild) {
    SceneTreeNode ret = (SceneTreeNode) childrenMap.get(prevChild);
    return ret;
  }
  public SceneTreeNode getTransformationTreeNode() {
    if (!isComponent) throw new UnsupportedOperationException("no component");
    return getTreeNodeForChild(((SceneGraphComponent)node).getTransformation());
  }
  public SceneTreeNode getAppearanceTreeNode() {
    if (!isComponent) throw new UnsupportedOperationException("no component");
    return getTreeNodeForChild(((SceneGraphComponent)node).getAppearance());
  }
  public SceneTreeNode getGeometryTreeNode() {
    if (!isComponent) throw new UnsupportedOperationException("no component");
    return getTreeNodeForChild(((SceneGraphComponent)node).getGeometry());
  }

  /**
   * disposes the whole tree from this node on
   * works recursively. also disposes the entity
   * if it is impty
   */
  protected void dispose(ArrayList disposedEntities) {
    for (Iterator i = getChildren().iterator(); i.hasNext(); ) {
      ((SceneTreeNode)i.next()).dispose(disposedEntities);
    }
    int prevSize=getEntity().size();
    getEntity().removeTreeNode(this);
    LoggingSystem.getLogger(this).log(Level.FINE, "entity size: prev="+prevSize+" new="+getEntity().size());
    if (getEntity().isEmpty()) {
      disposedEntities.add(getEntity());
    }
  }

}
