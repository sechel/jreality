/*
 * Created on Apr 1, 2005
 *
 * This file is part of the de.jreality.scene.tool package.
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
package de.jreality.scene.pick;

import de.jreality.scene.SceneGraphPath;

/**
 * @author brinkman
 *
 * TODO document PickResult
 * TODO add support for picking vertices, edges, faces, etc.
 */
public interface PickResult {
  
  public static final int PICK_TYPE_POINT=4;
  public static final int PICK_TYPE_LINE=2;
  public static final int PICK_TYPE_FACE=1;
  public static final int PICK_TYPE_OBJECT=0;
  
  
	public SceneGraphPath getPickPath();
	
	/**
	 * 
	 * @return pick point in world coordinates
	 */
	public double[] getWorldCoordinates();
	
	/**
	 * 
	 * @return pick point in object coordinates
	 */
	public double[] getObjectCoordinates();

  /**
   * returns the index of the picked face/edge/point
   * @return the index or -1 if not available
   */
  public int getIndex();
  
  /**
   * returns if the type of the pick:
   * - PICK_TYPE_OBJECT
   * - PICK_TYPE_FACE
   * - PICK_TYPE_LINE
   * - PICK_TYPE_POINT
   * @return
   */
  public int getPickType();
  
  /**
   * returns texture coordinates if available.
   * @return the coordinates of null.
   */
  public double[] getTextureCoordinates();

}
