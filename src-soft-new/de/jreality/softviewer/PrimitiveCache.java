/*
 * Created on 07.09.2006
 *
 * This file is part of the de.jreality.soft package.
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
package de.jreality.softviewer;

import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.scene.IndexedFaceSet;

public class PrimitiveCache {

    private static IndexedFaceSet sphere;
    private static IndexedFaceSet cylinder;
    
    private PrimitiveCache() {
        super();
        // TODO Auto-generated constructor stub
    }

    public static IndexedFaceSet getSphere() {
        if( sphere == null )
            sphere = SphereUtility.tessellatedIcosahedronSphere(4,true);
        return sphere;
    }
    public static IndexedFaceSet getCylinder() {
        if( cylinder == null )
            cylinder = Primitives.cylinder(16);
        return cylinder;
    }
    
}
