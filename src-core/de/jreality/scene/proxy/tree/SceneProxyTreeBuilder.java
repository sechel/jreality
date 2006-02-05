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

import java.util.WeakHashMap;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;

/**
 * class for generating proxy trees.
 * 
 * Limitation: this class must have a fixed root for all operations.
 * 
 * @author weissman
 *  
 */
public class SceneProxyTreeBuilder {

  protected SceneGraphComponent root;
  protected SceneTreeNode treeRoot;
  
  protected WeakHashMap proxyToTreeNodes = new WeakHashMap();
  
  protected ProxyTreeFactory proxyTreeFactory=new ProxyTreeFactory();
  private ProxyConnector connector=new ProxyConnector();
  
  public SceneProxyTreeBuilder(SceneGraphComponent root) {
    this.root = root;
  }

  Traversal traversal = new Traversal();
  protected class Traversal extends SceneGraphVisitor {

    SceneTreeNode proxy, proxyParent;
    SceneGraphPath path;
    
    // assume that proxyParent is null or contains
    // a tree node for the parent component of c
    public void visit(SceneGraphComponent c) {
      super.visit(c); // sets proxy to created proxy for the component
                      // and adds it to proxyParent as child
      if (treeRoot == null) treeRoot = proxy;
      SceneTreeNode oldParent = proxyParent;
      proxyParent = proxy;
      c.childrenAccept(this);
      proxy = proxyParent;
      proxyParent = oldParent;
    }

    /**
     * create a tree proxy node
     * for m (which includes a proxy already)
     */
    public void visit(SceneGraphNode m) {
      proxy=proxyTreeFactory.createProxyTreeNode(m);
      // add(proxyParent, proxy); this happens here in the tree
      // as we remove objects there too
      proxy.setConnector(connector);
      if (proxyParent != null) proxyParent.addChild(proxy);
      proxyToTreeNodes.put(proxy.getProxy(), proxy);
      postCreate(proxy);
    }
    
    void setRoot(SceneTreeNode root) {
      proxyParent = root;
    }
  };
  
  protected void postCreate(SceneTreeNode proxy) {
    // handle the scene tree node after creation
    // attatch listeners etc...
  }

  public SceneTreeNode createProxyTree() {
    if (proxyTreeFactory == null) throw new IllegalStateException("tree proxy factory not set!");
    if (treeRoot == null) traversal.visit(root);
    else throw new IllegalStateException("proxy tree already created");
    return treeRoot;
  }
  
  /**
   * traverses the tree along the given path
   * and returns the assigned proxy element
   */
  public Object getProxy(SceneGraphPath path) {
    SceneTreeNode n = treeRoot.findNodeForPath(path.iterator());
    return n.getProxy();
  }
  
  /**
   * proxies are stored in a WeakHashMap - so they need
   *  to have a working equals-method
   * 
   * @return the representing tree element
   */
  public SceneTreeNode getTreeNodeForProxy(Object proxy) {
    if (proxy == null) throw new IllegalStateException("proxy is null");
    if (!proxyToTreeNodes.containsKey(proxy)) throw new IllegalStateException("unknown proxy");
    return (SceneTreeNode) proxyToTreeNodes.get(proxy);
  }
  
  public SceneTreeNode getTreeRoot() {
    return treeRoot;
  }
  public ProxyTreeFactory getProxyTreeFactory() {
    return proxyTreeFactory;
  }
  public void setProxyTreeFactory(ProxyTreeFactory proxyTreeFactory) {
    this.proxyTreeFactory = proxyTreeFactory;
  }
  public ProxyConnector getConnector() {
    return connector;
  }
  public void setProxyConnector(ProxyConnector connector) {
    this.connector = connector;
  }
}
