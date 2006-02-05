
package de.jreality.scene.proxy;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphVisitor;

/**
 * Base class for builder that create some kind of mirroring structure
 * for a scene graph. This class does not make copies for each path
 * that reaches a node.
 * 
 */
public class SceneProxyBuilder {
    protected IdentityHashMap proxies = new IdentityHashMap();
    ProxyFactory proxyFactory;

    class Traversal extends SceneGraphVisitor {
        Object proxy, proxyParent;
        public void visit(SceneGraphComponent c) {
            boolean stopTraversing = proxies.containsKey(c);
        	super.visit(c);
        	if (stopTraversing) return;
            Object old=proxyParent;
            proxyParent=proxy;
            c.childrenAccept(this);
            proxy=proxyParent;
            proxyParent=old;
        }
        public void visit(SceneGraphNode m) {
            proxy=SceneProxyBuilder.this.getProxyImpl(m);
            add(proxyParent, proxy);
        }
    };
    Traversal traversal=new Traversal();

    protected SceneProxyBuilder() {}

    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public void setProxyFactory(ProxyFactory factory) {
        proxyFactory=factory;
    }

    protected Object getProxyImpl(SceneGraphNode target) {
        Object proxy=proxies.get(target);
        if (proxy == null) {
            target.accept(proxyFactory);
            proxy=proxyFactory.getProxy();
            proxies.put(target, proxy);
        }
        return proxy;
    }

    public void add(Object parentProxy, Object childProxy) {}

    public Object createProxyScene(SceneGraphNode node) {
        if (proxyFactory == null)
            throw new IllegalStateException("no proxy factory set");
        boolean traverse=(!proxies.containsKey(node))&&(node instanceof SceneGraphComponent);
        Object proxy=getProxyImpl(node);
        if(traverse) {
            traversal.proxyParent=proxy;
            ((SceneGraphComponent)node).childrenAccept(traversal);
        }
        return proxy;
    }
    
    public Object getProxy(Object local) {
    	return proxies.get(local);
    }
    public List getProxies(List l) {
    	List ret = new ArrayList(l.size());
    	for (int i = 0; i < l.size(); i++) {
    		ret.add(proxies.get(l.get(i)));
    	}
    	System.out.println("[SceneProxyBuilder] converted "+ret.size()+" proxies.");
    	return ret;
    }

    public void disposeProxy(SceneGraphComponent root) {
      root.accept(new DisposeTraversal());
    }
    
    class DisposeTraversal extends SceneGraphVisitor {
      Object proxy, proxyParent;
      public void visit(SceneGraphComponent c) {
          boolean stopTraversing = proxies.containsKey(c);
        super.visit(c);
        if (stopTraversing) return;
          Object old=proxyParent;
          proxyParent=proxy;
          c.childrenAccept(this);
          proxy=proxyParent;
          proxyParent=old;
      }
      public void visit(SceneGraphNode m) {
        if (proxies.containsKey(m)) disposeProxyImpl(m);
      }
  }

    protected void disposeProxyImpl(SceneGraphNode target) {
      proxies.remove(target);
    }

}
