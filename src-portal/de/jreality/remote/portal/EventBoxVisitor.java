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

import java.util.Iterator;
import java.util.List;

import szg.framework.event.WandEvent;
import de.jreality.portal.tools.BoxContext;
import de.jreality.portal.tools.EventBox;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;

/**
 *
 * New java Class
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class EventBoxVisitor extends SceneGraphVisitor {

	private SceneGraphComponent root;
	private WandEvent currentEvent;
	private SceneGraphPath path;
	private Transformation wandOffset;
	private boolean debug = false;

	public EventBoxVisitor(SceneGraphComponent root, Transformation wandOffset) {
		this.root = root;
		path = new SceneGraphPath();
		this.wandOffset = wandOffset;
	}
	
	public void process(WandEvent e) {
		currentEvent = e;
		root.accept(this);
	}
	
	public void visit(SceneGraphComponent sg) {
		path.push(sg);
		processBoxes(sg.getTools());
		sg.childrenAccept(this);
		path.pop();
	}

	/**
	 * @param tools
	 */
	private void processBoxes(List tools) {
		if (tools.isEmpty()) return;
		BoxContext context = new WandBoxContext(path, currentEvent, wandOffset);
		for (Iterator i = tools.iterator(); i.hasNext(); ) {
			EventBox box = (EventBox) i.next();
			if (debug) System.out.println("event type: "+currentEvent.getType());
			switch(currentEvent.getType()) {	
				case WandEvent.ID_WAND_DRAGGED:
					if (debug) System.out.println("ID_WAND_DRAGGED");
					box.processDrag(context);
				break;
				
				case WandEvent.ID_WAND_MOVED:
					if (debug) System.out.println("ID_WAND_MOVED");
					box.processMove(context);
				break;
				
				case WandEvent.ID_BUTTON_PRESSED:
					if (debug) System.out.println("ID_BUTTON_PRESSED");
					box.processButtonPress(context);
				break;
				
				case WandEvent.ID_BUTTON_RELEASED:
					if (debug) System.out.println("ID_BUTTON_RELEASED");
					box.processButtonRelease(context);
				break;
				
				case WandEvent.ID_BUTTON_TIPPED:
					//
				break;
			}
		}
	}
	public void setWandOffset(Transformation wandOffset) {
		this.wandOffset = wandOffset;
	}
}
