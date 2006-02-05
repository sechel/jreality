/*
 * Created on Dec 13, 2003
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

/**
 * An immutable array of {@link DoubleArray}s. A good JIT compiler optimizes this such that access it is not slower
 * as for the pure array. The advantage is, that only the creator who provided the array is allowed to 
 * change it. This class is abstract it leaves the implementation and especially the storage model 
 * to its subclasses.
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public abstract class DoubleArrayArray extends DataList {
  public abstract double getValueAt(final int n, final int i);
  public abstract int getLength();
  public abstract int getLengthAt(final int n);
  public abstract DoubleArray getValueAt(final int n);

  DoubleArrayArray(StorageModel sm, Object data) {
    super(sm, data);
  }
  DoubleArrayArray(StorageModel sm, Object data, int first, int num) {
    super(sm, data, first, num);
  }
  public final DoubleArrayArray toDoubleArrayArray() {
    return this;
  }
  public double[][] toDoubleArrayArray(double[][] target) {
    if(target==null) target=new double[getLength()][];
    for(int i=0, n=getLength(); i<n; i++) {
      double[] slot=target[i];
      final int slotlen=getLengthAt(i);
      if(slot==null) slot=target[i]=new double[slotlen];
      for(int j=0; j<slotlen; j++) slot[j]=getValueAt(i, j);
    }
    return target;
  }

  /**
   * This implementation of DoubleArrayArray stores the data as an array of 2-arrays.
   * @version 1.0
   * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
   *
   */
  public static final class Array extends DoubleArrayArray {
    private transient final double[][] data;
    private transient final DoubleArray[] arrays;

    public Array(final double[][] data) {
        super(StorageModel.DOUBLE_ARRAY_ARRAY, data);
        this.data=data;
        arrays=new DoubleArray[data.length];
      }

    public Array(final double[][] data, int numPerEntry) {
        super(StorageModel.DOUBLE_ARRAY.array(numPerEntry), data);
        this.data=data;
        arrays=new DoubleArray[data.length];
      }

    public double getValueAt(int n, int i) {
      return data[n][i];
    }

    public int getLength() {
      return data.length;
    }

    public int getLengthAt(int n) {
      return data[n].length;
    }

    public DoubleArray getValueAt(int n) {
      return subArray(n);
    }

    public DataItem item(int index) {
      return subArray(index);
    }

    private DoubleArray subArray(int index) {
      DoubleArray sarr=arrays[index];
      if(sarr!=null&&sarr.data==data[index]) return sarr;
      return arrays[index]=new DoubleArray(data[index]);
    }
  }
  public static final class Inlined extends DoubleArrayArray {
    private transient DoubleArray daView;
    private transient final double[] data;
    private transient final int offset, length, entryLength;
    private transient final DoubleArray[] arrays;
    public Inlined(final double[] initialData, int numPerEntry) {
      this(initialData, numPerEntry, 0, initialData.length/numPerEntry);
    }
    public Inlined(final double[] initialData, int numPerEntry,
                   int firstEntry, int numEntries) {
      super(StorageModel.DOUBLE_ARRAY.inlined(numPerEntry), initialData,
            firstEntry, numEntries);
      data=initialData;
      if(numPerEntry<1)
        throw new IllegalArgumentException("numPerEntry="+numPerEntry);
      entryLength=numPerEntry;
      offset=firstEntry;
      length= numEntries;
      arrays=new DoubleArray[length];
    }

    public DoubleArray toDoubleArray() {
      return daView!=null? daView:
        (daView=new DoubleArray(data, offset*entryLength, length*entryLength));
    }

    public final int getLength() {
      return length;
    }

    public int getLengthAt(int n)
    {
      return entryLength;
    }

    public double getValueAt(int n, int i)
    {
      return data[(n+offset)*entryLength+i];
    }

    public DoubleArray getValueAt(int n)
    {
      return subArray(n);
    }

    public DataItem item(int index)
    {
      return subArray(index);
    }

    private DoubleArray subArray(int ix) {
      DoubleArray sarr=arrays[ix];
      return (sarr==null)?
        arrays[ix]=new DoubleArray(data, (ix+offset)*entryLength, entryLength): sarr;
    }
  }
}