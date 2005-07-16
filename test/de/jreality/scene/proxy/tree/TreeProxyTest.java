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

import java.util.Iterator;

import de.jreality.scene.*;
import de.jreality.scene.proxy.ProxyFactory;
import junit.framework.TestCase;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class TreeProxyTest extends TestCase {

  public static void main(String[] args) {
    junit.swingui.TestRunner.run(TreeProxyTest.class);
  }
  
  static class PrintFactory extends ProxyFactory {
    public Object getProxy() {
      //System.out.println("PrintFactory.getProxy()");
      return null;
    }
  }
  
  static class TreeDumper {
    StringBuffer indent=new StringBuffer(" ");
    void dumpTree(SceneTreeNode node) {
      boolean isValid=node.toPath().isValid();
      System.out.println(indent.substring(0, indent.length()-1)+"-"+node.getNode().getName()+"["+node.getNode().getClass().getName()+"] valid="+isValid);
      indent.append(" | ");
      if (!node.isLeaf())
        for (Iterator i = ((SceneTreeNode)node).getChildren().iterator(); i.hasNext(); )
          dumpTree((SceneTreeNode) i.next());
      indent.delete(indent.length()-3, indent.length());
    }
  }

  public void testTreeProxy() {
    SceneGraphComponent root = null;
    // build up scene...
    SceneProxyTreeBuilder ttp = new SceneProxyTreeBuilder(root);
    ttp.setProxyTreeFactory(new ProxyTreeFactory());
    ttp.getProxyTreeFactory().setProxyFactory(new PrintFactory());
    ttp.setProxyConnector(new ProxyConnector());
    SceneTreeNode tn = ttp.createProxyTree();
    new TreeDumper().dumpTree(tn);
    System.out.println("++++++++++++++++++++++");
  }

  public void testTreeUpToDateProxy() {
    SceneGraphComponent root = new SceneGraphComponent();
    root.setName("root");
    SceneGraphComponent p1 = new SceneGraphComponent();
    p1.setName("p1");
    SceneGraphComponent p2 = new SceneGraphComponent();
    p2.setName("p2");
    SceneGraphComponent p3 = new SceneGraphComponent();
    p3.setName("p3");
    
    root.addChild(p1);
    root.addChild(p2);
    p2.setGeometry(new Sphere());
    
    UpToDateSceneProxyBuilder ttp = new UpToDateSceneProxyBuilder(root);
    
    TreeDumper td = new TreeDumper(); 
    ttp.setProxyTreeFactory(new ProxyTreeFactory());
    ttp.getProxyTreeFactory().setProxyFactory(new PrintFactory());
    ttp.setProxyConnector(new ProxyConnector());

    SceneTreeNode tn = ttp.createProxyTree();

    td.dumpTree(tn);
    System.err.println("created ++++++++++++++++++++++\n");

    root.addChild(p3);

    td.dumpTree(tn);
    System.err.println("added p3 to root ++++++++++++++++++++++\n");
    
    p1.addChild(p2);

    td.dumpTree(tn);
    System.err.println("added p2 to p1 ++++++++++++++++++++++\n");
    
    root.removeChild(p2);

    td.dumpTree(tn);
    System.err.println("removed p2 from root ++++++++++++++++++++++\n");

    p1.removeChild(p2);

    td.dumpTree(tn);
    System.err.println("removed p2 from p1 (now disposing entity?) ++++++++++++++++++++++\n");

    p1.addChild(p3);

    td.dumpTree(tn);
    System.err.println("added p3 to p1 ++++++++++++++++++++++\n");

    p2.addChild(p1);
    
    td.dumpTree(tn);
    System.err.println("added p1 to p2 (p2 not in tree) +++++++++++++\n");

    root.addChild(p2);

    td.dumpTree(tn);
    System.err.println("added p2 subtree ++++++++++++++++++++++\n");
  }
}
