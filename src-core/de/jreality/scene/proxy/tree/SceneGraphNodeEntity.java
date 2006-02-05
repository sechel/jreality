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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import de.jreality.scene.SceneGraphNode;
import de.jreality.util.LoggingSystem;


/**
 *
 * THis class contains several tree nodes for the covered node.
 * It is intended to be used for synchronizing a proxy tree.
 * 
 * Implement the update mechanism for geometry/apperance/transformation
 * changes here - and update the SceneTreeNodes as needed.
 * 
 * So you need to calculate the local data for the proxies
 * only once, instead of converting a jreality event in every copy instance.
 * 
 * A typical application would be a transformation change,
 * so one calculates the needed proxy matrix/quaternion representation
 * only once and sets it for all proxies then by iterating the TreeNodes.
 * 
 * <b>Note: subclasses need to implement Geometry/Transformation/Appearance-Listener
 * if the EntityFactory is set to update on these events.
 * </b>
 * 
 * @author weissman
 *
 */
public class SceneGraphNodeEntity {
  
  private List treeNodes = new ArrayList();
  private SceneGraphNode node;
  
  protected SceneGraphNodeEntity(SceneGraphNode node) {
    this.node = node;
  }
  
  protected void addTreeNode(SceneTreeNode tn) {
    LoggingSystem.getLogger(this).log(Level.INFO,
        "adding new TreeNode[{1}] for {0}", new Object[]{node.getName(), new Integer(treeNodes.size())});
    if (tn == null) throw new IllegalStateException("tree node is null");
    treeNodes.add(tn);
    tn.setEntity(this);
  }
  
  protected void removeTreeNode(SceneTreeNode tn) {
  	LoggingSystem.getLogger(this).log(Level.INFO,
        "removing TreeNode for {0}", node.getName());
    if (tn == null) throw new IllegalStateException("tree node is null");
    treeNodes.remove(tn);
  }
  
  public Iterator getTreeNodes() {
    return Collections.unmodifiableCollection(treeNodes).iterator();
  }
  
  boolean isEmpty() {
    return treeNodes.size() == 0;
  }
  
  int size() {
    return treeNodes.size();
  }

  protected void dispose() {
    treeNodes.clear();
    node = null;
  }
  
  public SceneGraphNode getNode() {
    return node;
  }
}
