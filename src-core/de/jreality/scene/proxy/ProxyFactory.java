
package de.jreality.scene.proxy;

import de.jreality.scene.SceneGraphVisitor;

/**
 * Factory for proxy objects. The scene proxy builder will call the visit
 * method exactly one time for each sg component encountered. The created
 * proxy object will queried through the {@link #getProxy() getProxy} method
 * after each call. Implementations of this class do not need to implement
 * any kind of history caching or such-alike.
 */
public class ProxyFactory extends SceneGraphVisitor
{
	/**
	 * Return the most recent created proxy.
	 */
	public Object getProxy() {
   return null; 
  }
}
