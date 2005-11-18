/*
 * Created on Mar 4, 2005
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

package de.jreality.reader.quake3;

import java.util.*;

import de.jreality.scene.SceneGraphComponent;

public class SceneGraphComponentSwitch extends SceneGraphComponent {

    private BitSet mask = null;

    public SceneGraphComponentSwitch() {
        super();
        myChildren = new ArrayList();
        mask = new BitSet();
    }

    List myChildren;

    private void setBitSet(BitSet bs) {
        mask.clear();
        mask.or(bs);
    }
    
    public final void applyMask(BitSet cm) {
        int added=0;
        int removed = 0;
        boolean o, n;
        for (int i=0;i<myChildren.size();i++) {
            o = mask.get(i);
            n = cm.get(i);
            if ( o != n ) {
                SceneGraphComponent child = ((SceneGraphComponent)myChildren.get(i));
                child.setVisible(n);
                if (n) added++;
                else removed++; 
            }            
        }
        setBitSet(cm);
        if (added+removed > 0) {
            System.err.println("added="+added+", removed="+removed+" currently visible: "+mask.cardinality());
        }
    }

    public void addChild(SceneGraphComponent sgc) {
        super.addChild(sgc);
        myChildren.add(sgc);
        sgc.setVisible(mask.get(myChildren.size()-1));
    }

    /**
     * removing Components is NOT supported -> UnsupportedOperationException!!
     */
    public void removeChild(SceneGraphComponent sgc) {
        throw new UnsupportedOperationException("can't remove from switch");
    }

}