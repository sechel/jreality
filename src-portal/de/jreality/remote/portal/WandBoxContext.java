/*
 * Created on 06-Dec-2004
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
package de.jreality.remote.portal;

import szg.framework.event.WandEvent;
import de.jreality.portal.tools.BoxContext;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.util.P3;

/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class WandBoxContext extends BoxContext {

	private SceneGraphPath path;
	private WandEvent event;
	private Transformation localTrafo;
	private Transformation wandOffset;
	public WandBoxContext(SceneGraphPath path, WandEvent event, Transformation wandOffset) {
		this.path = (SceneGraphPath) path.clone();
		this.event = event;
		this.wandOffset = wandOffset;
	}

	public Transformation getLocalTransformation() {
		if (localTrafo ==  null) computeLocalTrafo();
		return localTrafo;
	}

	private void computeLocalTrafo() {
		localTrafo = new Transformation(path.getInverseMatrix(null));
		localTrafo.multiplyOnRight(P3.transposeF2D(null, event.getMatrix()));
		localTrafo.multiplyOnRight(wandOffset);
	}

	public int getButton() {
		return event.getButton();
	}

	public SceneGraphPath getRootToLocal() {
		return path;
	}

}
