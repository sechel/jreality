/*
 * Created on Dec 1, 2003
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
package de.jreality.scene;

/**
 * This is a directional (or parallel or distance) light. It does not decay with distance. The light direction is the z-axis.
 * other directions may be obtained by changing the transformation associated with a parent component.
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 *TODO: see @Light
 */
public class DirectionalLight extends Light {

	/**
	 * 
	 */
	public DirectionalLight() {
		super();
	}

  public void accept(SceneGraphVisitor v) {
    v.visit(this);
  }
  static void superAccept(DirectionalLight l, SceneGraphVisitor v) {
    l.superAccept(v);
  }
  private void superAccept(SceneGraphVisitor v) {
    super.accept(v);
  }
}
