package de.jreality.jogl;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
/*
 * Created on 20-Nov-2004
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

/**
 * 
 * Main Class to display a Scene that implements the LoadableScene interface by its classname. 
 * 
 * @author weissman
 */
public class LoadableSceneViewer extends InteractiveViewerDemo {
	
	
	SceneGraphComponent world;

	/**
	 * @param string
	 */
	public LoadableSceneViewer(String classname) {
		super();
		LoadableScene wm = null;
		try {
			wm = (LoadableScene) Class.forName(classname).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// scene settings
		wm.setConfiguration(ConfigurationAttributes.getSharedConfiguration());
		world = wm.makeWorld();
		viewer.setSignature(wm.getSignature());
		System.out.println("loaded world "+classname+" successful.");

	}

	public SceneGraphComponent makeWorld() {
		return world;
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("usage: java -Djava.library.path=<path to libjogl.so> [-Djreality.config=<config-file>] <full classname of LoadableScene>" );
			return;
		}
		LoadableSceneViewer test = new LoadableSceneViewer(args[0]);
		test.begin();
	}

}
