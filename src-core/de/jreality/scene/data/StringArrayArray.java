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
 * @version 1.0
 * @author weissman
 *
 */
public abstract class StringArrayArray extends DataList {

  private transient final int offset, length;
  private transient final StringArray[] arrays;

  StringArrayArray(StorageModel sm, Object data) {
    this(sm, data, 0, sm.getLength(data));
  }
  StringArrayArray(StorageModel sm, Object data, int first, int num) {
    super(sm, data, first, num);
    offset=first;
    length=num;
    arrays=new StringArray[length];
  }
  public abstract String getValueAt(final int n, final int i);
  public final int getLength() {
    return super.length;
  }
  public abstract int getLengthAt(final int n);
  public abstract StringArray getValueAt(final int n);
  public final StringArrayArray toStringArrayArray() {
    return this;
  }
  public String[][] toStringArrayArray(String[][] target) {
    if(target==null) target=new String[getLength()][];
    for(int i=0, n=getLength(); i<n; i++) {
      String[] slot=target[i];
      final int slotlen=getLengthAt(i);
      if(slot==null) slot=target[i]=new String[slotlen];
      for(int j=0; j<slotlen; j++) slot[j]=getValueAt(i, j);
    }
    return target;
  }

  /**
   * This implementation of IntArrayArray stores the data as an array of 2-arrays.
   * @version 1.0
   * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
   *
   */
  public static final class Array extends StringArrayArray {
    private transient final String[][] data;

    public Array(final String[][] data) {
      super(StorageModel.STRING_ARRAY_ARRAY, data, 0, data.length);
      this.data= data;
    }

    public String getValueAt(int n, int i) {
      return data[n][i];
    }

    public int getLengthAt(int n) {
      return data[n].length;
    }

    public StringArray getValueAt(int n) {
      return subArray(n);
    }

    public DataItem item(int index) {
      return subArray(index);
    }

    private StringArray subArray(int index) {
      StringArray sarr=super.arrays[index];
      if(sarr!=null&&sarr.data==data[index]) return sarr;
      return super.arrays[index]=new StringArray(data[index]);
    }
  }
  public static final class Inlined extends StringArrayArray {
    private transient StringArray daView;
    private transient final String[] data;
    private transient final int entryLength;
    public Inlined(final String[] initialData, int numPerEntry) {
      this(initialData, numPerEntry, 0, initialData.length/numPerEntry);
    }
    public Inlined(final String[] initialData, int numPerEntry,
                   int firstEntry, int numEntries) {
      super(StorageModel.STRING_ARRAY.inlined(numPerEntry), initialData,
            firstEntry, numEntries);
      data=initialData;
      if(numPerEntry<1)
        throw new IllegalArgumentException("numPerEntry="+numPerEntry);
      entryLength=numPerEntry;
    }

    public StringArray toStringArray() {
      return daView!=null? daView: (daView=new StringArray(
        data, super.offset*entryLength, super.length*entryLength));
    }

    public int getLengthAt(int n)
    {
      return entryLength;
    }

    public String getValueAt(int n, int i)
    {
      return data[(n+super.offset)*entryLength+i];
    }

    public StringArray getValueAt(int n)
    {
      return subArray(n);
    }

    public DataItem item(int index)
    {
      return subArray(index);
    }

    private StringArray subArray(int ix) {
      StringArray sarr=super.arrays[ix];
      return (sarr!=null)? sarr: (super.arrays[ix]
        =new StringArray(data, (ix+super.offset)*entryLength, entryLength));
    }
  }
}