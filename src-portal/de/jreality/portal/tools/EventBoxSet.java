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

/**
 * @author weissman
 *
 */
public class EventBoxSet extends EventBox implements SpaceCollisionListener, SpaceDragListener {
	
	int numBoxes;
	final EventBox[] boxes;
	double dx, dy, dz;
	
	HashMap indexMap = new HashMap();
	
	public EventBoxSet(int numBoxes) {
		this.numBoxes = numBoxes;
		boxes = new EventBox[numBoxes];
		init();
	}
	
	private void init() {
		for (int i = 0; i < numBoxes; i++) {
			boxes[i] = new EventBox();
			boxes[i].addSpaceCollisionListener(this);
			boxes[i].addSpaceDragListener(this);
			indexMap.put(boxes[i], new Integer(i));
		}
	}

	public EventBoxSet(int numBoxes, double d) {
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

	public EventBox getBox(int i) {
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

	public void processButtonPress(BoxContext context) {
		for (int i =0; i < numBoxes; i++) boxes[i].processButtonPress(context);
	}

	public void processDrag(BoxContext context) {
		for (int i =0; i < numBoxes; i++) boxes[i].processDrag(context);
	}

	public void processMove(BoxContext context) {
		for (int i =0; i < numBoxes; i++) boxes[i].processMove(context);
	}

	public void processButtonRelease(BoxContext context) {
		for (int i =0; i < numBoxes; i++) boxes[i].processButtonRelease(context);
	}
	
	public int getButtonId() {
		return buttonId;
	}
	public void setButtonId(int buttonId) {
		this.buttonId = buttonId;
		for (int i =0; i < numBoxes; i++) boxes[i].setButtonId(buttonId);
	}
	public boolean isNotifyCollision() {
		return notifyCollision;
	}
	public void setNotifyCollision(boolean notifyCollision) {
		this.notifyCollision = notifyCollision;
		for (int i =0; i < numBoxes; i++) boxes[i].setNotifyCollision(notifyCollision);
	}
	public String toString() {
		String ret = "BoxSet: [ " + boxes[0].toString();
		for (int i =1; i < numBoxes; i++) ret += " : "+boxes[i].toString();
		return ret += " ]";
	}

}
