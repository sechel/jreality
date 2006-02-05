/*
 * Created on Dec 12, 2003
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * An immutable array. A good JIT compiler optimizes this such that access it is not slower
 * as for the pure array. The advantage is, that only the creator who provided the array is allowed to 
 * change it.
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class IntArray extends DataList {
  transient final int[] data;
  transient final int offset, length;
  /**
   * 
   */
  public IntArray(int[] data) {
    this(data, 0, data.length);
  }
  public IntArray(int[] data, int offset, int length) {
    super(StorageModel.INT_ARRAY, data, offset, length);
    this.data= data;
    this.offset=offset;
    this.length=length;
  }
  public IntArray toIntArray() {
    return this;
  }
  public final int[] toIntArray(int[] target) {
    if(target==null) target=new int[length];
    for(int src=offset, dst=0, n=length; dst<n; src++, dst++)
      target[dst]=data[src];
    return target;
  }
  /**
   * copies the containing data into a given or native ByteBuffer<br>
   * JUST FOR TESTING
   * @param bb
   * @return bb
   */
  public final ByteBuffer toNativeByteBuffer(ByteBuffer bb) {
      if(bb==null) {
          bb = (ByteBuffer)ByteBuffer.allocateDirect(length*4).order(ByteOrder.nativeOrder());      
      }
      IntBuffer target = bb.asIntBuffer();
      target.put(data, offset, length);
      return bb;
  }
  /**
   * Copies all entries of the underlying array into the <code>target</code>
   * parameter or into a new array using widening conversion for each entry.
   * Return the target array.
   */
  public final double[] toDoubleArray(double[] target) {
    if(target==null) target=new double[length];
    for(int src=offset, dst=0, n=length; dst<n; src++, dst++)
      target[dst]=data[src];
    return target;
  }
  public final int getValueAt(final int n) {
    if(n>=length) throw new ArrayIndexOutOfBoundsException();
    return data[n+offset];
  }
  public final int getLength() {
    return length;
  }
}
