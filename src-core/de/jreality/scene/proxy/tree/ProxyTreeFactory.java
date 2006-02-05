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

import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.proxy.ProxyFactory;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class ProxyTreeFactory extends SceneGraphVisitor {

  protected SceneTreeNode proxyNode;
  private ProxyFactory proxyFactory = new ProxyFactory();
  
  public void setProxyFactory(ProxyFactory pf) {
    this.proxyFactory = pf;
  }
  
  public SceneTreeNode createProxyTreeNode(SceneGraphNode n) {
    n.accept(proxyFactory);
    n.accept(this);
    proxyNode.setProxy(proxyFactory.getProxy());
    return proxyNode;
  }
  
  public void visit(SceneGraphNode m) {
    proxyNode = new SceneTreeNode(m);
  }
  
}
