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

package de.jreality.portal.tools;

import de.jreality.scene.Transformation;

/** The event that signals that some scene node is being dragged. */

public class SpaceDragEvent extends SpaceIndexedEvent {
    
	private static final long serialVersionUID = 1984L;

	double posX;
	double posY;
	double posZ;
	
	Transformation transformation;
	
	public SpaceDragEvent (
		Object source,
		double x,
		double y,
		double z
	) {
		super(source);
		posX = x;
		posY = y;
		posZ = z;
	}
	
	public SpaceDragEvent(Object source, Transformation transformation) {
		super(source);
		this.transformation = transformation;
		final double[] t = transformation.getTranslation();
		posX = t[0];
		posY = t[1];
		posZ = t[2];
	}
	
	/** The x-coordinate of this event's position. */
	public double getX() {
		return posX;
	}
	
	/** The y-coordinate of this event's position. */
	public double getY() {
		return posY;
	}
	
	/** The z-coordinate of this event's position. */
	public double getZ() {
		return posZ;
	}

	public Transformation getTransformation() {
		if (transformation == null) throw new UnsupportedOperationException("transformation not set");
		return transformation;
	}
	
}

