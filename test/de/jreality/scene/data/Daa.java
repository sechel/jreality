/*
 * Created on 16-Jan-2005
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
package de.jreality.scene.data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;


public abstract class Daa implements Serializable {

    final int length;

    public Daa(int length) {
        this.length = length;
    }
    
    public abstract int getLengthAt(int n);
    public abstract double getValueAt(int n, int i);
    protected abstract void setValueAt(int n, int j, double d);
  
  public void toByteBuffer(ByteBuffer bb) {
    final DoubleBuffer db = bb.asDoubleBuffer();
    for(int i = 0; i < length; i++) {
      final double slen=getLengthAt(i);
      for(int j=0; j<slen; j++)
        db.put(getValueAt(i, j));
    }
    bb.position(bb.position()+(db.position()<<3));
  }
  
    public final int getLength() {
        return length;
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof Daa)) return false;
        final Daa daa = (Daa)obj;
        if (getLength() != daa.getLength()) return false;
        for (int i = 0; i < getLength(); i++) {
            if (getLengthAt(i) != daa.getLengthAt(i)) return false;
            for (int j = 0; j < getLengthAt(i); j++)
                if (getValueAt(i,j) != daa.getValueAt(i,j)) return false;
        }
        return true;
    }
    public final void overwriteData(Daa d) {
        for (int i = 0; i < d.length; i++)
            for(int j=0; j < getLengthAt(i); j++)
                setValueAt(i, j, d.getValueAt(i, j));
    }
}
