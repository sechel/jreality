//JTEM - Java Tools for Experimental Mathematics
//Copyright (C) 2002 JEM-Group
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package de.jreality.scene.tool;

import java.util.EventObject;

import de.jreality.scene.PointSet;

/** The event that signals that some Point of a pointset is being dragged. */

public class PointDragEvent extends EventObject {
    
	private static final long serialVersionUID = 1984L;

  private final int index;
  private final double[] position;
	private final PointSet pointSet;
  
	public PointDragEvent(PointSet pointSet, int index, double[] position) {
		super(pointSet);
    this.pointSet=pointSet;
    this.index=index;
    this.position = (double[])position.clone();
	}
	
	/** The x-coordinate of this event's position. */
	public double getX() {
		return position[0];
	}
	
	/** The y-coordinate of this event's position. */
	public double getY() {
		return position[1];
	}
	
	/** The z-coordinate of this event's position. */
	public double getZ() {
		return position[2];
	}

  public double[] getPosition() {
    return (double[]) position.clone();
  }
  
  public int getIndex() {
    return index;
  }
  public PointSet getPointSet() {
    return pointSet;
  }
}

