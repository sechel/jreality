/*
 * Created on Dec 3, 2004
 *
 * This file is part of the de.jreality.portal.tools package.
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

import de.jreality.scene.Transformation;
import szg.framework.event.WandEvent;
import szg.framework.event.WandListener;
import szg.framework.event.WandMotionListener;

/**
 * @author weissman
 *
 **/
public class Box implements WandMotionListener, WandListener {

	double x, dx, y, dy, z, dz;
	private boolean dragging;
	private boolean isInside;
	
	
	
	public Box() {};
	
	public Box(double x, double dx, double y, double dy, double z, double dz) {
		this.x = x;
		this.dx = dx;
		this.y = y;
		this.dy = dy;
		this.z = z;
		this.dz = dz;
	}
	
	public double getDx() {
		return dx;
	}
	public void setDx(double dx) {
		this.dx = dx;
	}
	public double getDy() {
		return dy;
	}
	public void setDy(double dy) {
		this.dy = dy;
	}
	public double getDz() {
		return dz;
	}
	public void setDz(double dz) {
		this.dz = dz;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getZ() {
		return z;
	}
	public void setZ(double z) {
		this.z = z;
	}

	public void wandDragged(WandEvent event) {
		if (event.getButton() != 1) return; // only drag with center (?) button
		if (!dragging) return;
		fireDrag(createDragEvent(event));
	}

	public void wandMoved(WandEvent event) {
		boolean currentCollision = checkCollision(event);
		if (isInside ^ currentCollision) {
			isInside = currentCollision;
			if(isInside)
				fireEnter(new SpaceCollisionEvent(this));
			else
				fireLeave(new SpaceCollisionEvent(this));
		}
	}

	public void buttonPressed(WandEvent event) {
		if (event.getButton() != 1) return; // only drag with center (?) button
		if (checkCollision(event)) {
			dragging = true;
			fireDragStart(createDragEvent(event));
		}
	}

	public void buttonReleased(WandEvent event) {
		if (event.getButton() != 1) return; // only drag with center (?) button
		if (!dragging) return;
		dragging = false;
		fireDragEnd(createDragEvent(event));
	}

	public void buttonTipped(WandEvent event) {
	}

	public void axisMoved(WandEvent event) {
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
	
	private boolean checkCollision(WandEvent event) {
		final double[] t = getTranslation(event);
		return ( Math.abs(t[0]-x)<dx
				&& Math.abs(t[1]-y)<dy
				&& Math.abs(t[2]-z)<dz
				);
	}
		
	Transformation tempTransform = new Transformation();
	
	private SpaceDragEvent createDragEvent(WandEvent event) {
		final double[] t = getTranslation(event);
		 return new SpaceDragEvent(this, t[0], t[1], t[2]);
	}
	
	private double[] getTranslation(WandEvent e) {
		 float[] m=e.getMatrix();
		 double[] t={ m[12], m[13], m[14] };
		 return t;
	}
	
	public String toString() {
		return "Box: ["+x+","+y+","+z+"]\u00b1["+dx+","+dy+","+dz+"]";
	}

}
