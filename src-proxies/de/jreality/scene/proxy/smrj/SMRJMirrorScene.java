/*
 * Created on Nov 10, 2004
 *
 * This file is part of the de.jreality.scene.proxy.rmi package.
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
package de.jreality.scene.proxy.smrj;

import java.rmi.RemoteException;
import java.util.Arrays;

import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.proxy.SceneProxyBuilder;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphComponent;
import de.jreality.scene.proxy.rmi.RemoteSceneGraphNode;
import de.smrj.RemoteFactory;

/**
 * @author weissman
 *
 **/
public class SMRJMirrorScene extends SceneProxyBuilder {

	SMRJSceneGraphSynchronizer synchronizer;
	
	public SMRJMirrorScene(RemoteFactory rf) {
		setProxyFactory(new SMRJMirrorFactory(rf));
		synchronizer = new SMRJSceneGraphSynchronizer(this);
	}

    protected Object getProxyImpl(SceneGraphNode target) {
	    if (!proxies.containsKey(target)) // attatch the synchronizer
	    	((SceneGraphNode)target).accept(synchronizer);
    	return super.getProxyImpl(target);
    }

	/**
	 * Assumes that the created proxies are instanceof of the appropriate
	 * scene graph classes and adds the node to the component.
     */
    public void add(Object parentProxy, Object childProxy) {
        System.out.println("SMRJMirrorScene.add() "+childProxy.getClass().getName());
        System.out.println(Arrays.asList(childProxy.getClass().getInterfaces()).toString().replace(',', '\n'));
    	try {
            ((RemoteSceneGraphComponent)parentProxy).add((RemoteSceneGraphNode) childProxy);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
