/*
 * Created on Dec 6, 2004
 *
 * This file is part of the de.jreality.remote.portal package.
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

import java.util.List;

import de.jreality.portal.tools.SpaceDragEvent;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.tool.ToolContext;

/**
 * @author weissman
 *
 */
public abstract class BoxVisitor extends SceneGraphVisitor {

	/**
	 * 
	 */
	public BoxVisitor() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	protected abstract void perform(List boxes);

	List boxes = null;
	
	public void visit(SceneGraphComponent c) {
		if ((boxes = c.getTools()) != null) perform(boxes);
		c.childrenAccept(this);
	}

	/**
	 * @return
	 */
	protected ToolContext createContext() {
		// TODO Auto-generated method stub
		java.awt.Button bu = new java.awt.Button("bla");
		return null;
	}
}
