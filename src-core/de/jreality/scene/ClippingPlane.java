/*
 * Created on 29.07.2004
 *
 * This file is part of the de.jreality.scene package.
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
 * A clipping plane through the origin with normal pointing in positive z direction.
 * 
 * TODO: should that be a geometry? - appearance attribute?
 * 
 * @version 1.0
 * @author timh
 *
 */
public class ClippingPlane extends Geometry {

    public void accept(SceneGraphVisitor v) {
        v.visit(this);
    }
    static void superAccept(ClippingPlane c, SceneGraphVisitor v) {
        c.superAccept(v);
    }
    private void superAccept(SceneGraphVisitor v) {
        super.accept(v);
    }
}
