
package de.jreality.scene.proxy;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;

/**
 * Create a 1:1 copy of a scene graph.
 */
public class CopyScene extends SceneProxyBuilder
{
	SgAdd sg=new SgAdd();

	public CopyScene() {
		setProxyFactory(new CopyFactory());
	}
	/**
	 * Assumes that the created proxies are instanceof of the appropriate
	 * scene graph classes and adds the node to the component.
     */
    public void add(Object parentProxy, Object childProxy) {
        sg.add((SceneGraphComponent)parentProxy, (SceneGraphNode)childProxy);
    }

}
