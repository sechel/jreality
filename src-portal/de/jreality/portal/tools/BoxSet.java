/*
 * Created on 03-Dec-2004
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
package de.jreality.portal.tools;

import java.util.HashMap;

import szg.framework.event.WandEvent;
import szg.framework.event.WandListener;
import szg.framework.event.WandMotionListener;

/**
 * @author gollwas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BoxSet implements WandMotionListener, WandListener, SpaceCollisionListener, SpaceDragListener {
	
	int numBoxes;
	final Box[] boxes;
	double dx, dy, dz;
	
	HashMap indexMap = new HashMap();
	
	public BoxSet(int numBoxes) {
		this.numBoxes = numBoxes;
		boxes = new Box[numBoxes];
		init();
	}
	
	private void init() {
		for (int i = 0; i < numBoxes; i++) {
			boxes[i] = new Box();
			boxes[i].addSpaceCollisionListener(this);
			boxes[i].addSpaceDragListener(this);
			indexMap.put(boxes[i], new Integer(i));
		}
	}

	public BoxSet(int numBoxes, double d) {
		this(numBoxes);
		setTolerance(d,d,d);
	}
	
	public void setTolerance(double dx, double dy, double dz) {
		for (int i = 0; i < numBoxes; i++) {
			boxes[i].setDx(dx);
			boxes[i].setDy(dy);
			boxes[i].setDz(dz);
		}
	}

	public void wandDragged(WandEvent event) {
		for (int i = 0; i < numBoxes; i++)
			boxes[i].wandDragged(event);
	}

	public void wandMoved(WandEvent event) {
		for (int i = 0; i < numBoxes; i++)
			boxes[i].wandMoved(event);
	}

	public void buttonPressed(WandEvent event) {
		for (int i = 0; i < numBoxes; i++)
			boxes[i].buttonPressed(event);
	}

	public void buttonReleased(WandEvent event) {
		for (int i = 0; i < numBoxes; i++)
			boxes[i].buttonReleased(event);
	}

	public void buttonTipped(WandEvent event) {
		for (int i = 0; i < numBoxes; i++)
			boxes[i].buttonTipped(event);
	}

	public void axisMoved(WandEvent event) {
		for (int i = 0; i < numBoxes; i++)
			boxes[i].axisMoved(event);
	}
	
	public Box getBox(int i) {
		return boxes[i];
	}

	public void enter(SpaceCollisionEvent e) {
		int index = ((Integer)indexMap.get(e.getSource())).intValue();
		e.setIndex(index);
		fireEnter(e);
	}

	public void leave(SpaceCollisionEvent e) {
		int index = ((Integer)indexMap.get(e.getSource())).intValue();
		e.setIndex(index);
		fireLeave(e);
	}

	public void dragStart(SpaceDragEvent e) {
		int index = ((Integer)indexMap.get(e.getSource())).intValue();
		e.setIndex(index);
		fireDragStart(e);
	}

	public void drag(SpaceDragEvent e) {
		int index = ((Integer)indexMap.get(e.getSource())).intValue();
		e.setIndex(index);
		fireDrag(e);
	}

	public void dragEnd(SpaceDragEvent e) {
		int index = ((Integer)indexMap.get(e.getSource())).intValue();
		e.setIndex(index);
		fireDragEnd(e);
	}

	private SpaceDragListener dragListener;
	public void addSpaceDragListener(SpaceDragListener listener) {
		dragListener=
		  SpaceDragEventMulticaster.add(dragListener, listener);
	}
	public void removeSpaceDragListener(SpaceDragListener listener) {
		dragListener=
		  SpaceDragEventMulticaster.remove(dragListener, listener);
	}
    private SpaceCollisionListener collisionListener;
	public void addSpaceCollisionListener(SpaceCollisionListener listener) {
		collisionListener=
		  SpaceCollisionEventMulticaster.add(collisionListener, listener);
	}
	public void removeSpaceCollisionListener(SpaceCollisionListener listener) {
		collisionListener=
		  SpaceCollisionEventMulticaster.remove(collisionListener, listener);
	}

    protected void fireDragStart(SpaceDragEvent e) {
  	  final SpaceDragListener l=dragListener;
  	  if(l != null) l.dragStart(e);
	}
	
    protected void fireDrag(SpaceDragEvent e) {
	  final SpaceDragListener l=dragListener;
	  if(l != null) l.drag(e);
	}
	
    protected void fireDragEnd(SpaceDragEvent e) {
	  final SpaceDragListener l=dragListener;
	  if(l != null) l.dragEnd(e);
	}
	
    protected void fireLeave(SpaceCollisionEvent e) {
	  final SpaceCollisionListener l = collisionListener;
	  if (l != null) l.leave(e);
	}
    
	protected void fireEnter(SpaceCollisionEvent e) {
	  final SpaceCollisionListener l = collisionListener;
	  if (l != null) l.enter(e);
	}

}
