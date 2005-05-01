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

import java.util.List;

import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;

/**
 * @author weissman
 *
 **/
public class EventBox extends Tool {

	double x, dx, y, dy, z, dz;
	private boolean dragging;
	private boolean isInside;
	
	int buttonId = 1;
	boolean notifyCollision = true;
	private boolean debug = false;
	
	
	
	public EventBox() {};
	
	public EventBox(double x, double dx, double y, double dy, double z, double dz) {
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

	public void processButtonPress(BoxContext context) {
		if (context.getButton() != buttonId) return; // only drag with center button
		if (checkCollision(context)) {
			dragging = true;
			if (debug) System.out.println("start dragging [EventBox]");
			fireDragStart(createDragEvent(context));
		}
	}

	public void processDrag(BoxContext context) {
		if (context.getButton() != buttonId) return; // only drag with center button
		if (!dragging) return;
		if (debug) System.out.println("dragging [EventBox]");
		fireDrag(createDragEvent(context));
	}

	public void processButtonRelease(BoxContext context) {
		if (context.getButton() != 1) return; // only drag with center button
		if (!dragging) return;
		dragging = false;
		if (debug) System.out.println("stop dragging [EventBox]");
		fireDragEnd(createDragEvent(context));
	}

	public void processMove(BoxContext context) {
		if (!notifyCollision) return;
		boolean currentCollision = checkCollision(context);
		if (isInside ^ currentCollision) {
			isInside = currentCollision;
			if(isInside)
				fireEnter(new SpaceCollisionEvent(this));
			else
				fireLeave(new SpaceCollisionEvent(this));
		}
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
	
	private boolean checkCollision(BoxContext context) {
		final double[] t = context.getLocalTransformation().getTranslation();
		if (debug) System.out.println("distance: "+(t[0]-x)+", "+(t[1]-y)+", "+(t[2]-z));
		return ( Math.abs(t[0]-x)<dx
				&& Math.abs(t[1]-y)<dy
				&& Math.abs(t[2]-z)<dz
				);
	}
		
	private SpaceDragEvent createDragEvent(BoxContext context) {
		//final double[] t = context.getLocalTransformation().getTranslation();
		return new SpaceDragEvent(this, context.getLocalTransformation());
	}
		
	public String toString() {
		return "Box: ["+x+","+y+","+z+"]\u00b1["+dx+","+dy+","+dz+"]";
	}

	public int getButtonId() {
		return buttonId;
	}
	public void setButtonId(int buttonId) {
		this.buttonId = buttonId;
	}
	public boolean isNotifyCollision() {
		return notifyCollision;
	}
	public void setNotifyCollision(boolean notifyCollision) {
		this.notifyCollision = notifyCollision;
	}

  public List getActivationSlots() {
    // TODO Auto-generated method stub
    return null;
  }

  public List getCurrentSlots() {
    // TODO Auto-generated method stub
    return null;
  }

  public List getOutputSlots() {
    // TODO Auto-generated method stub
    return null;
  }

  public void activate(ToolContext tc) {
    // TODO Auto-generated method stub
    
  }

  public void perform(ToolContext tc) {
    // TODO Auto-generated method stub
    
  }

  public void deactivate(ToolContext tc) {
    // TODO Auto-generated method stub
    
  }
}
